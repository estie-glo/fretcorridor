import {
  afterNextRender,
  Component,
  DestroyRef,
  ElementRef,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import * as L from 'leaflet';

import { TrackingInfo, TrackingPoint } from '../../models/mission.model';

const TRACE_COLOR = '#d40f16';
const DEFAULT_CENTER: L.LatLngExpression = [6.5, 12.5];
const DEFAULT_ZOOM = 5;

@Component({
  selector: 'app-mission-tracking-map',
  imports: [TranslatePipe, DatePipe],
  templateUrl: './mission-tracking-map.component.html',
  styleUrl: './mission-tracking-map.component.scss',
})
export class MissionTrackingMapComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly translate = inject(TranslateService);

  readonly tracking = input<TrackingInfo | null>(null);
  readonly isLoading = input(false);
  readonly hasError = input(false);
  readonly lastUpdatedAt = input<Date | null>(null);

  readonly mapHost = viewChild.required<ElementRef<HTMLDivElement>>('mapHost');
  readonly hasGeoData = signal(false);
  readonly mapReady = signal(false);

  private map: L.Map | null = null;
  private layers: L.LayerGroup | null = null;

  constructor() {
    afterNextRender(() => {
      this.initMap();
      this.mapReady.set(true);
    });

    effect(() => {
      const ready = this.mapReady();
      this.tracking();
      if (!ready) {
        return;
      }
      this.renderLayers();
    });

    this.destroyRef.onDestroy(() => {
      this.map?.remove();
      this.map = null;
      this.layers = null;
    });
  }

  private initMap(): void {
    const host = this.mapHost().nativeElement;

    this.map = L.map(host, {
      zoomControl: true,
      attributionControl: true,
    }).setView(DEFAULT_CENTER, DEFAULT_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(this.map);

    this.layers = L.layerGroup().addTo(this.map);
    requestAnimationFrame(() => this.map?.invalidateSize());
  }

  private renderLayers(): void {
    if (!this.map || !this.layers) {
      return;
    }

    this.layers.clearLayers();

    const info = this.tracking();
    const points = info?.points ?? [];
    const last = info?.lastPosition ?? (points.length > 0 ? points[points.length - 1] : null);
    const bounds: L.LatLngExpression[] = [];

    if (points.length >= 2) {
      const latLngs: L.LatLngExpression[] = points.map((p) => [p.latitude, p.longitude]);
      bounds.push(...latLngs);
      this.layers.addLayer(
        L.polyline(latLngs, {
          color: TRACE_COLOR,
          weight: 4,
          opacity: 0.85,
          lineCap: 'round',
          lineJoin: 'round',
        }),
      );
    } else if (points.length === 1) {
      bounds.push([points[0].latitude, points[0].longitude]);
    }

    if (last) {
      const latLng: L.LatLngExpression = [last.latitude, last.longitude];
      bounds.push(latLng);

      const marker = L.circleMarker(latLng, {
        radius: 9,
        color: '#0a0a0a',
        weight: 2,
        fillColor: TRACE_COLOR,
        fillOpacity: 1,
      });
      marker.bindPopup(this.buildPopup(last, info?.zoneSensible));
      this.layers.addLayer(marker);
      marker.openPopup();
    }

    // Intermediate breadcrumbs (skip last — already marked)
    for (let i = 0; i < points.length - 1; i++) {
      const p = points[i];
      this.layers.addLayer(
        L.circleMarker([p.latitude, p.longitude], {
          radius: 4,
          color: TRACE_COLOR,
          weight: 1,
          fillColor: '#ffffff',
          fillOpacity: 1,
        }),
      );
    }

    this.hasGeoData.set(bounds.length > 0);

    if (bounds.length > 0) {
      this.map.fitBounds(L.latLngBounds(bounds), { padding: [40, 40], maxZoom: 10 });
    } else {
      this.map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
    }

    requestAnimationFrame(() => this.map?.invalidateSize());
  }

  private buildPopup(point: TrackingPoint, zoneSensible?: boolean): string {
    const parts: string[] = [
      `<strong>${this.escapeHtml(this.translate.instant('MISSIONS.MAP_POSITION'))}</strong>`,
    ];

    if (point.recordedAt) {
      parts.push(this.escapeHtml(point.recordedAt));
    }
    if (point.vitesseKmh !== undefined) {
      parts.push(`${point.vitesseKmh} km/h`);
    }
    if (zoneSensible) {
      parts.push(this.escapeHtml(this.translate.instant('MISSIONS.ZONE_SENSIBLE')));
    }

    return parts.join('<br/>');
  }

  private escapeHtml(value: string): string {
    return value
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;');
  }
}

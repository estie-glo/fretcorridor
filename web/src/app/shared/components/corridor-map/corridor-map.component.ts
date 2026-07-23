import {
  afterNextRender,
  Component,
  DestroyRef,
  ElementRef,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import * as L from 'leaflet';

import { AxeSummary, getAxeDisplayLabel, hasAxeGeo } from '../../models/axe.model';
import { Hub } from '../../models/hub.model';

const ACTIVATION_COLORS: Record<string, string> = {
  ACTIF: '#d40f16',
  VERROUILLE: '#b54708',
  INACTIF: '#a1a1aa',
};

const DEFAULT_CENTER: L.LatLngExpression = [6.5, 12.5];
const DEFAULT_ZOOM = 5;

@Component({
  selector: 'app-corridor-map',
  imports: [TranslatePipe],
  templateUrl: './corridor-map.component.html',
  styleUrl: './corridor-map.component.scss',
})
export class CorridorMapComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly translate = inject(TranslateService);

  readonly hubs = input<Hub[]>([]);
  readonly axes = input<AxeSummary[]>([]);
  readonly selectedAxeId = input<string | null>(null);
  readonly isLoading = input(false);
  readonly hasError = input(false);

  readonly axeSelect = output<string>();

  readonly mapHost = viewChild.required<ElementRef<HTMLDivElement>>('mapHost');

  readonly hasGeoData = signal(false);
  readonly mapReady = signal(false);

  private map: L.Map | null = null;
  private layers: L.LayerGroup | null = null;
  private polylines = new Map<string, L.Polyline>();

  constructor() {
    afterNextRender(() => {
      this.initMap();
      this.mapReady.set(true);
    });

    effect(() => {
      const ready = this.mapReady();
      this.hubs();
      this.axes();
      if (!ready) {
        return;
      }
      this.renderLayers();
      untracked(() => this.syncSelection());
    });

    effect(() => {
      this.selectedAxeId();
      if (this.mapReady()) {
        this.syncSelection();
      }
    });

    this.destroyRef.onDestroy(() => {
      this.map?.remove();
      this.map = null;
      this.layers = null;
      this.polylines.clear();
    });
  }

  activationColor(etat: string | undefined): string {
    if (!etat) {
      return ACTIVATION_COLORS['INACTIF'];
    }
    return ACTIVATION_COLORS[etat] ?? ACTIVATION_COLORS['INACTIF'];
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
    this.polylines.clear();

    const hubs = this.hubs();
    const axes = this.axes().filter(hasAxeGeo);
    const bounds: L.LatLngExpression[] = [];

    for (const hub of hubs) {
      const latLng: L.LatLngExpression = [hub.latitude, hub.longitude];
      bounds.push(latLng);

      const marker = L.circleMarker(latLng, {
        radius: 7,
        color: '#0a0a0a',
        weight: 1.5,
        fillColor: '#ffffff',
        fillOpacity: 1,
      });

      const pays = hub.pays ? `<br/><span>${this.escapeHtml(hub.pays)}</span>` : '';
      marker.bindPopup(`<strong>${this.escapeHtml(hub.nom)}</strong>${pays}`);
      this.layers.addLayer(marker);
    }

    for (const axe of axes) {
      const from: L.LatLngExpression = [axe.hubDepartLatitude!, axe.hubDepartLongitude!];
      const to: L.LatLngExpression = [axe.hubArriveeLatitude!, axe.hubArriveeLongitude!];
      bounds.push(from, to);

      const etat = axe.etatActivation ?? 'INACTIF';
      const color = this.activationColor(etat);
      const zoneSensible = !!axe.zoneSensible;

      const line = L.polyline([from, to], {
        color,
        weight: zoneSensible ? 4 : 3,
        opacity: etat === 'INACTIF' ? 0.45 : 0.9,
        dashArray: etat === 'INACTIF' ? '6 8' : undefined,
        lineCap: 'round',
        lineJoin: 'round',
      });

      const label = getAxeDisplayLabel(axe);
      const etatLabel = this.translate.instant(`AXES.ETAT_${etat}`);
      const zoneLabel = zoneSensible
        ? `<br/>${this.translate.instant('AXES.MAP_ZONE_SENSIBLE')}`
        : '';
      const trajet = [axe.hubDepart, axe.hubArrivee].filter(Boolean).join(' → ');

      line.bindPopup(
        `<strong>${this.escapeHtml(label)}</strong><br/>` +
          `${this.escapeHtml(trajet)}<br/>` +
          `${this.escapeHtml(String(etatLabel))}${zoneLabel}`,
      );

      line.on('click', () => this.axeSelect.emit(axe.id));
      this.layers.addLayer(line);
      this.polylines.set(axe.id, line);
    }

    this.hasGeoData.set(hubs.length > 0 || axes.length > 0);

    if (bounds.length > 0) {
      this.map.fitBounds(L.latLngBounds(bounds), { padding: [36, 36], maxZoom: 8 });
    } else {
      this.map.setView(DEFAULT_CENTER, DEFAULT_ZOOM);
    }

    requestAnimationFrame(() => this.map?.invalidateSize());
  }

  private syncSelection(): void {
    const selectedId = this.selectedAxeId();

    for (const [id, line] of this.polylines) {
      const axe = this.axes().find((a) => a.id === id);
      const etat = axe?.etatActivation ?? 'INACTIF';
      const color = this.activationColor(etat);
      const selected = id === selectedId;
      const zoneSensible = !!axe?.zoneSensible;

      line.setStyle({
        color,
        weight: selected ? 5 : zoneSensible ? 4 : 3,
        opacity: etat === 'INACTIF' ? 0.45 : selected ? 1 : 0.9,
        dashArray: etat === 'INACTIF' ? '6 8' : undefined,
      });

      if (selected) {
        line.bringToFront();
      }
    }

    if (selectedId) {
      const line = this.polylines.get(selectedId);
      if (line && this.map) {
        this.map.fitBounds(line.getBounds(), { padding: [48, 48], maxZoom: 8 });
        line.openPopup();
      }
    }
  }

  private escapeHtml(value: string): string {
    return value
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;');
  }
}

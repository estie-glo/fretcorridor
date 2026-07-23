import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../../core/auth/auth.service';
import { AuthError } from '../../../shared/models/auth.dto';
import { BrandLogoComponent } from '../../../shared/components/brand-logo/brand-logo.component';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';
import { environment } from '../../../../environments/environment';

interface DemoAccount {
  labelKey: string;
  telephone: string;
  pin: string;
}

interface ShowcaseSlide {
  kind: 'video' | 'image';
  src: string;
  poster?: string;
  titleKey: string;
  subtitleKey: string;
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, TranslatePipe, LanguageSwitcherComponent, BrandLogoComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private readonly showcaseVideo = viewChild<ElementRef<HTMLVideoElement>>('showcaseVideo');

  readonly errorMessage = signal<string | null>(null);
  readonly enableDemoLogin = environment.enableDemoLogin;

  readonly slides: ShowcaseSlide[] = [
    {
      kind: 'video',
      src: '/assets/login-preview.mp4',
      poster: '/assets/login-preview-poster.jpg',
      titleKey: 'AUTH.SHOWCASE_1_TITLE',
      subtitleKey: 'AUTH.SHOWCASE_1_SUBTITLE',
    },
    {
      kind: 'video',
      src: '/assets/login-preview-2.mp4',
      titleKey: 'AUTH.SHOWCASE_2_TITLE',
      subtitleKey: 'AUTH.SHOWCASE_2_SUBTITLE',
    },
    {
      kind: 'image',
      src: '/assets/login-outro.jpg',
      titleKey: 'AUTH.SHOWCASE_3_TITLE',
      subtitleKey: 'AUTH.SHOWCASE_3_SUBTITLE',
    },
  ];

  readonly slideIndex = signal(0);
  readonly currentSlide = computed(() => this.slides[this.slideIndex()]);
  readonly isVideoSlide = computed(() => this.currentSlide().kind === 'video');
  readonly isOutroSlide = computed(() => this.currentSlide().kind === 'image');

  readonly demoAccounts: DemoAccount[] = [
    { labelKey: 'AUTH.DEMO_AGENT', telephone: '+237600000001', pin: '1234' },
    { labelKey: 'AUTH.DEMO_BUREAU_TCHAD', telephone: '+235660000001', pin: '1234' },
    { labelKey: 'AUTH.DEMO_OPERATEUR', telephone: '+237600000002', pin: '1234' },
    { labelKey: 'AUTH.DEMO_CHARGEUR', telephone: '+237600000003', pin: '1234' },
  ];

  readonly form = this.fb.nonNullable.group({
    telephone: ['', [Validators.required, Validators.minLength(9)]],
    pin: ['', [Validators.required, Validators.pattern(/^\d{4,6}$/)]],
  });

  constructor() {
    effect(() => {
      const slide = this.currentSlide();
      const video = this.showcaseVideo()?.nativeElement;

      if (slide.kind !== 'video' || !video) {
        return;
      }

      if (video.getAttribute('src') !== slide.src) {
        video.src = slide.src;
        if (slide.poster) {
          video.poster = slide.poster;
        } else {
          video.removeAttribute('poster');
        }
        video.load();
      }

      void video.play().catch(() => undefined);
    });
  }

  get isSubmitting(): boolean {
    return this.authService.isLoading();
  }

  onVideoEnded(): void {
    const next = this.slideIndex() + 1;
    if (next < this.slides.length) {
      this.slideIndex.set(next);
    }
  }

  async onSubmit(): Promise<void> {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { telephone, pin } = this.form.getRawValue();
    await this.loginWithCredentials(telephone.trim(), pin);
  }

  async loginAsDemo(account: DemoAccount): Promise<void> {
    if (this.isSubmitting) {
      return;
    }

    this.form.setValue({
      telephone: account.telephone,
      pin: account.pin,
    });
    await this.loginWithCredentials(account.telephone, account.pin);
  }

  private async loginWithCredentials(telephone: string, pin: string): Promise<void> {
    this.errorMessage.set(null);

    try {
      await this.authService.login(telephone, pin);
      await this.router.navigateByUrl(this.authService.getHomeRoute());
    } catch (error) {
      if (error instanceof AuthError) {
        this.errorMessage.set(this.mapAuthErrorKey(error));
        return;
      }
      this.errorMessage.set('AUTH.ERROR_GENERIC');
    }
  }

  private mapAuthErrorKey(error: AuthError): string {
    switch (error.code) {
      case 'UTILISATEUR_INTROUVABLE':
        return 'AUTH.ERROR_UNKNOWN_USER';
      case 'PIN_INCORRECT':
        return error.tentativesRestantes === 1
          ? 'AUTH.ERROR_PIN_LAST_ATTEMPT'
          : 'AUTH.ERROR_PIN_INCORRECT';
      case 'COMPTE_BLOQUE':
        return 'AUTH.ERROR_ACCOUNT_LOCKED';
      case 'COMPTE_DESACTIVE':
        return 'AUTH.ERROR_ACCOUNT_DISABLED';
      case 'TROP_DE_TENTATIVES':
        return 'AUTH.ERROR_RATE_LIMIT';
      case 'VALIDATION_ECHOUEE':
        return 'AUTH.ERROR_VALIDATION';
      default:
        return 'AUTH.ERROR_GENERIC';
    }
  }
}

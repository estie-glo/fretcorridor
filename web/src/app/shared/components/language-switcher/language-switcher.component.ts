import { Component, inject } from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

const LANG_STORAGE_KEY = 'fc_lang';

@Component({
  selector: 'app-language-switcher',
  imports: [TranslatePipe],
  template: `
    <div class="lang-switcher" role="group" [attr.aria-label]="'LANG.FR' | translate">
      <button
        type="button"
        class="lang-switcher__btn"
        [class.lang-switcher__btn--active]="currentLang === 'fr'"
        (click)="setLanguage('fr')"
      >
        FR
      </button>
      <button
        type="button"
        class="lang-switcher__btn"
        [class.lang-switcher__btn--active]="currentLang === 'en'"
        (click)="setLanguage('en')"
      >
        EN
      </button>
    </div>
  `,
  styles: `
    .lang-switcher {
      display: inline-flex;
      border: 1px solid var(--fc-border);
      border-radius: 999px;
      overflow: hidden;
      background: var(--fc-surface);
    }

    .lang-switcher__btn {
      border: 0;
      background: transparent;
      color: var(--fc-muted);
      font: inherit;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.04em;
      padding: 0.35rem 0.75rem;
      cursor: pointer;
      transition: background 0.2s ease, color 0.2s ease;
    }

    .lang-switcher__btn--active {
      background: var(--fc-primary);
      color: var(--fc-primary-contrast);
    }

    .lang-switcher__btn:not(.lang-switcher__btn--active):hover {
      color: var(--fc-text);
      background: color-mix(in srgb, var(--fc-primary) 8%, transparent);
    }
  `,
})
export class LanguageSwitcherComponent {
  private readonly translate = inject(TranslateService);

  currentLang = this.translate.getCurrentLang() || 'fr';

  constructor() {
    const storedLang = localStorage.getItem(LANG_STORAGE_KEY);

    if (storedLang === 'fr' || storedLang === 'en') {
      this.setLanguage(storedLang);
    } else {
      this.translate.use('fr');
      this.currentLang = 'fr';
    }
  }

  setLanguage(lang: 'fr' | 'en'): void {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem(LANG_STORAGE_KEY, lang);
  }
}

import { Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-home',
  imports: [TranslatePipe],
  template: `
    <section class="home">
      <h1 class="home__title">{{ 'HOME.WELCOME' | translate }}</h1>
      <p class="home__text">{{ 'HOME.PLACEHOLDER' | translate }}</p>
    </section>
  `,
  styles: `
    .home {
      max-width: 48rem;
    }

    .home__title {
      margin: 0 0 0.5rem;
      font-size: 1.5rem;
    }

    .home__text {
      margin: 0;
      color: var(--fc-muted);
    }
  `,
})
export class HomeComponent {}

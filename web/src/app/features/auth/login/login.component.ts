import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { AuthService } from '../../../core/auth/auth.service';
import { LanguageSwitcherComponent } from '../../../shared/components/language-switcher/language-switcher.component';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, TranslatePipe, LanguageSwitcherComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    telephone: ['', [Validators.required, Validators.minLength(9)]],
    pin: ['', [Validators.required, Validators.pattern(/^\d{4,6}$/)]],
  });

  get isSubmitting(): boolean {
    return this.authService.isLoading();
  }

  async onSubmit(): Promise<void> {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { telephone, pin } = this.form.getRawValue();

    try {
      await this.authService.login(telephone.trim(), pin);
      await this.router.navigateByUrl(this.authService.getHomeRoute());
    } catch {
      this.errorMessage.set('AUTH.ERROR_INVALID');
    }
  }
}

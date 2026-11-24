import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonIcon,
  IonInput,
  IonInputPasswordToggle,
  IonNote,
  IonSpinner,
} from '@ionic/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { addIcons } from 'ionicons';
import { languageOutline, logInOutline, shieldCheckmarkOutline } from 'ionicons/icons';
import { AuthService } from '../../core/services/auth.service';
import { LanguageService } from '../../core/services/language.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    TranslatePipe,
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardSubtitle,
    IonCardContent,
    IonInput,
    IonInputPasswordToggle,
    IonButton,
    IonIcon,
    IonSpinner,
    IonNote,
  ],
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
})
export class LoginPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly lang = inject(LanguageService);

  readonly loading = signal(false);
  readonly submitted = signal(false);
  /** Translation key of the error shown above the button, or null. */
  readonly errorKey = signal<string | null>(null);

  readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
  });

  constructor() {
    addIcons({ logInOutline, shieldCheckmarkOutline, languageOutline });
  }

  /** Error visible once the field has been touched (or after pressing the button). */
  showError(field: 'username' | 'password'): boolean {
    const c = this.form.controls[field];
    return c.invalid && (c.touched || this.submitted());
  }

  submit(): void {
    this.submitted.set(true);
    this.errorKey.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/dashboard']);
      },
      error: (err: unknown) => {
        this.loading.set(false);
        const status = err instanceof HttpErrorResponse ? err.status : -1;
        // Generic message on 401: never reveal whether the user or the password failed.
        this.errorKey.set(status === 401 ? 'login.invalidCredentials' : status === 0 ? 'errors.network' : 'errors.server');
      },
    });
  }
}

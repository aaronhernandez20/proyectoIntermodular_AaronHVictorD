import { inject, Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { TranslateService } from '@ngx-translate/core';

/** Accessible toasts (Ionic announces them to screen readers). Receives translation keys. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly toast = inject(ToastController);
  private readonly translate = inject(TranslateService);

  success(key: string, params?: Record<string, unknown>): Promise<void> {
    return this.show(key, 'success', params);
  }

  error(key: string, params?: Record<string, unknown>): Promise<void> {
    return this.show(key, 'danger', params);
  }

  private async show(key: string, color: 'success' | 'danger', params?: Record<string, unknown>) {
    const message = this.translate.instant(key, params) as string;
    const t = await this.toast.create({
      message,
      color,
      duration: 4000,
      position: 'top',
      buttons: [{ text: 'OK', role: 'cancel' }],
    });
    await t.present();
  }
}

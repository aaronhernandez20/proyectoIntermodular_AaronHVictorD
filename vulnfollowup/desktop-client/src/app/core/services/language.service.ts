import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { environment } from '../../../environments/environment';

export type Lang = 'es' | 'en';
const STORAGE_KEY = 'vfu.lang';

/** Switches the UI language (ES/EN) and updates <html lang> for screen readers (WCAG 3.1.1). */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translate = inject(TranslateService);
  private readonly document = inject(DOCUMENT);

  readonly current = signal<Lang>(environment.defaultLang as Lang);

  init(): void {
    this.translate.addLangs(['es', 'en']);
    let saved: string | null = null;
    try {
      saved = localStorage.getItem(STORAGE_KEY);
    } catch {
      /* storage not available */
    }
    this.use(saved === 'en' || saved === 'es' ? saved : (environment.defaultLang as Lang));
  }

  toggle(): void {
    this.use(this.current() === 'es' ? 'en' : 'es');
  }

  use(lang: Lang): void {
    this.current.set(lang);
    this.translate.use(lang);
    this.document.documentElement.lang = lang;
    try {
      localStorage.setItem(STORAGE_KEY, lang);
    } catch {
      /* storage not available */
    }
  }
}

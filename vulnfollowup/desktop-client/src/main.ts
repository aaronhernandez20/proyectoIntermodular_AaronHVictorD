import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { inject, provideAppInitializer } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { RouteReuseStrategy, provideRouter, withComponentInputBinding, withPreloading, PreloadAllModules } from '@angular/router';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app/app.routes';
import { AppComponent } from './app/app.component';
import { authInterceptor } from './app/core/interceptors/auth.interceptor';
import { errorInterceptor } from './app/core/interceptors/error.interceptor';
import { LanguageService } from './app/core/services/language.service';

bootstrapApplication(AppComponent, {
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    provideIonicAngular({ mode: 'md' }),
    provideRouter(routes, withPreloading(PreloadAllModules), withComponentInputBinding()),
    // JWT + user-friendly errors on every request to our API
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    // ES/EN translations. useHttpBackend: the JSON files skip our interceptors
    // (avoids a circular dependency with the error interceptor, which uses translations).
    provideTranslateService({
      loader: provideTranslateHttpLoader({ prefix: './assets/i18n/', suffix: '.json', useHttpBackend: true }),
      fallbackLang: 'es',
    }),
    provideAppInitializer(() => inject(LanguageService).init()),
  ],
});

import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';
import { AuthService } from '../services/auth.service';

/**
 * Turns HTTP errors into messages the user understands.
 * 400 errors are not shown here: each form shows them next to the field.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const notify = inject(NotificationService);
  const isLogin = req.url.endsWith('/auth/login');

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        switch (err.status) {
          case 0:
            notify.error('errors.network');
            break;
          case 401:
            if (!isLogin) {
              notify.error('errors.sessionExpired');
              auth.logout();
            }
            break;
          case 403:
            notify.error('errors.forbidden');
            break;
          case 404:
            notify.error('errors.notFound');
            break;
          case 409:
            notify.error('errors.conflict');
            break;
          default:
            if (err.status >= 500) notify.error('errors.server');
        }
      }
      return throwError(() => err);
    }),
  );
};

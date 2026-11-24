import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Only lets in users with a valid session; otherwise goes to /login. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() !== null ? true : inject(Router).createUrlTree(['/login']);
};

/** Keeps a logged-in user away from /login (sends them to the dashboard). */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() === null ? true : inject(Router).createUrlTree(['/dashboard']);
};

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../../models';
import { AuthService } from '../services/auth.service';

/**
 * Guard factory: only lets in the given roles.
 * Usage in routes: canActivate: [authGuard, roleGuard('ADMIN', 'ANALYST')].
 * This is UX only: the backend returns 403 anyway if the role is not allowed.
 */
export const roleGuard = (...roles: Role[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    return auth.hasRole(...roles) ? true : inject(Router).createUrlTree(['/forbidden']);
  };
};

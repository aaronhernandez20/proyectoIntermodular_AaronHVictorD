import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

/**
 * Routes. Pages are lazy loaded (smaller initial bundle = less data transferred).
 * Role guards only adapt the UI; the backend checks the role again on every request.
 */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./components/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.page').then((m) => m.DashboardPage),
      },
      {
        path: 'vulnerabilities',
        loadComponent: () =>
          import('./pages/vulnerabilities/vulnerabilities.page').then((m) => m.VulnerabilitiesPage),
      },
      {
        path: 'assets',
        loadComponent: () => import('./pages/assets/assets.page').then((m) => m.AssetsPage),
      },
      {
        path: 'providers',
        loadComponent: () => import('./pages/providers/providers.page').then((m) => m.ProvidersPage),
      },
      {
        path: 'import',
        canActivate: [authGuard, roleGuard('ADMIN', 'ANALYST')],
        loadComponent: () => import('./pages/import/import.page').then((m) => m.ImportPage),
      },
      {
        path: 'forbidden',
        loadComponent: () => import('./pages/forbidden/forbidden.page').then((m) => m.ForbiddenPage),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

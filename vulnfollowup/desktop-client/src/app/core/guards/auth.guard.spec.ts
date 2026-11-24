import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Role } from '../../models';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

class FakeAuth {
  constructor(public currentRole: Role | null) {}
  token = () => (this.currentRole ? 'token' : null);
  hasRole = (...roles: Role[]) => this.currentRole !== null && roles.includes(this.currentRole);
}

function run(guard: ReturnType<typeof roleGuard>, role: Role | null) {
  TestBed.configureTestingModule({
    providers: [provideRouter([]), { provide: AuthService, useValue: new FakeAuth(role) }],
  });
  return TestBed.runInInjectionContext(() =>
    guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
  );
}

describe('Route guards', () => {
  const url = (tree: unknown) => TestBed.inject(Router).serializeUrl(tree as UrlTree);

  it('authGuard sends anonymous users to /login', () => {
    expect(url(run(authGuard, null))).toBe('/login');
  });

  it('authGuard lets logged-in users in', () => {
    expect(run(authGuard, 'VIEWER')).toBe(true);
  });

  it('roleGuard lets ADMIN and ANALYST into the import page', () => {
    expect(run(roleGuard('ADMIN', 'ANALYST'), 'ANALYST')).toBe(true);
  });

  it('roleGuard sends a VIEWER to /forbidden', () => {
    expect(url(run(roleGuard('ADMIN', 'ANALYST'), 'VIEWER'))).toBe('/forbidden');
  });
});

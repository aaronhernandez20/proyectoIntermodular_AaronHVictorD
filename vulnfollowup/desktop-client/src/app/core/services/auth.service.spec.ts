import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../../models';
import { AuthService, mainRole } from './auth.service';

/** Same shape as the real backend (docs/api/README.md, section 1). */
const response = (overrides: Partial<LoginResponse> = {}): LoginResponse => ({
  accessToken: 'test-token',
  tokenType: 'Bearer',
  expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
  username: 'analyst',
  fullName: 'Ana Analista',
  roles: ['ANALYST'],
  ...overrides,
});

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('starts without a session', () => {
    expect(service.isLoggedIn()).toBe(false);
    expect(service.token()).toBeNull();
  });

  it('sends the credentials to POST /api/auth/login and stores the session', () => {
    service.login({ username: 'analyst', password: 'analyst123' }).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'analyst', password: 'analyst123' });
    req.flush(response());

    expect(service.isLoggedIn()).toBe(true);
    expect(service.token()).toBe('test-token');
    expect(service.user()).toEqual({ username: 'analyst', fullName: 'Ana Analista', role: 'ANALYST' });
    expect(service.canEdit()).toBe(true);
    expect(service.isAdmin()).toBe(false);
  });

  it('never stores the password', () => {
    service.login({ username: 'analyst', password: 'analyst123' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(response());
    expect(sessionStorage.getItem('vfu.session')).not.toContain('analyst123');
  });

  it('treats an expired token as no session', () => {
    service.login({ username: 'analyst', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(response({ expiresAt: '2020-01-01T00:00:00Z' }));
    expect(service.token()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('clears the session on logout', () => {
    service.login({ username: 'analyst', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(response());
    service.logout(false);
    expect(service.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('vfu.session')).toBeNull();
  });
});

describe('mainRole', () => {
  it('keeps the most powerful role and ignores the ROLE_ prefix', () => {
    expect(mainRole(['VIEWER', 'ADMIN'])).toBe('ADMIN');
    expect(mainRole(['ROLE_ANALYST'])).toBe('ANALYST');
    expect(mainRole(['viewer'])).toBe('VIEWER');
    expect(mainRole(['SOMETHING'])).toBeNull();
  });
});

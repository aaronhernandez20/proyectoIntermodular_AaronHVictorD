import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginResponse } from '../../models';
import { AuthService } from './auth.service';

const RESPONSE: LoginResponse = {
  token: 'test-token',
  expiresIn: 3600,
  user: { id: 2, username: 'analyst', fullName: 'Ana Analyst', role: 'ANALYST' },
};

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

  it('sends the credentials to POST /auth/login and stores the session', () => {
    service.login({ username: 'analyst', password: 'Analyst123!' }).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'analyst', password: 'Analyst123!' });
    req.flush(RESPONSE);

    expect(service.isLoggedIn()).toBe(true);
    expect(service.token()).toBe('test-token');
    expect(service.role()).toBe('ANALYST');
    expect(service.canEdit()).toBe(true);
    expect(service.isAdmin()).toBe(false);
  });

  it('never stores the password', () => {
    service.login({ username: 'analyst', password: 'Analyst123!' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(RESPONSE);
    expect(sessionStorage.getItem('vfu.session')).not.toContain('Analyst123!');
  });

  it('treats an expired token as no session', () => {
    service.login({ username: 'analyst', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ ...RESPONSE, expiresIn: -1 });
    expect(service.token()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('clears the session on logout', () => {
    service.login({ username: 'analyst', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(RESPONSE);
    service.logout(false);
    expect(service.isLoggedIn()).toBe(false);
    expect(sessionStorage.getItem('vfu.session')).toBeNull();
  });
});

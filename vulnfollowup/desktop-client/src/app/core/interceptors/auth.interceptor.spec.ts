import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { token: () => 'abc.def.ghi' } },
      ],
    });
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
  });

  it('adds the Bearer token to requests to our API', () => {
    http.get(`${environment.apiUrl}/vulnerabilities`).subscribe();
    const req = ctrl.expectOne(`${environment.apiUrl}/vulnerabilities`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc.def.ghi');
  });

  it('never sends the token to other hosts', () => {
    http.get('https://example.org/data').subscribe();
    const req = ctrl.expectOne('https://example.org/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });
});

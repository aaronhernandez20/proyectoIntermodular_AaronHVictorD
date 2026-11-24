import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Role, ROLES, User } from '../../models';

interface StoredSession {
  token: string;
  user: User;
  /** Epoch milliseconds. */
  expiresAt: number;
}

const STORAGE_KEY = 'vfu.session';

/**
 * The backend returns a list of roles (e.g. ["ANALYST"], or "ROLE_ANALYST" with Spring's prefix).
 * The web keeps the most powerful one: ADMIN > ANALYST > VIEWER.
 */
export function mainRole(roles: string[]): Role | null {
  const clean = roles.map((r) => r.replace(/^ROLE_/, '').toUpperCase());
  return ROLES.find((r) => clean.includes(r)) ?? null;
}

/**
 * Keeps the session (JWT + user) in a signal.
 * It is stored in sessionStorage (not localStorage) so it disappears when the browser closes.
 * The role is only used to adapt the UI: the backend always checks permissions again.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<StoredSession | null>(this.restore());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly role = computed<Role | null>(() => this.session()?.user.role ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly canEdit = computed(() => this.hasRole('ADMIN', 'ANALYST'));
  readonly isAdmin = computed(() => this.hasRole('ADMIN'));

  /** POST /api/auth/login */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap((res) => {
        const role = mainRole(res.roles);
        if (!role) throw new Error('The user has no known role');
        const stored: StoredSession = {
          token: res.accessToken,
          user: { username: res.username, fullName: res.fullName, role },
          expiresAt: Date.parse(res.expiresAt),
        };
        this.session.set(stored);
        this.save(stored);
      }),
    );
  }

  logout(redirect = true): void {
    this.session.set(null);
    try {
      sessionStorage.removeItem(STORAGE_KEY);
    } catch {
      /* storage not available */
    }
    if (redirect) void this.router.navigate(['/login']);
  }

  /** Current token, or null if there is no session or it has expired. */
  token(): string | null {
    const s = this.session();
    if (!s) return null;
    if (!(Date.now() < s.expiresAt)) {
      this.logout(false);
      return null;
    }
    return s.token;
  }

  hasRole(...roles: Role[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  private restore(): StoredSession | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as StoredSession;
      return Date.now() < parsed.expiresAt ? parsed : null;
    } catch {
      return null;
    }
  }

  private save(s: StoredSession): void {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(s));
    } catch {
      /* storage not available: the session lives only in memory */
    }
  }
}

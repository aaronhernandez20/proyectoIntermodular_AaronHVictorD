import { Role } from './enums.model';

/** Logged-in user as the web keeps it (one main role). */
export interface User {
  username: string;
  fullName: string;
  role: Role;
}

/** Short user reference inside other resources (e.g. the assignee of a vulnerability). */
export interface UserRef {
  id: number;
  username: string;
  fullName: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

/** POST /api/auth/login → 200 */
export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  /** ISO-8601 instant, e.g. "2026-11-24T20:15:00Z". */
  expiresAt: string;
  username: string;
  fullName: string;
  roles: string[];
}

/** GET /api/auth/me */
export interface MeResponse {
  username: string;
  fullName: string;
  roles: string[];
}

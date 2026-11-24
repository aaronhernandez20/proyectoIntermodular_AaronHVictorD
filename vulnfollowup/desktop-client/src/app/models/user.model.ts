import { Role } from './enums.model';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: Role;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  /** Seconds until the token expires. */
  expiresIn: number;
  user: User;
}

import type { User, LoginRequest, SignUpRequest } from '../types/user';
import { api } from './client';

export const login = (credentials: LoginRequest): Promise<User> =>
  api<User>('/api/auth/login', { method: 'POST', body: JSON.stringify(credentials) });

export const signUp = (data: SignUpRequest): Promise<string> =>
  api<string>('/api/auth/signup', { method: 'POST', body: JSON.stringify(data) });

export const logout = (): Promise<void> =>
  api<void>('/api/auth/logout', { method: 'POST' });

export const currentUser = (): Promise<User> => api<User>('/api/auth/me');

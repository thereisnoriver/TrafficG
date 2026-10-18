// All requests are same-origin; Vite proxies /api to Spring Boot locally.
// Spring Security requires a CSRF token on state-changing requests.
let csrfToken: string | null = null;

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = (init.method || 'GET').toUpperCase();
  const headers = new Headers(init.headers);
  if (init.body) headers.set('Content-Type', 'application/json');
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    if (!csrfToken) {
      const response = await fetch('/api/auth/csrf', { credentials: 'include' });
      if (!response.ok) throw new Error('Unable to initialize a secure session');
      csrfToken = (await response.json()).token as string;
    }
    headers.set('X-XSRF-TOKEN', csrfToken!);
  }
  const response = await fetch(path, { ...init, headers, credentials: 'include' });
  if (!response.ok) {
    let reason = response.statusText;
    try {
      const body = await response.json();
      reason = body.detail || body.message || body.error || reason;
    } catch { /* Error body may be empty. */ }
    throw new Error(response.status + ': ' + reason);
  }
  if (response.status === 204) return undefined as T;
  const raw = await response.text();
  if (!raw) return undefined as T;
  try { return JSON.parse(raw) as T; }
  catch { return raw as T; }
}

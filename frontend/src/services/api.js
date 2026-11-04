const API_BASE = import.meta.env.VITE_API_BASE || '/api';

let csrfToken = sessionStorage.getItem('csrf_token') || '';

export function setCsrfToken(token) {
  csrfToken = token || '';
  if (token) {
    sessionStorage.setItem('csrf_token', token);
  } else {
    sessionStorage.removeItem('csrf_token');
  }
}

export async function apiRequest(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method) && csrfToken) {
    headers['X-CSRF-Token'] = csrfToken;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    credentials: 'include',
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  const json = await res.json().catch(() => ({
    ok: false,
    error: 'Unexpected server response. Please make sure Apache & MySQL are running in XAMPP.',
  }));

  if (!res.ok || !json.ok) {
    throw new Error(json.error || `Request failed (${res.status})`);
  }

  return json;
}

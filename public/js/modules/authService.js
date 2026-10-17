export async function apiLogin(email, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  return res.json();
}

export function saveSession(token, user) {
  localStorage.setItem('lead360_token', token);
  localStorage.setItem('lead360_user', JSON.stringify(user));
}

export function getSession() {
  const token = localStorage.getItem('lead360_token');
  const userStr = localStorage.getItem('lead360_user');
  if (!token || !userStr) return null;
  try {
    return { token, user: JSON.parse(userStr) };
  } catch (e) {
    return null;
  }
}

export function clearSession() {
  localStorage.removeItem('lead360_token');
  localStorage.removeItem('lead360_user');
}

const API = 'http://localhost:5000';

export function saveToken(token) { localStorage.setItem('pp_token', token); }
export function getToken() { return localStorage.getItem('pp_token'); }
export function removeToken() {
  localStorage.removeItem('pp_token');
  localStorage.removeItem('pp_role');
}

export function getRole() {
  return localStorage.getItem('pp_role');
}

export async function apiFetch(path, opts = {}) {
  opts.headers = opts.headers || {};
  const token = getToken();
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(API + path, opts);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Request failed');
  }
  return res.json();
}

export async function loginUser(email, password) {
  const res = await fetch(API + '/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  return res.json();
}

// Initialize auth-aware navigation (login/admin/logout links) on each page
export function initAuthUI() {
  const token = getToken();
  const role = getRole();

  const loginLink = document.getElementById('loginLink');
  const registerLink = document.getElementById('registerLink');
  const logoutLink = document.getElementById('logoutLink');
  const adminLink = document.getElementById('adminLink');

  if (token) {
    if (loginLink) loginLink.style.display = 'none';
    if (registerLink) registerLink.style.display = 'none';
    if (logoutLink) logoutLink.style.display = 'inline-block';
    if (adminLink) {
      adminLink.style.display = role === 'admin' ? 'inline-block' : 'none';
    }
    if (logoutLink) {
      logoutLink.addEventListener('click', (e) => {
        e.preventDefault();
        removeToken();
        window.location.href = 'index.html';
      });
    }
  } else {
    if (logoutLink) logoutLink.style.display = 'none';
    if (adminLink) adminLink.style.display = 'none';
    if (registerLink) registerLink.style.display = 'inline-block';
    if (loginLink) loginLink.style.display = 'inline-block';
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('DOMContentLoaded', initAuthUI);
}

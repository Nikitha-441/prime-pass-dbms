const API = 'http://localhost:5000';
const TOKEN_KEY = 'pp_token';
const ROLE_KEY = 'pp_role';

function setStorage(key, value, remember) {
  if (remember) {
    localStorage.setItem(key, value);
    sessionStorage.removeItem(key);
  } else {
    sessionStorage.setItem(key, value);
    localStorage.removeItem(key);
  }
}

export function saveToken(token, remember = true) {
  if (!token) return;
  setStorage(TOKEN_KEY, token, remember);
}

export function saveRole(role, remember = true) {
  if (!role) return;
  setStorage(ROLE_KEY, role, remember);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY) || sessionStorage.getItem(ROLE_KEY);
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ROLE_KEY);
  sessionStorage.removeItem(ROLE_KEY);
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

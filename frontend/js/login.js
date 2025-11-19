import { loginUser, saveToken, saveRole } from './app.js';

const loginForm = document.getElementById('loginForm');

loginForm?.addEventListener('submit', async (e) => {
  e.preventDefault();

  const formData = new FormData(loginForm);
  const email = (formData.get('email') || '').trim();
  const password = (formData.get('password') || '').trim();

  if (!email || !password) {
    alert('Please fill in both email and password.');
    return;
  }

  try {
    const result = await loginUser(email, password);
    if (!result.success) {
      alert(result.message || 'Login failed');
      return;
    }

    saveToken(result.token, true);
    const user = result.user;
    if (user && user.role) {
      saveRole(user.role, true);
    }

    if (user.role === 'admin') {
      window.location.href = 'admin.html';
    } else {
      const pending = localStorage.getItem('pp_pending');
      window.location.href = pending ? 'payment.html' : 'events.html';
    }
  } catch (err) {
    console.error(err);
    alert('Unable to login right now. Please try again.');
  }
});

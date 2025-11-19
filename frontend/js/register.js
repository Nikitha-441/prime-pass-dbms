import { saveToken } from './app.js';

const form = document.getElementById('registerForm');
const messageBox = document.getElementById('registerMessage');

function showMessage(text, type = 'error') {
  if (!messageBox) return;
  messageBox.textContent = text;
  messageBox.className = type === 'success' ? 'success-message' : 'error-message';
}

form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const data = new FormData(form);
  const name = (data.get('name') || '').trim();
  const email = (data.get('email') || '').trim();
  const phone = (data.get('phone') || '').trim();
  const password = (data.get('password') || '').trim();
  const confirm = (data.get('confirm') || '').trim();

  if (!name || !email || !password || !confirm) {
    showMessage('Please fill in all required fields.');
    return;
  }
  if (password.length < 6) {
    showMessage('Password must be at least 6 characters long.');
    return;
  }
  if (password !== confirm) {
    showMessage('Passwords do not match.');
    return;
  }

  try {
    const res = await fetch('http://localhost:5000/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password, phone })
    });
    const result = await res.json();
    if (!result.success) {
      showMessage(result.message || 'Signup failed.');
      return;
    }

    showMessage('Account created! Redirecting...', 'success');
    saveToken(result.token);
    if (result.user && result.user.role) {
      localStorage.setItem('pp_role', result.user.role);
    }
    setTimeout(() => {
      if (result.user?.role === 'admin') {
        window.location.href = 'admin.html';
      } else {
        window.location.href = 'events.html';
      }
    }, 800);
  } catch (err) {
    console.error(err);
    showMessage('Unable to sign up right now.');
  }
});


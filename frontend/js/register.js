import { saveToken, saveRole } from './app.js';

const form = document.getElementById('registerForm');
const messageBox = document.getElementById('registerMessage');
const submitBtn = document.getElementById('registerSubmit');
const staySignedIn = document.getElementById('staySignedIn');

function showMessage(text, type = 'error') {
  if (!messageBox) return;
  messageBox.textContent = text;
  messageBox.className = type === 'success' ? 'success-message' : 'error-message';
}

function getRole() {
  const selected = form.querySelector('input[name="role"]:checked');
  return selected ? selected.value : 'user';
}

form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const data = new FormData(form);
  const name = (data.get('name') || '').trim();
  const email = (data.get('email') || '').trim();
  const password = (data.get('password') || '').trim();
  const confirm = (data.get('confirm') || '').trim();
  const role = getRole();
  const remember = staySignedIn ? staySignedIn.checked : true;

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

  submitBtn?.setAttribute('disabled', 'disabled');
  showMessage('Creating your account...', 'success');

  try {
    const res = await fetch('http://localhost:5000/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password, role })
    });
    const result = await res.json();
    if (!result.success) {
      showMessage(result.message || 'Signup failed.');
      submitBtn?.removeAttribute('disabled');
      return;
    }

    showMessage(`Welcome ${result.user?.name || ''}! Redirecting you now...`, 'success');
    saveToken(result.token, remember);
    if (result.user && result.user.role) {
      saveRole(result.user.role, remember);
    }
    setTimeout(() => {
      if (result.user?.role === 'admin') {
        window.location.href = 'admin.html';
      } else {
        window.location.href = 'events.html';
      }
    }, 900);
  } catch (err) {
    console.error(err);
    showMessage('Unable to sign up right now.');
    submitBtn?.removeAttribute('disabled');
  }
});


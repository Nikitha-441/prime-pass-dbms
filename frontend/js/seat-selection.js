import { apiFetch, getToken } from './app.js';

const params = new URLSearchParams(window.location.search);
const showtime_id = params.get('showtime_id');
const seatGrid = document.getElementById('seatGrid');
const continueBtn = document.getElementById('continueBtn');
let selected = [];

async function loadSeats() {
  try {
    // Backend expects showtimeId as the request parameter name
    const seats = await apiFetch('/seats?showtimeId=' + showtime_id);
    seatGrid.innerHTML = '';
    seats.forEach(s => {
      const seatBtn = document.createElement('button');
      seatBtn.textContent = s.seatNumber;
      seatBtn.dataset.seatId = s.seatId;

      // Base seat styling + status state
      seatBtn.className = 'seat';
      if (s.status === 'booked') {
        seatBtn.classList.add('booked');
        seatBtn.disabled = true;
      } else if (s.status === 'blocked') {
        seatBtn.classList.add('blocked');
        seatBtn.disabled = true;
      } else {
        seatBtn.classList.add('available');
      }

      seatBtn.addEventListener('click', () => {
        const id = s.seatId;
        if (selected.includes(id)) {
          selected = selected.filter(x => x !== id);
          seatBtn.classList.remove('selected');
        } else {
          selected.push(id);
          seatBtn.classList.add('selected');
        }
      });

      seatGrid.appendChild(seatBtn);
    });
  } catch (err) {
    console.error(err);
    seatGrid.innerHTML = '<p>Error loading seats</p>';
  }
}

continueBtn?.addEventListener('click', async () => {
  if (!selected.length) {
    alert('Select seats');
    return;
  }
  const token = getToken();
  if (!token) {
    // Save selection in localStorage and redirect to login
    localStorage.setItem('pp_pending', JSON.stringify({ showtime_id, seats: selected }));
    window.location.href = 'login.html';
    return;
  }
  // Proceed to mock payment page with selection
  localStorage.setItem('pp_pending', JSON.stringify({ showtime_id, seats: selected }));
  window.location.href = 'payment.html';
});

loadSeats();

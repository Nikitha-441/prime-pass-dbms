import { apiFetch, getToken } from './app.js';

const subtotalEl = document.querySelector('.payment-row:nth-child(2) span:last-child');
const totalEl = document.querySelector('.payment-row.total span:last-child');
const form = document.querySelector('form');
let pendingBooking = null;

async function loadPayment() {
  try {
    // Get pending booking from localStorage
    const pending = localStorage.getItem('pp_pending');
    if (!pending) {
      window.location.href = 'events.html';
      return;
    }

    pendingBooking = JSON.parse(pending);
    const token = getToken();
    
    if (!token) {
      window.location.href = 'login.html';
      return;
    }

    // Get showtime details to calculate price
    const showtimeId = Number(pendingBooking.showtimeId ?? pendingBooking.showtime_id);
    if (!showtimeId) {
      alert('Missing showtime information.');
      window.location.href = 'events.html';
      return;
    }
    const seats = (pendingBooking.seats || []).map(Number).filter(Boolean);
    if (!seats.length) {
      alert('No seats selected for this booking.');
      window.location.href = 'events.html';
      return;
    }
    
    // Fetch showtime details to get price
    // Try the showtime endpoint, fallback to finding it from events
    let showtime;
    try {
        showtime = await apiFetch(`/events/showtimes/${showtimeId}`);
    } catch (e) {
        // Fallback: use stored price or default
        const storedPrice = localStorage.getItem(`showtime_${showtimeId}_price`);
        showtime = { price: storedPrice ? parseFloat(storedPrice) : 300 };
    }
    const pricePerSeat = showtime.price || 300;
    
    const subtotal = pricePerSeat * seats.length;
    const serviceFee = 0;
    const total = subtotal + serviceFee;

    subtotalEl.textContent = `₹${subtotal}`;
    totalEl.textContent = `₹${total}`;
  } catch (err) {
    console.error(err);
    alert('Error loading payment details');
  }
}

form?.addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const token = getToken();
  if (!token) {
    window.location.href = 'login.html';
    return;
  }

  if (!pendingBooking) {
    alert('No booking data found');
    return;
  }

  try {
    const result = await apiFetch('/bookings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        showtimeId,
        seatIds: seats,
        paymentMethod: 'CreditCard' // Mock payment
      })
    });

    if (result.success) {
      // Clear pending booking
      localStorage.removeItem('pp_pending');
      // Redirect to tickets page
      alert(`Booking successful! Booking Code: ${result.bookingCode}`);
      window.location.href = 'tickets.html';
    } else {
      alert('Booking failed: ' + result.message);
    }
  } catch (err) {
    console.error(err);
    alert('Payment failed. Please try again.');
  }
});

loadPayment();


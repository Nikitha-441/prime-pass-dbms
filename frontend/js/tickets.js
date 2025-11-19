import { apiFetch, getToken } from './app.js';

const ticketList = document.getElementById('ticketList');

async function loadTickets() {
  try {
    const token = getToken();
    if (!token) {
      ticketList.innerHTML = '<p>Please <a href="login.html">login</a> to view your tickets.</p>';
      return;
    }

    const bookings = await apiFetch('/bookings/history');
    
    if (bookings.length === 0) {
      ticketList.innerHTML = '<p>No bookings found. <a href="events.html">Browse events</a></p>';
      return;
    }

    ticketList.innerHTML = '';
    bookings.forEach(booking => {
      const card = document.createElement('div');
      card.className = 'card';
      const date = new Date(booking.bookingTime);
      card.innerHTML = `
        <h3>${booking.eventName}</h3>
        <p><strong>Booking Code:</strong> ${booking.bookingCode}</p>
        <p><strong>Category:</strong> ${booking.categoryName}</p>
        <p><strong>Show Date:</strong> ${new Date(booking.showDate).toLocaleString()}</p>
        <p><strong>Venue:</strong> ${booking.venueName}</p>
        <p><strong>Seats:</strong> ${booking.seats}</p>
        <p><strong>Amount:</strong> ₹${booking.totalAmount}</p>
        <p><strong>Status:</strong> ${booking.paymentStatus}</p>
        <p><strong>Booked on:</strong> ${date.toLocaleString()}</p>
      `;
      ticketList.appendChild(card);
    });
  } catch (err) {
    console.error(err);
    ticketList.innerHTML = '<p>Error loading tickets. Please try again.</p>';
  }
}

loadTickets();


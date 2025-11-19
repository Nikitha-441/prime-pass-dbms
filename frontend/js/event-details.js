import { apiFetch } from './app.js';
const params = new URLSearchParams(window.location.search);
const eventId = params.get('event_id');
const eventBox = document.getElementById('eventDetails');
const stContainer = document.getElementById('showtimesList');

async function load() {
  try {
    const data = await apiFetch('/events/' + eventId);
    const e = data.event;
    eventBox.innerHTML = `
      <h2>${e.eventName}</h2>
      <p>${e.description || ''}</p>
      <p><strong>Organizer:</strong> ${e.organizerName}</p>
      <p><strong>Category:</strong> ${e.categoryName}</p>
      <p><strong>Base Price (from):</strong> ₹${e.basePrice}</p>
    `;
    stContainer.innerHTML = '';
    data.showtimes.forEach(st => {
      const card = document.createElement('div');
      card.className = 'card';
      const showDate = new Date(st.showDate).toLocaleString();
      card.innerHTML = `
        <h4>${showDate}</h4>
        <p>Venue: ${st.venueName}</p>
        <p><strong>Price per seat:</strong> ₹${st.price}</p>
        <a class="btn" href="seat-selection.html?showtime_id=${st.showtimeId}">Select Seats</a>
      `;
      stContainer.appendChild(card);
    });
  } catch (err) {
    console.error(err);
    eventBox.innerHTML = '<p>Error loading event</p>';
  }
}

load();

import { apiFetch, getToken, removeToken } from './app.js';

// Basic client-side guard: require token and admin role (backend still enforces admin role)
const token = getToken();
const role = localStorage.getItem('pp_role');
if (!token || role !== 'admin') {
  window.location.href = 'login.html';
}

const tabs = document.querySelectorAll('.tabs button');
const tabSections = document.querySelectorAll('.admin-tab');

const eventForm = document.getElementById('eventForm');
const resetEventFormBtn = document.getElementById('resetEventForm');
const adminEventsList = document.getElementById('adminEventsList');

const showtimeForm = document.getElementById('showtimeForm');
const showtimeEditForm = document.getElementById('showtimeEditForm');
const deleteShowtimeBtn = document.getElementById('deleteShowtimeBtn');

const seatsShowtimeIdInput = document.getElementById('seatsShowtimeId');
const loadSeatsBtn = document.getElementById('loadSeatsBtn');
const adminSeatGrid = document.getElementById('adminSeatGrid');

const adminBookingsList = document.getElementById('adminBookingsList');
const logoutLink = document.getElementById('logoutLink');

// ----- Tab handling -----
tabs.forEach(btn => {
  btn.addEventListener('click', () => {
    const target = btn.getAttribute('data-tab');
    tabSections.forEach(sec => {
      sec.style.display = sec.id === target ? 'block' : 'none';
    });
  });
});

// ----- Event Management -----
async function loadAdminEvents() {
  try {
    // Admin events view exposes extra aggregates via SQL view
    const events = await apiFetch('/events'); // Using same endpoint for simplicity
    adminEventsList.innerHTML = '';
    events.forEach(ev => {
      const card = document.createElement('div');
      card.className = 'card';
      card.innerHTML = `
        <h4>${ev.eventName}</h4>
        <p>${ev.description || ''}</p>
        <p><strong>Category:</strong> ${ev.categoryName}</p>
        <p><strong>Base Price:</strong> ₹${ev.basePrice}</p>
        <div class="admin-card-actions">
          <button class="btn btn-outline" data-action="edit" data-id="${ev.eventId}">Edit</button>
          <button class="btn btn-outline" data-action="delete" data-id="${ev.eventId}">Delete</button>
        </div>
      `;
      adminEventsList.appendChild(card);
    });
  } catch (err) {
    console.error(err);
    adminEventsList.innerHTML = '<p>Failed to load events. Make sure you are logged in as admin.</p>';
  }
}

eventForm?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(eventForm);
  const body = {
    eventName: formData.get('eventName'),
    description: formData.get('description'),
    basePrice: Number(formData.get('basePrice')),
    organizerId: Number(formData.get('organizerId')),
    categoryId: Number(formData.get('categoryId')),
  };
  const eventId = formData.get('eventId');

  try {
    if (eventId) {
      await apiFetch(`/events/${eventId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
    } else {
      await apiFetch('/events', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
    }
    eventForm.reset();
    await loadAdminEvents();
  } catch (err) {
    console.error(err);
    alert('Failed to save event. Check console for details.');
  }
});

resetEventFormBtn?.addEventListener('click', () => {
  eventForm.reset();
  eventForm.querySelector('input[name="eventId"]').value = '';
});

adminEventsList?.addEventListener('click', async (e) => {
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;
  const id = btn.getAttribute('data-id');
  const action = btn.getAttribute('data-action');

  if (action === 'edit') {
    try {
      const detail = await apiFetch(`/events/${id}`);
      const ev = detail.event;
      eventForm.querySelector('input[name="eventId"]').value = ev.eventId;
      eventForm.querySelector('input[name="eventName"]').value = ev.eventName;
      eventForm.querySelector('textarea[name="description"]').value = ev.description || '';
      eventForm.querySelector('input[name="basePrice"]').value = ev.basePrice;
      // Organizer and category are required; keep previous or default 1
      eventForm.querySelector('input[name="organizerId"]').value = ev.organizerId || 1;
      eventForm.querySelector('input[name="categoryId"]').value = ev.categoryId || 1;
    } catch (err) {
      console.error(err);
      alert('Failed to load event details for editing.');
    }
  } else if (action === 'delete') {
    if (!confirm('Delete this event and its showtimes?')) return;
    try {
      await apiFetch(`/events/${id}`, { method: 'DELETE' });
      await loadAdminEvents();
    } catch (err) {
      console.error(err);
      alert('Failed to delete event.');
    }
  }
});

// ----- Showtime Management -----
showtimeForm?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(showtimeForm);
  const eventId = Number(formData.get('eventId'));
  const payload = {
    venueId: Number(formData.get('venueId')),
    showDate: new Date(formData.get('showDate')).toISOString(),
    price: Number(formData.get('price')),
  };

  try {
    await apiFetch(`/events/${eventId}/showtimes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    alert('Showtime created.');
    showtimeForm.reset();
  } catch (err) {
    console.error(err);
    alert('Failed to create showtime.');
  }
});

showtimeEditForm?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const formData = new FormData(showtimeEditForm);
  const showtimeId = Number(formData.get('showtimeId'));
  const payload = {
    venueId: Number(formData.get('venueId')),
    showDate: new Date(formData.get('showDate')).toISOString(),
    price: Number(formData.get('price')),
  };

  try {
    await apiFetch(`/events/showtimes/${showtimeId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    alert('Showtime updated.');
  } catch (err) {
    console.error(err);
    alert('Failed to update showtime.');
  }
});

deleteShowtimeBtn?.addEventListener('click', async () => {
  const formData = new FormData(showtimeEditForm);
  const showtimeId = Number(formData.get('showtimeId'));
  if (!showtimeId) {
    alert('Enter a valid showtime ID.');
    return;
  }
  if (!confirm('Delete this showtime?')) return;

  try {
    await apiFetch(`/events/showtimes/${showtimeId}`, { method: 'DELETE' });
    alert('Showtime deleted.');
    showtimeEditForm.reset();
  } catch (err) {
    console.error(err);
    alert('Failed to delete showtime.');
  }
});

// ----- Seat Management (block / unblock) -----
async function loadAdminSeats() {
  const showtimeId = Number(seatsShowtimeIdInput.value);
  if (!showtimeId) {
    alert('Enter a valid showtime ID.');
    return;
  }
  try {
    const seats = await apiFetch(`/seats?showtimeId=${showtimeId}`);
    adminSeatGrid.innerHTML = '';
    seats.forEach(seat => {
      const btn = document.createElement('button');
      btn.textContent = seat.seatNumber;
      btn.className = 'btn seat-btn';
      btn.dataset.seatId = seat.seatId;
      btn.dataset.status = seat.status;

      if (seat.status === 'booked') {
        btn.classList.add('seat-booked');
        btn.disabled = true;
      } else if (seat.status === 'blocked') {
        btn.classList.add('seat-blocked');
      } else {
        btn.classList.add('seat-available');
      }

      btn.addEventListener('click', () => toggleSeat(showtimeId, btn));
      adminSeatGrid.appendChild(btn);
    });
  } catch (err) {
    console.error(err);
    adminSeatGrid.innerHTML = '<p>Failed to load seats. Check console.</p>';
  }
}

async function toggleSeat(showtimeId, btn) {
  const seatId = Number(btn.dataset.seatId);
  const current = btn.dataset.status;
  const nextStatus = current === 'blocked' ? 'available' : 'blocked';

  try {
    const endpoint = nextStatus === 'blocked' ? '/seats/block' : '/seats/unblock';
    await apiFetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ showtimeId, seatId }),
    });

    btn.dataset.status = nextStatus;
    btn.classList.toggle('seat-blocked', nextStatus === 'blocked');
    btn.classList.toggle('seat-available', nextStatus === 'available');
  } catch (err) {
    console.error(err);
    alert('Failed to update seat status.');
  }
}

loadSeatsBtn?.addEventListener('click', loadAdminSeats);

logoutLink?.addEventListener('click', (e) => {
  e.preventDefault();
  removeToken();
  window.location.href = 'login.html';
});

// ----- Booking Management -----
async function loadAdminBookings() {
  try {
    const bookings = await apiFetch('/bookings/all');
    adminBookingsList.innerHTML = '';
    bookings.forEach(b => {
      const card = document.createElement('div');
      card.className = 'card';
      const bookingTime = new Date(b.bookingTime);
      card.innerHTML = `
        <h4>${b.eventName}</h4>
        <p><strong>Booking Code:</strong> ${b.bookingCode}</p>
        <p><strong>User:</strong> ${b.userName || ''}</p>
        <p><strong>Show:</strong> ${new Date(b.showDate).toLocaleString()} @ ${b.venueName}</p>
        <p><strong>Seats:</strong> ${b.seats}</p>
        <p><strong>Total:</strong> ₹${b.totalAmount}</p>
        <p><strong>Status:</strong> ${b.paymentStatus}</p>
        <p><strong>Booked on:</strong> ${bookingTime.toLocaleString()}</p>
      `;
      adminBookingsList.appendChild(card);
    });
  } catch (err) {
    console.error(err);
    adminBookingsList.innerHTML = '<p>Failed to load bookings.</p>';
  }
}

// Initial loads
loadAdminEvents();
loadAdminBookings();


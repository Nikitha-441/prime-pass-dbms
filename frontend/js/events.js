import { apiFetch } from './app.js';
const container = document.getElementById('eventsContainer');
const paginationEl = document.getElementById('eventsPagination');
const searchInput = document.getElementById('eventSearch');
const categorySelect = document.getElementById('eventCategory');
const dateInput = document.getElementById('eventDate');
const priceInput = document.getElementById('eventPrice');
const applyBtn = document.getElementById('applyEventFilters');
const resetBtn = document.getElementById('resetEventFilters');

const PAGE_SIZE = 6;
let events = [];
let filtered = [];
let currentPage = 1;

function formatDate(dateStr) {
  if (!dateStr) return 'TBA';
  return new Date(dateStr).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

function populateCategories() {
  if (!categorySelect) return;
  Array.from(categorySelect.querySelectorAll('option:not([value="all"])')).forEach(opt => opt.remove());
  const categories = Array.from(new Set(events.map(ev => ev.categoryName).filter(Boolean))).sort();
  categories.forEach(cat => {
    const option = document.createElement('option');
    option.value = cat;
    option.textContent = cat;
    categorySelect.appendChild(option);
  });
}

function renderPagination(total) {
  const pageCount = Math.ceil(total / PAGE_SIZE);
  if (pageCount <= 1) {
    paginationEl.innerHTML = '';
    return;
  }
  const buttons = [];
  for (let i = 1; i <= pageCount; i++) {
    buttons.push(`<button data-page="${i}" class="${i === currentPage ? 'active' : ''}">${i}</button>`);
  }
  paginationEl.innerHTML = buttons.join('');
}

function renderCards() {
  if (!filtered.length) {
    container.innerHTML = '<p>No events found for your filters.</p>';
    paginationEl.innerHTML = '';
    return;
  }
  const start = (currentPage - 1) * PAGE_SIZE;
  const pageItems = filtered.slice(start, start + PAGE_SIZE);
  container.innerHTML = pageItems.map(ev => `
    <div class="card">
      <h3>${ev.eventName}</h3>
      <p>${ev.description || 'Details coming soon.'}</p>
      <p><strong>Category:</strong> ${ev.categoryName}</p>
      <p><strong>Next show:</strong> ${formatDate(ev.nextShowDate)}</p>
      <p><strong>Venue:</strong> ${ev.nextVenue || 'TBA'} ${ev.nextVenueLocation ? `• ${ev.nextVenueLocation}` : ''}</p>
      <p><strong>Base price:</strong> ₹${ev.basePrice}</p>
      <a class="btn" href="event-details.html?event_id=${ev.eventId}">View Details</a>
    </div>
  `).join('');
  renderPagination(filtered.length);
}

function applyFilters() {
  const query = (searchInput?.value || '').toLowerCase().trim();
  const category = categorySelect?.value || 'all';
  const dateValue = dateInput?.value ? new Date(dateInput.value) : null;
  const maxPrice = priceInput?.value ? Number(priceInput.value) : null;

  filtered = events.filter(ev => {
    const matchesSearch = !query ||
      ev.eventName.toLowerCase().includes(query) ||
      (ev.description || '').toLowerCase().includes(query) ||
      (ev.nextVenue || '').toLowerCase().includes(query) ||
      (ev.nextVenueLocation || '').toLowerCase().includes(query);
    const matchesCategory = category === 'all' || ev.categoryName === category;
    const matchesDate = !dateValue || (ev.nextShowDate && new Date(ev.nextShowDate) >= dateValue);
    const matchesPrice = !maxPrice || ev.basePrice <= maxPrice;
    return matchesSearch && matchesCategory && matchesDate && matchesPrice;
  });

  currentPage = 1;
  renderCards();
}

async function loadEvents() {
  try {
    events = await apiFetch('/events');
    filtered = [...events];
    populateCategories();
    renderCards();
  } catch (err) {
    console.error(err);
    container.innerHTML = '<p>Error loading events</p>';
  }
}

applyBtn?.addEventListener('click', applyFilters);
resetBtn?.addEventListener('click', () => {
  searchInput.value = '';
  categorySelect.value = 'all';
  dateInput.value = '';
  priceInput.value = '';
  filtered = [...events];
  currentPage = 1;
  renderCards();
});
searchInput?.addEventListener('input', applyFilters);
categorySelect?.addEventListener('change', applyFilters);
dateInput?.addEventListener('change', applyFilters);
priceInput?.addEventListener('input', applyFilters);
paginationEl?.addEventListener('click', (e) => {
  const page = e.target.dataset.page;
  if (page) {
    currentPage = Number(page);
    renderCards();
  }
});

loadEvents();

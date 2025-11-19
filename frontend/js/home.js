import { apiFetch } from './app.js';

const searchInput = document.getElementById('homeSearch');
const categorySelect = document.getElementById('homeCategory');
const priceInput = document.getElementById('homePrice');
const filterBtn = document.getElementById('homeFilterBtn');
const resetBtn = document.getElementById('resetHomeFilters');
const cardsContainer = document.getElementById('homeEvents');

const categoryImages = {
  Movie: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=900&q=60',
  Concert: 'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=900&q=60',
  Drama: 'https://images.unsplash.com/photo-1462993348109-111bd4babcf4?auto=format&fit=crop&w=900&q=60',
  Sports: 'https://images.unsplash.com/photo-1483721310020-03333e577078?auto=format&fit=crop&w=900&q=60',
  Workshop: 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?auto=format&fit=crop&w=900&q=60'
};

let events = [];

function formatDate(dateStr) {
  if (!dateStr) return 'TBA';
  const date = new Date(dateStr);
  return date.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
}

function getImageForCategory(category) {
  return categoryImages[category] || 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=60';
}

function renderCards(list) {
  if (!list.length) {
    cardsContainer.innerHTML = '<p>No events match your filters. Try updating the search.</p>';
    return;
  }

  cardsContainer.innerHTML = list.slice(0, 6).map(ev => `
    <div class="card">
      <img class="card-image" src="${getImageForCategory(ev.categoryName)}" alt="${ev.categoryName}">
      <h3>${ev.eventName}</h3>
      <p>${ev.description || 'Experience something unforgettable.'}</p>
      <p><strong>Category:</strong> ${ev.categoryName}</p>
      <p><strong>Next show:</strong> ${formatDate(ev.nextShowDate)}</p>
      <p><strong>Venue:</strong> ${ev.nextVenue || 'TBA'}</p>
      <p><strong>Price from:</strong> ₹${ev.basePrice}</p>
      <a class="btn" href="event-details.html?event_id=${ev.eventId}">View Details</a>
    </div>
  `).join('');
}

function populateCategories(list) {
  const categories = Array.from(new Set(list.map(ev => ev.categoryName))).sort();
  categories.forEach(cat => {
    const option = document.createElement('option');
    option.value = cat;
    option.textContent = cat;
    categorySelect.appendChild(option);
  });
}

function applyFilters() {
  const search = (searchInput.value || '').toLowerCase().trim();
  const category = categorySelect.value;
  const maxPrice = Number(priceInput.value);

  const filtered = events.filter(ev => {
    const matchesSearch = !search ||
      ev.eventName.toLowerCase().includes(search) ||
      (ev.description || '').toLowerCase().includes(search) ||
      (ev.nextVenue || '').toLowerCase().includes(search);
    const matchesCategory = category === 'all' || ev.categoryName === category;
    const matchesPrice = !maxPrice || ev.basePrice <= maxPrice;
    return matchesSearch && matchesCategory && matchesPrice;
  });

  renderCards(filtered);
}

async function loadHomeEvents() {
  try {
    events = await apiFetch('/events');
    populateCategories(events);
    renderCards(events);
  } catch (err) {
    console.error(err);
    cardsContainer.innerHTML = '<p class="error-message">Unable to load events. Please try again.</p>';
  }
}

filterBtn?.addEventListener('click', applyFilters);
resetBtn?.addEventListener('click', () => {
  searchInput.value = '';
  categorySelect.value = 'all';
  priceInput.value = '';
  renderCards(events);
});
searchInput?.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    applyFilters();
  }
});

loadHomeEvents();


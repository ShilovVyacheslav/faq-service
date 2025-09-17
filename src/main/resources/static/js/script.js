const DEBOUNCE_DELAY = 200;
const API_BASE_URL = 'http://localhost:8080/admin/faq';

const pgSearchInput = document.getElementById('pgSearchInput');
const mongoSearchInput = document.getElementById('mongoSearchInput');
const pgResults = document.getElementById('pgResults');
const mongoResults = document.getElementById('mongoResults');
const pgStats = document.getElementById('pgStats');
const mongoStats = document.getElementById('mongoStats');

let pgSearchTimer = null;
let mongoSearchTimer = null;

document.addEventListener('DOMContentLoaded', function() {
    pgSearchInput.addEventListener('input', handlePgSearch);
    mongoSearchInput.addEventListener('input', handleMongoSearch);
});

function handlePgSearch(event) {
    clearTimeout(pgSearchTimer);
    const query = event.target.value.trim();

    if (query.length === 0) {
        clearResults(pgResults, pgStats);
        return;
    }

    pgResults.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Поиск...</div>';

    pgSearchTimer = setTimeout(() => {
        searchFaq('pg-search', query, pgResults, pgStats);
    }, DEBOUNCE_DELAY);
}

function handleMongoSearch(event) {
    clearTimeout(mongoSearchTimer);
    const query = event.target.value.trim();

    if (query.length === 0) {
        clearResults(mongoResults, mongoStats);
        return;
    }

    mongoResults.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Поиск...</div>';

    mongoSearchTimer = setTimeout(() => {
        searchFaq('search', query, mongoResults, mongoStats);
    }, DEBOUNCE_DELAY);
}

async function searchFaq(endpoint, query, resultsContainer, statsElement) {
    try {
        const url = `${API_BASE_URL}/${endpoint}?${endpoint === 'pg-search' ? 'key' : 'query'}=${encodeURIComponent(query)}`;

        const headers = { 'Content-Type': 'application/json' };

        if (!['search', 'pg-search'].includes(endpoint)) {
            headers['Authorization'] = `Bearer ${getAuthToken()}`;
        }

        const response = await fetch(url, { headers });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        displayResults(data, resultsContainer, statsElement, query);

    } catch (error) {
        console.error('Search error:', error);
        resultsContainer.innerHTML = `
            <div class="no-results">
                <i class="fas fa-exclamation-triangle" style="color: #f44336;"></i>
                <p>Ошибка при выполнении поиска</p>
                <small>${error.message}</small>
            </div>
        `;
        statsElement.textContent = '';

    }
}

function displayResults(results, container, statsElement, query) {
    if (!results || results.length === 0) {
        container.innerHTML = `
                    <div class="no-results">
                        <i class="fas fa-search" style="font-size: 2rem; margin-bottom: 15px;"></i>
                        <p>По запросу "${query}" ничего не найдено</p>
                    </div>
                `;
        statsElement.textContent = '0 результатов';
        return;
    }

    const resultsHtml = results.map((item, index) => `
                <div class="result-item fade-in" style="animation-delay: ${index * 0.1}s">
                    <div class="result-question">${escapeHtml(item.question || 'Без названия')}</div>
                    ${item.keywords && item.keywords.length > 0 ? `
                        <div class="keywords-container">
                            ${item.keywords.map(keyword => `
                                <span class="keyword">${escapeHtml(keyword)}</span>
                            `).join('')}
                        </div>
                    ` : ''}
                    <div class="status-badge ${item.active ? 'status-active' : 'status-inactive'}">
                        ${item.active ? 'Активный' : 'Неактивный'}
                    </div>
                </div>
            `).join('');

    container.innerHTML = resultsHtml;
    statsElement.textContent = `${results.length} результат(ов)`;
}

function clearResults(container, statsElement) {
    container.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-search" style="font-size: 2rem; margin-bottom: 15px;"></i>
                    <p>Начните вводить запрос для поиска</p>
                </div>
            `;
    statsElement.textContent = '';
}

function getAuthToken() {
    return 'your-jwt-token-here';
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
/**
 * Echo Chamber – The Perspective Flipper
 * Pure Vanilla JavaScript Client Application
 */

const API_BASE_URL =  "https://echo-chamber7.onrender.com";

// State variables
let currentAnalysis = null;
let loadingInterval = null;

// DOM Elements
const statementInput = document.getElementById('statementInput');
const analyzeBtn = document.getElementById('analyzeBtn');
const loadingState = document.getElementById('loadingState');
const loadingStatusText = document.getElementById('loadingStatusText');
const resultsSection = document.getElementById('resultsSection');
const statusToast = document.getElementById('statusToast');
const themeToggle = document.getElementById('themeToggle');
const flipBtn = document.getElementById('flipBtn');
const flipCardElement = document.getElementById('flipCardElement');
const historyList = document.getElementById('historyList');
const clearHistoryBtn = document.getElementById('clearHistoryBtn');

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    loadHistory();
    attachEventListeners();
});

function attachEventListeners() {
    // Preset Chips
    document.querySelectorAll('.chip').forEach((chip) => {
        chip.addEventListener('click', () => {
            statementInput.value = chip.getAttribute('data-text');
            statementInput.focus();
        });
    });

    // Action Buttons
    analyzeBtn.addEventListener('click', analyzePerspective);
    flipBtn.addEventListener('click', flipPerspective);
    themeToggle.addEventListener('click', toggleTheme);
    clearHistoryBtn.addEventListener('click', clearHistory);
}

/**
 * Main Analysis Orchestration
 */
async function analyzePerspective() {
    const statement = statementInput.value.trim();

    if (!statement) {
        showToast('Please enter an opinion or statement to analyze.');
        return;
    }

    hideToast();
    startLoading();

    try {
        const data = await sendToBackend(statement);
        currentAnalysis = {...data, statement, date: new Date().toLocaleDateString('en-GB') };
        displayAnalysis(currentAnalysis);
        saveHistory(currentAnalysis);
    } catch (error) {
        console.error('Analysis error:', error);
        showToast('⚠️ Unable to analyze the statement. Check if backend is active.');
    } finally {
        stopLoading();
    }
}

/**
 * Sends analysis payload to Spring Boot REST endpoint
 */
async function sendToBackend(statement) {
    const response = await fetch(`${API_BASE_URL}/analyze`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ statement }),
    });

    if (!response.ok) {
        throw new Error(`Server returned status: ${response.status}`);
    }

    return await response.json();
}

/**
 * Render Complete AI Analysis Results
 */
function displayAnalysis(analysis) {
    // Update Statistics
    document.getElementById('statPosCount').innerText = analysis.positivePoints ? .length || 0;
    document.getElementById('statNegCount').innerText = analysis.negativePoints ? .length || 0;
    document.getElementById('statBiasCount').innerText = analysis.biases ? .length || 0;
    document.getElementById('statBalanceScore').innerText = `${analysis.score}%`;

    // Render Flipper Cards
    document.getElementById('originalStatementDisplay').innerText = `"${analysis.statement}"`;
    document.getElementById('flippedStatementDisplay').innerText = analysis.alternativePerspective;
    flipCardElement.classList.remove('is-flipped');

    // Render Lists
    displayPositivePoints(analysis.positivePoints);
    displayNegativePoints(analysis.negativePoints);
    displayBiases(analysis.biases);

    // Update Balance Gauge
    updateScore(analysis.score);

    // Render Conclusion
    document.getElementById('balancedConclusionText').innerText = analysis.balancedConclusion;

    // Reveal Section
    resultsSection.classList.remove('hidden');
    resultsSection.scrollIntoView({ behavior: 'smooth' });
}

function displayPositivePoints(points = []) {
    const container = document.getElementById('positiveList');
    container.innerHTML = '';
    points.forEach((pt) => {
        const li = document.createElement('li');
        li.className = 'point-item';
        li.innerHTML = `<span class="point-icon">✓</span><span>${pt}</span>`;
        container.appendChild(li);
    });
}

function displayNegativePoints(points = []) {
    const container = document.getElementById('negativeList');
    container.innerHTML = '';
    points.forEach((pt) => {
        const li = document.createElement('li');
        li.className = 'point-item';
        li.innerHTML = `<span class="point-icon">✕</span><span>${pt}</span>`;
        container.appendChild(li);
    });
}

function displayBiases(biases = []) {
    const container = document.getElementById('biasesContainer');
    container.innerHTML = '';

    if (!biases || biases.length === 0) {
        container.innerHTML = '<p class="empty-state">No major cognitive biases detected in the statement.</p>';
        return;
    }

    biases.forEach((b) => {
        const biasDiv = document.createElement('div');
        biasDiv.className = 'bias-item';
        biasDiv.innerHTML = `
      <div class="bias-top">
        <span class="bias-name">⚠ ${b.name}</span>
        <span class="bias-severity">${b.severity || 'Medium'}</span>
      </div>
      <p class="bias-desc">${b.explanation}</p>
    `;
        container.appendChild(biasDiv);
    });
}

/**
 * Updates Circular Balance Gauge
 */
function updateScore(score) {
    const circle = document.getElementById('balanceRing');
    const valueDisplay = document.getElementById('gaugeValue');
    const labelDisplay = document.getElementById('gaugeLabel');

    const radius = circle.r.baseVal.value;
    const circumference = 2 * Math.PI * radius;

    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    const offset = circumference - (score / 100) * circumference;
    circle.style.strokeDashoffset = offset;

    valueDisplay.innerText = score;

    let labelText = '';
    let color = '#58a6ff';

    if (score <= 30) {
        labelText = 'Highly One-Sided';
        color = '#f85149';
    } else if (score <= 50) {
        labelText = 'Somewhat One-Sided';
        color = '#d29922';
    } else if (score <= 70) {
        labelText = 'Moderately Balanced';
        color = '#58a6ff';
    } else if (score <= 90) {
        labelText = 'Well Balanced';
        color = '#2ea043';
    } else {
        labelText = 'Highly Balanced';
        color = '#238636';
    }

    circle.style.stroke = color;
    labelDisplay.innerText = labelText;
}

/**
 * 3D Card Flip Handler
 */
function flipPerspective() {
    flipCardElement.classList.toggle('is-flipped');
}

/**
 * Loading State Visualizer
 */
function startLoading() {
    resultsSection.classList.add('hidden');
    loadingState.classList.remove('hidden');

    const steps = [
        'Checking positive arguments...',
        'Checking negative arguments...',
        'Flipping perspective...',
        'Detecting possible cognitive biases...',
        'Synthesizing balanced conclusion...'
    ];

    let stepIdx = 0;
    loadingStatusText.innerText = steps[stepIdx];
    loadingInterval = setInterval(() => {
        stepIdx = (stepIdx + 1) % steps.length;
        loadingStatusText.innerText = steps[stepIdx];
    }, 1200);
}

function stopLoading() {
    clearInterval(loadingInterval);
    loadingState.classList.add('hidden');
}

/**
 * History Management (LocalStorage fallback & sync)
 */
function saveHistory(item) {
    let history = getStoredHistory();
    // Avoid duplicates at top
    history = history.filter(h => h.statement.toLowerCase() !== item.statement.toLowerCase());
    history.unshift(item);
    if (history.length > 8) history.pop();
    localStorage.setItem('echo_chamber_history', JSON.stringify(history));
    loadHistory();
}

function getStoredHistory() {
    const raw = localStorage.getItem('echo_chamber_history');
    return raw ? JSON.parse(raw) : [];
}

function loadHistory() {
    const history = getStoredHistory();
    historyList.innerHTML = '';

    if (history.length === 0) {
        historyList.innerHTML = '<p class="empty-state">No previous analyses recorded yet.</p>';
        return;
    }

    history.forEach((entry) => {
        const itemEl = document.createElement('div');
        itemEl.className = 'history-item';
        itemEl.innerHTML = `
      <span class="history-statement">${entry.statement}</span>
      <div class="history-meta">
        <span>Score: <strong>${entry.score}</strong></span>
        <span>${entry.date || '31 Aug 2026'}</span>
      </div>
    `;
        itemEl.addEventListener('click', () => {
            statementInput.value = entry.statement;
            displayAnalysis(entry);
        });
        historyList.appendChild(itemEl);
    });
}

function clearHistory() {
    localStorage.removeItem('echo_chamber_history');
    loadHistory();
}

/**
 * Theme Toggle Handler
 */
function initTheme() {
    const savedTheme = localStorage.getItem('echo_chamber_theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const target = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', target);
    localStorage.setItem('echo_chamber_theme', target);
    updateThemeIcon(target);
}

function updateThemeIcon(theme) {
    const icon = themeToggle.querySelector('.theme-icon');
    icon.innerText = theme === 'dark' ? '☀️' : '🌙';
}

function showToast(message) {
    statusToast.innerText = message;
    statusToast.className = 'toast error';
    statusToast.classList.remove('hidden');
}

function hideToast() {
    statusToast.classList.add('hidden');
}

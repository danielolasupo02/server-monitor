/**
 * Dashboard.js - Handles dynamic functionality for the monitoring dashboard
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize dashboard
    initDashboard();

    // Set up auto-refresh (every 60 seconds)
    startAutoRefresh();

    // Set up auto-scrolling for long content
    initAutoScroll();
});

// Dashboard initialization
function initDashboard() {
    // Set up event listeners
    setupEventListeners();

    // Update the "last updated" timestamp
    updateLastUpdatedTime();
}

// Set up all event listeners for interactive elements
function setupEventListeners() {
    // Toggle auto-scroll button
    const toggleScrollBtn = document.getElementById('toggleScroll');
    if (toggleScrollBtn) {
        toggleScrollBtn.addEventListener('click', function() {
            toggleAutoScroll();
        });
    }

    // Manual refresh button
    const refreshBtn = document.getElementById('refreshData');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            refreshDashboardData();
        });
    }

    // Add server button
    const addServerBtn = document.getElementById('addServerBtn');
    if (addServerBtn) {
        addServerBtn.addEventListener('click', function() {
            window.location.href = '/monitoring/servers';
        });
    }
}

// Auto-refresh functionality
let refreshInterval;

function startAutoRefresh() {
    refreshInterval = setInterval(function() {
        refreshDashboardData();
    }, 60000); // Refresh every 60 seconds
}

function refreshDashboardData() {
    // Show loading indicator
    const loadingIndicator = document.createElement('div');
    loadingIndicator.className = 'scroll-notice';
    loadingIndicator.textContent = 'Refreshing data...';
    document.body.appendChild(loadingIndicator);

    // Fetch new data via AJAX
    fetch(window.location.href)
        .then(response => response.text())
        .then(html => {
            // Extract only the metrics sections
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');

            const newDiskMetrics = doc.querySelector('#diskMetricsSection');
            const newMemoryMetrics = doc.querySelector('#memoryMetricsSection');

            // Update sections with new data
            if (newDiskMetrics && document.getElementById('diskMetricsSection')) {
                document.getElementById('diskMetricsSection').innerHTML = newDiskMetrics.innerHTML;
            }

            if (newMemoryMetrics && document.getElementById('memoryMetricsSection')) {
                document.getElementById('memoryMetricsSection').innerHTML = newMemoryMetrics.innerHTML;
            }

            // Update timestamp
            updateLastUpdatedTime();

            // Remove loading indicator
            document.body.removeChild(loadingIndicator);
        })
        .catch(error => {
            console.error('Error refreshing dashboard:', error);
            // Show error message
            loadingIndicator.textContent = 'Error refreshing data. Retrying...';
            setTimeout(() => {
                if (document.body.contains(loadingIndicator)) {
                    document.body.removeChild(loadingIndicator);
                }
            }, 3000);
        });
}

// Update the last updated timestamp
function updateLastUpdatedTime() {
    const timestampElements = document.querySelectorAll('.last-updated');
    const now = new Date();
    const formattedTime = now.toLocaleString();

    timestampElements.forEach(element => {
        element.textContent = `Last updated: ${formattedTime}`;
    });
}

// Auto-scrolling functionality for long pages
let scrollInterval;
let isScrolling = false;

function initAutoScroll() {
    // Only initialize auto-scroll if the page content is taller than the viewport
    if (document.body.scrollHeight > window.innerHeight) {
        startAutoScroll();

        // Create scroll indicator
        const scrollIndicator = document.createElement('div');
        scrollIndicator.className = 'scroll-notice';
        scrollIndicator.id = 'scrollIndicator';
        scrollIndicator.innerHTML = 'Auto-scrolling enabled <button id="toggleScroll" class="btn btn-warning">Pause</button>';
        document.body.appendChild(scrollIndicator);

        // Set up the toggle button in the indicator
        document.getElementById('toggleScroll').addEventListener('click', toggleAutoScroll);
    }
}

function startAutoScroll() {
    if (!isScrolling) {
        isScrolling = true;

        const scrollStep = 1; // pixels per step
        const scrollInterval = 50; // milliseconds between steps
        const pauseAtEnds = 5000; // milliseconds to pause at top and bottom

        let direction = 'down';
        let pauseTimeout = null;

        // Update button text if it exists
        const toggleBtn = document.getElementById('toggleScroll');
        if (toggleBtn) {
            toggleBtn.textContent = 'Pause';
            toggleBtn.className = 'btn btn-warning';
        }

        window.scrollInterval = setInterval(() => {
            if (direction === 'down') {
                window.scrollBy(0, scrollStep);

                // If we've reached the bottom
                if ((window.innerHeight + window.scrollY) >= document.body.scrollHeight - 10) {
                    if (pauseTimeout === null) {
                        pauseTimeout = setTimeout(() => {
                            direction = 'up';
                            pauseTimeout = null;
                        }, pauseAtEnds);
                    }
                }
            } else {
                window.scrollBy(0, -scrollStep);

                // If we've reached the top
                if (window.scrollY <= 10) {
                    if (pauseTimeout === null) {
                        pauseTimeout = setTimeout(() => {
                            direction = 'down';
                            pauseTimeout = null;
                        }, pauseAtEnds);
                    }
                }
            }
        }, scrollInterval);
    }
}

function stopAutoScroll() {
    if (isScrolling) {
        isScrolling = false;
        clearInterval(window.scrollInterval);

        // Update button text if it exists
        const toggleBtn = document.getElementById('toggleScroll');
        if (toggleBtn) {
            toggleBtn.textContent = 'Resume';
            toggleBtn.className = 'btn btn-success';
        }
    }
}

function toggleAutoScroll() {
    if (isScrolling) {
        stopAutoScroll();
    } else {
        startAutoScroll();
    }
}
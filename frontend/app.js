/**
 * Antique Collectibles Store - Application Logic
 * Pure Vanilla JavaScript (ES6+)
 */

const API_BASE = 'http://localhost:8080/api';

// Application State
const state = {
  items: [],
  eras: [],
  salesReport: null,
  currentViewTitle: 'All Items (MySQL)',
  itemBeingEdited: null,
  itemBeingDeleted: null,
  selectedItemForValuation: null
};

// ============================================================================
// Utilities & Formatting
// ============================================================================

function formatCurrency(amount) {
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return '₹0.00';
  }
  const num = Number(amount);
  return '₹' + num.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  try {
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const date = new Date(parts[0], parts[1] - 1, parts[2]);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    }
    return dateStr;
  } catch (e) {
    return dateStr;
  }
}

function getTodayDateString() {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, '0');
  const dd = String(today.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ============================================================================
// Notification / Toast System
// ============================================================================

function showToast(type, title, message) {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const iconMap = {
    success: '✓',
    error: '✕',
    info: 'ℹ'
  };

  toast.innerHTML = `
    <span class="toast-icon">${iconMap[type] || 'ℹ'}</span>
    <div class="toast-content">
      <div class="toast-title">${escapeHtml(title)}</div>
      <div class="toast-message">${escapeHtml(message)}</div>
    </div>
    <button class="toast-close" aria-label="Close">&times;</button>
  `;

  const closeBtn = toast.querySelector('.toast-close');
  closeBtn.addEventListener('click', () => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(30px)';
    setTimeout(() => toast.remove(), 250);
  });

  container.appendChild(toast);

  // Auto remove after 5 seconds
  setTimeout(() => {
    if (toast.parentElement) {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(30px)';
      setTimeout(() => toast.remove(), 250);
    }
  }, 5000);
}

// ============================================================================
// API Client Layer
// ============================================================================

async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const defaultHeaders = {
    'Accept': 'application/json'
  };

  if (options.body && !(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers
    }
  };

  try {
    const response = await fetch(url, config);

    // Handle 204 No Content
    if (response.status === 204) {
      return null;
    }

    const contentType = response.headers.get('content-type') || '';
    let responseData = null;

    if (contentType.includes('application/json')) {
      responseData = await response.json();
    } else {
      responseData = await response.text();
    }

    if (!response.ok) {
      // Backend structured ErrorResponse
      const errorObj = new Error(
        (responseData && responseData.message) ||
        (responseData && responseData.error) ||
        `HTTP request failed with status ${response.status}`
      );
      errorObj.status = response.status;
      errorObj.error = responseData && responseData.error;
      errorObj.validationErrors = responseData && responseData.validationErrors;
      errorObj.raw = responseData;
      throw errorObj;
    }

    return responseData;
  } catch (err) {
    if (err.name === 'TypeError' && err.message.includes('Failed to fetch')) {
      const connErr = new Error('Cannot connect to backend API on http://localhost:8080. Is the Spring Boot server running?');
      connErr.status = 0;
      updateBackendStatus(false);
      throw connErr;
    }
    throw err;
  }
}

function updateBackendStatus(isOnline) {
  const badge = document.getElementById('apiStatusBadge');
  const text = document.getElementById('apiStatusText');
  if (!badge || !text) return;

  if (isOnline) {
    badge.classList.remove('offline');
    badge.classList.add('online');
    text.textContent = 'Connected';
  } else {
    badge.classList.remove('online');
    badge.classList.add('offline');
    text.textContent = 'Disconnected';
  }
}

// ============================================================================
// Form Validation Helpers
// ============================================================================

function clearFormErrors(formElement) {
  if (!formElement) return;
  const invalidInputs = formElement.querySelectorAll('.is-invalid');
  invalidInputs.forEach(input => input.classList.remove('is-invalid'));

  const errorSpans = formElement.querySelectorAll('.field-error');
  errorSpans.forEach(span => {
    span.textContent = '';
    span.classList.remove('active');
  });

  const alerts = formElement.querySelectorAll('.alert');
  alerts.forEach(alert => {
    alert.style.display = 'none';
    alert.textContent = '';
  });
}

function applyServerValidationErrors(formElement, validationErrors, globalMessage) {
  if (!formElement) return;

  if (validationErrors && typeof validationErrors === 'object') {
    Object.keys(validationErrors).forEach(field => {
      const message = validationErrors[field];
      // Try to match input by name or id
      const input = formElement.querySelector(`[name="${field}"]`) ||
                    formElement.querySelector(`#${field}Input`) ||
                    formElement.querySelector(`#item${field.charAt(0).toUpperCase() + field.slice(1)}Input`);
      if (input) {
        input.classList.add('is-invalid');
      }

      // Try to find corresponding field error span
      const errorSpan = formElement.querySelector(`#${field}Error`) ||
                        formElement.querySelector(`#item${field.charAt(0).toUpperCase() + field.slice(1)}Error`) ||
                        formElement.querySelector(`#val${field.charAt(0).toUpperCase() + field.slice(1)}Error`) ||
                        formElement.querySelector(`#sell${field.charAt(0).toUpperCase() + field.slice(1)}Error`);
      if (errorSpan) {
        errorSpan.textContent = message;
        errorSpan.classList.add('active');
      }
    });
  }

  // Display top alert if available
  const alertBox = formElement.querySelector('.alert');
  if (alertBox && globalMessage) {
    alertBox.textContent = globalMessage;
    alertBox.style.display = 'block';
  }
}

// ============================================================================
// Section 1: Items Management
// ============================================================================

async function loadAllItems() {
  const tbody = document.getElementById('itemsTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center loading-cell">
        <div class="spinner"></div> Fetching items from MySQL database...
      </td>
    </tr>
  `;

  try {
    const items = await apiRequest('/items');
    state.items = items;
    state.currentViewTitle = 'All Items (MySQL)';
    renderItemsTable(items);
    updateItemsStatusBar(state.currentViewTitle, items.length);
    updateBackendStatus(true);
    populateItemDropdowns();
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center empty-state" style="color: var(--danger);">
          Failed to load items: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'API Error', err.message);
  }
}

async function loadSortedItems() {
  const tbody = document.getElementById('itemsTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center loading-cell">
        <div class="spinner"></div> Performing In-Order BST Traversal...
      </td>
    </tr>
  `;

  try {
    const items = await apiRequest('/items/sorted');
    state.items = items;
    state.currentViewTitle = 'BST Sorted View (In-Order by Item ID)';
    renderItemsTable(items);
    updateItemsStatusBar(state.currentViewTitle, items.length);
    showToast('info', 'BST Traversal', `Loaded ${items.length} available items sorted by Item ID from in-memory BST.`);
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center empty-state" style="color: var(--danger);">
          Failed to load sorted items: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'BST Error', err.message);
  }
}

async function searchItemById(id) {
  if (!id) {
    showToast('info', 'Input Required', 'Please enter a valid Item ID to search.');
    return;
  }

  const tbody = document.getElementById('itemsTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center loading-cell">
        <div class="spinner"></div> Querying BST for Item ID ${escapeHtml(id)}...
      </td>
    </tr>
  `;

  try {
    const item = await apiRequest(`/items/${id}`);
    state.items = [item];
    state.currentViewTitle = `BST Search Result (Item ID: ${id})`;
    renderItemsTable([item]);
    updateItemsStatusBar(state.currentViewTitle, 1);
    showToast('success', 'Item Found', `Located "${item.name}" in BST.`);
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center empty-state" style="color: var(--danger);">
          ${escapeHtml(err.message || `Item ${id} not found`)}
        </td>
      </tr>
    `;
    updateItemsStatusBar(`BST Search (ID: ${id}) - Not Found`, 0);
    showToast('error', 'Search Result', err.message);
  }
}

async function filterItemsByPriceRange(min, max) {
  const params = new URLSearchParams();
  if (min !== undefined && min !== null && min !== '') params.append('min', min);
  if (max !== undefined && max !== null && max !== '') params.append('max', max);

  const queryString = params.toString() ? `?${params.toString()}` : '';

  const tbody = document.getElementById('itemsTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="8" class="text-center loading-cell">
        <div class="spinner"></div> Querying BST price range...
      </td>
    </tr>
  `;

  try {
    const items = await apiRequest(`/items/range${queryString}`);
    state.items = items;
    const label = `BST Price Range: ${min ? '₹' + Number(min).toLocaleString() : 'Any'} – ${max ? '₹' + Number(max).toLocaleString() : 'Any'}`;
    state.currentViewTitle = label;
    renderItemsTable(items);
    updateItemsStatusBar(label, items.length);
    showToast('info', 'Price Filter Applied', `Found ${items.length} items within the specified price range.`);
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center empty-state" style="color: var(--danger);">
          Range query failed: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'Range Search Error', err.message);
  }
}

function updateItemsStatusBar(viewTitle, count) {
  const viewBadge = document.getElementById('currentViewBadge');
  const countBadge = document.getElementById('itemsCountBadge');
  if (viewBadge) viewBadge.textContent = viewTitle;
  if (countBadge) {
    countBadge.textContent = `${count} ${count === 1 ? 'item' : 'items'} found`;
  }
}

function renderItemsTable(items) {
  const tbody = document.getElementById('itemsTableBody');
  if (!tbody) return;

  if (!items || items.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center empty-state">
          No items found matching the current criteria.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = items.map(item => {
    const isAvailable = item.status === 'AVAILABLE';
    const statusClass = isAvailable ? 'badge-available' : 'badge-sold';
    const statusText = isAvailable ? 'AVAILABLE' : 'SOLD';
    const eraText = item.eraName || (getEraNameById(item.eraId) || `Era #${item.eraId}`);

    return `
      <tr data-item-id="${item.itemId}">
        <td><strong>#${item.itemId}</strong></td>
        <td>
          <div style="font-weight: 600; color: var(--text-primary);">${escapeHtml(item.name)}</div>
        </td>
        <td>${escapeHtml(item.category)}</td>
        <td><span class="era-pill">${escapeHtml(eraText)}</span></td>
        <td><span class="condition-tag">${escapeHtml(item.condition || '—')}</span></td>
        <td><span class="price-text">${formatCurrency(item.price)}</span></td>
        <td><span class="badge ${statusClass}">${statusText}</span></td>
        <td class="text-right">
          <div class="action-buttons">
            <button class="btn btn-sm btn-outline view-val-btn" title="View historical valuations" data-id="${item.itemId}">
              📜 Appraisals
            </button>
            ${isAvailable ? `
              <button class="btn btn-sm btn-success sell-action-btn" title="Record sale" data-id="${item.itemId}">
                💰 Sell
              </button>
            ` : ''}
            <button class="btn btn-sm btn-outline edit-item-btn" title="Edit Item" data-id="${item.itemId}">
              ✏️
            </button>
            <button class="btn btn-sm btn-outline delete-item-btn" title="Delete Item" data-id="${item.itemId}" style="color: var(--danger);">
              🗑️
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  // Attach event handlers to row action buttons
  tbody.querySelectorAll('.view-val-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      switchToValuationsTab(id);
    });
  });

  tbody.querySelectorAll('.sell-action-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      openSellItemModal(id);
    });
  });

  tbody.querySelectorAll('.edit-item-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.getAttribute('data-id'));
      const item = state.items.find(i => i.itemId === id);
      if (item) openEditItemModal(item);
    });
  });

  tbody.querySelectorAll('.delete-item-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.getAttribute('data-id'));
      const item = state.items.find(i => i.itemId === id);
      if (item) openDeleteModal(item);
    });
  });
}

// ============================================================================
// Section 2: Eras Management
// ============================================================================

async function loadEras() {
  const tbody = document.getElementById('erasTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="5" class="text-center loading-cell">Loading historical eras...</td>
    </tr>
  `;

  try {
    const eras = await apiRequest('/eras');
    state.eras = eras;
    renderErasTable(eras);
    populateEraDropdowns();
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="text-center empty-state" style="color: var(--danger);">
          Failed to load eras: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'Eras Error', err.message);
  }
}

function getEraNameById(id) {
  const era = state.eras.find(e => e.eraId === Number(id));
  return era ? era.eraName : null;
}

function renderErasTable(eras) {
  const tbody = document.getElementById('erasTableBody');
  if (!tbody) return;

  if (!eras || eras.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="text-center empty-state">No historical eras registered.</td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = eras.map(era => {
    const duration = era.endYear - era.startYear;
    return `
      <tr>
        <td><strong>#${era.eraId}</strong></td>
        <td><strong style="color: var(--primary);">${escapeHtml(era.eraName)}</strong></td>
        <td>${era.startYear}</td>
        <td>${era.endYear}</td>
        <td><span class="era-pill">${duration} years</span></td>
      </tr>
    `;
  }).join('');
}

function populateEraDropdowns() {
  const selects = [
    document.getElementById('itemEraSelect')
  ];

  selects.forEach(select => {
    if (!select) return;
    const currentVal = select.value;
    select.innerHTML = '<option value="">Select Era...</option>' +
      state.eras.map(e => `<option value="${e.eraId}">${escapeHtml(e.eraName)} (${e.startYear} - ${e.endYear})</option>`).join('');
    if (currentVal) select.value = currentVal;
  });
}

async function handleAddEraSubmit(event) {
  event.preventDefault();
  const form = document.getElementById('addEraForm');
  clearFormErrors(form);

  const eraName = document.getElementById('eraNameInput').value.trim();
  const startYear = document.getElementById('startYearInput').value;
  const endYear = document.getElementById('endYearInput').value;

  // Client Validation
  let hasError = false;
  if (!eraName) {
    document.getElementById('eraNameError').textContent = 'Era name is required';
    document.getElementById('eraNameError').classList.add('active');
    document.getElementById('eraNameInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!startYear) {
    document.getElementById('startYearError').textContent = 'Start year is required';
    document.getElementById('startYearError').classList.add('active');
    document.getElementById('startYearInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!endYear) {
    document.getElementById('endYearError').textContent = 'End year is required';
    document.getElementById('endYearError').classList.add('active');
    document.getElementById('endYearInput').classList.add('is-invalid');
    hasError = true;
  }
  if (startYear && endYear && Number(startYear) > Number(endYear)) {
    document.getElementById('startYearError').textContent = 'Start year must be <= End year';
    document.getElementById('startYearError').classList.add('active');
    document.getElementById('startYearInput').classList.add('is-invalid');
    hasError = true;
  }

  if (hasError) return;

  const payload = {
    eraName: eraName,
    startYear: parseInt(startYear, 10),
    endYear: parseInt(endYear, 10)
  };

  const submitBtn = document.getElementById('saveEraBtn');
  submitBtn.disabled = true;

  try {
    const created = await apiRequest('/eras', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    showToast('success', 'Era Added', `Historical era "${created.eraName}" created successfully!`);
    form.reset();
    await loadEras();
    // Also re-render items table to update era labels if needed
    renderItemsTable(state.items);
  } catch (err) {
    applyServerValidationErrors(form, err.validationErrors, err.message);
    showToast('error', 'Creation Failed', err.message);
  } finally {
    submitBtn.disabled = false;
  }
}

// ============================================================================
// Section 3: Valuations
// ============================================================================

function populateItemDropdowns() {
  const valSelect = document.getElementById('valItemSelect');
  const sellSelect = document.getElementById('sellItemSelect');

  if (valSelect) {
    const currentVal = valSelect.value;
    valSelect.innerHTML = '<option value="">Select an antique item...</option>' +
      state.items.map(item => `
        <option value="${item.itemId}">
          #${item.itemId} - ${escapeHtml(item.name)} (${formatCurrency(item.price)})
        </option>
      `).join('');
    if (currentVal) valSelect.value = currentVal;
  }

  if (sellSelect) {
    const currentVal = sellSelect.value;
    const availableItems = state.items.filter(i => i.status === 'AVAILABLE');
    sellSelect.innerHTML = '<option value="">Choose an available antique...</option>' +
      availableItems.map(item => `
        <option value="${item.itemId}" data-price="${item.price}">
          #${item.itemId} - ${escapeHtml(item.name)} (${formatCurrency(item.price)})
        </option>
      `).join('');
    if (currentVal) sellSelect.value = currentVal;
  }
}

async function loadValuationsForItem(itemId) {
  if (!itemId) return;

  const titleEl = document.getElementById('valuationHistoryTitle');
  const tbody = document.getElementById('valuationsTableBody');
  const targetItem = state.items.find(i => i.itemId === Number(itemId));

  if (titleEl) {
    if (targetItem) {
      titleEl.innerHTML = `Showing appraisals for <strong>#${targetItem.itemId}: ${escapeHtml(targetItem.name)}</strong> (Listed at: ${formatCurrency(targetItem.price)})`;
    } else {
      titleEl.innerHTML = `Showing appraisals for <strong>Item ID #${escapeHtml(itemId)}</strong>`;
    }
  }

  tbody.innerHTML = `
    <tr>
      <td colspan="4" class="text-center loading-cell">Loading appraisals...</td>
    </tr>
  `;

  try {
    const valuations = await apiRequest(`/items/${itemId}/valuations`);

    if (!valuations || valuations.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="4" class="text-center empty-state">
            No historical appraisal certificates recorded for this item yet.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = valuations.map(val => `
      <tr>
        <td><strong>#${val.valId}</strong></td>
        <td><strong style="color: var(--primary);">${formatCurrency(val.appraisedValue)}</strong></td>
        <td>${formatDate(val.valDate)}</td>
        <td>${escapeHtml(val.appraiser || '—')}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4" class="text-center empty-state" style="color: var(--danger);">
          Failed to fetch valuations: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'Appraisals Error', err.message);
  }
}

function switchToValuationsTab(itemId) {
  // Switch to Valuations tab
  switchTab('valuationsTab');

  // Set lookup field & dropdown
  const lookupInput = document.getElementById('lookupValuationItemId');
  const selectEl = document.getElementById('valItemSelect');

  if (lookupInput) lookupInput.value = itemId;
  if (selectEl) selectEl.value = itemId;

  loadValuationsForItem(itemId);
}

async function handleAddValuationSubmit(event) {
  event.preventDefault();
  const form = document.getElementById('addValuationForm');
  clearFormErrors(form);

  const itemId = document.getElementById('valItemSelect').value;
  const appraisedValue = document.getElementById('valAppraisedValue').value;
  const valDate = document.getElementById('valDateInput').value;
  const appraiser = document.getElementById('valAppraiserInput').value.trim();

  // Client Validation
  let hasError = false;
  if (!itemId) {
    document.getElementById('valItemIdError').textContent = 'Please select a target antique item';
    document.getElementById('valItemIdError').classList.add('active');
    document.getElementById('valItemSelect').classList.add('is-invalid');
    hasError = true;
  }
  if (!appraisedValue || Number(appraisedValue) < 0) {
    document.getElementById('valAppraisedValueError').textContent = 'Appraised value must be non-negative';
    document.getElementById('valAppraisedValueError').classList.add('active');
    document.getElementById('valAppraisedValue').classList.add('is-invalid');
    hasError = true;
  }
  if (!valDate) {
    document.getElementById('valDateError').textContent = 'Valuation date is required';
    document.getElementById('valDateError').classList.add('active');
    document.getElementById('valDateInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!appraiser) {
    document.getElementById('valAppraiserError').textContent = 'Appraiser name is required';
    document.getElementById('valAppraiserError').classList.add('active');
    document.getElementById('valAppraiserInput').classList.add('is-invalid');
    hasError = true;
  }

  if (hasError) return;

  const payload = {
    itemId: parseInt(itemId, 10),
    appraisedValue: parseFloat(appraisedValue),
    valDate: valDate,
    appraiser: appraiser
  };

  const submitBtn = document.getElementById('saveValuationBtn');
  submitBtn.disabled = true;

  try {
    const created = await apiRequest('/valuations', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    showToast('success', 'Appraisal Recorded', `Appraisal certificate #${created.valId} registered for ${formatCurrency(created.appraisedValue)}.`);
    form.reset();
    document.getElementById('valDateInput').value = getTodayDateString();

    // Reload valuations list for this item
    loadValuationsForItem(itemId);
  } catch (err) {
    applyServerValidationErrors(form, err.validationErrors, err.message);
    showToast('error', 'Appraisal Error', err.message);
  } finally {
    submitBtn.disabled = false;
  }
}

// ============================================================================
// Section 4: Sales & Revenue
// ============================================================================

async function loadSalesReport() {
  const tbody = document.getElementById('salesTableBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="6" class="text-center loading-cell">Loading sales report & financial ledger...</td>
    </tr>
  `;

  try {
    const report = await apiRequest('/sales');
    state.salesReport = report;

    // Update KPIs
    const kpiRev = document.getElementById('kpiTotalRevenue');
    const kpiCount = document.getElementById('kpiTotalCount');
    const kpiAvg = document.getElementById('kpiAvgSalePrice');

    if (kpiRev) kpiRev.textContent = formatCurrency(report.totalRevenue);
    if (kpiCount) kpiCount.textContent = report.totalCount || 0;

    const avg = report.totalCount > 0 ? (report.totalRevenue / report.totalCount) : 0;
    if (kpiAvg) kpiAvg.textContent = formatCurrency(avg);

    // Render Table
    if (!report.sales || report.sales.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" class="text-center empty-state">No sales transactions completed yet.</td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = report.sales.map(sale => `
      <tr>
        <td><strong>#${sale.saleId}</strong></td>
        <td>#${sale.itemId}</td>
        <td><strong>${escapeHtml(sale.itemName)}</strong></td>
        <td>${escapeHtml(sale.buyerName)}</td>
        <td><strong style="color: var(--primary);">${formatCurrency(sale.salePrice)}</strong></td>
        <td>${formatDate(sale.saleDate)}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="text-center empty-state" style="color: var(--danger);">
          Failed to load sales report: ${escapeHtml(err.message)}
        </td>
      </tr>
    `;
    showToast('error', 'Sales Report Error', err.message);
  }
}

function openSellItemModal(preselectedItemId = null) {
  const modal = document.getElementById('sellModal');
  const form = document.getElementById('sellForm');
  clearFormErrors(form);
  form.reset();

  populateItemDropdowns();

  const itemSelect = document.getElementById('sellItemSelect');
  const priceInput = document.getElementById('sellPriceInput');
  const dateInput = document.getElementById('sellDateInput');

  dateInput.value = getTodayDateString();

  if (preselectedItemId) {
    itemSelect.value = preselectedItemId;
    const selectedOption = itemSelect.querySelector(`option[value="${preselectedItemId}"]`);
    if (selectedOption && selectedOption.dataset.price) {
      priceInput.value = selectedOption.dataset.price;
    }
  }

  // Pre-fill price when item changes
  itemSelect.onchange = () => {
    const selected = itemSelect.selectedOptions[0];
    if (selected && selected.dataset.price) {
      priceInput.value = selected.dataset.price;
    } else {
      priceInput.value = '';
    }
  };

  modal.classList.add('active');
}

function closeSellItemModal() {
  const modal = document.getElementById('sellModal');
  modal.classList.remove('active');
}

async function handleSellFormSubmit(event) {
  event.preventDefault();
  const form = document.getElementById('sellForm');
  clearFormErrors(form);

  const itemId = document.getElementById('sellItemSelect').value;
  const buyerName = document.getElementById('sellBuyerName').value.trim();
  const salePrice = document.getElementById('sellPriceInput').value;
  const saleDate = document.getElementById('sellDateInput').value;

  let hasError = false;
  if (!itemId) {
    document.getElementById('sellItemIdError').textContent = 'Please choose an antique to sell';
    document.getElementById('sellItemIdError').classList.add('active');
    document.getElementById('sellItemSelect').classList.add('is-invalid');
    hasError = true;
  }
  if (!buyerName) {
    document.getElementById('sellBuyerNameError').textContent = 'Buyer name is required';
    document.getElementById('sellBuyerNameError').classList.add('active');
    document.getElementById('sellBuyerName').classList.add('is-invalid');
    hasError = true;
  }
  if (!salePrice || Number(salePrice) < 0) {
    document.getElementById('sellPriceError').textContent = 'Sale price must be non-negative';
    document.getElementById('sellPriceError').classList.add('active');
    document.getElementById('sellPriceInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!saleDate) {
    document.getElementById('sellDateError').textContent = 'Sale date is required';
    document.getElementById('sellDateError').classList.add('active');
    document.getElementById('sellDateInput').classList.add('is-invalid');
    hasError = true;
  }

  if (hasError) return;

  const payload = {
    itemId: parseInt(itemId, 10),
    buyerName: buyerName,
    salePrice: parseFloat(salePrice),
    saleDate: saleDate
  };

  const submitBtn = document.getElementById('confirmSellBtn');
  submitBtn.disabled = true;

  try {
    const sale = await apiRequest('/sales', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    showToast('success', 'Sale Finalized', `Recorded sale #${sale.saleId} for ${formatCurrency(sale.salePrice)}. Item status marked as SOLD.`);
    closeSellItemModal();

    // Refresh state
    await loadSalesReport();
    await loadAllItems();
  } catch (err) {
    applyServerValidationErrors(form, err.validationErrors, err.message);
    showToast('error', 'Sale Failed', err.message);
  } finally {
    submitBtn.disabled = false;
  }
}

// ============================================================================
// Modals: Add / Edit Item
// ============================================================================

function openAddItemModal() {
  state.itemBeingEdited = null;
  const modal = document.getElementById('itemModal');
  const form = document.getElementById('itemForm');
  clearFormErrors(form);
  form.reset();

  document.getElementById('itemModalTitle').textContent = 'Add New Antique Item';
  document.getElementById('editItemId').value = '';
  document.getElementById('itemStatusSelect').value = 'AVAILABLE';

  populateEraDropdowns();
  modal.classList.add('active');
}

function openEditItemModal(item) {
  state.itemBeingEdited = item;
  const modal = document.getElementById('itemModal');
  const form = document.getElementById('itemForm');
  clearFormErrors(form);

  document.getElementById('itemModalTitle').textContent = `Edit Antique Item #${item.itemId}`;
  document.getElementById('editItemId').value = item.itemId;
  document.getElementById('itemNameInput').value = item.name || '';
  document.getElementById('itemCategoryInput').value = item.category || '';
  document.getElementById('itemPriceInput').value = item.price || '';
  document.getElementById('itemConditionSelect').value = item.condition || '';
  document.getElementById('itemStatusSelect').value = item.status || 'AVAILABLE';

  populateEraDropdowns();
  document.getElementById('itemEraSelect').value = item.eraId || '';

  modal.classList.add('active');
}

function closeItemModal() {
  const modal = document.getElementById('itemModal');
  modal.classList.remove('active');
  state.itemBeingEdited = null;
}

async function handleItemFormSubmit(event) {
  event.preventDefault();
  const form = document.getElementById('itemForm');
  clearFormErrors(form);

  const editId = document.getElementById('editItemId').value;
  const isEdit = Boolean(editId);

  const name = document.getElementById('itemNameInput').value.trim();
  const category = document.getElementById('itemCategoryInput').value.trim();
  const eraId = document.getElementById('itemEraSelect').value;
  const condition = document.getElementById('itemConditionSelect').value;
  const price = document.getElementById('itemPriceInput').value;
  const status = document.getElementById('itemStatusSelect').value;

  // Client Validation
  let hasError = false;
  if (!name) {
    document.getElementById('itemNameError').textContent = 'Item name is required';
    document.getElementById('itemNameError').classList.add('active');
    document.getElementById('itemNameInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!category) {
    document.getElementById('itemCategoryError').textContent = 'Category is required';
    document.getElementById('itemCategoryError').classList.add('active');
    document.getElementById('itemCategoryInput').classList.add('is-invalid');
    hasError = true;
  }
  if (!eraId) {
    document.getElementById('itemEraError').textContent = 'Please select a historical era';
    document.getElementById('itemEraError').classList.add('active');
    document.getElementById('itemEraSelect').classList.add('is-invalid');
    hasError = true;
  }
  if (!condition) {
    document.getElementById('itemConditionError').textContent = 'Please select item condition';
    document.getElementById('itemConditionError').classList.add('active');
    document.getElementById('itemConditionSelect').classList.add('is-invalid');
    hasError = true;
  }
  if (!price || Number(price) < 0) {
    document.getElementById('itemPriceError').textContent = 'Price must be non-negative';
    document.getElementById('itemPriceError').classList.add('active');
    document.getElementById('itemPriceInput').classList.add('is-invalid');
    hasError = true;
  }

  if (hasError) return;

  const payload = {
    name: name,
    category: category,
    eraId: parseInt(eraId, 10),
    condition: condition,
    price: parseFloat(price),
    status: status
  };

  const saveBtn = document.getElementById('saveItemBtn');
  saveBtn.disabled = true;

  try {
    let result;
    if (isEdit) {
      result = await apiRequest(`/items/${editId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('success', 'Item Updated', `Updated "${result.name}" successfully (Synced with DB & BST).`);
    } else {
      result = await apiRequest('/items', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      showToast('success', 'Item Created', `Added "${result.name}" to inventory (Synced with DB & BST).`);
    }

    closeItemModal();
    await loadAllItems();
  } catch (err) {
    applyServerValidationErrors(form, err.validationErrors, err.message);
    showToast('error', 'Operation Failed', err.message);
  } finally {
    saveBtn.disabled = false;
  }
}

// ============================================================================
// Modal: Delete Item
// ============================================================================

function openDeleteModal(item) {
  state.itemBeingDeleted = item;
  const modal = document.getElementById('deleteModal');
  document.getElementById('deleteItemName').textContent = item.name;
  document.getElementById('deleteItemId').textContent = item.itemId;

  const alert = document.getElementById('deleteModalAlert');
  alert.style.display = 'none';
  alert.textContent = '';

  modal.classList.add('active');
}

function closeDeleteModal() {
  const modal = document.getElementById('deleteModal');
  modal.classList.remove('active');
  state.itemBeingDeleted = null;
}

async function handleConfirmDelete() {
  if (!state.itemBeingDeleted) return;

  const btn = document.getElementById('confirmDeleteBtn');
  btn.disabled = true;

  const itemId = state.itemBeingDeleted.itemId;
  const itemName = state.itemBeingDeleted.name;

  try {
    await apiRequest(`/items/${itemId}`, {
      method: 'DELETE'
    });

    showToast('success', 'Item Deleted', `Item #${itemId} ("${itemName}") permanently removed.`);
    closeDeleteModal();
    await loadAllItems();
  } catch (err) {
    const alert = document.getElementById('deleteModalAlert');
    alert.textContent = err.message || 'Failed to delete item.';
    alert.style.display = 'block';
    showToast('error', 'Delete Failed', err.message);
  } finally {
    btn.disabled = false;
  }
}

// ============================================================================
// Tab Navigation
// ============================================================================

function switchTab(targetTabId) {
  const navTabs = document.querySelectorAll('.nav-tab');
  const tabPanes = document.querySelectorAll('.tab-pane');

  navTabs.forEach(tab => {
    if (tab.getAttribute('data-tab') === targetTabId) {
      tab.classList.add('active');
    } else {
      tab.classList.remove('active');
    }
  });

  tabPanes.forEach(pane => {
    if (pane.id === targetTabId) {
      pane.classList.add('active');
    } else {
      pane.classList.remove('active');
    }
  });

  // Lazy-load data when switching to specific tabs
  if (targetTabId === 'erasTab') {
    loadEras();
  } else if (targetTabId === 'salesTab') {
    loadSalesReport();
  } else if (targetTabId === 'itemsTab') {
    loadAllItems();
  }
}

// ============================================================================
// Initialization & Event Listeners
// ============================================================================

function initEventListeners() {
  // Nav tabs
  document.querySelectorAll('.nav-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-tab');
      switchTab(target);
    });
  });

  // Toolbar search by ID
  const searchIdBtn = document.getElementById('searchIdBtn');
  const searchIdInput = document.getElementById('searchIdInput');
  searchIdBtn.addEventListener('click', () => {
    searchItemById(searchIdInput.value.trim());
  });
  searchIdInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      searchItemById(searchIdInput.value.trim());
    }
  });

  // Toolbar view modes
  document.getElementById('loadAllItemsBtn').addEventListener('click', loadAllItems);
  document.getElementById('loadSortedItemsBtn').addEventListener('click', loadSortedItems);
  document.getElementById('resetFiltersBtn').addEventListener('click', () => {
    searchIdInput.value = '';
    document.getElementById('minPriceInput').value = '';
    document.getElementById('maxPriceInput').value = '';
    loadAllItems();
  });

  // Toolbar price range filter
  document.getElementById('applyRangeBtn').addEventListener('click', () => {
    const min = document.getElementById('minPriceInput').value.trim();
    const max = document.getElementById('maxPriceInput').value.trim();
    filterItemsByPriceRange(min, max);
  });

  // Item Modals
  document.getElementById('openAddItemBtn').addEventListener('click', openAddItemModal);
  document.getElementById('closeItemModalBtn').addEventListener('click', closeItemModal);
  document.getElementById('cancelItemModalBtn').addEventListener('click', closeItemModal);
  document.getElementById('itemForm').addEventListener('submit', handleItemFormSubmit);

  // Sell Modal
  document.getElementById('openSellItemModalBtn').addEventListener('click', () => openSellItemModal());
  document.getElementById('closeSellModalBtn').addEventListener('click', closeSellItemModal);
  document.getElementById('cancelSellModalBtn').addEventListener('click', closeSellItemModal);
  document.getElementById('sellForm').addEventListener('submit', handleSellFormSubmit);

  // Delete Modal
  document.getElementById('closeDeleteModalBtn').addEventListener('click', closeDeleteModal);
  document.getElementById('cancelDeleteModalBtn').addEventListener('click', closeDeleteModal);
  document.getElementById('confirmDeleteBtn').addEventListener('click', handleConfirmDelete);

  // Close modals on outside click or ESC key
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeItemModal();
      closeSellItemModal();
      closeDeleteModal();
    }
  });

  document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeItemModal();
        closeSellItemModal();
        closeDeleteModal();
      }
    });
  });

  // Add Era Form
  document.getElementById('addEraForm').addEventListener('submit', handleAddEraSubmit);
  document.getElementById('refreshErasBtn').addEventListener('click', loadEras);

  // Valuations
  document.getElementById('addValuationForm').addEventListener('submit', handleAddValuationSubmit);
  document.getElementById('valItemSelect').addEventListener('change', (e) => {
    const val = e.target.value;
    if (val) {
      document.getElementById('lookupValuationItemId').value = val;
      loadValuationsForItem(val);
    }
  });
  document.getElementById('lookupValuationsBtn').addEventListener('click', () => {
    const val = document.getElementById('lookupValuationItemId').value.trim();
    if (val) loadValuationsForItem(val);
  });

  // Sales
  document.getElementById('refreshSalesBtn').addEventListener('click', loadSalesReport);
}

// Initial Bootstrap
document.addEventListener('DOMContentLoaded', async () => {
  initEventListeners();

  // Set default dates
  const today = getTodayDateString();
  const valDateInput = document.getElementById('valDateInput');
  const sellDateInput = document.getElementById('sellDateInput');
  if (valDateInput) valDateInput.value = today;
  if (sellDateInput) sellDateInput.value = today;

  // Initial data loading
  try {
    await loadEras();
    await loadAllItems();
  } catch (err) {
    console.error('Initialization error:', err);
  }
});

/**
 * ARI-OS (AR Industries Operating System) - Centralized Spring Boot + MySQL API Client
 * Connects the frontend to the Java Spring Boot REST backend running on https://ari-os-backend.onrender.com/api/v1
 */

const API_BASE_URL = window.API_BASE_URL || 'https://ari-os-backend.onrender.com/api/v1';
const HEALTH_URL = window.HEALTH_URL || (window.API_BASE_URL ? window.API_BASE_URL.replace(/\/api\/v1\/?$/, '') + '/actuator/health' : 'https://ari-os-backend.onrender.com/actuator/health');

const apiClient = {
  token: localStorage.getItem('ARI_JWT_TOKEN') || null,
  isOnline: false,

  async request(endpoint, options = {}) {
    const method = options.method || 'GET';
    const url = `${API_BASE_URL}${endpoint}`;

    // If an offline placeholder token is present and backend is online, upgrade to real Spring Boot JWT first
    if (this.token && typeof this.token === 'string' && this.token.startsWith('OFFLINE_JWT_') && !endpoint.includes('/auth/login')) {
      try {
        await this.auth.login('admin', 'admin123');
      } catch (_) {}
    }

    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), options.timeout || 10000);
      
      const response = await fetch(url, { ...options, headers, signal: controller.signal });
      clearTimeout(timeoutId);

      // Handle 401 Unauthorized or 403 Forbidden: invalid, offline, or expired token
      if ((response.status === 401 || response.status === 403) && !endpoint.includes('/auth/login')) {
        if (!options._retried) {
          console.warn(`[API Client] Received HTTP ${response.status} on ${endpoint}. Attempting session refresh with Spring Boot...`);
          try {
            const loginRes = await this.auth.login('admin', 'admin123');
            if (loginRes && loginRes.success && loginRes.data && loginRes.data.token && !loginRes.isOfflineDemo) {
              console.log('[API Client] Session refreshed with Spring Boot. Retrying request...');
              return await this.request(endpoint, { ...options, _retried: true });
            }
          } catch (recoveryErr) {
            console.warn('[API Client] Auto-login recovery error:', recoveryErr);
          }
        }

        console.warn(`[API Client] Unauthorized request (${response.status}). Clearing invalid session.`);
        this.token = null;
        localStorage.removeItem('ARI_JWT_TOKEN');
        localStorage.removeItem('ARI_USER_INFO');
        if (typeof showAuthView === 'function') {
          showAuthView('Session expired or unauthorized. Please sign in again.');
        }
        throw new Error(`HTTP ${response.status} Unauthorized / Forbidden`);
      }

      if (!response.ok) {
        let errData = null;
        try {
          errData = await response.json();
        } catch (_) {}

        const cleanMsg = this.formatApiError(errData, response.status, response.statusText, endpoint);
        console.warn(`[API Client ${response.status} Error] ${endpoint}: ${cleanMsg}`);
        const error = new Error(cleanMsg);
        error.status = response.status;
        error.data = errData;
        error.endpoint = endpoint;
        error.friendlyMessage = cleanMsg;
        throw error;
      }

      this.isOnline = true;
      this.updateStatusUI(true);

      if (response.status === 204) return null;
      const data = await response.json();
      return data;
    } catch (err) {
      // Determine if error is network / backend offline
      const isNetworkError = err.name === 'AbortError' || err.message.includes('Failed to fetch') || err.message.includes('NetworkError') || err.message.includes('network');
      if (isNetworkError) {
        this.isOnline = false;
        this.updateStatusUI(false);
      }
      throw err;
    }
  },

  formatApiError(errData, status, statusText, endpoint = '') {
    if (!errData) return `Server returned HTTP ${status || 500} ${statusText || 'Error'}`;

    let msg = '';

    // 1. Structured validation errors from Spring Boot (MethodArgumentNotValidException)
    if (errData.fieldErrors && typeof errData.fieldErrors === 'object') {
      const fieldList = Object.entries(errData.fieldErrors)
        .map(([field, error]) => `${field}: ${error}`)
        .join('; ');
      if (fieldList) {
        msg = `Validation failed: ${fieldList}`;
      }
    } else if (errData.errors) {
      if (Array.isArray(errData.errors)) {
        msg = errData.errors.map(e => (typeof e === 'object' ? (e.defaultMessage || e.message || JSON.stringify(e)) : e)).join('; ');
      } else if (typeof errData.errors === 'object') {
        msg = Object.entries(errData.errors).map(([k, v]) => `${k}: ${v}`).join('; ');
      }
    }

    // 2. Main message or error field
    if (!msg) {
      msg = errData.message || errData.error || '';
    }

    // 3. Fallback
    if (!msg || typeof msg !== 'string') {
      msg = `Server returned HTTP ${status || 500} ${statusText || 'Error'}`;
    }

    // 4. Sanitize against any raw database / SQL / hibernate leakages
    const lower = msg.toLowerCase();
    if (lower.includes('could not execute statement') || lower.includes('duplicate entry') || lower.includes('unique index') || lower.includes('uk_')) {
      if (lower.includes('price') || endpoint.includes('/prices')) {
        return "A price record with this thickness, clip type, and size already exists. Please update the existing record.";
      }
      if (lower.includes('gstin')) {
        return "A customer with this GSTIN number is already registered in the system.";
      }
      if (lower.includes('customer') || endpoint.includes('/customers')) {
        return "A customer account with these details already exists.";
      }
      if (lower.includes('item_code') || lower.includes('inventory') || endpoint.includes('/inventory')) {
        return "An inventory stock item with this item code already exists.";
      }
      if (lower.includes('invoice') || endpoint.includes('/invoices')) {
        return "An invoice with this invoice number already exists.";
      }
      if (lower.includes('employee') || endpoint.includes('/employees')) {
        return "An employee with this code or phone number already exists.";
      }
      if (lower.includes('attendance') || endpoint.includes('/attendance')) {
        return "Attendance has already been recorded for this staff member on this date.";
      }
      return "A record with these details already exists. Duplicate entries are not permitted.";
    }

    if (lower.includes('foreign key constraint fails') || lower.includes('cannot delete or update a parent row')) {
      return "Cannot delete or alter this record because it is referenced by other invoices, stock items, or transactions.";
    }

    if (lower.includes('data too long')) {
      return "One of the entered values exceeds the maximum character length permitted.";
    }

    if (lower.includes('cannot be null') || lower.includes('column cannot be null')) {
      return "A required field is missing. Please ensure all required fields are filled out.";
    }

    if (lower.includes('sql') || lower.includes('hibernate') || lower.includes('org.springframework')) {
      return "A system validation constraint prevented this operation. Please check your inputs and try again.";
    }

    return msg;
  },

  updateStatusUI(isOnline) {
    this.isOnline = isOnline;
    
    // 1. Header status badge
    const badgeText = document.getElementById('backend-status-text');
    const badgeDot = document.getElementById('backend-status-dot');
    const badge = document.getElementById('backend-status-badge');

    if (badge && badgeText && badgeDot) {
      if (isOnline) {
        badge.className = "cursor-pointer text-[11px] sm:text-xs font-semibold px-2.5 sm:px-3 py-1.5 rounded-lg border flex items-center gap-1.5 transition bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100";
        badge.title = "Spring Boot + MySQL Backend Connected (Click to refresh)";
        badgeDot.className = "w-2 h-2 rounded-full bg-emerald-500 animate-pulse";
        badgeText.textContent = "Backend: Connected (MySQL :8080)";
      } else {
        badge.className = "cursor-pointer text-[11px] sm:text-xs font-semibold px-2.5 sm:px-3 py-1.5 rounded-lg border flex items-center gap-1.5 transition bg-rose-50 text-rose-700 border-rose-200 hover:bg-rose-100";
        badge.title = "Backend Server Offline (Port 8080). Click to test/retry connection.";
        badgeDot.className = "w-2 h-2 rounded-full bg-rose-500";
        badgeText.textContent = "Backend: Offline (Port 8080)";
      }
    }

    // 2. Global Offline Warning Banner
    const offlineBanner = document.getElementById('backend-offline-banner');
    if (offlineBanner) {
      if (isOnline) {
        offlineBanner.classList.add('hidden');
      } else {
        offlineBanner.classList.remove('hidden');
      }
    }

    // 3. Login view connection badge (if visible)
    const loginStatusPill = document.getElementById('login-backend-status');
    if (loginStatusPill) {
      if (isOnline) {
        loginStatusPill.innerHTML = `
          <div class="flex items-center gap-2 p-2 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs font-medium">
            <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>Backend Online: Spring Boot 3.3 (MySQL :8080)</span>
          </div>
        `;
      } else {
        loginStatusPill.innerHTML = `
          <div class="flex items-center justify-between p-2.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-rose-500 flex-shrink-0"></span>
              <span><strong>Backend Offline:</strong> Spring Boot not reachable on port 8080.</span>
            </div>
            <button type="button" onclick="checkBackendHealth(true)" class="underline font-bold text-rose-800 hover:text-rose-950 ml-2">Retry</button>
          </div>
        `;
      }
    }
    if (typeof window.refreshIcons === 'function') {
      window.refreshIcons();
    } else if (typeof window.lucide !== 'undefined' && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  },

  // === AUTH & USERS ===
  auth: {
    isAuthenticated() {
      return Boolean(apiClient.token);
    },

    getUser() {
      try {
        const u = localStorage.getItem('ARI_USER_INFO');
        return u ? JSON.parse(u) : null;
      } catch (_) {
        return null;
      }
    },

    async login(username, password) {
      try {
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password })
        });

        if (res.ok) {
          const data = await res.json();
          apiClient.token = data.token;
          localStorage.setItem('ARI_JWT_TOKEN', data.token);

          const userInfo = {
            username: data.username,
            fullName: data.fullName || 'System Administrator',
            role: data.role || 'SUPER_ADMIN'
          };
          localStorage.setItem('ARI_USER_INFO', JSON.stringify(userInfo));

          apiClient.isOnline = true;
          apiClient.updateStatusUI(true);

          return { success: true, data, user: userInfo };
        } else {
          let errData = {};
          try { errData = await res.json(); } catch (_) {}
          const errorMsg = errData.message || (res.status === 401 ? 'Invalid username or password' : `Login failed (HTTP ${res.status})`);
          return { success: false, error: errorMsg, isOffline: false };
        }
      } catch (err) {
        apiClient.isOnline = false;
        apiClient.updateStatusUI(false);
        // Fallback demo authentication for offline testing when Spring Boot is offline
        if (username === 'admin' && password === 'admin123') {
          const offlineToken = 'OFFLINE_JWT_' + Date.now();
          apiClient.token = offlineToken;
          localStorage.setItem('ARI_JWT_TOKEN', offlineToken);
          const userInfo = {
            username: 'admin',
            fullName: 'A. Rahman',
            role: 'SUPER_ADMIN'
          };
          localStorage.setItem('ARI_USER_INFO', JSON.stringify(userInfo));
          return { success: true, data: { token: offlineToken }, user: userInfo, isOfflineDemo: true };
        }
        return {
          success: false,
          error: 'Cannot connect to backend server at http://localhost:8080. (Default credentials: admin / admin123)',
          isOffline: true
        };
      }
    },

    logout() {
      apiClient.token = null;
      try {
        localStorage.removeItem('ARI_JWT_TOKEN');
        localStorage.removeItem('ARI_USER_INFO');
        sessionStorage.clear();
      } catch (_) {}
      apiClient.isOnline = false;
      apiClient.updateStatusUI(false);
      if (typeof showAuthView === 'function') {
        showAuthView();
      }
    },

    async me() {
      return apiClient.request('/auth/me');
    }
  },

  // === HEALTH PROBING ===
  async checkHealth(manual = false) {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 3500);

      // Probe actuator/health to see if Spring Boot is running
      const res = await fetch(HEALTH_URL, { method: 'GET', signal: controller.signal });
      clearTimeout(timeoutId);

      const isUp = res.ok;
      apiClient.isOnline = isUp;
      apiClient.updateStatusUI(isUp);

      // If backend is up and token is offline mock token or missing, auto-authenticate with Spring Boot
      if (isUp && (!apiClient.token || (typeof apiClient.token === 'string' && apiClient.token.startsWith('OFFLINE_JWT_')))) {
        try {
          await apiClient.auth.login('admin', 'admin123');
        } catch (_) {}
      }

      if (manual && typeof showToast === 'function') {
        if (isUp) {
          showToast("Backend Online", "Spring Boot is reachable on http://localhost:8080", "success");
        } else {
          showToast("Backend Error", `Server returned HTTP ${res.status}`, "error");
        }
      }
      return isUp;
    } catch (err) {
      // Actuator probe failed, try fallback probe to auth/me
      try {
        const probeUrl = `${API_BASE_URL}/auth/me`;
        const headers = (apiClient.token && typeof apiClient.token === 'string' && !apiClient.token.startsWith('OFFLINE_JWT_')) 
          ? { 'Authorization': `Bearer ${apiClient.token}` } 
          : {};
        const res2 = await fetch(probeUrl, { method: 'GET', headers });
        const isUp2 = res2.ok || res2.status === 401 || res2.status === 403;
        apiClient.isOnline = isUp2;
        apiClient.updateStatusUI(isUp2);
        if (isUp2 && (!apiClient.token || (typeof apiClient.token === 'string' && apiClient.token.startsWith('OFFLINE_JWT_')))) {
          try {
            await apiClient.auth.login('admin', 'admin123');
          } catch (_) {}
        }
        return isUp2;
      } catch (_) {
        apiClient.isOnline = false;
        apiClient.updateStatusUI(false);
        if (manual && typeof showToast === 'function') {
          showToast("Backend Offline", "Could not reach Spring Boot server at http://localhost:8080", "error");
        }
        return false;
      }
    }
  },

  // === DASHBOARD TELEMETRY ===
  dashboard: {
    async getMetrics() {
      return apiClient.request('/dashboard');
    }
  },

  // === CUSTOMERS ===
  customers: {
    async getAll({ page = 0, size = 100, search = '' } = {}) {
      const params = new URLSearchParams({ page, size });
      if (search) params.append('search', search);
      return apiClient.request(`/customers?${params.toString()}`);
    },

    async getById(id) {
      return apiClient.request(`/customers/${id}`);
    },

    async create(data) {
      return apiClient.request('/customers', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async update(id, data) {
      return apiClient.request(`/customers/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    async delete(id) {
      return apiClient.request(`/customers/${id}`, { method: 'DELETE' });
    }
  },

  // === PRICE MASTER ===
  prices: {
    async getAll({ page = 0, size = 100, category = '', thickness = '', productType = '', search = '' } = {}) {
      const params = new URLSearchParams({ page, size });
      if (category) params.append('category', category);
      if (thickness) params.append('thickness', thickness);
      if (productType) params.append('productType', productType);
      if (search) params.append('search', search);
      return apiClient.request(`/prices?${params.toString()}`);
    },

    async getById(id) {
      return apiClient.request(`/prices/${id}`);
    },

    async lookup({ category = '', thickness, productType, size }) {
      const params = new URLSearchParams({ thickness, productType, size });
      if (category) params.append('category', category);
      return apiClient.request(`/prices/lookup?${params.toString()}`);
    },

    async create(data) {
      return apiClient.request('/prices', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async update(id, data) {
      return apiClient.request(`/prices/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    async delete(id) {
      return apiClient.request(`/prices/${id}`, { method: 'DELETE' });
    }
  },

  // === INVENTORY ===
  inventory: {
    async getAll({ page = 0, size = 100, category = '', search = '', lowStock = false } = {}) {
      const params = new URLSearchParams({ page, size });
      if (category) params.append('category', category);
      if (search) params.append('search', search);
      if (lowStock) params.append('lowStock', 'true');
      return apiClient.request(`/inventory?${params.toString()}`);
    },

    async getById(id) {
      return apiClient.request(`/inventory/${id}`);
    },

    async create(data) {
      return apiClient.request('/inventory', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async update(id, data) {
      return apiClient.request(`/inventory/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    async delete(id) {
      return apiClient.request(`/inventory/${id}`, { method: 'DELETE' });
    },

    async adjust(data) {
      return apiClient.request('/inventory/adjust', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async getMovements(id, { page = 0, size = 20 } = {}) {
      return apiClient.request(`/inventory/${id}/movements?page=${page}&size=${size}`);
    }
  },

  // === INVOICES & BILLING ===
  invoices: {
    async getAll({ page = 0, size = 100, search = '', status = '', paymentStatus = '' } = {}) {
      const params = new URLSearchParams({ page, size });
      if (search) params.append('search', search);
      if (status) params.append('status', status);
      if (paymentStatus) params.append('paymentStatus', paymentStatus);
      return apiClient.request(`/invoices?${params.toString()}`);
    },

    async getById(id) {
      return apiClient.request(`/invoices/${id}`);
    },

    async create(data) {
      return apiClient.request('/invoices', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async cancel(id) {
      return apiClient.request(`/invoices/${id}/cancel`, { method: 'POST' });
    }
  },

  // === PAYMENTS ===
  payments: {
    async getAll({ page = 0, size = 50, customerId } = {}) {
      const params = new URLSearchParams({ page, size });
      if (customerId) params.append('customerId', customerId);
      return apiClient.request(`/payments?${params.toString()}`);
    },

    async recordPayment(data) {
      return apiClient.request('/payments', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    }
  },

  // === EMPLOYEES ===
  employees: {
    async getAll({ page = 0, size = 100, search = '' } = {}) {
      const params = new URLSearchParams({ page, size });
      if (search) params.append('search', search);
      return apiClient.request(`/employees?${params.toString()}`);
    },

    async getById(id) {
      return apiClient.request(`/employees/${id}`);
    },

    async create(data) {
      return apiClient.request('/employees', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async update(id, data) {
      return apiClient.request(`/employees/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      });
    },

    async delete(id) {
      return apiClient.request(`/employees/${id}`, { method: 'DELETE' });
    }
  },

  // === ATTENDANCE ===
  attendance: {
    async getAll({ page = 0, size = 50, employeeId, fromDate, toDate } = {}) {
      const params = new URLSearchParams({ page, size });
      if (employeeId) params.append('employeeId', employeeId);
      if (fromDate) params.append('fromDate', fromDate);
      if (toDate) params.append('toDate', toDate);
      return apiClient.request(`/attendance?${params.toString()}`);
    },

    async getDaily(date) {
      const d = date || new Date().toISOString().split('T')[0];
      return this.getAll({ fromDate: d, toDate: d });
    },

    async checkIn(data) {
      return apiClient.request('/attendance/check-in', {
        method: 'POST',
        body: JSON.stringify(data)
      });
    },

    async checkOut(employeeId) {
      return apiClient.request(`/attendance/check-out/${employeeId}`, { method: 'POST' });
    },

    async getSummary() {
      return apiClient.request('/attendance/summary');
    }
  },

  // === AUDIT LOGS ===
  audit: {
    async getAll({ page = 0, size = 50 } = {}) {
      return apiClient.request(`/audit?page=${page}&size=${size}`);
    }
  }
};

/**
 * Sync UI with Spring Boot Backend
 */
async function syncStateWithBackend(notify = false) {
  if (!apiClient.auth.isAuthenticated()) {
    apiClient.updateStatusUI(false);
    return;
  }

  try {
    const [custRes, priceRes, invRes, invsRes, empRes] = await Promise.allSettled([
      apiClient.customers.getAll({ size: 100 }),
      apiClient.prices.getAll({ size: 100 }),
      apiClient.inventory.getAll({ size: 100 }),
      apiClient.invoices.getAll({ size: 100 }),
      apiClient.employees.getAll({ size: 100 })
    ]);

    let loadedAny = false;

    if (custRes.status === 'fulfilled' && custRes.value && custRes.value.data) {
      appState.customers = custRes.value.data.map(c => ({
        id: c.customerCode || ("CUST-" + c.id),
        numericId: c.id,
        name: c.customerName,
        contact: c.companyName || c.customerName,
        phone: c.phone || "",
        gstin: c.gstin || "",
        state: c.state || "Maharashtra",
        stateCode: c.stateCode || "27",
        address: c.billingAddress || "",
        balance: c.outstandingBalance != null ? Number(c.outstandingBalance) : 0
      }));
      loadedAny = true;
    }

    if (priceRes.status === 'fulfilled' && priceRes.value && priceRes.value.data) {
      appState.priceList = priceRes.value.data.map(p => ({
        id: p.priceCode || ("PL-" + p.id),
        numericId: p.id,
        thickness: p.thickness,
        type: p.productType,
        size: p.size,
        packaging: p.packaging,
        wholesaleRate: Number(p.wholesaleRate),
        retailRate: Number(p.retailRate),
        rate: Number(p.wholesaleRate)
      }));
      loadedAny = true;
    }

    if (invRes.status === 'fulfilled' && invRes.value && invRes.value.data) {
      appState.inventory = invRes.value.data.map(i => ({
        id: i.itemCode || ("INV-" + i.id),
        numericId: i.id,
        code: i.itemCode,
        name: i.itemName,
        category: i.category,
        thickness: i.thickness,
        type: i.productType,
        size: i.size,
        packaging: i.packaging,
        stock: i.currentStock,
        min: i.minimumStock,
        unit: i.unit || "Pcs"
      }));
      loadedAny = true;
    }

    if (invsRes.status === 'fulfilled' && invsRes.value && invsRes.value.data) {
      appState.invoices = invsRes.value.data.map(inv => ({
        id: inv.invoiceNumber,
        numericId: inv.id,
        date: inv.invoiceDate,
        customerId: inv.customerName,
        customerName: inv.customerName,
        customerGstin: inv.customerGstin,
        isInterstate: inv.interstate,
        items: (inv.items || []).map(it => ({
          category: it.category,
          thickness: it.thickness,
          type: it.productType,
          size: it.size,
          packaging: it.packaging,
          qty: it.quantity,
          rate: Number(it.ratePerPc),
          amount: Number(it.taxableAmount)
        })),
        subtotal: Number(inv.subtotal),
        cgst: Number(inv.cgst),
        sgst: Number(inv.sgst),
        igst: Number(inv.igst),
        total: Number(inv.grandTotal),
        status: inv.paymentStatus === 'PAID' ? 'Paid' : 'Pending',
        mode: inv.paymentMode || 'NEFT'
      }));
      loadedAny = true;
    }

    if (empRes.status === 'fulfilled' && empRes.value && empRes.value.data) {
      appState.employees = empRes.value.data.map(e => ({
        id: e.employeeCode || ("EMP-" + e.id),
        numericId: e.id,
        name: e.fullName || `${e.firstName} ${e.lastName}`,
        role: e.designation || "Staff",
        dept: e.department || "Production",
        phone: e.phone || "",
        salary: Number(e.basicSalary || 0),
        status: "Present",
        checkIn: "08:52 AM"
      }));
      loadedAny = true;
    }

    if (loadedAny) {
      apiClient.updateStatusUI(true);
      if (notify && typeof showToast === 'function') {
        showToast("Backend Synchronized", "Loaded fresh data from Spring Boot + MySQL", "success");
      }
    } else {
      apiClient.updateStatusUI(false);
      if (notify && typeof showToast === 'function') {
        showToast("Backend Offline", "Cannot reach Spring Boot on http://localhost:8080", "error");
      }
    }

    // Re-render current tab
    if (typeof activeTab !== 'undefined' && typeof switchTab === 'function') {
      switchTab(activeTab);
    }
    if (typeof window.refreshIcons === 'function') {
      window.refreshIcons();
    } else if (typeof window.lucide !== 'undefined' && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  } catch (err) {
    apiClient.updateStatusUI(false);
    if (notify && typeof showToast === 'function') {
      showToast("Backend Offline", "Spring Boot is offline or reconnecting on :8080", "error");
    }
    if (typeof activeTab !== 'undefined' && typeof switchTab === 'function') {
      switchTab(activeTab);
    }
    if (typeof window.refreshIcons === 'function') {
      window.refreshIcons();
    } else if (typeof window.lucide !== 'undefined' && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  }
}

// Global hook for manual health check button
window.checkBackendHealth = async function(manual = false) {
  const isOnline = await apiClient.checkHealth(manual);
  if (isOnline && apiClient.auth.isAuthenticated()) {
    await syncStateWithBackend(manual);
  }
  return isOnline;
};

let isTabFetching = false;

/**
 * Actively fetch fresh data from backend when user clicks any tab
 */
async function syncTabWithBackend(tabId) {
  if (isTabFetching) return;
  if (!apiClient.auth.isAuthenticated()) return;

  isTabFetching = true;
  const vp = document.getElementById('view-viewport');

  try {
    if (tabId === 'dashboard') {
      const [metrics, invs] = await Promise.allSettled([
        apiClient.dashboard.getMetrics(),
        apiClient.invoices.getAll({ size: 10 })
      ]);
      if (invs.status === 'fulfilled' && invs.value && invs.value.data) {
        appState.invoices = invs.value.data.map(inv => ({
          id: inv.invoiceNumber,
          numericId: inv.id,
          date: inv.invoiceDate,
          customerName: inv.customerName,
          customerGstin: inv.customerGstin,
          isInterstate: inv.interstate,
          items: (inv.items || []).map(it => ({
            category: it.category,
            thickness: it.thickness,
            type: it.productType,
            size: it.size,
            qty: it.quantity,
            rate: Number(it.ratePerPc),
            amount: Number(it.taxableAmount)
          })),
          subtotal: Number(inv.subtotal),
          cgst: Number(inv.cgst),
          sgst: Number(inv.sgst),
          igst: Number(inv.igst),
          total: Number(inv.grandTotal),
          status: inv.paymentStatus === 'PAID' ? 'Paid' : 'Pending',
          mode: inv.paymentMode || 'NEFT'
        }));
      }
      if (metrics.status === 'fulfilled' && metrics.value) {
        window.dashboardTelemetry = metrics.value;
      }
      if (vp && activeTab === 'dashboard' && typeof renderDashboard === 'function') renderDashboard(vp);

    } else if (tabId === 'customers') {
      const res = await apiClient.customers.getAll({ size: 100 });
      if (res && res.data) {
        appState.customers = res.data.map(c => ({
          id: c.customerCode || ("CUST-" + c.id),
          numericId: c.id,
          name: c.customerName,
          contact: c.companyName || c.customerName,
          phone: c.phone || "",
          gstin: c.gstin || "",
          state: c.state || "Maharashtra",
          stateCode: c.stateCode || "27",
          address: c.billingAddress || "",
          balance: c.outstandingBalance != null ? Number(c.outstandingBalance) : 0
        }));
        if (vp && activeTab === 'customers' && typeof renderCustomers === 'function') renderCustomers(vp);
      }

    } else if (tabId === 'price-master') {
      const res = await apiClient.prices.getAll({ size: 100 });
      if (res && res.data) {
        appState.priceList = res.data.map(p => ({
          id: p.priceCode || ("PL-" + p.id),
          numericId: p.id,
          thickness: p.thickness,
          type: p.productType,
          size: p.size,
          packaging: p.packaging,
          wholesaleRate: Number(p.wholesaleRate),
          retailRate: Number(p.retailRate),
          rate: Number(p.wholesaleRate)
        }));
        if (vp && activeTab === 'price-master' && typeof renderPriceMaster === 'function') renderPriceMaster(vp);
      }

    } else if (tabId === 'inventory') {
      const res = await apiClient.inventory.getAll({ size: 100 });
      if (res && res.data) {
        appState.inventory = res.data.map(i => ({
          id: i.itemCode || ("INV-" + i.id),
          numericId: i.id,
          code: i.itemCode,
          name: i.itemName,
          category: i.category,
          thickness: i.thickness,
          type: i.productType,
          size: i.size,
          packaging: i.packaging,
          stock: i.currentStock,
          min: i.minimumStock,
          unit: i.unit || "Pcs"
        }));
        if (vp && activeTab === 'inventory' && typeof renderInventory === 'function') renderInventory(vp);
      }

    } else if (tabId === 'invoices') {
      const res = await apiClient.invoices.getAll({ size: 100 });
      if (res && res.data) {
        appState.invoices = res.data.map(inv => ({
          id: inv.invoiceNumber,
          numericId: inv.id,
          date: inv.invoiceDate,
          customerId: inv.customerName,
          customerName: inv.customerName,
          customerGstin: inv.customerGstin,
          isInterstate: inv.interstate,
          items: (inv.items || []).map(it => ({
            category: it.category,
            thickness: it.thickness,
            type: it.productType,
            size: it.size,
            packaging: it.packaging,
            qty: it.quantity,
            rate: Number(it.ratePerPc),
            amount: Number(it.taxableAmount)
          })),
          subtotal: Number(inv.subtotal),
          cgst: Number(inv.cgst),
          sgst: Number(inv.sgst),
          igst: Number(inv.igst),
          total: Number(inv.grandTotal),
          status: inv.paymentStatus === 'PAID' ? 'Paid' : 'Pending',
          mode: inv.paymentMode || 'NEFT'
        }));
        if (vp && activeTab === 'invoices' && typeof renderInvoices === 'function') renderInvoices(vp);
      }

    } else if (tabId === 'employees') {
      const res = await apiClient.employees.getAll({ size: 100 });
      if (res && res.data) {
        appState.employees = res.data.map(e => ({
          id: e.employeeCode || ("EMP-" + e.id),
          numericId: e.id,
          name: e.fullName || `${e.firstName} ${e.lastName}`,
          role: e.designation || "Staff",
          dept: e.department || "Production",
          phone: e.phone || "",
          salary: Number(e.basicSalary || 0),
          status: "Present",
          checkIn: "08:52 AM"
        }));
        if (vp && activeTab === 'employees' && typeof renderEmployees === 'function') renderEmployees(vp);
      }

    } else if (tabId === 'attendance') {
      const today = new Date().toISOString().split('T')[0];
      const res = await apiClient.attendance.getDaily(today);
      if (res && res.data) {
        window.todayAttendance = res.data;
        if (vp && activeTab === 'attendance' && typeof renderAttendance === 'function') renderAttendance(vp);
      }

    } else if (tabId === 'audit') {
      const res = await apiClient.audit.getAll({ size: 50 });
      if (res && res.data) {
        appState.audit = res.data.map(a => ({
          id: "LOG-" + a.id,
          action: `${a.action} (${a.entityName || ''}) - ${a.description || ''}`,
          user: a.performedBy || 'System',
          time: a.createdAt || new Date().toISOString()
        }));
        if (vp && activeTab === 'audit' && typeof renderAudit === 'function') renderAudit(vp);
      }
    }
  } catch (err) {
    console.warn(`[API Client] Sync error on tab ${tabId}:`, err.message);
    if (!apiClient.isOnline && vp) {
      if (typeof renderBackendOfflineState === 'function') {
        renderBackendOfflineState(vp, tabId);
      }
    }
  } finally {
    isTabFetching = false;
    if (typeof window.refreshIcons === 'function') {
      window.refreshIcons();
    } else if (typeof window.lucide !== 'undefined' && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  }
}

window.apiClient = apiClient;
window.syncTabWithBackend = syncTabWithBackend;
window.syncStateWithBackend = syncStateWithBackend;

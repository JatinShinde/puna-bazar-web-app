function isCommEnabledForCustomer(customer) {
    if (!customer) return false;
    return customer.commissionEnabled === true || customer.commissionEnabled === 'true' || customer.commissionEnabled === 1;
}

function isPagarEnabledForCustomer(customer) {
    if (!customer) return false;
    return customer.pagarEnabled === true || customer.pagarEnabled === 'true' || customer.pagarEnabled === 1;
}

function isShareEnabledForCustomer(customer) {
    if (!customer) return false;
    if (customer.share30ProfitOnly === true || customer.share30ProfitOnly === 'true') return true;
    return customer.shareRate != null && customer.shareRate > 0 && customer.shareRate < 100.0;
}

document.addEventListener('DOMContentLoaded', () => {
    const localDate = new Date();
    const localYyyy = localDate.getFullYear();
    const localMm = String(localDate.getMonth() + 1).padStart(2, '0');
    const localDd = String(localDate.getDate()).padStart(2, '0');
    const todayStr = `${localYyyy}-${localMm}-${localDd}`;

    const globalPicker = document.getElementById('globalDatePicker');
    const txPicker = document.getElementById('txDate');

    if (globalPicker) {
        globalPicker.value = todayStr;
        const onGlobalDateChange = () => {
            const selectedDate = globalPicker.value;
            if (txPicker) txPicker.value = selectedDate;
            loadDashboardMetrics(selectedDate);
            if (typeof populateAndRenderReceiptsDropdown === 'function') {
                populateAndRenderReceiptsDropdown(selectedDate);
            }
        };
        globalPicker.addEventListener('change', onGlobalDateChange);
        globalPicker.addEventListener('input', onGlobalDateChange);
    }

    if (txPicker) {
        txPicker.value = todayStr;
        const onTxDateChange = () => {
            const selectedDate = txPicker.value;
            if (globalPicker) globalPicker.value = selectedDate;
            checkIfMarketAlreadyUploaded();
            loadDashboardMetrics(selectedDate);
            if (typeof populateAndRenderReceiptsDropdown === 'function') {
                populateAndRenderReceiptsDropdown(selectedDate);
            }
        };
        txPicker.addEventListener('change', onTxDateChange);
        txPicker.addEventListener('input', onTxDateChange);
    }

    checkAuth();
    loadDashboardMetrics();
    loadCustomers();

    // Event Listeners for Live Math Calculator Engine
    ['tradeReceiptStyle', 'commPercent', 'shareRatePercent', 'tradeYene', 'tradeDene', 'farakAmount', 'pagarAmount'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('input', () => {
                if (id === 'tradeReceiptStyle') {
                    updateInputFieldsVisibility(el.value);
                }
                calculateMathPreview();
            });
            el.addEventListener('change', () => {
                if (id === 'tradeReceiptStyle') {
                    updateInputFieldsVisibility(el.value);
                }
                calculateMathPreview();
            });
        }
    });

    document.getElementById('selectCustomer').addEventListener('change', (e) => {
        const custId = e.target.value;
        if (custId) {
            const customer = customersData.find(c => c.id == custId);
            if (customer) {
                let style = (customer.receiptStyle || 'TYPE_3').trim().toUpperCase();

                const isCommEnabled = isCommEnabledForCustomer(customer);
                document.getElementById('commPercent').value = isCommEnabled ? (customer.commissionRate != null ? customer.commissionRate : 10.0) : 10.0;
                
                const isShareEnabled = isShareEnabledForCustomer(customer);
                document.getElementById('shareRatePercent').value = isShareEnabled ? customer.shareRate : 100.0;
                
                document.getElementById('tradeYene').value = customer.yene != null ? customer.yene : (customer.previousBalance > 0 ? customer.previousBalance : 0);
                document.getElementById('tradeDene').value = customer.dene != null ? customer.dene : (customer.previousBalance < 0 ? Math.abs(customer.previousBalance) : 0);
                
                const isPagarEnabled = isPagarEnabledForCustomer(customer);
                document.getElementById('pagarAmount').value = isPagarEnabled ? (customer.pagar || 0) : 0;
                document.getElementById('farakAmount').value = 0;

                const commGroup = document.getElementById('commGroup');
                if (commGroup) commGroup.style.display = isCommEnabled ? 'block' : 'none';

                const pagarGroup = document.getElementById('pagarGroup');
                if (pagarGroup) pagarGroup.style.display = isPagarEnabled ? 'block' : 'none';

                const shareGroup = document.getElementById('shareGroup');
                if (shareGroup) shareGroup.style.display = isShareEnabled ? 'block' : 'none';

                updateInputFieldsVisibility(style);
                renderCustomerMarketInputs(customer.marketCodes || 'PO,PC');
            }
        } else {
            document.getElementById('commPercent').value = '10.00';
            document.getElementById('shareRatePercent').value = '40.00';
            document.getElementById('tradeYene').value = '0';
            document.getElementById('tradeDene').value = '0';
            document.getElementById('pagarAmount').value = '0';
            document.getElementById('farakAmount').value = '0';

            const commGroup = document.getElementById('commGroup');
            if (commGroup) commGroup.style.display = 'none';

            const pagarGroup = document.getElementById('pagarGroup');
            if (pagarGroup) pagarGroup.style.display = 'none';

            const shareGroup = document.getElementById('shareGroup');
            if (shareGroup) shareGroup.style.display = 'none';

            updateInputFieldsVisibility('TYPE_3');
            renderCustomerMarketInputs('PO,PC');
        }
        calculateMathPreview();
        checkIfMarketAlreadyUploaded();
    });

function setTradeFormFieldsLocked(isLocked) {
    const tradeForm = document.getElementById('tradeForm');
    if (!tradeForm) return;

    const inputs = tradeForm.querySelectorAll('input:not(#selectCustomer):not(#txDate), select:not(#selectCustomer)');
    inputs.forEach(el => {
        if (isLocked) {
            el.disabled = true;
            el.style.opacity = '0.45';
            el.style.cursor = 'not-allowed';
            el.style.pointerEvents = 'none';
        } else {
            if (!el.hasAttribute('readonly')) {
                el.disabled = false;
                el.style.opacity = '1';
                el.style.cursor = 'default';
                el.style.pointerEvents = 'auto';
            }
        }
    });

    const btnSave = document.getElementById('btnSaveTrade');
    const btnShare = document.getElementById('btnShareTrade');
    if (btnSave) {
        btnSave.disabled = isLocked;
        btnSave.style.opacity = isLocked ? '0.45' : '1';
        btnSave.style.cursor = isLocked ? 'not-allowed' : 'pointer';
        btnSave.style.pointerEvents = isLocked ? 'none' : 'auto';
    }
    if (btnShare) {
        btnShare.disabled = isLocked;
        btnShare.style.opacity = isLocked ? '0.45' : '1';
        btnShare.style.cursor = isLocked ? 'not-allowed' : 'pointer';
        btnShare.style.pointerEvents = isLocked ? 'none' : 'auto';
    }
}

window.checkIfMarketAlreadyUploaded = async function() {
    const custId = document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '';
    const dateVal = document.getElementById('txDate') ? document.getElementById('txDate').value : '';
    const noticeEl = document.getElementById('alreadyUploadedNotice');

    if (!custId || !dateVal) {
        if (noticeEl) noticeEl.style.display = 'none';
        setTradeFormFieldsLocked(false);
        return false;
    }

    try {
        const res = await fetch(`/api/transactions/check-exists?customerId=${custId}&date=${dateVal}`);
        if (res.ok) {
            const data = await res.json();
            if (data && data.exists) {
                if (noticeEl) noticeEl.style.display = 'block';
                setTradeFormFieldsLocked(true);
                return true;
            }
        }
    } catch (err) {
        console.error('Error checking transaction existence:', err);
    }

    if (noticeEl) noticeEl.style.display = 'none';
    setTradeFormFieldsLocked(false);
    return false;
};

function updateInputFieldsVisibility(receiptStyle) {
    const commGroup = document.getElementById('commGroup');
    const shareGroup = document.getElementById('shareGroup');
    const farakGroup = document.getElementById('farakGroup');
    const pagarGroup = document.getElementById('pagarGroup');

    const custId = document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '';
    const customer = custId ? customersData.find(c => c.id == custId) : null;
    const isCommEnabled = isCommEnabledForCustomer(customer);
    const isPagarEnabled = isPagarEnabledForCustomer(customer);
    const isShareEnabled = isShareEnabledForCustomer(customer);

    if (commGroup) commGroup.style.display = isCommEnabled ? 'block' : 'none';
    if (pagarGroup) pagarGroup.style.display = isPagarEnabled ? 'block' : 'none';
    if (shareGroup) shareGroup.style.display = isShareEnabled ? 'block' : 'none';
}

    document.getElementById('tradeForm').addEventListener('submit', (e) => {
        e.preventDefault();
        processTradeAction('save');
    });
    document.getElementById('newCustomerForm').addEventListener('submit', handleNewCustomerSubmit);

    // Modal checkbox toggle listeners
    const custCommCheckbox = document.getElementById('custEnableComm');
    if (custCommCheckbox) {
        custCommCheckbox.addEventListener('change', (e) => {
            const container = document.getElementById('custCommContainer');
            if (container) container.style.display = e.target.checked ? 'block' : 'none';
        });
    }

    const custPagarCheckbox = document.getElementById('custEnablePagar');
    if (custPagarCheckbox) {
        custPagarCheckbox.addEventListener('change', (e) => {
            const container = document.getElementById('custPagarContainer');
            if (container) container.style.display = e.target.checked ? 'block' : 'none';
        });
    }

    const custShareCheckbox = document.getElementById('custShare4060');
    if (custShareCheckbox) {
        custShareCheckbox.addEventListener('change', (e) => {
            const container = document.getElementById('custShareRateContainer');
            if (container) container.style.display = e.target.checked ? 'block' : 'none';
        });
    }

    // Search and Market Filter Chips
    document.getElementById('searchInput').addEventListener('input', (e) => {
        if (currentSegment === 'weekly') {
            renderWeeklyTable(weeklyReceiptsData);
        } else {
            loadCustomers(e.target.value, getSelectedMarket());
        }
    });

    document.querySelectorAll('.market-chips .chip').forEach(chip => {
        chip.addEventListener('click', (e) => {
            document.querySelectorAll('.market-chips .chip').forEach(c => c.classList.remove('active'));
            e.target.classList.add('active');
            const market = e.target.getAttribute('data-market');
            if (currentSegment === 'weekly') {
                renderWeeklyTable(weeklyReceiptsData);
            } else {
                loadCustomers(document.getElementById('searchInput').value, market);
            }
        });
    });

    // New Customer Modal Open
    document.getElementById('btnNewCustomerModal').addEventListener('click', () => {
        if (document.getElementById('editingCustId')) document.getElementById('editingCustId').value = '';
        if (document.getElementById('custModalTitle')) document.getElementById('custModalTitle').textContent = '➕ Add New Market Customer';
        if (document.getElementById('btnSaveCust')) document.getElementById('btnSaveCust').textContent = '💾 Save Customer Profile';
        document.getElementById('newCustomerForm').reset();

        if (document.getElementById('custEnableComm')) document.getElementById('custEnableComm').checked = false;
        if (document.getElementById('custCommission')) document.getElementById('custCommission').value = '10.00';
        if (document.getElementById('custCommContainer')) document.getElementById('custCommContainer').style.display = 'none';

        if (document.getElementById('custEnablePagar')) document.getElementById('custEnablePagar').checked = false;
        if (document.getElementById('custPagar')) document.getElementById('custPagar').value = '0.00';
        if (document.getElementById('custPagarContainer')) document.getElementById('custPagarContainer').style.display = 'none';

        if (document.getElementById('custShare4060')) document.getElementById('custShare4060').checked = false;
        if (document.getElementById('custShareRateVal')) document.getElementById('custShareRateVal').value = '40.00';
        if (document.getElementById('custShareRateContainer')) document.getElementById('custShareRateContainer').style.display = 'none';

        openModal('customerModal');
    });

    // Initialize default market inputs
    renderCustomerMarketInputs('PO,PC');
});

let customersData = [];

function renderCustomerMarketInputs(marketCodesStr) {
    const container = document.getElementById('dynamicMarketInputs');
    const previewContainer = document.getElementById('dynamicPreviewSessions');
    if (!container || !previewContainer) return;

    const codes = (marketCodesStr || 'PO,PC').split(',').map(s => s.trim().toUpperCase()).filter(Boolean);
    if (codes.length === 0) codes.push('PO', 'PC');

    let inputsHtml = '';
    let previewHtml = '';

    codes.forEach(code => {
        const sellId = `sell_${code}`;
        const payId = `payment_${code}`;

        inputsHtml += `
            <div class="form-row">
                <div class="form-group">
                    <label for="${sellId}">SELL ${code} ₹</label>
                    <input type="number" id="${sellId}" class="form-control market-input" data-session="${code}" data-type="sell" placeholder="e.g. 10000" value="0" step="0.01">
                </div>
                <div class="form-group">
                    <label for="${payId}">PAYMENT ${code} ₹</label>
                    <input type="number" id="${payId}" class="form-control market-input" data-session="${code}" data-type="payment" placeholder="e.g. 5000" value="0" step="0.01">
                </div>
            </div>
        `;

        previewHtml += `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; font-size: 0.9rem; margin-bottom: 0.3rem;">
                <div>${code}:-<span id="preview_sell_${code}">₹0.00</span></div>
                <div><span id="preview_payment_${code}">₹0.00</span></div>
            </div>
        `;
    });

    container.innerHTML = inputsHtml;
    previewContainer.innerHTML = previewHtml;

    container.querySelectorAll('.market-input').forEach(el => {
        el.addEventListener('input', calculateMathPreview);
        el.addEventListener('change', calculateMathPreview);
    });
}

// 1. Auth Check
async function checkAuth() {
    try {
        const res = await fetch('/api/auth/me');
        if (res.ok) {
            const contentType = res.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const data = await res.json();
                if (data && data.authenticated && document.getElementById('loggedInUser')) {
                    document.getElementById('loggedInUser').textContent = (data.username || 'admin') + " (Admin)";
                    return;
                }
            }
        }
    } catch (err) {
        console.warn('Auth check info:', err);
    }
    if (document.getElementById('loggedInUser')) {
        document.getElementById('loggedInUser').textContent = "Admin";
    }
}

async function loadDashboardMetrics(targetDate = null) {
    try {
        const pickerVal = document.getElementById('globalDatePicker')?.value;
        const dateToFetch = targetDate || pickerVal || '';
        let url = '/api/dashboard/metrics';
        if (dateToFetch) url += '?date=' + encodeURIComponent(dateToFetch);

        const res = await fetch(url);
        const data = await res.json();
        latestDashboardData = data;

        if (document.getElementById('kpiTotalSell')) document.getElementById('kpiTotalSell').textContent = formatCurrency(data.todayTotalSell);
        if (document.getElementById('kpiPoSell')) document.getElementById('kpiPoSell').textContent = formatCurrency(data.todayPoSell || 0);
        if (document.getElementById('kpiPcSell')) document.getElementById('kpiPcSell').textContent = formatCurrency(data.todayPcSell || 0);
        if (document.getElementById('kpiTotalPayment')) document.getElementById('kpiTotalPayment').textContent = formatCurrency(data.todayTotalPayment);
        if (document.getElementById('kpiTotalCommission')) document.getElementById('kpiTotalCommission').textContent = formatCurrency(data.todayTotalCommission);
        if (document.getElementById('kpiTotalMissPayment')) document.getElementById('kpiTotalMissPayment').textContent = formatCurrency(data.todayTotalMissPayment || 0);
        if (document.getElementById('kpiTotalPagar')) document.getElementById('kpiTotalPagar').textContent = formatCurrency(data.todayTotalPagar || 0);
        if (document.getElementById('kpiTotal30Share')) document.getElementById('kpiTotal30Share').textContent = formatCurrency(data.todayTotal30Share || 0);
        if (document.getElementById('kpiCustomerBalance')) document.getElementById('kpiCustomerBalance').textContent = formatCurrency(data.totalCustomerBalance);

        if (document.getElementById('kpiGeneratedReceipts')) {
            const genCount = data.generatedReceiptCount != null ? data.generatedReceiptCount : 0;
            document.getElementById('kpiGeneratedReceipts').textContent = genCount;
        }
        if (document.getElementById('kpiMarketSubtext')) {
            const totCount = data.totalCustomerCount != null ? data.totalCustomerCount : (data.activeCustomerCount || 0);
            document.getElementById('kpiMarketSubtext').textContent = `out of ${totCount} Markets`;
        }

        const plVal = parseFloat(data.todayProfitLoss) || 0;
        const plStatus = data.todayProfitLossStatus || 'PROFIT';
        const plEl = document.getElementById('kpiProfitLoss');
        const plBadge = document.getElementById('kpiProfitLossBadge');
        const plIcon = document.getElementById('kpiProfitLossIcon');

        if (plEl) {
            plEl.textContent = formatCurrency(plVal);
            plEl.style.color = plStatus === 'PROFIT' ? '#34d399' : '#f87171';
        }
        if (plBadge) {
            plBadge.textContent = plStatus === 'PROFIT' ? 'PROFIT' : 'LOSS';
            plBadge.className = plStatus === 'PROFIT' ? 'kpi-badge profit' : 'kpi-badge loss';
        }
        if (plIcon) {
            plIcon.textContent = plStatus === 'PROFIT' ? '📈' : '📉';
        }
    } catch (err) {
        console.error('Failed to load KPI metrics:', err);
    }
}

// 3. Load & Filter Customers
async function loadCustomers(searchQuery = '', marketFilter = 'ALL') {
    try {
        let url = '/api/customers';
        const params = new URLSearchParams();
        if (searchQuery) params.append('search', searchQuery);
        if (marketFilter && marketFilter !== 'ALL') params.append('city', marketFilter);
        
        if (params.toString()) url += '?' + params.toString();

        const res = await fetch(url);
        if (!res.ok) {
            console.error('Failed to load customers API, HTTP status:', res.status);
            return;
        }
        const data = await res.json();
        if (Array.isArray(data)) {
            customersData = data;
            populateCustomerDropdown(customersData);
            renderCustomerTable(customersData);
        } else {
            console.error('API did not return an array:', data);
        }
    } catch (err) {
        console.error('Failed to load customers:', err);
    }
}

function getSelectedMarket() {
    const activeChip = document.querySelector('.market-chips .chip.active');
    return activeChip ? activeChip.getAttribute('data-market') : 'ALL';
}

function populateCustomerDropdown(list) {
    const select = document.getElementById('selectCustomer');
    const currentVal = select.value;
    select.innerHTML = '<option value="">-- Choose Customer --</option>';

    list.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        const cityTag = c.city || c.marketZone || '';
        opt.textContent = cityTag ? `${c.name} (${cityTag})` : c.name;
        select.appendChild(opt);
    });

    select.value = currentVal;
}

function renderCustomerTable(list) {
    const tbody = document.getElementById('customerTableBody');
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No matching customers found.</td></tr>';
        return;
    }

    list.forEach(c => {
        const tr = document.createElement('tr');
        const todayNet = c.todayNet != null ? c.todayNet : 0;
        const balColor = todayNet < 0 ? '#f87171' : '#34d399';
        const formattedNet = (todayNet < 0 ? '₹-' + Math.abs(todayNet).toFixed(2) : '₹' + todayNet.toFixed(2));
        const cityDisplay = c.city || c.marketZone || 'General';
        const mobileDisplay = c.mobileNumber && c.mobileNumber.trim() ? `📱 ${escapeHtml(c.mobileNumber)}` : '-';
        const subInfo = (c.marketZone || c.city) ? `${escapeHtml(c.marketZone || c.city)} • (${escapeHtml(c.marketCodes || 'PO,PC')})` : `(${escapeHtml(c.marketCodes || 'PO,PC')})`;

        tr.innerHTML = `
            <td>
                <strong style="color: #ffffff;">${escapeHtml(c.name)}</strong>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${subInfo}</div>
            </td>
            <td><span class="chip" style="margin: 0;">${escapeHtml(cityDisplay)}</span></td>
            <td>${mobileDisplay}</td>
            <td style="font-weight: 800; color: ${balColor};">${formattedNet}</td>
            <td>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <button onclick="triggerWhatsApp(${c.id})" class="btn-whatsapp">
                        <span>📲 WhatsApp</span>
                    </button>
                    <button onclick="editCustomer(${c.id})" class="chip" style="margin:0; background: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3);">
                        ✏️ Edit
                    </button>
                    <button onclick="deleteCustomer(${c.id}, '${escapeHtml(c.name)}')" class="chip" style="margin:0; background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3);">
                        🗑️ Delete
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });

    const tfoot = document.getElementById('customerTableFoot');
    if (tfoot) {
        const plVal = latestDashboardData ? (parseFloat(latestDashboardData.todayProfitLoss) || 0) : 0;
        const plStatus = latestDashboardData ? (latestDashboardData.todayProfitLossStatus || 'PROFIT') : 'PROFIT';
        const isProf = plStatus === 'PROFIT';
        const badgeBg = isProf ? 'rgba(52, 211, 153, 0.15)' : 'rgba(248, 113, 113, 0.15)';
        const badgeColor = isProf ? '#34d399' : '#f87171';
        const badgeBorder = isProf ? '1px solid rgba(52, 211, 153, 0.3)' : '1px solid rgba(248, 113, 113, 0.3)';

        tfoot.innerHTML = `
            <tr style="background: #0f172a; border-top: 2px solid #1e293b;">
                <td colspan="3" style="font-weight: 800; color: #f8fafc; font-size: 0.9rem; padding: 0.75rem 1rem;">
                    📊 TODAY'S TOTAL NET PROFIT / LOSS SUMMARY
                </td>
                <td colspan="2" style="text-align: right; padding: 0.75rem 1rem;">
                    <span style="display: inline-block; padding: 0.4rem 0.85rem; border-radius: 0.6rem; font-weight: 800; font-size: 0.9rem; background: ${badgeBg}; color: ${badgeColor}; border: ${badgeBorder};">
                        ${isProf ? '📈 TODAY\'S PROFIT' : '📉 TODAY\'S LOSS'}: ₹${plVal.toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                </td>
            </tr>
        `;
    }
}

let currentSegment = 'daily';
let weeklyReceiptsData = [];

window.switchChartSegment = function(segment) {
    currentSegment = segment;
    const tabDaily = document.getElementById('tabDailyChart');
    const tabWeekly = document.getElementById('tabWeeklyChart');
    const dailyContainer = document.getElementById('dailyChartContainer');
    const weeklyContainer = document.getElementById('weeklyChartContainer');

    if (segment === 'weekly') {
        if (tabDaily) tabDaily.classList.remove('active');
        if (tabWeekly) tabWeekly.classList.add('active');
        if (dailyContainer) dailyContainer.style.display = 'none';
        if (weeklyContainer) weeklyContainer.style.display = 'block';
        loadWeeklyReceipts();
    } else {
        if (tabWeekly) tabWeekly.classList.remove('active');
        if (tabDaily) tabDaily.classList.add('active');
        if (weeklyContainer) weeklyContainer.style.display = 'none';
        if (dailyContainer) dailyContainer.style.display = 'block';
        loadCustomers('', getSelectedMarket());
    }
};

let weeklyDailyMetricsData = {};

async function loadWeeklyReceipts() {
    try {
        const [resReceipts, resMetrics] = await Promise.all([
            fetch('/api/weekly-receipts'),
            fetch('/api/dashboard/weekly-daily-profit-loss')
        ]);
        weeklyReceiptsData = await resReceipts.json();
        weeklyDailyMetricsData = await resMetrics.json();
        renderWeeklyTable(weeklyReceiptsData);
    } catch (err) {
        console.error('Failed to load weekly receipts:', err);
    }
}

function renderWeeklyTable(list) {
    const tbody = document.getElementById('weeklyTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    const selectedMarket = getSelectedMarket();
    const searchVal = (document.getElementById('searchInput') ? document.getElementById('searchInput').value : '').toLowerCase().trim();

    let filtered = (list || []).filter(item => {
        const matchesMarket = selectedMarket === 'ALL' || (item.city && item.city.toLowerCase() === selectedMarket.toLowerCase());
        const matchesSearch = !searchVal || (item.customerName && item.customerName.toLowerCase().includes(searchVal)) || (item.city && item.city.toLowerCase().includes(searchVal));
        return matchesMarket && matchesSearch;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; color: var(--text-muted); padding: 1rem;">No weekly receipt records found for selected market.</td></tr>';
        const tfoot = document.getElementById('weeklyTableFoot');
        if (tfoot) tfoot.innerHTML = '';
        return;
    }

    filtered.forEach(item => {
        const tr = document.createElement('tr');

        const formatCell = (val) => {
            if (val == null || val === 0) return '<span style="color: #64748b; font-weight: 400;">-</span>';
            const isPos = val >= 0;
            const color = isPos ? '#34d399' : '#f87171';
            const suffix = isPos ? 'y' : 'd';
            return `<span style="font-weight: 700; color: ${color};">₹${Math.abs(val).toLocaleString('en-IN')} <small>${suffix}</small></span>`;
        };

        const totalNet = item.weeklyTotalNet || 0;
        const totalColor = totalNet >= 0 ? '#34d399' : '#f87171';
        const totalStatusStr = item.weeklyTotalStatus === 'DENE' ? 'dene' : 'yeṇe';

        tr.innerHTML = `
            <td>
                <strong style="color: #ffffff;">${escapeHtml(item.customerName)}</strong>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(item.city || 'General')}</div>
            </td>
            <td style="text-align: center;">${formatCell(item.mondayNet)}</td>
            <td style="text-align: center;">${formatCell(item.tuesdayNet)}</td>
            <td style="text-align: center;">${formatCell(item.wednesdayNet)}</td>
            <td style="text-align: center;">${formatCell(item.thursdayNet)}</td>
            <td style="text-align: center;">${formatCell(item.fridayNet)}</td>
            <td style="text-align: center;">${formatCell(item.saturdayNet)}</td>
            <td style="text-align: center;">${formatCell(item.sundayNet)}</td>
            <td style="text-align: right; font-weight: 800; color: ${totalColor};">
                ₹${Math.abs(totalNet).toLocaleString('en-IN')} <div style="font-size: 0.7rem; font-weight: 600;">${totalStatusStr}</div>
            </td>
            <td style="text-align: center;">
                <div style="display: flex; gap: 0.35rem; justify-content: center;">
                    <button onclick="shareWeeklyWhatsApp(${item.customerId})" class="btn-whatsapp" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;">
                        <span>📲 Share</span>
                    </button>
                    <button onclick="editWeeklyReceipt(${item.customerId})" class="chip" style="margin:0; background: rgba(59, 130, 246, 0.15); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.3); font-size: 0.75rem; padding: 0.3rem 0.6rem;">
                        ✏️ Edit
                    </button>
                </div>
            </td>
        `;

        tbody.appendChild(tr);
    });

    const tfoot = document.getElementById('weeklyTableFoot');
    if (tfoot) {
        const getDayVal = (dowKey) => {
            const m = weeklyDailyMetricsData ? weeklyDailyMetricsData[dowKey] : null;
            if (!m || m.todayProfitLoss == null) return 0;
            const val = parseFloat(m.todayProfitLoss) || 0;
            return m.todayProfitLossStatus === 'LOSS' ? -val : val;
        };

        let sumMon = getDayVal('MONDAY');
        let sumTue = getDayVal('TUESDAY');
        let sumWed = getDayVal('WEDNESDAY');
        let sumThu = getDayVal('THURSDAY');
        let sumFri = getDayVal('FRIDAY');
        let sumSat = getDayVal('SATURDAY');
        let sumSun = getDayVal('SUNDAY');
        let sumTotalNet = sumMon + sumTue + sumWed + sumThu + sumFri + sumSat + sumSun;

        const formatFooterCell = (val) => {
            if (val == null || val === 0) return '<span style="color: #64748b; font-weight: 400;">-</span>';
            const isPos = val >= 0;
            const color = isPos ? '#34d399' : '#f87171';
            const suffix = isPos ? 'y' : 'd';
            return `<span style="font-weight: 800; color: ${color};">₹${Math.abs(val).toLocaleString('en-IN')} <small>${suffix}</small></span>`;
        };

        const isWeeklyProf = sumTotalNet >= 0;
        const weeklyColor = isWeeklyProf ? '#34d399' : '#f87171';
        const weeklyBadgeBg = isWeeklyProf ? 'rgba(52, 211, 153, 0.15)' : 'rgba(248, 113, 113, 0.15)';
        const weeklyBadgeBorder = isWeeklyProf ? '1px solid rgba(52, 211, 153, 0.3)' : '1px solid rgba(248, 113, 113, 0.3)';

        tfoot.innerHTML = `
            <tr style="background: #0f172a; border-top: 2px solid #1e293b; font-weight: 800;">
                <td style="color: #f8fafc; font-size: 0.85rem; padding: 0.75rem 0.5rem;">
                    📊 WEEKLY MARKET TOTALS
                </td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumMon)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumTue)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumWed)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumThu)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumFri)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumSat)}</td>
                <td style="text-align: center; padding: 0.75rem 0.25rem;">${formatFooterCell(sumSun)}</td>
                <td style="text-align: right; padding: 0.75rem 0.5rem;">
                    <span style="display: inline-block; padding: 0.35rem 0.65rem; border-radius: 0.5rem; font-weight: 800; font-size: 0.85rem; background: ${weeklyBadgeBg}; color: ${weeklyColor}; border: ${weeklyBadgeBorder};">
                        ${isWeeklyProf ? 'WEEKLY PROFIT' : 'WEEKLY LOSS'}: ₹${Math.abs(sumTotalNet).toLocaleString('en-IN', {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                    </span>
                </td>
                <td></td>
            </tr>
        `;
    }
}

window.shareWeeklyWhatsApp = function(customerId) {
    const item = weeklyReceiptsData.find(w => w.customerId == customerId);
    if (!item || !item.formattedWeeklyMessage) return;

    currentFormattedMessage = item.formattedWeeklyMessage;
    originalFormattedMessage = item.formattedWeeklyMessage;
    const editTextEl = document.getElementById('waReceiptEditText');
    if (editTextEl) editTextEl.value = item.formattedWeeklyMessage;
    if (document.getElementById('waCustomerName')) {
        document.getElementById('waCustomerName').textContent = `Weekly Statement - ${item.customerName}`;
    }

    if (document.getElementById('btnEditReceipt')) document.getElementById('btnEditReceipt').style.display = 'flex';
    if (document.getElementById('waEditContainer')) document.getElementById('waEditContainer').style.display = 'none';

    openModal('whatsappModal');
    renderReceiptImageCanvas(item.formattedWeeklyMessage, item.customerName);
};

window.editWeeklyReceipt = function(customerId) {
    const item = weeklyReceiptsData.find(w => w.customerId == customerId);
    if (!item) return;

    if (document.getElementById('editWeeklyCustId')) document.getElementById('editWeeklyCustId').value = customerId;
    if (document.getElementById('weeklyEditTitle')) document.getElementById('weeklyEditTitle').textContent = `✏️ Weekly Commission - ${item.customerName}`;

    let commPct = 0;
    if (item.formattedWeeklyMessage) {
        const match = item.formattedWeeklyMessage.match(/COM\s*\((\d+(\.\d+)?)%\)/i);
        if (match && match[1]) commPct = parseFloat(match[1]);
    }
    if (document.getElementById('weeklyCommInput')) document.getElementById('weeklyCommInput').value = commPct > 0 ? commPct : '';
    openModal('weeklyEditModal');
};

window.saveWeeklyCommission = function() {
    const customerId = document.getElementById('editWeeklyCustId') ? document.getElementById('editWeeklyCustId').value : '';
    const item = weeklyReceiptsData.find(w => w.customerId == customerId);
    if (!item) return;

    const commPct = parseFloat(document.getElementById('weeklyCommInput').value) || 0;

    let mon = item.mondayNet || 0;
    let tue = item.tuesdayNet || 0;
    let wed = item.wednesdayNet || 0;
    let thu = item.thursdayNet || 0;
    let fri = item.fridayNet || 0;
    let sat = item.saturdayNet || 0;
    let sun = item.sundayNet || 0;

    let weeklySum = mon + tue + wed + thu + fri + sat + sun;
    let commVal = 0;
    if (commPct > 0) {
        commVal = Math.round(Math.abs(weeklySum) * (commPct / 100));
    }

    let afterCommSum = weeklySum >= 0 ? (weeklySum - commVal) : (weeklySum + commVal);

    let netWeekly = afterCommSum;
    item.weeklyTotalNet = netWeekly;
    item.weeklyTotalStatus = netWeekly >= 0 ? 'YENE' : 'DENE';

    const fmt = (n) => Math.abs(n).toLocaleString('en-IN');
    const fmtDay = (n) => (n == null || n === 0) ? '-' : (fmt(n) + (n >= 0 ? ' yeṇe' : ' dene'));
    const statusStr = netWeekly >= 0 ? 'yeṇe' : 'dene';
    const sumStatusStr = weeklySum >= 0 ? 'yeṇe' : 'dene';

    let sb = [];
    sb.push("==================================");
    sb.push("      *WEEKLY MARKET STATEMENT*");
    sb.push(`      *${item.customerName.toUpperCase()}*`);
    sb.push("==================================");
    sb.push("*DAY*                   *NET TRADE*");
    sb.push("----------------------------------");
    sb.push(`Mon :-               ${fmtDay(mon)}`);
    sb.push(`Tue :-               ${fmtDay(tue)}`);
    sb.push(`Wed :-               ${fmtDay(wed)}`);
    sb.push(`Thu :-               ${fmtDay(thu)}`);
    sb.push(`Fri :-               ${fmtDay(fri)}`);
    sb.push(`Sat :-               ${fmtDay(sat)}`);
    sb.push(`Sun :-               ${fmtDay(sun)}`);
    sb.push("----------------------------------");

    if (commPct > 0) {
        sb.push(`*TOTAL :-*           ${fmt(weeklySum)} ${sumStatusStr}`);
        sb.push(`*COM (${commPct}%) :-*       ${fmt(commVal)}`);
        sb.push("----------------------------------");
    }

    sb.push(`*WEEKLY TOTAL :-*    ${fmt(netWeekly)} ${statusStr}`);
    sb.push("==================================");
    sb.push(`TOTAL BALANCE DUE ${fmt(netWeekly)} ${statusStr}`);

    item.formattedWeeklyMessage = sb.join('\n');

    renderWeeklyTable(weeklyReceiptsData);
    closeModal('weeklyEditModal');
};

window.editCustomer = function(id) {
    const customer = customersData.find(c => c.id == id);
    if (!customer) return;

    if (document.getElementById('editingCustId')) document.getElementById('editingCustId').value = customer.id;
    if (document.getElementById('custModalTitle')) document.getElementById('custModalTitle').textContent = `✏️ Edit Customer (${customer.name})`;
    if (document.getElementById('btnSaveCust')) document.getElementById('btnSaveCust').textContent = '💾 Update Customer Profile';

    if (document.getElementById('custName')) document.getElementById('custName').value = customer.name || '';
    
    const isCommEnabled = isCommEnabledForCustomer(customer);
    if (document.getElementById('custEnableComm')) document.getElementById('custEnableComm').checked = isCommEnabled;
    if (document.getElementById('custCommission')) document.getElementById('custCommission').value = customer.commissionRate != null ? customer.commissionRate : 10.0;
    if (document.getElementById('custCommContainer')) document.getElementById('custCommContainer').style.display = isCommEnabled ? 'block' : 'none';

    const isPagarEnabled = isPagarEnabledForCustomer(customer);
    if (document.getElementById('custEnablePagar')) document.getElementById('custEnablePagar').checked = isPagarEnabled;
    if (document.getElementById('custPagar')) document.getElementById('custPagar').value = customer.pagar != null ? customer.pagar : 0.0;
    if (document.getElementById('custPagarContainer')) document.getElementById('custPagarContainer').style.display = isPagarEnabled ? 'block' : 'none';

    const isShareEnabled = isShareEnabledForCustomer(customer);
    const is30ProfitOnly = customer.share30ProfitOnly === true || customer.share30ProfitOnly === 'true';
    if (document.getElementById('custShare4060')) {
        document.getElementById('custShare4060').checked = isShareEnabled;
    }
    if (document.getElementById('custShare30ProfitOnly')) {
        document.getElementById('custShare30ProfitOnly').checked = is30ProfitOnly;
    }
    if (document.getElementById('custShareRateVal')) {
        document.getElementById('custShareRateVal').value = (isShareEnabled && customer.shareRate != null && customer.shareRate !== 100) ? customer.shareRate : 40.0;
    }
    if (document.getElementById('custShareRateContainer')) {
        document.getElementById('custShareRateContainer').style.display = isShareEnabled ? 'block' : 'none';
    }
    if (document.getElementById('custFarak')) {
        document.getElementById('custFarak').value = customer.farak != null ? customer.farak : 0.0;
    }

    openModal('customerModal');
};

// 4. Live Calculation Engine Math Logic
function calculateMathPreview() {
    const custId = document.getElementById('selectCustomer').value;
    const customer = custId ? customersData.find(c => c.id == custId) : null;

    let totalSell = 0;
    let totalPayment = 0;

    const marketInputs = document.querySelectorAll('#dynamicMarketInputs .market-input');
    marketInputs.forEach(input => {
        const val = parseFloat(input.value) || 0;
        const code = input.getAttribute('data-session');
        const type = input.getAttribute('data-type');

        if (type === 'sell') {
            totalSell += val;
            const prevEl = document.getElementById(`preview_sell_${code}`);
            if (prevEl) prevEl.textContent = formatCurrency(val);
        } else if (type === 'payment') {
            totalPayment += val;
            const prevEl = document.getElementById(`preview_payment_${code}`);
            if (prevEl) prevEl.textContent = formatCurrency(val);
        }
    });

    const commRate = parseFloat(document.getElementById('commPercent').value) || 10.0;
    const shareRate = parseFloat(document.getElementById('shareRatePercent').value) || 40.0;
    const yeneVal = parseFloat(document.getElementById('tradeYene').value) || 0;
    const deneVal = parseFloat(document.getElementById('tradeDene').value) || 0;
    const farakVal = parseFloat(document.getElementById('farakAmount').value) || 0;
    const pagarVal = parseFloat(document.getElementById('pagarAmount').value) || 0;

    const openingNet = yeneVal - deneVal;

    const isCommEnabled = isCommEnabledForCustomer(customer);
    const isPagarEnabled = isPagarEnabledForCustomer(customer);
    const isShareEnabled = isShareEnabledForCustomer(customer);

    // Commission is calculated whenever enabled for customer (regardless of payment vs sell)
    const actualCommRate = isCommEnabled ? commRate : 0;
    const commCut = (totalSell * actualCommRate) / 100.0;
    const totalAfterComm = totalSell - commCut;
    const afterPay = totalAfterComm - totalPayment;

    const actualPagarVal = isPagarEnabled ? pagarVal : 0;

    let netBalance = 0;
    let shareAmount = 0;

    const rowComi = document.getElementById('previewComiRow');
    const rowAfterComm = document.getElementById('previewAfterCommRow');
    const rowPayment = document.getElementById('previewPaymentRow');
    const rowFarak = document.getElementById('previewFarakRow');
    const rowShare = document.getElementById('previewShareRow');
    const rowPagar = document.getElementById('previewPagarRow');

    if (rowComi) rowComi.style.display = isCommEnabled ? 'flex' : 'none';
    if (rowAfterComm) rowAfterComm.style.display = isCommEnabled ? 'flex' : 'none';
    if (rowPayment) rowPayment.style.display = 'flex';
    if (rowFarak) rowFarak.style.display = farakVal !== 0 ? 'flex' : 'none';
    if (rowShare) rowShare.style.display = isShareEnabled ? 'flex' : 'none';
    if (rowPagar) rowPagar.style.display = isPagarEnabled ? 'flex' : 'none';

    const is30ProfitOnly = customer && (customer.share30ProfitOnly === true || customer.share30ProfitOnly === 'true');
    const effectiveSharePct = is30ProfitOnly ? 30.0 : shareRate;

    if (isShareEnabled) {
        const afterFarak = afterPay - farakVal;
        if (is30ProfitOnly) {
            shareAmount = afterFarak > 0 ? (afterFarak * 30.0) / 100.0 : 0;
        } else {
            shareAmount = (afterFarak * effectiveSharePct) / 100.0;
        }
        netBalance = (afterFarak - shareAmount - actualPagarVal) + openingNet;
    } else {
        netBalance = (afterPay - actualPagarVal) + openingNet;
    }

    if (document.getElementById('previewTotalSell')) document.getElementById('previewTotalSell').textContent = formatCurrency(totalSell);
    if (document.getElementById('previewTotalPayment')) document.getElementById('previewTotalPayment').textContent = formatCurrency(totalPayment);

    document.getElementById('previewCommRate').textContent = commRate.toFixed(1);
    document.getElementById('previewCommCut').textContent = `-` + formatCurrency(commCut);
    if (document.getElementById('previewTotalAfterComm')) document.getElementById('previewTotalAfterComm').textContent = formatCurrency(totalAfterComm);
    document.getElementById('previewPayment').textContent = `-` + formatCurrency(totalPayment);

    if (document.getElementById('previewShareRate')) document.getElementById('previewShareRate').textContent = effectiveSharePct.toFixed(0);
    if (document.getElementById('previewShareAmount')) document.getElementById('previewShareAmount').textContent = formatCurrency(shareAmount);

    if (document.getElementById('previewYene')) document.getElementById('previewYene').textContent = formatCurrency(yeneVal);
    if (document.getElementById('previewDene')) document.getElementById('previewDene').textContent = `-` + formatCurrency(deneVal);

    if (document.getElementById('previewFarak')) document.getElementById('previewFarak').textContent = (farakVal >= 0 ? `+` : ``) + formatCurrency(farakVal);
    if (document.getElementById('previewPagarAmount')) document.getElementById('previewPagarAmount').textContent = `-` + formatCurrency(actualPagarVal);
    
    const netText = netBalance >= 0 ? formatCurrency(netBalance) + ' (येणे)' : '-' + formatCurrency(Math.abs(netBalance)) + ' (देणे)';
    const netColor = netBalance >= 0 ? '#fbbf24' : '#f87171';
    const netEl = document.getElementById('previewNetBalance');
    if (netEl) {
        netEl.textContent = netText;
        netEl.style.color = netColor;
    }
}

let isSubmittingTrade = false;

// 5. Handle Trade Form Actions (Save & Download vs Share to WhatsApp)
window.processTradeAction = async function(actionType) {
    if (isSubmittingTrade) {
        console.warn('⚠️ Trade submission already in progress!');
        return;
    }

    const customerId = document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '';
    if (!customerId) {
        alert('⚠️ Please select a customer / trader first!');
        return;
    }

    // Enforce single market upload check
    const isAlreadyUploaded = await checkIfMarketAlreadyUploaded();
    if (isAlreadyUploaded) {
        alert('⚠️ Market entry for this customer on this date has ALREADY been uploaded!\n\nChanges cannot be submitted from Daily Trade Entry & Math Engine. Please use the "✏️ Edit Full Receipt" button to make any changes.');
        return;
    }

    const btnSave = document.getElementById('btnSaveTrade');
    const btnShare = document.getElementById('btnShareTrade');

    isSubmittingTrade = true;
    if (btnSave) {
        btnSave.disabled = true;
        btnSave.style.opacity = '0.5';
        btnSave.style.cursor = 'not-allowed';
        btnSave.style.pointerEvents = 'none';
        if (actionType === 'save') btnSave.textContent = '⏳ Saving...';
    }
    if (btnShare) {
        btnShare.disabled = true;
        btnShare.style.opacity = '0.5';
        btnShare.style.cursor = 'not-allowed';
        btnShare.style.pointerEvents = 'none';
        if (actionType === 'share') btnShare.textContent = '⏳ Processing...';
    }

    let sellPo = 0;
    let sellPc = 0;
    let paymentPo = 0;
    let paymentPc = 0;

    const marketInputs = document.querySelectorAll('#dynamicMarketInputs .market-input');
    marketInputs.forEach(input => {
        const val = parseFloat(input.value) || 0;
        const code = input.getAttribute('data-session');
        const type = input.getAttribute('data-type');

        if (code === 'PO') {
            if (type === 'sell') sellPo = val;
            if (type === 'payment') paymentPo = val;
        } else {
            if (type === 'sell') sellPc += val;
            if (type === 'payment') paymentPc += val;
        }
    });

    const yeneVal = parseFloat(document.getElementById('tradeYene').value) || 0;
    const deneVal = parseFloat(document.getElementById('tradeDene').value) || 0;
    const magilBaki = yeneVal - deneVal;
    const pagarAmount = parseFloat(document.getElementById('pagarAmount').value) || 0;
    const farakAmount = parseFloat(document.getElementById('farakAmount').value) || 0;
    const styleEl = document.getElementById('tradeReceiptStyle');

    const customerObj = customersData ? customersData.find(c => c.id == customerId) : null;
    const isShareEnabled = isShareEnabledForCustomer(customerObj);
    const effectiveShareRate = isShareEnabled ? (parseFloat(document.getElementById('shareRatePercent').value) || 40.0) : 100.0;

    const payload = {
        customerId: parseInt(customerId),
        transactionDate: document.getElementById('txDate').value,
        sellPo: sellPo,
        sellPc: sellPc,
        paymentPo: paymentPo,
        paymentPc: paymentPc,
        magilBaki: magilBaki,
        pagarAmount: pagarAmount,
        farak: farakAmount,
        receiptStyle: styleEl && styleEl.value ? styleEl.value : 'TYPE_1',
        shareRate: effectiveShareRate,
        rate: 1.0,
        commissionPercentage: parseFloat(document.getElementById('commPercent').value) || 10.0,
        paymentAmount: paymentPo + paymentPc,
        paymentMode: 'Cash/UPI',
        notes: 'SELL vs PAYMENT Market Entry'
    };

    try {
        const res = await fetch('/api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            await loadCustomers('', getSelectedMarket());
            if (currentSegment === 'weekly') {
                await loadWeeklyReceipts();
            }
            await loadDashboardMetrics();
            
            if (actionType === 'save') {
                await triggerWhatsApp(customerId, false);
                downloadReceiptPhoto();
                alert('✅ Trade Saved to Ledger & Receipt Downloaded Successfully!');
            } else if (actionType === 'share') {
                await triggerWhatsApp(customerId, true);
            }

            document.querySelectorAll('#dynamicMarketInputs input').forEach(inp => inp.value = '0');
            if (document.getElementById('pagarAmount')) document.getElementById('pagarAmount').value = '0';
            if (document.getElementById('farakAmount')) document.getElementById('farakAmount').value = '0';
            calculateMathPreview();
            checkIfMarketAlreadyUploaded();
        } else {
            let errorMsg = '❌ Failed to process trade transaction.';
            try {
                const errData = await res.json();
                if (errData && errData.message) errorMsg = errData.message;
            } catch (e) {}
            alert(errorMsg);
        }
    } catch (err) {
        console.error('Error submitting trade:', err);
    } finally {
        isSubmittingTrade = false;
        if (btnSave) {
            btnSave.disabled = false;
            btnSave.style.opacity = '1';
            btnSave.style.cursor = 'pointer';
            btnSave.style.pointerEvents = 'auto';
            btnSave.textContent = '💾 Save';
        }
        if (btnShare) {
            btnShare.disabled = false;
            btnShare.style.opacity = '1';
            btnShare.style.cursor = 'pointer';
            btnShare.style.pointerEvents = 'auto';
            btnShare.textContent = '📲 Share';
        }
    }
};

// 6. Add / Edit Customer
async function handleNewCustomerSubmit(e) {
    e.preventDefault();

    const editingId = document.getElementById('editingCustId') ? document.getElementById('editingCustId').value : '';
    const isEdit = Boolean(editingId);

    const nameVal = document.getElementById('custName') ? document.getElementById('custName').value : '';
    const isCommEnabledChecked = document.getElementById('custEnableComm') ? document.getElementById('custEnableComm').checked : false;
    const commVal = document.getElementById('custCommission') ? (parseFloat(document.getElementById('custCommission').value) || 10.0) : 10.0;

    const isPagarEnabledChecked = document.getElementById('custEnablePagar') ? document.getElementById('custEnablePagar').checked : false;
    const pagarVal = document.getElementById('custPagar') ? (parseFloat(document.getElementById('custPagar').value) || 0) : 0;

    const isShare4060Checked = document.getElementById('custShare4060') ? document.getElementById('custShare4060').checked : false;
    const isShare30ProfitOnlyChecked = document.getElementById('custShare30ProfitOnly') ? document.getElementById('custShare30ProfitOnly').checked : false;
    const customShareRate = document.getElementById('custShareRateVal') ? (parseFloat(document.getElementById('custShareRateVal').value) || 40.0) : 40.0;
    const shareRateVal = isShare4060Checked ? customShareRate : 100.0;

    const farakVal = document.getElementById('custFarak') ? (parseFloat(document.getElementById('custFarak').value) || 0) : 0;

    const payload = {
        name: nameVal,
        mobileNumber: '',
        city: '',
        receiptStyle: 'TYPE_1',
        shareRate: shareRateVal,
        share30ProfitOnly: isShare30ProfitOnlyChecked,
        commissionPercentage: commVal,
        commissionEnabled: isCommEnabledChecked,
        pagar: pagarVal,
        pagarEnabled: isPagarEnabledChecked,
        yene: 0,
        dene: 0,
        farak: farakVal,
        marketCodes: 'PO,PC'
    };

    try {
        const url = isEdit ? `/api/customers/${editingId}` : '/api/customers';
        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const savedCustomer = await res.json();
            alert(isEdit ? '✅ Customer Profile Updated Successfully!' : '✅ Customer Profile Saved Successfully!');
            closeModal('customerModal');
            document.getElementById('newCustomerForm').reset();
            if (document.getElementById('editingCustId')) document.getElementById('editingCustId').value = '';

            if (document.getElementById('custEnableComm')) document.getElementById('custEnableComm').checked = false;
            if (document.getElementById('custCommission')) document.getElementById('custCommission').value = '10.00';
            if (document.getElementById('custCommContainer')) document.getElementById('custCommContainer').style.display = 'none';

            if (document.getElementById('custEnablePagar')) document.getElementById('custEnablePagar').checked = false;
            if (document.getElementById('custPagar')) document.getElementById('custPagar').value = '0.00';
            if (document.getElementById('custPagarContainer')) document.getElementById('custPagarContainer').style.display = 'none';

            if (document.getElementById('custShare4060')) document.getElementById('custShare4060').checked = false;
            if (document.getElementById('custShareRateVal')) document.getElementById('custShareRateVal').value = '40.00';
            if (document.getElementById('custShareRateContainer')) document.getElementById('custShareRateContainer').style.display = 'none';

            await loadCustomers('', getSelectedMarket());
            await loadDashboardMetrics();

            const selectEl = document.getElementById('selectCustomer');
            if (selectEl && savedCustomer && savedCustomer.id) {
                selectEl.value = savedCustomer.id;
                selectEl.dispatchEvent(new Event('change'));
            }
        } else {
            alert(isEdit ? '❌ Failed to update customer profile.' : '❌ Failed to save customer profile.');
        }
    } catch (err) {
        console.error('Error saving/updating customer:', err);
    }
}

// 7. One-Click WhatsApp Statement Generator
async function triggerWhatsApp(customerId, autoShare = false) {
    try {
        const selectedCustId = document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '';
        if (!customerId && selectedCustId) {
            customerId = selectedCustId;
        }

        currentActiveCustomerId = customerId;

        const customer = customersData ? customersData.find(c => c.id == customerId) : null;

        const tradeStyleEl = document.getElementById('tradeReceiptStyle');
        let activeStyle = tradeStyleEl ? tradeStyleEl.value : (customer ? customer.receiptStyle : 'TYPE_1');

        let sellPoNum = 0;
        let sellPcNum = 0;
        let payPoNum = 0;
        let payPcNum = 0;

        const marketInputs = document.querySelectorAll('#dynamicMarketInputs .market-input');
        marketInputs.forEach(input => {
            const val = parseFloat(input.value) || 0;
            const code = input.getAttribute('data-session');
            const type = input.getAttribute('data-type');
            if (code === 'PO') {
                if (type === 'sell') sellPoNum = val;
                if (type === 'payment') payPoNum = val;
            } else {
                if (type === 'sell') sellPcNum += val;
                if (type === 'payment') payPcNum += val;
            }
        });

        let yeneVal = '';
        let deneVal = '';
        let farakVal = '';
        let pagarVal = '';

        const isLiveActiveForm = selectedCustId && selectedCustId == customerId && (sellPoNum > 0 || sellPcNum > 0 || payPoNum > 0 || payPcNum > 0);

        if (isLiveActiveForm) {
            activeStyle = tradeStyleEl ? tradeStyleEl.value : (customer ? customer.receiptStyle : 'TYPE_1');
            yeneVal = document.getElementById('tradeYene') ? document.getElementById('tradeYene').value : '';
            deneVal = document.getElementById('tradeDene') ? document.getElementById('tradeDene').value : '';
            farakVal = document.getElementById('farakAmount') ? document.getElementById('farakAmount').value : '';
            pagarVal = document.getElementById('pagarAmount') ? document.getElementById('pagarAmount').value : '';
        } else if (customer) {
            activeStyle = customer.receiptStyle || 'TYPE_1';
        }

        const params = new URLSearchParams();
        if (activeStyle) params.append('style', activeStyle);

        if (isLiveActiveForm) {
            params.append('sellPo', sellPoNum);
            params.append('sellPc', sellPcNum);
            params.append('payPo', payPoNum);
            params.append('payPc', payPcNum);
            if (parseFloat(yeneVal) > 0) params.append('yene', yeneVal);
            if (parseFloat(deneVal) > 0) params.append('dene', deneVal);
            if (parseFloat(farakVal) !== 0 && farakVal !== '' && farakVal !== null) params.append('farak', farakVal);
            if (parseFloat(pagarVal) > 0) params.append('pagar', pagarVal);
        }

        const url = `/api/whatsapp/generate/${customerId}?` + params.toString();
        const res = await fetch(url);
        const data = await res.json();

        if (document.getElementById('waCustomerName')) {
            const nameDisp = data.customerName || (customer ? customer.name : 'Customer Statement');
            const cityDisp = data.city || (customer ? customer.city : '');
            document.getElementById('waCustomerName').textContent = cityDisp ? `${nameDisp} (${cityDisp})` : nameDisp;
        }
        if (document.getElementById('waMessageText')) {
            document.getElementById('waMessageText').textContent = data.formattedMessage;
        }

        let cleanMobile = (data.mobileNumber || '').replace(/[^0-9]/g, '');
        if (cleanMobile.length === 10) {
            cleanMobile = '91' + cleanMobile;
        }

        currentCleanMobile = cleanMobile;
        currentFormattedMessage = data.formattedMessage;
        originalFormattedMessage = data.formattedMessage;

        const editTextEl = document.getElementById('waReceiptEditText');
        if (editTextEl) editTextEl.value = data.formattedMessage || '';
        populateQuickFieldsFromText(data.formattedMessage || '');
        const editContainerEl = document.getElementById('waEditContainer');
        if (editContainerEl) editContainerEl.style.display = 'none';

        const encodedMsg = encodeURIComponent(data.formattedMessage || '');
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const waLink = cleanMobile 
            ? (isMobile ? `https://api.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}` : `https://web.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}`)
            : (isMobile ? `https://api.whatsapp.com/send` : `https://web.whatsapp.com`);

        currentWaLink = waLink;

        // Render HD Receipt Canvas Photo
        if (document.getElementById('btnEditReceipt')) document.getElementById('btnEditReceipt').style.display = 'flex';
        renderReceiptImageCanvas(data.formattedMessage, data.customerName);

        openModal('whatsappModal');

        if (autoShare) {
            setTimeout(() => {
                shareReceiptPhotoToWhatsApp('web');
            }, 300);
        }
    } catch (err) {
        console.error('Error generating WhatsApp statement:', err);
    }
}

// 8. View Ledger History
async function viewHistory(customerId, customerName) {
    try {
        document.getElementById('historyCustomerTitle').textContent = `Ledger History - ${customerName}`;
        const res = await fetch(`/api/ledger/customer/${customerId}`);
        const list = await res.json();

        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '';

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No ledger entries found.</td></tr>';
        } else {
            list.forEach(l => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${l.entryDate}</td>
                    <td>₹${l.totalSell.toFixed(2)}</td>
                    <td>-₹${l.totalCommission.toFixed(2)}</td>
                    <td>-₹${l.totalPayment.toFixed(2)}</td>
                    <td>₹${l.previousBalance.toFixed(2)}</td>
                    <td style="font-weight: 800; color: #fca5a5;">₹${l.netBalanceDue.toFixed(2)}</td>
                `;
                tbody.appendChild(tr);
            });
        }

        openModal('historyModal');
    } catch (err) {
        console.error('Error loading history:', err);
    }
}

// Utility Functions
function formatCurrency(val) {
    const num = parseFloat(val) || 0;
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function openModal(id) {
    document.getElementById(id).style.display = 'flex';
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}

let latestDashboardData = null;

// Toggle Dropdown for Markets Whose Receipts Are Made
window.toggleReceiptsDropdown = function(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('receiptsDropdownMenu');
    const card = document.getElementById('kpiGeneratedReceiptsCard');
    const arrow = document.getElementById('receiptDropdownArrow');
    if (!dropdown) return;

    const isVisible = dropdown.style.display === 'block';
    if (isVisible) {
        dropdown.style.display = 'none';
        if (card) {
            card.style.zIndex = '';
            card.classList.remove('dropdown-active');
        }
        if (arrow) arrow.textContent = '▼';
    } else {
        dropdown.style.display = 'block';
        if (card) {
            card.style.zIndex = '9999';
            card.classList.add('dropdown-active');
        }
        dropdown.style.zIndex = '10000';
        if (arrow) arrow.textContent = '▲';
        populateAndRenderReceiptsDropdown();
    }
};

// Document Click Event Listener to Auto-Close Dropdown when clicking outside
document.addEventListener('click', (e) => {
    const dropdown = document.getElementById('receiptsDropdownMenu');
    const card = document.getElementById('kpiGeneratedReceiptsCard');
    const arrow = document.getElementById('receiptDropdownArrow');
    if (dropdown && dropdown.style.display === 'block') {
        if (!card || !card.contains(e.target)) {
            dropdown.style.display = 'none';
            if (card) {
                card.style.zIndex = '';
                card.classList.remove('dropdown-active');
            }
            if (arrow) arrow.textContent = '▼';
        }
    }
});

window.triggerWhatsAppFromDropdown = function(customerId) {
    const dropdown = document.getElementById('receiptsDropdownMenu');
    const card = document.getElementById('kpiGeneratedReceiptsCard');
    const arrow = document.getElementById('receiptDropdownArrow');
    if (dropdown) dropdown.style.display = 'none';
    if (card) {
        card.style.zIndex = '';
        card.classList.remove('dropdown-active');
    }
    if (arrow) arrow.textContent = '▼';

    triggerWhatsApp(customerId);
};

function parseMessageToVisualReceipt(rawMsg, customer, balVal) {
    const cityHeader = (customer.city || customer.marketZone || 'GENERAL').toUpperCase();
    if (!rawMsg) {
        return `
            <div style="background: #090d16; border: 1.5px solid rgba(56, 189, 248, 0.4); border-radius: 0.75rem; padding: 0.75rem; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif;">
                <div style="text-align: center; border-bottom: 1px dashed rgba(255,255,255,0.15); padding-bottom: 0.4rem; margin-bottom: 0.5rem;">
                    <div style="font-size: 0.72rem; color: #94a3b8; font-weight: 700;">Date: ${new Date().toLocaleDateString('en-GB')}</div>
                    <div style="font-size: 0.95rem; color: #38bdf8; font-weight: 900; margin-top: 0.1rem;">${escapeHtml(cityHeader)}</div>
                </div>
                <div style="background: rgba(56, 189, 248, 0.15); border: 1.5px solid #38bdf8; border-radius: 0.5rem; padding: 0.5rem; text-align: center; margin-top: 0.5rem;">
                    <div style="font-size: 0.65rem; color: #38bdf8; font-weight: 800; text-transform: uppercase;">TOTAL BALANCE DUE</div>
                    <div style="font-size: 1.1rem; color: #ffffff; font-weight: 900; margin-top: 0.1rem;">₹${balVal.toFixed(2)} ${balVal >= 0 ? 'yeṇe' : 'dene'}</div>
                </div>
            </div>
        `;
    }

    const lines = rawMsg.split('\n');
    let dateStr = '';
    let poSell = '0', poPay = '0';
    let pcSell = '0', pcPay = '0';
    let totalSell = '0', totalPay = '0';
    let payLine = '';
    let pagarLine = '';
    let remainingLine = '';
    let magilYeneLine = '';
    let magilDeneLine = '';
    let totalBalDueLine = '';

    lines.forEach(l => {
        const clean = l.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();

        if (upper.startsWith('DATE:')) {
            dateStr = clean.replace(/^DATE:\s*/i, '');
        } else if (upper.startsWith('PO:-') || upper.startsWith('PO:')) {
            const parts = clean.split(/\s+/);
            if (parts.length >= 3) {
                poSell = parts[1] || '0';
                poPay = parts[2] || '0';
            }
        } else if (upper.startsWith('PC:-') || upper.startsWith('PC:')) {
            const parts = clean.split(/\s+/);
            if (parts.length >= 3) {
                pcSell = parts[1] || '0';
                pcPay = parts[2] || '0';
            }
        } else if (upper.startsWith('TOTAL:-') || upper.startsWith('TOTAL:')) {
            const parts = clean.split(/\s+/);
            if (parts.length >= 3) {
                totalSell = parts[1] || '0';
                totalPay = parts[2] || '0';
            }
        } else if (upper.startsWith('PAYMENT:-')) {
            payLine = clean.replace(/^PAYMENT:-\s*/i, '');
        } else if (upper.startsWith('PAGAR:-')) {
            pagarLine = clean.replace(/^PAGAR:-\s*/i, '');
        } else if (upper.startsWith('REMAINING:-')) {
            remainingLine = clean.replace(/^REMAINING:-\s*/i, '');
        } else if (upper.includes('MAGIL YENE')) {
            magilYeneLine = clean.replace(/.*MAGIL YENE:\s*/i, '').replace(/.*MAGIL YENE:-\s*/i, '').replace(/.*MAGIL YENE\s*/i, '');
        } else if (upper.includes('MAGIL DENE')) {
            magilDeneLine = clean.replace(/.*MAGIL DENE:\s*/i, '').replace(/.*MAGIL DENE:-\s*/i, '').replace(/.*MAGIL DENE\s*/i, '');
        } else if (upper.includes('TOTAL BALANCE DUE')) {
            totalBalDueLine = clean.replace(/.*TOTAL BALANCE DUE:\s*/i, '').replace(/.*TOTAL BALANCE DUE\s*/i, '');
        }
    });

    if (!dateStr) dateStr = new Date().toLocaleDateString('en-GB');

    let summaryHtml = '';
    if (payLine) {
        summaryHtml += `<div style="display: flex; justify-content: space-between; color: #cbd5e1;"><span>PAYMENT :-</span><span style="font-weight: 700; color: #f87171;">-${escapeHtml(payLine)}</span></div>`;
    }
    if (pagarLine) {
        summaryHtml += `<div style="display: flex; justify-content: space-between; color: #cbd5e1;"><span>PAGAR :-</span><span style="font-weight: 700; color: #f87171;">-${escapeHtml(pagarLine)}</span></div>`;
    }
    if (remainingLine) {
        summaryHtml += `<div style="display: flex; justify-content: space-between; color: #cbd5e1;"><span>REMAINING :-</span><span style="font-weight: 700; color: #38bdf8;">${escapeHtml(remainingLine)}</span></div>`;
    }
    if (magilYeneLine) {
        summaryHtml += `<div style="display: flex; justify-content: space-between; color: #fbbf24;"><span>🔴 MAGIL YENE :-</span><span style="font-weight: 800;">${escapeHtml(magilYeneLine)}</span></div>`;
    }
    if (magilDeneLine) {
        summaryHtml += `<div style="display: flex; justify-content: space-between; color: #f87171;"><span>🔴 MAGIL DENE :-</span><span style="font-weight: 800;">${escapeHtml(magilDeneLine)}</span></div>`;
    }

    if (!totalBalDueLine) {
        totalBalDueLine = `${balVal.toFixed(0)} ${balVal >= 0 ? 'yeṇe' : 'dene'}`;
    }

    return `
        <div style="background: #090d16; border: 1.5px solid rgba(56, 189, 248, 0.4); border-radius: 0.75rem; padding: 0.75rem; color: #f8fafc; font-family: system-ui, -apple-system, sans-serif;">
            <div style="text-align: center; border-bottom: 1px dashed rgba(255,255,255,0.15); padding-bottom: 0.4rem; margin-bottom: 0.5rem;">
                <div style="font-size: 0.72rem; color: #94a3b8; font-weight: 700;">Date: ${escapeHtml(dateStr)}</div>
                <div style="font-size: 0.95rem; color: #38bdf8; font-weight: 900; letter-spacing: 0.05em; text-transform: uppercase; margin-top: 0.1rem;">
                    ${escapeHtml(cityHeader)}
                </div>
            </div>

            <table style="width: 100%; border-collapse: collapse; font-size: 0.78rem; margin-bottom: 0.5rem;">
                <thead>
                    <tr style="border-bottom: 1px solid rgba(255,255,255,0.12); color: #94a3b8;">
                        <th style="text-align: left; padding: 0.25rem 0;">PARTICULARS</th>
                        <th style="text-align: right; padding: 0.25rem 0; color: #34d399;">SELL (₹)</th>
                        <th style="text-align: right; padding: 0.25rem 0; color: #f87171;">PAYMENT (₹)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);">
                        <td style="font-weight: 700; color: #e2e8f0; padding: 0.3rem 0;">PO</td>
                        <td style="text-align: right; color: #34d399; font-weight: 700;">${escapeHtml(poSell)}</td>
                        <td style="text-align: right; color: #f87171; font-weight: 700;">${escapeHtml(poPay)}</td>
                    </tr>
                    <tr style="border-bottom: 1px solid rgba(255,255,255,0.05);">
                        <td style="font-weight: 700; color: #e2e8f0; padding: 0.3rem 0;">PC</td>
                        <td style="text-align: right; color: #34d399; font-weight: 700;">${escapeHtml(pcSell)}</td>
                        <td style="text-align: right; color: #f87171; font-weight: 700;">${escapeHtml(pcPay)}</td>
                    </tr>
                    <tr style="font-weight: 800; border-top: 1px solid rgba(255,255,255,0.2); border-bottom: 1px solid rgba(255,255,255,0.2);">
                        <td style="color: #38bdf8; padding: 0.35rem 0;">TOTAL</td>
                        <td style="text-align: right; color: #34d399;">${escapeHtml(totalSell)}</td>
                        <td style="text-align: right; color: #f87171;">${escapeHtml(totalPay)}</td>
                    </tr>
                </tbody>
            </table>

            ${summaryHtml ? `<div style="display: flex; flex-direction: column; gap: 0.25rem; font-size: 0.78rem; border-bottom: 1px dashed rgba(255,255,255,0.15); padding-bottom: 0.4rem; margin-bottom: 0.5rem;">${summaryHtml}</div>` : ''}

            <div style="background: rgba(56, 189, 248, 0.15); border: 1.5px solid #38bdf8; border-radius: 0.5rem; padding: 0.45rem 0.65rem; text-align: center;">
                <div style="font-size: 0.65rem; color: #38bdf8; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em;">TOTAL BALANCE DUE</div>
                <div style="font-size: 1.1rem; color: #ffffff; font-weight: 900; margin-top: 0.1rem;">
                    ${escapeHtml(totalBalDueLine)}
                </div>
            </div>
        </div>
    `;
}

function formatWhatsAppMessageToHtml(rawMsg) {
    if (!rawMsg) return '';
    let html = escapeHtml(rawMsg);
    html = html.replace(/\*(.*?)\*/g, '<strong>$1</strong>');
    html = html.replace(/\n/g, '<br>');
    return html;
}

async function populateAndRenderReceiptsDropdown() {
    const listContainer = document.getElementById('receiptsDropdownList');
    const countBadge = document.getElementById('receiptsDropdownCount');
    if (!listContainer) return;

    listContainer.innerHTML = `
        <div style="font-size: 0.8rem; color: #38bdf8; text-align: center; padding: 1.25rem 0;">
            Loading market receipts... 🧾
        </div>
    `;

    try {
        const res = await fetch('/api/whatsapp/today-statements');
        if (!res.ok) throw new Error('Failed to fetch today statements');
        const statements = await res.json();

        if (countBadge) countBadge.textContent = statements ? statements.length : 0;
        if (document.getElementById('kpiGeneratedReceipts')) {
            document.getElementById('kpiGeneratedReceipts').textContent = statements ? statements.length : 0;
        }

        if (!statements || statements.length === 0) {
            listContainer.innerHTML = `
                <div style="font-size: 0.8rem; color: var(--text-muted); text-align: center; padding: 0.75rem 0;">
                    No receipts generated yet today.
                </div>
            `;
            return;
        }

        let html = '';
        statements.forEach((stmt, idx) => {
            const rawMessage = stmt.formattedMessage || '';
            const visualHtml = formatWhatsAppMessageToHtml(rawMessage);

            html += `
                <div style="background: rgba(15, 23, 42, 0.95); border: 1.5px solid rgba(56, 189, 248, 0.35); border-radius: 0.85rem; padding: 0.75rem; margin-bottom: 0.75rem; display: flex; flex-direction: column; gap: 0.5rem; box-shadow: 0 10px 25px rgba(0,0,0,0.5);">
                    <!-- Market Name Header -->
                    <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid rgba(255, 255, 255, 0.1); padding-bottom: 0.4rem;">
                        <div style="display: flex; align-items: center; gap: 0.55rem; overflow: hidden;">
                            <span style="background: #38bdf8; color: #0f172a; font-weight: 900; font-size: 0.75rem; min-width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
                                ${idx + 1}
                            </span>
                            <div style="overflow: hidden;">
                                <div style="font-weight: 800; color: #f8fafc; font-size: 0.95rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                    ${escapeHtml(stmt.customerName)}
                                </div>
                                <div style="font-size: 0.7rem; color: #94a3b8;">
                                    ${escapeHtml(stmt.city || 'General Market')}
                                </div>
                            </div>
                        </div>
                        <button onclick="triggerWhatsAppFromDropdown(${stmt.customerId})" class="btn-whatsapp" style="font-size: 0.75rem; padding: 0.3rem 0.6rem; cursor: pointer;">
                            📲 Send
                        </button>
                    </div>

                    <!-- 100% EXACT WHATSAPP RECEIPT PREVIEW -->
                    <div style="background: #090d16; border: 1.5px solid rgba(56, 189, 248, 0.4); border-radius: 0.75rem; padding: 0.75rem; color: #f8fafc; font-family: 'Courier New', Courier, monospace; font-size: 0.82rem; line-height: 1.45;">
                        ${visualHtml}
                    </div>
                </div>
            `;
        });

        listContainer.innerHTML = html;
    } catch (e) {
        listContainer.innerHTML = `
            <div style="font-size: 0.8rem; color: var(--text-muted); text-align: center; padding: 0.75rem 0;">
                No receipts generated yet today.
            </div>
        `;
    }
}

// Open and Render Registered Markets List Modal
async function openMarketsModal() {
    try {
        let allCustomers = [];
        try {
            const res = await fetch('/api/customers');
            if (res.ok) {
                allCustomers = await res.json();
            }
        } catch (e) {
            console.warn('Error fetching customers for markets modal:', e);
        }

        if ((!allCustomers || allCustomers.length === 0) && typeof customersData !== 'undefined' && Array.isArray(customersData)) {
            allCustomers = customersData;
        }

        let marketsList = [];
        const seenKeys = new Set();

        if (allCustomers && allCustomers.length > 0) {
            allCustomers.forEach(c => {
                const marketName = (c.city || c.marketZone || '').trim();
                const custName = (c.name || '').trim();
                
                let displayName = '';
                let filterKey = '';
                if (marketName && custName) {
                    displayName = `${marketName} (${custName})`;
                    filterKey = marketName;
                } else if (marketName) {
                    displayName = marketName;
                    filterKey = marketName;
                } else if (custName) {
                    displayName = `${custName} (General Market)`;
                    filterKey = custName;
                }

                if (displayName && !seenKeys.has(displayName.toUpperCase())) {
                    seenKeys.add(displayName.toUpperCase());
                    marketsList.push({
                        displayName: displayName,
                        filterKey: filterKey || custName,
                        marketName: marketName || 'General',
                        customerName: custName
                    });
                }
            });
        }

        // Fallback to /api/customers/markets if still empty
        if (marketsList.length === 0) {
            try {
                const res = await fetch('/api/customers/markets');
                if (res.ok) {
                    const rawMarkets = await res.json();
                    (rawMarkets || []).forEach(m => {
                        if (m && typeof m === 'string' && m.trim()) {
                            const trimmed = m.trim();
                            if (!seenKeys.has(trimmed.toUpperCase())) {
                                seenKeys.add(trimmed.toUpperCase());
                                marketsList.push({
                                    displayName: trimmed,
                                    filterKey: trimmed,
                                    marketName: trimmed,
                                    customerName: ''
                                });
                            }
                        }
                    });
                }
            } catch (e) {}
        }

        renderMarketsList(marketsList);
        openModal('marketsModal');
    } catch (err) {
        console.error('Failed to open markets list modal:', err);
    }
}

function renderMarketsList(marketsList) {
    const container = document.getElementById('marketsListContainer');
    const countEl = document.getElementById('marketsModalCount');
    if (!container) return;

    if (countEl) {
        countEl.textContent = marketsList ? marketsList.length : 0;
    }

    if (!marketsList || marketsList.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; color: var(--text-muted); padding: 1.5rem;">
                No registered markets or customer accounts found.
            </div>
        `;
        return;
    }

    let html = '<div style="display: flex; flex-direction: column; gap: 0.65rem;">';
    marketsList.forEach((item, idx) => {
        html += `
            <div class="market-list-item" onclick="filterByMarketFromModal('${escapeHtml(item.filterKey)}')" style="display: flex; align-items: center; justify-content: space-between; background: rgba(30, 41, 59, 0.7); border: 1px solid var(--border-card); padding: 0.75rem 1rem; border-radius: 0.85rem; cursor: pointer; transition: all 0.2s ease;">
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                    <span style="background: rgba(56, 189, 248, 0.18); color: #38bdf8; font-weight: 800; font-size: 0.85rem; min-width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 1px solid rgba(56, 189, 248, 0.35);">
                        ${idx + 1}.
                    </span>
                    <span style="font-weight: 700; color: #f8fafc; font-size: 0.95rem;">
                        ${escapeHtml(item.displayName)}
                    </span>
                </div>
                <span style="font-size: 0.75rem; color: #34d399; font-weight: 600; background: rgba(52, 211, 153, 0.12); padding: 0.2rem 0.6rem; border-radius: 9999px; border: 1px solid rgba(52, 211, 153, 0.25);">
                    Active
                </span>
            </div>
        `;
    });
    html += '</div>';

    container.innerHTML = html;
}

window.filterByMarketFromModal = function(marketName) {
    const dropdown = document.getElementById('receiptsDropdownMenu');
    const card = document.getElementById('kpiGeneratedReceiptsCard');
    const arrow = document.getElementById('receiptDropdownArrow');
    if (dropdown) dropdown.style.display = 'none';
    if (card) {
        card.style.zIndex = '';
        card.classList.remove('dropdown-active');
    }
    if (arrow) arrow.textContent = '▼';

    closeModal('marketsModal');
    const chip = document.querySelector(`.market-chips .chip[data-market="${marketName}"]`);
    if (chip) {
        chip.click();
    } else {
        loadCustomers('', marketName);
    }
};

window.openMarketsModal = openMarketsModal;
window.renderMarketsList = renderMarketsList;

window.deleteCustomer = async function(id, name) {
    if (!confirm(`Are you sure you want to delete customer "${name}"? This action cannot be undone.`)) {
        return;
    }

    try {
        const res = await fetch(`/api/customers/${id}`, {
            method: 'DELETE'
        });

        if (res.ok) {
            alert(`✅ Customer "${name}" deleted successfully.`);
            await loadCustomers('', getSelectedMarket());
            await loadDashboardMetrics();
        } else {
            alert('❌ Failed to delete customer.');
        }
    } catch (err) {
        console.error('Delete error:', err);
        alert('❌ Error deleting customer.');
    }
};

// --- Receipt Photo (Image) Generation & Action Helpers ---
let currentCustomerName = 'Customer';
let currentCleanMobile = '';
let currentWaLink = '';
let currentActiveCustomerId = null;

window.saveEditedReceipt = async function() {
    const custId = currentActiveCustomerId || (document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '');
    if (!custId) {
        alert('⚠️ Customer not identified. Please select a customer first.');
        return;
    }

    const customerObj = customersData ? customersData.find(c => c.id == custId) : null;

    const poSell = parseFloat(document.getElementById('editPoSell') ? document.getElementById('editPoSell').value : 0) || 0;
    const poPay = parseFloat(document.getElementById('editPoPay') ? document.getElementById('editPoPay').value : 0) || 0;
    const pcSell = parseFloat(document.getElementById('editPcSell') ? document.getElementById('editPcSell').value : 0) || 0;
    const pcPay = parseFloat(document.getElementById('editPcPay') ? document.getElementById('editPcPay').value : 0) || 0;
    const magilYene = parseFloat(document.getElementById('editMagilYene') ? document.getElementById('editMagilYene').value : 0) || 0;
    const magilDene = parseFloat(document.getElementById('editMagilDene') ? document.getElementById('editMagilDene').value : 0) || 0;
    const missPayment = parseFloat(document.getElementById('editMissPayment') ? document.getElementById('editMissPayment').value : 0) || 0;
    const txDate = document.getElementById('txDate') ? document.getElementById('txDate').value : new Date().toISOString().split('T')[0];

    const styleEl = document.getElementById('tradeReceiptStyle');
    const style = styleEl && styleEl.value ? styleEl.value : (customerObj ? customerObj.receiptStyle : 'TYPE_1');
    const isShareEnabled = isShareEnabledForCustomer(customerObj);
    const effectiveShareRate = isShareEnabled ? (parseFloat(document.getElementById('shareRatePercent')?.value) || 40.0) : 100.0;
    const isCommEnabled = isCommEnabledForCustomer(customerObj);
    const commPercent = isCommEnabled ? (parseFloat(document.getElementById('commPercent')?.value) || (customerObj?.commissionRate != null ? customerObj.commissionRate : 10.0)) : 10.0;
    const pagarVal = (customerObj && isPagarEnabledForCustomer(customerObj)) ? (customerObj.pagar || 0) : 0;

    const payload = {
        customerId: parseInt(custId),
        transactionDate: txDate,
        sellPo: poSell,
        sellPc: pcSell,
        paymentPo: poPay,
        paymentPc: pcPay,
        magilBaki: magilYene - magilDene,
        pagarAmount: pagarVal,
        farak: missPayment,
        receiptStyle: style,
        shareRate: effectiveShareRate,
        rate: 1.0,
        commissionPercentage: commPercent,
        paymentAmount: poPay + pcPay,
        paymentMode: 'Cash/UPI',
        notes: 'Updated via Edit Full Receipt'
    };

    try {
        const btn = document.getElementById('btnSaveEditedReceipt');
        if (btn) {
            btn.disabled = true;
            btn.textContent = '⏳ Saving & Updating Everywhere...';
        }

        const res = await fetch('/api/transactions/update', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            await loadCustomers('', getSelectedMarket());
            if (currentSegment === 'weekly') {
                await loadWeeklyReceipts();
            }
            await loadDashboardMetrics();
            await triggerWhatsApp(custId, false);

            alert('✅ Receipt updated successfully! Today\'s Total Sell (PO & PC sell), Payments, Commission, and Ledgers have been updated everywhere across the application.');
        } else {
            let errorMsg = '❌ Failed to update receipt in database.';
            try {
                const errData = await res.json();
                if (errData && errData.message) errorMsg = errData.message;
            } catch (e) {}
            alert(errorMsg);
        }
    } catch (err) {
        console.error('Error saving edited receipt:', err);
        alert('❌ Error updating receipt in database.');
    } finally {
        const btn = document.getElementById('btnSaveEditedReceipt');
        if (btn) {
            btn.disabled = false;
            btn.textContent = '💾 Save & Update Receipt (Reflect Everywhere)';
        }
    }
};

function renderReceiptImageCanvas(formattedMessage, customerName) {
    currentCustomerName = customerName || 'Customer';
    const canvas = document.getElementById('receiptCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const rawLines = formattedMessage ? formattedMessage.split('\n') : [];

    const scale = 2; // High DPI 2x
    const canvasWidth = 560 * scale;
    const paddingX = 28 * scale;
    
    // Parse message into structured header & data rows
    let dateStr = new Date().toLocaleDateString('en-GB'); // default dd/mm/yyyy
    let cityTitle = 'PUNE';
    let totalBalanceStr = '';

    const isWeekly = formattedMessage ? (formattedMessage.toUpperCase().includes('WEEKLY') || formattedMessage.toUpperCase().includes('NET TRADE')) : false;

    const parsedRows = [];
    let pendingMissPrefix = false;

    rawLines.forEach(line => {
        const trimmed = line.trim();
        let cleanText = line.replace(/\*/g, '').trim();

        if (!cleanText) return;

        if (isWeekly && (cleanText.toUpperCase().includes('EXCL') || cleanText.toUpperCase().includes('MAGIL YENE / MAGIL DENE'))) {
            return;
        }

        if (isWeekly && cleanText.toUpperCase().includes('DAY') && cleanText.toUpperCase().includes('NET TRADE')) {
            return;
        }

        // 1. Check Date Badge
        if (cleanText.toLowerCase().includes('date:')) {
            const match = cleanText.match(/date:\s*([^\s*]+)/i);
            if (match && match[1]) dateStr = match[1];
            return;
        }

        // 2. Check Divider Lines
        if (trimmed.startsWith('---') || trimmed.includes('-----') || trimmed.startsWith('___') || trimmed.includes('____')) {
            if (parsedRows.length > 0 && parsedRows[parsedRows.length - 1].type !== 'divider') {
                parsedRows.push({ type: 'divider' });
            }
            return;
        }

        // 3. Check Total Balance Due Banner
        if (cleanText.toUpperCase().includes('TOTAL BALANCE DUE')) {
            const parts = cleanText.split(/:-|:/);
            if (parts.length > 1) {
                totalBalanceStr = parts[1].trim();
            } else {
                totalBalanceStr = cleanText.replace(/TOTAL BALANCE DUE/i, '').trim();
            }
            return;
        }

        // 4. Check Column Header Line (e.g. PARTICULARS SELL PAYMENT) -> Skip table header
        if (cleanText.toUpperCase().includes('PARTICULARS') || (cleanText.toUpperCase().includes('SELL') && cleanText.toUpperCase().includes('PAYMENT') && !cleanText.includes(':'))) {
            return;
        }

        // 5. Check Top Header Title (First non-numeric title line e.g. TEST123 or PUNE)
        if (!/\d/.test(cleanText) && !cleanText.includes(':') && !cleanText.includes(':-') && parsedRows.length === 0) {
            cityTitle = cleanText.toUpperCase();
            return;
        }

        // 6. Data Row
        let isMagil = line.includes('🔴') || cleanText.toUpperCase().includes('MAGIL');
        cleanText = cleanText.replace(/🔴/g, '').trim();

        const parts = cleanText.split(/:-|:/);
        let label = parts[0] ? parts[0].trim() : '';
        label = label.replace(/\s*\((Som|Mangal|Budh|Guru|Shukra|Shani|Ravi)\)/gi, '').trim();

        if (pendingMissPrefix) {
            label = 'MISS ' + label;
            pendingMissPrefix = false;
        }

        let isHighlight = label.toUpperCase().startsWith('TOTAL') || label.toUpperCase().startsWith('REMAINING') || label.toUpperCase().startsWith('NET') || label.toUpperCase().startsWith('WEEKLY TOTAL');

        let sellVal = '';
        let payVal = '';

        if (parts.length >= 3) {
            sellVal = parts[1].trim();
            payVal = parts[2].trim();
        } else if (parts.length === 2) {
            const valStr = parts[1].trim();
            if (isWeekly) {
                payVal = valStr;
                sellVal = '';
            } else {
                const numbers = valStr.match(/[\d,.]+(\s*(yeṇe|dene|yene|dene))?/gi) || [];

                if (numbers.length >= 2) {
                    sellVal = numbers[0].trim();
                    payVal = numbers[1].trim();
                } else if (numbers.length === 1) {
                    if (label.toUpperCase().includes('PAYMENT')) {
                        payVal = numbers[0].trim();
                    } else {
                        sellVal = numbers[0].trim();
                    }
                } else {
                    if (label.toUpperCase().includes('PAYMENT')) {
                        payVal = valStr;
                    } else {
                        sellVal = valStr;
                    }
                }
            }
        } else {
            if (label.toUpperCase() === 'MISS') {
                pendingMissPrefix = true;
                return;
            }
        }

        parsedRows.push({
            type: isHighlight ? 'highlight_row' : (isMagil ? 'magil_row' : 'row'),
            label: label ? (label.endsWith(':-') || label.endsWith(':') ? label : label + ' :-') : '',
            sellVal: sellVal,
            payVal: payVal,
            isHighlight: isHighlight,
            isMagil: isMagil
        });
    });

    if (!totalBalanceStr) {
        for (let i = parsedRows.length - 1; i >= 0; i--) {
            const r = parsedRows[i];
            if (r.type !== 'divider' && (r.sellVal || r.payVal)) {
                if ((r.sellVal && (r.sellVal.includes('yeṇe') || r.sellVal.includes('dene') || r.sellVal.includes('yene'))) ||
                    (r.payVal && (r.payVal.includes('yeṇe') || r.payVal.includes('dene') || r.payVal.includes('yene')))) {
                    totalBalanceStr = r.sellVal || r.payVal;
                    break;
                }
            }
        }
    }

    const fontStack = '"Plus Jakarta Sans", "Segoe UI", system-ui, -apple-system, sans-serif';

    // Compute dynamic canvas height
    const headerHeight = 110 * scale;
    const colHeaderHeight = 48 * scale;
    const rowHeight = 42 * scale;
    const bottomBannerHeight = 100 * scale;
    
    let contentRowsCount = 0;
    parsedRows.forEach(r => {
        if (r.type === 'divider') contentRowsCount += 0.5;
        else contentRowsCount += 1;
    });

    const canvasHeight = Math.ceil(headerHeight + colHeaderHeight + (contentRowsCount * rowHeight) + bottomBannerHeight + (35 * scale));

    canvas.width = canvasWidth;
    canvas.height = canvasHeight;

    const cornerRadius = 20 * scale;
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);

    // 1. White Card Background
    ctx.fillStyle = '#ffffff';
    drawRoundedRect(ctx, 0, 0, canvasWidth, canvasHeight, cornerRadius);
    ctx.fill();

    // 2. Outer Cyan/Blue Border (#0284c7)
    ctx.strokeStyle = '#0284c7';
    ctx.lineWidth = 4 * scale;
    drawRoundedRect(ctx, 2 * scale, 2 * scale, canvasWidth - 4 * scale, canvasHeight - 4 * scale, cornerRadius);
    ctx.stroke();

    // 3. Top Dark Navy Header Box (#0f172a)
    ctx.save();
    ctx.beginPath();
    drawRoundedRect(ctx, 2 * scale, 2 * scale, canvasWidth - 4 * scale, headerHeight, cornerRadius);
    ctx.clip();

    ctx.fillStyle = '#0f172a';
    ctx.fillRect(0, 0, canvasWidth, headerHeight + cornerRadius);

    // City Title & Customer Name Header
    let displayHeaderTitle = (currentCustomerName && currentCustomerName !== 'Customer') ? currentCustomerName.toUpperCase() : (cityTitle || 'CUSTOMER STATEMENT');

    // City Title (Centered Extra-Bold Light Blue #38bdf8)
    ctx.fillStyle = '#38bdf8';
    ctx.font = `900 ${26 * scale}px ${fontStack}`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillText(displayHeaderTitle, canvasWidth / 2, 18 * scale);

    // Date Pill Badge (#1e293b pill with border #334155) - Increased Size & Font
    const dateText = `DATE: ${dateStr}`;
    ctx.font = `900 ${18 * scale}px ${fontStack}`;
    const dateWidth = ctx.measureText(dateText).width + (32 * scale);
    const dateHeight = 34 * scale;
    const dateX = (canvasWidth - dateWidth) / 2;
    const dateY = 60 * scale;

    ctx.fillStyle = '#1e293b';
    ctx.strokeStyle = '#38bdf8';
    ctx.lineWidth = 2 * scale;
    drawRoundedRect(ctx, dateX, dateY, dateWidth, dateHeight, 17 * scale);
    ctx.fill();
    ctx.stroke();

    ctx.fillStyle = '#ffffff';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(dateText, canvasWidth / 2, dateY + (dateHeight / 2) + (1 * scale));
    ctx.restore();

    // 4. Column Headers
    let currentY = headerHeight + (25 * scale);
    const labelX = paddingX + (8 * scale);
    const sellColX = 370 * scale; // Right-aligned SELL column
    const payColX = 510 * scale;  // Right-aligned PAYMENT / NET TRADE column

    ctx.font = `900 ${16 * scale}px ${fontStack}`;
    ctx.fillStyle = '#334155';
    ctx.textBaseline = 'middle';

    if (isWeekly) {
        ctx.textAlign = 'left';
        ctx.fillText('DAY', labelX, currentY);

        ctx.textAlign = 'right';
        ctx.fillText('NET TRADE', payColX, currentY);
    } else {
        ctx.textAlign = 'left';
        ctx.fillText('PARTICULARS', labelX, currentY);

        ctx.textAlign = 'right';
        ctx.fillText('SELL (₹)', sellColX, currentY);
        ctx.fillText('PAYMENT (₹)', payColX, currentY);
    }

    currentY += 20 * scale;

    // Header divider line
    ctx.beginPath();
    ctx.strokeStyle = '#94a3b8';
    ctx.lineWidth = 2 * scale;
    ctx.moveTo(paddingX, currentY);
    ctx.lineTo(canvasWidth - paddingX, currentY);
    ctx.stroke();

    currentY += 20 * scale;

    // 5. Render Data Rows
    parsedRows.forEach(row => {
        if (row.type === 'divider') {
            ctx.beginPath();
            ctx.strokeStyle = '#94a3b8';
            ctx.lineWidth = 2 * scale;
            ctx.setLineDash([5 * scale, 5 * scale]);
            ctx.moveTo(paddingX, currentY - (11 * scale));
            ctx.lineTo(canvasWidth - paddingX, currentY - (11 * scale));
            ctx.stroke();
            ctx.setLineDash([]);
            currentY += 22 * scale;
            return;
        }

        const isHighlight = row.isHighlight;
        const isMagil = row.isMagil;

        // Row background highlight tint
        if (isHighlight) {
            ctx.fillStyle = '#f0f9ff';
            drawRoundedRect(ctx, paddingX, currentY - (16 * scale), canvasWidth - (2 * paddingX), 34 * scale, 8 * scale);
            ctx.fill();
        } else if (isMagil) {
            ctx.fillStyle = '#fff1f2';
            drawRoundedRect(ctx, paddingX, currentY - (16 * scale), canvasWidth - (2 * paddingX), 34 * scale, 8 * scale);
            ctx.fill();
        }

        ctx.font = `900 ${19 * scale}px ${fontStack}`;

        // Label
        if (isMagil) {
            // Red dot for Magil Yene / Magil Dene
            ctx.fillStyle = '#e11d48';
            ctx.beginPath();
            ctx.arc(labelX + (8 * scale), currentY, 7 * scale, 0, Math.PI * 2);
            ctx.fill();

            ctx.textAlign = 'left';
            ctx.fillText(row.label.replace(':-', '').trim() + ' :-', labelX + (22 * scale), currentY);

            ctx.textAlign = 'right';
            const valToDraw = row.sellVal || row.payVal;
            if (isWeekly && (valToDraw.toLowerCase().includes('dene'))) {
                ctx.fillStyle = '#dc2626';
            } else if (isWeekly && (valToDraw.toLowerCase().includes('yene') || valToDraw.toLowerCase().includes('yeṇe'))) {
                ctx.fillStyle = '#0f172a';
            } else {
                ctx.fillStyle = '#0f172a';
            }
            ctx.fillText(valToDraw, isWeekly ? payColX : sellColX, currentY);
        } else {
            ctx.fillStyle = isHighlight ? '#0284c7' : '#0f172a';
            ctx.textAlign = 'left';
            ctx.fillText(row.label, labelX, currentY);

            ctx.textAlign = 'right';
            if (row.sellVal) {
                if (isWeekly && row.sellVal.toLowerCase().includes('dene')) {
                    ctx.fillStyle = '#dc2626';
                } else if (isWeekly && (row.sellVal.toLowerCase().includes('yene') || row.sellVal.toLowerCase().includes('yeṇe'))) {
                    ctx.fillStyle = '#0f172a';
                } else {
                    ctx.fillStyle = isHighlight ? '#0284c7' : '#0f172a';
                }
                ctx.fillText(row.sellVal, sellColX, currentY);
            }
            if (row.payVal) {
                if (isWeekly && row.payVal.toLowerCase().includes('dene')) {
                    ctx.fillStyle = '#dc2626';
                } else if (isWeekly && (row.payVal.toLowerCase().includes('yene') || row.payVal.toLowerCase().includes('yeṇe'))) {
                    ctx.fillStyle = '#0f172a';
                } else {
                    ctx.fillStyle = isHighlight ? '#0284c7' : '#0f172a';
                }
                ctx.fillText(row.payVal, payColX, currentY);
            }
        }

        currentY += rowHeight;
    });

    // 6. Bottom Navy Banner (TOTAL BALANCE DUE)
    currentY += 15 * scale;
    const bannerBoxX = paddingX;
    const bannerBoxY = currentY;
    const bannerBoxWidth = canvasWidth - (2 * paddingX);
    const bannerBoxHeight = 90 * scale;

    ctx.fillStyle = '#0f172a';
    drawRoundedRect(ctx, bannerBoxX, bannerBoxY, bannerBoxWidth, bannerBoxHeight, 14 * scale);
    ctx.fill();

    ctx.strokeStyle = '#0284c7';
    ctx.lineWidth = 2.5 * scale;
    drawRoundedRect(ctx, bannerBoxX, bannerBoxY, bannerBoxWidth, bannerBoxHeight, 14 * scale);
    ctx.stroke();

    // Banner Text
    ctx.fillStyle = '#94a3b8';
    ctx.font = `800 ${14 * scale}px ${fontStack}`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillText('TOTAL BALANCE DUE', canvasWidth / 2, bannerBoxY + (16 * scale));

    const finalValText = totalBalanceStr || '₹0.00';
    const isDene = finalValText.toLowerCase().includes('dene');
    ctx.fillStyle = isDene ? '#f87171' : '#38bdf8';
    ctx.font = `900 ${30 * scale}px ${fontStack}`;
    ctx.textBaseline = 'bottom';
    ctx.fillText(finalValText, canvasWidth / 2, bannerBoxY + bannerBoxHeight - (12 * scale));
}

function drawRoundedRect(ctx, x, y, width, height, radius) {
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + width - radius, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    ctx.lineTo(x + width, y + height - radius);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    ctx.lineTo(x + radius, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
}

let currentFormattedMessage = '';
let originalFormattedMessage = '';

function toggleEditReceiptPanel() {
    const editContainer = document.getElementById('waEditContainer');
    const editText = document.getElementById('waReceiptEditText');
    if (!editContainer) return;

    const isWeekly = currentFormattedMessage ? (currentFormattedMessage.toUpperCase().includes('WEEKLY') || currentFormattedMessage.toUpperCase().includes('NET TRADE')) : false;

    const isHidden = editContainer.style.display === 'none' || !editContainer.style.display;
    if (isHidden) {
        editContainer.style.display = 'block';
        if (document.getElementById('dailyEditFields')) {
            document.getElementById('dailyEditFields').style.display = isWeekly ? 'none' : 'grid';
        }
        if (document.getElementById('weeklyEditFields')) {
            document.getElementById('weeklyEditFields').style.display = isWeekly ? 'block' : 'none';
        }
        if (editText) {
            editText.value = currentFormattedMessage || '';
            editText.focus();
        }
        populateQuickFieldsFromText(currentFormattedMessage || '');
        editContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } else {
        editContainer.style.display = 'none';
    }
}

function extractRowNumbers(cleanLine) {
    let afterLabel = cleanLine;
    const colonIdx = cleanLine.indexOf(':');
    if (colonIdx !== -1) {
        afterLabel = cleanLine.substring(colonIdx + 1);
    }
    const nums = afterLabel.match(/[\d,]+(?:\.\d+)?/g) || [];
    const sell = nums.length >= 1 ? (parseFloat(nums[0].replace(/,/g, '')) || 0) : 0;
    const pay = nums.length >= 2 ? (parseFloat(nums[1].replace(/,/g, '')) || 0) : 0;
    return { sell, pay };
}

function updateCalculatedValuesInText(text) {
    if (!text) return text;

    const isWeekly = text.toUpperCase().includes('WEEKLY') || text.toUpperCase().includes('NET TRADE');
    const lines = text.split('\n');

    if (isWeekly) {
        let weeklySum = 0;
        let commPct = 0;
        let inputCommEl = document.getElementById('editWeeklyComm');
        if (inputCommEl && inputCommEl.value !== '') {
            commPct = parseFloat(inputCommEl.value) || 0;
        }

        let magilYeneVal = 0;
        let magilDeneVal = 0;
        const magilYeneEl = document.getElementById('editWeeklyMagilYene');
        if (magilYeneEl && magilYeneEl.value !== '') magilYeneVal = parseFloat(magilYeneEl.value) || 0;
        const magilDeneEl = document.getElementById('editWeeklyMagilDene');
        if (magilDeneEl && magilDeneEl.value !== '') magilDeneVal = parseFloat(magilDeneEl.value) || 0;

        lines.forEach(l => {
            const clean = l.replace(/\*/g, '').trim();
            const upper = clean.toUpperCase();
            if (upper.startsWith('MON :-') || upper.startsWith('TUE :-') || upper.startsWith('WED :-') ||
                upper.startsWith('THU :-') || upper.startsWith('FRI :-') || upper.startsWith('SAT :-') || upper.startsWith('SUN :-') ||
                upper.startsWith('MON:') || upper.startsWith('TUE:') || upper.startsWith('WED:') ||
                upper.startsWith('THU:') || upper.startsWith('FRI:') || upper.startsWith('SAT:') || upper.startsWith('SUN:')) {
                const { sell } = extractRowNumbers(clean);
                if (upper.includes('DENE')) {
                    weeklySum -= sell;
                } else if (sell > 0) {
                    weeklySum += sell;
                }
            } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
                if (commPct === 0) {
                    const commMatch = clean.match(/COM\s*\((\d+(\.\d+)?)%\)/i);
                    if (commMatch && commMatch[1]) commPct = parseFloat(commMatch[1]);
                }
            } else if (upper.includes('MAGIL YENE')) {
                if (magilYeneVal === 0) {
                    const { sell, pay } = extractRowNumbers(clean);
                    magilYeneVal = sell || pay;
                }
            } else if (upper.includes('MAGIL DENE')) {
                if (magilDeneVal === 0) {
                    const { sell, pay } = extractRowNumbers(clean);
                    magilDeneVal = sell || pay;
                }
            } else if (upper.includes('MAGIL')) {
                const { sell, pay } = extractRowNumbers(clean);
                if (upper.includes('DENE')) {
                    if (magilDeneVal === 0) magilDeneVal = sell || pay;
                } else {
                    if (magilYeneVal === 0) magilYeneVal = sell || pay;
                }
            }
        });

        let commVal = 0;
        if (commPct > 0) {
            commVal = Math.round(Math.abs(weeklySum) * (commPct / 100));
        }

        let netWeekly = weeklySum >= 0 ? (weeklySum - commVal) : (weeklySum + commVal);
        let finalNet = netWeekly + magilYeneVal - magilDeneVal;

        let statusStr = netWeekly >= 0 ? 'yeṇe' : 'dene';
        let absNetStr = Math.abs(netWeekly).toLocaleString('en-IN');
        let absSumStr = Math.abs(weeklySum).toLocaleString('en-IN');
        let sumStatusStr = weeklySum >= 0 ? 'yeṇe' : 'dene';

        let finalStatusStr = finalNet >= 0 ? 'yeṇe' : 'dene';
        let absFinalNetStr = Math.abs(finalNet).toLocaleString('en-IN');

        let newLines = [];
        let hasTotalLine = false;

        lines.forEach(line => {
            const clean = line.replace(/\*/g, '').trim();
            const upper = clean.toUpperCase();

            if (upper.startsWith('TOTAL :-') || upper.startsWith('TOTAL:')) {
                if (commPct > 0) {
                    hasTotalLine = true;
                    newLines.push(`*TOTAL :-*           ${absSumStr} ${sumStatusStr}`);
                }
            } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
                if (commPct > 0) {
                    newLines.push(`*COM (${commPct}%) :-*       ${commVal.toLocaleString('en-IN')}`);
                }
            } else if (upper.startsWith('WEEKLY TOTAL :-') || upper.startsWith('WEEKLY TOTAL:')) {
                if (commPct > 0 && !hasTotalLine) {
                    newLines.push(`*TOTAL :-*           ${absSumStr} ${sumStatusStr}`);
                    newLines.push(`*COM (${commPct}%) :-*       ${commVal.toLocaleString('en-IN')}`);
                }
                newLines.push(`*WEEKLY TOTAL :-*    ${absNetStr} ${statusStr}`);
            } else if (upper.includes('MAGIL YENE')) {
                if (magilYeneVal > 0) {
                    newLines.push(`🔴 *MAGIL YENE :-*   ${magilYeneVal.toLocaleString('en-IN')} yeṇe`);
                }
            } else if (upper.includes('MAGIL DENE')) {
                if (magilDeneVal > 0) {
                    newLines.push(`🔴 *MAGIL DENE :-*   ${magilDeneVal.toLocaleString('en-IN')} dene`);
                }
            } else if (upper.includes('MAGIL')) {
                if (upper.includes('DENE')) {
                    if (magilDeneVal > 0) newLines.push(`🔴 *MAGIL DENE :-*   ${magilDeneVal.toLocaleString('en-IN')} dene`);
                } else {
                    if (magilYeneVal > 0) newLines.push(`🔴 *MAGIL YENE :-*   ${magilYeneVal.toLocaleString('en-IN')} yeṇe`);
                }
            } else if (upper.includes('TOTAL BALANCE DUE')) {
                newLines.push(`TOTAL BALANCE DUE ${absFinalNetStr} ${finalStatusStr}`);
            } else {
                newLines.push(line);
            }
        });

        return newLines.join('\n');
    }

    let sellPo = null, payPo = null;
    let sellPc = null, payPc = null;
    let commPct = 10;
    let commVal = null;
    let pagarVal = 0;
    let magilYeneVal = 0;
    let magilDeneVal = 0;
    let missPaymentVal = 0;
    let hasCommLine = false;

    lines.forEach(l => {
        const clean = l.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();
        if (upper.startsWith('PO :-') || upper.startsWith('PO:')) {
            const { sell, pay } = extractRowNumbers(clean);
            sellPo = sell;
            payPo = pay;
        } else if (upper.startsWith('PC :-') || upper.startsWith('PC:')) {
            const { sell, pay } = extractRowNumbers(clean);
            sellPc = sell;
            payPc = pay;
        } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
            hasCommLine = true;
            const commMatch = clean.match(/COM\s*\((\d+(\.\d+)?)%\)/i);
            if (commMatch && commMatch[1]) commPct = parseFloat(commMatch[1]);
        } else if (upper.includes('MAGIL YENE')) {
            const { sell } = extractRowNumbers(clean);
            magilYeneVal = sell;
        } else if (upper.includes('MAGIL DENE')) {
            const { sell } = extractRowNumbers(clean);
            magilDeneVal = sell;
        } else if (upper.includes('MAGIL')) {
            const { sell } = extractRowNumbers(clean);
            if (upper.includes('DENE')) {
                magilDeneVal = sell;
            } else {
                magilYeneVal = sell;
            }
        } else if (upper.startsWith('PAGAR')) {
            const { sell } = extractRowNumbers(clean);
            pagarVal = sell;
        } else if (upper.includes('MISS')) {
            const { sell, pay } = extractRowNumbers(clean);
            missPaymentVal = pay || sell;
        }
    });

    if (sellPo === null && sellPc === null) return text;

    const totalSell = (sellPo || 0) + (sellPc || 0);
    const totalPay = (payPo || 0) + (payPc || 0);

    // Commission is calculated if statement has a COM line (always applies on totalSell)
    if (hasCommLine) {
        commVal = Math.round(totalSell * (commPct / 100));
    } else {
        commVal = 0;
    }

    const totalAfterComm = totalSell - commVal;
    const remaining = totalAfterComm - totalPay - pagarVal - missPaymentVal;
    const netBal = remaining + magilYeneVal - magilDeneVal;
    const balSuffix = netBal >= 0 ? 'yeṇe' : 'dene';
    const absBalStr = Math.abs(netBal).toLocaleString('en-IN');

    let totalCount = 0;
    const updatedLines = lines.map(line => {
        const clean = line.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();

        if (upper.startsWith('TOTAL :-') || upper.startsWith('TOTAL:')) {
            totalCount++;
            if (totalCount === 1) {
                return line.replace(/:-.*/, `:- ${totalSell.toLocaleString('en-IN')} : ${totalPay.toLocaleString('en-IN')}`);
            } else if (totalCount === 2) {
                return line.replace(/:-.*/, `:- ${totalAfterComm.toLocaleString('en-IN')}`);
            }
        } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
            return line.replace(/:-.*/, `:- ${commVal.toLocaleString('en-IN')}`);
        } else if (upper.startsWith('REMAINING :-') || upper.startsWith('REMAINING:')) {
            return line.replace(/:-.*/, `:- ${remaining.toLocaleString('en-IN')}`);
        } else if (upper.includes('TOTAL BALANCE DUE')) {
            return line.replace(/TOTAL BALANCE DUE.*/i, `TOTAL BALANCE DUE ${absBalStr} ${balSuffix}`);
        }
        return line;
    });

    return updatedLines.join('\n');
}

function populateQuickFieldsFromText(text) {
    if (!text) return;
    const lines = text.split('\n');

    let poSell = null, poPay = null;
    let pcSell = null, pcPay = null;
    let magilYene = null, magilDene = null;
    let missPayment = null;
    let weeklyComm = 0;

    lines.forEach(line => {
        const clean = line.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();

        if (upper.startsWith('PO :-') || upper.startsWith('PO:')) {
            const { sell, pay } = extractRowNumbers(clean);
            poSell = sell;
            poPay = pay;
        } else if (upper.startsWith('PC :-') || upper.startsWith('PC:')) {
            const { sell, pay } = extractRowNumbers(clean);
            pcSell = sell;
            pcPay = pay;
        } else if (upper.includes('MAGIL YENE')) {
            const { sell } = extractRowNumbers(clean);
            magilYene = sell;
        } else if (upper.includes('MAGIL DENE')) {
            const { sell } = extractRowNumbers(clean);
            magilDene = sell;
        } else if (upper.includes('MAGIL')) {
            const { sell } = extractRowNumbers(clean);
            if (upper.includes('DENE')) {
                magilDene = sell;
            } else {
                magilYene = sell;
            }
        } else if (upper.includes('MISS')) {
            const { sell, pay } = extractRowNumbers(clean);
            missPayment = pay || sell;
        } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
            const match = clean.match(/COM\s*\((\d+(\.\d+)?)%\)/i);
            if (match && match[1]) weeklyComm = parseFloat(match[1]);
        }
    });

    if (document.getElementById('editPoSell')) document.getElementById('editPoSell').value = (poSell !== null && !isNaN(poSell)) ? poSell : 0;
    if (document.getElementById('editPoPay')) document.getElementById('editPoPay').value = (poPay !== null && !isNaN(poPay)) ? poPay : 0;
    if (document.getElementById('editPcSell')) document.getElementById('editPcSell').value = (pcSell !== null && !isNaN(pcSell)) ? pcSell : 0;
    if (document.getElementById('editPcPay')) document.getElementById('editPcPay').value = (pcPay !== null && !isNaN(pcPay)) ? pcPay : 0;
    if (document.getElementById('editMagilYene')) document.getElementById('editMagilYene').value = (magilYene !== null && !isNaN(magilYene)) ? magilYene : 0;
    if (document.getElementById('editMagilDene')) document.getElementById('editMagilDene').value = (magilDene !== null && !isNaN(magilDene)) ? magilDene : 0;
    if (document.getElementById('editMissPayment')) document.getElementById('editMissPayment').value = (missPayment !== null && !isNaN(missPayment)) ? missPayment : 0;
    if (document.getElementById('editWeeklyComm')) document.getElementById('editWeeklyComm').value = (weeklyComm > 0) ? weeklyComm : '';
    if (document.getElementById('editWeeklyMagilYene')) document.getElementById('editWeeklyMagilYene').value = (magilYene !== null && !isNaN(magilYene) && magilYene > 0) ? magilYene : '';
    if (document.getElementById('editWeeklyMagilDene')) document.getElementById('editWeeklyMagilDene').value = (magilDene !== null && !isNaN(magilDene) && magilDene > 0) ? magilDene : '';
}

window.onWeeklyQuickFieldEdited = function() {
    const magilYene = parseFloat(document.getElementById('editWeeklyMagilYene')?.value || 0) || 0;
    const magilDene = parseFloat(document.getElementById('editWeeklyMagilDene')?.value || 0) || 0;

    let text = document.getElementById('waReceiptEditText')?.value || currentFormattedMessage || '';
    let lines = text.split('\n');

    let hasMagilYene = false;
    let hasMagilDene = false;

    let updatedLines = lines.map(line => {
        const clean = line.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();

        if (upper.includes('MAGIL YENE')) {
            hasMagilYene = true;
            if (magilYene > 0) return `🔴 *MAGIL YENE :-*   ${magilYene.toLocaleString('en-IN')} yeṇe`;
            return null;
        } else if (upper.includes('MAGIL DENE')) {
            hasMagilDene = true;
            if (magilDene > 0) return `🔴 *MAGIL DENE :-*   ${magilDene.toLocaleString('en-IN')} dene`;
            return null;
        } else if (upper.includes('MAGIL')) {
            if (upper.includes('DENE')) {
                hasMagilDene = true;
                if (magilDene > 0) return `🔴 *MAGIL DENE :-*   ${magilDene.toLocaleString('en-IN')} dene`;
                return null;
            } else {
                hasMagilYene = true;
                if (magilYene > 0) return `🔴 *MAGIL YENE :-*   ${magilYene.toLocaleString('en-IN')} yeṇe`;
                return null;
            }
        }
        return line;
    }).filter(l => l !== null && l !== undefined);

    let insertIdx = updatedLines.findIndex(l => l.replace(/\*/g, '').toUpperCase().includes('TOTAL BALANCE DUE'));
    if (insertIdx === -1) insertIdx = updatedLines.length;

    if (!hasMagilYene && magilYene > 0) {
        updatedLines.splice(insertIdx, 0, `🔴 *MAGIL YENE :-*   ${magilYene.toLocaleString('en-IN')} yeṇe`);
        insertIdx++;
    }
    if (!hasMagilDene && magilDene > 0) {
        updatedLines.splice(insertIdx, 0, `🔴 *MAGIL DENE :-*   ${magilDene.toLocaleString('en-IN')} dene`);
        insertIdx++;
    }

    let newText = updatedLines.join('\n');
    newText = updateCalculatedValuesInText(newText);

    currentFormattedMessage = newText;
    const editText = document.getElementById('waReceiptEditText');
    if (editText) editText.value = newText;

    renderReceiptImageCanvas(newText, currentCustomerName);
};

function onQuickFieldEdited() {
    const sellPo = parseFloat(document.getElementById('editPoSell') ? document.getElementById('editPoSell').value : 0) || 0;
    const payPo = parseFloat(document.getElementById('editPoPay') ? document.getElementById('editPoPay').value : 0) || 0;
    const sellPc = parseFloat(document.getElementById('editPcSell') ? document.getElementById('editPcSell').value : 0) || 0;
    const payPc = parseFloat(document.getElementById('editPcPay') ? document.getElementById('editPcPay').value : 0) || 0;
    const magilYene = parseFloat(document.getElementById('editMagilYene') ? document.getElementById('editMagilYene').value : 0) || 0;
    const magilDene = parseFloat(document.getElementById('editMagilDene') ? document.getElementById('editMagilDene').value : 0) || 0;
    const missPayment = parseFloat(document.getElementById('editMissPayment') ? document.getElementById('editMissPayment').value : 0) || 0;

    let text = document.getElementById('waReceiptEditText').value || currentFormattedMessage || '';
    let lines = text.split('\n');

    let hasMagilYene = false, hasMagilDene = false, hasMiss = false;

    let updatedLines = lines.map(line => {
        const clean = line.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();

        if (upper.startsWith('PO :-') || upper.startsWith('PO:')) {
            return line.replace(/:-.*/, `:- ${sellPo.toLocaleString('en-IN')} : ${payPo.toLocaleString('en-IN')}`);
        } else if (upper.startsWith('PC :-') || upper.startsWith('PC:')) {
            return line.replace(/:-.*/, `:- ${sellPc.toLocaleString('en-IN')} : ${payPc.toLocaleString('en-IN')}`);
        } else if (upper.includes('MAGIL YENE')) {
            hasMagilYene = true;
            if (magilYene > 0) return `🔴 *MAGIL YENE:* *${magilYene.toLocaleString('en-IN')}*`;
            return null;
        } else if (upper.includes('MAGIL DENE')) {
            hasMagilDene = true;
            if (magilDene > 0) return `🔴 *MAGIL DENE:* *${magilDene.toLocaleString('en-IN')}*`;
            return null;
        } else if (upper.includes('MAGIL')) {
            if (upper.includes('DENE')) {
                hasMagilDene = true;
                if (magilDene > 0) return `🔴 *MAGIL DENE:* *${magilDene.toLocaleString('en-IN')}*`;
                return null;
            } else {
                hasMagilYene = true;
                if (magilYene > 0) return `🔴 *MAGIL YENE:* *${magilYene.toLocaleString('en-IN')}*`;
                return null;
            }
        } else if (upper.includes('MISS')) {
            hasMiss = true;
            if (missPayment > 0) return `*MISS PAYMENT :-* ${missPayment.toLocaleString('en-IN')}`;
            return null;
        }
        return line;
    }).filter(l => l !== null && l !== undefined);

    let insertIdx = updatedLines.findIndex(l => l.replace(/\*/g, '').toUpperCase().includes('TOTAL BALANCE DUE'));
    if (insertIdx === -1) insertIdx = updatedLines.length;

    if (!hasMiss && missPayment > 0) {
        updatedLines.splice(insertIdx, 0, `*MISS PAYMENT :-* ${missPayment.toLocaleString('en-IN')}`);
        insertIdx++;
    }
    if (!hasMagilYene && magilYene > 0) {
        updatedLines.splice(insertIdx, 0, `🔴 *MAGIL YENE:* *${magilYene.toLocaleString('en-IN')}*`);
        insertIdx++;
    }
    if (!hasMagilDene && magilDene > 0) {
        updatedLines.splice(insertIdx, 0, `🔴 *MAGIL DENE:* *${magilDene.toLocaleString('en-IN')}*`);
        insertIdx++;
    }

    let newText = updatedLines.join('\n');
    newText = updateCalculatedValuesInText(newText);

    currentFormattedMessage = newText;
    const editText = document.getElementById('waReceiptEditText');
    if (editText) editText.value = newText;

    renderReceiptImageCanvas(newText, currentCustomerName);
}

function onReceiptTextEdited() {
    const editText = document.getElementById('waReceiptEditText');
    if (!editText) return;

    let newText = editText.value;
    const autoCalc = document.getElementById('chkAutoCalc') ? document.getElementById('chkAutoCalc').checked : true;

    if (autoCalc) {
        newText = updateCalculatedValuesInText(newText);
    }

    currentFormattedMessage = newText;

    let cleanMobile = currentCleanMobile || '';
    const encodedMsg = encodeURIComponent(newText || '');
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    currentWaLink = cleanMobile 
        ? (isMobile ? `https://api.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}` : `https://web.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}`)
        : (isMobile ? `https://api.whatsapp.com/send` : `https://web.whatsapp.com`);

    renderReceiptImageCanvas(newText, currentCustomerName);
}

function resetReceiptText() {
    if (!originalFormattedMessage) return;
    const editText = document.getElementById('waReceiptEditText');
    if (editText) {
        editText.value = originalFormattedMessage;
        populateQuickFieldsFromText(originalFormattedMessage);
        onReceiptTextEdited();
    }
}

// Direct 1-Click WhatsApp Photo Share to Customer Mobile Number
async function shareReceiptPhotoToWhatsApp(targetMode) {
    const canvas = document.getElementById('receiptCanvas');
    if (!canvas) return;

    // No text parameter so ONLY photo is shared/pasted
    let targetUrl = currentCleanMobile
        ? `https://web.whatsapp.com/send?phone=${currentCleanMobile}`
        : `https://web.whatsapp.com`;

    if (targetMode === 'api' || /Android|iPhone|iPad/i.test(navigator.userAgent)) {
        targetUrl = currentCleanMobile
            ? `https://api.whatsapp.com/send?phone=${currentCleanMobile}`
            : `https://api.whatsapp.com/send`;
    }

    canvas.toBlob(async (blob) => {
        if (!blob) {
            alert('❌ Failed to generate receipt photo.');
            return;
        }

        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const fileName = `Receipt_${(currentCustomerName || 'Customer').replace(/\s+/g, '_')}.png`;
        const file = new File([blob], fileName, { type: 'image/png' });

        // Mobile Native Web Share API
        if (isMobile && navigator.canShare && navigator.canShare({ files: [file] })) {
            try {
                await navigator.share({
                    title: `Receipt - ${currentCustomerName}`,
                    text: `Receipt Statement for ${currentCustomerName}`,
                    files: [file]
                });
                return;
            } catch (err) {
                if (err.name === 'AbortError') return;
            }
        }

        // Desktop Clipboard Copy
        try {
            const item = new ClipboardItem({ 'image/png': blob });
            await navigator.clipboard.write([item]);
        } catch (clipErr) {
            console.warn('Clipboard write error:', clipErr);
            downloadReceiptPhoto();
        }

        alert(`🚀 Opening WhatsApp for ${currentCustomerName}!\n\n📋 Photo is copied to your clipboard — press Ctrl + V in the chat box to send!`);
        window.open(targetUrl, '_blank');
    });
}

function copyReceiptPhotoOnly() {
    const canvas = document.getElementById('receiptCanvas');
    if (!canvas) return;

    try {
        canvas.toBlob(async (blob) => {
            if (!blob) return;
            const item = new ClipboardItem({ 'image/png': blob });
            await navigator.clipboard.write([item]);
            alert('📋 Receipt photo copied to clipboard!');
        });
    } catch (err) {
        downloadReceiptPhoto();
    }
}

function downloadReceiptPhoto() {
    const canvas = document.getElementById('receiptCanvas');
    if (!canvas) return;

    const dateStr = new Date().toISOString().split('T')[0];
    const fileName = `Receipt_${(currentCustomerName || 'Customer').replace(/\s+/g, '_')}_${dateStr}.png`;
    
    const link = document.createElement('a');
    link.download = fileName;
    link.href = canvas.toDataURL('image/png');
    link.click();
}



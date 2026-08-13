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
    return customer.shareRate != null && customer.shareRate > 0 && customer.shareRate < 100.0;
}

document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today
    document.getElementById('txDate').value = new Date().toISOString().split('T')[0];

    // Initial Loaders
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
                document.getElementById('farakAmount').value = customer.farak || 0;

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
    });

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
        loadCustomers(e.target.value, getSelectedMarket());
    });

    document.querySelectorAll('.market-chips .chip').forEach(chip => {
        chip.addEventListener('click', (e) => {
            document.querySelectorAll('.market-chips .chip').forEach(c => c.classList.remove('active'));
            e.target.classList.add('active');
            const market = e.target.getAttribute('data-market');
            loadCustomers(document.getElementById('searchInput').value, market);
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
        const data = await res.json();
        if (data.authenticated) {
            document.getElementById('loggedInUser').textContent = data.username + " (Admin)";
        }
    } catch (err) {
        console.error('Auth error:', err);
    }
}

// 2. Load Real-Time Dashboard Metrics
async function loadDashboardMetrics() {
    try {
        const res = await fetch('/api/dashboard/metrics');
        const data = await res.json();

        document.getElementById('kpiTotalSell').textContent = formatCurrency(data.todayTotalSell);
        document.getElementById('kpiTotalPayment').textContent = formatCurrency(data.todayTotalPayment);
        document.getElementById('kpiTotalCommission').textContent = formatCurrency(data.todayTotalCommission);
        document.getElementById('kpiCustomerBalance').textContent = formatCurrency(data.totalCustomerBalance);
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
        customersData = await res.json();

        populateCustomerDropdown(customersData);
        renderCustomerTable(customersData);
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
        opt.textContent = `${c.name} (${c.city})`;
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
        const balColor = c.previousBalance > 0 ? '#fca5a5' : '#34d399';
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
            <td style="font-weight: 800; color: ${balColor};">₹${c.previousBalance.toFixed(2)}</td>
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
}

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
    const is30ProfitOnly = customer.share30ProfitOnly === true || customer.share30ProfitOnly === 'true' || customer.shareRate === 30;
    if (document.getElementById('custShare4060')) {
        document.getElementById('custShare4060').checked = isShareEnabled;
    }
    if (document.getElementById('custShare30ProfitOnly')) {
        document.getElementById('custShare30ProfitOnly').checked = is30ProfitOnly;
    }
    if (document.getElementById('custShareRateVal')) {
        document.getElementById('custShareRateVal').value = isShareEnabled ? customer.shareRate : 40.0;
    }
    if (document.getElementById('custShareRateContainer')) {
        document.getElementById('custShareRateContainer').style.display = isShareEnabled ? 'block' : 'none';
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

    const is30ProfitOnly = customer && (customer.share30ProfitOnly === true || customer.share30ProfitOnly === 'true' || shareRate === 30.0);

    if (isShareEnabled) {
        const afterFarak = afterPay - farakVal;
        if (is30ProfitOnly) {
            shareAmount = afterFarak > 0 ? (afterFarak * shareRate) / 100.0 : 0;
        } else {
            shareAmount = (afterFarak * shareRate) / 100.0;
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

    if (document.getElementById('previewShareRate')) document.getElementById('previewShareRate').textContent = shareRate.toFixed(0);
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

// 5. Handle Trade Form Actions (Save & Download vs Share to WhatsApp)
window.processTradeAction = async function(actionType) {
    const customerId = document.getElementById('selectCustomer') ? document.getElementById('selectCustomer').value : '';
    if (!customerId) {
        alert('⚠️ Please select a customer / trader first!');
        return;
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
            await loadDashboardMetrics();
            
            if (actionType === 'save') {
                // Generate receipt statement data & canvas image
                await triggerWhatsApp(customerId, false);
                // Download receipt photo image onto user's device
                downloadReceiptPhoto();
                alert('✅ Trade Saved to Ledger & Receipt Downloaded Successfully!');
            } else if (actionType === 'share') {
                // Generate receipt statement & launch WhatsApp Web/App directly
                await triggerWhatsApp(customerId, true);
            }

            // Reset market input fields after save/share
            document.querySelectorAll('#dynamicMarketInputs input').forEach(inp => inp.value = '0');
            if (document.getElementById('pagarAmount')) document.getElementById('pagarAmount').value = '0';
            if (document.getElementById('farakAmount')) document.getElementById('farakAmount').value = '0';
            calculateMathPreview();
        } else {
            alert('❌ Failed to process trade transaction.');
        }
    } catch (err) {
        console.error('Error submitting trade:', err);
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

    const payload = {
        name: nameVal,
        mobileNumber: '',
        city: '',
        receiptStyle: 'TYPE_1',
        shareRate: shareRateVal,
        share30ProfitOnly: isShare30ProfitOnlyChecked || shareRateVal === 30.0,
        commissionPercentage: commVal,
        commissionEnabled: isCommEnabledChecked,
        pagar: pagarVal,
        pagarEnabled: isPagarEnabledChecked,
        yene: 0,
        dene: 0,
        farak: 0,
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

        let yeneVal = document.getElementById('tradeYene') ? document.getElementById('tradeYene').value : '';
        let deneVal = document.getElementById('tradeDene') ? document.getElementById('tradeDene').value : '';
        let farakVal = document.getElementById('farakAmount') ? document.getElementById('farakAmount').value : '';
        let pagarVal = document.getElementById('pagarAmount') ? document.getElementById('pagarAmount').value : '';

        const isLiveActiveForm = selectedCustId && selectedCustId == customerId && (sellPoNum > 0 || sellPcNum > 0 || payPoNum > 0 || payPcNum > 0);

        if (selectedCustId && selectedCustId != customerId && customer) {
            activeStyle = customer.receiptStyle || 'TYPE_1';
            yeneVal = customer.yene != null ? customer.yene : '';
            deneVal = customer.dene != null ? customer.dene : '';
            farakVal = customer.farak != null ? customer.farak : '';
            pagarVal = customer.pagar != null ? customer.pagar : '';
        }

        const params = new URLSearchParams();
        if (activeStyle) params.append('style', activeStyle);

        if (isLiveActiveForm) {
            params.append('sellPo', sellPoNum);
            params.append('sellPc', sellPcNum);
            params.append('payPo', payPoNum);
            params.append('payPc', payPcNum);
        }

        if (yeneVal !== '' && yeneVal !== null) params.append('yene', yeneVal);
        if (deneVal !== '' && deneVal !== null) params.append('dene', deneVal);
        if (farakVal !== '' && farakVal !== null) params.append('farak', farakVal);
        if (pagarVal !== '' && pagarVal !== null) params.append('pagar', pagarVal);

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
        const editContainerEl = document.getElementById('waEditContainer');
        if (editContainerEl) editContainerEl.style.display = 'none';

        const encodedMsg = encodeURIComponent(data.formattedMessage || '');
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const waLink = cleanMobile 
            ? (isMobile ? `https://api.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}` : `https://web.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}`)
            : (isMobile ? `https://api.whatsapp.com/send` : `https://web.whatsapp.com`);

        currentWaLink = waLink;

        // Render HD Receipt Canvas Photo
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

    const parsedRows = [];
    let pendingMissPrefix = false;

    rawLines.forEach(line => {
        const trimmed = line.trim();
        let cleanText = line.replace(/\*/g, '').trim();

        if (!cleanText) return;

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

        if (pendingMissPrefix) {
            label = 'MISS ' + label;
            pendingMissPrefix = false;
        }

        let isHighlight = label.toUpperCase().startsWith('TOTAL') || label.toUpperCase().startsWith('REMAINING') || label.toUpperCase().startsWith('NET');

        let sellVal = '';
        let payVal = '';

        if (parts.length >= 3) {
            sellVal = parts[1].trim();
            payVal = parts[2].trim();
        } else if (parts.length === 2) {
            const valStr = parts[1].trim();
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

    // 4. Column Headers (PARTICULARS   SELL (₹)   PAYMENT (₹))
    let currentY = headerHeight + (25 * scale);
    const labelX = paddingX + (8 * scale);
    const sellColX = 370 * scale; // Right-aligned SELL column
    const payColX = 510 * scale;  // Right-aligned PAYMENT column

    ctx.font = `900 ${16 * scale}px ${fontStack}`;
    ctx.fillStyle = '#334155';
    ctx.textBaseline = 'middle';

    ctx.textAlign = 'left';
    ctx.fillText('PARTICULARS', labelX, currentY);

    ctx.textAlign = 'right';
    ctx.fillText('SELL (₹)', sellColX, currentY);
    ctx.fillText('PAYMENT (₹)', payColX, currentY);

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
            // Red dot for Magil Yene
            ctx.fillStyle = '#e11d48';
            ctx.beginPath();
            ctx.arc(labelX + (8 * scale), currentY, 7 * scale, 0, Math.PI * 2);
            ctx.fill();

            ctx.textAlign = 'left';
            ctx.fillText(row.label.replace(':-', '').trim() + ' :-', labelX + (22 * scale), currentY);

            ctx.textAlign = 'right';
            ctx.fillText(row.sellVal, sellColX, currentY);
        } else {
            ctx.fillStyle = isHighlight ? '#0284c7' : '#0f172a';
            ctx.textAlign = 'left';
            ctx.fillText(row.label, labelX, currentY);

            ctx.fillStyle = isHighlight ? '#0284c7' : '#0f172a';
            ctx.textAlign = 'right';
            if (row.sellVal) ctx.fillText(row.sellVal, sellColX, currentY);
            if (row.payVal) ctx.fillText(row.payVal, payColX, currentY);
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

    const isHidden = editContainer.style.display === 'none' || !editContainer.style.display;
    if (isHidden) {
        editContainer.style.display = 'block';
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

function updateCalculatedValuesInText(text) {
    if (!text) return text;
    const lines = text.split('\n');

    let sellPo = null, payPo = null;
    let sellPc = null, payPc = null;
    let commPct = 10;
    let commVal = null;
    let pagarVal = 0;
    let magilVal = 0;
    let hasCommLine = false;

    lines.forEach(l => {
        const clean = l.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();
        if (upper.startsWith('PO :-') || upper.startsWith('PO:')) {
            const parts = clean.split(/:-|:/);
            if (parts.length >= 3) {
                sellPo = parseFloat(parts[1].replace(/,/g, '')) || 0;
                payPo = parseFloat(parts[2].replace(/,/g, '')) || 0;
            } else if (parts.length === 2) {
                sellPo = parseFloat(parts[1].replace(/,/g, '')) || 0;
            }
        } else if (upper.startsWith('PC :-') || upper.startsWith('PC:')) {
            const parts = clean.split(/:-|:/);
            if (parts.length >= 3) {
                sellPc = parseFloat(parts[1].replace(/,/g, '')) || 0;
                payPc = parseFloat(parts[2].replace(/,/g, '')) || 0;
            } else if (parts.length === 2) {
                sellPc = parseFloat(parts[1].replace(/,/g, '')) || 0;
            }
        } else if (upper.includes('COM (') || upper.includes('COMMISSION')) {
            hasCommLine = true;
            const commMatch = clean.match(/COM\s*\((\d+(\.\d+)?)%\)/i);
            if (commMatch && commMatch[1]) commPct = parseFloat(commMatch[1]);
        } else if (upper.includes('MAGIL')) {
            const parts = clean.split(/:-|:/);
            if (parts.length >= 2) {
                magilVal = parseFloat(parts[1].replace(/[^0-9.]/g, '')) || 0;
            }
        } else if (upper.startsWith('PAGAR')) {
            const parts = clean.split(/:-|:/);
            if (parts.length >= 2) {
                pagarVal = parseFloat(parts[1].replace(/[^0-9.]/g, '')) || 0;
            }
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
    const remaining = totalAfterComm - totalPay - pagarVal;
    const netBal = remaining + magilVal;
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

function onQuickFieldEdited() {
    const sellPo = parseFloat(document.getElementById('editPoSell').value) || 0;
    const payPo = parseFloat(document.getElementById('editPoPay').value) || 0;
    const sellPc = parseFloat(document.getElementById('editPcSell').value) || 0;
    const payPc = parseFloat(document.getElementById('editPcPay').value) || 0;
    const magil = parseFloat(document.getElementById('editMagil').value) || 0;
    const pagar = parseFloat(document.getElementById('editPagar').value) || 0;

    let text = document.getElementById('waReceiptEditText').value || currentFormattedMessage || '';
    const lines = text.split('\n');

    const updatedLines = lines.map(line => {
        const clean = line.replace(/\*/g, '').trim();
        const upper = clean.toUpperCase();
        if (upper.startsWith('PO :-') || upper.startsWith('PO:')) {
            return line.replace(/:-.*/, `:- ${sellPo.toLocaleString('en-IN')} : ${payPo.toLocaleString('en-IN')}`);
        } else if (upper.startsWith('PC :-') || upper.startsWith('PC:')) {
            return line.replace(/:-.*/, `:- ${sellPc.toLocaleString('en-IN')} : ${payPc.toLocaleString('en-IN')}`);
        } else if (upper.includes('MAGIL')) {
            return line.replace(/:-.*/, `:- ${magil.toLocaleString('en-IN')}`);
        } else if (upper.startsWith('PAGAR')) {
            return line.replace(/:-.*/, `:- ${pagar.toLocaleString('en-IN')}`);
        }
        return line;
    });

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



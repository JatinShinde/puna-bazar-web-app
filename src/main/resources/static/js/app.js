// Pune Bazar Automatic WhatsApp Calculator & Ledger System Client JS

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
                if (style === 'FARAK_SHARE' || style === 'TYPE1') style = 'TYPE_1';
                else if (style === 'SIMPLE' || style === 'TYPE2') style = 'TYPE_2';
                else if (style === 'STANDARD' || style === 'TYPE3') style = 'TYPE_3';
                else if (style === 'TYPE4') style = 'TYPE_4';
                else if (style === 'SHARE_PERCENT' || style === 'TYPE5') style = 'TYPE_5';

                const tradeStyleEl = document.getElementById('tradeReceiptStyle');
                if (tradeStyleEl) {
                    tradeStyleEl.value = style;
                }

                document.getElementById('commPercent').value = customer.commissionRate != null ? customer.commissionRate : 10.0;
                document.getElementById('shareRatePercent').value = customer.shareRate != null ? customer.shareRate : 40.0;
                document.getElementById('tradeYene').value = customer.yene != null ? customer.yene : (customer.previousBalance > 0 ? customer.previousBalance : 0);
                document.getElementById('tradeDene').value = customer.dene != null ? customer.dene : (customer.previousBalance < 0 ? Math.abs(customer.previousBalance) : 0);
                document.getElementById('pagarAmount').value = customer.pagar || 0;
                document.getElementById('farakAmount').value = customer.farak || 0;

                updateInputFieldsVisibility(style);
                renderCustomerMarketInputs(customer.marketCodes || 'PO,PC');
            }
        } else {
            document.getElementById('tradeReceiptStyle').value = 'TYPE_3';
            document.getElementById('commPercent').value = '10.00';
            document.getElementById('shareRatePercent').value = '40.00';
            document.getElementById('tradeYene').value = '0';
            document.getElementById('tradeDene').value = '0';
            document.getElementById('pagarAmount').value = '0';
            document.getElementById('farakAmount').value = '0';

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

    const style = (receiptStyle || 'TYPE_3').toUpperCase();

    if (style === 'TYPE_2' || style === 'SIMPLE') {
        if (commGroup) commGroup.style.display = 'none';
        if (shareGroup) shareGroup.style.display = 'none';
        if (farakGroup) farakGroup.style.display = 'none';
        if (pagarGroup) pagarGroup.style.display = 'none';
    } else if (style === 'TYPE_5' || style === 'SHARE_PERCENT') {
        if (commGroup) commGroup.style.display = 'block';
        if (shareGroup) shareGroup.style.display = 'block';
        if (farakGroup) farakGroup.style.display = 'none';
        if (pagarGroup) pagarGroup.style.display = 'none';
    } else if (style === 'TYPE_1' || style === 'FARAK_SHARE') {
        if (commGroup) commGroup.style.display = 'block';
        if (shareGroup) shareGroup.style.display = 'block';
        if (farakGroup) farakGroup.style.display = 'block';
        if (pagarGroup) pagarGroup.style.display = 'none';
    } else if (style === 'TYPE_4') {
        if (commGroup) commGroup.style.display = 'none';
        if (shareGroup) shareGroup.style.display = 'none';
        if (farakGroup) farakGroup.style.display = 'none';
        if (pagarGroup) pagarGroup.style.display = 'block';
    } else {
        // TYPE_3 or STANDARD
        if (commGroup) commGroup.style.display = 'block';
        if (shareGroup) shareGroup.style.display = 'none';
        if (farakGroup) farakGroup.style.display = 'none';
        if (pagarGroup) pagarGroup.style.display = 'block';
    }
}

    // Form Submissions
    document.getElementById('tradeForm').addEventListener('submit', handleTradeSubmit);
    document.getElementById('newCustomerForm').addEventListener('submit', handleNewCustomerSubmit);

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

        tr.innerHTML = `
            <td>
                <strong style="color: #ffffff;">${escapeHtml(c.name)}</strong>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(c.marketZone || c.city)} • (${escapeHtml(c.marketCodes || 'PO,PC')})</div>
            </td>
            <td><span class="chip" style="margin: 0;">${escapeHtml(c.city)}</span></td>
            <td>📱 ${escapeHtml(c.mobileNumber)}</td>
            <td style="font-weight: 800; color: ${balColor};">₹${c.previousBalance.toFixed(2)}</td>
            <td>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <button onclick="triggerWhatsApp(${c.id})" class="btn-whatsapp">
                        <span>📲 WhatsApp</span>
                    </button>
                    <button onclick="viewHistory(${c.id}, '${escapeHtml(c.name)}')" class="chip" style="margin:0;">
                        📜 History
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

// 4. Live Calculation Engine Math Logic
function calculateMathPreview() {
    const custId = document.getElementById('selectCustomer').value;
    const customer = custId ? customersData.find(c => c.id == custId) : null;
    const styleEl = document.getElementById('tradeReceiptStyle');
    const receiptStyle = styleEl && styleEl.value ? styleEl.value : (customer && customer.receiptStyle ? customer.receiptStyle : 'TYPE_3');

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

    const commCut = (totalSell * commRate) / 100.0;
    const totalAfterComm = totalSell - commCut;
    const afterPay = totalAfterComm - totalPayment;

    let netBalance = 0;
    let shareAmount = 0;

    const rowComi = document.getElementById('previewComiRow');
    const rowAfterComm = document.getElementById('previewAfterCommRow');
    const rowPayment = document.getElementById('previewPaymentRow');
    const rowFarak = document.getElementById('previewFarakRow');
    const rowShare = document.getElementById('previewShareRow');
    const rowPagar = document.getElementById('previewPagarRow');

    if (receiptStyle === 'TYPE_2' || receiptStyle === 'SIMPLE') {
        if (rowComi) rowComi.style.display = 'none';
        if (rowAfterComm) rowAfterComm.style.display = 'none';
        if (rowFarak) rowFarak.style.display = 'none';
        if (rowShare) rowShare.style.display = 'none';
        if (rowPagar) rowPagar.style.display = 'none';

        netBalance = (totalSell - totalPayment) + openingNet;
    } else if (receiptStyle === 'TYPE_5' || receiptStyle === 'SHARE_PERCENT') {
        if (rowComi) rowComi.style.display = 'flex';
        if (rowAfterComm) rowAfterComm.style.display = 'flex';
        if (rowFarak) rowFarak.style.display = 'none';
        if (rowShare) rowShare.style.display = 'flex';
        if (rowPagar) rowPagar.style.display = 'none';

        shareAmount = (afterPay * shareRate) / 100.0;
        netBalance = shareAmount + openingNet;
    } else if (receiptStyle === 'TYPE_1' || receiptStyle === 'FARAK_SHARE') {
        if (rowComi) rowComi.style.display = 'flex';
        if (rowAfterComm) rowAfterComm.style.display = 'flex';
        if (rowFarak) rowFarak.style.display = 'flex';
        if (rowShare) rowShare.style.display = 'flex';
        if (rowPagar) rowPagar.style.display = 'none';

        const afterFarak = afterPay - farakVal;
        shareAmount = (afterFarak * shareRate) / 100.0;
        netBalance = shareAmount + openingNet;
    } else if (receiptStyle === 'TYPE_4') {
        if (rowComi) rowComi.style.display = 'none';
        if (rowAfterComm) rowAfterComm.style.display = 'none';
        if (rowFarak) rowFarak.style.display = 'none';
        if (rowShare) rowShare.style.display = 'none';
        if (rowPagar) rowPagar.style.display = 'flex';

        const afterPayDirect = totalSell - totalPayment;
        netBalance = (afterPayDirect - pagarVal) + openingNet;
    } else {
        // TYPE_3 or STANDARD
        if (rowComi) rowComi.style.display = 'flex';
        if (rowAfterComm) rowAfterComm.style.display = 'flex';
        if (rowFarak) rowFarak.style.display = 'none';
        if (rowShare) rowShare.style.display = 'none';
        if (rowPagar) rowPagar.style.display = 'flex';

        netBalance = (afterPay - pagarVal) + openingNet;
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
    if (document.getElementById('previewPagarAmount')) document.getElementById('previewPagarAmount').textContent = `-` + formatCurrency(pagarVal);
    
    const netText = netBalance >= 0 ? formatCurrency(netBalance) + ' (येणे)' : '-' + formatCurrency(Math.abs(netBalance)) + ' (देणे)';
    const netColor = netBalance >= 0 ? '#fbbf24' : '#f87171';
    const netEl = document.getElementById('previewNetBalance');
    if (netEl) {
        netEl.textContent = netText;
        netEl.style.color = netColor;
    }
}

// 5. Handle Trade Form Submission
async function handleTradeSubmit(e) {
    e.preventDefault();

    const customerId = document.getElementById('selectCustomer').value;
    if (!customerId) {
        alert('Please select a customer first!');
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
        receiptStyle: document.getElementById('tradeReceiptStyle').value || 'TYPE_3',
        shareRate: parseFloat(document.getElementById('shareRatePercent').value) || 40.0,
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
            
            // Open Receipt Photo WhatsApp modal immediately
            triggerWhatsApp(customerId);

            // Reset inputs after modal opens
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
}

// 6. Add New Customer
async function handleNewCustomerSubmit(e) {
    e.preventDefault();

    const yeneEl = document.getElementById('custYene');
    const deneEl = document.getElementById('custDene');
    const shareEl = document.getElementById('custShareRate');
    const farakEl = document.getElementById('custFarak');

    const yeneVal = yeneEl ? (parseFloat(yeneEl.value) || 0) : 0;
    const deneVal = deneEl ? (parseFloat(deneEl.value) || 0) : 0;

    const selectedStyle = document.getElementById('custReceiptStyle').value || 'TYPE_1';

    const pagarEl = document.getElementById('custPagar');
    const pagarVal = pagarEl ? (parseFloat(pagarEl.value) || 0) : 0;

    const payload = {
        name: document.getElementById('custName').value,
        mobileNumber: document.getElementById('custMobile').value,
        city: document.getElementById('custCity').value,
        receiptStyle: selectedStyle,
        shareRate: shareEl ? (parseFloat(shareEl.value) || 40.0) : 40.0,
        commissionPercentage: parseFloat(document.getElementById('custCommission').value) || 10.0,
        pagar: pagarVal,
        yene: yeneVal,
        dene: deneVal,
        farak: farakEl ? (parseFloat(farakEl.value) || 0) : 0,
        marketCodes: 'PO,PC'
    };

    try {
        const res = await fetch('/api/customers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const savedCustomer = await res.json();
            alert('✅ Customer Profile Saved Successfully!');
            closeModal('customerModal');
            document.getElementById('newCustomerForm').reset();
            if (document.getElementById('custCommission')) document.getElementById('custCommission').value = '10.00';
            if (document.getElementById('custPagar')) document.getElementById('custPagar').value = '0.00';

            await loadCustomers('', getSelectedMarket());
            await loadDashboardMetrics();

            const selectEl = document.getElementById('selectCustomer');
            if (selectEl && savedCustomer && savedCustomer.id) {
                selectEl.value = savedCustomer.id;
                selectEl.dispatchEvent(new Event('change'));
            }
        } else {
            alert('❌ Failed to save customer profile.');
        }
    } catch (err) {
        console.error('Error creating customer:', err);
    }
}

// 7. One-Click WhatsApp Statement Generator
async function triggerWhatsApp(customerId) {
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
            document.getElementById('waCustomerName').textContent = `${data.customerName} (${data.city})`;
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

        const encodedMsg = encodeURIComponent(data.formattedMessage || '');
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const waLink = isMobile
            ? `https://api.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}`
            : `https://web.whatsapp.com/send?phone=${cleanMobile}&text=${encodedMsg}`;

        currentWaLink = waLink;

        // Render HD Receipt Canvas Photo
        renderReceiptImageCanvas(data.formattedMessage, data.customerName);

        openModal('whatsappModal');
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
    
    // Scale for high resolution (2x DPI)
    const scale = 2;
    const paddingX = 40 * scale;
    const paddingY = 40 * scale;
    const lineHeight = 38 * scale;
    const fontSize = 21 * scale;
    
    const canvasWidth = 640 * scale;
    const canvasHeight = paddingY * 2 + Math.max(1, rawLines.length) * lineHeight;

    canvas.width = canvasWidth;
    canvas.height = canvasHeight;

    // Fixed Column Coordinates for Pixel-Perfect Vertical Alignment
    const labelX = paddingX;
    const sellColX = 420 * scale; // Right-aligned SELL column
    const payColX = 580 * scale;  // Right-aligned PAYMENT column

    // Draw background card (Dark Teal gradient with rounded corners)
    const cornerRadius = 24 * scale;
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);

    const grad = ctx.createLinearGradient(0, 0, 0, canvasHeight);
    grad.addColorStop(0, '#044e45');
    grad.addColorStop(1, '#023831');
    
    ctx.fillStyle = grad;
    drawRoundedRect(ctx, 0, 0, canvasWidth, canvasHeight, cornerRadius);
    ctx.fill();

    // Glowing Neon Green Border
    ctx.strokeStyle = '#20e39a';
    ctx.lineWidth = 4 * scale;
    drawRoundedRect(ctx, 2 * scale, 2 * scale, canvasWidth - 4 * scale, canvasHeight - 4 * scale, cornerRadius);
    ctx.stroke();

    // Set Font
    ctx.font = `bold ${fontSize}px "Consolas", "Fira Code", "Courier New", monospace`;
    ctx.textBaseline = 'middle';

    let currentY = paddingY + lineHeight / 2;
    let pendingMissPrefix = false;

    rawLines.forEach((line) => {
        const trimmed = line.trim();
        let cleanText = line.replace(/\*/g, '').trim();

        // 1. Divider line (---)
        if (trimmed.startsWith('---') || trimmed.includes('-----')) {
            ctx.beginPath();
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.35)';
            ctx.lineWidth = 2 * scale;
            ctx.setLineDash([4 * scale, 4 * scale]);
            ctx.moveTo(paddingX, currentY);
            ctx.lineTo(canvasWidth - paddingX, currentY);
            ctx.stroke();
            ctx.setLineDash([]); // Reset dash
            currentY += lineHeight;
        } 
        // 2. Handle MISS header line (Combine with next PAYMENT row)
        else if (cleanText === 'MISS') {
            pendingMissPrefix = true;
            return; // Skip drawing standalone MISS line
        }
        // 3. Table Header line (SELL  PAYMENT)
        else if (cleanText.includes('SELL') && cleanText.includes('PAYMENT')) {
            ctx.fillStyle = '#ffffff';
            ctx.textAlign = 'right';
            ctx.fillText('SELL', sellColX, currentY);
            ctx.fillText('PAYMENT', payColX, currentY);
            currentY += lineHeight;
        }
        // 4. Date line
        else if (cleanText.startsWith('Date:')) {
            ctx.fillStyle = '#ffffff';
            ctx.textAlign = 'left';
            ctx.fillText(cleanText, labelX, currentY);
            currentY += lineHeight;
        }
        // 5. Centered City Header (e.g. KALYAN / PUNE / SOLAPUR)
        else if (cleanText.length > 0 && !cleanText.includes(':-') && !cleanText.includes(':') && !/\d/.test(cleanText)) {
            ctx.fillStyle = '#ffffff';
            ctx.textAlign = 'center';
            ctx.fillText(cleanText, canvasWidth / 2, currentY);
            currentY += lineHeight;
        }
        // 6. Data & Amount Rows
        else if (cleanText.length > 0) {
            let hasRedDot = line.includes('🔴');
            if (hasRedDot) {
                cleanText = cleanText.replace(/🔴/g, '').trim();
                ctx.fillStyle = '#ef4444';
                ctx.beginPath();
                ctx.arc(labelX + 8 * scale, currentY, 7 * scale, 0, Math.PI * 2);
                ctx.fill();
            }

            ctx.fillStyle = '#ffffff';

            const parts = cleanText.split(/:-|:/);
            let label = parts[0] ? parts[0].trim() : '';

            if (pendingMissPrefix) {
                label = 'MISS ' + label;
                pendingMissPrefix = false;
            }

            if (parts.length > 1 && label) {
                // Draw Label on Left
                ctx.textAlign = 'left';
                const labelXOffset = hasRedDot ? (labelX + 24 * scale) : labelX;
                ctx.fillText(label + ':-', labelXOffset, currentY);

                // Extract numbers
                const valStr = parts.slice(1).join(':-').trim();
                const numbers = valStr.match(/[\d,]+(\s*(yeṇe|dene))?/gi) || [];

                if (numbers.length >= 2) {
                    ctx.textAlign = 'right';
                    ctx.fillText(numbers[0].trim(), sellColX, currentY);
                    ctx.fillText(numbers[1].trim(), payColX, currentY);
                } else if (numbers.length === 1) {
                    ctx.textAlign = 'right';
                    ctx.fillText(numbers[0].trim(), sellColX, currentY);
                }
            } else {
                // Standalone amount or text
                const numbers = cleanText.match(/[\d,]+(\s*(yeṇe|dene))?/gi) || [];
                if (numbers.length >= 1) {
                    ctx.textAlign = 'right';
                    ctx.fillText(numbers[0].trim(), sellColX, currentY);
                } else {
                    ctx.textAlign = 'left';
                    ctx.fillText(cleanText, labelX, currentY);
                }
            }
            currentY += lineHeight;
        }
    });
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

// Direct 1-Click WhatsApp Photo Share to Customer Mobile Number
async function shareReceiptPhotoToWhatsApp(targetMode) {
    const canvas = document.getElementById('receiptCanvas');
    if (!canvas) return;

    // No text parameter so ONLY photo is shared/pasted
    let targetUrl = `https://web.whatsapp.com/send?phone=${currentCleanMobile}`;

    if (targetMode === 'api' || /Android|iPhone|iPad/i.test(navigator.userAgent)) {
        targetUrl = `https://api.whatsapp.com/send?phone=${currentCleanMobile}`;
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



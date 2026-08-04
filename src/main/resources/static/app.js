(function () {
    const alertsBody = document.getElementById("alerts-body");
    const historyBody = document.getElementById("alert-history-body");
    const transactionsBody = document.getElementById("transactions-body");
    const rulesGrid = document.getElementById("rules-grid");
    const detailPanel = document.getElementById("alert-detail");
    const message = document.getElementById("dashboard-message");
    const filterIds = ["transaction-search", "min-amount", "max-amount", "from-time", "to-time"];

    const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);

    async function getJson(url, options) {
        const response = await fetch(url, options);
        if (!response.ok) throw new Error("Request failed");
        return response.json();
    }

    function transactionQuery() {
        const parameters = new URLSearchParams({size: "100"});
        const values = Object.fromEntries(filterIds.map(id => [id, document.getElementById(id).value.trim()]));
        if (values["transaction-search"]) parameters.set("search", values["transaction-search"]);
        if (values["min-amount"]) parameters.set("minAmount", values["min-amount"]);
        if (values["max-amount"]) parameters.set("maxAmount", values["max-amount"]);
        if (values["from-time"]) parameters.set("from", new Date(values["from-time"]).toISOString());
        if (values["to-time"]) parameters.set("to", new Date(values["to-time"]).toISOString());
        return parameters.toString();
    }

    function actionButtons(alert) {
        const buttons = [`<button class="secondary" data-view-alert="${alert.id}">View</button>`];
        if (alert.status === "OPEN") {
            buttons.push(`<button data-alert="${alert.id}" data-status="ACKNOWLEDGED">Acknowledge</button>`);
        }
        if (alert.status === "ACKNOWLEDGED") {
            buttons.push(`<button data-alert="${alert.id}" data-status="INVESTIGATING">Investigate</button>`);
            buttons.push(`<button class="secondary" data-alert="${alert.id}" data-status="DISMISSED">Dismiss</button>`);
        }
        if (alert.status === "INVESTIGATING") {
            buttons.push(`<button data-alert="${alert.id}" data-status="CLOSED">Close</button>`);
            buttons.push(`<button class="secondary" data-alert="${alert.id}" data-status="DISMISSED">Dismiss</button>`);
        }
        return `<div class="table-actions">${buttons.join("")}</div>`;
    }

    function alertRow(alert, history) {
        const finalTime = history ? (alert.closedAt ? new Date(alert.closedAt).toLocaleString() : "-")
                : new Date(alert.createdAt).toLocaleString();
        return `<tr>
            <td><strong>${escapeHtml(alert.ruleName)}</strong><br><small>${escapeHtml(alert.message)}</small></td>
            <td><span class="badge ${alert.severity.toLowerCase()}">${alert.severity}</span></td>
            <td><span class="badge">${alert.status}</span></td><td>${finalTime}</td>
            <td>${history ? `<button class="secondary" data-view-alert="${alert.id}">View</button>` : actionButtons(alert)}</td>
        </tr>`;
    }

    async function load() {
        try {
            const [summary, alertPage, transactionPage, rules] = await Promise.all([
                getJson("/api/dashboard/summary"), getJson("/api/alerts?size=100"),
                getJson(`/api/transactions?${transactionQuery()}`), getJson("/api/rules")
            ]);
            const alerts = alertPage.items;
            const transactions = transactionPage.items;
            const active = alerts.filter(alert => !["CLOSED", "DISMISSED"].includes(alert.status));
            const history = alerts.filter(alert => ["CLOSED", "DISMISSED"].includes(alert.status));

            document.getElementById("active-alert-count").textContent = summary.activeAlerts;
            document.getElementById("transaction-count").textContent = summary.transactions;
            document.getElementById("alert-count").textContent = summary.alerts;
            document.getElementById("transaction-volume").textContent = `$${Number(summary.transactionVolume).toFixed(2)}`;

            alertsBody.innerHTML = active.length ? active.map(alert => alertRow(alert, false)).join("")
                    : '<tr><td class="empty" colspan="5">No active alerts.</td></tr>';
            historyBody.innerHTML = history.length ? history.map(alert => alertRow(alert, true)).join("")
                    : '<tr><td class="empty" colspan="5">No completed alerts.</td></tr>';
            transactionsBody.innerHTML = transactions.length ? transactions.map(item => `<tr>
                <td>${escapeHtml(item.accountId)}</td><td>${escapeHtml(item.payeeId)}</td>
                <td>${escapeHtml(item.currency)} ${Number(item.amount).toFixed(2)}</td>
                <td>${new Date(item.transactionTime).toLocaleString()}</td>
            </tr>`).join("") : '<tr><td class="empty" colspan="4">No transactions found.</td></tr>';
            rulesGrid.innerHTML = rules.map(rule => `<article class="rule-card">
                <span class="badge ${rule.severity.toLowerCase()}">${rule.severity}</span>
                <h3>${escapeHtml(rule.name)}</h3><p>${escapeHtml(rule.setting)}</p>
            </article>`).join("");
            message.textContent = "";
        } catch (_error) {
            message.textContent = "Dashboard data could not be loaded. Please check the filters and try again.";
            message.className = "form-feedback error";
        }
    }

    async function showAlert(id) {
        const alert = await getJson(`/api/alerts/${id}`);
        const transactions = alert.triggeringTransactions.map(item =>
            `<li>${escapeHtml(item.accountId)} → ${escapeHtml(item.payeeId)}: ${item.currency} ${Number(item.amount).toFixed(2)}</li>`).join("");
        const timeline = alert.history.map(item =>
            `<li><strong>${item.newStatus}</strong><br><small>${new Date(item.changedAt).toLocaleString()}${item.note ? ` — ${escapeHtml(item.note)}` : ""}</small></li>`).join("");
        detailPanel.innerHTML = `<div class="panel-heading"><div><p class="eyebrow">INVESTIGATION</p>
            <h2>${escapeHtml(alert.ruleName)} #${alert.id}</h2></div></div>
            <p>${escapeHtml(alert.message)}</p><div class="detail-grid"><div><h3>Triggering transactions</h3>
            <ul>${transactions}</ul></div><div><h3>Status timeline</h3><ol class="timeline">${timeline}</ol></div></div>`;
        detailPanel.scrollIntoView({behavior: "smooth", block: "start"});
    }

    async function handleAlertClick(event) {
        const viewButton = event.target.closest("button[data-view-alert]");
        if (viewButton) {
            try { await showAlert(viewButton.dataset.viewAlert); }
            catch (_error) { message.textContent = "Alert details could not be loaded."; }
            return;
        }
        const button = event.target.closest("button[data-alert]");
        if (!button) return;
        const status = button.dataset.status;
        let resolutionNotes = null;
        if (["CLOSED", "DISMISSED"].includes(status)) {
            resolutionNotes = window.prompt("Enter resolution notes:");
            if (!resolutionNotes) return;
        }
        button.disabled = true;
        try {
            await getJson(`/api/alerts/${button.dataset.alert}/status`, {
                method: "PATCH", headers: {"Content-Type": "application/json"},
                body: JSON.stringify({targetStatus: status, resolutionNotes})
            });
            await load();
            await showAlert(button.dataset.alert);
        } catch (_error) {
            message.textContent = "Alert could not be updated.";
            message.className = "form-feedback error";
            button.disabled = false;
        }
    }

    async function createDemoTransactions(kind) {
        const stamp = Date.now();
        const accountId = `DEMO-${kind.toUpperCase()}-${stamp}`;
        const count = kind === "velocity" ? 6 : 1;
        const amount = kind === "high" ? 15000 : 100;
        for (let index = 0; index < count; index += 1) {
            await getJson("/api/transactions", {method: "POST", headers: {"Content-Type": "application/json"},
                body: JSON.stringify({accountId, payeeId: `PAYEE-${kind.toUpperCase()}`, amount, currency: "USD",
                    transactionTime: new Date(Date.now() + index * 1000).toISOString(), description: `${kind} demo`})});
        }
        message.textContent = `${kind} demo completed using the real API.`;
        message.className = "form-feedback success";
        await load();
    }

    alertsBody.addEventListener("click", handleAlertClick);
    historyBody.addEventListener("click", handleAlertClick);
    filterIds.forEach(id => document.getElementById(id).addEventListener("change", load));
    document.getElementById("transaction-search").addEventListener("input", load);
    document.getElementById("demo-high").addEventListener("click", () => createDemoTransactions("high"));
    document.getElementById("demo-new").addEventListener("click", () => createDemoTransactions("new payee"));
    document.getElementById("demo-velocity").addEventListener("click", () => createDemoTransactions("velocity"));
    window.addEventListener("secureflow:refresh", load);
    load();
})();

(function () {
    "use strict";

    const state = {
        alerts: [],
        transactions: [],
        rules: [],
        summary: null,
        alertFilter: "ALL",
        pendingResolution: null,
        activeChart: null,
        chartExpanded: false
    };
    const activeStatuses = ["OPEN", "ACKNOWLEDGED", "INVESTIGATING"];
    const finalStatuses = ["CLOSED", "DISMISSED"];
    const INDIA_TIME_ZONE = "Asia/Kolkata";
    const filterIds = ["transaction-search", "min-amount", "max-amount", "from-time", "to-time"];
    const chartOptionIds = ["transactions-hour", "alerts-rule", "alerts-status", "alerts-severity", "amount-buckets"];
    const transactionModal = document.getElementById("transaction-modal");
    const alertModal = document.getElementById("alert-modal");
    const resolutionModal = document.getElementById("resolution-modal");
    const message = document.getElementById("dashboard-message");

    const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);

    const formatMoney = value => new Intl.NumberFormat("en-IN", {
        style: "currency", currency: "INR", minimumFractionDigits: 2
    }).format(Number(value || 0));

    const formatDate = value => value ? new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium", timeStyle: "short", timeZone: INDIA_TIME_ZONE
    }).format(new Date(value)) + " IST" : "—";

    async function getJson(url, options) {
        const response = await fetch(url, options);
        const body = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(body?.detail || body?.title || `Request failed (${response.status})`);
        }
        return body;
    }

    function showMessage(text, kind = "") {
        message.textContent = text;
        message.className = `notice show ${kind}`.trim();
    }

    function clearMessage() {
        message.textContent = "";
        message.className = "notice";
    }

    function toast(text, kind = "success") {
        const region = document.getElementById("toast-region");
        const item = document.createElement("div");
        item.className = `toast ${kind}`;
        item.textContent = text;
        region.appendChild(item);
        window.setTimeout(() => item.remove(), 4200);
    }

    function openDialog(dialog) {
        if (!dialog.open) dialog.showModal();
    }

    function closeDialog(dialog) {
        if (dialog.open) dialog.close();
    }

    function showView(name) {
        document.querySelectorAll("[data-view-panel]").forEach(panel => panel.classList.toggle("active", panel.dataset.viewPanel === name));
        document.querySelectorAll("[data-view]").forEach(button => button.classList.toggle("active", button.dataset.view === name));
        document.getElementById("sidebar").classList.remove("open");
        document.getElementById("sidebar-scrim").classList.remove("show");
        document.getElementById("menu-button").setAttribute("aria-expanded", "false");
        history.replaceState(null, "", `#${name}`);
        document.getElementById(`view-${name}`)?.querySelector("h1")?.focus({preventScroll: true});
        window.scrollTo({top: 0, behavior: "smooth"});
        if (name === "system-health") loadHealth();
    }

    function transactionQuery() {
        const parameters = new URLSearchParams({size: "100"});
        const value = id => document.getElementById(id).value.trim();
        if (value("transaction-search")) parameters.set("search", value("transaction-search"));
        if (value("min-amount")) parameters.set("minAmount", value("min-amount"));
        if (value("max-amount")) parameters.set("maxAmount", value("max-amount"));
        if (value("from-time")) parameters.set("from", new Date(value("from-time")).toISOString());
        if (value("to-time")) parameters.set("to", new Date(value("to-time")).toISOString());
        return parameters.toString();
    }

    function badge(value, extra = "") {
        const label = String(value).replaceAll("_", " ");
        return `<span class="badge ${String(value).toLowerCase()} ${extra}"><i class="status-dot"></i>${escapeHtml(label)}</span>`;
    }

    function actionButtons(alert, includeView = true) {
        const buttons = includeView ? [`<button type="button" data-view-alert="${alert.id}">View</button>`] : [];
        if (alert.status === "OPEN") buttons.push(`<button class="action-primary" type="button" data-alert="${alert.id}" data-status="ACKNOWLEDGED">Acknowledge</button>`);
        if (alert.status === "ACKNOWLEDGED") {
            buttons.push(`<button class="action-primary" type="button" data-alert="${alert.id}" data-status="INVESTIGATING">Investigate</button>`);
            buttons.push(`<button type="button" data-alert="${alert.id}" data-status="DISMISSED">Dismiss</button>`);
        }
        if (alert.status === "INVESTIGATING") {
            buttons.push(`<button class="action-primary" type="button" data-alert="${alert.id}" data-status="CLOSED">Close</button>`);
            buttons.push(`<button type="button" data-alert="${alert.id}" data-status="DISMISSED">Dismiss</button>`);
        }
        return `<div class="table-actions">${buttons.join("")}</div>`;
    }

    function overviewAlertRow(alert) {
        return `<tr><td><strong>${escapeHtml(alert.ruleName)}</strong><br><small>#${alert.id} · ${escapeHtml(alert.accountId)}</small></td><td>${badge(alert.severity)}</td><td>${badge(alert.status)}</td><td>${formatDate(alert.createdAt)}</td><td>${actionButtons(alert)}</td></tr>`;
    }

    function alertRow(alert, history = false) {
        return `<tr><td><strong>${escapeHtml(alert.ruleName)}</strong><br><small>Alert #${alert.id}</small></td><td>${escapeHtml(alert.accountId)}</td><td>${badge(alert.severity)}</td><td>${badge(alert.status)}</td><td>${formatDate(history ? alert.closedAt : alert.createdAt)}</td><td>${history ? `<button class="text-button" type="button" data-view-alert="${alert.id}">View details →</button>` : actionButtons(alert)}</td></tr>`;
    }

    function transactionRow(item, detailed = false) {
        if (detailed) {
            return `<tr><td><strong>#${item.id}</strong></td><td>${escapeHtml(item.accountId)}</td><td>${escapeHtml(item.payeeId)}</td><td>${escapeHtml(item.description || "—")}</td><td class="amount">${formatMoney(item.amount)}</td><td>${formatDate(item.transactionTime)}</td></tr>`;
        }
        return `<tr><td><strong>${escapeHtml(item.accountId)}</strong></td><td>${escapeHtml(item.payeeId)}</td><td class="amount">${formatMoney(item.amount)}</td><td>${formatDate(item.transactionTime)}</td></tr>`;
    }

    function renderSummary(summary) {
        state.summary = summary;
        document.getElementById("active-alert-count").textContent = summary.activeAlertCount;
        document.getElementById("transaction-count").textContent = summary.transactionCountToday;
        document.getElementById("alert-count").textContent = summary.alertsToday;
        document.getElementById("transaction-volume").textContent = formatMoney(summary.transactionVolumeToday);
        document.getElementById("nav-alert-count").textContent = summary.activeAlertCount;
    }

    function renderAlerts() {
        const active = state.alerts.filter(alert => activeStatuses.includes(alert.status));
        const completed = state.alerts.filter(alert => finalStatuses.includes(alert.status));
        const selected = state.alertFilter === "ALL" ? active : active.filter(alert => alert.severity === state.alertFilter);
        document.getElementById("overview-alerts-body").innerHTML = active.length
            ? active.slice(0, 5).map(overviewAlertRow).join("")
            : '<tr><td class="empty" colspan="5">No active alerts. The queue is clear.</td></tr>';
        document.getElementById("alerts-body").innerHTML = selected.length
            ? selected.map(alert => alertRow(alert)).join("")
            : '<tr><td class="empty" colspan="6">No alerts match this filter.</td></tr>';
        document.getElementById("alert-history-body").innerHTML = completed.length
            ? completed.map(alert => alertRow(alert, true)).join("")
            : '<tr><td class="empty" colspan="6">No completed investigations yet.</td></tr>';
        document.getElementById("active-alert-results").textContent = `${selected.length} active alert${selected.length === 1 ? "" : "s"}`;
    }

    function renderTransactions(transactionPage) {
        const items = transactionPage.items || [];
        state.transactions = items;
        document.getElementById("recent-transactions-body").innerHTML = items.length
            ? items.slice(0, 5).map(item => transactionRow(item)).join("")
            : '<tr><td class="empty" colspan="4">No transactions have been submitted.</td></tr>';
        document.getElementById("transactions-body").innerHTML = items.length
            ? items.map(item => transactionRow(item, true)).join("")
            : '<tr><td class="empty" colspan="6">No transactions match these filters.</td></tr>';
        document.getElementById("transaction-results").textContent = `${transactionPage.totalItems} transaction${transactionPage.totalItems === 1 ? "" : "s"} found`;
    }

    function ruleSetting(rule) {
        if (rule.type === "AMOUNT_THRESHOLD") {
            return `More than ${formatMoney(rule.parameters?.threshold)}`;
        }
        if (rule.type === "VELOCITY") {
            return `More than ${rule.parameters?.maximumTransactions} transactions in ${rule.parameters?.windowMinutes} minutes`;
        }
        return rule.description;
    }

    function renderRules() {
        document.getElementById("rules-preview").innerHTML = state.rules.map(rule => `<div class="preview-rule ${rule.severity.toLowerCase()}"><i></i><span><b>${escapeHtml(rule.name)}</b><small>${escapeHtml(ruleSetting(rule))}</small></span><em>ACTIVE</em></div>`).join("");
        document.getElementById("rules-grid").innerHTML = state.rules.map((rule, index) => `<article class="rule-card ${rule.severity.toLowerCase()}"><div class="rule-number">0${index + 1}</div>${badge(rule.severity)}<h2>${escapeHtml(rule.name)}</h2><p>${escapeHtml(ruleSetting(rule))}</p><div class="rule-meta"><span>Evaluation: synchronous</span><b>● ACTIVE</b></div></article>`).join("");
    }

    function countBy(items, keySelector) {
        const map = new Map();
        items.forEach(item => {
            const key = keySelector(item);
            map.set(key, (map.get(key) || 0) + 1);
        });
        return map;
    }

    function hourLabel(isoTime) {
        if (!isoTime) return "Unknown";
        return new Intl.DateTimeFormat("en-IN", {
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
            timeZone: INDIA_TIME_ZONE
        }).format(new Date(isoTime));
    }

    function cssSafeLabel(label) {
        return String(label).toLowerCase().replace(/[^a-z0-9]+/g, "-");
    }

    function makeRowsFromMap(map, valueFormatter = value => String(value)) {
        const rows = Array.from(map.entries()).map(([label, value]) => ({
            label,
            value,
            display: valueFormatter(value),
            className: cssSafeLabel(label)
        }));
        return rows.sort((left, right) => right.value - left.value);
    }

    function renderBarChart(rows, emptyMessage) {
        if (!rows.length) {
            return { html: `<p class="empty">${escapeHtml(emptyMessage)}</p>`, legend: [] };
        }
        const max = Math.max(...rows.map(row => row.value), 1);
        const html = `<ol class="chart-bars">${rows.map(row => {
            const percent = Math.max(4, Math.round((row.value / max) * 100));
            return `<li class="chart-row"><span class="chart-label">${escapeHtml(row.label)}</span><span class="chart-track"><span class="chart-fill ${escapeHtml(row.className)}" style="width:${percent}%"></span></span><span class="chart-value">${escapeHtml(row.display)}</span></li>`;
        }).join("")}</ol>`;
        return { html, legend: rows.slice(0, 5) };
    }

    function buildChartModel(chartId) {
        if (chartId === "transactions-hour") {
            const hourly = countBy(state.transactions, item => hourLabel(item.transactionTime));
            const rows = makeRowsFromMap(hourly, value => `${value} txn`);
            return {
                title: "Transactions by hour (IST)",
                subtitle: "Based on currently loaded transaction rows.",
                ...renderBarChart(rows, "No transaction data is available for charting.")
            };
        }
        if (chartId === "alerts-rule") {
            const byRule = countBy(state.alerts, alert => alert.ruleName || "Unknown rule");
            const rows = makeRowsFromMap(byRule, value => `${value} alerts`);
            return {
                title: "Alerts by rule",
                subtitle: "Compares how many alerts each rule produced.",
                ...renderBarChart(rows, "No alert data is available for charting.")
            };
        }
        if (chartId === "alerts-status") {
            const byStatus = countBy(state.alerts, alert => String(alert.status || "UNKNOWN").replaceAll("_", " "));
            const rows = makeRowsFromMap(byStatus, value => `${value} alerts`);
            return {
                title: "Alert status mix",
                subtitle: "Tracks operational workload across investigation states.",
                ...renderBarChart(rows, "No alert statuses found yet.")
            };
        }
        if (chartId === "alerts-severity") {
            const bySeverity = countBy(state.alerts, alert => alert.severity || "UNKNOWN");
            const rows = makeRowsFromMap(bySeverity, value => `${value} alerts`);
            return {
                title: "Alert severity mix",
                subtitle: "Shows current HIGH vs MEDIUM alert balance.",
                ...renderBarChart(rows, "No alert severities found yet.")
            };
        }
        if (chartId === "amount-buckets") {
            const buckets = new Map([
                ["0-1K", 0],
                ["1K-5K", 0],
                ["5K-10K", 0],
                ["10K+", 0]
            ]);
            state.transactions.forEach(item => {
                const amount = Number(item.amount || 0);
                if (amount < 1000) buckets.set("0-1K", buckets.get("0-1K") + 1);
                else if (amount < 5000) buckets.set("1K-5K", buckets.get("1K-5K") + 1);
                else if (amount < 10000) buckets.set("5K-10K", buckets.get("5K-10K") + 1);
                else buckets.set("10K+", buckets.get("10K+") + 1);
            });
            const rows = makeRowsFromMap(buckets, value => `${value} txn`);
            return {
                title: "Transaction amount buckets",
                subtitle: "Distribution by payment size from current transaction feed.",
                ...renderBarChart(rows, "No transaction amounts are available yet.")
            };
        }
        return {
            title: "Choose a chart",
            subtitle: "Select an option above to expand chart details.",
            html: '<p class="empty">No chart selected yet.</p>',
            legend: []
        };
    }

    function renderLegend(rows) {
        if (!rows.length) return "";
        return rows.map(row => `<span class="legend-chip"><i class="legend-dot ${escapeHtml(row.className)}"></i>${escapeHtml(row.label)}</span>`).join("");
    }

    function renderChartExplorer() {
        const stage = document.getElementById("chart-stage");
        const title = document.getElementById("chart-stage-title");
        const subtitle = document.getElementById("chart-stage-subtitle");
        const plot = document.getElementById("chart-plot");
        const legend = document.getElementById("chart-legend");
        const buttons = document.querySelectorAll("[data-chart-option]");

        buttons.forEach(button => {
            const isActive = state.chartExpanded && button.dataset.chartOption === state.activeChart;
            button.classList.toggle("active", isActive);
            button.setAttribute("aria-selected", String(isActive));
            button.setAttribute("aria-expanded", String(isActive));
        });

        stage.classList.toggle("expanded", state.chartExpanded);
        if (!state.chartExpanded || !state.activeChart) {
            title.textContent = "Choose a chart";
            subtitle.textContent = "Select an option above to expand chart details.";
            plot.innerHTML = '<p class="empty">No chart selected yet.</p>';
            legend.innerHTML = "";
            return;
        }

        const model = buildChartModel(state.activeChart);
        title.textContent = model.title;
        subtitle.textContent = model.subtitle;
        plot.innerHTML = model.html;
        legend.innerHTML = renderLegend(model.legend || []);
    }

    function toggleChart(chartId) {
        if (state.activeChart === chartId && state.chartExpanded) {
            state.chartExpanded = false;
        } else {
            state.activeChart = chartId;
            state.chartExpanded = true;
        }
        renderChartExplorer();
    }

    async function loadDashboard() {
        try {
            const [summary, alertPage, transactionPage, rules] = await Promise.all([
                getJson("/api/dashboard/summary"),
                getJson("/api/alerts?size=100"),
                getJson(`/api/transactions?${transactionQuery()}`),
                getJson("/api/rules")
            ]);
            state.alerts = alertPage.items || [];
            state.rules = rules || [];
            renderSummary(summary);
            renderAlerts();
            renderTransactions(transactionPage);
            renderRules();
            renderChartExplorer();
            clearMessage();
        } catch (error) {
            showMessage(`Dashboard data could not be loaded. ${error.message}`, "error");
        }
    }

    function renderAlertDetail(alert) {
        const transactions = alert.triggeringTransactions.length
            ? alert.triggeringTransactions.map(item => `<div class="trigger-item"><span><b>${escapeHtml(item.accountId)} → ${escapeHtml(item.payeeId)}</b><small>${formatDate(item.transactionTime)}</small></span><strong class="amount">${formatMoney(item.amount)}</strong></div>`).join("")
            : '<p class="empty">No linked transactions.</p>';
        const timeline = alert.history.length
            ? alert.history.map(item => `<li><b>${escapeHtml(String(item.newStatus).replaceAll("_", " "))}</b><small>${formatDate(item.changedAt)}${item.note ? `<br>${escapeHtml(item.note)}` : ""}</small></li>`).join("")
            : '<li><b>Alert created</b></li>';
        document.getElementById("alert-detail").innerHTML = `<header class="detail-header"><div><p class="eyebrow">ALERT #${alert.id}</p><h2 id="alert-detail-title">${escapeHtml(alert.ruleName)}</h2><p>Account ${escapeHtml(alert.accountId)}</p><div class="detail-status">${badge(alert.severity)} ${badge(alert.status)}</div></div><button class="icon-button" type="button" data-close-alert aria-label="Close">×</button></header><div class="detail-body"><div class="detail-summary"><div><span>Created</span><strong>${formatDate(alert.createdAt)}</strong></div><div><span>Rule type</span><strong>${escapeHtml(String(alert.ruleType).replaceAll("_", " "))}</strong></div><div><span>Linked payments</span><strong>${alert.triggeringTransactions.length}</strong></div></div><p class="detail-message">${escapeHtml(alert.message)}</p><div class="detail-columns"><section class="detail-section"><h3>Triggering transactions</h3><div class="trigger-list">${transactions}</div></section><section class="detail-section"><h3>Status timeline</h3><ol class="timeline">${timeline}</ol></section></div>${alert.resolutionNotes ? `<div class="detail-section"><h3>Resolution notes</h3><p>${escapeHtml(alert.resolutionNotes)}</p></div>` : ""}<div class="detail-actions">${actionButtons(alert, false)}<button class="secondary" type="button" data-close-alert>Close panel</button></div></div>`;
    }

    async function showAlert(id) {
        document.getElementById("alert-detail").innerHTML = '<div class="detail-loading"><span class="spinner"></span>Loading alert details…</div>';
        openDialog(alertModal);
        try {
            renderAlertDetail(await getJson(`/api/alerts/${id}`));
        } catch (error) {
            closeDialog(alertModal);
            toast(`Alert details could not be loaded. ${error.message}`, "error");
        }
    }

    async function updateAlert(id, status, resolutionNotes = null) {
        try {
            await getJson(`/api/alerts/${id}/status`, {
                method: "PATCH",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({targetStatus: status, resolutionNotes})
            });
            toast(`Alert moved to ${status.replaceAll("_", " ").toLowerCase()}.`);
            await loadDashboard();
            if (alertModal.open) await showAlert(id);
        } catch (error) {
            toast(`Alert could not be updated. ${error.message}`, "error");
        }
    }

    function requestResolution(id, status) {
        state.pendingResolution = {id, status};
        document.getElementById("resolution-title").textContent = status === "CLOSED" ? "Close alert" : "Dismiss alert";
        document.getElementById("resolution-help").textContent = "Resolution notes are required to preserve a complete audit trail.";
        document.getElementById("resolution-notes").value = "";
        document.getElementById("resolution-error").textContent = "";
        openDialog(resolutionModal);
        document.getElementById("resolution-notes").focus();
    }

    async function loadHealth() {
        const symbol = document.getElementById("health-symbol");
        try {
            const health = await getJson("/actuator/health");
            const up = health.status === "UP";
            symbol.textContent = up ? "✓" : "!";
            symbol.className = `health-symbol ${up ? "up" : "down"}`;
            document.getElementById("health-status").textContent = up ? "All systems operational" : "System needs attention";
            document.getElementById("health-description").textContent = `Actuator reported ${health.status}.`;
        } catch (error) {
            symbol.textContent = "!";
            symbol.className = "health-symbol down";
            document.getElementById("health-status").textContent = "Health check unavailable";
            document.getElementById("health-description").textContent = error.message;
        }
    }

    document.addEventListener("click", event => {
        const nav = event.target.closest("[data-view], [data-go], [data-view-link]");
        if (nav) {
            event.preventDefault();
            showView(nav.dataset.view || nav.dataset.go || nav.dataset.viewLink);
            return;
        }
        const chartOption = event.target.closest("[data-chart-option]");
        if (chartOption) {
            toggleChart(chartOption.dataset.chartOption);
            return;
        }
        if (event.target.closest("[data-open-transaction]")) {
            openDialog(transactionModal);
            document.getElementById("accountId").focus();
            return;
        }
        if (event.target.closest("[data-close-modal]")) closeDialog(transactionModal);
        if (event.target.closest("[data-close-alert]")) closeDialog(alertModal);
        if (event.target.closest("[data-close-resolution]")) closeDialog(resolutionModal);
        const viewAlert = event.target.closest("[data-view-alert]");
        if (viewAlert) showAlert(viewAlert.dataset.viewAlert);
        const transition = event.target.closest("[data-alert]");
        if (transition) {
            const {alert: id, status} = transition.dataset;
            finalStatuses.includes(status) ? requestResolution(id, status) : updateAlert(id, status);
        }
        const filter = event.target.closest("[data-alert-filter]");
        if (filter) {
            state.alertFilter = filter.dataset.alertFilter;
            document.querySelectorAll("[data-alert-filter]").forEach(item => item.classList.toggle("active", item === filter));
            renderAlerts();
        }
    });

    document.getElementById("resolution-form").addEventListener("submit", async event => {
        event.preventDefault();
        const notes = document.getElementById("resolution-notes").value.trim();
        if (!notes) {
            document.getElementById("resolution-error").textContent = "Please enter resolution notes.";
            return;
        }
        const pending = state.pendingResolution;
        closeDialog(resolutionModal);
        await updateAlert(pending.id, pending.status, notes);
    });

    let filterTimer;
    filterIds.forEach(id => document.getElementById(id).addEventListener(id === "transaction-search" ? "input" : "change", () => {
        window.clearTimeout(filterTimer);
        filterTimer = window.setTimeout(loadDashboard, 250);
    }));
    document.getElementById("clear-filters").addEventListener("click", () => {
        filterIds.forEach(id => { document.getElementById(id).value = ""; });
        loadDashboard();
    });
    document.getElementById("menu-button").addEventListener("click", event => {
        const open = document.getElementById("sidebar").classList.toggle("open");
        document.getElementById("sidebar-scrim").classList.toggle("show", open);
        event.currentTarget.setAttribute("aria-expanded", String(open));
    });
    document.getElementById("sidebar-scrim").addEventListener("click", () => {
        document.getElementById("sidebar").classList.remove("open");
        document.getElementById("sidebar-scrim").classList.remove("show");
    });
    document.querySelector(".chart-options")?.addEventListener("keydown", event => {
        if (!["ArrowRight", "ArrowLeft", "Home", "End"].includes(event.key)) return;
        const buttons = chartOptionIds
            .map(id => document.querySelector(`[data-chart-option="${id}"]`))
            .filter(Boolean);
        if (!buttons.length) return;

        event.preventDefault();
        const index = buttons.indexOf(document.activeElement);
        const current = index === -1 ? 0 : index;
        let target = current;
        if (event.key === "ArrowRight") target = (current + 1) % buttons.length;
        if (event.key === "ArrowLeft") target = (current - 1 + buttons.length) % buttons.length;
        if (event.key === "Home") target = 0;
        if (event.key === "End") target = buttons.length - 1;
        buttons[target].focus();
    });
    window.addEventListener("secureflow:refresh", async () => {
        closeDialog(transactionModal);
        toast("Transaction submitted and evaluated successfully.");
        await loadDashboard();
    });

    const liveClock = new Intl.DateTimeFormat("en-IN", {
        dateStyle: "long", timeStyle: "medium", timeZone: INDIA_TIME_ZONE
    });
    const updateLiveClock = () => {
        document.getElementById("today-label").textContent = liveClock.format(new Date()) + " · IST";
    };
    updateLiveClock();
    window.setInterval(updateLiveClock, 1000);
    const initialView = location.hash.slice(1);
    if (document.querySelector(`[data-view-panel="${initialView}"]`)) showView(initialView);
    state.activeChart = "transactions-hour";
    state.chartExpanded = false;
    renderChartExplorer();
    loadDashboard();
})();

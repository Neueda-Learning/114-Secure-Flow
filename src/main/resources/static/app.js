(function () {
    const alertsBody = document.getElementById("alerts-body");
    const transactionsBody = document.getElementById("transactions-body");
    const rulesGrid = document.getElementById("rules-grid");
    const message = document.getElementById("dashboard-message");
    const search = document.getElementById("transaction-search");

    const escapeHtml = value => String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);

    async function getJson(url, options) {
        const response = await fetch(url, options);
        if (!response.ok) throw new Error("Request failed");
        return response.json();
    }

    function nextAction(alert) {
        if (alert.status === "OPEN") return `<button data-alert="${alert.id}" data-status="ACKNOWLEDGED">Acknowledge</button>`;
        if (alert.status === "ACKNOWLEDGED") return `<button data-alert="${alert.id}" data-status="INVESTIGATING">Investigate</button>`;
        if (alert.status === "INVESTIGATING") return `<button data-alert="${alert.id}" data-status="CLOSED">Close</button>`;
        return "Complete";
    }

    async function load() {
        try {
            const term = encodeURIComponent(search.value.trim());
            const [summary, alerts, transactions, rules] = await Promise.all([
                getJson("/api/dashboard/summary"), getJson("/api/alerts"),
                getJson(`/api/transactions?search=${term}`), getJson("/api/rules")
            ]);
            document.getElementById("active-alert-count").textContent = summary.activeAlerts;
            document.getElementById("transaction-count").textContent = summary.transactions;
            document.getElementById("alert-count").textContent = summary.alerts;
            document.getElementById("transaction-volume").textContent = `$${Number(summary.transactionVolume).toFixed(2)}`;

            alertsBody.innerHTML = alerts.length ? alerts.map(alert => `<tr>
                <td><strong>${escapeHtml(alert.ruleName)}</strong><br><small>${escapeHtml(alert.message)}</small></td>
                <td><span class="badge ${alert.severity.toLowerCase()}">${alert.severity}</span></td>
                <td><span class="badge">${alert.status}</span></td>
                <td>${new Date(alert.createdAt).toLocaleString()}</td><td>${nextAction(alert)}</td>
            </tr>`).join("") : '<tr><td class="empty" colspan="5">No alerts yet.</td></tr>';

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
            message.textContent = "Dashboard data could not be loaded. Please try again.";
            message.className = "form-feedback error";
        }
    }

    alertsBody.addEventListener("click", async event => {
        const button = event.target.closest("button[data-alert]");
        if (!button) return;
        const status = button.dataset.status;
        let resolutionNotes = null;
        if (status === "CLOSED") {
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
        } catch (_error) {
            message.textContent = "Alert could not be updated.";
            message.className = "form-feedback error";
            button.disabled = false;
        }
    });

    search.addEventListener("input", load);
    window.addEventListener("secureflow:refresh", load);
    load();
})();

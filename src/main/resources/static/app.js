var transactionForm = document.getElementById("transaction-form");
var searchForm = document.getElementById("search-form");
var alertDialog = document.getElementById("alert-dialog");

function escapeHtml(value) {
    var element = document.createElement("div");
    element.textContent = value == null ? "" : String(value);
    return element.innerHTML;
}

function money(value) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR"
    }).format(Number(value || 0));
}

function indiaTime(value) {
    return new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium",
        timeStyle: "short",
        timeZone: "Asia/Kolkata"
    }).format(new Date(value));
}

function showMessage(text, isError) {
    var box = document.getElementById("message");
    box.textContent = text;
    box.className = isError ? "message show error" : "message show";
}

async function api(url, options) {
    var response = await fetch(url, options);
    if (!response.ok) {
        var error = await response.json().catch(function () {
            return {};
        });
        throw new Error(error.detail || "Request failed");
    }
    return response.json();
}

function transactionUrl() {
    var values = new URLSearchParams();
    values.set("size", "100");

    ["search", "minAmount", "maxAmount"].forEach(function (name) {
        var value = document.getElementById(name).value.trim();
        if (value) {
            values.set(name, value);
        }
    });

    return "/api/transactions?" + values.toString();
}

function renderSummary(summary) {
    document.getElementById("active-alerts").textContent = summary.activeAlertCount;
    document.getElementById("transactions-today").textContent = summary.transactionCountToday;
    document.getElementById("alerts-today").textContent = summary.alertsToday;
    document.getElementById("volume-today").textContent = money(summary.transactionVolumeToday);
}

function renderRules(rules) {
    var html = rules.map(function (rule) {
        var setting = rule.description;
        if (rule.type === "AMOUNT_THRESHOLD") {
            setting = "More than " + money(rule.parameters.threshold);
        }
        if (rule.type === "VELOCITY") {
            setting = "More than " + rule.parameters.maximumTransactions
                + " transactions in " + rule.parameters.windowMinutes + " minutes";
        }

        return '<article class="rule ' + rule.severity.toLowerCase() + '">'
            + "<h3>" + escapeHtml(rule.name) + "</h3>"
            + "<p>" + escapeHtml(setting) + "</p>"
            + "</article>";
    }).join("");

    document.getElementById("rules").innerHTML = html;
}

function renderTransactions(page) {
    var html = page.items.map(function (item) {
        return "<tr>"
            + "<td>#" + item.id + "</td>"
            + "<td>" + escapeHtml(item.accountId) + "</td>"
            + "<td>" + escapeHtml(item.payeeId) + "</td>"
            + "<td>" + money(item.amount) + "</td>"
            + "<td>" + indiaTime(item.transactionTime) + "</td>"
            + "</tr>";
    }).join("");

    document.getElementById("transactions").innerHTML =
        html || '<tr><td colspan="5" class="empty">No transactions found.</td></tr>';
}

function actionButton(id, status, label) {
    return '<button data-id="' + id + '" data-status="' + status + '">'
        + label + "</button>";
}

function alertActions(alert) {
    var buttons = '<button data-details="' + alert.id + '">View</button>';

    if (alert.status === "OPEN") {
        buttons += actionButton(alert.id, "ACKNOWLEDGED", "Acknowledge");
    }
    if (alert.status === "ACKNOWLEDGED") {
        buttons += actionButton(alert.id, "INVESTIGATING", "Investigate");
        buttons += actionButton(alert.id, "DISMISSED", "Dismiss");
    }
    if (alert.status === "INVESTIGATING") {
        buttons += actionButton(alert.id, "CLOSED", "Close");
        buttons += actionButton(alert.id, "DISMISSED", "Dismiss");
    }

    return '<div class="actions">' + buttons + "</div>";
}

function renderAlerts(page) {
    var html = page.items.map(function (alert) {
        return "<tr>"
            + "<td>" + escapeHtml(alert.ruleName) + "</td>"
            + "<td>" + escapeHtml(alert.accountId) + "</td>"
            + '<td><span class="badge ' + alert.severity.toLowerCase() + '">'
            + alert.severity + "</span></td>"
            + '<td><span class="badge ' + alert.status.toLowerCase() + '">'
            + alert.status + "</span></td>"
            + "<td>" + alertActions(alert) + "</td>"
            + "</tr>";
    }).join("");

    document.getElementById("alerts").innerHTML =
        html || '<tr><td colspan="5" class="empty">No alerts found.</td></tr>';
}

async function loadPage() {
    try {
        var results = await Promise.all([
            api("/api/dashboard/summary"),
            api(transactionUrl()),
            api("/api/alerts?size=100"),
            api("/api/rules")
        ]);

        renderSummary(results[0]);
        renderTransactions(results[1]);
        renderAlerts(results[2]);
        renderRules(results[3]);
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function showAlert(id) {
    try {
        var alert = await api("/api/alerts/" + id);

        var transactions = alert.triggeringTransactions.map(function (item) {
            return "<li>" + escapeHtml(item.accountId) + " to "
                + escapeHtml(item.payeeId) + " - " + money(item.amount) + "</li>";
        }).join("");

        var history = alert.history.map(function (item) {
            return "<li><strong>" + item.newStatus + "</strong> - "
                + indiaTime(item.changedAt)
                + (item.note ? "<br>" + escapeHtml(item.note) : "")
                + "</li>";
        }).join("");

        document.getElementById("alert-details").innerHTML =
            "<h2>" + escapeHtml(alert.ruleName) + "</h2>"
            + "<p><strong>Account:</strong> " + escapeHtml(alert.accountId) + "</p>"
            + "<p><strong>Status:</strong> " + alert.status + "</p>"
            + "<p>" + escapeHtml(alert.message) + "</p>"
            + "<h3>Triggering transactions</h3><ul>" + transactions + "</ul>"
            + '<h3>Status history</h3><ol class="history">' + history + "</ol>";

        alertDialog.showModal();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function updateAlert(id, status) {
    var notes = null;

    if (status === "CLOSED" || status === "DISMISSED") {
        notes = window.prompt("Enter resolution notes:");
        if (notes === null) {
            return;
        }
    }

    try {
        await api("/api/alerts/" + id + "/status", {
            method: "PATCH",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                targetStatus: status,
                resolutionNotes: notes
            })
        });
        showMessage("Alert updated.", false);
        await loadPage();
    } catch (error) {
        showMessage(error.message, true);
    }
}

transactionForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    var data = {
        accountId: document.getElementById("accountId").value.trim(),
        payeeId: document.getElementById("payeeId").value.trim(),
        amount: Number(document.getElementById("amount").value),
        currency: "INR",
        description: document.getElementById("description").value.trim()
    };

    try {
        var result = await api("/api/transactions", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(data)
        });

        transactionForm.reset();
        showMessage(
            "Transaction saved. Alerts created: " + result.generatedAlerts.length,
            false);
        await loadPage();
    } catch (error) {
        showMessage(error.message, true);
    }
});

searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    loadPage();
});

document.getElementById("clear-search").addEventListener("click", function () {
    searchForm.reset();
    loadPage();
});

document.getElementById("alerts").addEventListener("click", function (event) {
    if (event.target.dataset.details) {
        showAlert(event.target.dataset.details);
    }
    if (event.target.dataset.status) {
        updateAlert(event.target.dataset.id, event.target.dataset.status);
    }
});

document.getElementById("close-dialog").addEventListener("click", function () {
    alertDialog.close();
});

document.querySelectorAll('.nav-link[href^="#"]').forEach(function (link) {
    link.addEventListener("click", function () {
        document.querySelectorAll('.nav-link[href^="#"]').forEach(function (item) {
            item.classList.remove("active");
        });
        link.classList.add("active");
    });
});

api("/actuator/health").then(function (health) {
    var healthLabel = document.getElementById("health");
    var isHealthy = health.status === "UP";
    healthLabel.textContent = isHealthy ? "System healthy" : "System unavailable";
    healthLabel.className = isHealthy
        ? "health-pill healthy" : "health-pill unhealthy";
}).catch(function () {
    var healthLabel = document.getElementById("health");
    healthLabel.textContent = "Health check failed";
    healthLabel.className = "health-pill unhealthy";
});

loadPage();

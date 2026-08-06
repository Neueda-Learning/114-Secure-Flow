var transactionForm = document.getElementById("transaction-form");
var searchForm = document.getElementById("search-form");
var alertDialog = document.getElementById("alert-dialog");
var demoButton = document.getElementById("load-demo");
var demoLabel = document.getElementById("demo-label");
var minimumAmount = document.getElementById("minAmount");
var maximumAmount = document.getElementById("maxAmount");
var transactionPage = 0;
var transactionPageSize = 6;
var transactionTotalPages = 1;
var currentAlertPage = 0;
var alertHistoryPage = 0;
var alertPageSize = 6;
var currentAlertItems = [];
var alertHistoryItems = [];
var chartTransactions = [];
var chartAlerts = [];
var activeChart = "transactions-hour";
var chartExpanded = false;
var chartOptionIds = [
    "transactions-hour",
    "alerts-rule",
    "alerts-status",
    "alerts-severity",
    "amount-buckets"
];

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

function formatDateTime(value) {
    return new Intl.DateTimeFormat("en-IN", {
        dateStyle: "medium",
        timeStyle: "short",
        timeZone: "Asia/Kolkata"
    }).format(new Date(value));
}

function updateClock() {
    var now = new Date();
    var date = new Intl.DateTimeFormat("en-IN", {
        weekday: "long",
        day: "2-digit",
        month: "long",
        year: "numeric",
        timeZone: "Asia/Kolkata"
    }).format(now);
    var time = new Intl.DateTimeFormat("en-IN", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: true,
        timeZone: "Asia/Kolkata"
    }).format(now);
    var year = new Intl.DateTimeFormat("en-IN", {
        year: "numeric",
        timeZone: "Asia/Kolkata"
    }).format(now);

    document.getElementById("live-date").textContent = date;
    document.getElementById("live-time").textContent = time;
    document.getElementById("footer-year").textContent = year;
}

var messageTimer = null;

function showMessage(text, isError) {
    var box = document.getElementById("message");
    box.textContent = text;
    box.className = isError ? "message show error" : "message show";

    clearTimeout(messageTimer);
    messageTimer = setTimeout(function () {
        box.className = "message";
    }, isError ? 6000 : 4000);
}

function validateAmountRange() {
    maximumAmount.setCustomValidity("");

    if (minimumAmount.value && maximumAmount.value
            && Number(minimumAmount.value) > Number(maximumAmount.value)) {
        maximumAmount.setCustomValidity(
                "Maximum amount must be equal to or greater than the minimum.");
    }
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
    values.set("page", transactionPage);
    values.set("size", transactionPageSize);

    ["search", "transactionId", "minAmount", "maxAmount"].forEach(function (name) {
        var value = document.getElementById(name).value.trim();
        if (value) {
            values.set(name, value);
        }
    });

    return "/api/transactions?" + values.toString();
}

function renderSummary(summary) {
    document.getElementById("active-alerts").textContent = summary.activeAlertCount;
    document.getElementById("transaction-count").textContent = summary.transactionCount;
    document.getElementById("alert-count").textContent = summary.alertCount;
    document.getElementById("transaction-volume").textContent = money(summary.transactionVolume);

    var alertCount = Number(summary.activeAlertCount || 0);
    var alertBadge = document.getElementById("sidebar-alert-count");
    alertBadge.textContent = alertCount > 99 ? "99+" : alertCount;
    document.getElementById("alerts-nav").setAttribute(
            "aria-label", "Alerts, " + alertCount + " current");
}

function countBy(items, labelForItem) {
    var counts = new Map();

    items.forEach(function (item) {
        var label = labelForItem(item);
        counts.set(label, (counts.get(label) || 0) + 1);
    });

    return counts;
}

function transactionHour(value) {
    if (!value) {
        return "Unknown";
    }

    return new Intl.DateTimeFormat("en-IN", {
        hour: "2-digit",
        hour12: false,
        timeZone: "Asia/Kolkata"
    }).format(new Date(value)) + ":00";
}

function chartClass(label) {
    return String(label).toLowerCase().replace(/[^a-z0-9]+/g, "-");
}

function chartRows(counts, unit) {
    var rows = [];

    counts.forEach(function (value, label) {
        var displayUnit = value === 1 ? unit.replace(/s$/, "") : unit;
        rows.push({
            label: label,
            value: value,
            display: value + " " + displayUnit,
            className: chartClass(label)
        });
    });

    rows.sort(function (left, right) {
        return right.value - left.value;
    });
    return rows;
}

function barChart(rows, emptyMessage) {
    if (rows.length === 0) {
        return '<p class="empty">' + escapeHtml(emptyMessage) + "</p>";
    }

    var maximum = Math.max.apply(null, rows.map(function (row) {
        return row.value;
    }));

    return '<ol class="chart-bars">' + rows.map(function (row) {
        var width = Math.max(4, Math.round((row.value / maximum) * 100));
        return '<li class="chart-row">'
            + '<span class="chart-label">' + escapeHtml(row.label) + "</span>"
            + '<span class="chart-track"><span class="chart-fill '
            + escapeHtml(row.className) + '" style="width:' + width
            + '%"></span></span>'
            + '<span class="chart-value">' + escapeHtml(row.display) + "</span>"
            + "</li>";
    }).join("") + "</ol>";
}

function chartModel(chartId) {
    var counts;
    var rows;

    if (chartId === "transactions-hour") {
        counts = countBy(chartTransactions, function (transaction) {
            return transactionHour(transaction.transactionTime);
        });
        rows = chartRows(counts, "transactions");
        return {
            title: "Transactions by hour",
            subtitle: "Based on up to 100 latest transactions.",
            rows: rows,
            empty: "No transaction data is available."
        };
    }

    if (chartId === "alerts-rule") {
        counts = countBy(chartAlerts, function (alert) {
            return alert.ruleName || "Unknown rule";
        });
        rows = chartRows(counts, "alerts");
        return {
            title: "Alerts by rule",
            subtitle: "Compares alerts generated by each monitoring rule.",
            rows: rows,
            empty: "No alert data is available."
        };
    }

    if (chartId === "alerts-status") {
        counts = countBy(chartAlerts, function (alert) {
            return String(alert.status || "UNKNOWN").replaceAll("_", " ");
        });
        rows = chartRows(counts, "alerts");
        return {
            title: "Alert status mix",
            subtitle: "Shows alerts across their current investigation stages.",
            rows: rows,
            empty: "No alert statuses are available."
        };
    }

    if (chartId === "alerts-severity") {
        counts = countBy(chartAlerts, function (alert) {
            return alert.severity || "UNKNOWN";
        });
        rows = chartRows(counts, "alerts");
        return {
            title: "Alert severity mix",
            subtitle: "Compares high and medium severity alerts.",
            rows: rows,
            empty: "No alert severities are available."
        };
    }

    counts = new Map([
        ["0-1K", 0],
        ["1K-5K", 0],
        ["5K-10K", 0],
        ["10K+", 0]
    ]);
    chartTransactions.forEach(function (transaction) {
        var amount = Number(transaction.amount || 0);
        var bucket = "10K+";
        if (amount < 1000) {
            bucket = "0-1K";
        } else if (amount < 5000) {
            bucket = "1K-5K";
        } else if (amount < 10000) {
            bucket = "5K-10K";
        }
        counts.set(bucket, counts.get(bucket) + 1);
    });
    rows = chartRows(counts, "transactions");
    return {
        title: "Transaction amount buckets",
        subtitle: "Groups the latest transactions by payment size.",
        rows: rows,
        empty: "No transaction amounts are available."
    };
}

function chartLegend(rows) {
    return rows.slice(0, 5).map(function (row) {
        return '<span class="legend-chip"><i class="legend-dot '
            + escapeHtml(row.className) + '"></i>'
            + escapeHtml(row.label) + "</span>";
    }).join("");
}

function renderChartExplorer() {
    var stage = document.getElementById("chart-stage");
    var model = chartModel(activeChart);

    document.querySelectorAll("[data-chart-option]").forEach(function (button) {
        var selected = chartExpanded
            && button.dataset.chartOption === activeChart;
        button.classList.toggle("active", selected);
        button.setAttribute("aria-selected", String(selected));
        button.setAttribute("aria-expanded", String(selected));
    });

    stage.classList.toggle("expanded", chartExpanded);
    if (!chartExpanded) {
        document.getElementById("chart-stage-title").textContent = "Choose a chart";
        document.getElementById("chart-stage-subtitle").textContent =
            "Select an option above to expand the chart.";
        document.getElementById("chart-plot").innerHTML =
            '<p class="empty">No chart selected yet.</p>';
        document.getElementById("chart-legend").innerHTML = "";
        return;
    }

    document.getElementById("chart-stage-title").textContent = model.title;
    document.getElementById("chart-stage-subtitle").textContent = model.subtitle;
    document.getElementById("chart-plot").innerHTML =
        barChart(model.rows, model.empty);
    document.getElementById("chart-legend").innerHTML = chartLegend(model.rows);
}

function toggleChart(chartId) {
    if (activeChart === chartId && chartExpanded) {
        chartExpanded = false;
    } else {
        activeChart = chartId;
        chartExpanded = true;
    }
    renderChartExplorer();
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

function transactionAlerts(transaction, alerts) {
    var relatedAlerts = alerts.filter(function (alert) {
        return alert.transactionIds.includes(transaction.id);
    });

    if (relatedAlerts.length === 0) {
        return '<span class="no-alert">None</span>';
    }

    return '<div class="transaction-alerts">'
        + relatedAlerts.map(function (alert) {
            return '<button class="transaction-alert-link" data-details="'
                + alert.id + '" title="' + escapeHtml(alert.ruleName) + '">'
                + '<span class="badge ' + alert.status.toLowerCase() + '">'
                + alert.status + '</span><span>View alert</span></button>';
        }).join("")
        + "</div>";
}

function renderPagination(page) {
    var totalPages = Math.max(page.totalPages, 1);
    transactionTotalPages = totalPages;
    document.getElementById("page-status").textContent =
        "Page " + (page.page + 1) + " of " + totalPages;
    document.getElementById("previous-page").disabled = page.page === 0;
    document.getElementById("next-page").disabled =
        page.totalPages === 0 || page.page + 1 >= page.totalPages;
}

function renderTransactions(page, alerts) {
    var html = page.items.map(function (item) {
        return "<tr>"
            + "<td>#" + item.id + "</td>"
            + "<td>" + escapeHtml(item.accountId) + "</td>"
            + "<td>" + escapeHtml(item.payeeId) + "</td>"
            + '<td class="transaction-description">'
            + escapeHtml(item.description || "—") + "</td>"
            + "<td>" + money(item.amount) + "</td>"
            + "<td>" + transactionAlerts(item, alerts) + "</td>"
            + "<td>" + formatDateTime(item.transactionTime) + "</td>"
            + "</tr>";
    }).join("");

    document.getElementById("transactions").innerHTML =
        html || '<tr><td colspan="7" class="empty">No transactions found.</td></tr>';
    renderPagination(page);
}

function actionButton(id, status, label) {
    return '<button data-id="' + id + '" data-status="' + status + '">'
        + label + "</button>";
}

function alertActions(alert) {
    var buttons = '<button data-details="' + alert.id + '">Review</button>';

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

function alertRows(alerts) {
    return alerts.map(function (alert) {
        var transactionIds = alert.transactionIds.map(function (id) {
            return "<span>#" + id + "</span>";
        }).join("");

        return "<tr>"
            + "<td>" + escapeHtml(alert.ruleName) + "</td>"
            + "<td>" + escapeHtml(alert.accountId) + "</td>"
            + '<td><div class="transaction-ids">' + transactionIds + "</div></td>"
            + "<td>" + formatDateTime(alert.createdAt) + "</td>"
            + '<td><span class="badge ' + alert.severity.toLowerCase() + '">'
            + alert.severity + "</span></td>"
            + '<td><span class="badge ' + alert.status.toLowerCase() + '">'
            + alert.status + "</span></td>"
            + "<td>" + alertActions(alert) + "</td>"
            + "</tr>";
    }).join("");
}

function renderAlertPage(items, requestedPage, tableId, controlPrefix, emptyText) {
    var totalPages = Math.max(Math.ceil(items.length / alertPageSize), 1);
    var page = Math.min(requestedPage, totalPages - 1);
    var start = page * alertPageSize;
    var visibleItems = items.slice(start, start + alertPageSize);

    document.getElementById(tableId).innerHTML = alertRows(visibleItems)
        || '<tr><td colspan="7" class="empty">' + emptyText + "</td></tr>";
    document.getElementById(controlPrefix + "-page").textContent =
        "Page " + (page + 1) + " of " + totalPages;
    document.getElementById(controlPrefix + "-previous").disabled = page === 0;
    document.getElementById(controlPrefix + "-next").disabled =
        items.length === 0 || page + 1 >= totalPages;

    return page;
}

function renderAlertLists() {
    currentAlertPage = renderAlertPage(
            currentAlertItems,
            currentAlertPage,
            "current-alerts",
            "current-alerts",
            "No current alerts.");
    alertHistoryPage = renderAlertPage(
            alertHistoryItems,
            alertHistoryPage,
            "alert-history",
            "alert-history",
            "No alert history yet.");
}

function renderAlerts(page) {
    currentAlertItems = page.items.filter(function (alert) {
        return alert.status !== "CLOSED" && alert.status !== "DISMISSED";
    });
    alertHistoryItems = page.items.filter(function (alert) {
        return alert.status === "CLOSED" || alert.status === "DISMISSED";
    });

    renderAlertLists();
}

async function loadPage() {
    try {
        var results = await Promise.all([
            api("/api/dashboard/summary"),
            api(transactionUrl()),
            api("/api/alerts?size=100"),
            api("/api/rules"),
            api("/api/transactions?size=100")
        ]);

        chartAlerts = results[2].items;
        chartTransactions = results[4].items;
        renderSummary(results[0]);
        renderTransactions(results[1], results[2].items);
        renderAlerts(results[2]);
        renderRules(results[3]);
        renderChartExplorer();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function showAlert(id) {
    try {
        var alert = await api("/api/alerts/" + id);

        var transactions = alert.triggeringTransactions.map(function (item) {
            return "<li><strong>#" + item.id + "</strong> "
                + escapeHtml(item.accountId) + " to "
                + escapeHtml(item.payeeId) + " - " + money(item.amount) + "</li>";
        }).join("");

        var history = alert.history.map(function (item) {
            return "<li><strong>" + item.newStatus + "</strong> - "
                + formatDateTime(item.changedAt)
                + (item.note ? "<br>" + escapeHtml(item.note) : "")
                + "</li>";
        }).join("");

        document.getElementById("alert-details").innerHTML =
            "<h2>" + escapeHtml(alert.ruleName) + "</h2>"
            + "<p><strong>Account:</strong> " + escapeHtml(alert.accountId) + "</p>"
            + '<p><strong>Status:</strong> <span class="badge '
            + alert.status.toLowerCase() + '">' + alert.status + "</span></p>"
            + "<p>" + escapeHtml(alert.message) + "</p>"
            + alertStageActions(alert)
            + "<h3>Triggering transactions</h3><ul>" + transactions + "</ul>"
            + '<h3>Status history</h3><ol class="history">' + history + "</ol>";

        if (!alertDialog.open) {
            alertDialog.showModal();
        }
    } catch (error) {
        showMessage(error.message, true);
    }
}

function alertStageActions(alert) {
    var buttons = "";

    if (alert.status === "OPEN") {
        buttons = actionButton(alert.id, "ACKNOWLEDGED", "Acknowledge");
    }
    if (alert.status === "ACKNOWLEDGED") {
        buttons = actionButton(alert.id, "INVESTIGATING", "Start investigation")
            + actionButton(alert.id, "DISMISSED", "Dismiss");
    }
    if (alert.status === "INVESTIGATING") {
        buttons = actionButton(alert.id, "CLOSED", "Close alert")
            + actionButton(alert.id, "DISMISSED", "Dismiss");
    }

    if (!buttons) {
        return "";
    }

    return '<section class="dialog-actions"><h3>Update status</h3><div class="actions">'
        + buttons + "</div></section>";
}

async function updateAlert(id, status) {
    var notes = null;

    if (status === "CLOSED" || status === "DISMISSED") {
        notes = window.prompt("Provide resolution notes:");
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
        return true;
    } catch (error) {
        showMessage(error.message, true);
        return false;
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
        transactionPage = 0;
        showMessage(
            "Transaction recorded. Alerts generated: " + result.generatedAlerts.length,
            false);
        await loadPage();
    } catch (error) {
        showMessage(error.message, true);
    }
});

searchForm.addEventListener("submit", function (event) {
    event.preventDefault();
    validateAmountRange();
    if (!searchForm.reportValidity()) {
        return;
    }
    transactionPage = 0;
    loadPage();
});

minimumAmount.addEventListener("input", validateAmountRange);
maximumAmount.addEventListener("input", validateAmountRange);

document.getElementById("clear-search").addEventListener("click", function () {
    searchForm.reset();
    validateAmountRange();
    transactionPage = 0;
    loadPage();
});

document.getElementById("previous-page").addEventListener("click", function () {
    if (transactionPage > 0) {
        transactionPage -= 1;
        loadPage();
    }
});

document.getElementById("next-page").addEventListener("click", function () {
    if (transactionPage + 1 < transactionTotalPages) {
        transactionPage += 1;
        loadPage();
    }
});

document.getElementById("current-alerts-previous").addEventListener("click", function () {
    if (currentAlertPage > 0) {
        currentAlertPage -= 1;
        renderAlertLists();
    }
});

document.getElementById("current-alerts-next").addEventListener("click", function () {
    if ((currentAlertPage + 1) * alertPageSize < currentAlertItems.length) {
        currentAlertPage += 1;
        renderAlertLists();
    }
});

document.getElementById("alert-history-previous").addEventListener("click", function () {
    if (alertHistoryPage > 0) {
        alertHistoryPage -= 1;
        renderAlertLists();
    }
});

document.getElementById("alert-history-next").addEventListener("click", function () {
    if ((alertHistoryPage + 1) * alertPageSize < alertHistoryItems.length) {
        alertHistoryPage += 1;
        renderAlertLists();
    }
});

document.getElementById("transactions").addEventListener("click", function (event) {
    if (event.target.closest("[data-details]")) {
        showAlert(event.target.closest("[data-details]").dataset.details);
    }
});

document.querySelectorAll("#current-alerts, #alert-history").forEach(function (table) {
    table.addEventListener("click", function (event) {
        if (event.target.dataset.details) {
            showAlert(event.target.dataset.details);
        }
        if (event.target.dataset.status) {
            updateAlert(event.target.dataset.id, event.target.dataset.status);
        }
    });
});

document.getElementById("close-dialog").addEventListener("click", function () {
    alertDialog.close();
});

alertDialog.addEventListener("click", async function (event) {
    var button = event.target.closest("[data-status]");
    if (!button) {
        return;
    }

    button.disabled = true;
    var updated = await updateAlert(button.dataset.id, button.dataset.status);
    if (updated) {
        await showAlert(button.dataset.id);
    } else {
        button.disabled = false;
    }
});

demoButton.addEventListener("click", async function () {
    demoButton.disabled = true;
    demoLabel.textContent = "Adding demo data...";

    try {
        var result = await api("/api/demo/seed", {method: "POST"});
        showMessage(result.message, false);
        await loadPage();
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        demoButton.disabled = false;
        demoLabel.textContent = "Add demo data";
    }
});

document.querySelectorAll('.nav-link[href^="#"]').forEach(function (link) {
    link.addEventListener("click", function () {
        document.querySelectorAll('.nav-link[href^="#"]').forEach(function (item) {
            item.classList.remove("active");
        });
        link.classList.add("active");
    });
});

document.querySelectorAll("[data-chart-option]").forEach(function (button) {
    button.addEventListener("click", function () {
        toggleChart(button.dataset.chartOption);
    });
});

document.querySelector(".chart-options").addEventListener("keydown", function (event) {
    if (!["ArrowRight", "ArrowLeft", "Home", "End"].includes(event.key)) {
        return;
    }

    var buttons = chartOptionIds.map(function (id) {
        return document.querySelector('[data-chart-option="' + id + '"]');
    });
    var current = buttons.indexOf(document.activeElement);
    var target = current < 0 ? 0 : current;

    event.preventDefault();
    if (event.key === "ArrowRight") {
        target = (target + 1) % buttons.length;
    }
    if (event.key === "ArrowLeft") {
        target = (target - 1 + buttons.length) % buttons.length;
    }
    if (event.key === "Home") {
        target = 0;
    }
    if (event.key === "End") {
        target = buttons.length - 1;
    }
    buttons[target].focus();
});

updateClock();
window.setInterval(updateClock, 1000);
renderChartExplorer();
loadPage();

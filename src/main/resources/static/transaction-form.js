(function () {
    const form = document.getElementById("create-transaction-form");
    const feedback = document.getElementById("transaction-form-feedback");

    if (!form || !feedback) {
        return;
    }

    const accountIdInput = document.getElementById("accountId");
    const payeeIdInput = document.getElementById("payeeId");
    const amountInput = document.getElementById("amount");
    const currencyInput = document.getElementById("currency");
    const transactionTimeInput = document.getElementById("transactionTime");
    const descriptionInput = document.getElementById("description");
    const submitButton = document.getElementById("submit-transaction");

    function showFeedback(message, kind) {
        feedback.textContent = message;
        feedback.classList.remove("success", "error");

        if (kind) {
            feedback.classList.add(kind);
        }
    }

    function validateForm() {
        const accountId = accountIdInput.value.trim();
        const payeeId = payeeIdInput.value.trim();
        const amount = Number.parseFloat(amountInput.value);
        const currency = currencyInput.value.trim();
        const transactionTime = transactionTimeInput.value;

        if (!accountId || !payeeId || !currency || !transactionTime || Number.isNaN(amountInput.valueAsNumber)) {
            showFeedback("Please fill in all required fields.", "error");
            return null;
        }

        if (!(amount > 0)) {
            showFeedback("Amount must be greater than 0.", "error");
            return null;
        }

        if (!/^[A-Za-z]{3}$/.test(currency)) {
            showFeedback("Currency must be exactly 3 letters.", "error");
            return null;
        }

        const transactionDate = new Date(transactionTime);
        if (Number.isNaN(transactionDate.getTime())) {
            showFeedback("Transaction time is invalid.", "error");
            return null;
        }

        const description = descriptionInput.value.trim();

        return {
            accountId: accountId,
            payeeId: payeeId,
            amount: amount,
            currency: currency.toUpperCase(),
            transactionTime: transactionDate.toISOString(),
            description: description
        };
    }

    form.addEventListener("submit", async function (event) {
        event.preventDefault();
        showFeedback("", null);

        const payload = validateForm();
        if (!payload) {
            return;
        }

        submitButton.disabled = true;

        try {
            const response = await fetch("/api/transactions", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                showFeedback("Unable to submit transaction. Please check the data and try again.", "error");
                return;
            }

            form.reset();
            showFeedback("Transaction submitted successfully.", "success");
            window.dispatchEvent(new CustomEvent("secureflow:refresh"));
        } catch (_error) {
            showFeedback("Unable to submit transaction right now. Please try again.", "error");
        } finally {
            submitButton.disabled = false;
        }
    });
})();

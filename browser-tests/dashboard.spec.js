const {test, expect} = require("@playwright/test");
const AxeBuilder = require("@axe-core/playwright").default;

test("dashboard supports its main browser journey", async ({page}) => {
    await page.goto("/");

    await expect(page.getByRole("heading", {name: "Submit transaction"}))
        .toBeVisible();

    const uniqueSuffix = Date.now().toString();
    const accountId = "WEB-" + uniqueSuffix;

    await page.getByLabel("Account ID").fill(accountId);
    await page.getByLabel("Payee ID").fill("PAYEE-" + uniqueSuffix);
    await page.getByRole("spinbutton", {name: "Amount", exact: true})
        .fill("12500.00");
    await page.getByLabel("Description").fill("Browser verification payment");
    await page.getByRole("button", {name: "Submit for monitoring"}).click();

    await expect(page.getByRole("status"))
        .toContainText("Transaction recorded");
    await expect(page.locator("#transactions")).toContainText(accountId);

    await page.getByRole("tab", {name: "Alerts by rule"}).click();
    await expect(page.locator("#chart-stage-title")).toHaveText("Alerts by rule");
});

test("dashboard has no automatically detectable WCAG A or AA violations",
    async ({page}) => {
        await page.goto("/");
        await expect(page.locator("#transactions tr").first()).toBeVisible();
        await expect(page.locator("#chart-stage")).toHaveCSS("opacity", "1");

        const results = await new AxeBuilder({page})
            .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"])
            .analyze();

        expect(results.violations).toEqual([]);
    });

test("close and dismiss use themed resolution forms", async ({page}) => {
    await page.goto("/");

    const uniqueSuffix = Date.now().toString();
    const accountId = "RESOLVE-" + uniqueSuffix;
    await page.getByLabel("Account ID").fill(accountId);
    await page.getByLabel("Payee ID").fill("PAYEE-" + uniqueSuffix);
    await page.getByRole("spinbutton", {name: "Amount", exact: true})
        .fill("12500.00");
    await page.getByLabel("Description").fill("Resolution form verification");
    await page.getByRole("button", {name: "Submit for monitoring"}).click();

    const alertRows = page.locator("#current-alerts tr").filter({hasText: accountId});
    await expect(alertRows).toHaveCount(2);

    const firstAlert = alertRows.first();
    await firstAlert.getByRole("button", {name: "Acknowledge"}).click();
    await firstAlert.getByRole("button", {name: "Investigate"}).click();
    await firstAlert.getByRole("button", {name: "Close", exact: true}).click();

    const resolutionForm = page.locator("#resolution-form");
    await expect(resolutionForm).toBeVisible();
    await expect(page.getByRole("heading", {name: "Close this alert"})).toBeVisible();
    await expect(page.getByLabel("Resolution notes")).toBeFocused();
    const dialogAccessibility = await new AxeBuilder({page})
        .include("#alert-dialog")
        .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"])
        .analyze();
    expect(dialogAccessibility.violations).toEqual([]);
    await resolutionForm.getByRole("button", {name: "Close alert", exact: true}).click();
    await expect(page.getByRole("alert")).toHaveText(
        "Enter at least 3 characters before confirming.");
    await expect(page.getByLabel("Resolution notes")).toHaveAttribute(
        "aria-invalid", "true");
    await page.getByLabel("Resolution notes").fill("Investigation completed safely");
    await expect(page.locator("#resolution-count")).toHaveText("30 / 500");
    await resolutionForm.getByRole("button", {name: "Close alert", exact: true}).click();
    await expect(page.getByText("CLOSED", {exact: true}).last()).toBeVisible();
    await page.getByRole("button", {name: "Close alert details"}).click();

    const remainingAlert = page.locator("#current-alerts tr")
        .filter({hasText: accountId}).first();
    await remainingAlert.getByRole("button", {name: "Acknowledge"}).click();
    await remainingAlert.getByRole("button", {name: "Dismiss", exact: true}).click();

    await expect(resolutionForm).toBeVisible();
    await expect(page.getByRole("heading", {name: "Dismiss this alert"})).toBeVisible();
    await page.getByLabel("Resolution notes").fill("Confirmed false positive");
    await resolutionForm.getByRole("button", {name: "Dismiss alert", exact: true}).click();
    await expect(page.getByText("DISMISSED", {exact: true}).last()).toBeVisible();
});

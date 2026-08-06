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

        const results = await new AxeBuilder({page})
            .withTags(["wcag2a", "wcag2aa", "wcag21a", "wcag21aa"])
            .analyze();

        expect(results.violations).toEqual([]);
    });

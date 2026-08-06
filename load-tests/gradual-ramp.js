import http from "k6/http";
import { check } from "k6";

export const options = {
  scenarios: {
    gradual_load: {
      executor: "ramping-arrival-rate",
      startRate: 5,
      timeUnit: "1s",
      preAllocatedVUs: 80,
      maxVUs: 1000,
      stages: [
        { target: 20, duration: "30s" },
        { target: 50, duration: "30s" },
        { target: 100, duration: "45s" },
        { target: 150, duration: "45s" },
        { target: 200, duration: "60s" },
      ],
      gracefulStop: "20s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.10"],
    http_req_duration: ["p(95)<2000", "p(99)<5000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://127.0.0.1:8081";
const ENDPOINT = `${BASE_URL}/api/transactions`;

function randomAmount() {
  const amount = Math.random() < 0.2
    ? 10000 + Math.random() * 40000
    : 10 + Math.random() * 9000;
  return amount.toFixed(2);
}

function payload() {
  const suffix = `${Date.now()}-${__VU}-${__ITER}`;
  return JSON.stringify({
    accountId: `LT-ACC-${suffix}`.slice(0, 50),
    payeeId: `LT-PAY-${suffix}`.slice(0, 50),
    amount: randomAmount(),
    currency: "INR",
    description: `Gradual-load-${suffix}`.slice(0, 255),
  });
}

export default function () {
  const response = http.post(ENDPOINT, payload(), {
    headers: { "Content-Type": "application/json" },
    timeout: "30s",
  });

  check(response, {
    "transaction accepted": (r) => r.status === 201,
  });
}

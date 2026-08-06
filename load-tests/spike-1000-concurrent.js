import http from "k6/http";
import { check } from "k6";

export const options = {
  scenarios: {
    instant_1000: {
      executor: "shared-iterations",
      vus: 1000,
      iterations: 1000,
      maxDuration: "2m",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.15"],
    http_req_duration: ["p(95)<4000", "p(99)<8000"],
  },
};

const BASE_URL = __ENV.BASE_URL || "http://127.0.0.1:8081";
const ENDPOINT = `${BASE_URL}/api/transactions`;

function randomAmount() {
  const amount = Math.random() < 0.25
    ? 10000 + Math.random() * 50000
    : 10 + Math.random() * 8000;
  return amount.toFixed(2);
}

function payload() {
  const suffix = `${Date.now()}-${__VU}-${__ITER}`;
  return JSON.stringify({
    accountId: `SP-ACC-${suffix}`.slice(0, 50),
    payeeId: `SP-PAY-${suffix}`.slice(0, 50),
    amount: randomAmount(),
    currency: "INR",
    description: `Spike-load-${suffix}`.slice(0, 255),
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

import http from 'k6/http';
import { check, sleep } from 'k6';  // ✅ Importar sleep

export const options = {
  vus: 3,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<5000'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = 'https://labpiloto.com';

export default function() {
  // Test 1: Página principal
  const homeResponse = http.get(BASE_URL);
  check(homeResponse, {
    'home page status 200': (r) => r.status === 200,
    'home page loaded': (r) => r.body.includes('LabPilot'),
  });

  // Test 2: API de laboratorios
  const labsResponse = http.get(`${BASE_URL}/api/laboratorios/disponibles`);
  check(labsResponse, {
    'labs API status 200': (r) => r.status === 200,
    'labs API response time < 2s': (r) => r.timings.duration < 2000,
  });

  // Test 3: API de cursos
  const cursosResponse = http.get(`${BASE_URL}/api/cursos`);
  check(cursosResponse, {
    'cursos API status 200': (r) => r.status === 200,
    'cursos API response time < 2s': (r) => r.timings.duration < 2000,
  });

  sleep(1);  // ✅ Ahora funciona
}

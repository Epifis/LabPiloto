// config.js - Configuración central de la API
const API_BASE_URL = 'https://labpiloto.com/api';

const API_ENDPOINTS = {
  usuarios: `${API_BASE_URL}/usuarios`,
  laboratorios: `${API_BASE_URL}/laboratorios`,
  elementos: `${API_BASE_URL}/elementos`,
  reservas: `${API_BASE_URL}/reservas`,
  prestamos: `${API_BASE_URL}/prestamos`,
  cursos: `${API_BASE_URL}/cursos`,
  reservasRecurrentes: `${API_BASE_URL}/reservas/recurrentes`
};

// Funciones helper para hacer peticiones
const api = {
  async get(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Error: ${response.status}`);
    return response.json();
  },

  async post(url, data) {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`Error: ${response.status}`);
    return response.json();
  },

  async put(url, data) {
    const response = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (!response.ok) throw new Error(`Error: ${response.status}`);
    return response.json();
  },

  async delete(url) {
    const response = await fetch(url, { method: 'DELETE' });
    if (!response.ok) throw new Error(`Error: ${response.status}`);
    return response.ok;
  }
};

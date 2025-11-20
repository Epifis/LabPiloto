// Configuración centralizada de la API - HACER GLOBAL
window.API_BASE_URL = 'https://labpiloto.com/api';

window.API_ENDPOINTS = {
  auth: `${window.API_BASE_URL}/auth`,
  usuarios: `${window.API_BASE_URL}/usuarios`,
  laboratorios: `${window.API_BASE_URL}/laboratorios`,
  elementos: `${window.API_BASE_URL}/elementos`,
  reservas: `${window.API_BASE_URL}/reservas`,
  prestamos: `${window.API_BASE_URL}/prestamos`,
  admins: `${window.API_BASE_URL}/admins`
};


const currentPage = window.location.pathname.split('/').pop();
const adminPages = ['admin.html', 'registrar-admin.html'];

if (adminPages.includes(currentPage)) {
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = 'login.html';
    }
}
const api = {
  async get(url) {
    console.log('GET:', url);
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error en GET:', errorText);
      throw new Error(`Error: ${response.status}`);
    }
    
    return response.json();
  },

  async post(url, data) {
    console.log('POST:', url);
    console.log('Data:', JSON.stringify(data, null, 2));
    
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const responseText = await response.text();
    console.log('Respuesta del servidor:', responseText);
    
    if (!response.ok) {
      console.error('Error en POST:', response.status, responseText);
      
      try {
        const errorData = JSON.parse(responseText);
        const error = new Error(`Error: ${response.status}`);
        error.response = errorData;
        throw error;
      } catch (parseError) {
        throw new Error(`Error: ${response.status} - ${responseText}`);
      }
    }

    try {
      return JSON.parse(responseText);
    } catch (e) {
      return responseText;
    }
  },

  async put(url, data) {
    console.log('PUT:', url);
    console.log('Data:', JSON.stringify(data, null, 2));
    
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'PUT',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const responseText = await response.text();
    console.log('Respuesta del servidor:', responseText);

    if (!response.ok) {
      console.error('Error en PUT:', response.status, responseText);
      
      try {
        const errorData = JSON.parse(responseText);
        const error = new Error(`Error: ${response.status}`);
        error.response = errorData;
        throw error;
      } catch (parseError) {
        throw new Error(`Error: ${response.status} - ${responseText}`);
      }
    }

    try {
      return JSON.parse(responseText);
    } catch (e) {
      return responseText;
    }
  },

  async delete(url) {
    console.log('DELETE:', url);
    const token = localStorage.getItem('authToken');
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error en DELETE:', errorText);
      throw new Error(`Error: ${response.status}`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }
};

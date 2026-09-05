import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { api } from '../services/api';
import { AxiosError } from 'axios';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Username and password are required');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await api.post('/auth/login', { username, password });
      setAuth(response.data.token, response.data.user);
      navigate('/map');
    } catch (err) {
      if (err instanceof AxiosError && err.response) {
         setError(err.response.data?.message || 'Invalid credentials');
      } else {
         setError('Login failed. Please try again.');
      }
      console.error('Login failed', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded shadow-md w-96">
        <h2 className="text-2xl font-bold mb-6 text-center text-blue-600">Land Stack Portal</h2>
        {error && <div className="mb-4 text-sm text-red-600 text-center">{error}</div>}
        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-1">Username</label>
            <input 
              type="text" 
              className="w-full border rounded p-2" 
              value={username} 
              onChange={(e) => setUsername(e.target.value)} 
              disabled={loading}
            />
          </div>
          <div className="mb-6">
            <label className="block text-sm font-medium mb-1">Password</label>
            <input 
              type="password" 
              className="w-full border rounded p-2" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              disabled={loading}
            />
          </div>
          <button type="submit" disabled={loading} className="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700 disabled:opacity-50">
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  );
}

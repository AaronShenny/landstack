import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MapViewer from './pages/MapViewer';
import AdminDashboard from './pages/AdminDashboard';
import { useAuthStore } from './store/useAuthStore';
import { api } from './services/api';

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const token = useAuthStore((state) => state.token);
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function App() {
  const [loading, setLoading] = useState(true);
  const { token, setAuth, logout } = useAuthStore();

  useEffect(() => {
    const initAuth = async () => {
      if (token) {
        try {
          const response = await api.get('/auth/me');
          setAuth(token, response.data);
        } catch (error) {
          console.error("Failed to fetch user profile", error);
          logout();
        }
      }
      setLoading(false);
    };
    initAuth();
  }, [token, setAuth, logout]);

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route 
          path="/map" 
          element={
            <ProtectedRoute>
              <MapViewer />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/admin" 
          element={
            <ProtectedRoute>
              <AdminDashboard />
            </ProtectedRoute>
          } 
        />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';


function ProtectedRoute({ children }){
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/rides"
        element={
          <ProtectedRoute>
            <div>Ride list goes here</div>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/rides" />} />
    </Routes>
  )
}

export default App;


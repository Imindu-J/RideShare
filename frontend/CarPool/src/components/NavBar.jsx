import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; 


export default function NavBar() {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate('/login');
    }

    if (!isAuthenticated) return null;

    return(
        <nav style={{display: 'flex', gap: '1rem', padding: '1rem', borderBottom: '1px solid #ccc'}}>
            <Link to="/rides">Rides</Link>
            <Link to="/rides/new">Offer a Ride</Link>
            <Link to="/my-bookings">My Bookings</Link>
            <Link to="/my-vehicles">My Vehicles</Link>
            <span style={{ marginLeft: 'auto' }}>
                {user?.name} ({user?.email})
                <button onClick={handleLogout} style={{ marginLeft: '1rem'}}>Log out</button>
            </span>
        </nav>
    );

}

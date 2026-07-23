import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getRides } from '../api/rides';


export default function RideList() {
    const [rides, setRides] = useState([]);
    const [loading, setLoading] = useState(true);
    const [ error, setError] = useState('');

    useEffect (() => {
        getRides()
            .then((res) => setRides(res.data))
            .catch((err) => setError(err.response?.data?.message || 'Failed to load rides'))
            .finally(() => setLoading(false));

    }, []);

    if (loading) return <p>Loading rides...</p>

    if (error) return <p style={{ color: 'red'}}>{error}</p>

    return (
        <div>
            <h2>Available Rides</h2>
            {rides.length === 0 && <p>No rides yet.</p>}
            <ul>
                {rides.map((ride)=>(
                    <li key={ride.id}>
                        <Link to={`/rides/${ride.id}`}>
                            {ride.origin} → {ride.destination}
                        </Link>
                        { ' - ' }
                        {new Date(ride.departureTime).toLocaleString()}
                        { ' - ' }
                        {ride.seatsAvailable}/{ride.totalSeats} seats - {ride.status}
                    </li>
                ))}
            </ul>
        </div>
    )

}

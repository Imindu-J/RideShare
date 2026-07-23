import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';


export default function Register() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [roles, setRoles] = useState(['RIDER']);
    const [error, setError] = useState('');
    const { register } = useAuth();
    const navigate = useNavigate();

    function toggleRole(role){
        setRoles((prev) => 
            prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
        );
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        if (roles.length === 0){
            setError('Select at least one role');
            return;
        }
        try {
            await register(name, email, password, roles);
            navigate('/rides');
        } catch(err) {
            setError(err.response?.data?.message || 'Registration failed');
        }
    }

    return (
        <div>
            <h2>Register</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Name</label>
                    <input value={name} onChange={(e) => setName(e.target.value)} required />
                </div>
                <div>
                    <label>Email</label>
                    <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                </div>
                <div>
                    <label>Password</label>
                    <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
                </div>
                <div>
                    <label>
                        <input type='checkbox' checked={roles.includes('DRIVER')} onChange={() => toggleRole('DRIVER')} />
                        Driver
                    </label>
                    <label>
                        <input type='checkbox' checked={roles.includes('RIDER')} onChange={() => toggleRole('RIDER')} />
                        Rider
                    </label>
                </div>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <button type="submit">Register</button>
            </form>
            <p>Already have an account? <Link to="/login">Log in</Link></p>
        </div>
    )



}

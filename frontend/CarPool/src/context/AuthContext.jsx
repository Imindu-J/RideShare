import {createContext, useContext, useState } from 'react';
import api from '../api/axios';


const AuthContext = createContext(null);

export function AuthProvider({ children }){
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    });

    async function login(email, password) {
        const res = await api.post('/auth/login', {email, password});
        const { token, email: userEmail, name } = res.data;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({email:userEmail, name}));
        setToken(token);
        setUser({email: userEmail, name});
    }

    async function register(name, email, password, roles) {
        const res = await api.post('/auth/register', {name, email, password, roles });
        const { token, email: userEmail, name: userName } = res.data;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({email:userEmail, name: userName}));
        setToken(token);
        setUser({email:userEmail, name: userName});
    }

    function logout(){
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    }

    return(
        <AuthContext.Provider value={{token, user, login, register, logout, isAuthenticated: !!token}}>
            {children}
        </AuthContext.Provider>
    );
}


export function useAuth() {
    return useContext(AuthContext);
}



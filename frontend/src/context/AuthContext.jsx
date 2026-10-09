import React, { createContext, useContext, useEffect, useState } from 'react';
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged,
  signInWithPopup
} from 'firebase/auth';
import { auth, googleProvider } from '../firebase';

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [userRole, setUserRole] = useState('user');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        setCurrentUser(user);
        try {
          const tokenResult = await user.getIdTokenResult();
          const role = tokenResult.claims.role || 'user';
          setUserRole(role);
        } catch (e) {
          setUserRole('user');
        }
      } else {
        // Check for local dev mock user
        const devUser = localStorage.getItem('dev_user');
        if (devUser) {
          const parsed = JSON.parse(devUser);
          setCurrentUser(parsed);
          setUserRole(parsed.role || 'user');
        } else {
          setCurrentUser(null);
          setUserRole(null);
        }
      }
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const login = (email, password) => {
    localStorage.removeItem('dev_user');
    localStorage.removeItem('dev_token');
    return signInWithEmailAndPassword(auth, email, password);
  };

  const signup = (email, password) => {
    localStorage.removeItem('dev_user');
    localStorage.removeItem('dev_token');
    return createUserWithEmailAndPassword(auth, email, password);
  };

  const loginWithGoogle = () => {
    return signInWithPopup(auth, googleProvider);
  };

  const loginDevMock = (role = 'user') => {
    const mockToken = role === 'admin' ? 'mock-dev-token-admin' : 'mock-dev-token-user';
    const mockUser = {
      uid: role === 'admin' ? 'dev-admin-uid' : 'dev-user-uid',
      email: role === 'admin' ? 'admin@fintech.com' : 'user@fintech.com',
      displayName: role === 'admin' ? 'Dev Admin User' : 'Dev Standard User',
      role: role
    };
    localStorage.setItem('dev_token', mockToken);
    localStorage.setItem('dev_user', JSON.stringify(mockUser));
    setCurrentUser(mockUser);
    setUserRole(role);
  };

  const logout = () => {
    localStorage.removeItem('dev_user');
    localStorage.removeItem('dev_token');
    return signOut(auth).then(() => {
      setCurrentUser(null);
      setUserRole(null);
    });
  };

  const value = {
    currentUser,
    userRole,
    login,
    signup,
    loginWithGoogle,
    loginDevMock,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

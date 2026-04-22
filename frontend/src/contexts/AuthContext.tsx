import { createContext, useContext, useState, useCallback, type ReactNode, useEffect } from 'react';
import { ACCESS_TOKEN_KEY, USER_DATA_KEY } from '../constants';

interface User {
  userId: string;
  login: string;
}

interface AuthContextType {
   user: User | null;
   isAuthenticated: boolean;
   login: (login: string, password: string, rememberMe?: boolean) => Promise<boolean>;
   register: (login: string, password: string) => Promise<boolean>;
   logout: () => void;
   getAuthHeader: () => Record<string, string>;
   fetchUserById: (userId: string) => Promise<{ id: string; login: string } | null>;
   error: string | null;
 }

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const savedToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    const savedUser = localStorage.getItem(USER_DATA_KEY);
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
  }, []);

const login = useCallback(async (login: string, password: string): Promise<boolean> => {
  try {
    setError(null);
    
    const response = await fetch('/auth/signin', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ login, password }),
    });

    if (!response.ok) {
      setError('Invalid login or password');
      return false;
    }

    const authData = await response.json(); 
    const token = authData.accessToken;

    const meResponse = await fetch('/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!meResponse.ok) {
      setError('Failed to fetch user profile');
      return false;
    }

    const userDataFromApi = await meResponse.json();

    const userData: User = { 
      userId: userDataFromApi.id,
      login: userDataFromApi.login 
    };

    setToken(token);
    setUser(userData);
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
    localStorage.setItem(USER_DATA_KEY, JSON.stringify(userData));

    return true;
  } catch (err) {
    setError('Connection error');
    console.error('Login flow error:', err);
    return false;
  }
}, []);

  const register = useCallback(async (login: string, password: string): Promise<boolean> => {
    try {
      setError(null);
      const response = await fetch('/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ login, password }),
      });

      if (!response.ok) {
        const data = await response.json();
        setError(data.message || 'Registration failed');
        return false;
      }

      return true;
    } catch (err) {
      setError('Connection error');
      console.error('Registration error:', err);
      return false;
    }
  }, []);

  const getAuthHeader = useCallback((): Record<string, string> => {
    if (!token) return {};
    return { Authorization: `Bearer ${token}` };
  }, [token]);

  const fetchUserById = useCallback(async (userId: string): Promise<{ id: string; login: string } | null> => {
    try {
      const headers = getAuthHeader();
      const response = await fetch(`/auth/${userId}`, {
        headers: Object.keys(headers).length ? headers : undefined,
      });
      if (!response.ok) {
        return null;
      }
      const data = await response.json();
      return { id: data.id, login: data.login };
    } catch (err) {
      console.error('Fetch user error:', err);
      return null;
    }
  }, [getAuthHeader]);

const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(USER_DATA_KEY);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        getAuthHeader,
        fetchUserById,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

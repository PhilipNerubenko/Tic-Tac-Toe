import { createContext, useContext, useState, useCallback, type ReactNode, useEffect } from 'react';

interface User {
  userId: string;
  login: string;
  password: string;
}

interface AuthContextType {
   user: User | null;
   isAuthenticated: boolean;
   login: (login: string, password: string) => Promise<boolean>;
   register: (login: string, password: string) => Promise<boolean>;
   logout: () => void;
   getAuthHeader: () => Record<string, string>;
   fetchUserById: (userId: string) => Promise<{ id: string; login: string } | null>;
   error: string | null;
 }

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_KEY = 'tic_tac_toe_auth';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Загружаем сохраненные учетные данные при монтировании
  useEffect(() => {
    const savedAuth = localStorage.getItem(STORAGE_KEY);
    if (savedAuth) {
      try {
        const userData = JSON.parse(savedAuth);
        setUser(userData);
      } catch (err) {
        console.error('Failed to parse saved auth data:', err);
        localStorage.removeItem(STORAGE_KEY);
      }
    }
  }, []);

  const login = useCallback(async (login: string, password: string): Promise<boolean> => {
    try {
      setError(null);
      const credentials = btoa(`${login}:${password}`);
      const response = await fetch('/auth/signin', {
        method: 'POST',
        headers: {
          'Authorization': `Basic ${credentials}`,
        },
      });

      if (!response.ok) {
        setError('Invalid login or password');
        return false;
      }

      const data = await response.json();
      const userData = { userId: data.userId, login, password };
      setUser(userData);
      // Сохраняем в localStorage
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
      return true;
    } catch (err) {
      setError('Connection error');
      console.error('Login error:', err);
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
    if (!user) return {};
    const credentials = btoa(`${user.login}:${user.password}`);
    return { Authorization: `Basic ${credentials}` };
  }, [user]);

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
    setUser(null);
    setError(null);
    localStorage.removeItem(STORAGE_KEY);
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

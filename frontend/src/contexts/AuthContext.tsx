import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';

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
  getAuthHeader: () => { Authorization: string } | {};
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

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
      setUser({ userId: data.userId, login, password });
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

  const logout = useCallback(() => {
    setUser(null);
    setError(null);
  }, []);

  const getAuthHeader = useCallback(() => {
    if (!user) return {};
    const credentials = btoa(`${user.login}:${user.password}`);
    return { Authorization: `Basic ${credentials}` };
  }, [user]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        getAuthHeader,
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

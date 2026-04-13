import { createContext, useContext, useState, useCallback, type ReactNode, useEffect, useRef } from 'react';
import { STORAGE_KEY, PASSWORD_STORAGE_KEY } from '../constants';

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



// RFC 7617: кодируем учетные данные Basic Auth с поддержкой UTF-8
const encodeBasicCredentials = (login: string, password: string) => {
  const bytes = new TextEncoder().encode(`${login}:${password}`);
  let binary = '';
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
};

// Используем sessionStorage для пароля (очищается при закрытии вкладки)
// и localStorage для userId/login (с опцией "запомнить")
function getStoredPassword(): string | null {
  return sessionStorage.getItem(PASSWORD_STORAGE_KEY);
}

function setStoredPassword(password: string): void {
  sessionStorage.setItem(PASSWORD_STORAGE_KEY, password);
}

function clearStoredPassword(): void {
  sessionStorage.removeItem(PASSWORD_STORAGE_KEY);
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);
  const passwordRef = useRef<string | null>(null);

  // Загружаем сохраненные учетные данные при монтировании
  useEffect(() => {
    const savedAuth = localStorage.getItem(STORAGE_KEY);
    if (savedAuth) {
      try {
        const userData = JSON.parse(savedAuth) as User;
        const savedPassword = getStoredPassword();
      if (savedPassword) {
        passwordRef.current = savedPassword;
        setTimeout(() => setUser(userData), 0);
      } else {
          // Пароль не сохранён в sessionStorage — очищаем данные
          localStorage.removeItem(STORAGE_KEY);
        }
      } catch (err) {
        console.error('Failed to parse saved auth data:', err);
        localStorage.removeItem(STORAGE_KEY);
      }
    }
  }, []);

  const login = useCallback(async (login: string, password: string): Promise<boolean> => {
    try {
      setError(null);
      const credentials = encodeBasicCredentials(login, password);
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
      const userData: User = { userId: data.userId, login };
      setUser(userData);
      passwordRef.current = password;
      // Сохраняем userId и login в localStorage
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
      // Сохраняем пароль в sessionStorage (очищается при закрытии вкладки)
      setStoredPassword(password);
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
    if (!user || !passwordRef.current) return {};
    const credentials = encodeBasicCredentials(user.login, passwordRef.current);
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
    passwordRef.current = null;
    localStorage.removeItem(STORAGE_KEY);
    clearStoredPassword();
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

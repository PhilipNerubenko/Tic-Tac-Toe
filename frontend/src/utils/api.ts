import { ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, USER_DATA_KEY, TOKEN_EXPIRY_KEY } from '../constants';

let isRefreshing = false;
let pendingRequests: Array<{ resolve: (token: string) => void; reject: (error?: any) => void }> =
  [];

export async function authorizedFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

  const shouldSetContentType = options.body !== undefined;
  const headers: Record<string, string> = {};

  if (shouldSetContentType) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Merge with any additional headers from options
  if (options.headers) {
    Object.assign(headers, options.headers as Record<string, string>);
  }

  const response = await fetch(url, { ...options, headers });

  if (response.status === 401 && refreshToken) {
    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const refreshResponse = await fetch('/auth/refresh/access', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        });

        if (refreshResponse.ok) {
          const { accessToken, refreshToken: newRefreshToken } = await refreshResponse.json();
          localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
          if (newRefreshToken) {
            localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
          }
          isRefreshing = false;

          // Retry pending requests
          pendingRequests.forEach((cb) => cb.resolve(accessToken));
          pendingRequests = [];

          // Retry original request with new token
          const newHeaders: Record<string, string> = { ...headers };
          newHeaders['Authorization'] = `Bearer ${accessToken}`;
          return fetch(url, {
            ...options,
            headers: newHeaders,
          });
        } else {
          logout();
          isRefreshing = false;
          const error = new Error('Refresh token invalid');
          pendingRequests.forEach((cb) => cb.reject(error));
          pendingRequests = [];
          return response;
        }
      } catch (error) {
        console.error('Token refresh failed:', error);
        logout();
        isRefreshing = false;
        pendingRequests.forEach((cb) => cb.reject(error));
        pendingRequests = [];
        return response;
      }
    } else {
      // Queue request until refresh completes
      return new Promise((resolve, reject) => {
        pendingRequests.push({
          resolve: (newToken: string) => {
            const newHeaders: Record<string, string> = { ...headers };
            newHeaders['Authorization'] = `Bearer ${newToken}`;
            resolve(
              fetch(url, {
                ...options,
                headers: newHeaders,
              })
            );
          },
          reject,
        });
      });
    }
  }

  return response;
}

export function logout() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_DATA_KEY);
  localStorage.removeItem(TOKEN_EXPIRY_KEY);
  window.location.href = '/';
}

let isRefreshing = false;
let pendingRequests: Array<(token: string) => void> = [];

export async function authorizedFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem('access_token');
  const refreshToken = localStorage.getItem('refresh_token');

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
          localStorage.setItem('access_token', accessToken);
          if (newRefreshToken) {
            localStorage.setItem('refresh_token', newRefreshToken);
          }
          isRefreshing = false;

          // Retry pending requests
          pendingRequests.forEach(callback => callback(accessToken));
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
          return response;
        }
      } catch (error) {
        console.error('Token refresh failed:', error);
        logout();
        return response;
      }
    } else {
      // Queue request until refresh completes
      return new Promise((resolve) => {
        pendingRequests.push((newToken: string) => {
          const newHeaders: Record<string, string> = { ...headers };
          newHeaders['Authorization'] = `Bearer ${newToken}`;
          resolve(fetch(url, {
            ...options,
            headers: newHeaders,
          }));
        });
      });
    }
  }

  return response;
}

export function logout() {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('user_data');
  localStorage.removeItem('token_expiry');
  window.location.href = '/';
}

import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';

interface UserInfo {
  id: string;
  login: string;
}

interface UserProfileProps {
  onClose: () => void;
}

export function UserProfile({ onClose }: UserProfileProps) {
  const { user, fetchUserById } = useAuth();
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadUserInfo = async () => {
      if (user?.userId) {
        setLoading(true);
        setError(null);
        const info = await fetchUserById(user.userId);
        if (info) {
          setUserInfo(info);
        } else {
          setError('Failed to load user information');
        }
        setLoading(false);
      }
    };

    loadUserInfo();
  }, [user?.userId, fetchUserById]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>
          &times;
        </button>
        <h2>User Profile</h2>
        
        {loading && <p>Loading...</p>}
        
        {error && <p className="error-message">{error}</p>}
        
        {userInfo && !loading && !error && (
          <div className="profile-info">
            <div className="profile-field">
              <strong>User ID:</strong>
              <span>{userInfo.id}</span>
            </div>
            <div className="profile-field">
              <strong>Username:</strong>
              <span>{userInfo.login}</span>
            </div>
          </div>
        )}
        
        {!user && !loading && !error && (
          <p>No user information available</p>
        )}
      </div>
    </div>
  );
}

import { Link } from 'react-router-dom';
import { useUser } from '../../contexts/UserContext';
import { logout } from '../../api/auth';
import styles from './Header.module.css';

const Header = () => {
  const { user, setUser } = useUser();
  const handleLogout = async () => {
    try {
      await logout();
      setUser(null);
      window.location.assign('/');
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Logout failed');
    }
  };
  return (
    <header className={styles.header}>
      <Link to="/" className={styles.title}>TrafficG</Link>
      <nav>
        <Link to="/showList">Performances</Link>
      </nav>
      {user ? (
        <div className={styles.welcome}>{user.name} <button onClick={() => void handleLogout()}>Sign out</button></div>
      ) : (
        <Link to="/login">Sign in</Link>
      )}
    </header>
)};

export default Header;

import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import styles from "../styles/Navbar.module.css";

function Navbar() {
    const { user, logout } = useAuth();

    return (
        <nav className={styles.navbar}>
            <div className={styles.container}>
                <Link to="/trips" className={styles.brand}>
                    <svg className={styles.brandIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className={styles.brandText}>Our Journey</span>
                </Link>

                <div className={styles.rightSection}>
                    {user && (
                        <>
                            <div className={styles.userInfo}>
                                <div className={styles.avatar}>
                                    {user.name.charAt(0).toUpperCase()}
                                </div>
                                <span className={styles.userName}>
                                    Hello, {user.name}
                                </span>
                            </div>

                            <button 
                                onClick={logout}
                                className={styles.logoutButton}
                            >
                                <svg className={styles.logoutIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                                </svg>
                                Log out
                            </button>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Navbar;
import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useAuth } from "../context/useAuth";
import styles from "../styles/Navbar.module.css";

function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const menuRef = useRef<HTMLDivElement>(null);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [failedImageUrl, setFailedImageUrl] = useState<string | null>(null);

    useEffect(() => {
        function handlePointerDown(event: PointerEvent) {
            if (!menuRef.current?.contains(event.target as Node)) {
                setIsMenuOpen(false);
            }
        }

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setIsMenuOpen(false);
            }
        }

        document.addEventListener("pointerdown", handlePointerDown);
        document.addEventListener("keydown", handleKeyDown);

        return () => {
            document.removeEventListener("pointerdown", handlePointerDown);
            document.removeEventListener("keydown", handleKeyDown);
        };
    }, []);

    function handleLogout() {
        setIsMenuOpen(false);
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <nav className={styles.navbar} aria-label="Main navigation">
            <div className={styles.container}>
                <Link to="/trips" className={styles.brand}>
                    <svg className={styles.brandIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className={styles.brandText}>Our Journey</span>
                </Link>

                {user && (
                    <div className={styles.profileMenu} ref={menuRef}>
                        <button
                            type="button"
                            className={styles.profileTrigger}
                            onClick={() => setIsMenuOpen((open) => !open)}
                            aria-expanded={isMenuOpen}
                            aria-haspopup="menu"
                            aria-controls="profile-menu"
                        >
                            <span className={styles.avatar}>
                                {user.profilePicture
                                && failedImageUrl !== user.profilePicture ? (
                                    <img
                                        src={user.profilePicture}
                                        alt=""
                                        className={styles.avatarImage}
                                        onError={() =>
                                            setFailedImageUrl(user.profilePicture)
                                        }
                                    />
                                ) : (
                                    user.name.charAt(0).toUpperCase()
                                )}
                            </span>
                            <span className={styles.userName}>{user.name}</span>
                            <svg className={`${styles.chevron} ${isMenuOpen ? styles.chevronOpen : ""}`} viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                                <path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.17l3.71-3.94a.75.75 0 111.08 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z" clipRule="evenodd" />
                            </svg>
                        </button>

                        {isMenuOpen && (
                            <div id="profile-menu" className={styles.dropdown} role="menu">
                                <div className={styles.dropdownHeader}>
                                    <span className={styles.dropdownName}>{user.name}</span>
                                    <span className={styles.dropdownEmail}>{user.email}</span>
                                </div>
                                <div className={styles.dropdownDivider} />
                                <Link
                                    to="/account-settings"
                                    className={styles.dropdownItem}
                                    role="menuitem"
                                    onClick={() => setIsMenuOpen(false)}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                                        <circle cx="12" cy="12" r="3" strokeWidth={1.8} />
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth={1.8}
                                            d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 11-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 11-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 11-2.83-2.83l.06-.06A1.65 1.65 0 004.6 15a1.65 1.65 0 00-1.51-1H3a2 2 0 110-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06A1.65 1.65 0 009 4.6a1.65 1.65 0 001-1.51V3a2 2 0 114 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06A1.65 1.65 0 0019.4 9c.12.6.6 1.06 1.2 1.09H21a2 2 0 110 4h-.09A1.65 1.65 0 0019.4 15z"
                                        />
                                    </svg>
                                    Account settings
                                </Link>
                                <button type="button" className={`${styles.dropdownItem} ${styles.logoutItem}`} role="menuitem" onClick={handleLogout}>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.8} d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M18 15l3-3m0 0l-3-3m3 3H9" />
                                    </svg>
                                    Log out
                                </button>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </nav>
    );
}

export default Navbar;

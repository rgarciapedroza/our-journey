import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar() {
    const { user, logout } = useAuth();

    return (
        <nav>
            <Link to="/trips">
                Our Journey
            </Link>

            <div>
                {user && (
                    <>
                        <span>
                            Hello, {user.name}
                        </span>

                        <button onClick={logout}>
                            Log out
                        </button>
                    </>
                )}
            </div>
        </nav>
    );
}

export default Navbar;
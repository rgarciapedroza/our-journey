import {
    createContext,
    useContext,
    useEffect,
    useState,
    type ReactNode,
} from "react";

import {
    getCurrentUser,
    getToken,
    login as loginApi,
    logout as logoutApi,
} from "../api/auth";

import type {
    LoginRequest,
    LoginResponse,
    User,
} from "../types/auth";

interface AuthContextType {
    user: User | null;
    loading: boolean;
    login: (request: LoginRequest) => Promise<LoginResponse>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadUser() {
            const token = getToken();

            if (!token) {
                setLoading(false);
                return;
            }

            try {
                const currentUser = await getCurrentUser();
                setUser(currentUser);
            } catch (error) {
                console.error("Could not load current user:", error);
                logoutApi();
                setUser(null);
            } finally {
                setLoading(false);
            }
        }

        loadUser();
    }, []);

    async function login(request: LoginRequest) {
        const response = await loginApi(request);

        setUser({
            id: response.id,
            name: response.name,
            email: response.email,
            profilePicture: response.profilePicture,
        });

        return response;
    }

    function logout() {
        logoutApi();
        setUser(null);
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return context;
}
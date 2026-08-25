import { createContext } from "react";

import type {
    LoginRequest,
    LoginResponse,
    User,
} from "../types/auth";

export interface AuthContextValue {
    user: User | null;
    loading: boolean;
    login: (request: LoginRequest) => Promise<LoginResponse>;
    logout: () => void;
    updateUser: (user: User) => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(
    undefined
);

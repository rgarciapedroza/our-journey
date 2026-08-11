import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";

import { register } from "../api/auth";

function RegisterPage() {
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError("");

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        try {
            setLoading(true);

            await register({
                name,
                email,
                password,
                confirmPassword,
            });

            navigate("/login");
        } catch (error) {
            setError("Could not create account");
        } finally {
            setLoading(false);
        }
    }

    return (
        <main>
            <h1>Create your account</h1>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="name">Name</label>

                    <input
                        id="name"
                        type="text"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="email">Email</label>

                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="password">Password</label>

                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="confirmPassword">
                        Confirm password
                    </label>

                    <input
                        id="confirmPassword"
                        type="password"
                        value={confirmPassword}
                        onChange={(event) =>
                            setConfirmPassword(event.target.value)
                        }
                        required
                    />
                </div>

                {error && <p>{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading ? "Creating account..." : "Create account"}
                </button>
            </form>

            <p>
                Already have an account?{" "}
                <button
                    type="button"
                    onClick={() => navigate("/login")}
                >
                    Log in
                </button>
            </p>
        </main>
    );
}

export default RegisterPage;
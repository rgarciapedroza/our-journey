import { BrowserRouter, Route, Routes } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import TripsPage from "./pages/TripsPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import TripDetailPage from "./pages/TripDetailPage";
import CreateTripPage from "./pages/CreateTripPage";
import EditTripPage from "./pages/EditTripPage";
import AccountSettingsPage from "./pages/AccountSettingsPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/account-settings" element={<AccountSettingsPage />} />

                <Route element={<ProtectedRoute />}>
                    <Route element={<Layout />}>
                        <Route path="/trips" element={<TripsPage />} />
                        <Route path="/trips/:id" element={<TripDetailPage />} />
                        <Route path="/trips/new" element={<CreateTripPage />} />
                        <Route path="/trips/:id/edit" element={<EditTripPage />} />
                    </Route>
                </Route>

                <Route
                    path="/"
                    element={<h1>Our Journey</h1>}
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;

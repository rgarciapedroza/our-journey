import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import TripsPage from "./pages/TripsPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import TripDetailPage from "./pages/TripDetailPage";
import CreateTripPage from "./pages/CreateTripPage";
import EditTripPage from "./pages/EditTripPage";
import AccountSettingsPage from "./pages/AccountSettingsPage";
import TripGalleryPage from "./components/TripPhotoGallery";
import TripItineraryPage from "./pages/TripItineraryPage";
import { useAuth } from "./context/useAuth";

function HomeRedirect() {
    const { user, loading } = useAuth();

    if (loading) {
        return <p>Loading...</p>;
    }

    return (
        <Navigate
            to={user ? "/trips" : "/register"}
            replace
        />
    );
}

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
                        <Route path="/trips/:id/gallery" element={<TripGalleryPage />} />
                        <Route path="/trips/:id/itinerary" element={<TripItineraryPage />} />
                    </Route>
                </Route>

                <Route path="/" element={<HomeRedirect />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;

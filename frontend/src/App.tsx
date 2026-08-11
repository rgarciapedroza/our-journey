import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import TripsPage from "./pages/TripsPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<h1>Our Journey</h1>} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/trips" element={<TripsPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
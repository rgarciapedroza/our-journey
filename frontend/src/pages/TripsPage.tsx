import { useEffect, useState } from "react";
import { getTrips } from "../api/trips";
import type { Trip } from "../types/trip";

function TripsPage() {
    const [trips, setTrips] = useState<Trip[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadTrips() {
            try {
                setLoading(true);

                const data = await getTrips();

                setTrips(data);
            } catch (error) {
                console.error(error);
                setError("Could not load trips.");
            } finally {
                setLoading(false);
            }
        }

        loadTrips();
    }, []);

    if (loading) {
        return <p>Loading trips...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <main>
            <h1>Our Trips</h1>

            {trips.length === 0 ? (
                <p>No trips found.</p>
            ) : (
                <div>
                    {trips.map((trip) => (
                        <article key={trip.id}>
                            <h2>{trip.name}</h2>

                            <p>{trip.destination}</p>

                            <p>{trip.description}</p>

                            <p>
                                {trip.startDate} - {trip.endDate}
                            </p>
                        </article>
                    ))}
                </div>
            )}
        </main>
    );
}

export default TripsPage;
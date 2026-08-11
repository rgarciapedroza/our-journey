import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import { getTrip } from "../api/trips";
import type { Trip } from "../types/trip";

function TripDetailPage() {
    const { id } = useParams<{ id: string }>();

    const [trip, setTrip] = useState<Trip | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadTrip() {
            if (!id) {
                setError("Trip not found.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);

                const data = await getTrip(Number(id));

                setTrip(data);
            } catch (error) {
                console.error(error);
                setError("Could not load this trip.");
            } finally {
                setLoading(false);
            }
        }

        loadTrip();
    }, [id]);

    if (loading) {
        return <p>Loading trip...</p>;
    }

    if (error || !trip) {
        return (
            <main>
                <h1>Trip not found</h1>
                <p>{error}</p>

                <Link to="/trips">
                    Back to trips
                </Link>
            </main>
        );
    }

    return (
        <main>
            <Link to="/trips">
                ← Back to trips
            </Link>

            <article>
                <h1>{trip.name}</h1>

                <p>{trip.destination}</p>

                <p>{trip.description}</p>

                <p>
                    {trip.startDate} - {trip.endDate}
                </p>

                {trip.coverImage && (
                    <img
                        src={trip.coverImage}
                        alt={trip.name}
                    />
                )}
            </article>
        </main>
    );
}

export default TripDetailPage;
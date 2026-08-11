import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getTrips } from "../api/trips";
import type { Trip } from "../types/trip";
import styles from "../styles/TripsPage.module.css";

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
        return (
            <main className={styles.main}>
                <div className={styles.loadingContainer}>
                    <div className={styles.spinner}></div>
                    <p className={styles.loadingText}>Loading trips...</p>
                </div>
            </main>
        );
    }

    if (error) {
        return (
            <main className={styles.main}>
                <div className={styles.errorContainer}>
                    <p className={styles.errorText}>{error}</p>
                </div>
            </main>
        );
    }

    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <div className={styles.header}>
                    <h1 className={styles.title}>Our Trips</h1>
                    <p className={styles.subtitle}>Discover amazing destinations</p>
                </div>

                {trips.length === 0 ? (
                    <div className={styles.emptyContainer}>
                        <p className={styles.emptyText}>No trips found.</p>
                    </div>
                ) : (
                    <div className={styles.grid}>
                        {trips.map((trip) => (
                            <article key={trip.id}>
                                <h2>
                                    <Link to={`/trips/${trip.id}`}>
                                        {trip.name}
                                    </Link>
                                </h2>

                                <p>{trip.destination}</p>

                                <p>{trip.description}</p>

                                <p>
                                    {trip.startDate} - {trip.endDate}
                                </p>
                            </article>
                        ))}
                    </div>
                )}
            </div>
        </main>
    );
}

export default TripsPage;
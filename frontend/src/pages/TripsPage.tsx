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
                            <article key={trip.id} className={styles.card}>
                                {trip.coverImage && (
                                    <div className={styles.cardImageContainer}>
                                        <img 
                                            src={trip.coverImage} 
                                            alt={trip.name}
                                            className={styles.cardImage}
                                        />
                                        <div className={styles.cardImageOverlay}>
                                            <span className={styles.destinationBadge}>
                                                {trip.destination}
                                            </span>
                                        </div>
                                    </div>
                                )}

                                <div className={styles.cardBody}>
                                    <div className={styles.cardHeader}>
                                        <h2 className={styles.cardTitle}>
                                            <Link to={`/trips/${trip.id}`} className={styles.cardLink}>
                                                {trip.name}
                                            </Link>
                                        </h2>
                                        
                                        {!trip.coverImage && (
                                            <span className={styles.destination}>
                                                {trip.destination}
                                            </span>
                                        )}
                                    </div>

                                    <p className={styles.description}>
                                        {trip.description.length > 120 
                                            ? `${trip.description.substring(0, 120)}...` 
                                            : trip.description}
                                    </p>

                                    <div className={styles.cardFooter}>
                                        <div className={styles.dateContainer}>
                                            <svg className={styles.dateIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                            </svg>
                                            <span className={styles.date}>
                                                {trip.startDate} - {trip.endDate}
                                            </span>
                                        </div>
                                        
                                        <Link to={`/trips/${trip.id}`} className={styles.detailsButton}>
                                            View Details
                                            <svg className={styles.arrowIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                            </svg>
                                        </Link>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </div>
        </main>
    );
}

export default TripsPage;
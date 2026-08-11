import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getTrip } from "../api/trips";
import { deleteTrip } from "../api/trips";
import type { Trip } from "../types/trip";
import styles from "../styles/TripDetailPage.module.css";

function TripDetailPage() {
    const { id } = useParams<{ id: string }>();

    const [deleting, setDeleting] = useState(false);

    const navigate = useNavigate();
    const [showConfirm, setShowConfirm] = useState(false);

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
        return (
            <main className={styles.main}>
                <div className={styles.loadingContainer}>
                    <div className={styles.spinner}></div>
                    <p className={styles.loadingText}>Loading trip details...</p>
                </div>
            </main>
        );
    }

    if (error || !trip) {
        return (
            <main className={styles.main}>
                <div className={styles.errorContainer}>
                    <div className={styles.errorContent}>
                        <svg className={styles.errorIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <h1 className={styles.errorTitle}>Trip not found</h1>
                        <p className={styles.errorMessage}>{error || "The trip you're looking for doesn't exist."}</p>
                        <Link to="/trips" className={styles.backButton}>
                            <svg className={styles.backIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                            </svg>
                            Back to trips
                        </Link>
                    </div>
                </div>
            </main>
        );
    }

    async function handleDelete() {
        if (!showConfirm) {
            setShowConfirm(true);
            return;
        }

        setDeleting(true);
        try {
            if (!id) return;
            await deleteTrip(Number(id));
            navigate("/trips");
        } catch (error) {
            console.error(error);
            setError("Could not delete the trip.");
        } finally {
            setDeleting(false);
            setShowConfirm(false);
        }
    }

    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <Link to="/trips" className={styles.backLink}>
                    <svg className={styles.backIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to trips
                </Link>

                <article className={styles.article}>
                    {trip.coverImage && (
                        <div className={styles.imageContainer}>
                            <img
                                src={trip.coverImage}
                                alt={trip.name}
                                className={styles.coverImage}
                            />
                            <div className={styles.imageOverlay}>
                                <span className={styles.destinationBadge}>
                                    {trip.destination}
                                </span>
                            </div>
                        </div>
                    )}

                    <div className={styles.content}>
                        <h1 className={styles.title}>{trip.name}</h1>

                        {!trip.coverImage && (
                            <div className={styles.destinationTag}>
                                <svg className={styles.locationIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                </svg>
                                <span>{trip.destination}</span>
                            </div>
                        )}

                        <div className={styles.tripActions}>
                            <Link to={`/trips/${trip.id}/edit`} className={styles.editButton}>
                                <svg className={styles.actionIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                                Edit Trip
                            </Link>
                            
                            <button
                                type="button"
                                onClick={handleDelete}
                                disabled={deleting}
                                className={`${styles.deleteButton} ${showConfirm ? styles.deleteButtonConfirm : ''}`}
                            >
                                {deleting ? (
                                    <>
                                        <svg className={styles.spinnerSmall} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle className={styles.spinnerCircle} cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className={styles.spinnerPath} fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                        Deleting...
                                    </>
                                ) : showConfirm ? (
                                    "Confirm Delete"
                                ) : (
                                    <>
                                        <svg className={styles.actionIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                        Delete Trip
                                    </>
                                )}
                            </button>
                        </div>

                        <div className={styles.dates}>
                            <svg className={styles.dateIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                            <span>{trip.startDate} — {trip.endDate}</span>
                        </div>

                        <div className={styles.divider}></div>

                        <div className={styles.descriptionSection}>
                            <h2 className={styles.sectionTitle}>About this trip</h2>
                            <p className={styles.description}>{trip.description}</p>
                        </div>

                        <div className={styles.actions}>
                            <button className={styles.bookButton}>
                                <svg className={styles.bookIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                </svg>
                                Itinerary
                            </button>
                            
                            <Link to="/trips" className={styles.secondaryButton}>
                                View all trips
                            </Link>
                        </div>
                    </div>
                </article>
            </div>
        </main>
    );
}

export default TripDetailPage;
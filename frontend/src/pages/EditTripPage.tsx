import { useEffect, useState, type FormEvent } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { getTrip, updateTrip } from "../api/trips";
import type { TripRequest } from "../types/trip";
import styles from "../styles/EditTripPage.module.css";

function EditTripPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [form, setForm] = useState<TripRequest>({
        name: "",
        description: "",
        destination: "",
        startDate: "",
        endDate: "",
        coverImage: "",
    });

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
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
                const trip = await getTrip(Number(id));
                setForm({
                    name: trip.name,
                    description: trip.description || "",
                    destination: trip.destination,
                    startDate: trip.startDate,
                    endDate: trip.endDate,
                    coverImage: trip.coverImage || "",
                });
            } catch (error) {
                console.error(error);
                setError("Could not load trip details.");
            } finally {
                setLoading(false);
            }
        }

        loadTrip();
    }, [id]);

    function handleChange(
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) {
        const { name, value } = event.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null);
        setSaving(true);

        try {
            if (!id) {
                setError("Trip ID is missing.");
                return;
            }
            await updateTrip(Number(id), form);
            navigate(`/trips/${id}`);
        } catch (error) {
            console.error(error);
            setError("Could not update the trip.");
        } finally {
            setSaving(false);
        }
    }

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

    if (error) {
        return (
            <main className={styles.main}>
                <div className={styles.errorContainer}>
                    <p className={styles.errorText}>{error}</p>
                    <Link to="/trips" className={styles.backButton}>
                        Back to trips
                    </Link>
                </div>
            </main>
        );
    }

    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <Link to={`/trips/${id}`} className={styles.backLink}>
                    <svg className={styles.backIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to trip details
                </Link>

                <div className={styles.header}>
                    <h1 className={styles.title}>Edit Trip</h1>
                    <p className={styles.subtitle}>Update your trip information</p>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label htmlFor="name" className={styles.label}>
                            Trip Name
                            <span className={styles.required}>*</span>
                        </label>
                        <input
                            id="name"
                            name="name"
                            type="text"
                            value={form.name}
                            onChange={handleChange}
                            required
                            minLength={3}
                            maxLength={100}
                            className={styles.input}
                            placeholder="e.g., Summer Adventure in Bali"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="destination" className={styles.label}>
                            Destination
                            <span className={styles.required}>*</span>
                        </label>
                        <input
                            id="destination"
                            name="destination"
                            type="text"
                            value={form.destination}
                            onChange={handleChange}
                            required
                            className={styles.input}
                            placeholder="e.g., Bali, Indonesia"
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="description" className={styles.label}>
                            Description
                        </label>
                        <textarea
                            id="description"
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            maxLength={1000}
                            className={styles.textarea}
                            placeholder="Describe your trip..."
                            rows={4}
                        />
                    </div>

                    <div className={styles.row}>
                        <div className={styles.formGroup}>
                            <label htmlFor="startDate" className={styles.label}>
                                Start Date
                                <span className={styles.required}>*</span>
                            </label>
                            <input
                                id="startDate"
                                name="startDate"
                                type="date"
                                value={form.startDate}
                                onChange={handleChange}
                                required
                                className={styles.input}
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="endDate" className={styles.label}>
                                End Date
                                <span className={styles.required}>*</span>
                            </label>
                            <input
                                id="endDate"
                                name="endDate"
                                type="date"
                                value={form.endDate}
                                onChange={handleChange}
                                required
                                className={styles.input}
                            />
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="coverImage" className={styles.label}>
                            Cover Image URL
                        </label>
                        <input
                            id="coverImage"
                            name="coverImage"
                            type="text"
                            value={form.coverImage}
                            onChange={handleChange}
                            placeholder="https://example.com/image.jpg"
                            className={styles.input}
                        />
                        {form.coverImage && (
                            <div className={styles.imagePreview}>
                                <img 
                                    src={form.coverImage} 
                                    alt="Cover preview"
                                    className={styles.previewImage}
                                    onError={(e) => {
                                        (e.target as HTMLImageElement).style.display = 'none';
                                    }}
                                />
                            </div>
                        )}
                    </div>

                    {error && (
                        <div className={styles.errorContainer}>
                            <p className={styles.errorMessage}>{error}</p>
                        </div>
                    )}

                    <div className={styles.actions}>
                        <button
                            type="button"
                            onClick={() => navigate(`/trips/${id}`)}
                            className={styles.cancelButton}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={saving}
                            className={`${styles.submitButton} ${saving ? styles.buttonLoading : ''}`}
                        >
                            {saving ? (
                                <span className={styles.buttonContent}>
                                    <svg className={styles.spinner} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className={styles.spinnerCircle} cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className={styles.spinnerPath} fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Saving...
                                </span>
                            ) : (
                                "Save Changes"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default EditTripPage;
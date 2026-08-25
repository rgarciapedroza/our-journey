import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { deleteTripCover, getTrip, updateTrip, uploadTripCover } from "../api/trips";
import type { TripRequest } from "../types/trip";
import styles from "../styles/EditTripPage.module.css";

function EditTripPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const coverInputRef = useRef<HTMLInputElement>(null);

    const [form, setForm] = useState<TripRequest>({
        name: "",
        description: "",
        destination: "",
        startDate: "",
        endDate: "",
    });

    const [currentCover, setCurrentCover] = useState<string | null>(null);
    const [coverFile, setCoverFile] = useState<File | null>(null);
    const [coverPreview, setCoverPreview] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadTrip() {
            if (!id) {
                setLoadError("Trip not found.");
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
                });
                setCurrentCover(trip.coverImage);
            } catch (error) {
                console.error(error);
                setLoadError("Could not load trip details.");
            } finally {
                setLoading(false);
            }
        }

        loadTrip();
    }, [id]);

    useEffect(() => {
        return () => {
            if (coverPreview) {
                URL.revokeObjectURL(coverPreview);
            }
        };
    }, [coverPreview]);

    function handleChange(
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ) {
        const { name, value } = event.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    }

    function handleCoverChange(event: ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];

        if (!file) {
            return;
        }

        if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
            setError("Choose a JPEG, PNG or WebP image.");
            event.target.value = "";
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            setError("The cover image must not exceed 5 MB.");
            event.target.value = "";
            return;
        }

        setError(null);
        setCoverFile(file);
        setCoverPreview(URL.createObjectURL(file));
    }

    async function handleRemoveCover() {
        if (coverFile) {
            setCoverFile(null);
            setCoverPreview(null);

            if (coverInputRef.current) {
                coverInputRef.current.value = "";
            }
            return;
        }

        if (!id || !currentCover) {
            return;
        }

        try {
            setSaving(true);
            setError(null);
            await deleteTripCover(Number(id));
            setCurrentCover(null);
        } catch (error) {
            console.error(error);
            setError("Could not remove the cover image.");
        } finally {
            setSaving(false);
        }
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

            if (coverFile) {
                await uploadTripCover(Number(id), coverFile);
            }

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

    if (loadError) {
        return (
            <main className={styles.main}>
                <div className={styles.errorContainer}>
                    <p className={styles.errorText}>{loadError}</p>
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
                            Cover image
                        </label>
                        <input
                            id="coverImage"
                            ref={coverInputRef}
                            type="file"
                            accept="image/jpeg,image/png,image/webp"
                            onChange={handleCoverChange}
                            className={styles.fileInput}
                        />
                        <span className={styles.fileHint}>
                            JPEG, PNG or WebP. Maximum size: 5 MB.
                        </span>
                        {(coverPreview || currentCover) && (
                            <div className={styles.imagePreview}>
                                <img
                                    src={coverPreview ?? currentCover ?? ""}
                                    alt="Cover preview"
                                    className={styles.previewImage}
                                />
                                <button
                                    type="button"
                                    onClick={handleRemoveCover}
                                    disabled={saving}
                                    className={styles.removeImageButton}
                                >
                                    Remove cover
                                </button>
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

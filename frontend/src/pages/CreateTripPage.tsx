import { type ChangeEvent, type FormEvent, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createTrip, deleteTrip, uploadTripCover } from "../api/trips";
import type { TripRequest } from "../types/trip";
import styles from "../styles/CreateTripPage.module.css";

function CreateTripPage() {
    const navigate = useNavigate();
    const coverInputRef = useRef<HTMLInputElement>(null);

    const [form, setForm] = useState<TripRequest>({
        name: "",
        description: "",
        destination: "",
        startDate: "",
        endDate: "",
    });

    const [coverFile, setCoverFile] = useState<File | null>(null);
    const [coverPreview, setCoverPreview] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        return () => {
            if (coverPreview) {
                URL.revokeObjectURL(coverPreview);
            }
        };
    }, [coverPreview]);

    function handleChange(
        event: React.ChangeEvent<
            HTMLInputElement | HTMLTextAreaElement
        >
    ) {
        const { name, value } = event.target;

        setForm((previous) => ({
            ...previous,
            [name]: value,
        }));
    }

    function handleCoverChange(event: ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];

        if (!file) {
            return;
        }

        const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

        if (!allowedTypes.includes(file.type)) {
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

    function clearCoverSelection() {
        setCoverFile(null);
        setCoverPreview(null);

        if (coverInputRef.current) {
            coverInputRef.current.value = "";
        }
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    ) {
        event.preventDefault();

        setError(null);
        setLoading(true);

        try {
            const createdTrip = await createTrip(form);

            if (coverFile) {
                try {
                    await uploadTripCover(createdTrip.id, coverFile);
                } catch (uploadError) {
                    try {
                        await deleteTrip(createdTrip.id);
                    } catch (cleanupError) {
                        console.error("Could not clean up incomplete trip", cleanupError);
                    }
                    throw uploadError;
                }
            }

            navigate(`/trips/${createdTrip.id}`);
        } catch (error) {
            console.error(error);
            setError("Could not create the trip.");
        } finally {
            setLoading(false);
        }
    }

    // Calcular longitudes para evitar undefined
    const nameLength = form.name?.length ?? 0;
    const descriptionLength = form.description?.length ?? 0;

    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <button
                    type="button"
                    onClick={() => navigate("/trips")}
                    className={styles.backButton}
                >
                    <svg className={styles.backIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to trips
                </button>

                <div className={styles.header}>
                    <h1 className={styles.title}>Create a New Trip</h1>
                    <p className={styles.subtitle}>Plan your next adventure</p>
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
                            value={form.name || ""}
                            onChange={handleChange}
                            required
                            minLength={3}
                            maxLength={100}
                            className={styles.input}
                            placeholder="e.g., Summer Adventure in Bali"
                        />
                        <span className={styles.hint}>
                            {nameLength}/100 characters
                        </span>
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
                            value={form.destination || ""}
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
                            value={form.description || ""}
                            onChange={handleChange}
                            maxLength={1000}
                            className={styles.textarea}
                            placeholder="Describe your trip..."
                            rows={4}
                        />
                        <span className={styles.hint}>
                            {descriptionLength}/1000 characters
                        </span>
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
                                value={form.startDate || ""}
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
                                value={form.endDate || ""}
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
                        {coverPreview && (
                            <div className={styles.imagePreview}>
                                <img
                                    src={coverPreview}
                                    alt="Cover preview"
                                    className={styles.previewImage}
                                />
                                <button
                                    type="button"
                                    onClick={clearCoverSelection}
                                    className={styles.removeImageButton}
                                >
                                    Remove selected image
                                </button>
                            </div>
                        )}
                    </div>

                    {error && (
                        <div className={styles.errorContainer}>
                            <svg className={styles.errorIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                            <p className={styles.errorMessage}>{error}</p>
                        </div>
                    )}

                    <div className={styles.actions}>
                        <button
                            type="button"
                            onClick={() => navigate("/trips")}
                            className={styles.cancelButton}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className={`${styles.submitButton} ${loading ? styles.buttonLoading : ''}`}
                        >
                            {loading ? (
                                <span className={styles.buttonContent}>
                                    <svg className={styles.spinner} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className={styles.spinnerCircle} cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className={styles.spinnerPath} fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Creating...
                                </span>
                            ) : (
                                "Create Trip"
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default CreateTripPage;

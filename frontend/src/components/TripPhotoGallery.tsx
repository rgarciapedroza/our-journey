import { useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getTrip } from "../api/trips";
import { getTripPhotos, uploadTripPhoto, deletePhoto } from "../api/tripPhoto";
import type { Trip } from "../types/trip";
import type { TripPhoto } from "../types/tripPhoto";
import PhotoModal from "../components/PhotoModal";
import styles from "../styles/TripPhotoGallery.module.css";
import { useAuth } from "../context/AuthContext";

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_FILE_TYPES = ["image/jpeg", "image/png", "image/webp"];

function TripGalleryPage() {
    const { id } = useParams<{ id: string }>();
    const { user } = useAuth();

    const fileInputRef = useRef<HTMLInputElement>(null);

    const [trip, setTrip] = useState<Trip | null>(null);
    const [photos, setPhotos] = useState<TripPhoto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Upload state
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [caption, setCaption] = useState("");
    const [uploading, setUploading] = useState(false);
    const [uploadError, setUploadError] = useState<string | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    // Modal state
    const [selectedPhotoIndex, setSelectedPhotoIndex] = useState<number | null>(null);

    useEffect(() => {
        async function loadData() {
            if (!id) {
                setError("Trip not found.");
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                const [tripData, photosData] = await Promise.all([
                    getTrip(Number(id)),
                    getTripPhotos(Number(id)),
                ]);
                setTrip(tripData);
                setPhotos(photosData);
            } catch {
                setError("Could not load trip gallery.");
            } finally {
                setLoading(false);
            }
        }

        loadData();
    }, [id]);

    useEffect(() => {
        if (!selectedFile) {
            setPreviewUrl(null);
            return;
        }

        const objectUrl = URL.createObjectURL(selectedFile);
        setPreviewUrl(objectUrl);

        return () => URL.revokeObjectURL(objectUrl);
    }, [selectedFile]);

    function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0];
        setUploadError(null);

        if (!file) {
            setSelectedFile(null);
            return;
        }

        if (!ALLOWED_FILE_TYPES.includes(file.type)) {
            setUploadError("Please select a JPEG, PNG or WebP image.");
            event.target.value = "";
            setSelectedFile(null);
            return;
        }

        if (file.size > MAX_FILE_SIZE) {
            setUploadError("The photo must not exceed 5 MB.");
            event.target.value = "";
            setSelectedFile(null);
            return;
        }

        setSelectedFile(file);
    }

    async function handleUpload(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!id || !selectedFile) return;

        try {
            setUploading(true);
            setUploadError(null);

            const uploadedPhoto = await uploadTripPhoto(
                Number(id),
                selectedFile,
                caption
            );

            setPhotos((prev) => [uploadedPhoto, ...prev]);
            setSelectedFile(null);
            setCaption("");
            setPreviewUrl(null);
            if (fileInputRef.current) fileInputRef.current.value = "";
        } catch {
            setUploadError("Could not upload the photo.");
        } finally {
            setUploading(false);
        }
    }

    async function handleDeletePhoto(photoId: number): Promise<void>{
        if (!id){
            return;
        }
        await deletePhoto(Number(id), photoId);

        setPhotos((currentPhotos) => currentPhotos.filter((photo => photo.id !== photoId)))

        setSelectedPhotoIndex(null);
    }

    if (loading) {
        return (
            <main className={styles.main}>
                <div className={styles.loadingContainer}>
                    <div className={styles.spinner}></div>
                    <p className={styles.loadingText}>Loading gallery...</p>
                </div>
            </main>
        );
    }

    if (error || !trip) {
        return (
            <main className={styles.main}>
                <div className={styles.errorContainer}>
                    <p className={styles.errorText}>{error || "Trip not found."}</p>
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
                {/* Header */}
                <header className={styles.header}>
                    <div className={styles.headerTop}>
                        <Link to={`/trips/${id}`} className={styles.backLink}>
                            <svg className={styles.backIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                            </svg>
                            <span>Back</span>
                        </Link>
                        <h1 className={styles.title}>Photos</h1>
                        <span className={styles.photoCount}>{photos.length}</span>
                    </div>
                    <p className={styles.subtitle}>{trip.name}</p>
                </header>

                <div className={styles.uploadSection}>
                        <form onSubmit={handleUpload} className={styles.uploadForm}>
                            <div className={styles.uploadRow}>
                                <div className={styles.fileInputWrapper}>
                                    <input
                                        ref={fileInputRef}
                                        type="file"
                                        accept="image/jpeg,image/png,image/webp"
                                        onChange={handleFileChange}
                                        disabled={uploading}
                                        className={styles.fileInput}
                                        id="photo-upload"
                                    />
                                    <label htmlFor="photo-upload" className={styles.fileLabel}>
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                                        </svg>
                                        {selectedFile ? "Change Photo" : "Add Photo"}
                                    </label>
                                </div>

                                {selectedFile && (
                                    <>
                                        <input
                                            type="text"
                                            placeholder="Add a caption..."
                                            value={caption}
                                            onChange={(e) => setCaption(e.target.value)}
                                            maxLength={250}
                                            className={styles.captionInput}
                                            disabled={uploading}
                                        />
                                        <button
                                            type="submit"
                                            disabled={uploading}
                                            className={styles.uploadButton}
                                        >
                                            {uploading ? "Uploading..." : "Upload"}
                                        </button>
                                    </>
                                )}
                            </div>

                            {previewUrl && (
                                <div className={styles.previewContainer}>
                                    <img src={previewUrl} alt="Preview" className={styles.previewImage} />
                                    <button
                                        type="button"
                                        onClick={() => {
                                            setSelectedFile(null);
                                            setPreviewUrl(null);
                                            if (fileInputRef.current) fileInputRef.current.value = "";
                                        }}
                                        className={styles.removePreviewButton}
                                    >
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                        </svg>
                                    </button>
                                </div>
                            )}

                            {uploadError && <p className={styles.uploadError}>{uploadError}</p>}
                        </form>
                </div>

                {/* Photo Grid */}
                {photos.length === 0 ? (
                    <div className={styles.emptyState}>
                        <svg className={styles.emptyIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p className={styles.emptyText}>No photos yet</p>
                        <p className={styles.emptySubtext}>Tap "Add Photo" to share memories</p>
                    </div>
                ) : (
                    <div className={styles.photoGrid}>
                        {photos.map((photo, index) => (
                            <button
                                key={photo.id}
                                className={styles.photoThumb}
                                onClick={() => setSelectedPhotoIndex(index)}
                            >
                                <img
                                    src={photo.imageUrl}
                                    alt={photo.caption || "Trip photo"}
                                    loading="lazy"
                                    className={styles.thumbImage}
                                />
                                {photo.caption && (
                                    <span className={styles.thumbCaption}>{photo.caption}</span>
                                )}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            {/* Photo Modal */}
            {selectedPhotoIndex !== null && (
                <PhotoModal
                    photos={photos}
                    initialIndex={selectedPhotoIndex}
                    currentUserId={user?.id}
                    onDelete={handleDeletePhoto}
                    onClose={() => setSelectedPhotoIndex(null)}
                />
            )}
        </main>
    );
}

export default TripGalleryPage;

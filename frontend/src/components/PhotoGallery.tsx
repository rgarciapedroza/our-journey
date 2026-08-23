import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getTripPhotos } from "../api/tripPhoto";
import type { TripPhoto } from "../types/tripPhoto";
import styles from "../styles/PhotoGallery.module.css";

interface PhotoGalleryProps {
    tripId: number;
    maxDisplay?: number;
}

function PhotoGallery({ tripId, maxDisplay = 4 }: PhotoGalleryProps) {
    const navigate = useNavigate();
    const [photos, setPhotos] = useState<TripPhoto[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadPhotos() {
            try {
                const data = await getTripPhotos(tripId);
                setPhotos(data);
            } catch {
                // Silently fail
            } finally {
                setLoading(false);
            }
        }
        loadPhotos();
    }, [tripId]);

    if (loading) {
        return (
            <div className={styles.galleryPreview}>
                <div className={styles.skeleton}></div>
                <div className={styles.skeleton}></div>
                <div className={styles.skeleton}></div>
                <div className={styles.skeleton}></div>
            </div>
        );
    }

    if (photos.length === 0) {
        return (
            <div className={styles.galleryPreview}>
                <button
                    className={styles.emptyButton}
                    onClick={() => navigate(`/trips/${tripId}/gallery`)}
                >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span>Add Photos</span>
                </button>
            </div>
        );
    }

    const displayPhotos = photos.slice(0, maxDisplay);
    const remainingCount = photos.length - maxDisplay;

    return (
        <div className={styles.galleryPreview}>
            {displayPhotos.map((photo, index) => (
                <button
                    key={photo.id}
                    className={styles.photoThumb}
                    onClick={() => navigate(`/trips/${tripId}/gallery`)}
                >
                    <img
                        src={photo.imageUrl}
                        alt={photo.caption || "Trip photo"}
                        className={styles.thumbImage}
                        loading="lazy"
                    />
                    {index === 3 && remainingCount > 0 && (
                        <div className={styles.photoOverlay}>
                            <span className={styles.remainingCount}>+{remainingCount}</span>
                        </div>
                    )}
                </button>
            ))}
            <button
                className={styles.viewAllButton}
                onClick={() => navigate(`/trips/${tripId}/gallery`)}
            >
                <span>View All</span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                </svg>
            </button>
        </div>
    );
}

export default PhotoGallery;
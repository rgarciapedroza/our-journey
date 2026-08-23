import { useEffect, useState, type TouchEvent } from "react";

import type { TripPhoto } from "../types/tripPhoto";
import { formatRelativeDate } from "../utils/date";
import styles from "../styles/PhotoModal.module.css";

interface PhotoModalProps {
    photos: TripPhoto[];
    initialIndex: number;
    currentUserId?: number;
    onDelete: (photoId: number) => Promise<void>;
    onClose: () => void;
}

function PhotoModal({
    photos,
    initialIndex,
    currentUserId,
    onDelete,
    onClose,
}: PhotoModalProps) {
    const [currentIndex, setCurrentIndex] = useState(initialIndex);
    const [isZoomed, setIsZoomed] = useState(false);
    const [touchStart, setTouchStart] = useState<number | null>(null);
    const [touchEnd, setTouchEnd] = useState<number | null>(null);

    const currentPhoto = photos[currentIndex];

    const [confirmingDelete, setConfirmingDelete] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState<string | null>(null);

    const canDelete = currentUserId === currentPhoto?.uploadedById;

    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === "Escape") {
                onClose();
            }

            if (event.key === "ArrowLeft" && currentIndex > 0) {
                setCurrentIndex((index) => index - 1);
                setIsZoomed(false);
            }

            if (
                event.key === "ArrowRight"
                && currentIndex < photos.length - 1
            ) {
                setCurrentIndex((index) => index + 1);
                setIsZoomed(false);
            }
        }

        document.addEventListener("keydown", handleKeyDown);
        document.body.style.overflow = "hidden";

        return () => {
            document.removeEventListener("keydown", handleKeyDown);
            document.body.style.overflow = "";
        };
    }, [currentIndex, onClose, photos.length]);

    useEffect(() => {
        setConfirmingDelete(false);
        setDeleteError(null);
    }, [currentIndex]);

    function handleTouchStart(event: TouchEvent) {
        setTouchStart(event.targetTouches[0].clientX);
        setTouchEnd(null);
    }

    function handleTouchMove(event: TouchEvent) {
        setTouchEnd(event.targetTouches[0].clientX);
    }

    function handleTouchEnd() {
        if (touchStart === null || touchEnd === null) {
            return;
        }

        const difference = touchStart - touchEnd;

        if (difference > 50 && currentIndex < photos.length - 1) {
            setCurrentIndex((index) => index + 1);
            setIsZoomed(false);
        }

        if (difference < -50 && currentIndex > 0) {
            setCurrentIndex((index) => index - 1);
            setIsZoomed(false);
        }

        setTouchStart(null);
        setTouchEnd(null);
    }

    if (!currentPhoto) {
        return null;
    }

    async function handleDelete(){
        if (!currentPhoto || deleting) {
            return;
        }

        if (!confirmingDelete) {
            setConfirmingDelete(true);
            setDeleteError(null);
            return;
        }

        try{
            setDeleting(true);
            setDeleteError(null);
            await onDelete(currentPhoto.id);
        } catch{
            setDeleteError("Could not delete the photo. Please try again");
            setConfirmingDelete(false);
        } finally{
            setDeleting(false);
        }
    }

    return (
        <div
            className={styles.overlay}
            role="dialog"
            aria-modal="true"
            aria-label="Trip photo viewer"
            onClick={onClose}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
        >
            <button
                type="button"
                className={styles.closeButton}
                onClick={onClose}
                aria-label="Close photo viewer"
            >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>

            <div className={styles.counter}>
                {currentIndex + 1} / {photos.length}
            </div>

            <div
                className={styles.photoContainer}
                onClick={(event) => {
                    event.stopPropagation();
                    setIsZoomed((zoomed) => !zoomed);
                }}
            >
                <img
                    src={currentPhoto.imageUrl}
                    alt={currentPhoto.caption || "Trip photo"}
                    className={`${styles.mainImage} ${isZoomed ? styles.zoomed : ""}`}
                />
            </div>

            {currentIndex > 0 && (
                <button
                    type="button"
                    className={`${styles.navButton} ${styles.navLeft}`}
                    onClick={(event) => {
                        event.stopPropagation();
                        setCurrentIndex((index) => index - 1);
                        setIsZoomed(false);
                    }}
                    aria-label="Previous photo"
                >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                    </svg>
                </button>
            )}

            {currentIndex < photos.length - 1 && (
                <button
                    type="button"
                    className={`${styles.navButton} ${styles.navRight}`}
                    onClick={(event) => {
                        event.stopPropagation();
                        setCurrentIndex((index) => index + 1);
                        setIsZoomed(false);
                    }}
                    aria-label="Next photo"
                >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                    </svg>
                </button>
            )}

            <div
                className={styles.bottomInfo}
                onClick={(event) => event.stopPropagation()}
            >
                <div className={styles.infoContent}>
                    <div className={styles.uploaderInfo}>
                        {currentPhoto.uploadedByProfilePicture ? (
                            <img
                                src={currentPhoto.uploadedByProfilePicture}
                                alt=""
                                className={styles.uploaderAvatar}
                            />
                        ) : (
                            <div className={styles.uploaderAvatarPlaceholder}>
                                {currentPhoto.uploadedByName
                                    .charAt(0)
                                    .toUpperCase() || "U"}
                            </div>
                        )}

                        <div>
                            <span className={styles.uploaderName}>
                                {currentPhoto.uploadedByName}
                            </span>
                            <span className={styles.uploadDate}>
                                {formatRelativeDate(currentPhoto.createdAt)}
                            </span>
                        </div>
                    </div>

                    {currentPhoto.caption && (
                        <p className={styles.caption}>
                            {currentPhoto.caption}
                        </p>
                    )}
                </div>

                {canDelete && (
                <div className={styles.deleteActions}>
                    {deleteError && (
                        <span className={styles.deleteError}>
                            {deleteError}
                        </span>
                    )}

                    <button
                        type="button"
                        className={`${styles.deleteButton} ${
                            confirmingDelete ? styles.deleteButtonConfirm : ""
                        }`}
                        onClick={handleDelete}
                        disabled={deleting}
                    >
                        <svg
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth={2}
                            aria-hidden="true"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M6 7h12m-10 0 1 13h6l1-13m-5 4v5m3-5v5m-4-9V4h4v3"
                            />
                        </svg>

                        {deleting
                            ? "Deleting..."
                            : confirmingDelete
                                ? "Confirm delete"
                                : "Delete"}
                    </button>

                    {confirmingDelete && !deleting && (
                        <button
                            type="button"
                            className={styles.deleteCancelButton}
                            onClick={() => setConfirmingDelete(false)}
                        >
                            Cancel
                        </button>
                    )}
                </div>
            )}
            </div>

            <div className={styles.progressBar} aria-hidden="true">
                {photos.map((photo, index) => (
                    <div
                        key={photo.id}
                        className={`${styles.progressDot} ${
                            index === currentIndex
                                ? styles.progressDotActive
                                : ""
                        }`}
                    />
                ))}
            </div>
        </div>
    );
}

export default PhotoModal;

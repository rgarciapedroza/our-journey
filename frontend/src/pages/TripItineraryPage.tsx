import { useEffect, useState, type ChangeEvent, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";

import { createItineraryItem, deleteItineraryItem, getItineraryItems, updateItineraryItem } from "../api/itinerary";
import { getTrip } from "../api/trips";
import type { ItineraryItem, ItineraryItemRequest } from "../types/itineraryItem";
import type { Trip } from "../types/trip";
import { formatTripDate } from "../utils/date";
import styles from "../styles/TripItineraryPage.module.css";

const EMPTY_FORM: ItineraryItemRequest = {
    activityDate: "", startTime: "", endTime: "", title: "", description: "", place: "",
};

function formatTime(time: string) {
    return time.slice(0, 5);
}

function sortItems(items: ItineraryItem[]) {
    return [...items].sort((a, b) =>
        `${a.activityDate}T${a.startTime}`.localeCompare(`${b.activityDate}T${b.startTime}`)
    );
}

function TripItineraryPage() {
    const { id } = useParams<{ id: string }>();
    const [trip, setTrip] = useState<Trip | null>(null);
    const [items, setItems] = useState<ItineraryItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form, setForm] = useState<ItineraryItemRequest>(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState<string | null>(null);
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
    const [deletingId, setDeletingId] = useState<number | null>(null);
    const [expandedItemId, setExpandedItemId] = useState<number | null>(null);

    useEffect(() => {
        let ignore = false;
        async function load() {
            if (!id || Number.isNaN(Number(id))) {
                setError("Trip not found."); setLoading(false); return;
            }
            try {
                const tripId = Number(id);
                const [tripData, itinerary] = await Promise.all([getTrip(tripId), getItineraryItems(tripId)]);
                if (!ignore) { setTrip(tripData); setItems(itinerary); }
            } catch {
                if (!ignore) setError("Could not load the itinerary.");
            } finally {
                if (!ignore) setLoading(false);
            }
        }
        load();
        return () => { ignore = true; };
    }, [id]);

    function openCreateForm() {
        setEditingId(null);
        setForm({ ...EMPTY_FORM, activityDate: trip?.startDate ?? "" });
        setFormError(null); setShowForm(true);
        setExpandedItemId(null);
    }

    function openEditForm(item: ItineraryItem) {
        setEditingId(item.id);
        setForm({
            activityDate: item.activityDate,
            startTime: formatTime(item.startTime),
            endTime: item.endTime ? formatTime(item.endTime) : "",
            title: item.title,
            description: item.description ?? "",
            place: item.place ?? "",
        });
        setConfirmDeleteId(null); setFormError(null); setShowForm(true);
        setExpandedItemId(null);
    }

    function closeForm() {
        setShowForm(false); setEditingId(null); setForm(EMPTY_FORM); setFormError(null);
    }

    function toggleExpand(itemId: number) {
        setExpandedItemId(expandedItemId === itemId ? null : itemId);
    }

    function handleChange(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        const { name, value } = event.target;
        setForm((current) => ({ ...current, [name]: value }));
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!id) return;
        try {
            setSubmitting(true); setFormError(null);
            const request: ItineraryItemRequest = {
                ...form,
                title: form.title.trim(),
                endTime: form.endTime || null,
                description: form.description?.trim() || null,
                place: form.place?.trim() || null,
            };
            if (editingId === null) {
                const created = await createItineraryItem(Number(id), request);
                setItems((current) => sortItems([...current, created]));
            } else {
                const updated = await updateItineraryItem(Number(id), editingId, request);
                setItems((current) => sortItems(current.map((item) => item.id === updated.id ? updated : item)));
            }
            closeForm();
        } catch (requestError) {
            setFormError(requestError instanceof Error ? requestError.message : "Could not save the activity.");
        } finally {
            setSubmitting(false);
        }
    }

    async function handleDelete(itemId: number) {
        if (!id) return;
        if (confirmDeleteId !== itemId) { setConfirmDeleteId(itemId); return; }
        try {
            setDeletingId(itemId);
            await deleteItineraryItem(Number(id), itemId);
            setItems((current) => current.filter((item) => item.id !== itemId));
            setConfirmDeleteId(null);
            setExpandedItemId(null);
        } catch (requestError) {
            setError(requestError instanceof Error ? requestError.message : "Could not delete the activity.");
        } finally {
            setDeletingId(null);
        }
    }

    // Agrupar items por día
    const groupedItems: Record<string, ItineraryItem[]> = {};
    items.forEach(item => {
        if (!groupedItems[item.activityDate]) {
            groupedItems[item.activityDate] = [];
        }
        groupedItems[item.activityDate].push(item);
    });

    if (loading) {
        return (
            <main className={styles.page}>
                <div className={styles.loadingContainer}>
                    <div className={styles.spinner}></div>
                    <p className={styles.loadingText}>Loading itinerary...</p>
                </div>
            </main>
        );
    }

    if (!trip) {
        return (
            <main className={styles.page}>
                <div className={styles.status}>
                    <p>{error ?? "Trip not found."}</p>
                    <Link to="/trips" className={styles.backButton}>Back to trips</Link>
                </div>
            </main>
        );
    }

    return (
        <main className={styles.page}>
            <div className={styles.container}>
                {/* Header */}
                <header className={styles.header}>
                    <Link to={`/trips/${trip.id}`} className={styles.backLink}>
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
                        </svg>
                        Back to trip
                    </Link>
                    <div className={styles.heading}>
                        <div>
                            <h1 className={styles.title}>Itinerary</h1>
                            <p className={styles.subtitle}>{trip.name}</p>
                        </div>
                        <button type="button" className={styles.addButton} onClick={openCreateForm}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                            </svg>
                            Add Activity
                        </button>
                    </div>
                </header>

                {error && <p className={styles.pageError}>{error}</p>}

                {/* Formulario */}
                {showForm && (
                    <section className={styles.formCard}>
                        <div className={styles.formHeader}>
                            <h2>{editingId === null ? "Add Activity" : "Edit Activity"}</h2>
                            <button type="button" className={styles.formClose} onClick={closeForm}>
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>
                        <form className={styles.form} onSubmit={handleSubmit}>
                            <div className={styles.formGroup}>
                                <label htmlFor="title">Activity Title</label>
                                <input
                                    id="title"
                                    name="title"
                                    value={form.title}
                                    onChange={handleChange}
                                    maxLength={120}
                                    required
                                    placeholder="e.g., Visit the Colosseum"
                                />
                            </div>
                            <div className={styles.formRow}>
                                <div className={styles.formGroup}>
                                    <label htmlFor="activityDate">Date</label>
                                    <input
                                        id="activityDate"
                                        type="date"
                                        name="activityDate"
                                        value={form.activityDate}
                                        onChange={handleChange}
                                        min={trip.startDate}
                                        max={trip.endDate}
                                        required
                                    />
                                </div>
                                <div className={styles.formGroup}>
                                    <label htmlFor="startTime">Start Time</label>
                                    <input
                                        id="startTime"
                                        type="time"
                                        name="startTime"
                                        value={form.startTime}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className={styles.formGroup}>
                                    <label htmlFor="endTime">End Time</label>
                                    <input
                                        id="endTime"
                                        type="time"
                                        name="endTime"
                                        value={form.endTime ?? ""}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                            <div className={styles.formGroup}>
                                <label htmlFor="place">Location</label>
                                <input
                                    id="place"
                                    name="place"
                                    value={form.place ?? ""}
                                    onChange={handleChange}
                                    maxLength={250}
                                    placeholder="e.g., Piazza del Colosseo, Rome"
                                />
                            </div>
                            <div className={styles.formGroup}>
                                <label htmlFor="description">Description</label>
                                <textarea
                                    id="description"
                                    name="description"
                                    value={form.description ?? ""}
                                    onChange={handleChange}
                                    maxLength={1000}
                                    rows={3}
                                    placeholder="Add details about this activity..."
                                />
                            </div>
                            {formError && <p className={styles.formError}>{formError}</p>}
                            <div className={styles.formActions}>
                                <button type="button" className={styles.cancelButton} onClick={closeForm} disabled={submitting}>
                                    Cancel
                                </button>
                                <button type="submit" className={styles.saveButton} disabled={submitting}>
                                    {submitting ? "Saving..." : editingId === null ? "Add Activity" : "Save Changes"}
                                </button>
                            </div>
                        </form>
                    </section>
                )}

                {/* Timeline */}
                {items.length === 0 ? (
                    <section className={styles.emptyState}>
                        <div className={styles.emptyIcon}>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.5}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                            </svg>
                        </div>
                        <h2>No activities yet</h2>
                        <p>Start planning your trip by adding the first activity.</p>
                        {!showForm && (
                            <button type="button" className={styles.emptyAction} onClick={openCreateForm}>
                                Add First Activity
                            </button>
                        )}
                    </section>
                ) : (
                    <section className={styles.timeline}>
                        {Object.entries(groupedItems).map(([date, dayItems]) => (
                            <div key={date} className={styles.dayGroup}>
                                <div className={styles.dayHeader}>
                                    <span className={styles.dayDate}>{formatTripDate(date)}</span>
                                    <span className={styles.dayCount}>{dayItems.length} activities</span>
                                </div>
                                <div className={styles.dayTimeline}>
                                    {dayItems.map((item, index) => {
                                        const isExpanded = expandedItemId === item.id;
                                        const isFirst = index === 0;
                                        const isLast = index === dayItems.length - 1;

                                        return (
                                            <div
                                                key={item.id}
                                                className={`${styles.timelineItem} ${isExpanded ? styles.expanded : ''} ${isFirst ? styles.first : ''} ${isLast ? styles.last : ''}`}
                                            >
                                                <div className={styles.timelineConnector}>
                                                    <div className={styles.timelineDot} />
                                                    {!isLast && <div className={styles.timelineLine} />}
                                                </div>

                                                <div
                                                    className={styles.itemCard}
                                                    onClick={() => toggleExpand(item.id)}
                                                >
                                                    <div className={styles.itemHeader}>
                                                        <div className={styles.itemTime}>
                                                            <span className={styles.itemTimeStart}>{formatTime(item.startTime)}</span>
                                                            {item.endTime && (
                                                                <span className={styles.itemTimeEnd}>
                                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
                                                                    </svg>
                                                                    {formatTime(item.endTime)}
                                                                </span>
                                                            )}
                                                        </div>
                                                        <h3 className={styles.itemTitle}>{item.title}</h3>
                                                        {item.place && (
                                                            <div className={styles.itemPlace}>
                                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                                                </svg>
                                                                <span>{item.place}</span>
                                                            </div>
                                                        )}
                                                        <button
                                                            className={styles.expandButton}
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                toggleExpand(item.id);
                                                            }}
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                                                                <path strokeLinecap="round" strokeLinejoin="round" d={isExpanded ? "M5 15l7-7 7 7" : "M19 9l-7 7-7-7"} />
                                                            </svg>
                                                        </button>
                                                    </div>

                                                    <div className={styles.itemDetails}>
                                                        {item.description && (
                                                            <p className={styles.itemDescription}>{item.description}</p>
                                                        )}
                                                        <div className={styles.itemMeta}>
                                                            <span className={styles.itemCreator}>
                                                                Added by {item.createdByName}
                                                            </span>
                                                            <div className={styles.itemActions}>
                                                                <button
                                                                    type="button"
                                                                    className={styles.editButton}
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        openEditForm(item);
                                                                    }}
                                                                >
                                                                    Edit
                                                                </button>
                                                                <button
                                                                    type="button"
                                                                    className={confirmDeleteId === item.id ? styles.confirmDeleteButton : styles.deleteButton}
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        handleDelete(item.id);
                                                                    }}
                                                                    disabled={deletingId === item.id}
                                                                >
                                                                    {deletingId === item.id ? "Deleting..." : confirmDeleteId === item.id ? "Confirm" : "Delete"}
                                                                </button>
                                                                {confirmDeleteId === item.id && deletingId === null && (
                                                                    <button
                                                                        type="button"
                                                                        className={styles.cancelDeleteButton}
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            setConfirmDeleteId(null);
                                                                        }}
                                                                    >
                                                                        Cancel
                                                                    </button>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                        ))}
                    </section>
                )}
            </div>
        </main>
    );
}

export default TripItineraryPage;
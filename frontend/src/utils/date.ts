const tripDateFormatter = new Intl.DateTimeFormat("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
});

export function formatTripDate(value: unknown): string {
    const dateParts = Array.isArray(value)
        ? value.slice(0, 3).map(Number)
        : typeof value === "string"
            ? value.split("-").map(Number)
            : [];

    const [year, month, day] = dateParts;

    if (!year || !month || !day) {
        return "Date unavailable";
    }

    return tripDateFormatter.format(
        new Date(year, month - 1, day)
    );
}

function parseDateTime(value: unknown): Date | null {
    let date: Date;

    if (Array.isArray(value)) {
        const [year, month, day, hours = 0, minutes = 0, seconds = 0, nanoseconds = 0] =
            value.map(Number);

        if (![year, month, day, hours, minutes, seconds, nanoseconds].every(Number.isFinite)) {
            return null;
        }

        date = new Date(
            year,
            month - 1,
            day,
            hours,
            minutes,
            seconds,
            Math.floor(nanoseconds / 1_000_000)
        );
    } else if (typeof value === "string" || typeof value === "number") {
        date = new Date(value);
    } else {
        return null;
    }

    return Number.isNaN(date.getTime()) ? null : date;
}

export function formatRelativeDate(value: unknown): string {
    const date = parseDateTime(value);

    if (!date) {
        return "Date unavailable";
    }

    const elapsedMilliseconds = Date.now() - date.getTime();

    if (elapsedMilliseconds < 0) {
        return "Just now";
    }

    const elapsedMinutes = Math.floor(elapsedMilliseconds / 60_000);
    const elapsedHours = Math.floor(elapsedMilliseconds / 3_600_000);
    const elapsedDays = Math.floor(elapsedMilliseconds / 86_400_000);

    if (elapsedMinutes < 1) return "Just now";
    if (elapsedMinutes < 60) return `${elapsedMinutes} minute${elapsedMinutes === 1 ? "" : "s"} ago`;
    if (elapsedHours < 24) return `${elapsedHours} hour${elapsedHours === 1 ? "" : "s"} ago`;
    if (elapsedDays === 1) return "Yesterday";
    if (elapsedDays < 7) return `${elapsedDays} days ago`;
    if (elapsedDays < 30) {
        const weeks = Math.floor(elapsedDays / 7);
        return `${weeks} week${weeks === 1 ? "" : "s"} ago`;
    }
    if (elapsedDays < 365) {
        const months = Math.floor(elapsedDays / 30);
        return `${months} month${months === 1 ? "" : "s"} ago`;
    }

    const years = Math.floor(elapsedDays / 365);
    return `${years} year${years === 1 ? "" : "s"} ago`;
}

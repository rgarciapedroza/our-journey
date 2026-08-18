import { useState, type ChangeEvent, type FormEvent } from "react";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";
import {
    changePassword,
    updateProfile,
    updateProfilePicture,
    verifyCurrentPassword,
} from "../api/users";
import styles from "../styles/AccountSettingsPage.module.css";

function AccountSettingsPage() {
    const { user, updateUser, logout } = useAuth();

    const [isEditing, setIsEditing] = useState(false);

    const [name, setName] = useState(user?.name ?? "");
    const [email, setEmail] = useState(user?.email ?? "");

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmNewPassword, setConfirmNewPassword] = useState("");
    const [isCurrentPasswordValid, setIsCurrentPasswordValid] = useState(false);
    const [isCheckingPassword, setIsCheckingPassword] = useState(false);

    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [profilePictureVersion, setProfilePictureVersion] = useState(0);

    const [profileLoading, setProfileLoading] = useState(false);
    const [passwordLoading, setPasswordLoading] = useState(false);
    const [pictureLoading, setPictureLoading] = useState(false);

    const [profileError, setProfileError] = useState("");
    const [profileSuccess, setProfileSuccess] = useState("");
    const [passwordError, setPasswordError] = useState("");
    const [passwordSuccess, setPasswordSuccess] = useState("");
    const [pictureError, setPictureError] = useState("");
    const [pictureSuccess, setPictureSuccess] = useState("");

    if (!user) {
        return (
            <main className={styles.main}>
                <div className={styles.container}>
                    <div className={styles.header}>
                        <h1 className={styles.title}>Account Settings</h1>
                        <p className={styles.subtitle}>
                            Please log in to access your account settings.
                        </p>
                    </div>
                    <div className={styles.errorContainer}>
                        <p className={styles.errorMessage}>
                            You need to be logged in to view this page.
                        </p>
                    </div>
                </div>
            </main>
        );
    }

    function handleEdit() {
        setName(user!.name);
        setEmail(user!.email);
        setCurrentPassword("");
        setNewPassword("");
        setConfirmNewPassword("");
        setIsCurrentPasswordValid(false);
        setSelectedFile(null);
        setProfileError("");
        setProfileSuccess("");
        setPasswordError("");
        setPasswordSuccess("");
        setPictureError("");
        setPictureSuccess("");
        setIsEditing(true);
    }

    function handleCancel() {
        setName(user!.name);
        setEmail(user!.email);
        setCurrentPassword("");
        setNewPassword("");
        setConfirmNewPassword("");
        setIsCurrentPasswordValid(false);
        setSelectedFile(null);
        setProfileError("");
        setProfileSuccess("");
        setPasswordError("");
        setPasswordSuccess("");
        setPictureError("");
        setPictureSuccess("");
        setIsEditing(false);
    }

    async function handleProfileSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setProfileError("");
        setProfileSuccess("");
        setProfileLoading(true);

        try {
            const updatedUser = await updateProfile({
                name,
            });

            updateUser(updatedUser);
            setProfileSuccess("Profile updated successfully.");

            setTimeout(() => {
                setIsEditing(false);
            }, 1500);
        } catch (error) {
            console.error("Could not update profile:", error);
            setProfileError("Could not update your profile.");
        } finally {
            setProfileLoading(false);
        }
    }

    async function handleCheckCurrentPassword() {
        if (!currentPassword) {
            setPasswordError("Please enter your current password.");
            return;
        }

        setIsCheckingPassword(true);
        setPasswordError("");
        setPasswordSuccess("");

        try {
            await verifyCurrentPassword(currentPassword);
            setIsCurrentPasswordValid(true);
            setPasswordSuccess("✓ Current password verified. Please enter your new password.");
            setPasswordError("");
        } catch (error) {
            console.error("Invalid current password:", error);
            setPasswordError("Current password is incorrect.");
            setIsCurrentPasswordValid(false);
        } finally {
            setIsCheckingPassword(false);
        }
    }

    async function handlePasswordSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setPasswordError("");
        setPasswordSuccess("");

        if (!isCurrentPasswordValid) {
            setPasswordError("Please verify your current password first.");
            return;
        }

        if (newPassword !== confirmNewPassword) {
            setPasswordError("New passwords do not match.");
            return;
        }

        if (newPassword.length < 6) {
            setPasswordError("Password must be at least 6 characters.");
            return;
        }

        setPasswordLoading(true);

        try {
            await changePassword({
                currentPassword,
                newPassword,
                confirmNewPassword,
            });

            setCurrentPassword("");
            setNewPassword("");
            setConfirmNewPassword("");
            setIsCurrentPasswordValid(false);

            setPasswordSuccess("Password changed successfully.");

            setTimeout(() => {
                setPasswordSuccess("");
            }, 3000);
        } catch (error) {
            console.error("Could not change password:", error);
            setPasswordError("Could not change your password.");
        } finally {
            setPasswordLoading(false);
        }
    }

    function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0] ?? null;

        if (file && file.size > 5 * 1024 * 1024) {
            setPictureError("Image must be smaller than 5MB.");
            setSelectedFile(null);
            event.target.value = "";
            return;
        }

        setSelectedFile(file);
        setPictureError("");
        setPictureSuccess("");
    }

    async function handlePictureSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setPictureError("");
        setPictureSuccess("");

        if (!selectedFile) {
            setPictureError("Please select an image.");
            return;
        }

        setPictureLoading(true);

        try {
            const updatedUser = await updateProfilePicture(selectedFile);

            updateUser(updatedUser);
            setProfilePictureVersion(Date.now());
            setSelectedFile(null);

            const fileInput = document.getElementById("profilePicture") as HTMLInputElement;
            if (fileInput) {
                fileInput.value = "";
            }

            setPictureSuccess("Profile picture updated successfully.");
        } catch (error) {
            console.error("Could not update profile picture:", error);
            setPictureError("Could not update your profile picture.");
        } finally {
            setPictureLoading(false);
        }
    }

    function handleLogout() {
        logout();
    }

    if (!isEditing) {
        return (
            <main className={styles.main}>
                <div className={styles.container}>
                    <div className={styles.header}>
                        <h1 className={styles.title}>Account Settings</h1>
                        <p className={styles.subtitle}>
                            View and manage your account information
                        </p>
                    </div>

                    <section className={styles.section}>
                        <div className={styles.sectionHeader}>
                            <h2 className={styles.sectionTitle}>Profile Information</h2>
                            <button
                                type="button"
                                onClick={handleEdit}
                                className={styles.editButton}
                            >
                                <svg className={styles.editIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                                Edit Profile
                            </button>
                        </div>

                        <div className={styles.viewMode}>
                            <div className={styles.profilePictureContainer}>
                                {user.profilePicture ? (
                                    <img
                                        src={`${user.profilePicture}?v=${profilePictureVersion}`}
                                        alt="Profile"
                                        className={styles.profilePicture}
                                        referrerPolicy="no-referrer"
                                        onError={(e) => {
                                            (e.target as HTMLImageElement).style.display = 'none';
                                        }}
                                    />
                                ) : (
                                    <div className={styles.profilePicturePlaceholder}>
                                        {user.name.charAt(0).toUpperCase()}
                                    </div>
                                )}
                            </div>

                            <div className={styles.viewField}>
                                <span className={styles.viewLabel}>Full Name</span>
                                <span className={styles.viewValue}>{user.name}</span>
                            </div>

                            <div className={styles.viewField}>
                                <span className={styles.viewLabel}>Email Address</span>
                                <span className={styles.viewValue}>{user.email}</span>
                            </div>
                        </div>
                    </section>

                    <section className={styles.logoutSection}>
                        <div className={styles.accountActions}>
                            <Link to="/trips" className={styles.myTripsButton}>
                                <svg className={styles.myTripsIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                My Trips
                            </Link>

                            <button
                                type="button"
                                onClick={handleLogout}
                                className={styles.logoutButton}
                            >
                                <svg className={styles.logoutIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                                </svg>
                                Log out
                            </button>
                        </div>
                    </section>
                </div>
            </main>
        );
    }

    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <div className={styles.header}>
                    <h1 className={styles.title}>Edit Account Settings</h1>
                    <p className={styles.subtitle}>
                        Update your profile information
                    </p>
                </div>

                <section className={styles.section}>
                    <h2 className={styles.sectionTitle}>Profile Picture</h2>

                    <div className={styles.profilePictureContainer}>
                        {user.profilePicture ? (
                            <img
                                src={`${user.profilePicture}?v=${profilePictureVersion}`}
                                alt="Profile"
                                className={styles.profilePicture}
                                referrerPolicy="no-referrer"
                                onError={(e) => {
                                    (e.target as HTMLImageElement).style.display = 'none';
                                }}
                            />
                        ) : (
                            <div className={styles.profilePicturePlaceholder}>
                                {user.name.charAt(0).toUpperCase()}
                            </div>
                        )}
                    </div>

                    <form onSubmit={handlePictureSubmit} className={styles.form}>
                        <div className={styles.formGroup}>
                            <label htmlFor="profilePicture" className={styles.label}>
                                Upload new picture
                                <span className={styles.labelOptional}> (JPG, PNG, max 5MB)</span>
                            </label>

                            <div className={styles.fileInputWrapper}>
                                <input
                                    id="profilePicture"
                                    type="file"
                                    accept="image/*"
                                    onChange={handleFileChange}
                                    className={styles.fileInput}
                                    disabled={pictureLoading}
                                />
                                <span className={styles.fileInputLabel}>
                                    {selectedFile ? selectedFile.name : "Choose an image..."}
                                </span>
                                <span className={styles.fileInputButton}>Browse</span>
                            </div>
                        </div>

                        {pictureError && (
                            <div className={styles.errorContainer}>
                                <svg className={styles.errorIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <p className={styles.errorMessage}>{pictureError}</p>
                            </div>
                        )}

                        {pictureSuccess && (
                            <div className={styles.successContainer}>
                                <svg className={styles.successIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                                <p className={styles.successMessage}>{pictureSuccess}</p>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={pictureLoading || !selectedFile}
                            className={styles.button}
                        >
                            {pictureLoading ? "Uploading..." : "Change picture"}
                        </button>
                    </form>
                </section>

                <section className={styles.section}>
                    <h2 className={styles.sectionTitle}>Personal Information</h2>

                    <form onSubmit={handleProfileSubmit} className={styles.form}>
                        <div className={styles.formGroup}>
                            <label htmlFor="name" className={styles.label}>
                                Full Name
                            </label>

                            <input
                                id="name"
                                type="text"
                                value={name}
                                onChange={(event) => setName(event.target.value)}
                                required
                                className={styles.input}
                                placeholder="Your full name"
                                minLength={2}
                                maxLength={100}
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="email" className={styles.label}>
                                Email Address
                            </label>

                            <input
                                id="email"
                                type="email"
                                value={email}
                                disabled
                                className={`${styles.input} ${styles.inputDisabled}`}
                            />
                            <span className={styles.inputHint}>Email cannot be changed</span>
                        </div>

                        {profileError && (
                            <div className={styles.errorContainer}>
                                <svg className={styles.errorIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <p className={styles.errorMessage}>{profileError}</p>
                            </div>
                        )}

                        {profileSuccess && (
                            <div className={styles.successContainer}>
                                <svg className={styles.successIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                                <p className={styles.successMessage}>{profileSuccess}</p>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={profileLoading}
                            className={styles.saveButtonInline}
                        >
                            {profileLoading ? "Saving..." : "Save Changes"}
                        </button>
                    </form>
                </section>

                <section className={styles.section}>
                    <h2 className={styles.sectionTitle}>Change Password</h2>

                    <form onSubmit={handlePasswordSubmit} className={styles.form}>
                        <div className={styles.formGroup}>
                            <label htmlFor="currentPassword" className={styles.label}>
                                Current Password
                            </label>

                            <div className={styles.passwordInputGroup}>
                                <input
                                    id="currentPassword"
                                    type="password"
                                    value={currentPassword}
                                    onChange={(event) => {
                                        setCurrentPassword(event.target.value);
                                        setIsCurrentPasswordValid(false);
                                        setPasswordError("");
                                        setPasswordSuccess("");
                                    }}
                                    required
                                    className={styles.input}
                                    placeholder="Enter your current password"
                                    autoComplete="current-password"
                                    disabled={isCurrentPasswordValid}
                                />
                                <button
                                    type="button"
                                    onClick={handleCheckCurrentPassword}
                                    disabled={isCheckingPassword || isCurrentPasswordValid || !currentPassword}
                                    className={`${styles.verifyButton} ${isCurrentPasswordValid ? styles.verifyButtonVerified : ''}`}
                                >
                                    {isCheckingPassword ? "Verifying..." : isCurrentPasswordValid ? "✓ Verified" : "Verify"}
                                </button>
                            </div>
                        </div>

                        {isCurrentPasswordValid && (
                            <>
                                <div className={styles.formGroup}>
                                    <label htmlFor="newPassword" className={styles.label}>
                                        New Password
                                        <span className={styles.labelOptional}> (min 6 characters)</span>
                                    </label>

                                    <input
                                        id="newPassword"
                                        type="password"
                                        value={newPassword}
                                        onChange={(event) => setNewPassword(event.target.value)}
                                        required
                                        className={styles.input}
                                        placeholder="Enter your new password"
                                        minLength={6}
                                        autoComplete="new-password"
                                    />
                                </div>

                                <div className={styles.formGroup}>
                                    <label htmlFor="confirmNewPassword" className={styles.label}>
                                        Confirm New Password
                                    </label>

                                    <input
                                        id="confirmNewPassword"
                                        type="password"
                                        value={confirmNewPassword}
                                        onChange={(event) => setConfirmNewPassword(event.target.value)}
                                        required
                                        className={styles.input}
                                        placeholder="Confirm your new password"
                                        minLength={6}
                                        autoComplete="new-password"
                                    />
                                </div>
                            </>
                        )}

                        {passwordError && (
                            <div className={styles.errorContainer}>
                                <svg className={styles.errorIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <p className={styles.errorMessage}>{passwordError}</p>
                            </div>
                        )}

                        {passwordSuccess && (
                            <div className={styles.successContainer}>
                                <svg className={styles.successIcon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                                <p className={styles.successMessage}>{passwordSuccess}</p>
                            </div>
                        )}

                        {isCurrentPasswordValid && (
                            <button
                                type="submit"
                                disabled={passwordLoading || !newPassword || !confirmNewPassword}
                                className={styles.saveButtonInline}
                            >
                                {passwordLoading ? "Changing..." : "Change Password"}
                            </button>
                        )}
                    </form>
                </section>

                <div className={styles.editActions}>
                    <button
                        type="button"
                        onClick={handleCancel}
                        className={styles.cancelButton}
                        disabled={profileLoading || passwordLoading || pictureLoading}
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </main>
    );
}

export default AccountSettingsPage;

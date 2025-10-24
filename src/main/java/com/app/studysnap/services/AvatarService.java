package com.app.studysnap.services;

import com.app.studysnap.model.User;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;

import static com.app.studysnap.services.TextParser.*;

/**
 * Service for managing user avatar images.
 * <p>
 * Avatars are stored under a per-user filename in {@code ~/.studysnap/avatars}.
 * The service can locate, save (replace), delete, and render avatars with a circular crop.
 * </p>
 */
public final class AvatarService {

    /** Base directory where user avatars are persisted. */
    private static final Path AVATARS_DIR =
            Paths.get(System.getProperty("user.home"), ".studysnap", "avatars");

    /** Supported image filename extensions for avatars. */
    private static final String[] AVATAR_FORMATS = {".png", ".jpg", ".jpeg", ".gif"};

    /** Classpath resource path for the fallback/default avatar image. */
    private static final String DEFAULT_AVATAR_RESOURCE =
            "/com/app/studysnap/images/default_avatar.png";

    /**
     * Default constructor:
     * Creates a new {@code AvatarService}.
     */
    public AvatarService() {}

    // Load, save, delete

    /**
     * Returns the path to the user's existing avatar file, if any.
     * @param user the user whose avatar to find (can be {@code null})
     * @return an {@link Optional} containing the avatar path if found; otherwise empty
     */
    public Optional<Path> findAvatarFile(User user) {
        String base = baseAvatarName(user);
        for (String ext : AVATAR_FORMATS) {
            Path p = AVATARS_DIR.resolve(base + ext);
            if (Files.exists(p)) return Optional.of(p);
        }
        return Optional.empty();
    }

    /**
     * Saves (or replaces) the user's avatar with the given image file.
     * Existing avatar files for other extensions are removed.
     * @param user the user who owns the avatar
     * @param chosen the source image file to copy
     * @return the filesystem path of the saved avatar image
     * @throws Exception if the directory cannot be created or the copy fails
     */
    public Path saveAvatar(User user, File chosen) throws Exception {
        Files.createDirectories(AVATARS_DIR);

        String base = baseAvatarName(user);
        String ext = extLower(chosen.getName());
        if (!ext.matches("\\.(png|jpg|jpeg|gif)")) ext = ".png"; // normalize

        // remove older versions in other extensions
        for (String e : AVATAR_FORMATS) {
            Files.deleteIfExists(AVATARS_DIR.resolve(base + e));
        }

        Path target = AVATARS_DIR.resolve(base + ext);
        Files.copy(chosen.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    /**
     * Deletes any avatar files for the given user.
     * @param user the user whose avatar files should be removed
     * @throws Exception if an IO error occurs while deleting
     */
    public void deleteAvatar(User user) throws Exception {
        String base = baseAvatarName(user);
        for (String e : AVATAR_FORMATS) {
            Files.deleteIfExists(AVATARS_DIR.resolve(base + e));
        }
    }

    /**
     * Loads the default avatar image from the classpath.
     * @param requestedSizePx target width/height hint in pixels
     * @return the default {@link Image}, or {@code null} if not found on the classpath
     */
    public Image loadDefaultAvatar(double requestedSizePx) {
        URL url = AvatarService.class.getResource(DEFAULT_AVATAR_RESOURCE);
        if (url == null) return null;

        // Width/height hints keep memory reasonable and speed up decode
        return new Image(url.toExternalForm(), requestedSizePx, requestedSizePx, true, true);
    }

    // Apply image

    /**
     * Loads and applies the user's avatar to the given {@link ImageView}, falling back
     * to {@code fallbackDefault}.
     * The image is cropped to a square and displayed as a circle.
     * @param view target ImageView
     * @param user the user whose avatar to load
     * @param sizePx target rendered size in pixels
     * @param fallbackDefault optional fallback image (can be {@code null})
     */
    public void applyUserAvatarOrDefault(ImageView view, User user, double sizePx, Image fallbackDefault) {
        Image img = findAvatarFile(user)
                .map(p -> new Image(p.toUri().toString()))
                .orElseGet(() -> fallbackDefault != null ? fallbackDefault : loadDefaultAvatar(sizePx));

        if (img != null) {
            applyCircularAvatar(view, img, sizePx);
        }
    }

    /**
     * Applies a circular clip and square viewport to the given {@link ImageView} and sets its image.
     * @param iv target ImageView
     * @param img image to display
     * @param sizePx target rendered size in pixels
     */
    public void applyCircularAvatar(ImageView iv, Image img, double sizePx) {
        iv.setImage(img);

        double w = img.getWidth();
        double h = img.getHeight();
        double s = Math.min(w, h);
        double x = (w - s) / 2.0;
        double y = (h - s) / 2.0;
        iv.setViewport(new Rectangle2D(x, y, s, s));

        iv.setFitWidth(sizePx);
        iv.setFitHeight(sizePx);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);

        Circle clip = new Circle(sizePx / 2.0, sizePx / 2.0, sizePx / 2.0);
        iv.setClip(clip);

        // Keep clip centred if the ImageView gets resized by layout
        iv.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            double cx = newB.getWidth() / 2.0;
            double cy = newB.getHeight() / 2.0;
            double r = Math.min(newB.getWidth(), newB.getHeight()) / 2.0;
            clip.setCenterX(cx);
            clip.setCenterY(cy);
            clip.setRadius(r);
        });
    }

    // Helpers

    /**
     * Getter for the base avatar directory path
     * @return the base avatars directory path
     */
    public static Path getAvatarsDir() { return AVATARS_DIR; }

    /**
     * Computes a filesystem-safe base filename for the user's avatar.
     * @param u the user (can be {@code null})
     * @return base filename for avatar storing
     */
    public static String baseAvatarName(User u) {
        if (u != null && u.getUserId() > 0) return "u" + u.getUserId();
        String email = trim(u == null ? null : u.getEmail());
        return isBlank(email) ? "anonymous" : email.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Extracts file extension
     * @param name a file or path.
     * @return The lowercase file extension (including the dot), e.g. ".png".
     */
    private static String extLower(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i).toLowerCase() : "";
    }
}
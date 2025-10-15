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

public final class AvatarService {

    // Where user avatars are stored
    private static final Path AVATARS_DIR =
            Paths.get(System.getProperty("user.home"), ".studysnap", "avatars");

    // Supported formats
    private static final String[] AVATAR_FORMATS = {".png", ".jpg", ".jpeg", ".gif"};

    // Classpath resource of the default avatar image
    private static final String DEFAULT_AVATAR_RESOURCE =
            "/com/app/studysnap/images/default_avatar.png";

    public AvatarService() {}

    // Load, save, delete

    // Returns the path to an existing avatar file for the user, if any.
    public Optional<Path> findAvatarFile(User user) {
        String base = baseAvatarName(user);
        for (String ext : AVATAR_FORMATS) {
            Path p = AVATARS_DIR.resolve(base + ext);
            if (Files.exists(p)) return Optional.of(p);
        }
        return Optional.empty();
    }

    // Saves a chosen image file as the user's avatar (replacing any previous).
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

    // Deletes any avatar files for the given user.
    public void deleteAvatar(User user) throws Exception {
        String base = baseAvatarName(user);
        for (String e : AVATAR_FORMATS) {
            Files.deleteIfExists(AVATARS_DIR.resolve(base + e));
        }
    }

    // Loads the default avatar image from the classpath.
    public Image loadDefaultAvatar(double requestedSizePx) {
        URL url = AvatarService.class.getResource(DEFAULT_AVATAR_RESOURCE);
        if (url == null) return null;
        // Width/height hints keep memory reasonable and speed up decode
        return new Image(url.toExternalForm(), requestedSizePx, requestedSizePx, true, true);
    }

    // Apply image

    // Loads the user's avatar if present; otherwise uses the provided fallback image
    public void applyUserAvatarOrDefault(ImageView view, User user, double sizePx, Image fallbackDefault) {
        Image img = findAvatarFile(user)
            .map(p -> new Image(p.toUri().toString()))
            .orElseGet(() -> fallbackDefault != null ? fallbackDefault : loadDefaultAvatar(sizePx));

        if (img != null) {
            applyCircularAvatar(view, img, sizePx);
        }
    }

    // Applies circular clip to an ImageView and sets its image.
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

    public static Path getAvatarsDir() { return AVATARS_DIR; }

    public static String baseAvatarName(User u) {
        if (u != null && u.getUserId() > 0) return "u" + u.getUserId();
        String email = safe(u == null ? null : u.getEmail());
        return email.isBlank() ? "anonymous" : email.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private static String extLower(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i).toLowerCase() : "";
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
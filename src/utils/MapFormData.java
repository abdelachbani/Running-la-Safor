package utils;

import java.io.File;

/**
 * Immutable value object that captures all the form fields needed to
 * add a new map region.  Centralises blank-checking, image-file
 * validation, coordinate parsing, and range validation that were
 * previously scattered across multiple primitive-heavy helper methods
 * in the controller.
 * <p>
 * Construct instances via the factory method {@link #parse}, which
 * returns either a valid {@code MapFormData} or a descriptive error
 * message.
 */
public final class MapFormData {

    private final String name;
    private final File imageFile;
    private final GeoBounds bounds;

    private MapFormData(String name, File imageFile, GeoBounds bounds) {
        this.name = name;
        this.imageFile = imageFile;
        this.bounds = bounds;
    }

    // ── accessors ────────────────────────────────────────────────────

    public String getName()      { return name; }
    public File   getImageFile() { return imageFile; }
    public GeoBounds getBounds() { return bounds; }

    // ── factory / validation ─────────────────────────────────────────

    /**
     * Parses raw form strings into a validated {@code MapFormData}.
     *
     * @return a {@link Result} that is either {@link Result#isOk() ok}
     *         (carrying the parsed data) or contains a user-facing
     *         {@link Result#getError() error message}.
     */
    public static Result parse(String name, String imagePath,
                               String latMinText, String latMaxText,
                               String lonMinText, String lonMaxText) {

        // ── blank check ──────────────────────────────────────────────
        if (isBlank(name) || isBlank(imagePath)
                || isBlank(latMinText) || isBlank(latMaxText)
                || isBlank(lonMinText) || isBlank(lonMaxText)) {
            return Result.error("Rellena todos los campos del mapa.");
        }

        // ── image file validation ────────────────────────────────────
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return Result.error("El fichero de imagen no existe.");
        }
        if (!imageFile.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            return Result.error("El fichero debe ser JPG, JPEG o PNG.");
        }

        // ── coordinate parsing ───────────────────────────────────────
        double latMin;
        double latMax;
        double lonMin;
        double lonMax;
        try {
            latMin = Double.parseDouble(latMinText);
            latMax = Double.parseDouble(latMaxText);
            lonMin = Double.parseDouble(lonMinText);
            lonMax = Double.parseDouble(lonMaxText);
        } catch (NumberFormatException ex) {
            return Result.error("Las coordenadas deben ser números válidos.");
        }

        // ── range validation ─────────────────────────────────────────
        GeoBounds bounds;
        try {
            bounds = new GeoBounds(latMin, latMax, lonMin, lonMax);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }

        return Result.ok(new MapFormData(name, imageFile, bounds));
    }

    // ── Result wrapper ───────────────────────────────────────────────

    /**
     * Simple success-or-error wrapper returned by {@link #parse}.
     */
    public static final class Result {
        private final MapFormData data;
        private final String error;

        private Result(MapFormData data, String error) {
            this.data = data;
            this.error = error;
        }

        static Result ok(MapFormData data)  { return new Result(data, null); }
        static Result error(String message) { return new Result(null, message); }

        public boolean isOk()          { return data != null; }
        public MapFormData getData()   { return data; }
        public String getError()       { return error; }
    }

    // ── private helpers ──────────────────────────────────────────────

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

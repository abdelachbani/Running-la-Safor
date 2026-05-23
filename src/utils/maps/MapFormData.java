package utils.maps;

import java.io.File;

/**
 * Immutable value object that captures all the validated fields needed
 * to add a new map region.  Centralises blank-checking, image-file
 * validation, coordinate parsing, and range validation.
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
     * Parses and validates raw form input into a {@code MapFormData}.
     *
     * @param input the raw form fields bundled in a {@link MapFormInput}
     * @return a {@link Result} that is either {@link Result#isOk() ok}
     *         (carrying the parsed data) or contains a user-facing
     *         {@link Result#getError() error message}.
     */
    public static Result parse(MapFormInput input) {
        if (input.hasBlankField()) {
            return Result.error("Rellena todos los campos del mapa.");
        }

        String imageError = validateImageFile(input);
        if (imageError != null) {
            return Result.error(imageError);
        }

        try {
            GeoBounds bounds = parseBounds(input);
            return Result.ok(new MapFormData(
                    input.getName(), new File(input.getImagePath()), bounds));
        } catch (NumberFormatException ex) {
            return Result.error("Las coordenadas deben ser números válidos.");
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    // ── private validation helpers ───────────────────────────────────

    /**
     * Validates that the image file exists and has an accepted extension.
     *
     * @return an error message, or {@code null} if the file is valid.
     */
    private static String validateImageFile(MapFormInput input) {
        File file = new File(input.getImagePath());
        if (!file.exists()) {
            return "El fichero de imagen no existe.";
        }
        if (!file.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            return "El fichero debe ser JPG, JPEG o PNG.";
        }
        return null;
    }

    /**
     * Parses the four coordinate strings from the input and constructs
     * a validated {@link GeoBounds}.
     *
     * @throws NumberFormatException    if any coordinate is not numeric
     * @throws IllegalArgumentException if min ≥ max for lat or lon
     */
    private static GeoBounds parseBounds(MapFormInput input) {
        return new GeoBounds(
                Double.parseDouble(input.getLatMinText()),
                Double.parseDouble(input.getLatMaxText()),
                Double.parseDouble(input.getLonMinText()),
                Double.parseDouble(input.getLonMaxText()));
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
}

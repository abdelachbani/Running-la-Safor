package utils.maps;

import java.util.stream.Stream;

/**
 * Immutable value object that bundles the raw string fields from the
 * "add map" form.  This replaces the six individual {@code String}
 * parameters that were passed to {@link MapFormData#parse}, eliminating
 * both the excess-arguments and string-heavy-arguments code smells.
 * <p>
 * Construct instances via the fluent {@link #builder()}.
 */
public final class MapFormInput {

    private final String name;
    private final String imagePath;
    private final String latMinText;
    private final String latMaxText;
    private final String lonMinText;
    private final String lonMaxText;

    private MapFormInput(Builder builder) {
        this.name       = builder.name;
        this.imagePath  = builder.imagePath;
        this.latMinText = builder.latMinText;
        this.latMaxText = builder.latMaxText;
        this.lonMinText = builder.lonMinText;
        this.lonMaxText = builder.lonMaxText;
    }

    // ── accessors ────────────────────────────────────────────────────

    public String getName()       { return name; }
    public String getImagePath()  { return imagePath; }
    public String getLatMinText() { return latMinText; }
    public String getLatMaxText() { return latMaxText; }
    public String getLonMinText() { return lonMinText; }
    public String getLonMaxText() { return lonMaxText; }

    /**
     * Returns {@code true} if any of the six fields is null or blank.
     */
    public boolean hasBlankField() {
        return Stream.of(name, imagePath, latMinText, latMaxText,
                         lonMinText, lonMaxText)
                .anyMatch(s -> s == null || s.isBlank());
    }

    // ── builder ──────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name       = "";
        private String imagePath  = "";
        private String latMinText = "";
        private String latMaxText = "";
        private String lonMinText = "";
        private String lonMaxText = "";

        Builder() { }

        public Builder name(String name)          { this.name = name;       return this; }
        public Builder imagePath(String path)     { this.imagePath = path;  return this; }
        public Builder latMin(String text)         { this.latMinText = text; return this; }
        public Builder latMax(String text)         { this.latMaxText = text; return this; }
        public Builder lonMin(String text)         { this.lonMinText = text; return this; }
        public Builder lonMax(String text)         { this.lonMaxText = text; return this; }

        public MapFormInput build() { return new MapFormInput(this); }
    }
}

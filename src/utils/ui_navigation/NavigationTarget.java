package utils.ui_navigation;

/**
 * Encapsulates all the parameters needed for a scene navigation,
 * keeping the {@link NavigationUtils#navigateTo} signature to just
 * two arguments (ActionEvent + NavigationTarget).
 * <p>
 * Use the fluent builder obtained via {@link #to(String)} to construct
 * instances, for example:
 * <pre>
 *   NavigationTarget.to("/view/Home.fxml")
 *       .minSize(900, 600)
 *       .onError("Could not open the screen.")
 *       .build();
 * </pre>
 */
public final class NavigationTarget {

    private final String fxmlPath;
    private final String errorMsg;
    private final double minWidth;
    private final double minHeight;
    private final double width;
    private final double height;
    private final boolean centerOnScreen;

    private NavigationTarget(Builder builder) {
        this.fxmlPath = builder.fxmlPath;
        this.errorMsg = builder.errorMsg;
        this.minWidth = builder.minWidth;
        this.minHeight = builder.minHeight;
        this.width = builder.width;
        this.height = builder.height;
        this.centerOnScreen = builder.centerOnScreen;
    }

    public String getFxmlPath()    { return fxmlPath; }
    public String getErrorMsg()    { return errorMsg; }
    public double getMinWidth()    { return minWidth; }
    public double getMinHeight()   { return minHeight; }
    public double getWidth()       { return width; }
    public double getHeight()      { return height; }
    public boolean isCenterOnScreen() { return centerOnScreen; }

    /** Returns {@code true} when an explicit window size was configured. */
    public boolean hasExplicitSize() {
        return width > 0 && height > 0;
    }

    /**
     * Starts building a {@code NavigationTarget} for the given FXML view.
     *
     * @param fxmlPath classpath to the FXML file, e.g. "/view/Home.fxml"
     * @return a new {@link Builder}
     */
    public static Builder to(String fxmlPath) {
        return new Builder(fxmlPath);
    }

    /**
     * Fluent builder for {@link NavigationTarget}.
     * Every setter returns {@code this} so calls can be chained.
     */
    public static final class Builder {

        private final String fxmlPath;
        private String errorMsg = "";
        private double minWidth;
        private double minHeight;
        private double width  = -1;
        private double height = -1;
        private boolean centerOnScreen;

        private Builder(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }

        /** Sets the minimum stage dimensions. */
        public Builder minSize(double w, double h) {
            this.minWidth  = w;
            this.minHeight = h;
            return this;
        }

        /** Sets an explicit stage size (also un-maximises the window). */
        public Builder size(double w, double h) {
            this.width  = w;
            this.height = h;
            return this;
        }

        /** Centres the stage on screen after navigation. */
        public Builder center() {
            this.centerOnScreen = true;
            return this;
        }

        /** Error message shown if the FXML cannot be loaded. */
        public Builder onError(String msg) {
            this.errorMsg = msg;
            return this;
        }

        /** Builds an immutable {@link NavigationTarget}. */
        public NavigationTarget build() {
            return new NavigationTarget(this);
        }
    }
}

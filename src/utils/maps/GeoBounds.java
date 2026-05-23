package utils.maps;

/**
 * Immutable value object that represents a geographic bounding box
 * defined by latitude and longitude ranges.
 * <p>
 * Replaces the four loose {@code double} parameters
 * ({@code latMin, latMax, lonMin, lonMax}) that were passed between
 * validation and map-creation methods, eliminating primitive obsession.
 */
public final class GeoBounds {

    private final double latMin;
    private final double latMax;
    private final double lonMin;
    private final double lonMax;

    /**
     * Creates a new {@code GeoBounds}.
     *
     * @throws IllegalArgumentException if {@code latMin >= latMax}
     *                                  or {@code lonMin >= lonMax}
     */
    public GeoBounds(double latMin, double latMax,
                     double lonMin, double lonMax) {
        if (latMin >= latMax) {
            throw new IllegalArgumentException(
                    "La latitud mínima debe ser menor que la máxima.");
        }
        if (lonMin >= lonMax) {
            throw new IllegalArgumentException(
                    "La longitud mínima debe ser menor que la máxima.");
        }
        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin = lonMin;
        this.lonMax = lonMax;
    }

    public double getLatMin() { return latMin; }
    public double getLatMax() { return latMax; }
    public double getLonMin() { return lonMin; }
    public double getLonMax() { return lonMax; }

    @Override
    public String toString() {
        return String.format("GeoBounds[lat=%.6f..%.6f, lon=%.6f..%.6f]",
                latMin, latMax, lonMin, lonMax);
    }
}

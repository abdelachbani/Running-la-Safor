package utils;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Centralises FXML scene‐switching logic so that every controller can
 * navigate with a single method call instead of duplicating the
 * load → scene → stylesheet → stage boilerplate.
 */
public final class NavigationUtils {

    private static final String STYLESHEET = "/resources/styles.css";

    private NavigationUtils() {
        // utility class – not instantiable
    }

    /**
     * Loads an FXML view, applies the shared stylesheet, and replaces the
     * current scene on the stage obtained from {@code event}.
     *
     * @param event      the originating ActionEvent (used to obtain the Stage)
     * @param fxmlPath   absolute classpath to the FXML file, e.g. "/view/Home.fxml"
     * @param minWidth   minimum stage width after navigation
     * @param minHeight  minimum stage height after navigation
     * @param errorMsg   message shown in an error alert if loading fails
     */
    public static void navigateTo(ActionEvent event, String fxmlPath,
                                  double minWidth, double minHeight,
                                  String errorMsg) {
        try {
            Parent root = FXMLLoader.load(
                    NavigationUtils.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    NavigationUtils.class.getResource(STYLESHEET).toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.show();
        } catch (IOException e) {
            AlertUtils.showError("Error", errorMsg);
        }
    }

    /**
     * Overload that also sets an explicit window size and centres the stage.
     * Useful when the target view requires fixed dimensions (e.g. Login).
     *
     * @param event      the originating ActionEvent
     * @param fxmlPath   absolute classpath to the FXML file
     * @param width      explicit stage width
     * @param height     explicit stage height
     * @param minWidth   minimum stage width
     * @param minHeight  minimum stage height
     * @param errorMsg   message shown in an error alert if loading fails
     */
    public static void navigateTo(ActionEvent event, String fxmlPath,
                                  double width, double height,
                                  double minWidth, double minHeight,
                                  String errorMsg) {
        try {
            Parent root = FXMLLoader.load(
                    NavigationUtils.class.getResource(fxmlPath));
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                    NavigationUtils.class.getResource(STYLESHEET).toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            AlertUtils.showError("Error", errorMsg);
        }
    }
}

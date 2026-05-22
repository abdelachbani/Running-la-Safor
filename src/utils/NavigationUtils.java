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
     * Loads an FXML view described by {@code target}, applies the shared
     * stylesheet, and replaces the current scene on the stage obtained
     * from {@code event}.
     *
     * @param event  the originating ActionEvent (used to obtain the Stage)
     * @param target a {@link NavigationTarget} holding the FXML path,
     *               dimensions, and error message
     */
    public static void navigateTo(ActionEvent event, NavigationTarget target) {
        try {
            Parent root = FXMLLoader.load(
                    NavigationUtils.class.getResource(target.getFxmlPath()));

            Scene scene;
            if (target.hasExplicitSize()) {
                scene = new Scene(root, target.getWidth(), target.getHeight());
            } else {
                scene = new Scene(root);
            }
            scene.getStylesheets().add(
                    NavigationUtils.class.getResource(STYLESHEET).toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            if (target.hasExplicitSize()) {
                stage.setMaximized(false);
            }

            stage.setScene(scene);

            if (target.hasExplicitSize()) {
                stage.setWidth(target.getWidth());
                stage.setHeight(target.getHeight());
            }

            stage.setMinWidth(target.getMinWidth());
            stage.setMinHeight(target.getMinHeight());

            if (target.isCenterOnScreen()) {
                stage.centerOnScreen();
            }

            stage.show();
        } catch (IOException e) {
            AlertUtils.showError("Error", target.getErrorMsg());
        }
    }
}

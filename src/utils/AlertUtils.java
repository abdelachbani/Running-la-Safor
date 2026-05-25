package utils;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Simple helpers for showing JavaFX alert dialogs. Eliminates the repeated
 * showInfo / showWarning / showError methods that were copy‐pasted into every
 * controller.
 */
public final class AlertUtils {

    private AlertUtils() {
        // utility class – not instantiable
    }

    /**
     * Shows an INFORMATION alert with the given title and message.
     */
    public static void showInfo(String title, String text) {
        showAlert(Alert.AlertType.INFORMATION, title, text);
    }

    /**
     * Shows a WARNING alert with the given title and message.
     */
    public static void showWarning(String title, String text) {
        showAlert(Alert.AlertType.WARNING, title, text);
    }

    /**
     * Shows an ERROR alert with the given title and message.
     */
    public static void showError(String title, String text) {
        showAlert(Alert.AlertType.ERROR, title, text);
    }

    private static void showAlert(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(
                new Image(AlertUtils.class.getResource("/resources/logo.png").toString()));
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}

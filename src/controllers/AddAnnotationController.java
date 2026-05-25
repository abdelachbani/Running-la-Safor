package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.SportActivityApp;
import java.util.ResourceBundle;
import utils.AlertUtils;
import utils.AvatarUtils;
import utils.ui_navigation.NavigationTarget;
import utils.ui_navigation.NavigationUtils;

/**
 * FXML Controller class
 *
 * @author Ikerc
 */
public class AddAnnotationController implements Initializable {

    @FXML
    private RadioButton rbPoint;
    @FXML
    private RadioButton rbText;
    @FXML
    private RadioButton rbLine;
    @FXML
    private RadioButton rbCircle;
    @FXML
    private TextField txtAnnotationText;
    @FXML
    private TextField txtColor;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Label usernameLabel;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private ToggleGroup tipoGroup;

    private final SportActivityApp app = SportActivityApp.getInstance();

    private Activity currentActivity;
    private List<GeoPoint> geoPoints;
    @FXML
    private ComboBox<String> comboStrokeWidth;

    public void setData(Activity activity, List<GeoPoint> geoPoints) {
        this.currentActivity = activity;
        this.geoPoints = geoPoints;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboStrokeWidth.getItems().addAll("2", "3", "4", "5");
        comboStrokeWidth.setValue("2");

        colorPicker.setValue(Color.web("#E74C3C"));

        txtColor.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                colorPicker.setValue(Color.web(newVal));
            } catch (Exception ignored) {
            }
        });

        if (app.getCurrentUser() != null) {
            usernameLabel.setText(app.getCurrentUser().getNickName());
            loadAvatar();
        }
    }

    private void loadAvatar() {
        Image avatar = app.getCurrentUser().getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }

        AvatarUtils.applyCircularAvatar(avatarImageView, avatar);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateBackToDetails(event);
    }

    @FXML
    private void handleColorPickerChange(ActionEvent event) {
        Color c = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
        txtColor.setText(hex);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateBackToDetails(event);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!isStateValid()) {
            AlertUtils.showError("Error", "No hay actividad o puntos geográficos seleccionados.");
            return;
        }

        AnnotationType type = getSelectedType();
        if (!arePointsSufficient(type)) {
            return;
        }

        Double strokeWidth = getValidStrokeWidth();
        if (strokeWidth == null) {
            return;
        }

        processSave(type, strokeWidth, event);
    }

    private boolean isStateValid() {
        if (currentActivity == null) {
            return false;
        }
        if (geoPoints == null) {
            return false;
        }
        return !geoPoints.isEmpty();
    }

    private boolean arePointsSufficient(AnnotationType type) {
        int requiredPoints = 2;
        if (type == AnnotationType.POINT) {
            requiredPoints = 1;
        } else if (type == AnnotationType.TEXT) {
            requiredPoints = 1;
        }

        if (geoPoints.size() < requiredPoints) {
            AlertUtils.showError("Puntos insuficientes",
                    "Este tipo de anotación necesita " + requiredPoints + " punto(s) en el mapa.");
            return false;
        }
        return true;
    }

    private Double getValidStrokeWidth() {
    String raw;

    // Si es editable, el texto escrito está en el editor
    if (comboStrokeWidth.isEditable()) {
        raw = comboStrokeWidth.getEditor().getText().trim();
    } else {
        Object val = comboStrokeWidth.getValue();
        raw = val != null ? val.toString().trim() : "";
    }

    if (raw.isEmpty()) {
        AlertUtils.showError("Grosor inválido", "El grosor no puede estar vacío.");
        return null;
    }

    try {
        double strokeWidth = Double.parseDouble(raw);
        if (strokeWidth <= 0) {
            AlertUtils.showError("Grosor inválido", "El grosor debe ser mayor que 0.");
            return null;
        }
        return strokeWidth;
    } catch (NumberFormatException e) {
        AlertUtils.showError("Grosor inválido", "Introduce un número válido para el grosor.");
        return null;
    }
    }

    private void processSave(AnnotationType type, double strokeWidth, ActionEvent event) {
        String color = txtColor.getText().replace("#", "");
        Annotation ann = new Annotation(type, txtAnnotationText.getText(), color, strokeWidth, geoPoints);
        Annotation saved = app.addAnnotation(currentActivity, ann);

        if (saved != null) {
            AlertUtils.showInfo("Anotación guardada", "La anotación se ha guardado correctamente.");
            handleBack(event);
        } else {
            AlertUtils.showError("Error", "No se pudo guardar la anotación.");
        }
    }

    private AnnotationType getSelectedType() {
        if (rbText.isSelected()) {
            return AnnotationType.TEXT;
        }
        if (rbLine.isSelected()) {
            return AnnotationType.LINE;
        }
        if (rbCircle.isSelected()) {
            return AnnotationType.CIRCLE;
        }
        return AnnotationType.POINT;
    }

    @FXML
    private void handleLogOut(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
    }

    public void handleWindowChange(String path, int minWidth, int minHeight, ActionEvent event) {
        URL homeView = getClass().getResource(path);
        URL styles = getClass().getResource("/resources/styles.css");

        if (homeView == null || styles == null) {
            AlertUtils.showError("Error", "No se pudo volver a la pantalla principal.");
            return;
        }

        try {
            Parent root = FXMLLoader.load(homeView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();

            if (currentScene != null) {
                currentScene.setRoot(root);
                currentScene.getStylesheets().setAll(styles.toExternalForm());
            } else {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(styles.toExternalForm());
                stage.setScene(scene);
            }

            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);

            if (!wasMaximized) {
                stage.centerOnScreen();
            }
            stage.show();
        } catch (IOException ex) {
            AlertUtils.showError("Error", "No se pudo volver a la pantalla principal.");
        }
    }

    private void navigateBackToDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/ActivityDetails.fxml")
            );
            Parent root = loader.load();

            ActivityDetailsController controller = loader.getController();
            controller.setActivity(currentActivity);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            Scene currentScene = stage.getScene();
            URL styles = getClass().getResource("/resources/styles.css");

            if (currentScene != null) {
                currentScene.setRoot(root);
                currentScene.getStylesheets().setAll(styles.toExternalForm());
            } else {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(styles.toExternalForm());
                stage.setScene(scene);
            }

            stage.setMinWidth(1200);
            stage.setMinHeight(780);

            if (!wasMaximized) {
                stage.centerOnScreen();
            }
            stage.show();

        } catch (IOException e) {
            AlertUtils.showError("Error", "No se pudo volver a la actividad.");
        }
    }
}

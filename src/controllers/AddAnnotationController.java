/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
    private Label lblUser;
    @FXML
    private ImageView imgUser;
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
        
        // Sincronizar TextField de color con ColorPicker
        txtColor.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                colorPicker.setValue(Color.web(newVal));
            } catch (Exception ignored) {}
        });
        
        if (app.getCurrentUser() != null) {
            lblUser.setText(app.getCurrentUser().getNickName());
            loadAvatar();
        }
    }
    
    private void loadAvatar() {
        Image avatar = app.getCurrentUser().getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }

        AvatarUtils.applyCircularAvatar(imgUser, avatar);
    }
    
    
      

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtils.navigateTo(event, NavigationTarget.to("/view/ActivityDetails.fxml")
                .minSize(1200, 780)
                .onError("No se pudo abrir la pantalla de visualización de actividad.")
                .build());
    }
    


    @FXML
    private void handleColorPickerChange(ActionEvent event) {
        Color c = colorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue() * 255));
        txtColor.setText(hex);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
       handleBack(event);
    }

    @FXML
    private void handleSave(ActionEvent event) {
          
        if (currentActivity == null || geoPoints == null || geoPoints.isEmpty()) {
            showError("Error", "No hay actividad o puntos geográficos seleccionados.");
            return;
        }

       
        AnnotationType type = getSelectedType();

        // Validar número de GeoPoints según el tipo
        int requiredPoints = (type == AnnotationType.POINT || type == AnnotationType.TEXT) ? 1 : 2;
        if (geoPoints.size() < requiredPoints) {
            showError("Puntos insuficientes",
                "Este tipo de anotación necesita " + requiredPoints + " punto(s) en el mapa.");
            return;
        }

        
        String color = txtColor.getText().replace("#", "");
        String raw;
        double strokeWidth;
        try {
            raw = comboStrokeWidth.getValue().trim();
            strokeWidth = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            showError("Grosor inválido", "Introduce un número válido para el grosor.");
            return;
        }

        // Crear y guardar la anotación con la librería
        Annotation ann = new Annotation(
            type,
            txtAnnotationText.getText(),
            color,
            strokeWidth,
            geoPoints
        );

        Annotation saved = app.addAnnotation(currentActivity, ann);

        if (saved != null) {
            showInfo("Anotación guardada", "La anotación se ha guardado correctamente.");
            handleBack(event); // Volver a la pantalla anterior
        } else {
            showError("Error", "No se pudo guardar la anotación.");
        }
    }
    
    private AnnotationType getSelectedType() {
        if (rbText.isSelected())   return AnnotationType.TEXT;
        if (rbLine.isSelected())   return AnnotationType.LINE;
        if (rbCircle.isSelected()) return AnnotationType.CIRCLE;
        return AnnotationType.POINT; // por defecto
    }
    

    @FXML
    private void handleLogOut(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
    }
    
     public void handleWindowChange(String path, int minWidth, int minHeight, ActionEvent event) {
        URL homeView = getClass().getResource(path);
        URL styles = getClass().getResource("/resources/styles.css");

        if (homeView == null || styles == null) {
            showError("Error", "No se pudo volver a la pantalla principal.");
            return;
        }

        try {
            Parent root = FXMLLoader.load(homeView);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(styles.toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.show();
        } catch (IOException ex) {
            showError("Error", "No se pudo volver a la pantalla principal.");
        }
    }
     
     private void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
     
      private void showInfo(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}


package controllers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;
import utils.AvatarUtils;

public class MapManagementController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutButton;
    @FXML private Button backButton;

    @FXML private Label mapsCountLabel;
    @FXML private Label scriptHintLabel;

    @FXML private TableView<MapRegion> mapsTable;
    @FXML private TableColumn<MapRegion, String> nameColumn;
    @FXML private TableColumn<MapRegion, String> imageColumn;
    @FXML private TableColumn<MapRegion, String> statusColumn;

    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    @FXML private ImageView previewImageView;
    @FXML private Label previewPlaceholderLabel;

    @FXML private Label selectedNameLabel;
    @FXML private Label selectedPathLabel;
    @FXML private Label selectedLatMinLabel;
    @FXML private Label selectedLatMaxLabel;
    @FXML private Label selectedLonMinLabel;
    @FXML private Label selectedLonMaxLabel;
    @FXML private Label selectedStatusLabel;

    @FXML private TextField nameField;
    @FXML private TextField imagePathField;
    @FXML private TextField latMinField;
    @FXML private TextField latMaxField;
    @FXML private TextField lonMinField;
    @FXML private TextField lonMaxField;

    @FXML private Button browseButton;
    @FXML private Button addButton;

    private final SportActivityApp app = SportActivityApp.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        titleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("top-action-button");
        backButton.getStyleClass().add("nav-button");
        refreshButton.getStyleClass().add("table-action-button");
        deleteButton.getStyleClass().add("table-action-button");
        browseButton.getStyleClass().add("nav-button");
        addButton.getStyleClass().add("import-button");

        mapsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mapsTable.getStyleClass().add("activities-table");

        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeText(cell.getValue().getName())));

        imageColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(safeText(cell.getValue().getImagePath())));

        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(isRemovable(cell.getValue()) ? "Eliminable" : "En uso"));

        mapsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            updateSelectedMapView(newSel);
        });

        mapsTable.setPlaceholder(new Label("No hay mapas registrados."));
        deleteButton.disableProperty().bind(mapsTable.getSelectionModel().selectedItemProperty().isNull());

        loadUserData();
        loadMaps();
        updateSelectedMapView(mapsTable.getSelectionModel().getSelectedItem());
    }

    private void loadUserData() {
        User currentUser = app.getCurrentUser();
        if (currentUser == null) {
            usernameLabel.setText("Usuario");
            AvatarUtils.applyCircularAvatar(avatarImageView, AvatarUtils.getDefaultAvatar());
            return;
        }

        usernameLabel.setText(currentUser.getNickName());

        Image avatar = currentUser.getAvatar();
        if (avatar == null) {
            avatar = AvatarUtils.getDefaultAvatar();
        }
        AvatarUtils.applyCircularAvatar(avatarImageView, avatar);
    }

    private void loadMaps() {
        List<MapRegion> maps = app.getMapRegions();
        mapsTable.getItems().setAll(maps);
        mapsCountLabel.setText("Mapas: " + maps.size());
        if (!maps.isEmpty() && mapsTable.getSelectionModel().getSelectedItem() == null) {
            mapsTable.getSelectionModel().selectFirst();
        }
    }

    private void updateSelectedMapView(MapRegion region) {
        if (region == null) {
            previewImageView.setImage(null);
            previewPlaceholderLabel.setVisible(true);

            selectedNameLabel.setText("-");
            selectedPathLabel.setText("-");
            selectedLatMinLabel.setText("-");
            selectedLatMaxLabel.setText("-");
            selectedLonMinLabel.setText("-");
            selectedLonMaxLabel.setText("-");
            selectedStatusLabel.setText("-");
            return;
        }

        selectedNameLabel.setText(safeText(region.getName()));
        selectedPathLabel.setText(safeText(region.getImagePath()));
        selectedLatMinLabel.setText(String.format("%.6f", region.getLatMin()));
        selectedLatMaxLabel.setText(String.format("%.6f", region.getLatMax()));
        selectedLonMinLabel.setText(String.format("%.6f", region.getLonMin()));
        selectedLonMaxLabel.setText(String.format("%.6f", region.getLonMax()));
        selectedStatusLabel.setText(isRemovable(region) ? "Eliminable" : "En uso");

        File imageFile = new File(region.getImagePath());
        if (imageFile.exists()) {
            previewImageView.setImage(new Image(imageFile.toURI().toString()));
            previewPlaceholderLabel.setVisible(false);
        } else {
            previewImageView.setImage(null);
            previewPlaceholderLabel.setVisible(true);
            previewPlaceholderLabel.setText("No se ha encontrado la imagen del mapa");
        }
    }

    @FXML
    private void handleBrowseMap(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar mapa");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes JPG/JPEG/PNG", "*.jpg", "*.jpeg", "*.png")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selected = chooser.showOpenDialog(stage);
        if (selected != null) {
            imagePathField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleAddMap(ActionEvent event) {
        String name = text(nameField);
        String imagePath = text(imagePathField);
        String latMinText = text(latMinField);
        String latMaxText = text(latMaxField);
        String lonMinText = text(lonMinField);
        String lonMaxText = text(lonMaxField);

        if (name.isBlank() || imagePath.isBlank() || latMinText.isBlank() || latMaxText.isBlank()
                || lonMinText.isBlank() || lonMaxText.isBlank()) {
            showError("Rellena todos los campos del mapa.");
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            showError("El fichero de imagen no existe.");
            return;
        }

        if (!imageFile.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            showError("El fichero debe ser JPG, JPEG o PNG.");
            return;
        }

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
            showError("Las coordenadas deben ser números válidos.");
            return;
        }

        if (latMin >= latMax) {
            showError("La latitud mínima debe ser menor que la máxima.");
            return;
        }
        if (lonMin >= lonMax) {
            showError("La longitud mínima debe ser menor que la máxima.");
            return;
        }

        MapRegion created = app.addMapRegion(name, imageFile, latMin, latMax, lonMin, lonMax);
        if (created == null) {
            showError("No se pudo añadir el mapa. Revisa el nombre y las coordenadas.");
            return;
        }

        showInfo("Mapa añadido correctamente.\n\nRecuerda que las coordenadas deben ser exactamente las que imprime el script generar_mapas_hd.py.");
        clearForm();
        loadMaps();
        selectMapByName(created.getName());
    }

    @FXML
    private void handleDeleteMap(ActionEvent event) {
        MapRegion selected = mapsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Selecciona un mapa", "Debes seleccionar un mapa para eliminarlo.");
            return;
        }

        if (!isRemovable(selected)) {
            showWarning("Mapa en uso", "Este mapa está referenciado por alguna actividad y no puede eliminarse.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("Eliminar mapa");
        confirm.setContentText("¿Quieres eliminar \"" + selected.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            app.removeMapRegion(selected);
        } catch (Exception ex) {
            showError("No se pudo eliminar el mapa.");
            return;
        }

        loadMaps();
        showInfo("Mapa eliminado correctamente.");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMaps();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.show();
        } catch (IOException ex) {
            showError("No se pudo volver a la pantalla principal.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        app.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException ex) {
            showError("No se pudo cerrar sesión.");
        }
    }

    private boolean isRemovable(MapRegion region) {
        if (region == null) {
            return false;
        }
        return app.getUnusedMapRegions().stream()
                .anyMatch(r -> r != null && safeText(r.getName()).equals(safeText(region.getName())));
    }

    private void selectMapByName(String name) {
        for (MapRegion region : mapsTable.getItems()) {
            if (region != null && safeText(region.getName()).equals(safeText(name))) {
                mapsTable.getSelectionModel().select(region);
                mapsTable.scrollTo(region);
                return;
            }
        }
    }

    private void clearForm() {
        nameField.clear();
        imagePathField.clear();
        latMinField.clear();
        latMaxField.clear();
        lonMinField.clear();
        lonMaxField.clear();
    }

    private String safeText(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
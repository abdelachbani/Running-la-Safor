package controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import utils.AlertUtils;
import utils.AvatarUtils;
import utils.GeoBounds;
import utils.MapFormData;
import utils.NavigationTarget;
import utils.NavigationUtils;

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
        MapFormData.Result result = MapFormData.parse(
                text(nameField), text(imagePathField),
                text(latMinField), text(latMaxField),
                text(lonMinField), text(lonMaxField));

        if (!result.isOk()) {
            AlertUtils.showError("Error", result.getError());
            return;
        }

        MapFormData form = result.getData();
        GeoBounds bounds = form.getBounds();

        MapRegion created = app.addMapRegion(
                form.getName(), form.getImageFile(),
                bounds.getLatMin(), bounds.getLatMax(),
                bounds.getLonMin(), bounds.getLonMax());

        if (created == null) {
            AlertUtils.showError("Error", "No se pudo añadir el mapa. Revisa el nombre y las coordenadas.");
            return;
        }

        AlertUtils.showInfo("Información",
                "Mapa añadido correctamente.\n\nRecuerda que las coordenadas deben ser "
                + "exactamente las que imprime el script generar_mapas_hd.py.");
        clearForm();
        loadMaps();
        selectMap(created);
    }

    @FXML
    private void handleDeleteMap(ActionEvent event) {
        MapRegion selected = mapsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Selecciona un mapa", "Debes seleccionar un mapa para eliminarlo.");
            return;
        }

        if (!isRemovable(selected)) {
            AlertUtils.showWarning("Mapa en uso", "Este mapa está referenciado por alguna actividad y no puede eliminarse.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("Eliminar mapa");
        confirm.setContentText("¿Quieres eliminar \"" + selected.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || !result.get().equals(ButtonType.OK)) {
            return;
        }

        try {
            app.removeMapRegion(selected);
        } catch (Exception ex) {
            AlertUtils.showError("Error", "No se pudo eliminar el mapa.");
            return;
        }

        loadMaps();
        AlertUtils.showInfo("Información", "Mapa eliminado correctamente.");
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMaps();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtils.navigateTo(event, NavigationTarget.to("/view/Home.fxml")
                .minSize(900, 600)
                .onError("No se pudo volver a la pantalla principal.")
                .build());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
    }

    private boolean isRemovable(MapRegion region) {
        if (region == null) {
            return false;
        }
        return app.getUnusedMapRegions().stream()
                .anyMatch(r -> r != null && safeText(r.getName()).equals(safeText(region.getName())));
    }

    private void selectMap(MapRegion target) {
        for (MapRegion region : mapsTable.getItems()) {
            if (region != null && safeText(region.getName()).equals(safeText(target.getName()))) {
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

}
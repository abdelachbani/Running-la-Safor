package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Callback;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;
import upv.ipc.sportlib.User;
import utils.AlertUtils;
import utils.AvatarUtils;
import utils.ui_navigation.NavigationTarget;
import utils.ui_navigation.NavigationUtils;

public class ActivityDetailsController implements Initializable {

    @FXML
    private Label titleLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private ImageView avatarImageView;

    @FXML
    private Button logoutButton;
    @FXML
    private Button backButton;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label mapStatusLabel;

    @FXML
    private Label distanceValueLabel;
    @FXML
    private Label durationValueLabel;
    @FXML
    private Label speedValueLabel;
    @FXML
    private Label paceValueLabel;
    @FXML
    private Label gainValueLabel;
    @FXML
    private Label lossValueLabel;
    @FXML
    private Label minElevationValueLabel;
    @FXML
    private Label maxElevationValueLabel;

    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private Pane mapContentPane;

    @FXML
    private TableView<Annotation> annotationTable;
    @FXML
    private TableColumn<Annotation, String> annotationTypeColumn;
    @FXML
    private TableColumn<Annotation, String> annotationTextColumn;
    @FXML
    private TableColumn<Annotation, String> annotationColorColumn;

    private final SportActivityApp app = SportActivityApp.getInstance();

    private Activity activity;
    private MapProjection projection;

    private Pane mapPane;
    private final Scale mapScale = new Scale(1, 1, 0, 0);

    private double currentMapWidth;
    private double currentMapHeight;

    private double dragStartX;
    private double dragStartY;
    private double dragStartH;
    private double dragStartV;

    private boolean isInitialZoomSet = false;

    private List<GeoPoint> selectedGeoPoints = new ArrayList<>();
    @FXML
    private Button DeleteAnnotationButton;
    @FXML
    private Button addAnnotationButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        annotationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        annotationTypeColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        cell.getValue() != null
                        && cell.getValue().getType() != null
                        ? cell.getValue().getType().name()
                        : "-"
                ));

        annotationTextColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        safeText(
                                cell.getValue() == null
                                ? null
                                : cell.getValue().getText()
                        )
                ));

        annotationColorColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        safeText(
                                cell.getValue() == null
                                ? null
                                : cell.getValue().getColor()
                        )
                ));

        annotationTextColumn.setCellFactory(createWrappingCellFactory());

        // We let the ScrollPane handle panning naturally
        // mapContentPane is now a Pane, no alignment needed
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal)
                -> applyZoom(newVal.doubleValue()));

        mapScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (activity != null && mapPane != null) {
                fitInitialZoom();
            }
        });

        loadUserData();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        renderActivity();
    }

    private void renderActivity() {
        if (activity == null) {
            return;
        }

        loadUserData();

        titleLabel.setText(
                "Running la Safor - "
                + safeText(activity.getName())
        );

        distanceValueLabel.setText(formatDistance(activity.getTotalDistance()));
        durationValueLabel.setText(formatDuration(activity.getDuration()));
        speedValueLabel.setText(String.format(
                Locale.ROOT, "%.2f km/h", activity.getAverageSpeed()));
        paceValueLabel.setText(String.format(
                Locale.ROOT, "%.2f min/km", activity.getAveragePace()));
        gainValueLabel.setText(String.format(
                Locale.ROOT, "%.0f m", activity.getElevationGain()));
        lossValueLabel.setText(String.format(
                Locale.ROOT, "%.0f m", activity.getElevationLoss()));
        minElevationValueLabel.setText(String.format(
                Locale.ROOT, "%.0f m", activity.getMinElevation()));
        maxElevationValueLabel.setText(String.format(
                Locale.ROOT, "%.0f m", activity.getMaxElevation()));

        annotationTable.getItems().setAll(activity.getAnnotations());

        MapRegion region = activity.getSuggestedMap();
        if (region == null) {
            showMapMessage("No se encontró mapa.");
            return;
        }

        File mapFile = new File(region.getImagePath());
        if (!mapFile.exists()) {
            showMapMessage("No existe la imagen.");
            return;
        }

        Image mapImage = new Image(mapFile.toURI().toString());

        currentMapWidth = mapImage.getWidth();
        currentMapHeight = mapImage.getHeight();

        projection = new MapProjection(
                region,
                currentMapWidth,
                currentMapHeight
        );

        ImageView mapView = new ImageView(mapImage);
        mapView.setFitWidth(currentMapWidth);
        mapView.setFitHeight(currentMapHeight);

        mapPane = new Pane();
        mapPane.setPrefSize(currentMapWidth, currentMapHeight);
        mapPane.setMinSize(currentMapWidth, currentMapHeight);
        mapPane.setMaxSize(currentMapWidth, currentMapHeight);

        mapPane.getChildren().add(mapView);

        drawRoute();
        drawEndpoints();
        drawAnnotations();

        Rectangle mapBounds = new Rectangle(currentMapWidth, currentMapHeight);
        mapPane.setClip(mapBounds);
        mapPane.setShape(mapBounds);

        mapPane.getTransforms().add(mapScale);
        Group contentGroup = new Group(mapPane);
        mapScrollPane.setContent(contentGroup);

        isInitialZoomSet = false;
        Platform.runLater(this::fitInitialZoom);

        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                GeoPoint geo = projection.unproject(e.getX(), e.getY());
                selectedGeoPoints.add(geo);

                Circle marker = new Circle(e.getX(), e.getY(), 5, Color.ORANGE);
                marker.setStroke(Color.WHITE);
                mapPane.getChildren().add(marker);

                mapStatusLabel.setText("Puntos seleccionados: " + selectedGeoPoints.size());
            }
        });

        mapPane.setOnScroll(e -> {
            if (e.isControlDown()) {
                double delta = e.getDeltaY();
                if (delta > 0) {
                    zoomSlider.setValue(Math.min(zoomSlider.getValue() + 0.05, zoomSlider.getMax()));
                } else if (delta < 0) {
                    zoomSlider.setValue(Math.max(zoomSlider.getValue() - 0.05, zoomSlider.getMin()));
                }
                e.consume();
            }
        });
    }

    private void fitInitialZoom() {
        if (mapPane == null) {
            return;
        }

        Bounds viewport = mapScrollPane.getViewportBounds();
        double viewportW = viewport.getWidth();
        double viewportH = viewport.getHeight();

        if (!hasValidDimensions(viewportW, viewportH)) {
            return;
        }

        double scaleX = viewportW / currentMapWidth;
        double scaleY = viewportH / currentMapHeight;

        double initialZoom = sanitizeZoom(Math.max(scaleX, scaleY));

        zoomSlider.setMin(initialZoom);
        zoomSlider.setMax(Math.max(3.0, initialZoom * 3.0));

        if (!isInitialZoomSet) {
            zoomSlider.setValue(initialZoom);
            applyZoom(initialZoom);
            isInitialZoomSet = true;
        }
    }

    private boolean hasValidDimensions(double viewportW, double viewportH) {
        return viewportW > 0 && viewportH > 0
                && currentMapWidth > 0 && currentMapHeight > 0;
    }

    private double sanitizeZoom(double zoom) {
        return (Double.isFinite(zoom) && zoom > 0) ? zoom : 1.0;
    }

    private void applyZoom(double zoom) {
        if (mapPane == null) {
            return;
        }

        double scrollH = mapScrollPane.getHvalue();
        double scrollV = mapScrollPane.getVvalue();

        mapScale.setX(zoom);
        mapScale.setY(zoom);

        mapScrollPane.setHvalue(scrollH);
        mapScrollPane.setVvalue(scrollV);
    }

    private void drawRoute() {
        List<Point2D> points = projection.projectActivity(activity);
        if (points == null || points.isEmpty()) {
            return;
        }

        Polyline route = new Polyline();
        for (Point2D p : points) {
            route.getPoints().addAll(p.getX(), p.getY());
        }

        route.setStroke(Color.DODGERBLUE);
        route.setStrokeWidth(3);
        route.setFill(null);

        mapPane.getChildren().add(route);
    }

    private void drawEndpoints() {
        TrackPoint start = activity.getStartPoint();
        TrackPoint end = activity.getEndPoint();

        if (start != null) {
            addMarker(projection.project(start), Color.LIMEGREEN, "Inicio");
        }

        if (end != null) {
            addMarker(projection.project(end), Color.RED, "Fin");
        }
    }

    private void drawAnnotations() {
        for (Annotation annotation : activity.getAnnotations()) {
            if (!hasGeoPoints(annotation)) {
                continue;
            }

            Color color = safeColor(annotation.getColor(), Color.ORANGE);

            switch (annotation.getType()) {
                case POINT ->
                    drawPointAnnotation(annotation, color);
                case LINE ->
                    drawLineAnnotation(annotation, color);
                case TEXT ->
                    drawTextAnnotation(annotation, color);
                case CIRCLE ->
                    drawCircleAnnotation(annotation, color);
                default -> {
                }
            }
        }
    }

    private boolean hasGeoPoints(Annotation annotation) {
        return annotation != null
                && annotation.getGeoPoints() != null
                && !annotation.getGeoPoints().isEmpty();
    }

    private void drawPointAnnotation(Annotation annotation, Color color) {
        Point2D p = projection.project(annotation.getGeoPoints().get(0));
        Circle c = new Circle(p.getX(), p.getY(), 6);
        c.setFill(color);
        c.setStroke(Color.WHITE);
        mapPane.getChildren().add(c);

        c.setUserData("annotation");
    }

    private void drawLineAnnotation(Annotation annotation, Color color) {
        if (annotation.getGeoPoints().size() < 2) {
            return;
        }

        Point2D p1 = projection.project(annotation.getGeoPoints().get(0));
        Point2D p2 = projection.project(annotation.getGeoPoints().get(1));

        Line line = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        line.setStroke(color);
        line.setStrokeWidth(annotation.getStrokeWidth());
        mapPane.getChildren().add(line);

        line.setUserData("annotation");
    }

    private void drawTextAnnotation(Annotation annotation, Color color) {
        Point2D p = projection.project(annotation.getGeoPoints().get(0));

        Circle anchor = new Circle(p.getX(), p.getY(), 4);
        anchor.setFill(color);
        anchor.setStroke(Color.WHITE);

        Label label = new Label(annotation.getText());
        label.setLayoutX(p.getX() + 8);
        label.setLayoutY(p.getY() - 18);
        label.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);"
                + "-fx-text-fill: white;"
                + "-fx-padding: 2 6;"
                + "-fx-background-radius: 4;"
                + "-fx-font-size: 11;"
        );

        mapPane.getChildren().addAll(anchor, label);

        anchor.setUserData("annotation");
        label.setUserData("annotation");
    }

    private void drawCircleAnnotation(Annotation annotation, Color color) {
        if (annotation.getGeoPoints().size() < 2) {
            return;
        }

        Point2D center = projection.project(annotation.getGeoPoints().get(0));
        Point2D edge = projection.project(annotation.getGeoPoints().get(1));

        double radius = Math.sqrt(
                Math.pow(edge.getX() - center.getX(), 2)
                + Math.pow(edge.getY() - center.getY(), 2)
        );

        Circle circle = new Circle(center.getX(), center.getY(), radius);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(color);
        circle.setStrokeWidth(annotation.getStrokeWidth());

        mapPane.getChildren().add(circle);

        circle.setUserData("annotation");
    }

    private void addMarker(Point2D p, Color color, String text) {
        Circle circle = new Circle(p.getX(), p.getY(), 7);
        circle.setFill(color);
        circle.setStroke(Color.WHITE);

        Text label = new Text(p.getX() + 10, p.getY() - 8, text);
        label.setFill(color);

        mapPane.getChildren().addAll(circle, label);
    }

    @FXML
    private void handleZoomIn(ActionEvent event) {
        zoomSlider.setValue(Math.min(zoomSlider.getValue() + 0.1, zoomSlider.getMax()));
    }

    @FXML
    private void handleZoomOut(ActionEvent event) {
        zoomSlider.setValue(Math.max(zoomSlider.getValue() - 0.1, zoomSlider.getMin()));
    }

    @FXML
    private void handleAddAnnotation(ActionEvent event) {
        if (activity == null) {
            AlertUtils.showError("Error", "No hay actividad cargada.");
            return;
        }
        if (selectedGeoPoints.isEmpty()) {
            AlertUtils.showWarning("Sin puntos", "Haz clic derecho en el mapa para marcar puntos antes de añadir una anotación.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/AddAnnotation.fxml")
            );
            Parent root = loader.load();

            AddAnnotationController controller = loader.getController();
            controller.setData(activity, selectedGeoPoints);

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

            stage.setMinWidth(520);
            stage.setMinHeight(400);

            if (!wasMaximized) {
                stage.centerOnScreen();
            }
            stage.show();

        } catch (IOException e) {
            AlertUtils.showError("Error", "No se pudo abrir la pantalla de anotación.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationUtils.navigateTo(event, NavigationTarget.to("/view/Home.fxml")
                .minSize(900, 600)
                .onError("No se pudo volver.")
                .build());
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        NavigationUtils.logoutAndNavigateToLogin(event);
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

    private void startMapDrag(MouseEvent event) {
        if (!event.isPrimaryButtonDown()) {
            return;
        }

        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        dragStartH = mapScrollPane.getHvalue();
        dragStartV = mapScrollPane.getVvalue();
        event.consume();
    }

    private void dragMap(MouseEvent event) {
        if (!event.isPrimaryButtonDown() || mapPane == null) {
            return;
        }

        Bounds viewport = mapScrollPane.getViewportBounds();
        Bounds content = mapPane.getBoundsInParent();

        double contentW = content.getWidth();
        double contentH = content.getHeight();
        double viewportW = viewport.getWidth();
        double viewportH = viewport.getHeight();

        if (contentW <= viewportW && contentH <= viewportH) {
            event.consume();
            return;
        }

        double deltaX = event.getSceneX() - dragStartX;
        double deltaY = event.getSceneY() - dragStartY;

        double hRange = Math.max(1.0, contentW - viewportW);
        double vRange = Math.max(1.0, contentH - viewportH);

        mapScrollPane.setHvalue(clamp(dragStartH - deltaX / hRange, 0.0, 1.0));
        mapScrollPane.setVvalue(clamp(dragStartV - deltaY / vRange, 0.0, 1.0));

        event.consume();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void showMapMessage(String message) {
        mapStatusLabel.setText(message);
    }

    private String formatDistance(double meters) {
        return String.format(Locale.ROOT, "%.2f km", meters / 1000.0);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "-";
        }
        long seconds = duration.getSeconds();
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Color safeColor(String css, Color fallback) {
        try {
            return Color.web(css);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void showError(String text) {
        AlertUtils.showError("Error", text);
    }

    private Callback<TableColumn<Annotation, String>, TableCell<Annotation, String>> createWrappingCellFactory() {
        return column -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.maxWidthProperty().bind(column.widthProperty().subtract(10));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        };
    }

    @FXML
    private void handleDeleteAnnotation(ActionEvent event) {
        Annotation selected = (Annotation) annotationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtils.showWarning("Sin selección", "Selecciona una anotación para eliminarla.");
            return;
        }

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        confirm.setTitle("Eliminar anotación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Quieres eliminar esta anotación?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                app.removeAnnotation(activity, selected);

                annotationTable.getItems().remove(selected);
                redrawAnnotations();
            }
        });
    }

    private void redrawAnnotations() {
        if (mapPane == null) {
            return;
        }

        mapPane.getChildren().removeIf(node
                -> node.getUserData() != null && node.getUserData().equals("annotation")
        );

        drawAnnotations();
    }
}

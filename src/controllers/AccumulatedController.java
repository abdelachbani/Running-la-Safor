package controllers;

import java.net.URL;
import java.time.Duration;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import utils.ui_navigation.NavigationTarget;
import utils.ui_navigation.NavigationUtils;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;
import utils.AvatarUtils;

public class AccumulatedController implements Initializable {

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
    private Button periodButton;

    @FXML
    private Label activitiesCountLabel;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Label totalDistanceLabel;
    @FXML
    private Label totalGainLabel;
    @FXML
    private Label totalLossLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private TableView<Activity> monthlyActivitiesTable;
    @FXML
    private TableColumn<Activity, String> nameColumn;
    @FXML
    private TableColumn<Activity, String> dateColumn;
    @FXML
    private TableColumn<Activity, String> distanceColumn;
    @FXML
    private TableColumn<Activity, String> durationColumn;
    @FXML
    private TableColumn<Activity, String> gainColumn;
    @FXML
    private TableColumn<Activity, String> lossColumn;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Locale spanishLocale = new Locale("es", "ES");

    private YearMonth selectedPeriod = YearMonth.now();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        titleLabel.getStyleClass().add("screen-title");
        usernameLabel.getStyleClass().add("user-name");
        logoutButton.getStyleClass().add("top-action-button");
        backButton.getStyleClass().add("nav-button");
        periodButton.getStyleClass().add("import-button");

        activitiesCountLabel.getStyleClass().add("summary-label");
        totalTimeLabel.getStyleClass().add("summary-label");
        totalDistanceLabel.getStyleClass().add("summary-label");
        totalGainLabel.getStyleClass().add("summary-label");
        totalLossLabel.getStyleClass().add("summary-label");
        statusLabel.getStyleClass().add("summary-label");

        configureTable();
        loadUserData();
        updatePeriodButtonText();
        loadAccumulatedData();
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(safeText(cell.getValue().getName())));

        dateColumn.setCellValueFactory(cell -> {
            Activity activity = cell.getValue();
            String value = activity.getStartTime() != null
                    ? activity.getStartTime().toLocalDate().format(dateFormatter)
                    : "-";
            return new SimpleStringProperty(value);
        });

        distanceColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(formatDistance(cell.getValue().getTotalDistance())));

        durationColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(formatDuration(cell.getValue().getDuration())));

        gainColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(String.format(Locale.ROOT, "%.0f m", cell.getValue().getElevationGain())));

        lossColumn.setCellValueFactory(cell
                -> new SimpleStringProperty(String.format(Locale.ROOT, "%.0f m", cell.getValue().getElevationLoss())));

        monthlyActivitiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        monthlyActivitiesTable.setPlaceholder(new Label("No hay actividades en este periodo."));
    }

    private void updatePeriodButtonText() {
        periodButton.setText("Elegir fecha");
    }

    private void loadAccumulatedData() {
        List<Activity> filtered = filterActivitiesByPeriod();
        monthlyActivitiesTable.getItems().setAll(filtered);
        updateSummaryLabels(filtered);
        updateStatusLabel(filtered);
    }

    private List<Activity> filterActivitiesByPeriod() {
        List<Activity> result = new ArrayList<>();
        for (Activity activity : app.getUserActivities()) {
            if (belongsToPeriod(activity)) {
                result.add(activity);
            }
        }
        return result;
    }

    private boolean belongsToPeriod(Activity activity) {
        return activity != null
                && activity.getStartTime() != null
                && YearMonth.from(activity.getStartTime()).equals(selectedPeriod);
    }

    private void updateSummaryLabels(List<Activity> filtered) {
        double totalDistance = 0.0;
        double totalGain = 0.0;
        double totalLoss = 0.0;

        for (Activity activity : filtered) {
            totalDistance += activity.getTotalDistance();
            totalGain += activity.getElevationGain();
            totalLoss += activity.getElevationLoss();
        }

        activitiesCountLabel.setText("Actividades: " + filtered.size());
        totalTimeLabel.setText("Tiempo total: " + formatDuration(sumDurations(filtered)));
        totalDistanceLabel.setText("Distancia total: " + formatDistance(totalDistance));
        totalGainLabel.setText(String.format(Locale.ROOT, "Ascenso total: %.0f m", totalGain));
        totalLossLabel.setText(String.format(Locale.ROOT, "Descenso total: %.0f m", totalLoss));
    }

    private Duration sumDurations(List<Activity> activities) {
        Duration total = Duration.ZERO;
        for (Activity activity : activities) {
            Duration d = activity.getDuration();
            if (d != null) {
                total = total.plus(d);
            }
        }
        return total;
    }

    private void updateStatusLabel(List<Activity> filtered) {
        String prefix = filtered.isEmpty()
                ? "No hay actividades en "
                : "Mostrando actividades de ";
        statusLabel.setText(prefix + periodDescription());
    }

    @FXML
    private void handlePeriodSelection(ActionEvent event) {
        ComboBox<Month> monthCombo = createMonthCombo();
        ComboBox<Integer> yearCombo = createYearCombo();

        Dialog<YearMonth> dialog = buildPeriodDialog(monthCombo, yearCombo);

        dialog.showAndWait().ifPresent(this::applySelectedPeriod);
    }

    private ComboBox<Month> createMonthCombo() {
        ComboBox<Month> combo = new ComboBox<>(FXCollections.observableArrayList(Month.values()));
        combo.setValue(selectedPeriod.getMonth());
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setCellFactory(cb -> createMonthCell());
        combo.setButtonCell(createMonthCell());
        return combo;
    }

    private ListCell<Month> createMonthCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : capitalize(item.getDisplayName(TextStyle.FULL, spanishLocale)));
            }
        };
    }

    private ComboBox<Integer> createYearCombo() {
        int currentYear = 2026;
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            years.add(y);
        }

        ComboBox<Integer> combo = new ComboBox<>(FXCollections.observableArrayList(years));
        combo.setValue(selectedPeriod.getYear());
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private Dialog<YearMonth> buildPeriodDialog(ComboBox<Month> monthCombo, ComboBox<Integer> yearCombo) {
        Dialog<YearMonth> dialog = new Dialog<>();
        dialog.setTitle("Seleccionar periodo");
        dialog.setHeaderText("Elige mes y año");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Mes:"), 0, 0);
        grid.add(monthCombo, 1, 0);
        grid.add(new Label("Año:"), 0, 1);
        grid.add(yearCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Integer year = yearCombo.getValue();
                Month month = monthCombo.getValue();
                if (year != null && month != null) {
                    return YearMonth.of(year, month);
                }
            }
            return null;
        });

        return dialog;
    }

    private void applySelectedPeriod(YearMonth period) {
        selectedPeriod = period;
        updatePeriodButtonText();
        loadAccumulatedData();
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

    private String periodDescription() {
        String monthName = selectedPeriod.getMonth().getDisplayName(TextStyle.FULL, spanishLocale);
        return capitalize(monthName) + " " + selectedPeriod.getYear();
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String formatDistance(double meters) {
        return String.format(Locale.ROOT, "%.2f km", meters / 1000.0);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "-";
        }
        long seconds = duration.getSeconds();
        long abs = Math.abs(seconds);
        return String.format(Locale.ROOT, "%d:%02d:%02d", abs / 3600, (abs % 3600) / 60, abs % 60);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}

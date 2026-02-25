package com.tgrah;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tgrah.model.ListEntry;
import com.tgrah.util.TotpGenerator;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.input.ClipboardContent;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MainController {

    @FXML
    private TextField secretField;

    @FXML
    private TextField nameField;

    @FXML
    private Label selectedTotp;

    @FXML
    private VBox newEntryView;

    @FXML
    private VBox codeView;

    @FXML
    private ListView<ListEntry> totpListView;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Label banner;

    private Timeline totpTimeline;

    @FXML
    private StackPane contentArea;

    private ObservableList<ListEntry> observableList = FXCollections.observableArrayList();
    private final Gson gson = new Gson();
    private final String JSON_FILE = "totp.json";

    @FXML
    public void initialize() {

        loadTotpList();
        if (splitPane != null) {
            splitPane.setDividerPositions(0.2);
            splitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
                splitPane.getDividers().get(0).setPosition(0.2);
            });
        }
        if (codeView != null) {
            codeView.setVisible(false);
            codeView.setManaged(false);
        }
        if (banner != null) {
            banner.setOpacity(0);
        }
        if (totpListView != null) {

            totpListView.setItems(observableList);
            totpListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(ListEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            totpListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedTotp.setText("Loading");
                    newEntryView.setVisible(false);
                    newEntryView.setManaged(false);
                    codeView.setVisible(true);
                    codeView.setManaged(true);
                }
            });

        }
        startTotpUpdater();
    }

    private void loadTotpList() {
        try (FileReader reader = new FileReader(JSON_FILE)) {
            List<ListEntry> entries = gson.fromJson(reader, new TypeToken<List<ListEntry>>() {
            }.getType());
            observableList.clear();
            if (entries != null) {
                for (ListEntry e : entries) {
                    observableList.add(e);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void saveNewSecret() {
        if (secretField.getText() != null && !secretField.getText().isEmpty()) {
            String secret = secretField.getText();
            String name = nameField.getText();

            try {
                long totp = Long.parseLong(TotpGenerator.generateTotp(secret));

                List<ListEntry> entries = new ArrayList<>();
                try (FileReader reader = new FileReader(JSON_FILE)) {
                    entries = gson.fromJson(reader, new TypeToken<List<ListEntry>>() {
                    }.getType());
                    if (entries == null) {
                        entries = new ArrayList<>();
                    }
                } catch (Exception ignored) {
                }

                if (entries.stream().anyMatch(e -> e.getName().equals(name))) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Ein Eintrag mit dem Namen existiert bereits");
                    alert.setTitle("Fehler");
                    alert.setHeaderText(null);
                    alert.setGraphic(null);
                    alert.show();
                    return;
                }
                ListEntry newEntry = new ListEntry(name, secret, totp);
                entries.add(newEntry);

                try (FileWriter writer = new FileWriter(JSON_FILE)) {
                    gson.toJson(entries, writer);
                }

                observableList.add(newEntry);

                nameField.clear();
                secretField.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deleteSecret() {
        if (selectedTotp.getText() != null && !selectedTotp.getText().isEmpty()) {

            try {

                List<ListEntry> entries = new ArrayList<>();
                try (FileReader reader = new FileReader(JSON_FILE)) {
                    entries = gson.fromJson(reader, new TypeToken<List<ListEntry>>() {
                    }.getType());
                    if (entries == null) {
                        entries = new ArrayList<>();
                    }
                } catch (Exception ignored) {
                }

                entries.removeIf((e) -> e.getTotp().toString().equals(selectedTotp.getText()));
                try (FileWriter writer = new FileWriter(JSON_FILE)) {
                    gson.toJson(entries, writer);
                }

                String selectedName = totpListView.getSelectionModel().getSelectedItem().getName();

                entries.removeIf(e -> e.getName().equals(selectedName));
                observableList.removeIf(e -> e.getName().equals(selectedName));
                // Switch view
                if (newEntryView != null && codeView != null) {
                    newEntryView.setVisible(true);
                    codeView.setVisible(false);
                    newEntryView.setVisible(true);
                    newEntryView.setManaged(true);
                    codeView.setVisible(false);
                    codeView.setManaged(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startTotpUpdater() {
        totpTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateSelectedTotp()));
        totpTimeline.setCycleCount(Timeline.INDEFINITE);
        totpTimeline.play();
    }

    private void updateSelectedTotp() {
        ListEntry selected = totpListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                String newTotp = TotpGenerator.generateTotp(selected.getSecret());
                selectedTotp.setText(newTotp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void showNewEntryView() {
        if (newEntryView != null && codeView != null) {
            newEntryView.setVisible(true);
            newEntryView.setManaged(true);
            codeView.setVisible(false);
            codeView.setManaged(false);

            totpListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    public void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(selectedTotp.getText());
        clipboard.setContent(content);

        showCopyBanner();
    }

    private void showCopyBanner() {

        banner.setText("Code erfolgreich kopiert");

        banner.setOpacity(0);

        StackPane.setAlignment(banner, Pos.TOP_CENTER);
        StackPane.setMargin(banner, new Insets(20));

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), banner);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(1.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), banner);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> contentArea.getChildren().remove(banner));

        fadeIn.setOnFinished(e -> stay.play());
        stay.setOnFinished(e -> fadeOut.play());

        fadeIn.play();
    }
}

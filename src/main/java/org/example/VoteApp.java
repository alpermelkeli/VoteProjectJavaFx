package org.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.BackgroundFill;
import javafx.util.Duration;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import java.io.File;
import javafx.geometry.Insets;

public class VoteApp extends Application {

    private VoteLogic voteLogic;
    private TextField filePathField;
    private ComboBox<String> voteTypeComboBox;
    private TextField minuteField;
    private Label countdownLabel;
    private Timeline countdownTimeline;
    private int totalSeconds;

    @Override
    public void start(Stage primaryStage) {

        voteLogic = new VoteLogic("", 10, "ittifak");

        Label label = new Label("Oylama Süreci");

        Button chooseFileButton = new Button("Dosya Seç");

        chooseFileButton.setOnAction(e -> chooseFile(primaryStage));

        filePathField = new TextField();
        filePathField.setPromptText("Dosya yolu burada gösterilecek");
        filePathField.setEditable(false);

        voteTypeComboBox = new ComboBox<>();
        voteTypeComboBox.getItems().addAll("ittifak", "tur1", "tur2", "yerel");
        voteTypeComboBox.setPromptText("Bir seçim tipi seçin");

        minuteField = new TextField();
        minuteField.setPromptText("Dakika girin");

        countdownLabel = new Label("Kalan süre: -");

        Button startButton = new Button("Başlat");
        startButton.setOnAction(e -> {
            if (!filePathField.getText().isEmpty() && voteTypeComboBox.getValue() != null && !minuteField.getText().isEmpty()) {
                String voteType = voteTypeComboBox.getValue();
                int minutes;
                try {
                    minutes = Integer.parseInt(minuteField.getText());
                } catch (NumberFormatException ex) {
                    System.out.println("Lütfen geçerli bir dakika girin.");
                    return;
                }

                voteLogic = new VoteLogic(filePathField.getText(), minutes, voteType);
                voteLogic.run();

                startCountdown(minutes);
            } else {
                System.out.println("Lütfen bir dosya seçin, bir seçim türü ve dakika girin.");
            }
        });
        Button stopButton = new Button("Durdur");
        stopButton.setOnAction(e -> {
            voteLogic.stop();
            countdownTimeline.stop();
            countdownLabel.setText("Kalan süre: Durduruldu");

        });

        VBox root = new VBox(10, label, chooseFileButton, filePathField, voteTypeComboBox, minuteField, startButton,stopButton, countdownLabel);
        root.setAlignment(Pos.CENTER);
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        root.setBackground(new Background(backgroundFill));
        Scene scene = new Scene(root, 400, 400);

        primaryStage.setTitle("Seçim Oy Yüklenme Programı");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void startCountdown(int minutes) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        totalSeconds = minutes * 60;

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            totalSeconds--;
            int minutesLeft = totalSeconds / 60;
            int secondsLeft = totalSeconds % 60;
            countdownLabel.setText(String.format("Kalan süre: %02d:%02d", minutesLeft, secondsLeft));

            if (totalSeconds <= 0) {
                countdownTimeline.stop();
                countdownLabel.setText("Kalan süre: Tamamlandı");
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

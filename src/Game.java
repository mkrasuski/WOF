import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.function.Function;

public class Game extends WheelOfFortune {

    private final int SEGMENTS = 10;
    private final int scoreOfSegment[] = {
            0, 100, 1000, 500, 300, 0, 1000, 300, 5000, 800
    };

    Game(String secret) {
        super(secret);
    }


    Node createWheelArc(double angle, int index, Color c) {

        Arc arc = new Arc();
        arc.setType(ArcType.ROUND);
        arc.setLength(angle);
        arc.setRadiusX(150);
        arc.setRadiusY(150);
        arc.setFill(c);
        arc.getTransforms().add(new Rotate(index*angle,0,0));
        return arc;
    }

    Node createWheelLabel(double angle, int index, String text) {

        Label label = new Label(text);
        label.setText(text);
        label.setFont(new Font(15));
        label.setTranslateX(90);
        label.getTransforms().add(new Rotate(angle*(index + 0.35), -90, 0));
        label.toFront();
        return label;
    }

    Node createIndicator() {

        Rectangle indicator = new Rectangle(10, 10);
        indicator.setFill(Color.BLACK);
        indicator.setY(-5);
        indicator.setX(150);
        indicator.setRotate(45);
        return indicator;
    }

    Node createWheel() {

        double angle = 360.0 / SEGMENTS;

        Group wheel = new Group();

        for (int index = 0; index < SEGMENTS; index++) {

            Node wheelArc = createWheelArc(angle, index, (index % 2) == 0 ? Color.CORAL : Color.LIGHTBLUE);
            Node label = createWheelLabel(angle, index, String.valueOf(scoreOfSegment[index]));
            wheel.getChildren().addAll(
                    wheelArc,
                    label);
            label.toFront();
            wheelArc.toBack();
        }

        return wheel;
    }

    void rollWheel(Node wheel, final Consumer<Integer> onEnd) {

        SequentialTransition animation = new SequentialTransition();

        double angle = wheel.getRotate();
        double step = 15 + 5 * Math.random();
        double speed = 20*step;

        for (int k = 1; k <= 20; k++) {

            RotateTransition rotation = new RotateTransition(Duration.millis(50 + 10*k),  wheel);
            rotation.setInterpolator(k < 20 ? Interpolator.LINEAR : Interpolator.EASE_OUT);
            rotation.setFromAngle(angle);
            angle += speed;
            rotation.setToAngle(angle);
            animation.getChildren().add(rotation);
            speed -= step;
        }

        animation.setOnFinished(ev -> {
            // handler is run in main loop, outside of animation to overcome limitations of IllegalState
            Platform.runLater(() -> {
                double a = wheel.getRotate() % 360.0;
                int index = SEGMENTS - 1 - (int)Math.floor(a / (360.0 / SEGMENTS));
                onEnd.accept(scoreOfSegment[index]);
            });
        });

        animation.play();
    }

    Stage createGameScreen() {

        final BorderPane root = new BorderPane();

        root.setTop(centered(
                withMargin(createLetterPane(), 20)));

        final Node wheel = createWheel();
        root.setCenter(new Group(wheel, createIndicator()));

        final Button rollButton = new Button("Roll!");
        rollButton.setPrefSize(200, 50);
        rollButton.setFont(new Font(30));
        rollButton.setOnAction(event -> {

            rollButton.setDisable(true);
            rollWheel(wheel, score -> {

                switch (nextTurn(askForLetter(), score)) {
                    case WIN:
                        new Alert(Alert.AlertType.NONE,
                                "You have Won! Your score is " + scoreProperty().get(),
                                ButtonType.OK).showAndWait();
                        break;
                    case LOOSE:
                        new Alert(Alert.AlertType.WARNING,
                                "You have lost! Your score is " + scoreProperty().get(),
                                ButtonType.OK).showAndWait();
                }

                rollButton.setDisable(false);
            });
        });

        VBox box = new VBox(createScoreLabel(), rollButton);
        box.setSpacing(20);
        box.setAlignment(Pos.CENTER);
        root.setBottom(withMargin(new BorderPane(box), 20));

        Stage stage = new Stage();
        Scene scene = new Scene(root, 480, 640);
        stage.setTitle("Have fun!");
        stage.setScene(scene);
        stage.setMinHeight(500);
        stage.setMinWidth(500);
        return stage;
    }

    Node createScoreLabel() {

        Font font = new Font(20);

        Label scoreLabel = new Label();
        scoreLabel.setFont(font);
        scoreLabel.textProperty().bind(
                Bindings.format("Score: %d, Faults %d/3",
                        scoreProperty(),
                        faultsProperty()));

        return scoreLabel;
    }

    private Node createLetterPane() {

        FlowPane flowPane = new FlowPane();
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setHgap(10);
        flowPane.setVgap(10);

        String secretValue = secretProperty().get();

        for (int n = 0; n < secretValue.length(); n++)  {

            final char ch = secretValue.charAt(n);

            final Label label = new Label();
            label.setMinSize(50, 50);
            label.setFont(Font.font(30));

            if (ch != ' ') {
                label.setStyle("-fx-background-color:silver");
            }

            // each label is bound to exactly one letter of guessed string
            final int i = n;
            label.textProperty().bind(
                    Bindings.createStringBinding(
                            () -> guessProperty().get().substring(i,i+1),
                            guessProperty()));

            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            flowPane.getChildren().add(label);
        }
        return flowPane;
    }

    String askForInput(String promptText, String buttonText, Function<String,String> validate) {

        final Stage stage = new Stage();
        final BorderPane root = new BorderPane();

        final TextField field = new TextField();
        final Label label = new Label(promptText);
        final Button button = new Button(buttonText);

        button.setDefaultButton(true);
        button.setOnAction(ev -> {
            String error = validate.apply(field.getText().trim());
            if (error != null) {
                label.setText(error);
                label.setTextFill(Color.RED);
            }
            else {
                stage.close();
            }
        });

        root.setTop(withMargin(label));
        root.setCenter(withMargin(field));
        root.setBottom(withMargin(button));

        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return field.getText().trim();
    }

    char askForLetter() {

        String letter = askForInput("Guess a letter and press ENTER...", "guess", str -> {

            if (str.length() != 1) {
                return "Should enter EXACTLY ONE letter...";
            }

            final char ch = str.toUpperCase().charAt(0);

            if (alreadyGuessed(ch)) {
                return "Already guessed. Retry...";
            }

            if (!validLetter(ch)) {
                return "Should enter LETTER...";
            }

            return null;
        });

        return letter.toUpperCase().charAt(0);
    }

    private Node withMargin(Node node, double width) {
        BorderPane.setMargin(node, new Insets(width));
        return node;
    }

    private Node withMargin(Node node) {
        return withMargin(node, 8);
    }

    private Node centered(Node node) {
        BorderPane.setAlignment(node, Pos.CENTER);
        return node;
    }

}

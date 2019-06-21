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

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

class GameUI implements UITools {

    private final int SEGMENTS = 10;
    private final int[] scoreOfSegment = {
            0, 100, 1000, 500, 300, 0, 1000, 300, 5000, 800
    };
    private final WheelOfFortune game;

    GameUI(final WheelOfFortune game) {
        this.game = game;
    }

    Stage createGameScreen() {

        final BorderPane root = new BorderPane();

        root.setTop(centered(
                withMargin(20, createLettersPane())));

        final Node wheel = createWheel();
        root.setCenter(new Group(wheel, createIndicator()));

        final Button rollButton = new Button("Roll!");
        rollButton.setPrefSize(200, 50);
        rollButton.setFont(Font.font(30));
        rollButton.setOnAction(event -> {

            rollButton.setDisable(true);
            rollWheel(wheel, score -> {

                switch (game.nextTurn(askForLetter(), score)) {
                    case WIN:
                        new Alert(Alert.AlertType.NONE,
                                "You have Won! Your score is " + game.scoreProperty().get(),
                                ButtonType.OK).showAndWait();
                        break;
                    case LOOSE:
                        new Alert(Alert.AlertType.WARNING,
                                "You have lost! Your score is " + game.scoreProperty().get(),
                                ButtonType.OK).showAndWait();
                }

                rollButton.setDisable(false);
            });
        });

        final VBox box = new VBox(createScoreLabel(), rollButton);
        box.setSpacing(10);
        box.setAlignment(Pos.CENTER);
        root.setBottom(withMargin(20, new BorderPane(box)));

        final Stage stage = new Stage();
        final Scene scene = new Scene(root, 480, 640);
        stage.setTitle("Have fun!");
        stage.setScene(scene);
        stage.setMinHeight(500);
        stage.setMinWidth(500);

        return stage;
    }

    private void rollWheel(final Node wheel, final Consumer<Integer> onEnd) {

        final SequentialTransition animation = new SequentialTransition();

        double angle = wheel.getRotate();
        double step = 15 + 5* (new Random().nextDouble());
        double speed = 20 * step;

        for (int k = 1; k <= 20; k++) {

            final RotateTransition rotation = new RotateTransition(Duration.millis(50 + 10 * k), wheel);
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
                int index = (SEGMENTS - 1) - (int) Math.floor(a / (360.0 / SEGMENTS));
                onEnd.accept(scoreOfSegment[index]);
            });
        });

        animation.play();
    }

    private Node createWheelArc(double angle, int index, Color c) {

        final Arc arc = new Arc();
        arc.setType(ArcType.ROUND);
        arc.setLength(angle);
        arc.setRadiusX(150);
        arc.setRadiusY(150);
        arc.setFill(c);
        arc.getTransforms().add(new Rotate(index * angle, 0, 0));

        return arc;
    }

    private Node createWheelLabel(double angle, int index, String text) {

        final Label label = new Label(text);
        label.setText(text);
        label.setFont(new Font(15));
        label.setTranslateX(90);
        label.getTransforms().add(new Rotate(angle * (index + 0.35), -90, 0));
        label.toFront();

        return label;
    }

    private Node createIndicator() {

        final Rectangle indicator = new Rectangle(10, 10);
        indicator.setFill(Color.BLACK);
        indicator.setY(-5);
        indicator.setX(150);
        indicator.setRotate(45);

        return indicator;
    }

    private Node createWheel() {

        double angle = 360.0 / SEGMENTS;

        final Group wheel = new Group();

        for (int index = 0; index < SEGMENTS; index++) {

            final Node wheelArc = createWheelArc(angle, index, (index % 2) == 0 ? Color.CORAL : Color.LIGHTBLUE);
            final Node label = createWheelLabel(angle, index, String.valueOf(scoreOfSegment[index]));
            wheel.getChildren().addAll(
                    wheelArc,
                    label);
            label.toFront();
            wheelArc.toBack();
        }

        return wheel;
    }

    private Node createScoreLabel() {

        final Label scoreLabel = new Label();
        scoreLabel.setFont(Font.font(20));
        scoreLabel.textProperty().bind(
                Bindings.format("Score: %d, Faults %d/3",
                        game.scoreProperty(),
                        game.faultsProperty()));

        return scoreLabel;
    }

    private Node createLettersPane() {

        final FlowPane flowPane = new FlowPane();
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setHgap(10);
        flowPane.setVgap(10);

        String secretValue = game.secretProperty().get();

        for (int n = 0; n < secretValue.length(); n++) {

            final char ch = secretValue.charAt(n);

            final Label label = new Label();
            label.setMinSize(35, 35);
            label.setFont(Font.font(30));

            if (ch != ' ') {
                label.setStyle("-fx-background-color:silver");
            }

            // each label is bound to exactly one letter of guessed string
            final int i = n;
            label.textProperty().bind(
                    Bindings.createStringBinding(
                            () -> String.valueOf(game.guessProperty().get().charAt(i)),
                            game.guessProperty()));

            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            flowPane.getChildren().add(label);
        }
        return flowPane;
    }

    private char askForLetter() {

        String letter = askForInput("Guess a letter and press ENTER...", "guess", str -> {

            if (str.length() != 1) {
                return "Should enter EXACTLY ONE letter...";
            }

            final char ch = str.toUpperCase().charAt(0);

            if (game.alreadyGuessed(ch)) {
                return "Already guessed. Retry...";
            }

            if (!game.validLetter(ch)) {
                return "Should enter valid LETTER...";
            }

            return null;
        });

        return letter.toUpperCase().charAt(0);
    }



}
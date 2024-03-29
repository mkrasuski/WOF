import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.Callable;
import java.util.function.Function;

public interface UITools {

    default Node withMargin(double width, Node node) {

        BorderPane.setMargin(node, new Insets(width));
        return node;
    }

    default Node withMargin(Node node) {

        return withMargin(8, node);
    }

    default Node centered(Node node) {

        BorderPane.setAlignment(node, Pos.CENTER);
        return node;
    }

    default String askForInput(String promptText, String buttonText, Function<String, String> validate) {

        final var stage = new Stage();
        final var root = new BorderPane();

        final var field = new TextField();
        final var label = new Label(promptText);
        final var button = new Button(buttonText);

        button.setDefaultButton(true);
        button.setOnAction(ev -> {
            String error = validate.apply(field.getText().trim());
            if (error != null) {
                label.setText(error);
                label.setTextFill(Color.RED);
            } else {
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

    default Node hbox(Pos pos, Node ...nodes) {
        final var box = new HBox(nodes);
        box.setSpacing(8);
        box.setAlignment(pos);
        return box;
    }

    default Pane vbox(Pos pos, Node ...nodes) {
        final var box = new VBox(nodes);
        box.setSpacing(8);
        box.setAlignment(pos);
        return box;
    }

    default BooleanBinding checkIf(Callable<Boolean> test, Observable ...props) {
        return Bindings.createBooleanBinding(test, props);
    }

    default Node wideChildren(Pane node) {
        for (var ch : node.getChildren()) {
            if (ch instanceof Control) {
                ((Control) ch).setMaxWidth(Double.MAX_VALUE);
            }
        }
        return node;
    }
}

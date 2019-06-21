import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Random;

class SetupUI implements UITools {

    SetupUI() {
    }

    Stage createSetupScreen(ObservableList<String> library) {
        final var root = new BorderPane();

        final var leftList = new ListView<>(library);
        final var rightList = new ListView<String>();

        final var ltrButton = new Button(">");
        final var rtlButton = new Button("<");
        final var addButton = new Button("Add to library");
        final var playButton = new Button("Play!");

        root.setCenter(withMargin(
                hbox(Pos.CENTER,
                    vbox(Pos.TOP_LEFT,
                            new Label("Library secrets"),
                            leftList),

                    wideChildren(vbox(Pos.CENTER,
                            ltrButton,
                            rtlButton,
                            addButton,
                            playButton)),

                    vbox(Pos.TOP_LEFT,
                            new Label("Secrets for game"),
                            rightList))));

        ltrButton.setOnAction(
                ev -> moveSelectedItems(leftList, rightList));
        ltrButton.disableProperty().bind(
                checkIf(() -> leftList.getSelectionModel().getSelectedItems().size() == 0,
                        leftList.getSelectionModel().getSelectedItems()));

        rtlButton.setOnAction(
                ev -> moveSelectedItems(rightList, leftList));
        rtlButton.disableProperty().bind(
                checkIf(() -> rightList.getSelectionModel().getSelectedItems().size() == 0,
                        rightList.getSelectionModel().getSelectedItems()));

        addButton.setOnAction(
                ev -> library.add(askForNewSecret()));

        playButton.setOnAction(
                ev -> play(rightList.getItems()));
        playButton.disableProperty().bind(
                checkIf(() -> rightList.getItems().size() == 0,
                        rightList.getItems()));

        final var stage = new Stage();
        stage.setScene(new Scene(root));

        return stage;
    }

    private void play(ObservableList<String> items) {

        int randomIndex = new Random().nextInt(items.size());
        WheelOfFortune game = new WheelOfFortune(items.get(randomIndex));
        new GameUI(game).createGameScreen().show();
    }

    private String askForNewSecret() {

        return askForInput("Insert new secret", "Add to library",
                str -> {
                    if (str.length() < 3) return "Secret has to have at least 3 characters...";
                    return null;
                });
    }

    private void moveSelectedItems(ListView<String> fromList, ListView<String> toList) {

        var selectedItems = fromList.getSelectionModel().getSelectedItems();
        if (selectedItems.size() > 0) {
            toList.getItems().addAll(selectedItems);
            fromList.getItems().removeAll(selectedItems);
        }
    }
}

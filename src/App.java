import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        new Game("guess me now").createGameScreen().show();
    }

    public static void main(String[] args) {

        Locale.setDefault(Locale.forLanguageTag("pl_PL"));
        launch(args);
    }
}

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        new Game("guess me now").createGameScreen().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

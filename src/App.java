import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        new SetupUI().createSetupScreen(loadLibrary("data.txt")).show();
    }

    private ObservableList<String> loadLibrary(String fileName) {
        ObservableList<String> library = FXCollections.observableArrayList();
        try {
            var in = new BufferedReader(new FileReader(fileName, Charset.forName("UTF-8")));

            while (true) {
                var str = in.readLine();
                if (str != null)
                    library.add(str.trim());
                else
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return library;
    }

    public static void main(String[] args) {

        Locale.setDefault(Locale.forLanguageTag("pl_PL"));
        launch(args);
    }
}

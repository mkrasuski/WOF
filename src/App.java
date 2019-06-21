import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        new SetupUI().createSetupScreen(loadLibrary("data.txt")).show();
    }

    private ObservableList<String> loadLibrary(String fileName) {
        ObservableList<String> lib = FXCollections.observableArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName, Charset.forName("UTF-8")));
            String str;
            while (true) {
                str = in.readLine();
                if (str != null)
                    lib.add(str.trim());
                else
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lib;
    }

    public static void main(String[] args) {

        Locale.setDefault(Locale.forLanguageTag("pl_PL"));
        launch(args);
    }
}

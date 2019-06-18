import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
/**
 *  {@link WheelOfFortune} is class with game logic. Has nothing to GUI.
 *
 */
public class WheelOfFortune {

    public enum GameState {
        LOOSE, WIN, NEXT;
    }

    /**
     *  secretProperty stores secret to guess
     */
    private StringProperty secret = new SimpleStringProperty("");
    public StringProperty secretProperty() {
        return secret;
    }

    /**
     *  guessProperty stores current state of guessed letters
     */
    private StringProperty guess = new SimpleStringProperty( "");
    public StringProperty guessProperty() { return guess; }

    /**
     * scoreProperty stores current score
     */
    private IntegerProperty score = new SimpleIntegerProperty(0);
    public IntegerProperty scoreProperty() { return score; }

    /**
     *  faultsProperty stores number of faults. Player looses after 3 faults
     */
    private IntegerProperty faults = new SimpleIntegerProperty( 0);
    public IntegerProperty faultsProperty() { return faults; }

    WheelOfFortune(String aSecret) {

        aSecret = aSecret.toUpperCase();

        secretProperty().set(aSecret);
        guessProperty().set(stringOfSpaces(aSecret.length()));
    }

    /**
     * @param letter - next guessed letter
     * @param scoreForLetter - score for every occurrence of letter
     * @return next game state (win, loose or next turn)
     */
    GameState nextTurn(char letter, int scoreForLetter) {

        int found = 0;
        String guessValue = guess.get();
        String secretValue = secret.get();
        StringBuffer b = new StringBuffer();

        for (int n = 0; n < secretValue.length(); n++) {

            if (letter == secretValue.charAt(n)) {
                found++;
                b.append(letter);
            }
            else {
                b.append(guessValue.charAt(n));
            }
        }
        guess.set(b.toString());

        score.set(score.get() + scoreForLetter * found);

        if (found == 0) {
            faults.set(faults.get() + 1);
        }

        if (faults.get() == 3) {
            return GameState.LOOSE;
        }

        if (guess.get().equals(secret.get())) {
            return GameState.WIN;
        }

        return GameState.NEXT;
    }

    boolean alreadyGuessed(char ch) {
        return guess.get().contains(String.valueOf(ch));
    }

    boolean validLetter(char ch) {
        return "ABCDEFGHIJKLMNOPRSTUVWXYZĄŃŁÓŹŻĆĘ".contains(String.valueOf(ch));
    }

    String stringOfSpaces(int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, ' ');
        return String.valueOf(chars);
    }
}

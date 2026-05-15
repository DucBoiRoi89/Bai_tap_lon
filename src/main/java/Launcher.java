import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Launcher {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {}
        
        MainApp.main(args);
    }
}
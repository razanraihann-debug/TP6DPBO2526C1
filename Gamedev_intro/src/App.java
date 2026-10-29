
import javax.swing.*;
import java.awt.*;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Logic logika = new Logic();

            View tampilan = new View(logika);
            logika.setView(tampilan);

            JFrame frame = new JFrame("FlappyBird");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // CardLayout: berpindah antara Main Menu dan layar permainan
            CardLayout cards = new CardLayout();
            JPanel root = new JPanel(cards);

            Runnable showGame = () -> {
                cards.show(root, "game");
                logika.startGame();
                tampilan.requestFocusInWindow();
            };
            Runnable exitGame = () -> System.exit(0);

            MainMenu menu = new MainMenu(logika.getBackgroundImage(), showGame, exitGame);

            root.add(menu, "menu");
            root.add(tampilan, "game");

            // Tombol M saat game over: kembali ke Main Menu
            logika.setMenuListener(() -> cards.show(root, "menu"));

            frame.add(root);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

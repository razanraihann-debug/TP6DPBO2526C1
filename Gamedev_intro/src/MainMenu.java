import javax.swing.*;
import java.awt.*;

// Tampilan Main Menu: tombol Mulai dan tombol Keluar
public class MainMenu extends JPanel {

    private final Image background;

    public MainMenu(Image background, Runnable onStart, Runnable onExit) {
        this.background = background;

        setPreferredSize(new Dimension(360, 640));
        setLayout(null);

        JLabel title = new JLabel("FLAPPY BIRD", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 140, 360, 60);
        add(title);

        JLabel hint = new JLabel("SPASI = terbang  |  R = restart", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.BOLD, 14));
        hint.setForeground(Color.WHITE);
        hint.setBounds(0, 200, 360, 24);
        add(hint);

        JButton startButton = createButton("MULAI", 280);
        startButton.addActionListener(e -> onStart.run());
        add(startButton);

        JButton exitButton = createButton("KELUAR", 350);
        exitButton.addActionListener(e -> onExit.run());
        add(exitButton);
    }

    private JButton createButton(String text, int y) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setBounds(90, y, 180, 50);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class View extends JPanel {

    int width = 360;
    int height = 640;

    private Logic logic;

    private final JLabel scoreLabel;    // menampilkan skor
    private final JLabel messageLabel;  // "Tekan SPASI" / "GAME OVER"
    private boolean hasMessage = false;

    // Efek Game Over: flash merah lalu fade ke abu-abu (saturasi 0)
    private static final int FLASH_MS = 400;
    private static final int GRAY_DELAY_MS = 120;
    private static final int GRAY_FADE_MS = 1300;
    private BufferedImage frozenScene;
    private BufferedImage grayScene;
    private long gameOverStart = 0;

    public View(Logic logic) {
        this.logic = logic;

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.CYAN);
        setLayout(null);

        setFocusable(true);
        addKeyListener(logic);

        scoreLabel = new JLabel("0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setBounds(0, 20, width, 60);
        add(scoreLabel);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBounds(0, 200, width, 160);
        add(messageLabel);
    }

    public void setScore(int score) {
        scoreLabel.setText(String.valueOf(score));
    }

    public void showMessage(String htmlText) {
        hasMessage = htmlText != null && !htmlText.isEmpty();
        messageLabel.setText(htmlText == null ? "" : "<html><div style='text-align:center'>" + htmlText + "</div></html>");
    }

    // Mengambil gambar adegan terakhir dan membuat versi abu-abunya
    public void startGameOverEffect() {
        frozenScene = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = frozenScene.createGraphics();
        drawScene(g2);
        g2.dispose();

        grayScene = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = frozenScene.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, gr = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                int lum = (int) (0.299 * r + 0.587 * gr + 0.114 * b);
                grayScene.setRGB(x, y, (lum << 16) | (lum << 8) | lum);
            }
        }
        gameOverStart = System.currentTimeMillis();
    }

    public void clearGameOverEffect() {
        frozenScene = null;
        grayScene = null;
        gameOverStart = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (frozenScene != null) {
            long t = System.currentTimeMillis() - gameOverStart;
            Graphics2D g2 = (Graphics2D) g.create();

            // dasar: adegan berwarna, lalu abu-abu memudar masuk (fade in)
            g2.drawImage(frozenScene, 0, 0, null);
            float grayAlpha = Math.max(0f, Math.min(1f, (t - GRAY_DELAY_MS) / (float) GRAY_FADE_MS));
            g2.setComposite(AlphaComposite.SrcOver.derive(grayAlpha));
            g2.drawImage(grayScene, 0, 0, null);

            // flash merah yang cepat menghilang
            float redAlpha = Math.max(0f, 0.75f * (1f - t / (float) FLASH_MS));
            if (redAlpha > 0f) {
                g2.setComposite(AlphaComposite.SrcOver.derive(redAlpha));
                g2.setColor(Color.RED);
                g2.fillRect(0, 0, width, height);
            }
            g2.dispose();
        } else {
            drawScene(g);
        }

        // Latar semi-transparan di belakang pesan agar mudah dibaca
        if (hasMessage) {
            g.setColor(new Color(0, 0, 0, 110));
            g.fillRoundRect(40, 190, width - 80, 180, 24, 24);
        }
    }

    public void drawScene(Graphics g) {
        if (logic == null) {
            return;
        }

        // Menggambar latar belakang
        if (logic.getBackgroundImage() != null) {
            g.drawImage(logic.getBackgroundImage(), 0, 0, width, height, this);
        }

        // Menggambar seluruh pipa
        ArrayList<Pipe> pipes = logic.getPipes();

        if (pipes != null) {
            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);

                if (pipe.getImage() != null) {
                    g.drawImage(
                            pipe.getImage(),
                            pipe.getPosX(),
                            pipe.getPosY(),
                            pipe.getWidth(),
                            pipe.getHeight(),
                            this
                    );
                }
            }
        }

        // Menggambar burung (dengan rotasi mengikuti gerakan)
        Player player = logic.getPlayer();

        if (player != null && player.getImage() != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double cx = player.getPosX() + player.getWidth() / 2.0;
            double cy = player.getPosY() + player.getHeight() / 2.0;
            g2.rotate(Math.toRadians(player.getAngle()), cx, cy);

            g2.drawImage(
                    player.getImage(),
                    player.getPosX(),
                    player.getPosY(),
                    player.getWidth(),
                    player.getHeight(),
                    this
            );
            g2.dispose();
        }
    }
}

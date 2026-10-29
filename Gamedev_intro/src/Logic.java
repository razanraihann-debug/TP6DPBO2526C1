
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;

public class Logic implements ActionListener, KeyListener {

    // Status permainan
    private enum State { READY, PLAYING, GAME_OVER }

    int frameWidth = 360;
    int frameHeight = 640;

    int playerStartPosX = frameWidth / 2;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;

    // Posisi dan ukuran pipa
    int pipeStartPosX = frameWidth;
    int pipeStartPosY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    Timer gameLoop;
    Timer pipesCooldown;

    int gravity = 1;
    int pipeVelocityX = -2;

    View view;
    Image birdImage;
    Image backgroundImage;
    Player player;

    // Gambar pipa
    Image lowerPipeImage;
    Image upperPipeImage;

    // Daftar pipa
    ArrayList<Pipe> pipes;

    // Skor, status, audio
    private int score = 0;
    private int bestScore = 0;
    private State state = State.READY;
    private final Sound sound;
    private Runnable menuListener;

    public Logic() {
        birdImage = loadImage("Assets/bird.png");
        backgroundImage = loadImage("Assets/background.png");
        lowerPipeImage = loadImage("Assets/lowerPipe.png");
        upperPipeImage = loadImage("Assets/upperPipe.png");

        sound = new Sound();

        player = new Player(
                playerStartPosX,
                playerStartPosY,
                playerWidth,
                playerHeight,
                birdImage
        );

        pipes = new ArrayList<>();

        // Timer untuk memunculkan pipa setiap 1,5 detik (dijalankan saat game dimulai)
        pipesCooldown = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Timer utama game (dijalankan saat tombol Mulai ditekan)
        gameLoop = new Timer(1000 / 60, this);
    }

    // Memuat gambar dan memeriksa keberadaan resource
    private Image loadImage(String path) {
        java.net.URL imageURL = getClass().getResource(path);

        if (imageURL == null) {
            throw new IllegalStateException(
                    "Gambar tidak ditemukan: " + path
            );
        }

        return new ImageIcon(imageURL).getImage();
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setMenuListener(Runnable menuListener) {
        this.menuListener = menuListener;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Pipe> getPipes() {
        return pipes;
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    // Dipanggil dari Main Menu (tombol Mulai)
    public void startGame() {
        resetGame();
        sound.startBgm();
        gameLoop.start();
    }

    // Mengembalikan semua nilai ke kondisi awal (dipakai juga untuk restart)
    private void resetGame() {
        pipesCooldown.stop();
        pipes.clear();

        player.setPosX(playerStartPosX);
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);
        player.setAngle(0);

        score = 0;
        if (view != null) {
            view.clearGameOverEffect();
        }
        state = State.READY;

        if (view != null) {
            view.setScore(score);
            view.showMessage("Tekan SPASI<br>untuk mulai");
            view.repaint();
        }
    }

    private void gameOver() {
        state = State.GAME_OVER;
        pipesCooldown.stop();
        sound.stopBgm();
        sound.play("hit");
        sound.play("gameover");

        if (score > bestScore) {
            bestScore = score;
        }

        if (view != null) {
            view.startGameOverEffect();
            view.showMessage("GAME OVER"
                    + "<br><span style='font-size:16px'>Skor: " + score
                    + " | Terbaik: " + bestScore + "</span>"
                    + "<br><span style='font-size:14px'>Tekan R untuk restart<br>Tekan M untuk menu</span>");
            view.repaint();
        }
    }

    // Membuat sepasang pipa dengan posisi acak

    public void placePipes() {
        int openingSpace = 160;
        int margin = 80;

        // Menentukan posisi celah secara acak dalam batas layar
        int minGapY = margin;
        int maxGapY = frameHeight - openingSpace - margin;

        int gapY = minGapY
                + (int) (Math.random() * (maxGapY - minGapY + 1));

        // Pipa atas berakhir tepat di awal celah
        Pipe upperPipe = new Pipe(
                pipeStartPosX,
                gapY - pipeHeight,
                pipeWidth,
                pipeHeight,
                upperPipeImage
        );

        pipes.add(upperPipe);

        // Pipa bawah dimulai tepat setelah celah
        Pipe lowerPipe = new Pipe(
                pipeStartPosX,
                gapY + openingSpace,
                pipeWidth,
                pipeHeight,
                lowerPipeImage
        );

        pipes.add(lowerPipe);
    }

    // Area tabrakan burung (sedikit lebih kecil dari gambar agar adil)
    private Rectangle playerBounds() {
        int pad = 3;
        return new Rectangle(
                player.getPosX() + pad,
                player.getPosY() + pad,
                player.getWidth() - 2 * pad,
                player.getHeight() - 2 * pad
        );
    }

    public void move() {
        // Selama belum mulai / sudah game over, tidak ada yang bergerak
        if (state != State.PLAYING) {
            return;
        }

        // Gerakan burung akibat gravitasi
        player.setVelocityY(player.getVelocityY() + gravity);
        player.setPosY(player.getPosY() + player.getVelocityY());
        player.setPosY(Math.max(player.getPosY(), 0));

        // Efek swing: burung mendongak saat loncat, lalu perlahan menukik saat jatuh
        double targetAngle = Math.max(-30, Math.min(80, player.getVelocityY() * 4.5));
        player.setAngle(player.getAngle() + (targetAngle - player.getAngle()) * 0.2);

        // Game over jika jatuh ke batas bawah JFrame
        if (player.getPosY() + player.getHeight() >= frameHeight) {
            player.setPosY(frameHeight - player.getHeight());
            gameOver();
            return;
        }

        Rectangle playerRect = playerBounds();

        // Menggerakkan pipa ke kiri
        Iterator<Pipe> iterator = pipes.iterator();

        while (iterator.hasNext()) {
            Pipe pipe = iterator.next();

            pipe.setPosX(pipe.getPosX() + pipeVelocityX);

            // Game over jika burung menyentuh pipa
            Rectangle pipeRect = new Rectangle(
                    pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight());

            if (playerRect.intersects(pipeRect)) {
                gameOver();
                return;
            }

            // Skor +1 saat burung melewati pipa (dihitung dari pipa atas saja,
            // supaya sepasang pipa hanya bernilai satu poin)
            if (!pipe.isPassed() && player.getPosX() > pipe.getPosX() + pipe.getWidth()) {
                pipe.setPassed(true);

                if (pipe.getImage() == upperPipeImage) {
                    score++;

                    // Setiap kelipatan 10: efek spesial, selain itu efek skor biasa
                    if (score % 10 == 0) {
                        sound.playSpecial();
                    } else {
                        sound.play("score");
                    }

                    if (view != null) {
                        view.setScore(score);
                    }
                }
            }

            // Perbaikan: hapus pipa yang sudah keluar layar
            if (pipe.getPosX() + pipe.getWidth() < 0) {
                iterator.remove();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();

        if (view != null) {
            view.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Tidak digunakan
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE) {
            if (state == State.READY) {
                // Flap pertama memulai permainan
                state = State.PLAYING;
                if (view != null) {
                    view.showMessage(null);
                }
                placePipes();
                pipesCooldown.start();
            }

            if (state == State.PLAYING) {
                player.setVelocityY(-10);
                sound.play("flap");
            }
        } else if (key == KeyEvent.VK_R && state == State.GAME_OVER) {
            // Restart permainan
            resetGame();
            sound.startBgm();
        } else if (key == KeyEvent.VK_M && state == State.GAME_OVER) {
            // Kembali ke Main Menu
            gameLoop.stop();
            if (menuListener != null) {
                menuListener.run();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Tidak digunakan
    }
}

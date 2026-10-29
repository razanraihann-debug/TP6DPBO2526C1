import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Pengelola audio (BGM dan sound effect). Jika perangkat audio tidak ada,
// game tetap berjalan tanpa suara.
public class Sound {

    private final Map<String, Clip> clips = new HashMap<>();
    private Clip bgm;

    public Sound() {
        for (String name : new String[]{"flap", "score", "special", "hit", "gameover"}) {
            clips.put(name, load(name));
        }
        bgm = load("bgm");
    }

    private Clip load(String name) {
        try {
            URL url = getClass().getResource("Assets/" + name + ".wav");
            if (url == null) {
                return null;
            }
            AudioInputStream in = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(url.openStream()));
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            return clip;
        } catch (Exception | Error e) {
            System.err.println("Audio '" + name + "' tidak dapat dimuat: " + e);
            return null;
        }
    }

    public void play(String name) {
        Clip clip = clips.get(name);
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    // Efek suara spesial (kelipatan 10): mirip efek skor, dengan satu nada tambahan
    public void playSpecial() {
        play("special");
    }

    public void startBgm() {
        if (bgm == null || bgm.isRunning()) {
            return;
        }
        setVolume(bgm, 0.35f);
        bgm.setFramePosition(0);
        bgm.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopBgm() {
        if (bgm != null) {
            bgm.stop();
        }
    }

    private void setVolume(Clip clip, float linear) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue((float) (20.0 * Math.log10(linear)));
        } catch (Exception ignored) {
            // kontrol volume tidak tersedia
        }
    }
}

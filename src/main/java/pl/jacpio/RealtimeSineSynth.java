
package pl.jacpio;

import com.formdev.flatlaf.FlatLightLaf;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealtimeSineSynth {

    public static void main(String[] args) {
       FlatLightLaf.setup();
       SwingUtilities.invokeLater(() -> new RealtimeSineSynth().createAndShowGUI());
    }

    private JPanel tonesPanel;

    private final float SAMPLE_RATE = 44100f;
    private final int SAMPLE_BITS = 16;
    private final int CHANNELS = 1;
    private final int BUFFER_SECONDS = 2;
    private final int CIRCULAR_BUFFER_SIZE = (int) (SAMPLE_RATE * BUFFER_SECONDS);

    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final float[] circularBuffer = new float[CIRCULAR_BUFFER_SIZE];
    private int writeIndex = 0;

    private JFrame frame;
    private WavePanel wavePanel;
    private FFTPanel fftPanel;
    private JTabbedPane tabbedPane;
    private JButton startStopBtn;

    private Thread audioThread;
    private SourceDataLine line;

    private final List<Tone> tones = new ArrayList<>();

    private void createAndShowGUI() {

        frame = new JFrame("Syntezator pokazowy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        tonesPanel = new JPanel();
        tonesPanel.setLayout(new BoxLayout(tonesPanel, BoxLayout.Y_AXIS));

        JScrollPane tonesScroll = new JScrollPane(tonesPanel);
        tonesScroll.setPreferredSize(new Dimension(360, 0));

        JButton addToneBtn = new JButton("+ Ton");
        addToneBtn.addActionListener(e -> addNewTone());

        startStopBtn = new JButton("Start");
        startStopBtn.addActionListener(e -> togglePlayback());

        JButton savePlotBtn = new JButton("Zapisz wykres (PNG)");
        savePlotBtn.addActionListener(e -> saveCurrentPlot());

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        leftBottom.add(addToneBtn);
        leftBottom.add(startStopBtn);
        leftBottom.add(savePlotBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        leftPanel.add(tonesScroll, BorderLayout.CENTER);
        leftPanel.add(leftBottom, BorderLayout.SOUTH);

        frame.add(leftPanel, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();

        wavePanel = new WavePanel(
                circularBuffer,
                () -> writeIndex,
                tones
        );

        fftPanel = new FFTPanel(SAMPLE_RATE);

        tabbedPane.addTab("Oscyloskop", wavePanel);
        tabbedPane.addTab("FFT (widmo)", fftPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            fftPanel.updateFFT(circularBuffer, writeIndex);
            wavePanel.repaint();
            fftPanel.repaint();
            System.out.println("writeIndex: " + writeIndex);
        });

        addNewTone();

        Timer repaintTimer = new Timer(50, e -> {
            wavePanel.repaint();
            fftPanel.updateFFT(circularBuffer, writeIndex);
            fftPanel.repaint();
        });
        repaintTimer.start();
    }



    private void addNewTone() {
        Tone tone = new Tone(440, 0.5, SAMPLE_RATE);

        synchronized (tones) {
            tones.add(tone);
        }

        ToneControlPanel panel = new ToneControlPanel(tone);
        tonesPanel.add(panel);

        tonesPanel.revalidate();
        tonesPanel.repaint();
    }

    private void togglePlayback() {
        if (!playing.get()) startAudio();
        else stopAudio();
    }

    private void startAudio() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_BITS, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, 4096);
            line.start();
        } catch (LineUnavailableException ex) {
            JOptionPane.showMessageDialog(frame, "Nie można otworzyć linii audio: " + ex.getMessage(),
                    "Błąd audio", JOptionPane.ERROR_MESSAGE);
            return;
        }

        playing.set(true);
        startStopBtn.setText("Stop");

        audioThread = new Thread(() -> {

            byte[] byteBuf = new byte[1024 * 2];
            int samplesPerChunk = byteBuf.length / 2;

            while (playing.get()) {

                for (int i = 0; i < samplesPerChunk; i++) {

                    double sample = 0.0;

                    synchronized (tones) {
                        for (Tone tone : tones) {
                            if (!tone.isEnabled()) continue;

                            double s = tone.nextSample();

                            if (!Double.isNaN(s) && !Double.isInfinite(s)) {
                                sample += s;
                            }
                        }
                    }

                    if (sample > 1.0) sample = 1.0;
                    if (sample < -1.0) sample = -1.0;

                    synchronized (circularBuffer) {
                        circularBuffer[writeIndex] = (float) sample;
                        writeIndex = (writeIndex + 1) % circularBuffer.length;
                    }

                    short pcm = (short) (sample * Short.MAX_VALUE);
                    int idx = i * 2;
                    byteBuf[idx]     = (byte) (pcm & 0xff);
                    byteBuf[idx + 1] = (byte) ((pcm >> 8) & 0xff);
                }

                line.write(byteBuf, 0, byteBuf.length);
            }

            if (line != null) {
                line.drain();
                line.stop();
                line.close();
            }

        }, "Audio-Generator");

        audioThread.setDaemon(true);
        audioThread.start();
    }

    private void stopAudio() {
        playing.set(false);
        startStopBtn.setText("Start");
    }





    private void saveCurrentPlot() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp == null) return;
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        comp.paint(g);
        g.dispose();
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("plot.png"));
        int res = fc.showSaveDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try { javax.imageio.ImageIO.write(img, "png", f); JOptionPane.showMessageDialog(frame, "Zapisano: " + f.getAbsolutePath()); }
            catch (IOException ex) { JOptionPane.showMessageDialog(frame, "Błąd zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE); }
        }
    }
}

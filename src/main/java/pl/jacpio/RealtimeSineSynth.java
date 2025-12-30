package pl.jacpio;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RealtimeSineSynth {

    public static void main(String[] args) {
        System.setProperty("flatlaf.useNativeWindowDecorations", "false");
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new RealtimeSineSynth().createAndShowGUI());
    }

    private JPanel tonesPanel;
    private final List<Short> recordedSamples = new ArrayList<>();

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

        frame = new JFrame("Realtime Sine Synth");
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

        JButton exportWavBtn = new JButton("Eksport WAV");
        exportWavBtn.addActionListener(e -> exportWav());

        JButton themeBtn = new JButton("Dark / Light");
        themeBtn.addActionListener(e -> toggleTheme());

        JPanel topButtons = new JPanel();
        topButtons.setLayout(new GridLayout(2, 1, 6, 6));
        topButtons.setBorder(BorderFactory.createTitledBorder("Sterowanie"));

        topButtons.add(startStopBtn);
        topButtons.add(addToneBtn);
        topButtons.add(savePlotBtn);
        topButtons.add(exportWavBtn);
        topButtons.add(themeBtn);

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        leftPanel.add(topButtons, BorderLayout.SOUTH);
        leftPanel.add(tonesScroll, BorderLayout.CENTER);

        frame.add(leftPanel, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();

        wavePanel = new WavePanel(
                circularBuffer,
                () -> writeIndex,
                tones
        );

        fftPanel = new FFTPanel(SAMPLE_RATE);

        tabbedPane.addTab("Oscyloskop", wavePanel);
        tabbedPane.addTab("FFT", fftPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        addNewTone();

        Timer repaintTimer = new Timer(50, e -> {
            wavePanel.repaint();
            fftPanel.updateFFT(circularBuffer, writeIndex);
            fftPanel.repaint();
        });
        repaintTimer.start();
    }

    private void toggleTheme() {
        try {
            if (UIManager.getLookAndFeel() instanceof FlatLightLaf)
                FlatDarkLaf.setup();
            else
                FlatLightLaf.setup();

            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewTone() {
        Tone tone = new Tone(440, 0.5, SAMPLE_RATE);
        synchronized (tones) {
            tones.add(tone);
        }
        tonesPanel.add(new ToneControlPanel(tone));
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
            line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
            line.open(format, 4096);
            line.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Audio error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        recordedSamples.clear();
        playing.set(true);
        startStopBtn.setText("Stop");

        audioThread = new Thread(() -> {
            byte[] buffer = new byte[2048];
            while (playing.get()) {
                for (int i = 0; i < buffer.length / 2; i++) {
                    double sample = 0.0;
                    synchronized (tones) {
                        for (Tone t : tones)
                            if (t.isEnabled()) sample += t.nextSample();
                    }
                    sample = Math.max(-1, Math.min(1, sample));
                    short pcm = (short) (sample * Short.MAX_VALUE);
                    recordedSamples.add(pcm);

                    buffer[i * 2] = (byte) (pcm & 0xff);
                    buffer[i * 2 + 1] = (byte) ((pcm >> 8) & 0xff);

                    synchronized (circularBuffer) {
                        circularBuffer[writeIndex] = (float) sample;
                        writeIndex = (writeIndex + 1) % circularBuffer.length;
                    }
                }
                line.write(buffer, 0, buffer.length);
            }
            line.drain();
            line.close();
        });

        audioThread.setDaemon(true);
        audioThread.start();
    }

    private void stopAudio() {
        playing.set(false);
        startStopBtn.setText("Start");
    }

    private void saveCurrentPlot() {
        Component comp = tabbedPane.getSelectedComponent();
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
        comp.paint(img.getGraphics());

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                javax.imageio.ImageIO.write(img, "png", fc.getSelectedFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportWav() {
        if (recordedSamples.isEmpty()) return;

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        short[] data = new short[recordedSamples.size()];
        for (int i = 0; i < data.length; i++) data[i] = recordedSamples.get(i);

        WavWriter.write(fc.getSelectedFile(), data, (int) SAMPLE_RATE);
    }
}

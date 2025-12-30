package pl.jacpio;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.IntSupplier;

public class WavePanel extends JPanel {

    private final float[] circularBuffer;
    private final IntSupplier writeIndexSupplier;
    private final List<Tone> tones;
    private double zoom = 1.0;

    public WavePanel(float[] circularBuffer, IntSupplier writeIndexSupplier, List<Tone> tones) {
        this.circularBuffer = circularBuffer;
        this.writeIndexSupplier = writeIndexSupplier;
        this.tones = tones;
        setBackground(Color.white);
        setBorder(BorderFactory.createLineBorder(Color.gray));

        addMouseWheelListener(e -> {
            zoom *= e.getWheelRotation() < 0 ? 1.1 : 0.9;
            zoom = Math.max(0.2, Math.min(5.0, zoom));
            repaint();
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawWave((Graphics2D) g);
    }

    private void drawWave(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.BLACK);
        g2.drawLine(0, h / 2, w, h / 2);

        float[] copy;
        int len = (int)(Math.min(w, 2048) / zoom);
        copy = new float[len];
        synchronized (circularBuffer) {
            int writeIndex = writeIndexSupplier.getAsInt();
            int readIndex = writeIndex - len;
            if (readIndex < 0) readIndex += circularBuffer.length;
            for (int i = 0; i < len; i++) {
                copy[i] = circularBuffer[(readIndex + i) % circularBuffer.length];
            }
        }

        double vertScale = (h / 2.0) * 0.9;

        g2.setColor(new Color(0, 120, 255));
        int prevX = 0;
        int prevY = h / 2;
        for (int i = 0; i < len; i++) {
            int x = (int) ((double) i / (len - 1) * (w - 1));
            int y = (int) ((double) h / 2 - copy[i] * vertScale);
            g2.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        int yPos = 18;
        synchronized (tones) {
            for (Tone t : tones) {
                if (t.isEnabled()) {
                    String s = String.format("%.0fHz %.0f%%", t.getFrequency(), t.getAmplitude() * 100);
                    g2.drawString(s, 8, yPos);
                    yPos += 18;
                }
            }
        }
    }
}
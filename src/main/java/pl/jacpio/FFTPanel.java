package pl.jacpio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FFTPanel extends JPanel {
    private final float sampleRate;
    private final int fftSize = 4096;
    private final float[] window;
    private final double[] magnitude;
    private int lastComputedSize = 0;

    private BufferedImage backBuffer;

    public FFTPanel(float sampleRate) {
        this.sampleRate = sampleRate;
        setBackground(Color.white);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        window = new float[fftSize];
        for (int i = 0; i < fftSize; i++)
            window[i] = (float) (0.5 * (1 - Math.cos(2 * Math.PI * i / (fftSize - 1))));
        magnitude = new double[fftSize / 2];
    }

    public void updateFFT(float[] circularBuffer, int writeIndex) {
        double[] real = new double[fftSize];
        double[] imag = new double[fftSize];
        int readIndex = (writeIndex - fftSize + circularBuffer.length) % circularBuffer.length;

        if (readIndex + fftSize <= circularBuffer.length) {
            for (int i = 0; i < fftSize; i++) {
                real[i] = circularBuffer[readIndex + i] * window[i];
                imag[i] = 0.0;
            }
        } else {
            int firstPart = circularBuffer.length - readIndex;
            for (int i = 0; i < firstPart; i++) {
                real[i] = circularBuffer[readIndex + i] * window[i];
                imag[i] = 0.0;
            }
            for (int i = firstPart; i < fftSize; i++) {
                real[i] = circularBuffer[i - firstPart] * window[i];
                imag[i] = 0.0;
            }
        }

        fft(real, imag);
        for (int i = 0; i < fftSize / 2; i++) {
            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }
        lastComputedSize = fftSize / 2;

        createBackBuffer();
    }

    private void createBackBuffer() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        backBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = backBuffer.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(0, h - 20, w, h - 20);

        double max = 1e-9;
        for (int i = 1; i < lastComputedSize; i++) {
            if (magnitude[i] > max) max = magnitude[i];
        }

        g2.setColor(new Color(200, 30, 30));
        int binsToDisplay = lastComputedSize;
        for (int i = 1; i < binsToDisplay; i++) {
            double freq = i * sampleRate / fftSize;
            if (freq > 20000) break;
            int x = (int) ((Math.log10(freq + 1) / Math.log10(20000 + 1)) * (w - 1));
            double value = magnitude[i] / max;
            if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) continue;

            double db = 20 * Math.log10(value);
            db = Math.max(-120, Math.min(0, db));
            double norm = (db + 120) / 120.0;
            int y = (int) ((1.0 - norm) * (h - 30)) + 10;
            g2.drawLine(x, h - 20, x, y);
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        String info = String.format("Rozmiar FFT: %d  Częstotliwość próbkowania: %.0f Hz", fftSize, sampleRate);
        g2.drawString(info, 8, 18);
        g2.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backBuffer != null) {
            g.drawImage(backBuffer, 0, 0, null);
        }
    }

    private void fft(double[] real, double[] imag) {
        int n = real.length;
        if (Integer.bitCount(n) != 1) throw new IllegalArgumentException("FFT length must be power of 2");

        int j = 0;
        for (int i = 0; i < n; i++) {
            if (i < j) {
                double tr = real[i];
                double ti = imag[i];
                real[i] = real[j];
                imag[i] = imag[j];
                real[j] = tr;
                imag[j] = ti;
            }
            int m = n >> 1;
            while (m >= 1 && j >= m) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }

        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2.0 * Math.PI / len;
            double wlenReal = Math.cos(angle);
            double wlenImag = Math.sin(angle);
            for (int i = 0; i < n; i += len) {
                double ur = 1.0;
                double ui = 0.0;
                for (int k = 0; k < len / 2; k++) {
                    int evenIndex = i + k;
                    int oddIndex = i + k + (len / 2);
                    double vr = real[oddIndex] * ur - imag[oddIndex] * ui;
                    double vi = real[oddIndex] * ui + imag[oddIndex] * ur;

                    real[oddIndex] = real[evenIndex] - vr;
                    imag[oddIndex] = imag[evenIndex] - vi;
                    real[evenIndex] += vr;
                    imag[evenIndex] += vi;

                    double nextUr = ur * wlenReal - ui * wlenImag;
                    double nextUi = ur * wlenImag + ui * wlenReal;
                    ur = nextUr;
                    ui = nextUi;
                }
            }
        }
    }
}
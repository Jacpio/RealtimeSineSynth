package pl.jacpio;

import javax.sound.sampled.*;
import java.io.*;

public class WavWriter {

    public static void write(File file, short[] samples, int sampleRate) {
        try {
            byte[] data = new byte[samples.length * 2];
            for (int i = 0; i < samples.length; i++) {
                data[i * 2] = (byte) (samples[i] & 0xff);
                data[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xff);
            }

            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            AudioInputStream ais = new AudioInputStream(bais, format, samples.length);

            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

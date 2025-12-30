package pl.jacpio;

class Tone {
    private double frequency;
    private double amplitude;
    private double phase = 0.0;
    private boolean enabled = true;
    private final double sampleRate;
    private WaveType waveType = WaveType.SINE;

    public Tone(double frequency, double amplitude, double sampleRate) {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.sampleRate = sampleRate;
    }

    public double nextSample() {
        double v;
        switch (waveType) {
            case SQUARE -> v = Math.signum(Math.sin(phase));
            case TRIANGLE -> v = 2 / Math.PI * Math.asin(Math.sin(phase));
            case SAW -> v = 2 * (phase / (2 * Math.PI)) - 1;
            default -> v = Math.sin(phase);
        }

        phase += 2.0 * Math.PI * frequency / sampleRate;
        if (phase >= 2 * Math.PI) phase -= 2 * Math.PI;

        return enabled ? v * amplitude : 0.0;
    }

    public WaveType getWaveType() { return waveType; }
    public void setWaveType(WaveType waveType) { this.waveType = waveType; }

    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }

    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

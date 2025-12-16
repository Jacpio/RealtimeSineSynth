package pl.jacpio;

class Tone {
    private double frequency;
    private double amplitude;
    private double phase = 0.0;
    private boolean enabled = true;
    private final double sampleRate;

    public Tone(double frequency, double amplitude, double sampleRate) {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.sampleRate = sampleRate;
    }

    public double nextSample() {
        double value = Math.sin(phase) * amplitude;
        phase += 2.0 * Math.PI * frequency / sampleRate;
        if (phase > Math.PI * 2) phase -= Math.PI * 2;
        return enabled ? value : 0.0;
    }

    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }

    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

package pl.jacpio;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
class ToneControlPanel extends JPanel {

    ToneControlPanel(Tone tone) {

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Ton"));

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        setPreferredSize(new Dimension(320, 190));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JCheckBox enabledBox = new JCheckBox("Włączony", tone.isEnabled());
        enabledBox.addActionListener(e -> tone.setEnabled(enabledBox.isSelected()));

        JTextField freqField = new JTextField(String.valueOf((int) tone.getFrequency()));
        JTextField ampField = new JTextField(String.valueOf((int) (tone.getAmplitude() * 100)));

        ((AbstractDocument) freqField.getDocument()).setDocumentFilter(new IntFilter(1, 20000));
        ((AbstractDocument) ampField.getDocument()).setDocumentFilter(new IntFilter(0, 100));

        JLabel freqLabel = new JLabel("Częstotliwość (Hz)");
        JLabel ampLabel = new JLabel("Amplituda (%)");

        JSlider freqSlider = new JSlider(1, 20000, (int) tone.getFrequency());
        JSlider ampSlider = new JSlider(0, 100, (int) (tone.getAmplitude() * 100));

        freqSlider.addChangeListener(e -> {
            int v = freqSlider.getValue();
            tone.setFrequency(v);
            freqField.setText(String.valueOf(v));
        });

        ampSlider.addChangeListener(e -> {
            int v = ampSlider.getValue();
            tone.setAmplitude(v / 100.0);
            ampField.setText(String.valueOf(v));
        });

        freqField.addActionListener(e -> {
            int v = Integer.parseInt(freqField.getText());
            freqSlider.setValue(v);
        });

        ampField.addActionListener(e -> {
            int v = Integer.parseInt(ampField.getText());
            ampSlider.setValue(v);
        });

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        add(enabledBox, c);

        c.gridwidth = 1;
        c.gridy++;
        add(freqLabel, c);
        c.gridx = 1;
        add(freqField, c);

        c.gridx = 0; c.gridy++;
        c.gridwidth = 2;
        add(freqSlider, c);

        c.gridwidth = 1;
        c.gridy++;
        add(ampLabel, c);
        c.gridx = 1;
        add(ampField, c);

        c.gridx = 0; c.gridy++;
        c.gridwidth = 2;
        add(ampSlider, c);
    }

    static class IntFilter extends DocumentFilter {
        private final int min;
        private final int max;

        IntFilter(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);

            if (next.isEmpty()) return;

            try {
                int val = Integer.parseInt(next);
                if (val >= min && val <= max) {
                    super.replace(fb, offset, length, text, attrs);
                }
            } catch (NumberFormatException ignored) {}
        }
    }
}

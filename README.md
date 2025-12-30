# Realtime Sine Synth

🌍 **Languages / Języki:**  
[🇵🇱 Polski](#-wersja-polska) | [🇬🇧 English](#-english-version)

---

## Table of Contents / Spis treści

- [🇵🇱 Wersja polska](#-wersja-polska)
- [🇬🇧 English version](#-english-version)
- [License / Licencja](#license--licencja)

---

## 🇵🇱 Wersja polska

### Opis projektu

**Realtime Sine Synth** to edukacyjna aplikacja napisana w Javie, służąca do:

- generowania dźwięków sinusoidalnych w czasie rzeczywistym
- wizualizacji przebiegu fali (oscyloskop)
- analizy widma częstotliwości (FFT)
- obserwowania nakładania się fal oraz harmonicznych

Aplikacja jest przeznaczona głównie do **nauki fizyki, akustyki i przetwarzania sygnałów**.

---

### Funkcje

- Generowanie **wielu tonów sinusoidalnych jednocześnie**
- Regulacja **częstotliwości (Hz)** i **amplitudy (%)** dla każdego tonu
- Włączanie / wyłączanie poszczególnych tonów
- Dodawanie kolejnych fal jednym kliknięciem
- Oscyloskop – wykres fali w dziedzinie czasu
- FFT – analiza widma częstotliwości do 20 kHz
- Wizualizacja harmonicznych i interferencji
- Start / Stop generowania dźwięku w czasie rzeczywistym

---

### Zastosowania edukacyjne

- Zrozumienie czym jest **częstotliwość i amplituda**
- Obserwacja **superpozycji fal**
- Nauka **FFT (Szybka Transformacja Fouriera)**
- Porównanie sygnału w czasie i w dziedzinie częstotliwości
- Wprowadzenie do syntezy dźwięku

---

### Wymagania

- Java **17 lub nowsza**
- System operacyjny:
    - Windows
    - Linux
- Działająca karta dźwiękowa

---

### Uruchamianie

```bash
 java -jar Jacpio RSS-1.0.jar
```

---

### Kompilacja (Gradle)

```bash
./gradlew clean build
```

---

## 🇬🇧 English Version

### Description

**Realtime Sine Synth** is an educational Java application designed for:

- real-time sine wave audio generation
- waveform visualization (oscilloscope)
- frequency spectrum analysis (FFT)
- observing wave superposition and harmonics

The application is mainly intended for **learning physics, acoustics, and digital signal processing (DSP)**.

---

### Features

- Real-time generation of **multiple sine waves simultaneously**
- Adjustable **frequency (Hz)** and **amplitude (%)** per tone
- Enable / disable individual tones
- Add new waves dynamically
- Oscilloscope – time-domain waveform display
- FFT – frequency spectrum analysis up to 20 kHz
- Visualization of harmonics and interference
- Start / Stop real-time sound generation

---

### Educational Purpose

The application helps users understand:

- **Frequency** and **amplitude**
- **Wave superposition**
- **Fast Fourier Transform (FFT)**
- Time-domain vs frequency-domain signals
- Basics of digital sound synthesis

---

### Requirements

- **Java 17 or newer**
- Operating systems:
    - Windows
    - Linux
- Working audio output device

---

### Running the Application

```bash
java -jar Jacpio RSS-1.0.jar
```

---

### Building the Project

Using **Gradle**:

```bash
./gradlew clean build
```

---


## License / Licencja

This project is licensed under the **MIT License**.  
Projekt jest udostępniany na licencji **MIT**.

➡️ [LICENSE](LICENSE)

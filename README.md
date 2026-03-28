# VisionCart 🛒
**The Multimodal Grocery Assistant for Low-Vision Shoppers**

> An accessible mobile application designed to empower individuals with low vision to shop for groceries independently using a combination of **Camera**, **Voice**, and **Audio** inputs.

---

## 🌟 The Vision
Individuals with low vision (BCVA 20/70 to 20/400) face significant barriers in grocery stores: small-print labels, complex ingredient lists, and hidden expiration dates. **VisionCart** bridges this gap by replacing visual effort with multimodal interaction.

## 🛠️ The Three Multimodal Pillars

### 1. Camera Input — See 👁️
- **Real-Time Scanning:** Uses Google **ML Kit Barcode Scanning** for immediate product identification.
- **AI Expiration Detection:** Leverages **Google Gemini 1.5 Flash** to vision-read printed expiration dates that barcodes don't carry.
- **No Manual Focus:** The app auto-detects barcodes without needing a button press.

### 2. Voice Input — Speak 🗣️
- **Hands-Free Navigation:** Built-in **Speech Recognizer** allows users to control the app through voice.
- **Commands:** 
  - *"Read ingredients"* — Deep-dive into the product's contents.
  - *"Any allergens?"* — Immediate confirmation of product safety.
  - *"Repeat"* — Re-hear the structured product summary.
  - *"Back"* — Quickly return to the scanner.

### 3. Audio Output — Hear 🎧
- **Structured TTS Summary:** Delivers information in a logical hierarchy: `Name -> Health Rating -> Expiration -> Allergens`.
- **"Priority Alert" Pitch Scaling:** 
  - **Normal Pitch:** For general product info.
  - **High Pitch / Urgent:** Automatically triggered when reading **Allergen Warnings** or **Unhealthy** items (based on custom nutritional thresholds).
  - **Instant Context:** It lets the user know "pay attention, this is important" without them needing to listen for specific words—the change in voice alone conveys the warning.


---

## 🏗️ Technical Architecture

- **Language:** 100% Kotlin
- **Networking:** Retrofit + OkHttp + Kotlin Serialization
- **Database (Global):** Open Food Facts API (Real-time cloud lookup)
- **AI Engine:** 
  - **ML Kit:** On-Device Barcode Detection
  - **Generative AI SDK:** Google Gemini Pro Vision
- **UI/UX (Accessibility):**
  - **60dp Tap Zones:** Minimum target size for all interactive elements.
  - **High Contrast:** Pure black backgrounds with bright Yellow/White text.
  - **Dynamic Scaling:** Fully compatible with Android magnification and TalkBack.

## 🚀 Getting Started

To run the application with full AI capabilities:

1.  Clone the repository.
2.  Add your **Google Gemini API Key** to `local.properties`:
    ```properties
    GEMINI_API_KEY=your_key_here
    ```
3.  Ensure your device has **Internet access** and **Camera permissions**.
4.  Build and Run!

## 🌏 Impact in the Philippines
In the Philippine context, uncorrected refractive error is a leading cause of visual impairment. VisionCart provides a free, technology-driven solution for millions of Filipinos experiencing functional low vision daily, restoring their autonomy and privacy in the supermarket.

---
*Created with ❤️ for Vision Accessibility.*

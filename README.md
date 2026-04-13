# VisionCart

Advanced Multimodal Grocery Assistant for Vision Accessibility

VisionCart is a specialized mobile application designed to empower individuals with low vision to shop for groceries independently. By combining state-of-the-art AI, lightning-fast barcode scanning, and multi-themed high-contrast interfaces, VisionCart transforms complex visual information into clear audio and haptic feedback.

---

## Key Features

### 1. Intelligent Product Recognition
*   **Fast Barcode Scanning**: Powered by Google ML Kit, the scanner identifies products instantly without requiring manual focus or button presses.
*   **Enhanced Gemini AI Integration**: Uses Google Gemini 1.5 Flash to identify products with high accuracy. The AI now detects **Brand, Product Name, Weight, Category, Ingredients, Allergens, and Health Ratings** directly from the camera frame.
*   **AI Question & Answer**: Users can ask specific questions about a product (e.g., "Does this contain nuts?" or "Is this healthy?") for immediate AI-driven answers.

### 2. Two-Way Voice Communication
*   **"VisionCart" Wake Word**: The application is always listening for the "VisionCart" keyword. Saying it will activate the assistant for hands-free operation.
*   **Voice Commands**: Users can say "VisionCart, scan" or "VisionCart, history" to navigate the app entirely by voice.
*   **Continuous Engagement**: The assistant responds with "Yes? I am listening" and facilitates a natural dialogue for grocery tasks.

### 3. High-Contrast Visual Customization
VisionCart features three distinct, professionally designed color schemes optimized for different types of visual impairment:
*   **Yellow on Black**: The primary high-visibility theme for maximum contrast.
*   **White on Blue**: A cool, high-detail theme for reduced eye strain.
*   **Black on White**: A classic high-contrast light mode for well-lit environments (Refined for visibility across all product detail cards).

### 4. Advanced Accessibility Support
*   **Extra Large Font Scaling**: All primary buttons and UI elements use dynamic sizing. When system fonts are set to "Extra Large," the interface automatically expands to prevent clipping and maintain legibility.
*   **Speaking Assistant**: A persistent voice assistant provides constant orientation, reading navigation labels and product details aloud. TTS is automatically managed to prevent overlapping speech during navigation.
*   **Haptic Feedback**: Meaningful vibration patterns confirm successful scans, navigation clicks, and critical alerts.

### 5. Offline-First History
*   **Room Database Persistence**: All scanned products are stored locally in a secure, high-performance database.
*   **Shopping List Integration**: Users can "star" specific items in their history to create a quick-access shopping list for future trips.

---

## Technical Stack

*   **Platform**: Native Android (Kotlin)
*   **Architecture**: MVVM with Coroutines and Flow
*   **AI Engine**: Google AI Studio (Gemini 1.5 Flash SDK) & ML Kit Barcode Scanning
*   **Database**: Room Persistence Library
*   **UI Components**: Material 3 with Custom High-Contrast Attributes
*   **Network**: Retrofit 2 & OkHttp 4

---

## Setup and Installation

To build and run VisionCart with full functionality, follow these steps:

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/mardnts28/VisionCart.git
    ```

2.  **Configure API Keys**:
    Add your Google Gemini API Key to the `local.properties` file in the project root:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```

3.  **Firebase Setup**:
    Place your `google-services.json` file in the `app/` directory.

4.  **Hardware Requirements**:
    *   Android Device running API 24 (Nougat) or higher.
    *   Camera with Autofocus support.
    *   Active Internet connection (required for Gemini AI features).

---

## Privacy and Security

VisionCart is designed with user privacy in mind. Sensitive configuration files such as `local.properties` and `google-services.json` are excluded from version control via `.gitignore`. Product history is stored strictly on the local device and is never uploaded to external servers without explicit user action.

---

*Mission: Restoring autonomy and privacy to the grocery shopping experience through multimodal technology.*

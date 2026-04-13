# VisionCart

Advanced Multimodal Grocery Assistant for Vision Accessibility

VisionCart is a specialized mobile application designed to empower individuals with low vision to shop for groceries independently. By combining state-of-the-art AI, lightning-fast barcode scanning, and multi-themed high-contrast interfaces, VisionCart transforms complex visual information into clear audio and haptic feedback.

---

## Key Features

### 1. Intelligent Product Recognition
*   **Fast Barcode Scanning**: Powered by Google ML Kit, the scanner features an **enlarged 450dp viewfinder** for improved ergonomics, identifying products instantly without requiring precise alignment.
*   **Enhanced Gemini AI Integration**: Uses Google Gemini 1.5 Flash to detect **Brand, Product Name, Weight, Category, Ingredients, Allergens, and Health Ratings** directly from the camera frame.
*   **AI Question & Answer**: A conversational interface allowing users to ask nutritional or product-related questions.

### 2. Functional Voice Assistant
*   **Custom Wake Word**: Listening for "VisionCart" to activate hands-free operation.
*   **Eyes-Free Navigation**: Users can navigate the app using functional commands like *"Go to Gemini search"* or *"Go to History"*.
*   **Specific Theme Control**: Direct theme selection via voice, such as *"Change theme to blue"* or *"Set theme to dark"*.
*   **Continuous Engagement**: Provides orientation and status updates throughout the shopping journey.

### 3. High-Contrast Visual Customization
VisionCart adapts its entire UI and branding assets across three professionally designed color schemes:
*   **Yellow on Black**: High-visibility theme for maximum contrast.
*   **White on Blue**: Reduced eye-strain theme with blue-accented assets.
*   **Black on White**: Classic high-contrast light mode.

### 4. Advanced Accessibility & Safety
*   **Safety-First Allergen Warnings**: The system screens product data for known allergens. If detected, the **TTS engine shifts pitch and volume** for an urgent audio alert, ensuring critical safety information is prioritized.
*   **Cleaned Data Communication**: All metadata is processed to remove language prefixes (e.g., "en:"), providing clear and natural audio output.
*   **Extra Large Font Scaling**: Responsive layouts ensure that even at maximum system font sizes, UI elements remain functional and legible.
*   **Haptic Orientation**: Distinct vibration patterns provide tactile feedback for successful scans and critical alerts.

### 5. Offline-First History & Lists
*   **Local Persistence**: All scanned products are stored in a secure local **Room Database**, allowing for offline review.
*   **Shopping Assistant**: Users can "star" items to build localized shopping lists for future retrieval.

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

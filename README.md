Project Title *
Grama-Sanjeevini: AI-Powered Rural Pharmacy Network

Short Description
A Native Android application built with Jetpack Compose that bridges the healthcare gap in rural areas. It utilizes the Gemini AI API to instantly digitize handwritten prescriptions and connects villagers to local pharmacies for real-time inventory checking and route mapping.

Live URL
(Since this is a mobile app and not a website, you should provide a Google Drive link to download your .apk file here, or write "N/A - Native Android App. Please refer to GitHub for source code and Demo Video.")

Problem Statement
In rural areas, patients frequently struggle to decipher complex handwritten medical prescriptions and often travel long distances to multiple pharmacies only to find critical medicines are out of stock. There is a severe digital disconnect between rural patients and local medical inventory, leading to delayed treatments and wasted travel time. Grama-Sanjeevini solves this by digitizing prescriptions via AI and cross-referencing them with local pharmacy databases to confirm stock and location instantly.

Technologies Used
Frontend: Kotlin, Jetpack Compose (Declarative UI)
Backend/Database: Firebase Authentication (Email and Phone/OTP), Firebase Firestore (NoSQL Real-Time Sync)
AI Integration: Google Generative AI SDK (Gemini 2.5 Flash for JSON-based OCR and data extraction)
Architecture: MVVM (Model-View-ViewModel), Kotlin Coroutines (Asynchronous multi-threading)
Native Integrations: Android OS Intents (Emergency Dialer)

Research paper published
(If the IEEE International Conference paper you previously published is related to this project, paste the link or DOI here. If it was for a different project, you should enter: "N/A for this specific project build.")

PRD Document Outline (Save as PDF)

Product Vision
To eliminate the uncertainty of rural healthcare logistics by connecting villagers directly to local medical inventory through an accessible, AI-driven mobile interface.

Target Audience
Villagers/Patients: Individuals in rural areas needing quick access to medicine availability and emergency first-aid protocols.
Pharmacists: Local medical store owners who need to manage inventory and receive direct order inquiries.

Core Features (MVP)
AI Prescription Scanner: Upload an image of a prescription; Gemini AI parses the text and extracts medicine names into a structured JSON format.
Real-Time Inventory Match: Cross-references parsed medicines with local Firestore databases to verify stock and price.
Multi-Role Authentication: Secure login routing for Villagers vs. Pharmacists using Firebase.
Emergency Helper Hub: Native 104 dialer integration and offline-accessible First-Aid digital flashcards.

Technical Requirements
Minimum SDK: API 24 (Android 7.0)
Internet connection required for AI parsing and live database syncing.
Camera permissions required for prescription scanning.

Future Scope
Integration with Google Maps API for live turn-by-turn navigation to the pharmacy.
Regional language support (Kannada) for the Villager UI.

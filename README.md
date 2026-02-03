# OPD Token Allocation Engine 🏥

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.1-brightgreen?style=for-the-badge&logo=springboot)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

## 📋 Overview

The **OPD (Outpatient Department) Token Allocation Engine** is a robust backend system designed to manage and optimize patient flow in hospital departments. It handles doctor schedules, time slots, and intelligent token generation to reduce waiting times and improve operational efficiency.

This project includes both a **RESTful API** for production integration and a **CLI-based Simulation** for algorithm verification.

---

## 🚀 Key Features

- **Dynamic Token Management**: Smart allocation of tokens based on doctor availability and slot capacity.
- **Doctor Scheduling**: Flexible creation and management of doctor profiles and their consultation slots.
- **Real-time Availability**: Instant checking of open slots and booking status.
- **REST API**: Full-featured API endpoints for frontend or mobile app integration.
- **Simulation Mode**: Interactive command-line tool to test the allocation logic without a frontend.

---

## 🛠️ Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.1
- **Database**: H2 (In-Memory) for rapid development/testing
- **Build Tool**: Maven
- **Utilities**: Lombok for boilerplate reduction

---

## 📂 Project Structure

```bash
Backend_doc/
├── opd-token-engine/       # Main Spring Boot Application Source
│   ├── src/main/java/      # Java Source Code
│   │   ├── controller/     # REST Controllers (API Endpoints)
│   │   ├── domain/         # Data Models (Doctor, Token, TimeSlot)
│   │   ├── service/        # Business Logic
│   │   └── ...
│   └── pom.xml             # Maven Dependencies
├── run_app.bat             # Script to run the full Spring Boot App
├── run_simulation.bat      # Script to run the standalone CLI Simulation
└── package.json            # Project configuration
```

---

## ⚡ Getting Started

### Prerequisites

- **Java JDK 17** or higher installed.
- **Maven** installed and configured in your system PATH.

### Installation

1.  Clone repository or download the source code.
2.  Navigate to the project root:
    ```cmd
    cd e:\Projects\Backend_doc
    ```

---

## 🖥️ Usage Guide

You can run the project in two modes:

### 1. Full Spring Boot Application 🌐
Run the backend server to expose the REST APIs.

**Method A: Using the Batch Script (Recommended for Windows)**
Double-click `run_app.bat` or run:
```cmd
.\run_app.bat
```

**Method B: Manual**
```cmd
cd opd-token-engine
mvn spring-boot:run
```

*The server will start at `http://localhost:8080`*

### 2. Interactive Simulation 🕹️
Run the lightweight CLI version to test logic logic instantly.

**Method A: Using the Batch Script**
Double-click `run_simulation.bat` or run:
```cmd
.\run_simulation.bat
```

**Method B: Manual**
```cmd
cd opd-token-engine
javac InteractiveOpdEngine.java
java InteractiveOpdEngine
```

---

## 📖 API Reference

Base URL: `http://localhost:8080/api/schedule`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **GET** | `/{doctorId}` | Get all time slots for a specific doctor |
| **POST** | `/doctor` | Register a new doctor |
| **POST** | `/slot` | Create a time slot for a doctor |
| **POST** | `/book` | Book a token for a patient |
| **DELETE** | `/cancel/{tokenId}` | Cancel an existing token |

### Example Request (Book Token)
**POST** `/api/schedule/book`
```json
// Query Params
?patientName=John Doe
&contactNumber=9876543210
&userIdNumber=UID123
&source=MOBILE_APP
&slotId=1
```

---

## 🤝 Contribution

1.  Fork the project.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

*Generated with ❤️ by the OPD Token Engine Team*

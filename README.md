# Library Manager - A Library Management System
![icon-256.png](src/main/resources/icon/icon-256.png)

## Overview
Library Manager is a comprehensive library management system designed to automate the operations of academic and public libraries. The system handles book inventory management, user authentication, borrowing and returning processes, and fine calculations.

## Features
- **User Management**
  - Support for different user types (Students, General Public, Admins)
  - Secure user authentication with encrypted user data
  - User profiles with borrowing history

- **Book Management**
  - Comprehensive book catalog with metadata (title, author, publisher)
  - Intelligent shelf organization system

- **Borrowing System**
  - Different lending periods based on user type (15 days for students, 7 days for general public)
  - Automated due date calculations
  - Book status tracking

- **Fine Management**
  - Automatic calculation of overdue fines
  - Different fine rates for user categories (€0.50/day for students, €1.00/day for general public)
  - Overdue items reporting with accumulated fines

- **Internationalization**
  - Multi-language support (English and Portuguese)
  - Easily switch between languages via the options menu
  - Persistent language settings between sessions

- **Security**
  - AES-256 encryption for sensitive user data
  - Password-protected administration functions

- **Logging & Error Handling**
  - Comprehensive logging system with different log levels
  - Automatic error handling and recovery
  - Graceful shutdown procedures

## Technical Details

### Data Structure
- **Books**: Unique BookID system combining shelf location and book identifier
- **Users**: Encrypted user information with borrowing history
- **Data Persistence**: JSON-based data storage with automatic backup

### Requirements
- Java 22 or higher (bundled with installer)
- GSON library for JSON processing (included in the lib directory)

## Installation

### Option 1: Using the Installer
Download and run the latest installer for your platform from the [releases](https://github.com/TheBanditOfRed/Library-Manager/releases) page

### Option 2: Building from Source (Cross-Platform)
1. Clone the repository:
   ```
   git clone https://github.com/TheBanditOfRed/Library-Manager.git
   ```

2. Build the installer using the provided script:
   ```
   # On Windows (Git Bash), macOS, or Linux:
   ./build-installer.sh
   ```

3. Find the generated installer in the "installers/[your-platform]" directory

### Option 3: Development Setup (Cross-Platform)
1. Clone the repository
2. Open the project in your preferred Java IDE
3. Run the main application class ([LibraryManagementSystem.java](src/main/java/LibraryManagementSystem.java))

## Usage
1. Launch the application
2. The system will automatically detect your language preference or default to English
3. Use the login system to access different user levels (Admin, Student, General Public)
    * See [UserDataUnencrypted.json](src/main/resources/data/UserDataUnencrypted.json) for example user login credentials
4. Navigate through the intuitive GUI to manage books, users, and borrowing operations
5. Access the options menu to change language settings or perform administrative tasks

## Project Status
|       Component       |   Status    | Description                                                                                                                                         | Platform Support |
|:---------------------:|:-----------:|-----------------------------------------------------------------------------------------------------------------------------------------------------|:----------------:|
| **Core Application**  | ✅ Completed | The main library management system with all features implemented including user management, book management, borrowing system, and fine calculation |  Cross-Platform  |
|   **Localisation**    | ✅ Completed | Multi-language support with full translations in English and Portuguese, persistent language settings between sessions                              |  Cross-Platform  |
|  **Data Management**  | ✅ Completed | JSON-based data persistence with encryption for sensitive information                                                                               |  Cross-Platform  |
| **Windows Installer** | ✅ Completed | MSI installer package with bundled JRE, Start Menu integration, and desktop shortcuts                                                               |     Windows      |
|  **macOS Installer**  | ✅ Completed | DMG installer package with macOS-specific icons and application bundle structure                                                                    |      macOS       |
|  **Linux Installer**  | ✅ Completed | DEB package with Linux desktop integration and application shortcuts                                                                                |      Linux       |

## License
See the [LICENSE](LICENSE) file for details about the project license.

## Third-Party Dependencies
This project uses the following third-party library:
- Gson 2.9.0 (Apache License 2.0) - Used for JSON processing

For details of third-party licenses, see the [THIRD_PARTY_LICENSE](THIRD_PARTY_LICENSE) file.
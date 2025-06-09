# Library Manager - A Library Management System
![icon-256.png](src/resources/icon/icon-256.png)

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

### Option 1: Using the Installer (Windows)
1. Download the latest installer from the releases page
2. Run the "Library Manager-1.1.msi" installer
3. Follow the installation wizard instructions
4. Launch the application from the Start Menu or desktop shortcut

### Option 2: Building from Source
1. Clone the repository:
   ```
   git clone https://github.com/TheBanditOfRed/Library-Manager.git
   ```

2. Build the installer using the provided script:
   ```
   build-installer.bat
   ```

3. Find the generated installer in the "installer" directory

### Option 3: Development Setup
1. Clone the repository
2. Open the project in your preferred Java IDE
3. Run the main application class (`main.LibraryManagementSystem`)

## Usage
1. Launch the application
2. The system will automatically detect your language preference or default to English
3. Use the login system to access different user levels (Admin, Student, General Public)
   - See the [Unencrypted User Data](src/main/resources/unencrypted_user_data.txt) file for the example user credentials provided for testing

4. Navigate through the intuitive GUI to manage books, users, and borrowing operations
5. Access the options menu to change language settings or perform administrative tasks

## Project Status
✅ **COMPLETED** - This project is feature-complete and ready for production use.

## License
See the [LICENSE](LICENSE) file for details about the project license.

## Third-Party Dependencies
This project uses the following third-party library:
- Gson 2.9.0 (Apache License 2.0) - Used for JSON processing

For details of third-party licenses, see the [THIRD_PARTY_LICENSE](THIRD_PARTY_LICENSE) file.
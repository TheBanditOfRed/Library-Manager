# THIS PROJECT IS IN ACTIVE DEVELOPMENT
# NOT ALL LISTED FEATURES ARE IMPLEMENTED YET

---

# Library Manager - A Library Management System

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

## Technical Details

### Data Structure
- **Books**: Unique BookID system combining shelf location and book identifier
- **Users**: Encrypted user information with borrowing history

### Requirements
- Java 17 or higher
- GSON library for JSON processing

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/TheBanditOfRed/Library-Manager.git
   ```

2. Open the project in your preferred Java IDE

3. Make sure the GSON library is properly included in your project dependencies

4. Run the main application class (`main.LibraryManagementSystem`)

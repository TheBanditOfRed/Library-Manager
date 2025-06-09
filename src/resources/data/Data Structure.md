# Data Structure

---
## [BookData.json](BookData.json)

```json
{
    "BookID": "B001",
    "Title": "Introduction to Algorithms",
    "Author": "Thomas H. Cormen",
    "Publisher": "MIT Press",
    "Available": 5,
    "OnLoan": 2
}
```
- **BookID**: Unique identifier for each book.
  - Letters refer to the shelf number:
    - A-Z → 1-26
    - AA-ZZ → 27-702
  - Numbers refer to the books unique book id
  - BookID is automatically generated when a book is added to the system.

- **Title**: Title of the book. [Required]
- **Author**: Author of the book. [Optional]
- **Publisher**: Publisher of the book. [Optional]
- **Available**: Number of copies available. [Required]
- **OnLoan**: Number of copies currently on loan.
  - Generated automatically when a book is borrowed.

---
## [UserData.json](UserData.json)

```json
{
    "Students": [
        {
            "UserID": "123456",
            "Name": "John Doe",
            "Password": "password123",
            "Books": [
                {
                    "BookID": "B001",
                    "DateIssued": "2025-05-15",
                    "Status": 1
                }
            ]
        }
    ],
    "General Public": [
        {
            "UserID": "654321",
            "Name": "Jane Doe",
            "Password": "password321",
            "Books": []
        }
    ],
    "Admins": [
        {
            "UserID": "admin",
            "Name": "Admin User",
            "Password": "adminpassword"
        }
    ]
}
```
All sensitive user data is encrypted using AES-256 encryption.

Example of encrypted data:
```json
{
    "UserID": "123456",
    "Name": "aW4Kd83hJnP9dkLzXcVb2Q==",
    "Password": "J2nKl5OpQ7tRvXzY1bC3dF==",
    "Books": [
        {
            "BookID": "B001",
            "DateIssued": "pQ3sT6uV9wY2aD5fH8jK=",
            "Status": 1
        }
    ]
}
```

- **UserID**: Unique identifier for each user.
    - Random 6-digit integer for students and general public.

- **Name**: Name of the user. [Required]
- **Password**: Password for the user. [Required]
  - Used as the key for this user's data encryption.
- **Books**: List of books borrowed by the user.  
  - Generated automatically when a book is borrowed.
  - **BookID**: Unique identifier for each book.
  - **DateIssued**: Date the book was issued.
  - **Status**: Status of the book.
    - 0 = Late
    - 1 = On Loan
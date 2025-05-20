
# 📘 Library Management System Simulation – Project Overview

## General Description

The goal is to simulate the operation of an **automated library management system**, similar to those found in schools or public libraries. The system should interact with users and handle:

- Borrowing and returning books/magazines
- Fines:
  - **Students**: €0.50 per day of delay
  - **General public**: €1.00 per day of delay

The system must allow **stock management**. Stock is a list of items, where each item has a title and author. Items are organized on shelves, each with a number (1, 2, ...). You should be able to add items by specifying the quantity, title, author, and shelf.

### Return deadlines:
- **Students**: 15 days
- **General public**: 7 days

### Additional Features:
- Listing overdue items with fines and total amount accumulated
- Return process with user ID validation and item selection

## ⭐ Bonus Features (Optional)
- Graphical interface (default is text-based)
- Automatic calculation of overdue days

---

## 🖥️ Interaction Examples

### 1. Add Items to Stock

Adding 5 copies of *"Amor de Perdição"* by *Camilo Castelo Branco* to shelf 1.

```text
> Add: 5, Amor de Perdição, Camilo Castelo Branco, 1
```

---

### 2. List Stock Items

```text
> List:
- Amor de Perdição (Camilo Castelo Branco) [Shelf 1]
- Don Quixote (Miguel de Cervantes) [Shelf 2]
```

---

### 3. Borrow Item (Student)

```text
> Enter student number: 12345
> Select item: Amor de Perdição
> Due in 15 days
```

---

### 4. Borrow Item (General Public)

```text
> Enter ID: 67890
> Pay €1
> Select item: Science magazine
> Due in 7 days
```

---

### 5. Return (Student)

```text
> Enter student number: 12345
> Select item: Don Quixote
> Days late: 0
> Return successful
```

---

### 6. Return (General Public)

```text
> Enter ID: 67890
> Select item: Science
> Days late: 3
> Fine: €3.00
```

---

### 7. List Overdue Items

```text
> 1234 - Science (3 days late) - Fine: €1.50
> 1432 - Don Quixote (5 days late) - Fine: €5.00
> Total: €6.50
```

---

## 📄 Report Requirements

- Use of object-oriented programming principles
- Limitations of the program
- (Optional) UML diagram ([https://app.diagrams.net](https://app.diagrams.net))
- Self-assessment (0-100 scale) with justification
- Group projects: contribution breakdown (e.g., "Zé: 30%, Maria: 70%")

---

## 📬 Submission Guidelines

- **Groups**: 1 or 2 students
- **Due date**: June 18, 2024
- Submit via Moodle by the student with the **lowest student number**
- Submit:
  - `.zip` or `.tar.gz` of Java source code in a folder named like: `12345_12346`
  - PDF report

---

## 🎓 Evaluation

| Component       | Weight |
|----------------|--------|
| Code           | 65%    |
| Report         | 30%    |
| Self-evaluation| 5%     |

Final grade = `oral defense × min(20, base grade + bonus features)`

- Bonus Features:
  - main.ui.GUI.main.ui.GUI: +0.5
  - Auto late day calc: +0.5
  - **Total possible bonus: +1.0**

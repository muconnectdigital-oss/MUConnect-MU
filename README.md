# School Chat Application Requirements Document

## 1. Application Overview
**Application Name:** EduConnect
\n**Application Description:** A web-based chat application designed for school/college communication between students, teachers, and administrators.\n
## 2. User Roles & Authentication
\n### 2.1 Student Registration & Login
- **Registration Requirements:**
  - GR Number/Enrollment Number
  - Date of Birth
  - Phone Number\n  - OTP verification required
  - Registration only succeeds if all information matches existing records in Firebase
  - If details don't match, display message: 'Details not found'\n\n- **Login Credentials:**
  - Username: Enrollment Number/GR Number
  - Password: Date of Birth (numbers only, no special characters)
\n- **Forgot Password:**
  - Recovery using GR Number/Enrollment Number + Phone Number
\n### 2.2 Teacher/Professor Registration & Login
- **Registration Requirements:**
  - Employee Code
  - Respective details verification required
  - Similar matching process as students

- **Login Credentials:**
  - Username: Employee Code
  - Password: As per their registered details

- **Forgot Password:**
  - Recovery using Employee Code + registered details

### 2.3 Admin Registration & Login\n- **Registration:**
  - No matching requirement for first-time registration
  - Can register directly using Phone Number, Name, etc.

- **Login:**
  - Standard credentials based on registration

## 3. Core Features

### 3.1 Student Dashboard
- New users see empty dashboard initially
- Groups appear only after being added by Teacher/Professor or Admin
- Display group details and information entered by Teacher/Professor/Admin

### 3.2 Teacher/Professor Features
- **Group Creation:**
  - Select students from existing Firebase records
  - Add student details: GR Number/Enrollment, Date of Birth, Phone, Class, Batch
  - Manage group membership

- **Student Management:**
  - Add student information to Firebase
  - Update student details
\n### 3.3 Admin Features\n- **Group Creation:**
  - Same capabilities as Teacher/Professor
  - Select and organize students into groups

- **User Management:**
  - Add and manage student details
  - Add and manage teacher details

### 3.4 Chat Functionality
- Group-based messaging\n- Real-time communication between group members
\n## 4. Data Storage\n- All user information stored in Firebase
- Student details: GR Number/Enrollment, Date of Birth, Phone Number, Class, Batch
- Teacher details: Employee Code and respective information
- Admin details: Phone Number, Name, and other registration information

## 5. Design Style
- **Color Scheme:** Primary blue (#2196F3) with white background, creating a professional educational atmosphere
- **Layout:** Card-based design with clear separation between dashboard sections and chat groups
- **Visual Details:** Rounded corners (8px radius), subtle shadows for depth, clean sans-serif typography
- **Icons:** Minimalist line icons for navigation and actions, ensuring clarity and ease of use

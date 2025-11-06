# Data Model: User Management System

## Entities

### User

Represents a registered user in the system.

**Attributes**:
- `id` (String): Unique identifier for the user
- `email` (String): User's email address (unique)
- `passwordHash` (String): Hashed password using bcrypt
- `createdAt` (LocalDateTime): Timestamp when the user was created
- `lastLoginAt` (LocalDateTime): Timestamp of the last successful login

**Validation Rules**:
- Email must be in valid format and unique
- Password must meet security requirements (minimum 8 characters, mix of letters, numbers, and special characters)

### UserProfile

Contains additional information about a user.

**Attributes**:
- `userId` (String): Reference to the User entity
- `firstName` (String): User's first name
- `lastName` (String): User's last name
- `updatedAt` (LocalDateTime): Timestamp when the profile was last updated

**Validation Rules**:
- All fields except lastName are required
- Names should not exceed 50 characters

## Relationships

- One-to-One: User to UserProfile (each User has exactly one UserProfile)
- UserProfile is dependent on User (cannot exist without a User)

## State Transitions

### User States
1. **Pending**: User has started registration but not completed (if email verification is implemented)
2. **Active**: User has completed registration and can log in
3. **Locked**: User account is temporarily locked due to security concerns
4. **Disabled**: User account is permanently disabled

For this implementation, we'll focus on Active and Disabled states.

## Data Persistence

### In-Memory Storage
- Users will be stored in a ConcurrentHashMap for thread-safe access
- UserProfile data will be stored in a separate ConcurrentHashMap
- Data will be lost when the application terminates

### File-based Persistence (Optional)
- JSON format for data serialization
- Automatic save on data changes
- Load data on application startup
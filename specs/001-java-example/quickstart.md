# Quick Start Guide: Java User Management System

## Prerequisites
- Java 17 JDK or later
- Maven 3.8+
- Git

## Building the Project
```bash
# Clone the repository
git clone &lt;repository-url&gt;
cd java-user-management

# Build the project
mvn clean install
```

## Running the Application
```bash
# Register a new user
java -jar target/user-management.jar register --email "user@example.com" --password "securePassword123" --first-name "John" --last-name "Doe"

# Authenticate a user
java -jar target/user-management.jar login --email "user@example.com" --password "securePassword123"

# Get user profile
java -jar target/user-management.jar get-profile --user-id &lt;user-id&gt;

# Update user profile
java -jar target/user-management.jar update-profile --user-id &lt;user-id&gt; --first-name "Jane" --last-name "Smith"
```

## Running Tests
```bash
# Run all tests
mvn test

# Run unit tests only
mvn test -Dtest.include="**/*Test.java"

# Run integration tests only
mvn test -Dtest.include="**/*IntegrationTest.java"
```

## Project Structure
```
src/main/java/
├── model/          # Data models (User, UserProfile)
├── service/        # Business logic (UserService, AuthenticationService)
├── cli/            # Command-line interface handlers
└── util/           # Utility classes

src/test/java/
├── contract/       # Contract tests for APIs
├── integration/    # Integration tests
└── unit/           # Unit tests
```

## Configuration
The application uses default in-memory storage. To enable file-based persistence, set the following environment variable:
```bash
export USER_DATA_FILE=/path/to/user-data.json
```

## Example Usage
```bash
# Register a user
java -jar target/user-management.jar register --email "alice@example.com" --password "password123" --first-name "Alice"

# Login
java -jar target/user-management.jar login --email "alice@example.com" --password "password123"

# Get profile (token would be used in a real implementation)
java -jar target/user-management.jar get-profile --user-id "user-123"

# Update profile
java -jar target/user-management.jar update-profile --user-id "user-123" --first-name "Alice" --last-name "Smith"
```
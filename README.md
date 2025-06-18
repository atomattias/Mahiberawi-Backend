# Mahiberawi Backend

A Spring Boot-based backend application for the Mahiberawi platform, providing robust user management and group functionality.

## 🚀 Features

- User Management
  - User registration and authentication
  - Role-based access control (USER, ADMIN roles)
  - User profile management
  - Account status tracking (ACTIVE, INACTIVE, SUSPENDED, DELETED)

- Group Management
  - Create and manage groups
  - Group membership management
  - Group roles and permissions

- Event Management
  - Create and manage events
  - Event participation tracking

- Messaging System
  - Direct messaging between users
  - Message history

- Payment Integration
  - Payment tracking and management
  - Transaction history

- Notification System
  - Real-time notifications
  - Notification preferences

## 🛠️ Technology Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok

## 📋 Prerequisites

- JDK 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher

## 🔧 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/atomattias/Mahiberawi-Backend.git
   cd Mahiberawi-Backend
   ```

2. Configure the database:
   - Create a PostgreSQL database
   - Update `application.properties` with your database credentials

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## 📁 Project Structure

```
src/main/java/com/mahiberawi/
├── entity/           # JPA entities
├── repository/       # Data access layer
├── service/         # Business logic
├── controller/      # REST controllers
├── dto/             # Data transfer objects
├── config/          # Configuration classes
└── security/        # Security related classes
```

## 🔐 Security

The application uses Spring Security for authentication and authorization. Key security features include:

- JWT-based authentication
- Role-based access control
- Password encryption
- Session management

## 📝 API Documentation

The API documentation is available at `/swagger-ui.html` when running the application.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Atom Attias** - *Initial work* - [atomattias](https://github.com/atomattias)

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- All contributors who have helped shape this project 
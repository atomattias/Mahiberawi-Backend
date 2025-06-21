# Mahiberawi Backend

A Spring Boot-based backend application for the Mahiberawi platform, providing robust user management and group functionality.

## 🚀 Features

- User Management
  - User registration and authentication
  - Role-based access control (MEMBER, ADMIN, SUPER_ADMIN roles)
  - User profile management with intentions (JOIN_ONLY, CREATE_GROUPS, BOTH, UNDECIDED)
  - Account status tracking (ACTIVE, INACTIVE, SUSPENDED, DELETED)
  - Super admin user management capabilities

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

## 🚀 Deployment (Pre-Production)

### Option 1: Heroku Deployment

1. **Install Heroku CLI** and login:
   ```bash
   heroku login
   ```

2. **Create a new Heroku app**:
   ```bash
   heroku create mahiberawi-backend-prod
   ```

3. **Add PostgreSQL addon**:
   ```bash
   heroku addons:create heroku-postgresql:mini
   ```

4. **Set environment variables**:
   ```bash
   heroku config:set SPRING_PROFILES_ACTIVE=prod
   heroku config:set JWT_SECRET=your-super-secure-jwt-secret-key-here
   heroku config:set JWT_REFRESH_SECRET=your-super-secure-refresh-secret-key-here
   heroku config:set MAIL_USERNAME=your-email@gmail.com
   heroku config:set MAIL_PASSWORD=your-app-password
   ```

5. **Deploy**:
   ```bash
   git push heroku main
   ```

### Option 2: Railway Deployment

1. **Connect your GitHub repository** to Railway
2. **Add PostgreSQL service** in Railway dashboard
3. **Set environment variables** in Railway dashboard
4. **Deploy automatically** on git push

### Option 3: Docker Deployment

1. **Build the Docker image**:
   ```bash
   docker build -t mahiberawi-backend .
   ```

2. **Run with environment variables**:
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DATABASE_URL=your-database-url \
     -e JWT_SECRET=your-jwt-secret \
     -e MAIL_USERNAME=your-email \
     -e MAIL_PASSWORD=your-password \
     mahiberawi-backend
   ```

### Environment Variables Required

For production deployment, set these environment variables:

```bash
# Database
DATABASE_URL=your-postgresql-connection-string
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# JWT
JWT_SECRET=your-super-secure-jwt-secret-key
JWT_REFRESH_SECRET=your-super-secure-refresh-secret-key

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587

# Server
PORT=8080
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
- Role-based access control (MEMBER, ADMIN, SUPER_ADMIN)
- Password encryption
- Session management
- Super admin user management with audit logging

## 📝 API Documentation

The API documentation is available at `/swagger-ui.html` when running the application in development mode.

### Key API Endpoints

#### User Management (Super Admin Only)
- `GET /api/users` - Get all users
- `GET /api/users/search?q={query}` - Search users
- `GET /api/users?role={role}` - Filter users by role
- `PATCH /api/users/{userId}/role` - Update user role
- `DELETE /api/users/{userId}` - Delete user

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification

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
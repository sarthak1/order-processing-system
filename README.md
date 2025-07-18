# Order Processing System

A Spring Boot-based order processing system with RESTful APIs for managing orders, built with Java 21 and Spring Boot 3.2.2.

## Features

- Create, retrieve, update, and cancel orders
- Filter orders by status (PENDING, PROCESSING, COMPLETED, CANCELLED)
- Swagger/OpenAPI documentation
- H2 in-memory database with web console
- Comprehensive logging

## Prerequisites

- Java 21 or later
- Maven 3.6.3 or later
- Git

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/sarthak1/order-processing-system.git
cd order-processing-system
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Configure Environment

Create a copy of `application.properties` as `application-local.properties` for local development:

```bash
cp src/main/resources/application.properties src/main/resources/application-local.properties
```

## Running the Application

### Development Mode with Hot Reload

1. First, ensure you have Spring Boot DevTools in your `pom.xml` (already included in this project).

2. Run the application with:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   ```

3. The application will start on `http://localhost:8082`

4. **Hot Reloading**:
   - Make changes to your code
   - Save the file (Ctrl+S / Cmd+S)
   - The application will automatically restart with your changes
   - For static resources (templates, static files), changes are applied immediately without restart

### Common Development Commands

#### Start a fresh instance
```bash
# Stop any running instances first
pkill -f "java.*order-processing" || true

# Clear target directory and rebuild
mvn clean install

# Start the application
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

#### Stop the application
```bash
# Graceful shutdown (sends SIGTERM)
pkill -f "java.*order-processing"

# Force stop (use if above doesn't work)
pkill -9 -f "java.*order-processing"
```

#### Free up port if it's in use
```bash
# Find and kill process using port 8082 (Linux/Mac)
lsof -i :8082 | grep LISTEN | awk '{print $2}' | xargs -r kill -9

# Windows equivalent
# netstat -ano | findstr :8082
# taskkill /PID <PID> /F
```

#### Check if the application is running
```bash
ps aux | grep "[o]rder-processing"
```

### Production Mode

```bash
java -jar target/order-processing-system-1.0.0.jar
```

## Development Tools

### Spring Boot DevTools
This project includes Spring Boot DevTools which provides:
- Automatic application restart when files change
- Live reload for static resources
- Development-time configuration properties
- Remote debug support

#### Configuration
Development-time properties can be set in `src/main/resources/application-local.properties`

#### Disabling Live Reload
Set the following in `application-local.properties`:
```properties
spring.devtools.livereload.enabled=false
```

## Accessing the Application

- **API Documentation (Swagger UI)**: http://localhost:8082/swagger-ui.html
- **H2 Database Console**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:order-processing`
  - Username: `sa`
  - Password: (leave empty)
- **API Docs (JSON)**: http://localhost:8082/api-docs

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/peerislands/orderprocessing/
│   │       ├── config/       # Configuration classes
│   │       ├── controller/   # REST controllers
│   │       ├── model/        # Entity classes
│   │       ├── repository/   # Data repositories
│   │       ├── service/      # Business logic
│   │       └── OrderProcessingApplication.java
│   └── resources/
│       └── application.properties
└── test/                     # Test files
```

## API Endpoints

### Orders

- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create a new order
- `DELETE /api/orders/{id}` - Cancel an order
- `GET /api/orders/status/{status}` - Get orders by status

## Development Workflow

1. Create a new branch for your feature/bugfix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit them:
   ```bash
   git add .
   git commit -m "Your commit message"
   ```

3. Push your changes to the remote:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Create a Pull Request on GitHub

## Logging

Logs are written to `logs/order-processing.log` in the project root directory.

To view logs in real-time:
```bash
tail -f logs/order-processing.log
```

## Deployment

### Prerequisites
- Docker and Docker Compose installed
- Configured production database (if not using H2 in production)

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t order-processing-system .
   ```

2. Run the container:
   ```bash
   docker run -p 8082:8082 order-processing-system
   ```

### Environment Variables

For production, set these environment variables:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/orderdb
export SPRING_DATASOURCE_USERNAME=dbuser
export SPRING_DATASOURCE_PASSWORD=dbpassword
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please open an issue on the [GitHub repository](https://github.com/sarthak1/order-processing-system/issues).

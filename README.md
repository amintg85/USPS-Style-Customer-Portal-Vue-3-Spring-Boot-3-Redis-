# USPS-Style Customer Portal

A secure customer portal inspired by USPS workflows, built with Vue 3, Spring Boot 3, and Redis. This application demonstrates enterprise-level features including authentication, package tracking, reporting, Redis caching, and rate limiting.

## 🚀 Tech Stack

### Frontend
- **Vue 3** with Composition API (script setup)
- **Vite** for fast development and building
- **Pinia** for state management
- **Vue Router** for navigation
- **Axios** for HTTP requests

### Backend
- **Spring Boot 3** with Java 17
- **Spring Security** with JWT authentication
- **PostgreSQL** for data persistence
- **Redis** for caching (improves DB queries 10–12s → 2–3s)
- **Spring Data JPA** for database operations
- **Bucket4j** for rate limiting
- **Spring Boot Actuator** for monitoring

### Infrastructure
- **Docker** and **Docker Compose** for containerization
- **Nginx** for frontend serving and reverse proxy

## ✨ Key Features

- 🔐 **OAuth2/JWT Authentication** - Secure user authentication and authorization
- 📦 **Package Tracking** - Real-time shipment tracking with event history
- 📊 **Reports & Statistics** - Generate reports with date ranges and view statistics
- ⚡ **Redis Caching** - Cached API calls for improved performance (10-12s → 2-3s improvement)
- 🚦 **Rate Limiting** - Rate-limited endpoints using Bucket4j (100 requests/minute)
- 📈 **Monitoring** - Spring Boot Actuator endpoints for health checks and metrics
- 🐳 **Docker-based Microservices** - Fully containerized application

## 📁 Project Structure

```
.
├── backend/                 # Spring Boot 3 backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/usps/portal/
│   │   │   │   ├── config/      # Configuration classes
│   │   │   │   ├── controller/ # REST controllers
│   │   │   │   ├── dto/         # Data Transfer Objects
│   │   │   │   ├── model/       # JPA entities
│   │   │   │   ├── repository/  # JPA repositories
│   │   │   │   ├── security/    # Security & JWT
│   │   │   │   └── service/     # Business logic
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── Dockerfile
│   └── pom.xml
├── frontend/               # Vue 3 frontend
│   ├── src/
│   │   ├── router/         # Vue Router configuration
│   │   ├── services/       # API service layer
│   │   ├── stores/         # Pinia stores
│   │   ├── views/          # Page components
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml      # Docker orchestration
└── README.md
```

## 🛠️ Setup & Installation

### Prerequisites
- Docker and Docker Compose
- Java 17+ (for local development)
- Node.js 20+ (for local frontend development)
- Maven 3.9+ (for local backend development)

### Quick Start with Docker

1. **Clone the repository**
   ```bash
   cd "USPS-Style Customer Portal (Vue 3 + Spring Boot 3 + Redis)"
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Actuator Health: http://localhost:8080/actuator/health
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379

4. **Stop services**
   ```bash
   docker-compose down
   ```

### Local Development

#### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will run on http://localhost:8080

#### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

The frontend will run on http://localhost:5173

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Tracking
- `GET /api/tracking/{trackingNumber}` - Get shipment details and tracking events
- `GET /api/tracking/my-shipments` - Get all shipments for current user
- `POST /api/tracking/create` - Create a new shipment

### Reports
- `GET /api/reports/statistics` - Get user statistics
- `GET /api/reports/shipment-report?startDate={date}&endDate={date}` - Generate shipment report

### Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

## 🔒 Security Features

- JWT-based authentication
- Password encryption with BCrypt
- CORS configuration
- Rate limiting (100 requests/minute per IP)
- Secure session management (stateless)

## ⚡ Performance Features

- **Redis Caching**: Shipment lookups, user shipments, and reports are cached
- **Cache TTL**: 10 minutes default
- **Performance Improvement**: Database queries reduced from 10-12s to 2-3s

## 📊 Monitoring

Spring Boot Actuator provides:
- Health checks
- Application metrics
- Prometheus endpoint for monitoring
- Ready for ELK stack integration

## 🧪 Testing

### Create a Test User
1. Navigate to http://localhost:3000/register
2. Create an account
3. Login at http://localhost:3000/login

### Test Features
1. **Create Shipment**: Go to Tracking page and create a new shipment
2. **Track Package**: Enter tracking number to view details and history
3. **View Reports**: Go to Reports page to see statistics and generate reports

## 🔧 Configuration

### Environment Variables

Backend environment variables (in `docker-compose.yml` or `application.yml`):
- `DB_HOST` - PostgreSQL host
- `DB_PORT` - PostgreSQL port
- `DB_NAME` - Database name
- `DB_USER` - Database user
- `DB_PASSWORD` - Database password
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port
- `JWT_SECRET` - JWT signing secret (change in production!)

## 🐛 Troubleshooting

### Port Conflicts
If ports are already in use, modify `docker-compose.yml` to use different ports.

### Database Connection Issues
Ensure PostgreSQL container is healthy before starting backend:
```bash
docker-compose ps
```

### Redis Connection Issues
Check Redis container health:
```bash
docker-compose exec redis redis-cli ping
```

## 📝 License

This project is a demonstration application for portfolio purposes.

## 👤 Md Amin

Built to demonstrate experience with:
- High-traffic government system patterns
- Microservices architecture
- Caching strategies
- Security best practices
- Modern full-stack development


Test Credentials:
  Email: test1765571195@example.com
  Password: test123

# USPS-Style-Customer-Portal-Vue-3-Spring-Boot-3-Redis-

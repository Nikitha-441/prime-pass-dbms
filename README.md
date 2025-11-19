# PrimePass - Event Booking System

A complete event booking system with frontend, backend, and database components.

## Project Structure

```
basic/
├── database/           # SQL files
│   ├── schema.sql      # Database schema and sample data
│   ├── views.sql       # Role-based views
│   ├── queries.sql     # Complex queries
│   ├── transactions.sql # Transaction management
│   ├── concurrency.sql # Concurrency control
│   └── authentication.sql # Authentication functions
├── frontend/          # HTML/CSS/JS frontend
│   ├── *.html         # Frontend pages
│   ├── js/            # JavaScript modules
│   └── style.css      # Styling
└── backend/           # Java Spring Boot backend
    ├── pom.xml        # Maven dependencies
    └── src/
        └── main/
            ├── java/  # Java source code
            └── resources/
                └── application.properties
```

## Features

### Database Features
- ✅ ER with 8-10 entities (Users, Organizer, Category, Venue, Event, Showtime, Seat, Seat_Status, Booking, Booked_Seats, Payment)
- ✅ Normalization up to 3NF
- ✅ Relationship types (1:1, 1:M, M:N, M:1)
- ✅ Appropriate data types and constraints
- ✅ Complex queries with joins, nested queries, and aggregates
- ✅ Role-based views (user and admin)
- ✅ Transaction management with ACID properties
- ✅ Concurrency control mechanisms
- ✅ Basic security (authentication, role-based access)

### User Functions
- Browse Events
- Event Details View
- Showtime Selection
- Seat Selection
- Booking & Mock Payment
- Ticket Generation (alphanumeric booking code)
- Booking History
- User Account Management (Login, Signup, Logout)

### Admin Functions
- Admin Login
- Event Management (Add, Update, Delete)
- Showtime Management
- Seat Management (Block/Unblock)
- Booking Management (View all bookings)
- Admin Panel

## Setup Instructions

### Prerequisites
1. **Java 17+** - Download from [Oracle](https://www.oracle.com/java/) or [OpenJDK](https://openjdk.org/)
2. **Maven 3.6+** - Download from [Apache Maven](https://maven.apache.org/download.cgi)
3. **PostgreSQL 12+** - Download from [PostgreSQL](https://www.postgresql.org/download/)
4. **Node.js** (optional, for serving frontend) - Download from [Node.js](https://nodejs.org/)

### Database Setup

1. **Install PostgreSQL** and start the service

2. **Create Database**:
   ```sql
   CREATE DATABASE primepass;
   ```

3. **Run SQL Files** (in order):
   ```bash
   # Connect to PostgreSQL
   psql -U postgres -d primepass
   
   # Run schema
   \i database/schema.sql
   
   # Run views
   \i database/views.sql
   
   # Run queries (optional, for reference)
   \i database/queries.sql
   
   # Run transactions
   \i database/transactions.sql
   
   # Run concurrency
   \i database/concurrency.sql
   
   # Run authentication
   \i database/authentication.sql
   ```

   Or use pgAdmin GUI to run these files.

4. **Update Database Credentials** in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/primepass
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_PASSWORD
   ```

### Backend Setup

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

   Or if you have an IDE (IntelliJ IDEA, Eclipse):
   - Import as Maven project
   - Run `PrimePassApplication.java`

4. **Verify backend is running**:
   - Open browser: http://localhost:5000
   - You should see a Whitelabel Error Page (this is normal, means server is running)

### Frontend Setup

1. **Option 1: Simple HTTP Server (Python)**:
   ```bash
   cd frontend
   python -m http.server 8000
   ```
   Then open: http://localhost:8000

2. **Option 2: Node.js HTTP Server**:
   ```bash
   cd frontend
   npx http-server -p 8000
   ```

3. **Option 3: VS Code Live Server**:
   - Install "Live Server" extension
   - Right-click on `index.html` → "Open with Live Server"

4. **Update API URL** (if needed):
   - Edit `frontend/js/app.js`
   - Change `const API = 'http://localhost:5000';` if your backend runs on different port

## API Endpoints

### Authentication
- `POST /auth/login` - User/Admin login
- `POST /auth/signup` - User registration

### Events (Public)
- `GET /events` - Get all events
- `GET /events/{eventId}` - Get event details with showtimes

### Seats (Public)
- `GET /seats?showtime_id={id}` - Get seats for a showtime

### Bookings (Authenticated)
- `POST /bookings` - Create booking (requires login)
- `GET /bookings/history` - Get user's booking history

### Admin Endpoints (Admin only)
- `POST /events` - Create event
- `PUT /events/{eventId}` - Update event
- `DELETE /events/{eventId}` - Delete event
- `POST /events/{eventId}/showtimes` - Create showtime
- `POST /seats/block` - Block seat
- `POST /seats/unblock` - Unblock seat
- `GET /bookings/all` - Get all bookings

## Default Users

From `schema.sql`:
- **User**: `rishitha@primepass.com` / password: `$2b$10$u1`
- **Admin**: `arjun@primepass.com` / password: `$2b$10$u2`

**Note**: In production, use proper password hashing (bcrypt). Current implementation uses plain text for simplicity.

## Testing the Application

1. **Start Backend**: `cd backend && mvn spring-boot:run`
2. **Start Frontend**: Serve `frontend/` folder on port 8000
3. **Open Browser**: http://localhost:8000
4. **Test Flow**:
   - Browse events
   - View event details
   - Select showtime
   - Select seats
   - Login/Signup
   - Complete booking
   - View booking history

## Troubleshooting

### Backend won't start
- Check Java version: `java -version` (should be 17+)
- Check Maven: `mvn -version`
- Check database connection in `application.properties`
- Check if PostgreSQL is running

### Database connection errors
- Verify PostgreSQL is running
- Check database name, username, password in `application.properties`
- Ensure database `primepass` exists

### CORS errors
- Backend CORS is configured to allow all origins
- If issues persist, check `CorsConfig.java`

### Frontend can't connect to backend
- Verify backend is running on port 5000
- Check browser console for errors
- Verify API URL in `frontend/js/app.js`

## Project Architecture

### Backend (Java Spring Boot)
- **Simple and Beginner-Friendly**: Uses basic Spring Boot with JDBC (no complex frameworks)
- **Layered Architecture**:
  - `Controller` - REST API endpoints
  - `Service` - Business logic
  - `DAO` - Database access
  - `Model` - Data models
  - `Util` - Utilities (JWT, Auth)

### Database (PostgreSQL)
- **Normalized Schema**: Up to 3NF
- **Views**: Role-based data access
- **Transactions**: ACID-compliant booking process
- **Concurrency**: Row-level locking for seat booking

### Frontend (Vanilla JavaScript)
- **No Framework**: Pure HTML/CSS/JS for simplicity
- **ES6 Modules**: Modern JavaScript
- **REST API Integration**: Fetch API for backend communication

## Future Enhancements
- QR code generation for tickets
- Print/Download ticket functionality
- Ticket cancellation
- Email notifications
- Password hashing (bcrypt)
- Payment gateway integration
- Advanced search and filters

## License
This is an educational project.

## Support
For issues or questions, check the code comments or database schema documentation.


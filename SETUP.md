# Quick Setup Guide

## Step 1: Database Setup

1. **Install PostgreSQL** (if not installed)
2. **Create database**:
   ```sql
   CREATE DATABASE primepass;
   ```

3. **Run SQL files** (in pgAdmin or psql):
   - `database/schema.sql` - Creates tables and sample data
   - `database/views.sql` - Creates views
   - `database/transactions.sql` - Creates transaction functions
   - `database/concurrency.sql` - Creates concurrency functions
   - `database/authentication.sql` - Creates auth functions

4. **Update database credentials** in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_PASSWORD
   ```

## Step 2: Backend Setup

1. **Install Java 17+** and **Maven 3.6+**

2. **Navigate to backend folder**:
   ```bash
   cd backend
   ```

3. **Build and run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

   Backend will run on: http://localhost:5000

## Step 3: Frontend Setup

1. **Navigate to frontend folder**:
   ```bash
   cd frontend
   ```

2. **Start a simple HTTP server**:
   ```bash
   # Python 3
   python -m http.server 8000
   
   # Or Node.js
   npx http-server -p 8000
   ```

3. **Open browser**: http://localhost:8000

## Step 4: Test the Application

1. **Browse events** - Should see sample events
2. **View event details** - Click on an event
3. **Select showtime** - Choose a date/time
4. **Select seats** - Pick available seats
5. **Login/Signup** - Create account or login
6. **Complete booking** - Mock payment
7. **View tickets** - See booking history

## Default Login Credentials

- **User**: `rishitha@primepass.com` / `$2b$10$u1`
- **Admin**: `arjun@primepass.com` / `$2b$10$u2`

**Note**: These are sample passwords. In production, use proper password hashing.

## Troubleshooting

- **Backend won't start**: Check Java version, Maven, and database connection
- **Database errors**: Verify PostgreSQL is running and credentials are correct
- **CORS errors**: Backend CORS is configured, check browser console
- **Frontend can't connect**: Verify backend is running on port 5000

## Project Structure

```
basic/
├── database/          # SQL files
├── frontend/          # HTML/CSS/JS
└── backend/           # Java Spring Boot
    └── src/main/
        ├── java/      # Source code
        └── resources/ # Configuration
```

For detailed documentation, see `README.md`.


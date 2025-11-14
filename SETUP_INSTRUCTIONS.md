# Setup Instructions for Hotel Booking Service

## Prerequisites

Before running this project, ensure you have the following installed:

1. **Java Development Kit (JDK) 11 or higher**
   - Download from: https://adoptium.net/
   - Verify installation: `java -version`

2. **Apache Maven 3.8 or higher**
   - Download from: https://maven.apache.org/download.cgi
   - Add Maven to your system PATH
   - Verify installation: `mvn -version`

## Installation Steps

### 1. Install Maven (if not already installed)

**For Windows:**
1. Download Maven binary zip from https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add Maven bin directory to PATH:
   - Open System Properties → Environment Variables
   - Edit PATH variable
   - Add: `C:\Program Files\Apache\maven\bin`
4. Verify: Open new terminal and run `mvn -version`

### 2. Build the Project

```bash
cd d:\shantanu\hotel-booking-service
mvn clean package
```

This will:
- Download all dependencies
- Compile the source code
- Run all tests
- Create the executable JAR

### 3. Run the Application

**Option A: Development Mode (Recommended for testing)**
```bash
mvn quarkus:dev
```
or simply double-click `run-dev.bat`

**Option B: Production Mode**
```bash
java -jar target\quarkus-app\quarkus-run.jar
```

### 4. Access the Application

Once running, access:
- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/q/swagger-ui/
- **OpenAPI Spec**: http://localhost:8080/q/openapi

## Running Tests

Execute all REST Assured tests:
```bash
mvn test
```
or double-click `run-tests.bat`

## Sample API Calls

### Create a Customer
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "phoneNumber": "07123456789"
  }'
```

### Create a Hotel
```bash
curl -X POST http://localhost:8080/api/hotels \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Hotel",
    "phoneNumber": "01234567890",
    "postcode": "NE11AB"
  }'
```

### Create a Booking
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {"id": 1},
    "hotel": {"id": 1},
    "bookingDate": "2025-12-31"
  }'
```

### Get All Customers
```bash
curl http://localhost:8080/api/customers
```

## Troubleshooting

### Issue: Port 8080 already in use
**Solution**: Change port in `src/main/resources/application.properties`:
```properties
quarkus.http.port=8081
```

### Issue: Tests failing
**Solution**: 
1. Ensure no other instance is running
2. Check test output for specific errors
3. Verify H2 database permissions

### Issue: Maven build fails
**Solution**:
1. Delete `.m2\repository` folder in your home directory
2. Run `mvn clean install -U` to refresh dependencies

## Project Structure

The project follows standard Maven directory structure:
- `src/main/java` - Application source code
- `src/main/resources` - Configuration files
- `src/test/java` - Test source code
- `target/` - Build output (created by Maven)

## Database

The application uses an in-memory H2 database:
- **Type**: In-memory (data lost on restart)
- **Console**: Not exposed by default
- **Initial Data**: Loaded from `src/main/resources/import.sql`

Sample data includes:
- 3 customers (including Shantanu Raj Chaudhary)
- 3 hotels

## Configuration for Part 3

To use external Taxi and Flight services, update `application.properties`:

```properties
taxi-api/mp-rest/url=http://colleague-taxi-service-url/api
flight-api/mp-rest/url=http://colleague-flight-service-url/api
```

Replace with actual URLs provided by colleagues.

## Deployment to OpenShift

(Follow instructions from the tutorial for OpenShift deployment)

1. Ensure you have OpenShift CLI installed
2. Login to OpenShift: `oc login`
3. Create new application: `oc new-app . --name=hotel-service`
4. Start build: `oc start-build hotel-service`
5. Get route: `oc get routes`

## IDE Setup

### VS Code
1. Install "Extension Pack for Java"
2. Install "Quarkus" extension
3. Open project folder
4. Maven will auto-import dependencies

### IntelliJ IDEA
1. Open as Maven project
2. Wait for dependency download
3. Enable annotation processing
4. Run Application.java

### Eclipse
1. Import as "Existing Maven Project"
2. Wait for dependency resolution
3. Run as Java Application

## Next Steps

1. Review `README.md` for API documentation
2. Read `REPORT.md` for coursework insights
3. Check `PROJECT_STRUCTURE.md` for file layout
4. Explore Swagger UI for interactive API testing
5. Run tests to verify all functionality

## Support

For issues:
1. Check Maven and Java versions
2. Verify internet connection for dependency downloads
3. Review error logs in console output
4. Check `TROUBLESHOOTING.md` if available

## License

This is a coursework project for Newcastle University CSC8104 module.

---

**Author**: Shantanu Raj Chaudhary  
**Date**: November 2025  
**Module**: Enterprise Software and Services

# KiriEngine Application - Running Instructions

## ✅ Compilation Issues Fixed
All major compilation errors have been resolved:
- Missing DTO classes created
- Package declarations fixed
- Import statements corrected
- Service classes implemented

## 🚀 How to Run the Application

### Option 1: IntelliJ IDEA (Recommended)
1. Open the project in IntelliJ IDEA
2. Wait for Maven to download dependencies
3. Navigate to `src/main/java/com/disrupton/KiriEngineApplication.java`
4. Right-click and select "Run 'KiriEngineApplication'"
5. Or click the green play button next to the main method

### Option 2: IntelliJ Settings Check
If you encounter Java version issues:
1. Go to **File → Project Structure**
2. Set **Project SDK** to Java 17
3. Set **Language Level** to 17
4. Go to **Settings → Build → Compiler → Java Compiler**
5. Set **Project bytecode version** to 17

### Option 3: Maven Command Line (if available)
```bash
mvn clean spring-boot:run
```

## 🔧 Troubleshooting Java Version Issues

The error `javac 24 was used to compile java sources` indicates a version mismatch.

### Fix in IntelliJ:
1. **File → Settings → Build, Execution, Deployment → Build Tools → Maven → Importing**
   - Set JDK for importer to Java 17
2. **File → Settings → Build, Execution, Deployment → Compiler → Java Compiler**
   - Set Project bytecode version to 17
3. **File → Project Structure → Project**
   - Set Project SDK to Java 17
   - Set Project language level to 17

### Alternative Java Versions:
If you only have Java 24 available, you can update the project to use it:
- Change `<java.version>24</java.version>` in pom.xml
- Update all Java version references to 24

## 📋 Application Features
- Cultural object management
- AR photo handling
- User analytics
- Campus zone tracking
- Avatar interactions
- Geolocation services

## 🌐 API Endpoints
Once running, the application will be available at:
- Base URL: `http://localhost:8080`
- API Documentation: Check controllers for endpoint mappings

## 📝 Configuration
- Main config: `src/main/resources/application.yml`
- Firebase config: `src/main/java/com/disrupton/config/FirebaseConfig.java`

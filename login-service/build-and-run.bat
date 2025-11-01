@echo off
echo ========================================
echo Credexa - Login Service Builder
echo ========================================
echo.

echo Checking Java version...
java -version
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)
echo.

echo Checking Maven...
call mvn -version
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)
echo.

echo ========================================
echo Building Credexa Parent Project...
echo ========================================
cd ..
call mvn clean install -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)
echo.

echo ========================================
echo Build Successful!
echo ========================================
echo.
echo Starting Login Service...
echo Service will be available at:
echo   - Application: http://localhost:8081/api/auth
echo   - Swagger UI: http://localhost:8081/api/auth/swagger-ui.html
echo.
echo Press Ctrl+C to stop the service
echo ========================================
echo.

cd login-service
call mvn spring-boot:run

pause

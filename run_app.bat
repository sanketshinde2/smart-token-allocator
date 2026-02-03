@echo off
echo Attempting to run Spring Boot OPD Application...
cd opd-token-engine
call mvn spring-boot:run
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Maven build failed. This is likely due to the system configuration issue.
    echo Please ensure Maven is installed correctly or run 'run_simulation.bat' for the standalone verification.
)
pause

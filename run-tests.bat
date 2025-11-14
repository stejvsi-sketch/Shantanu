@echo off
echo Running all REST Assured tests...
echo.
call mvn clean test
echo.
echo Tests completed. Check the output above for results.
pause

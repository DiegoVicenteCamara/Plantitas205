@ECHO OFF
SETLOCAL
SET "BASE_DIR=%~dp0"
SET "MAVEN_DIR=%BASE_DIR%\.mvn\apache-maven-3.9.12"
SET "MAVEN_ZIP=%BASE_DIR%\.mvn\apache-maven-3.9.12-bin.zip"

IF NOT EXIST "%MAVEN_DIR%\bin\mvn.cmd" (
  IF NOT EXIST "%MAVEN_ZIP%" (
    ECHO Maven zip not found: %MAVEN_ZIP%
    EXIT /B 1
  )
  POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%BASE_DIR%\.mvn' -Force"
)

"%MAVEN_DIR%\bin\mvn.cmd" %*

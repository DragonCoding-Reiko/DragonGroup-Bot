@echo off

set KEYSTORE_NAME=groupbot-store.p12

IF EXIST %KEYSTORE_NAME% (
    del %KEYSTORE_NAME%
)

set /p "KEYSTORE_PASS=Enter keystore password: "

echo Enter the discord token:
C:\Users\pospi\.jdks\openjdk-19.0.1\bin\keytool -importpass -storetype PKCS12 -keystore %KEYSTORE_NAME% -storepass %KEYSTORE_PASS% -alias discord-token
@echo .

echo Enter the database password:
C:\Users\pospi\.jdks\openjdk-19.0.1\bin\keytool -importpass -storetype PKCS12 -keystore %KEYSTORE_NAME% -storepass %KEYSTORE_PASS% -alias db-password
@echo .
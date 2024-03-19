@echo off

set KEYSTORE_NAME=groupbot-store.p12

IF EXIST %KEYSTORE_NAME% (
    del %KEYSTORE_NAME%
)

set /p "KEYSTORE_PASS=Enter keystore password: "

echo Enter the discord token:
keytool -importpass -storetype PKCS12 -keystore %KEYSTORE_NAME% -storepass %KEYSTORE_PASS% -alias discord-token
@echo .

echo Enter the database password:
keytool -importpass -storetype PKCS12 -keystore %KEYSTORE_NAME% -storepass %KEYSTORE_PASS% -alias db-password
@echo .
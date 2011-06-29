@echo off
java -jar @bootstrapJarName@ %*
REM java -cp .;@classesDirName@;@bootstrapJarName@;@windowsLibClasspath@ -jar @bootstrapJarName@ %*
REM exit /b %errorlevel%

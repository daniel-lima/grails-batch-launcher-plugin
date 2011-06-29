@echo off
java -cp .;@classesDirName@;@bootstrapJarName@;@windowsLibClasspath@ -jar @bootstrapJarName@ %*
exit /b %errorlevel%

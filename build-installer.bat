@echo off
echo Building Library Management System Installer...

:: Clean previous builds
echo Cleaning previous builds...
if exist "dist" rmdir /s /q "dist"
if exist "build" rmdir /s /q "build"
if exist "installer" rmdir /s /q "installer"

:: Create necessary directories
mkdir dist
mkdir build
mkdir installer

:: Find Java files
echo Finding Java source files...
dir /s /b src\*.java > sources.txt

:: Compile Java files
echo Compiling Java files...
javac -cp "lib\*" -d build @sources.txt

:: Create JAR
echo Creating JAR file...
jar --create --file dist\LibraryManager.jar --main-class LibraryManagementSystem -C build . -C src resources

:: Copy dependencies
echo Copying dependencies...
if exist "lib" copy lib\*.jar dist\

:: Create installer with custom resources
echo Creating installer with bundled Java runtime...
jpackage --type msi --input dist --dest installer --name "Library Manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version 1.1 --description "Library Management System" --copyright "Copyright 2025" --win-dir-chooser --win-menu --win-menu-group "Library Manager" --win-shortcut --win-shortcut-prompt --icon src\resources\icon\icon.ico

echo Build completed!
echo Installer created: installer\Library Manager-1.1.msi

:: List files in installer-output
echo Installer output directory contents:
dir installer
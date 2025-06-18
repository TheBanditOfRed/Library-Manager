#!/bin/bash

echo "Building Library Management System Installer..."

APP_VERSION="1.2.1"

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    OS_TYPE="windows"
    INSTALLER_TYPE="msi"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="macos"
    INSTALLER_TYPE="dmg"
else
    OS_TYPE="linux"
    INSTALLER_TYPE="deb"
fi

echo "Detected OS: $OS_TYPE (Building $INSTALLER_TYPE package)"

# Clean previous build artifacts
echo "Cleaning build directories..."
rm -rf dist build

# Set up new structure
echo "Setting up directory structure..."
mkdir -p dist
mkdir -p build
mkdir -p installers/$OS_TYPE

echo "Finding Java source files..."
find src/main/java -name "*.java" > sources.txt

echo "Compiling Java files..."
javac -cp "lib/*" -d build @sources.txt

echo "Copying resources..."
cp -r src/main/resources/* build/ 2>/dev/null || true

echo "Creating JAR file..."
jar --create --file dist/LibraryManager.jar --main-class LibraryManagementSystem -C build .

echo "Copying dependencies..."
if [ -d "lib" ]; then
    cp lib/*.jar dist/
fi

echo "Creating $OS_TYPE installer with bundled Java runtime..."
if [[ "$OS_TYPE" == "windows" ]]; then

    jpackage --type msi --input dist --dest installers/windows --name "library-manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --win-dir-chooser --win-menu --win-menu-group "Library Manager" --win-shortcut --win-shortcut-prompt --icon src/main/resources/icon/icon.ico

elif [[ "$OS_TYPE" == "macos" ]]; then

    jpackage --type dmg --input dist --dest installers/macos --name "library-manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --mac-package-name "Library Manager" --icon src/main/resources/icon/icon.icns

else

    jpackage --type deb --input dist --dest installers/linux --name "library-manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --linux-menu-group "Office" --linux-shortcut --icon src/main/resources/icon/icon.png

fi

echo "Build completed!"
echo "Installer created in: installers/$OS_TYPE/"

echo "Cleaning up temporary files..."
rm -rf dist build
rm -f sources.txt

echo "Installer output directory contents:"
ls -la installers/$OS_TYPE/
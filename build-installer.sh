#!/bin/bash

echo "Building Library Management System Installer..."

APP_VERSION="1.1"

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    OS_TYPE="windows"
    INSTALLER_TYPE="msi"
    echo "Detected OS: $OS_TYPE (Building $INSTALLER_TYPE package)"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="macos"
    INSTALLER_TYPE="dmg"
else
    OS_TYPE="linux"
    INSTALLER_TYPE="deb"
fi

echo "Detected OS: $OS_TYPE (Building $INSTALLER_TYPE package)"

echo "Cleaning build directories..."
rm -rf dist build

echo "Setting up directory structure..."
mkdir -p dist
mkdir -p build
mkdir -p installers
mkdir -p installers/$OS_TYPE

echo "Finding Java source files..."
find src -name "*.java" > sources.txt

echo "Compiling Java files..."
javac -cp "lib/*" -d build @sources.txt

echo "Creating JAR file..."
jar --create --file dist/LibraryManager.jar --main-class LibraryManagementSystem -C build . -C src resources

echo "Copying dependencies..."
if [ -d "lib" ]; then
    cp lib/*.jar dist/
fi

echo "Creating $OS_TYPE installer with bundled Java runtime..."
if [[ "$OS_TYPE" == "windows" ]]; then

    jpackage --type msi --input dist --dest installers/windows --name "Library Manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --win-dir-chooser --win-menu --win-menu-group "Library Manager" --win-shortcut --win-shortcut-prompt --icon src/resources/icon/icon.ico

elif [[ "$OS_TYPE" == "macos" ]]; then

    # macOS installer not tested yet
    jpackage --type dmg --input dist --dest installers/macos --name "Library Manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --mac-package-name "Library Manager" --icon src/resources/icon/icon.icns

else

    jpackage --type deb --input dist --dest installers/linux --name "Library Manager" --main-jar LibraryManager.jar --main-class LibraryManagementSystem --app-version $APP_VERSION --description "Library Management System" --copyright "Copyright 2025" --linux-menu-group "Office" --linux-shortcut

fi

echo "Build completed!"
echo "Installer created in: installer/$OS_TYPE/"

echo "Installer output directory contents:"
ls -la installers/$OS_TYPE/
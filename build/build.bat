@echo off
echo Building file...
javac -d ./build *.java
cd build
jar cmf manifest.mf frcZebraDisplay.jar *
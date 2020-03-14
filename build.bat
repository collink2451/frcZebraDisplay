@echo off
echo Building file...
javac -d ./build1 zebraDataDisplay.java
javac -d ./build2 videoTracking.java
cd build1
jar cmf manifest.mf frcZebraDisplay.jar *
cd..
cd build2
jar cmf manifest.mf videoTracking.jar *
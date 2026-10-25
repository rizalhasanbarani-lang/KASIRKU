@echo off
title Uji Logika OOP Kasir (Console)
echo Melakukan kompilasi dan pengujian konsep OOP...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src/main/java/com/kasir/*.java src/main/java/com/kasir/model/*.java src/main/java/com/kasir/service/*.java src/main/java/com/kasir/view/*.java
java -cp bin com.kasir.TestKasirOOP
pause

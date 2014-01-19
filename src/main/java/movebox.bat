cls
cd %~dp0
dir
del tablero*.txt
java -classpath bin -Xmx1500M movebox.Movebox
pause
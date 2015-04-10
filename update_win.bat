
echo %PATH%

echo %1 %2

rem where is the diagramgen ?
cd %HOMEPATH%

rem Extract diagrams from EA-repos and put result as specified in ea.application.properties
java -jar diagramgen.jar %1

pause



echo %PATH%

echo %1 %2

rem where is the diagramgen ?
rem cd %HOMEPATH%

rem Extract diagrams from EA-repos and put result as specified in ea.application.properties
java -jar diagramgen.jar ea.application.properties %1

rem find the URL of the generated diagram and put it on the clipboard
for /f "delims=" %%a in ('type generated.txt') do @set abc=%%a
echo http://confluence.yourorg.org/ea%abc% | clip

pause



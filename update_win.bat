
echo %PATH%

echo %1 %2 %3 %4 %5

rem where is the diagramgen ?
rem cd %HOMEPATH%

rem Extract diagrams from EA-repos and put result as specified in ea.application.properties

if [%3]==[] (
    java -jar diagramgen.jar ea.application.properties %1 %4 %5
) else (
    java -jar diagramgen.jar ea.application.properties %1 -p ea.project=%3 %4 %5
)

rem find the URL of the generated diagram and put it on the clipboard
for /f "delims=" %%a in ('type generated.txt') do @set abc=%%a
echo http://ea-images.elhub.org%abc% | clip

if "%2"=="pause" (
pause
)



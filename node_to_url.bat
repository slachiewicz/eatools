
echo %PATH%

set tmpFile=generated.txt

paste.exe > "%tmpFile%"
type %tmpFile%

set /p getClip= < "%tmpFile%"

echo %getClip%
java -jar diagramgen.jar ea.application.properties -np "%getClip%"

echo %getClip%
type %tmpFile%

rem find the URL of the generated diagram and put it on the clipboard
for /f "delims=" %%a in ('type %tmpFile%') do @set abc=%%a
echo http://ea-images.elhub.org%abc% | clip

paste.exe

if "%1"=="pause" (
    pause
)

@echo off
setlocal enabledelayedexpansion
if "%~1"=="" (set "EXTRA_ARGS=") else set "EXTRA_ARGS=%*"
for /d %%D in (*) do (
  if exist "%%D\gradlew.bat" (
    echo.
    echo === Building %%D ===
    pushd "%%D"
    call gradlew.bat --no-daemon build %EXTRA_ARGS%
    popd
  ) else if exist "%%D\gradlew" (
    echo.
    echo === Building %%D (gradlew) ===
    pushd "%%D"
    call gradlew --no-daemon build %EXTRA_ARGS%
    popd
  )
)
endlocal
echo All builds finished.

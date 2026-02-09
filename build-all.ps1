param([Parameter(ValueFromRemainingArguments=$true)] $ExtraArgs)

Get-ChildItem -Directory | ForEach-Object {
  $d = $_.FullName
  if (Test-Path (Join-Path $d 'gradlew.bat')) {
    Write-Host "=== Building $($_.Name) with gradlew.bat ==="
    Push-Location $d
    & .\gradlew.bat --no-daemon build @ExtraArgs
    Pop-Location
  } elseif (Test-Path (Join-Path $d 'gradlew')) {
    Write-Host "=== Building $($_.Name) with gradlew ==="
    Push-Location $d
    & ./gradlew --no-daemon build @ExtraArgs
    Pop-Location
  }
}

Write-Host "All builds finished."

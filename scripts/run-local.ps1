param([string]$MavenCommand = "")
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
if ([string]::IsNullOrWhiteSpace($MavenCommand)) { $MavenCommand = Join-Path (Get-Location).Path "mvnw.cmd" }
& $MavenCommand -B -ntp verify
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación o las pruebas" }
$projectRoot = (Get-Location).Path
foreach ($svc in @("eventos","compras")) {
  $jar = Join-Path $projectRoot "$svc-service/target/$svc-service-1.0.0.jar"
  $outLog = Join-Path $projectRoot "$svc.log"
  $errLog = Join-Path $projectRoot "$svc-error.log"
  $proc = Start-Process java -ArgumentList @("-jar", "`"$jar`"", "--spring.profiles.active=local") -WorkingDirectory $projectRoot -RedirectStandardOutput $outLog -RedirectStandardError $errLog -WindowStyle Hidden -PassThru
  Write-Host "$svc iniciado con PID $($proc.Id). Log: $outLog"
}
Write-Host "Espera el arranque y revisa http://localhost:8081/actuator/health y http://localhost:8082/actuator/health"
Write-Host "Para detener: Stop-Process -Id <PID mostrado>"




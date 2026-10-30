param(
  [string]$DatabaseHost = "localhost",
  [ValidateRange(1,65535)][int]$DatabasePort = 1521,
  [string]$DatabaseService = "XEPDB1",
  [string]$MavenCommand = "",
  [string]$CredentialFile = "",
  [switch]$SkipBuild
)
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot
$null = Get-Command java -ErrorAction Stop
$socket = [Net.Sockets.TcpClient]::new()
try {
  $connect = $socket.ConnectAsync($DatabaseHost,$DatabasePort)
  if (-not $connect.Wait(3000) -or -not $socket.Connected) { throw "Oracle no responde en el host y puerto indicados." }
} finally { $socket.Dispose() }
if (-not $SkipBuild) {
  if ([string]::IsNullOrWhiteSpace($MavenCommand)) { $MavenCommand = Join-Path $projectRoot "mvnw.cmd" }
  & $MavenCommand -B -ntp verify
  if ($LASTEXITCODE -ne 0) { throw "Falló la compilación o las pruebas." }
}
foreach ($svc in @("eventos","compras")) {
  $jar = Join-Path $projectRoot "$svc-service/target/$svc-service-1.0.0.jar"
  if (-not (Test-Path -LiteralPath $jar)) { throw "Falta el JAR de $svc. Ejecuta sin -SkipBuild." }
}
if ([string]::IsNullOrWhiteSpace($CredentialFile)) {
  $eventosCredential = Get-Credential -UserName "EVENTOS_APP" -Message "Contrasena del esquema de eventos"
  if ($null -eq $eventosCredential) { throw "Inicio cancelado." }
  $comprasCredential = Get-Credential -UserName "COMPRAS_APP" -Message "Contrasena del esquema de compras"
  if ($null -eq $comprasCredential) { throw "Inicio cancelado." }
} else {
  $savedCredentials = @{}
  foreach ($line in [IO.File]::ReadAllLines((Resolve-Path -LiteralPath $CredentialFile).Path,[Text.Encoding]::UTF8)) {
    if ($line -match '^(EVENTOS_APP|COMPRAS_APP):\s*([A-Za-z][A-Za-z0-9_]{15,29})\s*$') {
      $savedCredentials[$Matches[1]] = $Matches[2]
    }
  }
  if ($savedCredentials.Count -ne 2) { throw "El archivo no contiene ambas credenciales." }
  $eventosCredential = [Management.Automation.PSCredential]::new("EVENTOS_APP",(ConvertTo-SecureString $savedCredentials["EVENTOS_APP"] -AsPlainText -Force))
  $comprasCredential = [Management.Automation.PSCredential]::new("COMPRAS_APP",(ConvertTo-SecureString $savedCredentials["COMPRAS_APP"] -AsPlainText -Force))
  $savedCredentials.Clear()
}
if ($eventosCredential.UserName -cne "EVENTOS_APP" -or $comprasCredential.UserName -cne "COMPRAS_APP") {
  throw "Conserva los nombres de usuario EVENTOS_APP y COMPRAS_APP del asistente."
}
$oldEnvironment = @{}
foreach ($key in @("DB_URL","DB_USERNAME","DB_PASSWORD","SPRING_PROFILES_ACTIVE","EVENTOS_BASE_URL")) {
  $oldEnvironment[$key] = [Environment]::GetEnvironmentVariable($key,"Process")
}
$started = @()
try {
  $env:DB_URL = "jdbc:oracle:thin:@//{0}:{1}/{2}" -f $DatabaseHost,$DatabasePort,$DatabaseService
  $env:SPRING_PROFILES_ACTIVE = "oracle"
  $env:EVENTOS_BASE_URL = "http://localhost:8081"
  foreach ($svc in @("eventos","compras")) {
    $credential = if ($svc -eq "eventos") { $eventosCredential } else { $comprasCredential }
    $env:DB_USERNAME = $credential.UserName
    $env:DB_PASSWORD = $credential.GetNetworkCredential().Password
    $jar = Join-Path $projectRoot "$svc-service/target/$svc-service-1.0.0.jar"
    $proc = Start-Process java -ArgumentList @("-jar", "`"$jar`"") -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $projectRoot "$svc-oracle.log") -RedirectStandardError (Join-Path $projectRoot "$svc-oracle-error.log") -PassThru
    $started += $proc
    Write-Host "$svc proceso creado con PID $($proc.Id); esperando arranque."
  }
  for ($i=0; $i -lt $started.Count; $i++) {
    $proc = $started[$i]
    $port = @(8081,8082)[$i]
    $up = $false
    for ($attempt=0; $attempt -lt 60; $attempt++) {
      $proc.Refresh()
      if ($proc.HasExited) {
        throw "El servicio del puerto $port se cerro. Revisa su archivo -oracle.log; busca el primer error ORA-."
      }
      try {
        if ((Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 2).status -eq "UP") {
          $up = $true
          break
        }
      } catch {}
      Start-Sleep -Seconds 1
    }
    if (-not $up) { throw "El servicio del puerto $port no confirmo salud UP. Revisa los logs." }
    Write-Host "OK: servicio $port responde UP contra Oracle."
  }
} catch {
  foreach ($proc in $started) { Stop-Process -Id $proc.Id -ErrorAction SilentlyContinue }
  throw
} finally {
  foreach ($key in $oldEnvironment.Keys) { [Environment]::SetEnvironmentVariable($key,$oldEnvironment[$key],"Process") }
  $credential = $null
  $eventosCredential = $null
  $comprasCredential = $null
}
Write-Host "Revisa http://localhost:8081/actuator/health y http://localhost:8082/actuator/health después del arranque."
Write-Host "Para detener los servicios usa Stop-Process -Id con los PID mostrados."


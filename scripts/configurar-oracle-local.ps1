param(
  [string]$SqlPlusCommand = "sqlplus",
  [ValidatePattern('^//[A-Za-z0-9.-]+:[0-9]{1,5}/[A-Za-z0-9_.-]+$')]
  [string]$DatabaseConnect = "//localhost:1521/XEPDB1"
)
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
$sqlplus = Get-Command $SqlPlusCommand -ErrorAction Stop
$oldHome = $env:ORACLE_HOME
try {
  $env:ORACLE_HOME = Split-Path (Split-Path $sqlplus.Source -Parent) -Parent
  Write-Host "Conexion: SYSTEM@$DatabaseConnect"
  Write-Host "Introduce la contrasena nueva de SYSTEM al solicitarla SQL Plus."
  Write-Host "El instalador solo se ejecuta despues de autenticar. No borra usuarios."
  & $sqlplus.Source '-L' "SYSTEM@$DatabaseConnect" '@database/00_instalar_oracle_xe.sql'
  if ($LASTEXITCODE -ne 0) {
    throw "No se completo la configuracion. Revisa el primer error mostrado; si se crearon objetos, revisalos antes de repetir."
  }
} finally { $env:ORACLE_HOME = $oldHome }

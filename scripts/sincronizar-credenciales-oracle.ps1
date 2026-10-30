param(
  [Parameter(Mandatory=$true)][string]$CredentialFile,
  [string]$SqlPlusCommand = "sqlplus",
  [switch]$IniciarServicios
)
$ErrorActionPreference="Stop"
$projectRoot=Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot
$credentialPath=(Resolve-Path -LiteralPath $CredentialFile).Path
$passwords=@{}
foreach($line in [IO.File]::ReadAllLines($credentialPath,[Text.Encoding]::UTF8)) {
  if($line -match '^(EVENTOS_APP|COMPRAS_APP):\s*([A-Za-z][A-Za-z0-9_]{15,29})\s*$') {
    $passwords[$Matches[1]]=$Matches[2]
  }
}
if($passwords.Count -ne 2){throw "El archivo no contiene las dos credenciales con el formato esperado."}
$sqlplus=Get-Command $SqlPlusCommand -ErrorAction Stop
$tempSql=Join-Path ([IO.Path]::GetTempPath()) ("oracle-app-"+[guid]::NewGuid().ToString("N")+".sql")
$oldHome=$env:ORACLE_HOME
try {
  $sql=@(
    "set echo off",
    "set verify off",
    "set define off",
    "whenever oserror exit failure rollback",
    "whenever sqlerror exit failure rollback",
    "begin",
    " if sys_context('USERENV','SESSION_USER') <> 'SYSTEM' or sys_context('USERENV','CON_NAME') <> 'XEPDB1' then",
    "  raise_application_error(-20001,'Conecta como SYSTEM a XEPDB1.');",
    " end if;",
    "end;",
    "/",
    "declare n number;",
    "begin",
    " select count(*) into n from dba_users where username in ('EVENTOS_APP','COMPRAS_APP');",
    " if n <> 2 then raise_application_error(-20002,'Faltan usuarios. No se han cambiado contrasenas.'); end if;",
    "end;",
    "/",
    ('alter user EVENTOS_APP identified by "'+$passwords["EVENTOS_APP"]+'" account unlock;'),
    ('alter user COMPRAS_APP identified by "'+$passwords["COMPRAS_APP"]+'" account unlock;'),
    "prompt CONTRASENAS SINCRONIZADAS",
    "exit success"
  ) -join [Environment]::NewLine
  [IO.File]::WriteAllText($tempSql,$sql,[Text.Encoding]::ASCII)
  $env:ORACLE_HOME=Split-Path (Split-Path $sqlplus.Source -Parent) -Parent
  Write-Host "Introduce SOLO la contrasena de SYSTEM cuando SQL Plus la solicite."
  Write-Host "Se sincronizan los dos usuarios de aplicacion con el archivo local. No se borran tablas ni registros."
  & $sqlplus.Source '-L' 'SYSTEM@//localhost:1521/XEPDB1' ('@"'+$tempSql+'"')
  if($LASTEXITCODE -ne 0){throw "No se confirmo la sincronizacion. Revisa el primer error mostrado."}
} finally {
  $passwords.Clear()
  $sql=$null
  if(Test-Path -LiteralPath $tempSql){Remove-Item -LiteralPath $tempSql -Force}
  $env:ORACLE_HOME=$oldHome
}
if($IniciarServicios) {
  & (Join-Path $PSScriptRoot 'run-oracle-local.ps1') -SkipBuild -CredentialFile $credentialPath
}

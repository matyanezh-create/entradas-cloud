param([string]$EventosUrl="http://localhost:8081",[string]$ComprasUrl="http://localhost:8082")
$ErrorActionPreference="Stop"
$fecha=(Get-Date).ToUniversalTime().AddDays(30).ToString("o")
$e=Invoke-RestMethod -Method Post -Uri "$EventosUrl/api/eventos" -ContentType "application/json" -Body (@{nombre="Evento prueba";fecha=$fecha;precio=12500.50}|ConvertTo-Json)
$c=Invoke-RestMethod -Method Post -Uri "$ComprasUrl/api/compras" -ContentType "application/json" -Body (@{eventoId=$e.id;cantidad=3}|ConvertTo-Json)
if ($c.precioTotal -ne 37501.50) { throw "Total incorrecto" }
$guardada=Invoke-RestMethod -Uri "$ComprasUrl/api/compras/$($c.id)"
if ($guardada.id -ne $c.id) { throw "Compra no recuperable" }
Write-Host "OK: evento $($e.id), compra $($c.id), total $($c.precioTotal). Registros conservados para revisar persistencia."


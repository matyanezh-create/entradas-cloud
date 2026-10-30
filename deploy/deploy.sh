#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")"
namespace="${1:?Docker Hub username required}"
revision="${2:?Commit SHA required}"
engine="${3:-postgres}"
[[ "$namespace" =~ ^[a-z0-9][a-z0-9_-]*$ ]] || exit 2
[[ "$revision" =~ ^[a-f0-9]{40}$ ]] || exit 2
test -f .env || { echo 'Falta deploy/.env'; exit 2; }
case "$engine" in
  postgres) compose_file=compose.postgres.yml ;;
  oracle) compose_file=compose.yml; test -s wallet/tnsnames.ora || { echo 'Falta Oracle Wallet'; exit 2; } ;;
  *) echo 'Motor no admitido'; exit 2 ;;
esac
D=(docker)
if ! docker info >/dev/null 2>&1; then D=(sudo -n docker); fi
exec 9>.deploy.lock
flock -n 9 || { echo 'Ya existe un despliegue en curso'; exit 2; }
export EVENTOS_IMAGE="$namespace/entradas-eventos:$revision"
export COMPRAS_IMAGE="$namespace/entradas-compras:$revision"
export APP_VERSION="$revision"
"${D[@]}" compose -p entradas-cloud -f "$compose_file" config --quiet
"${D[@]}" compose -p entradas-cloud -f "$compose_file" pull
if test -f .current-images; then cp .current-images .previous-images; fi
healthy() {
  local port
  for port in 8081 8082; do
    curl -fsS --max-time 5 "http://localhost:$port/actuator/health" | grep -q '"status":"UP"' || return 1
    curl -fsS --max-time 5 "http://localhost:$port/actuator/info" | grep -q "$APP_VERSION" || return 1
  done
}
rollback() {
  echo 'Fallo de despliegue; intentando restaurar las imagenes anteriores'
  if test -f .previous-images; then
    set -a
    source .previous-images
    set +a
    "${D[@]}" compose -p entradas-cloud -f "$compose_file" up -d --remove-orphans
    echo 'Restauracion solicitada; revisar salud'
  else
    echo 'Primer despliegue: no hay version anterior'
  fi
}
if "${D[@]}" compose -p entradas-cloud -f "$compose_file" up -d --remove-orphans; then
  for i in $(seq 1 36); do
    if healthy; then
      printf 'EVENTOS_IMAGE=%s\nCOMPRAS_IMAGE=%s\nAPP_VERSION=%s\n' "$EVENTOS_IMAGE" "$COMPRAS_IMAGE" "$APP_VERSION" > .current-images
      echo "Despliegue verificado: $revision"
      exit 0
    fi
    sleep 5
  done
fi
"${D[@]}" compose -p entradas-cloud -f "$compose_file" logs --tail 60 eventos compras
rollback
exit 1

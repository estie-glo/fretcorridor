#!/usr/bin/env bash
# Lance l'app Flutter avec API_BASE détectée automatiquement (appareil physique).
#
# Usage (depuis mobile/) :
#   ./scripts/run_dev.sh              # IP WiFi locale
#   ./scripts/run_dev.sh --emulator   # 10.0.2.2 (émulateur Android)
#   API_BASE=http://host:8080/api ./scripts/run_dev.sh
#
# Arguments supplémentaires passés à `flutter run` (ex. -d chrome).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

_detect_ip() {
  local ip=""
  if ip link show wlp1s0 &>/dev/null; then
    ip="$(ip -4 addr show wlp1s0 2>/dev/null | awk '/inet / {print $2}' | cut -d/ -f1 | head -1)"
  fi
  if [[ -z "$ip" ]]; then
    ip="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for (i = 1; i <= NF; i++) if ($i == "src") { print $(i + 1); exit }}')"
  fi
  echo "$ip"
}

if [[ "${1:-}" == "--emulator" ]]; then
  API_BASE="http://10.0.2.2:8080/api"
  shift
elif [[ -z "${API_BASE:-}" ]]; then
  IP="$(_detect_ip)"
  if [[ -z "$IP" ]]; then
    echo "❌ IP locale introuvable. Définissez API_BASE manuellement :"
    echo "   API_BASE=http://VOTRE_IP:8080/api ./scripts/run_dev.sh"
    exit 1
  fi
  API_BASE="http://${IP}:8080/api"
fi

echo "✅ API_BASE=$API_BASE"
echo "   Backend attendu sur le port 8080 (0.0.0.0)"

cd "$MOBILE_ROOT"
exec flutter run --dart-define=API_BASE="$API_BASE" "$@"

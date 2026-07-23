#!/usr/bin/env bash
# Déprécié — Phase C : la config API passe par --dart-define (voir run_dev.sh).
echo "⚠️  update_ip.sh est déprécié (Phase C)."
echo "    Utilisez plutôt : ./scripts/run_dev.sh"
echo "    Émulateur Android : ./scripts/run_dev.sh --emulator"
echo ""
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/run_dev.sh" "$@"

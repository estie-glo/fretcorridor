#!/bin/bash

# Détecte l'IP WiFi active (ignore localhost, docker, libvirt)
IP=$(ip addr show wlp1s0 2>/dev/null | grep "inet " | awk '{print $2}' | cut -d/ -f1)

if [ -z "$IP" ]; then
  echo "❌ Impossible de détecter l'IP WiFi (wlp1s0). Vérifie que tu es bien connectée."
  exit 1
fi

echo "✅ IP détectée : $IP"

# Met à jour le baseUrl dans dio_provider.dart
sed -i "s|http://[0-9.]*:8080/api|http://$IP:8080/api|" lib/providers/dio_provider.dart

echo "✅ baseUrl mis à jour dans lib/providers/dio_provider.dart"
grep -n "baseUrl" lib/providers/dio_provider.dart

echo ""
echo " N'oublie pas de vérifier que le backend tourne (mvn spring-boot:run)"
echo "Et que ton téléphone est sur le même réseau que ce PC"

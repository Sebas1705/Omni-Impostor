#!/usr/bin/env bash
# Run this once after cloning: bash scripts/setup-secrets.sh
# Requires: doppler CLI installed + `doppler login` already done
set -euo pipefail

mkdir -p app

SIGNING_KEYSTORE_PASSWORD=$(doppler secrets get SIGNING_KEYSTORE_PASSWORD --plain)
SIGNING_KEY_ALIAS=$(doppler secrets get SIGNING_KEY_ALIAS --plain)
EXAMPLE_URL_DEV=$(doppler secrets get EXAMPLE_URL_DEV --plain)
EXAMPLE_URL_PRO=$(doppler secrets get EXAMPLE_URL_PRO --plain)

cat > app/secrets.properties << EOF
signing_keystore_file=keystore.jks
signing_key_alias=${SIGNING_KEY_ALIAS}
signing_keystore_password=${SIGNING_KEYSTORE_PASSWORD}
signing_key_password=${SIGNING_KEYSTORE_PASSWORD}
EXAMPLE_URL_DEV=${EXAMPLE_URL_DEV}
EXAMPLE_URL_PRO=${EXAMPLE_URL_PRO}
EOF

doppler secrets get GOOGLE_SERVICES_JSON --plain > app/google-services.json
doppler secrets get KEYSTORE_BASE64 --plain | base64 --decode > app/keystore.jks

echo "Done — app/secrets.properties, app/google-services.json and app/keystore.jks created."

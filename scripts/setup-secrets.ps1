# Run this once after cloning: .\scripts\setup-secrets.ps1
# Requires: doppler CLI installed + `doppler login` already done
$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path "app" | Out-Null

$keystorePass = doppler secrets get SIGNING_KEYSTORE_PASSWORD --plain
$keyAlias     = doppler secrets get SIGNING_KEY_ALIAS --plain
$urlDev       = doppler secrets get EXAMPLE_URL_DEV --plain
$urlPro       = doppler secrets get EXAMPLE_URL_PRO --plain

@"
signing_keystore_file=keystore.jks
signing_key_alias=$keyAlias
signing_keystore_password=$keystorePass
signing_key_password=$keystorePass
EXAMPLE_URL_DEV=$urlDev
EXAMPLE_URL_PRO=$urlPro
"@ | Set-Content -Path "app\secrets.properties" -Encoding UTF8

doppler secrets get GOOGLE_SERVICES_JSON --plain | Set-Content -Path "app\google-services.json" -Encoding UTF8

$keystoreB64 = doppler secrets get KEYSTORE_BASE64 --plain
[System.IO.File]::WriteAllBytes(
    (Resolve-Path ".").Path + "\app\keystore.jks",
    [Convert]::FromBase64String($keystoreB64)
)

Write-Host "Done - app\secrets.properties, app\google-services.json and app\keystore.jks created."

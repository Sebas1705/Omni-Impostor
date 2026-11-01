# Intermediate builds with Firebase App Distribution

Every push to `develop` runs `.github/workflows/distribute.yml`: it builds the **Dev release** APK
(`es.sebas1705.impostorandroidgame.dev`, signed with the release key, version `<default>-dev.<run>` and
`versionCode = run number`) and sends it to the **`testers`** group of the Firebase project `impostor-1b6c2`.
Official versions (Pro) keep going through `release.yml` / Play Store. You can also run the workflow by hand
from the Actions tab (`Run workflow`) choosing another group or custom release notes.

## Where the secrets live

Everything is in the Doppler project **`omni-impostor`** (configs `prd` for CI, `dev_personal` for local work).
The only GitHub secret is `DOPPLER_TOKEN`.

| Doppler secret | Used for |
|---|---|
| `GOOGLE_SERVICES_JSON`, `KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_KEYSTORE_PASSWORD`, `EXAMPLE_URL_DEV/PRO` | Build and signing (already there) |
| `FIREBASE_DISTRIBUTION_SERVICE_ACCOUNT_JSON` | Service account key with only the *Firebase App Distribution Admin* role |
| `FIREBASE_APP_ID_DEV` / `FIREBASE_APP_ID_PRO` | Firebase app ids (`1:421126265133:android:...`) of the Dev and Pro release apps |

Until `FIREBASE_DISTRIBUTION_SERVICE_ACCOUNT_JSON` and `FIREBASE_APP_ID_DEV` exist, the workflow does nothing
(it ends green with a notice).

## One-time setup

Needs a project owner (you), the Firebase CLI and the Doppler CLI, both logged in:

```bash
firebase login
doppler login
node scripts/firebase-distribution/setup.cjs you@example.com   # emails of the testers
```

The script enables the APIs, creates the `app-distribution` service account with only the App Distribution Admin
role, stores its key in Doppler (never printed), stores the app ids and creates the `testers` group.

Manual alternative: in the Google Cloud console of `impostor-1b6c2` create a service account, give it the role
*Firebase App Distribution Admin*, create a JSON key and load it with
`doppler secrets set FIREBASE_DISTRIBUTION_SERVICE_ACCOUNT_JSON --project omni-impostor --config prd < key.json`
(then delete the file); set the two app ids the same way; create the group `testers` in Firebase console,
App Distribution.

## Testers

Add or remove testers in Firebase console, App Distribution, Testers & Groups, `testers`. They receive an
email per build and install it with the App Tester app. To rotate the key, create a new key for the service
account and overwrite the Doppler secret.

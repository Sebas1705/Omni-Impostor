# Publishing to Google Play from CI

The automation is in place but **off**: nothing touches Google Play until the owner finishes the
steps below. While the repository variable `PLAY_TRACK` is unset, `release.yml` behaves as it always
did (GitHub Release with the signed AAB) and its `publish_play` job shows as skipped.

Package: `es.sebas1705.impostorandroidgame`. The tool is `fastlane supply` 2.241.0, the same as Owlsplit.

## What the owner has to do (once)

All of this needs your Google accounts. Never paste the JSON key in a chat or commit it.

1. **Create the service account.** In the [Google Cloud console](https://console.cloud.google.com/),
   Firebase project `impostor-1b6c2` (any Cloud project works):
   - *APIs & Services → Library* → **Google Play Android Developer API** → **Enable**.
   - *IAM & Admin → Service accounts → Create*: name `play-publisher`, so the email is
     `play-publisher@impostor-1b6c2.iam.gserviceaccount.com`. **No Cloud IAM roles.** Do not reuse
     `app-distribution`: it should keep only its Firebase role.
   - On the new account: *Keys → Add key → JSON*. A `.json` file is downloaded.

2. **Invite it in Play Console.** *Users and permissions → Invite new users*, with that email. No
   account-level or financial permissions. Under *App permissions* add **Omni-Impostor** with:
   - **View app information (read only)**
   - **Release to testing tracks**
   - **Manage testing tracks and edit tester lists**
   - **Release to production, exclude devices, and use Play App Signing**

   *Manage store presence* is not needed: this automation only sends release notes, never the
   listing. No Google Cloud project has to be linked in Play Console any more. A new service account
   can take some hours to start working.

3. **Store the key in Doppler**, project `omni-impostor`, config `prd` (the one CI reads), secret
   **`PLAY_SERVICE_ACCOUNT_JSON`**, as plain JSON (not base64). It is not needed in `dev_personal`:
   ```bash
   doppler secrets set PLAY_SERVICE_ACCOUNT_JSON --project omni-impostor --config prd < play-publisher.json
   rm play-publisher.json
   ```
   Nothing changes in GitHub secrets: `DOPPLER_TOKEN` is still the only one.

4. **Pick the track and turn it on**, last, because from then on every tag uploads. In GitHub:
   *Settings → Secrets and variables → Actions → Variables*, or:
   ```bash
   gh variable set PLAY_TRACK --repo Sebas1705/Omni-Impostor --body internal
   ```

| Repository variable | Values | Meaning |
|---|---|---|
| `PLAY_TRACK` | `internal`, `alpha`, `beta`, `production` or a custom closed track | Where a tag uploads. Unset: no upload. |
| `PLAY_RELEASE_STATUS` | `draft` (default when unset), `completed` | How the upload lands in that track. |

The default is `draft` because this is a live production app: a tag must never reach users on its
own, and `completed` on `production` would mean 100 % of users at once. Choose one of:

- **Testers first (recommended):** `PLAY_TRACK=internal` and `PLAY_RELEASE_STATUS=completed`. Each
  tag reaches the internal testers straight away, and production is a later, deliberate promote.
- **Straight to production, by hand:** `PLAY_TRACK=production` and no status. Each tag leaves a
  draft in production and you start the rollout with *Play promote*, or in Play Console.

`PLAY_TRACK=internal` with no status also works, but a draft has to be published by hand or
promoted with its version code.

If `PLAY_TRACK` is set and the key is missing or lacks permissions, the job **fails**: a release that
did not reach Play must not look green. The GitHub Release is already published by then, so the
Play upload can be retried with *Re-run failed jobs* within a day (the artifact lives for one day).

The first upload jumps `versionCode` from 7 to the commit count (above 175), which is expected. That
AAB must be signed with the upload key Play knows; `KEYSTORE_BASE64` signed the 1.3.0 AAB uploaded by
hand, so it should be.

## Publishing a version

```bash
git tag v1.4.0 && git push origin v1.4.0     # or: scripts/new-release.ps1 -Version 1.4.0 -Push
```

The tag must be a commit of `main`. There is nothing to bump: `versionName` is the tag without the
`v`, and `versionCode` is `git rev-list --count HEAD` at the tag, which needs a full clone
(`fetch-depth: 0`; a shallow checkout would count far too few, so the release fails if it sees one).
Tags must therefore go forward: Play refuses a `versionCode` it has seen or a lower one in the same
track.

Use the tag push rather than *Run workflow* on `release.yml` for a new version: a tag created by the
workflow itself does not start the tag-push run that publishes the release, a GitHub limitation of
`GITHUB_TOKEN`. Running it by hand only works for a tag that already exists.

## Release notes

`scripts/release-notes.mjs` writes the notes for the **10 listing locales**: en-US, en-AU, en-CA,
en-GB, en-IN, en-SG and en-ZA get the English text, es-ES, es-419 and es-US the Spanish one, 500
characters at most each. A language added to or removed from the listing has to be changed in the
script as well.

By default they are the `feat`/`fix`/`perf` PRs of the GitHub Release with their prefix removed, so
**those PR titles are what users read**, in English under a translated heading. For real notes, add
hand-written ones to the PR that precedes the tag; they are used as they are, and `validate.yml`
checks both exist and fit:

```
fastlane/release-notes/1.4.0/en-US.txt
fastlane/release-notes/1.4.0/es-ES.txt
```

## Play promote

*Actions → Play promote → Run workflow*. Google reviews what reaches production as usual.

| To… | `from` | `to` | `rollout` | `version_code` |
|---|---|---|---|---|
| Send the tested release to 10 % of users | `internal` | `production` | `0.1` | |
| Raise that rollout | `production` | `production` | `0.5` | |
| Finish it | `production` | `production` | `1` | |
| Start a draft that is in production | `production` | `production` | `0.1` | |
| Promote a draft from another track | `internal` | `production` | `0.1` | its code |

`version_code` is needed for a draft in another track, and when a track holds both a rollout in
progress and a new draft. Without it, a promote finds only a `completed` release of `from`, and
fails rather than guessing if there is none.

#!/usr/bin/env node
// Google Play release notes from the GitHub Release body ("* <PR title> by @user in <url>").
//
//   node scripts/release-notes.mjs <version> <body-file> <metadata-dir> <version-code>   (release.yml)
//   node scripts/release-notes.mjs --check                                               (validate.yml)
//
// Writes <metadata-dir>/<locale>/changelogs/<version-code>.txt, the layout `fastlane supply` reads,
// for the 10 locales of the Play listing: English ones get the English text, Spanish ones the
// Spanish text, 500 characters at most (Google Play's limit). Only user-facing changes are listed:
// feat/fix/perf PRs without their prefix. PR titles are in English, so both languages list them
// under a translated heading.
//
// Hand-written notes win: fastlane/release-notes/<version>/{en-US,es-ES}.txt are used as they are,
// and `--check` keeps them within the limit, so a text too long fails the PR and not the release.
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const CURATED_DIR = join(ROOT, "fastlane", "release-notes");
const PLAY_LIMIT = 500;
// The languages of the Play listing, by the text they take.
const LOCALES = {
  "en-US": ["en-US", "en-AU", "en-CA", "en-GB", "en-IN", "en-SG", "en-ZA"],
  "es-ES": ["es-ES", "es-419", "es-US"],
};

const read = (file) => readFileSync(file, "utf8").replace(/\r\n/g, "\n").trim();

if (process.argv[2] === "--check") process.exit(check());

const [version, bodyFile, metadataDir, versionCode] = process.argv.slice(2);
if (!version || !bodyFile || !metadataDir || !versionCode) {
  console.error("usage: release-notes.mjs <version> <body-file> <metadata-dir> <version-code> | --check");
  process.exit(2);
}

const USER_FACING = /^(feat|fix|perf)(\(.*?\))?!?:\s*/i;

const titles = readFileSync(bodyFile, "utf8")
  .split("\n")
  .map((line) => /^\*\s+(.*?)\s+by\s+@\S+\s+in\s+\S+\s*$/.exec(line)?.[1])
  .filter((title) => title && USER_FACING.test(title))
  .map((title) => title.replace(USER_FACING, ""))
  .map((title) => title.charAt(0).toUpperCase() + title.slice(1));

/** Heading plus as many bullets as fit in Play's limit; a fallback line when nothing is user-facing. */
function notes(heading, fallback) {
  if (titles.length === 0) return `${heading}\n${fallback}`;
  let text = heading;
  for (const title of titles) {
    const next = `${text}\n• ${title}`;
    if (next.length > PLAY_LIMIT) break;
    text = next;
  }
  return text;
}

/** The hand-written notes of this version and language, or null. Too long is an error, not a cut. */
function curated(locale) {
  const file = join(CURATED_DIR, version, `${locale}.txt`);
  if (!existsSync(file)) return null;
  const text = read(file);
  if (text.length > PLAY_LIMIT) {
    console.error(`${file}: ${text.length} characters, Google Play allows ${PLAY_LIMIT}`);
    process.exit(1);
  }
  return text;
}

const generated = {
  "en-US": () => notes(`What's new in ${version}:`, "• Improvements and fixes."),
  "es-ES": () => notes(`Novedades de la ${version}:`, "• Mejoras y correcciones."),
};

for (const [base, locales] of Object.entries(LOCALES)) {
  const text = curated(base) ?? generated[base]();
  for (const locale of locales) {
    const dir = join(metadataDir, locale, "changelogs");
    mkdirSync(dir, { recursive: true });
    writeFileSync(join(dir, `${versionCode}.txt`), text);
  }
  console.log(`--- ${base} (${text.length}/${PLAY_LIMIT}), for ${locales.join(", ")}\n${text}`);
}

/** Every hand-written note present in both languages and within Play's limit; 0 when they are. */
function check() {
  const versions = existsSync(CURATED_DIR) ? readdirSync(CURATED_DIR) : [];
  const problems = [];
  for (const ver of versions) {
    for (const base of Object.keys(LOCALES)) {
      const file = join(CURATED_DIR, ver, `${base}.txt`);
      const length = existsSync(file) ? read(file).length : null;
      const ok = length !== null && length <= PLAY_LIMIT;
      console.log(`${ok ? "ok" : "PROBLEM"}  ${ver}/${base}.txt: ${length ?? "missing"}/${PLAY_LIMIT}`);
      if (!ok) problems.push(file);
    }
  }
  if (versions.length === 0) console.log("No hand-written release notes.");
  if (problems.length) console.error(`${problems.length} note(s) missing or over Google Play's limit`);
  return problems.length ? 1 : 0;
}

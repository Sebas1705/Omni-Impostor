#!/usr/bin/env node
/**
 * One-time Firebase App Distribution setup. Run it yourself, logged in as a project owner:
 *
 *   firebase login && doppler login
 *   node scripts/firebase-distribution/setup.cjs you@example.com [other@example.com ...]
 *
 * It (1) enables the needed APIs, (2) creates the service account `app-distribution` with ONLY the
 * "Firebase App Distribution Admin" role, (3) creates a key and stores it in Doppler (prd + dev_personal)
 * as FIREBASE_DISTRIBUTION_SERVICE_ACCOUNT_JSON without printing it, (4) stores the Dev/Pro Firebase app ids
 * in Doppler, and (5) creates the `testers` group with the given emails.
 * It reuses the OAuth login of the Firebase CLI (firebase-tools), so no extra credentials are needed.
 */
const { execFileSync } = require('node:child_process');
const path = require('node:path');

const PROJECT = 'impostor-1b6c2';
const NUMBER = '421126265133';
const SA_ID = 'app-distribution';
const SA_EMAIL = `${SA_ID}@${PROJECT}.iam.gserviceaccount.com`;
const DOPPLER = { project: 'omni-impostor', configs: ['prd', 'dev_personal'] };
const APP_IDS = {
  FIREBASE_APP_ID_DEV: '1:421126265133:android:1715960b1f99da0aae9ba4',
  FIREBASE_APP_ID_PRO: '1:421126265133:android:e4c81a4d5f9c0a99ae9ba4',
};
const testers = process.argv.slice(2);

const globalRoot = execFileSync('npm', ['root', '-g'], { shell: true }).toString().trim();
const ft = (file) => require(path.join(globalRoot, 'firebase-tools', 'lib', file));
const auth = ft('auth.js');
const api = ft('apiv2.js');
const account = auth.getGlobalDefaultAccount();
if (!account) throw new Error('Run `firebase login` first.');
api.setRefreshToken(account.tokens.refresh_token);

const call = (prefix, method, p, body) =>
  new api.Client({ urlPrefix: prefix, auth: true }).request({ method, path: p, body, resolveOnHTTPError: true });
const brief = (r) => `${r.status}${r.status >= 300 ? ' ' + JSON.stringify(r.body).slice(0, 300) : ''}`;
const dopplerSet = (name, value) => {
  for (const config of DOPPLER.configs) {
    execFileSync('doppler', ['secrets', 'set', name, '--project', DOPPLER.project, '--config', config], {
      input: value,
      stdio: ['pipe', 'ignore', 'inherit'],
    });
  }
};

(async () => {
  for (const svc of ['firebaseappdistribution.googleapis.com', 'iam.googleapis.com', 'cloudresourcemanager.googleapis.com']) {
    console.log('enable', svc, brief(await call('https://serviceusage.googleapis.com', 'POST', `/v1/projects/${PROJECT}/services/${svc}:enable`, {})));
  }

  let r = await call('https://iam.googleapis.com', 'GET', `/v1/projects/${PROJECT}/serviceAccounts/${SA_EMAIL}`);
  if (r.status === 404) {
    r = await call('https://iam.googleapis.com', 'POST', `/v1/projects/${PROJECT}/serviceAccounts`, {
      accountId: SA_ID,
      serviceAccount: { displayName: 'App Distribution (CI)' },
    });
    console.log('create service account', brief(r));
  } else console.log('service account exists', r.status);

  const crm = 'https://cloudresourcemanager.googleapis.com';
  const policy = (await call(crm, 'POST', `/v1/projects/${PROJECT}:getIamPolicy`, {})).body;
  const role = 'roles/firebaseappdistro.admin';
  let binding = policy.bindings.find((b) => b.role === role && !b.condition);
  if (!binding) policy.bindings.push((binding = { role, members: [] }));
  if (!binding.members.includes(`serviceAccount:${SA_EMAIL}`)) binding.members.push(`serviceAccount:${SA_EMAIL}`);
  console.log('grant role', brief(await call(crm, 'POST', `/v1/projects/${PROJECT}:setIamPolicy`, { policy })));

  r = await call('https://iam.googleapis.com', 'POST', `/v1/projects/${PROJECT}/serviceAccounts/${SA_EMAIL}/keys`, {
    keyAlgorithm: 'KEY_ALG_RSA_2048',
    privateKeyType: 'TYPE_GOOGLE_CREDENTIALS_FILE',
  });
  if (r.status >= 300) throw new Error('create key failed: ' + brief(r));
  dopplerSet('FIREBASE_DISTRIBUTION_SERVICE_ACCOUNT_JSON', Buffer.from(r.body.privateKeyData, 'base64').toString('utf8'));
  console.log('service account key stored in Doppler (not printed)');

  for (const [name, value] of Object.entries(APP_IDS)) dopplerSet(name, value);
  console.log('app ids stored in Doppler');

  console.log('create group', brief(await call('https://firebaseappdistribution.googleapis.com', 'POST', `/v1/projects/${NUMBER}/groups?groupId=testers`, { displayName: 'Testers' })));
  if (testers.length) {
    console.log('add testers', brief(await call('https://firebaseappdistribution.googleapis.com', 'POST', `/v1/projects/${NUMBER}/groups/testers:batchJoin`, { emails: testers, createMissingTesters: true })));
  }
})().catch((e) => {
  console.error('ERROR', e.message);
  process.exit(1);
});

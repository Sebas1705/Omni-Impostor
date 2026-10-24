import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, deleteDoc, increment, serverTimestamp } from 'firebase/firestore';
import fs from 'node:fs';

const rules = fs.readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8');
const env = await initializeTestEnvironment({ projectId: 'demo-omni', firestore: { rules, host: '127.0.0.1', port: 8765 } });
let failed = 0;
async function check(name, p, expectOk) {
  try { expectOk ? await assertSucceeds(p) : await assertFails(p); console.log('ok  ', name); }
  catch (e) { failed++; console.log('FAIL', name, '-', String(e.message).split('\n')[0]); }
}
const anon = env.authenticatedContext('anon1', { firebase: { sign_in_provider: 'anonymous' } }).firestore();
const noauth = env.unauthenticatedContext().firestore();
const base = { word: 'Moose', category: 'ANIMALS', clue: 'Forest' };
const d = (db, id) => doc(db, 'clue_ratings', id);

// what the app really writes: set(merge) with increment(1)
await check('create positive vote (app shape)', setDoc(d(anon, 'a'), { ...base, positive: increment(1) }, { merge: true }), true);
await check('update: second positive vote on same doc', setDoc(d(anon, 'a'), { ...base, positive: increment(1) }, { merge: true }), true);
await check('update: negative vote on same doc', setDoc(d(anon, 'a'), { ...base, negative: increment(1) }, { merge: true }), true);
await check('create negative vote', setDoc(d(anon, 'b'), { ...base, negative: increment(1) }, { merge: true }), true);

await check('unauthenticated create denied', setDoc(d(noauth, 'c'), { ...base, positive: increment(1) }, { merge: true }), false);
await check('create with +999 denied', setDoc(d(anon, 'd'), { ...base, positive: 999 }), false);
await check('update with +5 denied', setDoc(d(anon, 'a'), { ...base, positive: increment(5) }, { merge: true }), false);
await check('decrement denied', setDoc(d(anon, 'a'), { ...base, positive: increment(-1) }, { merge: true }), false);
await check('create with extra field denied', setDoc(d(anon, 'e'), { ...base, positive: 1, admin: true }), false);
await check('create with huge text denied', setDoc(d(anon, 'f'), { ...base, clue: 'x'.repeat(500), positive: 1 }), false);
await check('create with no vote denied', setDoc(d(anon, 'g'), { ...base }), false);
await check('create with two votes denied', setDoc(d(anon, 'h'), { ...base, positive: 1, negative: 1 }), false);
await check('changing the clue text denied', setDoc(d(anon, 'a'), { ...base, clue: 'Other', positive: increment(1) }, { merge: true }), false);
await check('read denied', getDoc(d(anon, 'a')), false);
await check('delete denied', deleteDoc(d(anon, 'a')), false);
await check('other collection denied', setDoc(doc(anon, 'users', 'x'), { a: 1 }), false);

await env.cleanup();
console.log(failed ? `${failed} FAILED` : 'ALL OK');
process.exit(failed ? 1 : 0);

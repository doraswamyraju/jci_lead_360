import assert from 'node:assert/strict';
import { db } from './src/database/db.js';
import { seedInitialData } from './src/database/seed.js';

async function runSystemVerification() {
  console.log('🧪 Starting JCI LEAD360 Production Verification Tests...');

  // 1. Seed & Data Store Test
  await seedInitialData();
  const stats = db.getStats();
  console.log('  ✓ Stats verified:', stats);
  assert.ok(stats.totalUsers > 0, 'Users must be seeded');
  assert.ok(stats.totalChapters > 0, 'Chapters must be seeded');
  assert.ok(stats.totalTemplates > 0, 'Templates must be seeded');
  assert.ok(stats.totalFrames > 0, 'Frames must be seeded');
  assert.ok(stats.totalEvents > 0, 'Calendar compliance events must be seeded');

  // 2. Auth Lookup Verification
  const admin = db.findUserByEmail('admin@lead360.com');
  assert.ok(admin, 'Super admin account must exist');
  assert.equal(admin.role, 'SUPER_ADMIN', 'Super admin role must match');

  const pres = db.findUserByEmail('president@jcitirupati.com');
  assert.ok(pres, 'President account must exist');
  assert.equal(pres.role, 'PRESIDENT', 'President role must match');

  // 3. Template Retrieval & Coordinate Integrity
  const templates = db.getTemplates();
  const sampleTemplate = templates[0];
  assert.ok(sampleTemplate.layers.length > 0, 'Template layers must exist');
  assert.ok(sampleTemplate.dimensions.width >= 1080, 'Template resolution must be 1080px or higher');

  // 4. Calendar Compliance Event Integrity
  const events = db.getCalendarEvents();
  assert.ok(events.length >= 3, 'Must have at least 3 initial efficiency events');
  const mandatoryEvent = events.find(e => e.mandatory);
  assert.ok(mandatoryEvent, 'Mandatory 100% Efficiency event must exist');

  console.log('🎉 All 4 Automated System Verification Tests Passed with 0 errors!');
}

runSystemVerification().catch(err => {
  console.error('❌ Verification failed:', err);
  process.exit(1);
});

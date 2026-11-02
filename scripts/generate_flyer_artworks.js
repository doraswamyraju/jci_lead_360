import sharp from 'sharp';
import fs from 'fs';

async function createFlyer1() {
  const width = 1080;
  const height = 1080;
  const svg = `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#001a33"/>
        <stop offset="40%" stop-color="#002855"/>
        <stop offset="100%" stop-color="#004b87"/>
      </linearGradient>
      <linearGradient id="goldGrad" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#f59e0b"/>
        <stop offset="50%" stop-color="#fbbf24"/>
        <stop offset="100%" stop-color="#d97706"/>
      </linearGradient>
      <linearGradient id="cyanGrad" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#0097d8"/>
        <stop offset="100%" stop-color="#38bdf8"/>
      </linearGradient>
    </defs>

    <rect width="100%" height="100%" fill="url(#bgGrad)"/>
    
    <circle cx="1000" cy="80" r="320" fill="#0097d8" opacity="0.15"/>
    <circle cx="80" cy="1000" r="260" fill="#f59e0b" opacity="0.12"/>
    <path d="M-50,400 L500,0 L400,-50 Z" fill="#ffffff" opacity="0.04"/>
    <path d="M1130,600 L600,1100 L750,1150 Z" fill="#ffffff" opacity="0.04"/>

    <rect x="0" y="0" width="1080" height="8" fill="url(#cyanGrad)"/>
    
    <rect x="720" y="60" width="280" height="44" rx="22" fill="rgba(0,151,216,0.25)" stroke="#0097d8" stroke-width="1.5"/>
    <text x="860" y="88" fill="#38bdf8" font-family="Arial, sans-serif" font-size="16" font-weight="bold" text-anchor="middle" letter-spacing="2">JCI INDIA • ZONE IV</text>

    <rect x="60" y="200" width="960" height="460" rx="24" fill="rgba(255,255,255,0.05)" stroke="rgba(255,255,255,0.15)" stroke-width="1.5"/>

    <text x="540" y="270" fill="url(#goldGrad)" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" letter-spacing="4">CORDIALLY INVITES YOU TO THE</text>

    <text x="540" y="370" fill="#ffffff" font-family="Arial, sans-serif" font-size="64" font-weight="900" text-anchor="middle" letter-spacing="1">1ST GENERAL BODY MEETING</text>
    <text x="540" y="435" fill="#38bdf8" font-family="Arial, sans-serif" font-size="32" font-weight="bold" text-anchor="middle" letter-spacing="6">TENURE 2027</text>

    <line x1="240" y1="475" x2="840" y2="475" stroke="rgba(255,255,255,0.2)" stroke-width="2"/>

    <text x="540" y="530" fill="#e2e8f0" font-family="Arial, sans-serif" font-size="22" text-anchor="middle">Theme: "Empowering Young Leaders for Sustainable Impact"</text>
    <text x="540" y="575" fill="#94a3b8" font-family="Arial, sans-serif" font-size="18" text-anchor="middle">Join us as we chart the roadmap for an extraordinary year of leadership, fellowship and service.</text>

    <rect x="60" y="690" width="460" height="170" rx="16" fill="rgba(0,151,216,0.1)" stroke="rgba(0,151,216,0.3)" stroke-width="1.5"/>
    
    <rect x="560" y="690" width="460" height="170" rx="16" fill="rgba(245,158,11,0.1)" stroke="rgba(245,158,11,0.3)" stroke-width="1.5"/>

    <rect x="0" y="1020" width="1080" height="60" fill="#001326"/>
    <text x="540" y="1056" fill="#94a3b8" font-family="Arial, sans-serif" font-size="16" text-anchor="middle" letter-spacing="1">Junior Chamber International • Developing Leaders for a Changing World</text>
  </svg>`;

  await sharp(Buffer.from(svg)).png().toFile('./public/assets/tpl_gbm_bg.png');
  console.log('Created tpl_gbm_bg.png');
}

async function createFlyer2() {
  const width = 1080;
  const height = 1350;
  const svg = `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="bgGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#0a192f"/>
        <stop offset="40%" stop-color="#112240"/>
        <stop offset="100%" stop-color="#003366"/>
      </linearGradient>
      <linearGradient id="cyanGrad2" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#0097d8"/>
        <stop offset="100%" stop-color="#00d2ff"/>
      </linearGradient>
      <linearGradient id="goldGrad2" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#f59e0b"/>
        <stop offset="100%" stop-color="#fbbf24"/>
      </linearGradient>
    </defs>

    <rect width="100%" height="100%" fill="url(#bgGrad2)"/>

    <circle cx="900" cy="200" r="350" fill="#0097d8" opacity="0.12"/>
    <circle cx="150" cy="1200" r="300" fill="#00d2ff" opacity="0.08"/>

    <rect x="0" y="0" width="1080" height="10" fill="url(#cyanGrad2)"/>

    <rect x="365" y="60" width="350" height="48" rx="24" fill="rgba(0,151,216,0.2)" stroke="#0097d8" stroke-width="1.5"/>
    <text x="540" y="91" fill="#38bdf8" font-family="Arial, sans-serif" font-size="18" font-weight="bold" text-anchor="middle" letter-spacing="3">100% EFFICIENCY CRITERIA</text>

    <rect x="60" y="220" width="960" height="580" rx="24" fill="rgba(255,255,255,0.04)" stroke="rgba(255,255,255,0.12)" stroke-width="1.5"/>

    <text x="540" y="300" fill="url(#goldGrad2)" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" letter-spacing="4">FLAGSHIP PROGRAM</text>
    
    <text x="540" y="390" fill="#ffffff" font-family="Arial, sans-serif" font-size="56" font-weight="900" text-anchor="middle">LOM ACTION PLAN</text>
    <text x="540" y="460" fill="url(#cyanGrad2)" font-family="Arial, sans-serif" font-size="50" font-weight="900" text-anchor="middle">ADOPTION &amp; LAUNCH</text>

    <line x1="240" y1="510" x2="840" y2="510" stroke="rgba(255,255,255,0.18)" stroke-width="2"/>

    <text x="540" y="580" fill="#f1f5f9" font-family="Arial, sans-serif" font-size="24" text-anchor="middle" font-weight="500">Mandatory Compliance Session for All Active Chapter Members</text>
    
    <rect x="110" y="630" width="260" height="120" rx="14" fill="rgba(0,151,216,0.12)" stroke="rgba(0,151,216,0.25)" stroke-width="1"/>
    <text x="240" y="675" fill="#38bdf8" font-family="Arial, sans-serif" font-size="22" font-weight="bold" text-anchor="middle">100%</text>
    <text x="240" y="715" fill="#cbd5e1" font-family="Arial, sans-serif" font-size="16" text-anchor="middle">Efficiency Points</text>

    <rect x="410" y="630" width="260" height="120" rx="14" fill="rgba(0,151,216,0.12)" stroke="rgba(0,151,216,0.25)" stroke-width="1"/>
    <text x="540" y="675" fill="#38bdf8" font-family="Arial, sans-serif" font-size="22" font-weight="bold" text-anchor="middle">ANNUAL</text>
    <text x="540" y="715" fill="#cbd5e1" font-family="Arial, sans-serif" font-size="16" text-anchor="middle">Budget Approval</text>

    <rect x="710" y="630" width="260" height="120" rx="14" fill="rgba(0,151,216,0.12)" stroke="rgba(0,151,216,0.25)" stroke-width="1"/>
    <text x="840" y="675" fill="#38bdf8" font-family="Arial, sans-serif" font-size="22" font-weight="bold" text-anchor="middle">PROJECT</text>
    <text x="840" y="715" fill="#cbd5e1" font-family="Arial, sans-serif" font-size="16" text-anchor="middle">Portfolio Allocation</text>

    <rect x="60" y="840" width="960" height="220" rx="18" fill="rgba(255,255,255,0.05)" stroke="rgba(0,151,216,0.3)" stroke-width="1.5"/>

    <rect x="0" y="1280" width="1080" height="70" fill="#001326"/>
    <text x="540" y="1322" fill="#94a3b8" font-family="Arial, sans-serif" font-size="16" text-anchor="middle" letter-spacing="1">Junior Chamber International • JCI India Zone IV</text>
  </svg>`;

  await sharp(Buffer.from(svg)).png().toFile('./public/assets/tpl_efficiency_bg.png');
  console.log('Created tpl_efficiency_bg.png');
}

Promise.all([createFlyer1(), createFlyer2()])
  .then(() => {
    console.log('Done generating flyer assets');
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });


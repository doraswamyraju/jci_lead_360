import sharp from 'sharp';

async function generateImages() {
  // 1. Crystal Memento
  const mementoSvg = `<svg width="400" height="400" viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="woodBg" x1="0%" y1="0%" x2="0%" y2="100%">
        <stop offset="0%" stop-color="#3d2314"/>
        <stop offset="100%" stop-color="#1f1108"/>
      </linearGradient>
      <linearGradient id="crystalShine" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#ffffff" stop-opacity="0.9"/>
        <stop offset="30%" stop-color="#bbf2f6" stop-opacity="0.6"/>
        <stop offset="70%" stop-color="#0097d8" stop-opacity="0.2"/>
        <stop offset="100%" stop-color="#ffffff" stop-opacity="0.8"/>
      </linearGradient>
      <linearGradient id="gold" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#d97706"/>
        <stop offset="50%" stop-color="#fbbf24"/>
        <stop offset="100%" stop-color="#d97706"/>
      </linearGradient>
    </defs>
    <!-- Background wall -->
    <rect width="400" height="400" fill="#f8fafc"/>
    <circle cx="200" cy="180" r="160" fill="#e0f2fe" opacity="0.6"/>
    <!-- Wooden Base -->
    <rect x="90" y="310" width="220" height="35" rx="6" fill="url(#woodBg)"/>
    <rect x="70" y="340" width="260" height="25" rx="6" fill="#180c05"/>
    <!-- Crystal Body -->
    <polygon points="200,50 310,130 290,310 110,310 90,130" fill="url(#crystalShine)" stroke="#38bdf8" stroke-width="2"/>
    <!-- JCI Shield Logo in Trophy -->
    <path d="M200,120 L240,140 L230,200 L200,230 L170,200 L160,140 Z" fill="#0097d8" opacity="0.85"/>
    <text x="200" y="175" fill="#ffffff" font-family="Arial, sans-serif" font-weight="900" font-size="18" text-anchor="middle">JCI</text>
    <text x="200" y="270" fill="url(#gold)" font-family="Arial, sans-serif" font-weight="bold" font-size="14" text-anchor="middle" letter-spacing="1">EXCELLENCE AWARD</text>
  </svg>`;
  await sharp(Buffer.from(mementoSvg)).png().toFile('./public/assets/item_memento.png');

  // 2. Banner & Standee
  const bannerSvg = `<svg width="400" height="400" viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="jciBlue" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#001f3f"/>
        <stop offset="50%" stop-color="#005a9c"/>
        <stop offset="100%" stop-color="#0097d8"/>
      </linearGradient>
    </defs>
    <rect width="400" height="400" fill="#f1f5f9"/>
    <!-- Metal Hardware Base -->
    <rect x="110" y="350" width="180" height="18" rx="4" fill="#64748b"/>
    <rect x="130" y="365" width="20" height="15" rx="2" fill="#475569"/>
    <rect x="250" y="365" width="20" height="15" rx="2" fill="#475569"/>
    <!-- Standee Screen -->
    <rect x="120" y="40" width="160" height="310" rx="3" fill="url(#jciBlue)" stroke="#cbd5e1" stroke-width="2"/>
    <circle cx="200" cy="100" r="30" fill="#ffffff" opacity="0.15"/>
    <text x="200" y="106" fill="#ffffff" font-family="Arial, sans-serif" font-weight="900" font-size="18" text-anchor="middle">JCI</text>
    <text x="200" y="170" fill="#38bdf8" font-family="Arial, sans-serif" font-weight="bold" font-size="13" text-anchor="middle">LEAD 360</text>
    <line x1="145" y1="185" x2="255" y2="185" stroke="#f59e0b" stroke-width="2"/>
    <text x="200" y="215" fill="#ffffff" font-family="Arial, sans-serif" font-weight="bold" font-size="11" text-anchor="middle">INSTALLATION</text>
    <text x="200" y="235" fill="#ffffff" font-family="Arial, sans-serif" font-weight="bold" font-size="11" text-anchor="middle">BANQUET 2027</text>
    <rect x="140" y="270" width="120" height="25" rx="12" fill="#f59e0b"/>
    <text x="200" y="287" fill="#001f3f" font-family="Arial, sans-serif" font-weight="bold" font-size="10" text-anchor="middle">OFFICIAL PARTNER</text>
  </svg>`;
  await sharp(Buffer.from(bannerSvg)).png().toFile('./public/assets/item_banner.png');

  // 3. Brochures & Flyers
  const brochureSvg = `<svg width="400" height="400" viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
    <rect width="400" height="400" fill="#fafafa"/>
    <!-- Folded Pamphlet perspective -->
    <polygon points="70,100 160,80 160,320 70,340" fill="#0097d8" stroke="#0077b6" stroke-width="2"/>
    <polygon points="160,80 250,100 250,340 160,320" fill="#ffffff" stroke="#cbd5e1" stroke-width="2"/>
    <polygon points="250,100 330,75 330,295 250,320" fill="#003566" stroke="#001d3d" stroke-width="2"/>
    <!-- Details on cover -->
    <text x="115" y="150" fill="#ffffff" font-family="Arial, sans-serif" font-weight="900" font-size="16" text-anchor="middle">JCI</text>
    <line x1="85" y1="180" x2="145" y2="170" stroke="#f59e0b" stroke-width="2"/>
    <rect x="85" y="200" width="55" height="6" rx="2" fill="#bae6fd"/>
    <rect x="85" y="215" width="55" height="6" rx="2" fill="#bae6fd"/>
    <rect x="85" y="230" width="40" height="6" rx="2" fill="#bae6fd"/>
    <!-- Inside fold details -->
    <text x="205" y="160" fill="#0f172a" font-family="Arial, sans-serif" font-weight="bold" font-size="12" text-anchor="middle">ACTION PLAN</text>
    <rect x="175" y="190" width="60" height="5" rx="2" fill="#94a3b8"/>
    <rect x="175" y="205" width="60" height="5" rx="2" fill="#94a3b8"/>
    <rect x="175" y="220" width="50" height="5" rx="2" fill="#94a3b8"/>
  </svg>`;
  await sharp(Buffer.from(brochureSvg)).png().toFile('./public/assets/item_brochure.png');

  // 4. Apparel & T-Shirt
  const apparelSvg = `<svg width="400" height="400" viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
    <rect width="400" height="400" fill="#f8fafc"/>
    <!-- Polo Shirt Path -->
    <path d="M130,100 L170,80 L230,80 L270,100 L320,150 L285,185 L260,165 L260,330 L140,330 L140,165 L115,185 L80,150 Z" fill="#005a9c" stroke="#003e6b" stroke-width="2"/>
    <!-- Collar -->
    <polygon points="170,80 200,140 200,100 170,80" fill="#003e6b"/>
    <polygon points="230,80 200,140 200,100 230,80" fill="#003e6b"/>
    <polygon points="170,80 200,115 230,80" fill="#ffffff"/>
    <!-- Buttons -->
    <circle cx="200" cy="150" r="3" fill="#ffffff"/>
    <circle cx="200" cy="170" r="3" fill="#ffffff"/>
    <!-- JCI Emblem Crest on Chest -->
    <rect x="220" y="160" width="22" height="14" rx="2" fill="#ffffff"/>
    <text x="231" y="171" fill="#005a9c" font-family="Arial, sans-serif" font-weight="900" font-size="8" text-anchor="middle">JCI</text>
  </svg>`;
  await sharp(Buffer.from(apparelSvg)).png().toFile('./public/assets/item_apparel.png');

  console.log('Vendor item product graphics generated successfully!');
}

generateImages().catch(console.error);

import fs from 'fs';

const filePath = 'mobile-android/app/src/main/java/com/jci2027/lead360/screens/AppScreens.kt';
let s = fs.readFileSync(filePath, 'utf8');

// 1. Update JciDigitalBusinessCard signature
const targetSignature = `fun JciDigitalBusinessCard(
    presidentName: String,
    role: String,
    chapterName: String,
    zone: String,
    phone: String,
    email: String,
    photoBitmap: Bitmap?,
    chapterLogoBitmap: Bitmap?,
    onPhotoClick: () -> Unit
)`;

const newSignature = `fun JciDigitalBusinessCard(
    presidentName: String,
    role: String,
    chapterName: String,
    zone: String,
    phone: String,
    email: String,
    photoBitmap: Bitmap?,
    chapterLogoBitmap: Bitmap?,
    photoServerUrl: String? = null,
    baseUrl: String = "https://lead.sriddha.com",
    onPhotoClick: () -> Unit
)`;

if (!s.includes(targetSignature)) {
  console.error("Target signature not found!");
  process.exit(1);
}
s = s.replace(targetSignature, newSignature);

// 2. Update photo display box
const targetPhotoBox = `                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(2.dp, Color(0xFF0097D7), CircleShape)
                                .clickable { onPhotoClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap.asImageBitmap(),
                                    contentDescription = "Portrait",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    presidentName.take(2).uppercase(),
                                    color = Color(0xFF0097D7),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }`;

const newPhotoBox = `                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(2.dp, Color(0xFF0097D7), CircleShape)
                                .clickable { onPhotoClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap.asImageBitmap(),
                                    contentDescription = "Portrait",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (!photoServerUrl.isNullOrBlank()) {
                                val fullUrl = if (photoServerUrl.startsWith("http")) photoServerUrl else "$baseUrl$photoServerUrl"
                                AsyncImage(
                                    model = fullUrl,
                                    contentDescription = "President Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    presidentName.take(2).uppercase(),
                                    color = Color(0xFF0097D7),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }`;

if (!s.includes(targetPhotoBox)) {
  console.error("Target photo box not found!");
  process.exit(1);
}
s = s.replace(targetPhotoBox, newPhotoBox);

// 3. Update call site in ProfileScreen
const targetCallSite = `        JciDigitalBusinessCard(
            presidentName = presidentNameInput,
            role = role,
            chapterName = chapterNameInput,
            zone = chapterZone,
            phone = presidentPhone,
            email = presidentEmail,
            photoBitmap = presidentBitmap,
            chapterLogoBitmap = chapterLogoBitmap,
            onPhotoClick = { presidentPhotoLauncher.launch("image/*") }
        )`;

const newCallSite = `        val serverPhotoUrl = prefs.getString("president_photo_server_url", null)
        JciDigitalBusinessCard(
            presidentName = presidentNameInput,
            role = role,
            chapterName = chapterNameInput,
            zone = chapterZone,
            phone = presidentPhone,
            email = presidentEmail,
            photoBitmap = presidentBitmap,
            chapterLogoBitmap = chapterLogoBitmap,
            photoServerUrl = serverPhotoUrl,
            onPhotoClick = { presidentPhotoLauncher.launch("image/*") }
        )`;

if (!s.includes(targetCallSite)) {
  console.error("Target call site not found!");
  process.exit(1);
}
s = s.replace(targetCallSite, newCallSite);

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated AppScreens.kt!');


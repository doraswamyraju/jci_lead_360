import fs from 'fs';

const filePath = 'mobile-android/app/src/main/java/com/jci2027/lead360/MainActivity.kt';
let s = fs.readFileSync(filePath, 'utf8');

// 1. Add AsyncImage import if needed
if (!s.includes('import coil.compose.AsyncImage')) {
  s = s.replace('import androidx.compose.material3.*', 'import androidx.compose.material3.*\nimport coil.compose.AsyncImage');
}

// 2. Add serverPhotoUrl state
const targetState = `                var userPhotoPath by remember { mutableStateOf(prefs.getString("president_photo_path", null)) }`;
const newState = `                var userPhotoPath by remember { mutableStateOf(prefs.getString("president_photo_path", null)) }
                var serverPhotoUrl by remember { mutableStateOf(prefs.getString("president_photo_server_url", null)) }`;

if (s.includes(targetState) && !s.includes('var serverPhotoUrl')) {
  s = s.replace(targetState, newState);
}

// 3. Update drawer avatar display
const targetAvatar = `                                            if (userProfileBitmap != null) {
                                                Image(
                                                    bitmap = userProfileBitmap.asImageBitmap(),
                                                    contentDescription = userName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            }`;

const newAvatar = `                                            if (userProfileBitmap != null) {
                                                Image(
                                                    bitmap = userProfileBitmap.asImageBitmap(),
                                                    contentDescription = userName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else if (!serverPhotoUrl.isNullOrBlank()) {
                                                val photoFullUrl = if (serverPhotoUrl!!.startsWith("http")) serverPhotoUrl!! else "https://lead.sriddha.com$serverPhotoUrl"
                                                AsyncImage(
                                                    model = photoFullUrl,
                                                    contentDescription = userName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(userName.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            }`;

if (s.includes(targetAvatar)) {
  s = s.replace(targetAvatar, newAvatar);
}

// 4. Update onLoginSuccess
const targetLoginSuccess = `                            userPhotoPath = prefs.getString("president_photo_path", null)
                            chapterLogoPath = prefs.getString("chapter_logo_path", null)`;

const newLoginSuccess = `                            userPhotoPath = prefs.getString("president_photo_path", null)
                            serverPhotoUrl = prefs.getString("president_photo_server_url", null)
                            chapterLogoPath = prefs.getString("chapter_logo_path", null)`;

if (s.includes(targetLoginSuccess)) {
  s = s.replace(targetLoginSuccess, newLoginSuccess);
}

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated MainActivity.kt!');


import express from 'express';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { ROLES } from '../../config/roles.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const uploadDir = path.join(__dirname, '../../../public/uploads');

// Ensure upload directory exists recursively with safety check
try {
  if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
  }
} catch (err) {
  console.error('[UploadDir] Error creating uploads root folder:', err);
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    try {
      if (!fs.existsSync(uploadDir)) {
        fs.mkdirSync(uploadDir, { recursive: true });
      }
      cb(null, uploadDir);
    } catch (e) {
      cb(e);
    }
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const cleanBase = path.basename(file.originalname, ext).replace(/[^a-zA-Z0-9]/g, '_').substring(0, 30);
    const uniqueName = `asset_${Date.now()}_${cleanBase}${ext}`;
    cb(null, uniqueName);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 50 * 1024 * 1024 }, // 50MB
  fileFilter: (req, file, cb) => {
    const allowedExts = ['.png', '.jpg', '.jpeg', '.webp', '.svg', '.jfif', '.avif', '.bmp'];
    const ext = path.extname(file.originalname).toLowerCase();
    const isImageMime = file.mimetype.startsWith('image/');

    if (allowedExts.includes(ext) || isImageMime) {
      cb(null, true);
    } else {
      console.warn(`[Upload Rejected] name: ${file.originalname}, ext: ${ext}, mime: ${file.mimetype}`);
      cb(new Error(`File "${file.originalname}" is not supported. Please upload a standard image (PNG, JPG, JPEG, WebP, SVG).`));
    }
  }
});

const router = express.Router();

/**
 * Upload Asset Endpoint (Protected for Super Admin and Employees)
 * Handles multer errors explicitly so the client receives clear JSON errors instead of unhandled 500 crashes
 */
router.post('/', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  upload.single('image')(req, res, (err) => {
    if (err instanceof multer.MulterError) {
      console.error('[Multer Error]:', err.message);
      return res.status(400).json({ success: false, message: `Upload error: ${err.message}` });
    } else if (err) {
      console.error('[Upload Error]:', err.message);
      return res.status(400).json({ success: false, message: err.message || 'Error processing uploaded file' });
    }

    if (!req.file) {
      return res.status(400).json({ success: false, message: 'No image file uploaded' });
    }

    const fileUrl = `/uploads/${req.file.filename}`;
    return res.json({
      success: true,
      fileUrl,
      filename: req.file.filename,
      size: req.file.size
    });
  });
});

export default router;

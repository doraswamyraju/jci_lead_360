/**
 * Standalone Client-Side Canvas Image Compositor Engine
 * Renders high-resolution 1080x1080 / 1080x1350 flyers in the browser
 * with custom fonts, dynamic coordinates, shapes, colors, alignment, and logo placement.
 */

export class CanvasRenderer {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
  }

  /**
   * Load an image source into an HTMLImageElement
   */
  static loadImage(src) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload = () => resolve(img);
      img.onerror = () => reject(new Error(`Failed to load image: ${src}`));
      img.src = src;
    });
  }

  /**
   * Main Render Pipeline: Base flyer + Dynamic Fields + Chapter Branding
   */
  async renderFlyer({ template, values, dimensions }) {
    const width = dimensions?.width || template.dimensions?.width || 1080;
    const height = dimensions?.height || template.dimensions?.height || 1080;

    this.canvas.width = width;
    this.canvas.height = height;

    // 1. Draw Background Image
    try {
      const baseImg = await CanvasRenderer.loadImage(template.baseImageUrl);
      this.ctx.drawImage(baseImg, 0, 0, width, height);
    } catch (e) {
      // Fallback gradient if base image fails
      const grad = this.ctx.createLinearGradient(0, 0, width, height);
      grad.addColorStop(0, '#0097D8');
      grad.addColorStop(1, '#002B49');
      this.ctx.fillStyle = grad;
      this.ctx.fillRect(0, 0, width, height);
    }

    // 2. Iterate through each configured layer/zone
    for (const layer of (template.layers || [])) {
      const x = (layer.x / (template.dimensions?.width || 1080)) * width;
      const y = (layer.y / (template.dimensions?.height || 1080)) * height;
      const w = (layer.width / (template.dimensions?.width || 1080)) * width;
      const h = (layer.height / (template.dimensions?.height || 1080)) * height;

      // Extract layer styling options
      const textColor = layer.textColor || '#ffffff';
      const textAlign = layer.textAlign || 'left';
      const fontSize = layer.fontSize || Math.max(16, Math.round(w * 0.065));
      const fontWeight = layer.fontWeight || '700';
      const hasShadow = layer.hasShadow !== false;
      const shape = layer.shape || (layer.type.includes('photo') ? 'circle' : 'rectangle');
      const borderWidth = layer.borderWidth ?? (layer.type.includes('photo') ? 4 : 0);
      const borderColor = layer.borderColor || '#ffffff';

      switch (layer.type) {
        case 'chapter_logo':
        case 'cohost_logo':
          const logoSrc = (layer.type === 'cohost_logo') ? (values.cohostLogo || values.chapterLogo) : values.chapterLogo;
          if (logoSrc) {
            try {
              const logo = await CanvasRenderer.loadImage(logoSrc);
              if (shape === 'circle') {
                this.drawCircleImage(logo, x, y, Math.min(w, h), borderWidth, borderColor);
              } else {
                this.drawContainImage(logo, x, y, w, h);
              }
            } catch (err) {
              console.warn('Logo render failed:', err);
            }
          }
          break;

        case 'president_photo':
        case 'chief_guest_photo':
        case 'secretary_photo':
          let photoUrl = values.presidentPhoto;
          if (layer.type === 'chief_guest_photo') photoUrl = values.chiefGuestPhoto || values.presidentPhoto;
          if (layer.type === 'secretary_photo') photoUrl = values.secretaryPhoto || values.presidentPhoto;

          if (photoUrl) {
            try {
              const photo = await CanvasRenderer.loadImage(photoUrl);
              if (shape === 'circle') {
                this.drawCircleImage(photo, x, y, Math.min(w, h), borderWidth, borderColor);
              } else {
                this.ctx.drawImage(photo, x, y, w, h);
                if (borderWidth > 0) {
                  this.ctx.strokeStyle = borderColor;
                  this.ctx.lineWidth = borderWidth;
                  this.ctx.strokeRect(x, y, w, h);
                }
              }
            } catch (err) {
              console.warn('Photo render failed:', err);
            }
          }
          break;

        case 'president_name':
          if (values.presidentName) {
            this.drawText(values.presidentName, x, y, w, h, {
              fontSize: layer.fontSize || Math.max(18, Math.round(w * 0.07)),
              fontWeight,
              color: textColor,
              align: textAlign,
              hasShadow
            });
          }
          break;

        case 'event_date_time':
          if (values.dateTime) {
            this.drawText(`📅 ${values.dateTime}`, x, y, w, h, {
              fontSize: layer.fontSize || Math.max(16, Math.round(w * 0.05)),
              fontWeight,
              color: textColor,
              align: textAlign,
              hasShadow
            });
          }
          break;

        case 'event_venue':
          if (values.venue) {
            this.drawText(`📍 ${values.venue}`, x, y, w, h, {
              fontSize: layer.fontSize || Math.max(16, Math.round(w * 0.05)),
              fontWeight,
              color: textColor,
              align: textAlign,
              hasShadow
            });
          }
          break;

        case 'chief_guest':
          if (values.chiefGuest) {
            this.drawText(values.chiefGuest, x, y, w, h, {
              fontSize: layer.fontSize || Math.max(16, Math.round(w * 0.055)),
              fontWeight,
              color: textColor,
              align: textAlign,
              hasShadow
            });
          }
          break;

        default:
          const textVal = values[layer.type] || layer.label || '';
          if (textVal) {
            this.drawText(textVal, x, y, w, h, {
              fontSize: layer.fontSize || 20,
              fontWeight,
              color: textColor,
              align: textAlign,
              hasShadow
            });
          }
      }
    }

    return this.canvas.toDataURL('image/png', 1.0);
  }

  /**
   * Helper: Render image maintaining aspect ratio within bounding box
   */
  drawContainImage(img, x, y, w, h) {
    const ratio = Math.min(w / img.naturalWidth, h / img.naturalHeight);
    const nw = img.naturalWidth * ratio;
    const nh = img.naturalHeight * ratio;
    const ox = x + (w - nw) / 2;
    const oy = y + (h - nh) / 2;
    this.ctx.drawImage(img, ox, oy, nw, nh);
  }

  /**
   * Helper: Render circular avatar cropped cleanly with custom border
   */
  drawCircleImage(img, x, y, size, borderWidth = 4, borderColor = '#ffffff') {
    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.arc(x + size / 2, y + size / 2, size / 2, 0, Math.PI * 2);
    this.ctx.closePath();
    this.ctx.clip();
    this.ctx.drawImage(img, x, y, size, size);
    this.ctx.restore();

    if (borderWidth > 0) {
      this.ctx.strokeStyle = borderColor;
      this.ctx.lineWidth = borderWidth;
      this.ctx.beginPath();
      this.ctx.arc(x + size / 2, y + size / 2, size / 2, 0, Math.PI * 2);
      this.ctx.stroke();
    }
  }

  /**
   * Helper: Draw wrapped text with dropshadow and precise alignment
   */
  drawText(text, x, y, maxWidth, maxHeight, { fontSize = 18, fontWeight = '600', color = '#ffffff', align = 'left', hasShadow = false }) {
    this.ctx.save();
    this.ctx.font = `${fontWeight} ${fontSize}px 'Outfit', 'Inter', -apple-system, sans-serif`;
    this.ctx.fillStyle = color;
    this.ctx.textAlign = align;
    this.ctx.textBaseline = 'top';

    if (hasShadow) {
      this.ctx.shadowColor = 'rgba(0, 0, 0, 0.85)';
      this.ctx.shadowBlur = 6;
      this.ctx.shadowOffsetX = 2;
      this.ctx.shadowOffsetY = 2;
    }

    // Determine target anchor X based on alignment
    let textAnchorX = x;
    if (align === 'center') {
      textAnchorX = x + (maxWidth / 2);
    } else if (align === 'right') {
      textAnchorX = x + maxWidth;
    }

    const words = text.split(' ');
    let line = '';
    let currentY = y;
    const lineHeight = fontSize * 1.35;

    for (let i = 0; i < words.length; i++) {
      const testLine = line + words[i] + ' ';
      const metrics = this.ctx.measureText(testLine);
      if (metrics.width > maxWidth && i > 0) {
        this.ctx.fillText(line.trim(), textAnchorX, currentY);
        line = words[i] + ' ';
        currentY += lineHeight;
        if (currentY + lineHeight > y + maxHeight) break;
      } else {
        line = testLine;
      }
    }
    this.ctx.fillText(line.trim(), textAnchorX, currentY);
    this.ctx.restore();
  }

  /**
   * Render Photo Inside JCI 2027 Frame Overlay
   */
  async renderFrameWithPhoto({ frame, userPhotoUrl, shape = 'circle' }) {
    const size = 1080;
    this.canvas.width = size;
    this.canvas.height = size;

    // 1. Draw user photo in the mask zone
    if (userPhotoUrl) {
      try {
        const photo = await CanvasRenderer.loadImage(userPhotoUrl);
        const { x, y, w, h } = frame.maskZone || { x: 240, y: 240, w: 600, h: 600 };
        if (shape === 'circle') {
          this.drawCircleImage(photo, x, y, Math.min(w, h));
        } else {
          this.ctx.drawImage(photo, x, y, w, h);
        }
      } catch (err) {
        console.warn('User photo load error:', err);
      }
    }

    // 2. Draw Frame Overlay on Top
    try {
      const frameImg = await CanvasRenderer.loadImage(frame.frameOverlayUrl);
      this.ctx.drawImage(frameImg, 0, 0, size, size);
    } catch (e) {
      console.warn('Frame overlay load error:', e);
    }

    return this.canvas.toDataURL('image/png', 1.0);
  }
}

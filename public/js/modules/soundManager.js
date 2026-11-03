/**
 * Standalone Audio Notification Synthesizer
 * Uses Web Audio API to play pleasant, modern chimes & bells
 * without depending on external MP3 files or network latency.
 */

class AudioNotificationService {
  constructor() {
    this.audioCtx = null;
  }

  init() {
    if (!this.audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.audioCtx = new AudioContext();
      }
    }
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  /**
   * Play crystal-clear modern order alert chime
   * Frequencies: C6 (1046Hz) -> E6 (1318Hz) -> G6 (1567Hz)
   */
  playOrderChime() {
    try {
      this.init();
      if (!this.audioCtx) return;

      const now = this.audioCtx.currentTime;
      const notes = [
        { freq: 1046.50, start: 0.0, dur: 0.18 }, // C6
        { freq: 1318.51, start: 0.12, dur: 0.18 }, // E6
        { freq: 1567.98, start: 0.24, dur: 0.35 }  // G6
      ];

      notes.forEach(note => {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();

        osc.type = 'sine';
        osc.frequency.setValueAtTime(note.freq, now + note.start);

        gain.gain.setValueAtTime(0.001, now + note.start);
        gain.gain.exponentialRampToValueAtTime(0.25, now + note.start + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + note.start + note.dur);

        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.start(now + note.start);
        osc.stop(now + note.start + note.dur);
      });
    } catch (e) {
      console.warn('Audio chime warning:', e);
    }
  }

  /**
   * Play bell ding for RFQs / updates
   */
  playBellDing() {
    try {
      this.init();
      if (!this.audioCtx) return;

      const now = this.audioCtx.currentTime;
      const osc = this.audioCtx.createOscillator();
      const gain = this.audioCtx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(880, now); // A5

      gain.gain.setValueAtTime(0.2, now);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.4);

      osc.connect(gain);
      gain.connect(this.audioCtx.destination);

      osc.start(now);
      osc.stop(now + 0.4);
    } catch (e) {
      console.warn('Audio bell warning:', e);
    }
  }
}

export const soundManager = new AudioNotificationService();

// User gesture unlock for browser autoplay policies
window.addEventListener('click', () => soundManager.init(), { once: true });

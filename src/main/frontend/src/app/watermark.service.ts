import { Injectable } from '@angular/core';
import {UserService} from "./service/user.service";

export interface WatermarkUserInfo {
  id?: any;
  email?: string;
  ip?: string;
  assignmentID?: any
}

@Injectable({ providedIn: 'root' })
export class WatermarkService {
  private overlay?: HTMLDivElement;
  private timer?: any;
  private user: WatermarkUserInfo = {};
  private extraLines: string[] = [];
  private persistent = false;

  constructor(private vio: UserService) {}

  async initUser(info: Partial<WatermarkUserInfo>) {
    this.user = { ...this.user, ...info };
    if (!this.user.ip) {
      try {
        const res = await fetch('https://api.ipify.org?format=json');
        const data = await res.json();
        this.user.ip = data?.ip;
      } catch {
        // ignore
      }
    }
  }

  setExtraLines(lines: string[]) {
    this.extraLines = Array.isArray(lines) ? lines : [];
    this.render();
  }

  showOverlay(options?: { persistent?: boolean; blockPointer?: boolean }) {
    this.persistent = !!options?.persistent;
    this.ensureOverlay();
    if (!this.overlay) return;
    this.overlay.style.display = 'grid';
    this.overlay.style.pointerEvents = options?.blockPointer ? 'auto' : 'none';
    this.startTick();
    this.render();
  }

  hideOverlay() {
    if (this.persistent) return; // nếu persistent, không tự ẩn
    if (this.overlay) this.overlay.style.display = 'none';
    this.stopTick();
  }

  flashOverlay(ttlMs = 3000, blockPointer = false) {
    this.persistent = false;
    this.showOverlay({ persistent: false, blockPointer });
    const desc = `Email=${this.user.email || ''}; IP=${this.user.ip || ''}; time=${new Date().toISOString()}; agent=${navigator.userAgent}`;
    this.vio.createViolation({
      assignmentID: this.user.assignmentID,
      typeViolation: 'OUT_SCREEN',
      description: desc
    }).subscribe();
    window.setTimeout(() => this.hideOverlay(), ttlMs);
  }

  private ensureOverlay() {
    if (this.overlay && document.body.contains(this.overlay)) return;

    const div = document.createElement('div');
    div.className = 'exam-watermark-overlay';
    // Base styles
    Object.assign(div.style, {
      position: 'fixed',
      inset: '0',
      zIndex: '2147483647',
      display: 'none',
      alignItems: 'center',
      justifyContent: 'center',
      textAlign: 'center',
      color: 'rgba(0,0,0,0.25)',
      fontSize: '50px',
      fontWeight: '600',
      whiteSpace: 'pre-line',
      lineHeight: '1.6',
      background:
        'repeating-linear-gradient(45deg, transparent 0 30px, rgba(0,0,0,0.05) 30px 60px)',
      backdropFilter: 'blur(1px)',
    } as CSSStyleDeclaration);

    document.body.appendChild(div);
    this.overlay = div;
  }

  private startTick() {
    this.stopTick();
    this.timer = window.setInterval(() => this.render(), 1000);
  }

  private stopTick() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }

  private render() {
    if (!this.overlay) return;
    const lines = [
      this.user.id ? `ID: ${this.user.id}` : '',
      this.user.email ? `Email: ${this.user.email}` : '',
      this.user.ip ? `IP: ${this.user.ip}` : '',
      ...this.extraLines,
    ].filter(Boolean);

    // Tạo grid watermark dày hơn bằng cách lặp nội dung
    const block = lines.join('   -   ');
    const repeated = Array.from({ length: 1 })
      .map(() => block)
      .join('\n\n');

    this.overlay.textContent = repeated;
  }
}

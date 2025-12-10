import { Directive, HostListener, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import {WatermarkService} from "./watermark.service";

@Directive({
  selector: '[appAntiCheat]',
  standalone: true, // nếu component của bạn là standalone, có thể import trực tiếp
})
export class AntiCheatDirective {
  private violationFlashMs = 3500;

  constructor(
    private watermark: WatermarkService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  // Chuột phải, drag, clipboard
  @HostListener('contextmenu', ['$event'])
  onContextMenu(e: Event) { e.preventDefault(); }

  @HostListener('dragstart', ['$event'])
  onDragStart(e: Event) { e.preventDefault(); }

  @HostListener('copy', ['$event'])
  onCopy(e: ClipboardEvent) { e.preventDefault(); }

  @HostListener('cut', ['$event'])
  onCut(e: ClipboardEvent) { e.preventDefault(); }

  @HostListener('paste', ['$event'])
  onPaste(e: ClipboardEvent) { e.preventDefault(); }

  // Phím tắt phổ biến + PrintScreen
  @HostListener('keydown', ['$event'])
  async onKeydown(e: KeyboardEvent) {
    const k = (e.key || '').toLowerCase();
    const ctrl = e.ctrlKey || e.metaKey;

    if (
      (ctrl && ['p','s','c','v','x','a','u'].includes(k)) ||
      k === 'f12' ||
      (e.ctrlKey && e.shiftKey && ['i','j','c','k'].includes(k))
    ) {
      e.preventDefault();
      e.stopPropagation();
    }

    if (k === 'printscreen') {
      e.preventDefault();
      e.stopPropagation();
      // Cố gắng “bẩn” clipboard (chỉ hiệu quả phần nào, HTTPS + quyền)
      try { await navigator.clipboard.writeText('Screenshots are not allowed during the exam.'); } catch {}
      // Bật watermark phủ toàn màn
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  // Khi thoát fullscreen hoặc ẩn tab, cũng bật watermark (răn đe)
  @HostListener('document:visibilitychange')
  onVisibilityChange() {
    if (this.document.visibilityState === 'hidden') {
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  @HostListener('document:fullscreenchange')
  onFullscreenChange() {
    if (!this.document.fullscreenElement) {
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }
}

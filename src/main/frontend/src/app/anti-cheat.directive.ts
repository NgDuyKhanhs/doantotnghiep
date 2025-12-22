import { Directive, HostListener, Inject, OnDestroy } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { WatermarkService } from "./watermark.service";

@Directive({
  selector: '[appAntiCheat]',
  standalone: true,
})
export class AntiCheatDirective implements OnDestroy {
  private violationFlashMs = 3500;

  // heuristic devtools detection
  private devtoolsCheckInterval: any = null;
  private devtoolsOpen = false;

  constructor(
    private watermark: WatermarkService,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.startDevtoolsDetector();
  }

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

  // Phím tắt phổ biến + PrintScreen + F12
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
      // show watermark as warning for attempts using developer shortcuts
      this.watermark.flashOverlay(this.violationFlashMs);
    }

    // PrintScreen handling (some browsers/platforms report 'PrintScreen', others 'Print' etc.)
    if (k === 'printscreen' || k === 'print' || k === 'prtsc') {
      e.preventDefault();
      e.stopPropagation();
      try { await navigator.clipboard.writeText('Screenshots are not allowed during the exam.'); } catch {}
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  // Some systems may only fire keyup for PrintScreen — handle it too
  @HostListener('keyup', ['$event'])
  async onKeyup(e: KeyboardEvent) {
    const k = (e.key || '').toLowerCase();
    if (k === 'printscreen' || k === 'print' || k === 'prtsc') {
      try { await navigator.clipboard.writeText('Screenshots are not allowed during the exam.'); } catch {}
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  // Khi thoát fullscreen hoặc ẩn tab, cũng bật watermark (răn đe)
  @HostListener('document:visibilitychange')
  onVisibilityChange() {
    if (this.document.visibilityState === 'hidden' || (this.document as any).hidden) {
      // tab hidden ( đổi tab / minimize / switch window )
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  @HostListener('document:fullscreenchange')
  onFullscreenChange() {
    if (!this.document.fullscreenElement) {
      this.watermark.flashOverlay(this.violationFlashMs);
    }
  }

  // window blur = lose focus (Alt+Tab, click outside, switch apps)
  @HostListener('window:blur')
  onWindowBlur() {
    this.watermark.flashOverlay(this.violationFlashMs);
  }

  // Optional: when user returns
  @HostListener('window:focus')
  onWindowFocus() {
    // you might want to log or notify that user returned
  }

  private startDevtoolsDetector() {
    // Heuristic: when DevTools is opened the outerInner diffs usually change a lot.
    // This is only heuristic and not reliable on all browsers/platforms.
    const widthThreshold = 160;
    const heightThreshold = 160;
    this.devtoolsCheckInterval = setInterval(() => {
      try {
        const opened = (window.outerWidth - window.innerWidth > widthThreshold)
          || (window.outerHeight - window.innerHeight > heightThreshold);

        if (opened && !this.devtoolsOpen) {
          this.devtoolsOpen = true;
          // Detected likely devtools open -> warn visually
          this.watermark.flashOverlay(this.violationFlashMs);
          // optional: also attempt to clear the console or print message
          console.warn('Developer tools detected — action recorded.');
        } else if (!opened && this.devtoolsOpen) {
          this.devtoolsOpen = false;
        }
      } catch (err) {
        // ignore cross-origin or other errors
      }
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.devtoolsCheckInterval) {
      clearInterval(this.devtoolsCheckInterval);
      this.devtoolsCheckInterval = null;
    }
  }
}

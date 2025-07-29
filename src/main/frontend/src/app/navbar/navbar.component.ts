import {AfterViewInit, Component, EventEmitter, HostListener, Output, Renderer2} from '@angular/core';
import {TokenService} from "../service/token.service";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements AfterViewInit {
  @Output() toggleSidebar1 = new EventEmitter<void>();
  sidebarOpen = false;
  dropdownOpen = false;
  dropdownOpen1 = false;
  constructor(private renderer: Renderer2,
              public token: TokenService) {
  }

  ngAfterViewInit(): void {
    this.setFullHeight();
  }

  toggleSidebarClicked() {
    this.toggleSidebar1.emit();
  }

  @HostListener('window:resize')
  onWindowResize() {
    this.setFullHeight();
  }

  setFullHeight(): void {
    const elements = document.querySelectorAll<HTMLElement>('.js-fullheight');
    const height = window.innerHeight;
    elements.forEach(el => {
      this.renderer.setStyle(el, 'height', `${height}px`);
    });
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  toggleDropdown1(): void {
    this.dropdownOpen1 = !this.dropdownOpen1;
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
      sidebar.classList.toggle('active');
    }
  }
}

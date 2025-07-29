import {AfterViewInit, Component, HostListener, Renderer2} from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements AfterViewInit{
  sidebarOpen = false;
  constructor(private renderer: Renderer2) {}

  ngAfterViewInit(): void {
    this.setFullHeight();
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

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
      sidebar.classList.toggle('active');
    }
  }

}

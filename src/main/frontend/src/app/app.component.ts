import {AfterViewInit, Component, HostListener, OnDestroy, OnInit, Renderer2} from '@angular/core';
import {Subscription} from 'rxjs';
import {ChatService, Message} from './service/chat.service';
import {RouterOutlet} from "@angular/router";
import {animate, style, transition, trigger} from "@angular/animations";
import {AuthService} from "./service/auth.service";
import {TokenService} from "./service/token.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [
    trigger('routeAnimations', [
      transition('LoginComponent => RegisterComponent, RegisterComponent => ChatBoxComponent, LoginComponent => ChatBoxComponent', [
        style({opacity: 0, transform: 'translateX(-100%)'}),
        animate('150ms ease-in-out', style({opacity: 1, transform: 'translateX(0%)'})),
      ]),
      transition('ChatBoxComponent => RegisterComponent, RegisterComponent => LoginComponent, ChatBoxComponent => LoginComponent', [
        style({opacity: 0, transform: 'translateX(100%)'}),
        animate('150ms ease-in-out', style({opacity: 1, transform: 'translateX(0%)'})),
      ]),
      transition('* <=> *', [
        style({opacity: 0}),
        animate('150ms ease-in-out', style({opacity: 1})),
      ]),
    ]),
  ],
})
export class AppComponent implements AfterViewInit{
  sidebarOpen = false;
  selectedTab: string = '';
  constructor(public authService: AuthService,
              private renderer: Renderer2,
              public token: TokenService) {
  }

  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData['animation'];
  }

  selectTab(tabId: string) {
    this.selectedTab = tabId;
  }
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

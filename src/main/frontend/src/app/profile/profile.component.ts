import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {NgxSpinnerService} from 'ngx-spinner';
import {Location} from "@angular/common";
import {MessageService} from "primeng/api";
import {AuthService} from "../service/auth.service";
import {TokenService} from "../service/token.service";
import {UserService} from "../service/user.service";
import {CookieService} from "ngx-cookie-service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  idUser: any;
  currentRole: any;
  currentIDUser: any;
  request: any = {};
  user: any = {};
  selectedTab: any = 'infors';

  constructor(public authService: AuthService,
              private tokenService: TokenService,
              private route: ActivatedRoute,
              private router: Router,
              private spinner: NgxSpinnerService,
              private cookieService: CookieService,
              private messageService: MessageService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.currentRole = this.tokenService.getRoleFromToken();
    this.getProfile()
  }

  getProfile() {
    this.user = this.route.snapshot.data['user'];
  }

  selectTab(tabId: string) {
    this.selectedTab = tabId;
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.cookieService.delete('accessToken', '/');
      this.router.navigate(['/dang-nhap'])
    })
  }

  protected readonly onabort = onabort;
}

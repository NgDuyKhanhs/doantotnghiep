import {Component, OnInit} from '@angular/core';
import {AuthService} from "../service/auth.service";
import {Router} from "@angular/router";
import {CookieService} from "ngx-cookie-service";
import {MessageService} from "primeng/api";
import {NgxSpinnerService} from "ngx-spinner";
import {TokenService} from "../service/token.service";
import {catchError, of} from "rxjs";
import {NotificationService} from "../service/notification.service";
import {Location} from "@angular/common";
import {UserService} from "../service/user.service";
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  user: any = {};
  currentRole: any;
  syslog: any = {};
  constructor(private authService: AuthService,
              private router: Router,
              private cookieService: CookieService,
              private messageService: MessageService,
              private spinner: NgxSpinnerService,
              private tokenService: TokenService,
              private location: Location,
              private notificationService: NotificationService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.currentRole = this.tokenService.getRoleFromToken();
  }

  login() {
    this.spinner.show();
    this.authService.loginUser(this.user).pipe(
      catchError(error => {
        this.notificationService.showFail('Đăng nhập thất bại: ' + (error.message || 'Lỗi không xác định'));
        this.spinner.hide();
        return of('Đăng nhập thất bại');
      })
    ).subscribe(res => {
      if (res && res.accessToken) {
        this.cookieService.set('accessToken', res.accessToken, {path: '/'});
        this.notificationService.showSuccess('Đăng nhập thành công');
        this.spinner.hide();
        setTimeout(()=> {
          this.router.navigate([`thong-tin-ca-nhan`]);
        },500)
      } else {
        this.notificationService.showFail('Đăng nhập thất bại');
      }
    });
  }

}

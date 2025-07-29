import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TokenService} from "../service/token.service";
import {MessageService} from "primeng/api";
import {AuthService} from "../service/auth.service";
import {catchError, of} from "rxjs";
import {NotificationService} from "../service/notification.service";
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit{
  user: any = {};
  classes: any = [];
  currentRole: any;
  selectedClass : any;
  constructor(private router: Router,
              private tokenService: TokenService,
              private notificationService: NotificationService,
              private authService: AuthService,
              private userService: UserService) {

  }

  register() {
    this.user.classId = this.selectedClass;
    this.authService.register(this.user).pipe(
      // Xử lý lỗi trong pipeline
      catchError(error => {
        console.log(error)
        return of('Đăng ký thất bại. Vui lòng thử lại!');
      })
    ).subscribe(res => {
      if (res) {
        this.notificationService.showSuccess('Đăng ký thành công! Hãy kiểm tra email của bạn');

        // this.router.navigate(['/login']);
      }
    });
  }

  ngOnInit(): void {
    this.userService.viewAllClasses().subscribe(res => {
      this.classes = res;
      this.selectedClass = this.classes[0].id;
    })

  }
}

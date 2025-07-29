import {Component, OnInit} from '@angular/core';
import {NotificationService} from "../service/notification.service";
import {AuthService} from "../service/auth.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-active-account',
  templateUrl: './active-account.component.html',
  styleUrls: ['./active-account.component.scss']
})
export class ActiveAccountComponent implements OnInit{
  activeToken: any;
  isActivating = true;

  constructor(private notificationService: NotificationService,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.activeToken = this.route.snapshot.paramMap.get('token');
    if (this.activeToken) {
      this.activateAccount(this.activeToken);
    } else {
      this.isActivating = false;
      this.activeToken = null;
    }
  }

  activateAccount(token: any): void {
    this.authService.activeToken(token).subscribe(res => {
      this.notificationService.showSuccess('Tài khoản kích hoạt thành công');

    }, error => {
      this.notificationService.showFail('Tài khoản kích hoạt thất bại');
    })
  }
}

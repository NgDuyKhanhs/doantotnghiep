import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {MessageService} from "primeng/api";
import {NotificationService} from "../service/notification.service";
import {TokenService} from "../service/token.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-registerlearn',
  templateUrl: './registerlearn.component.html',
  styleUrls: ['./registerlearn.component.scss']
})
export class RegisterlearnComponent implements OnInit {
  enrollments: any = [];
  isAnySelected = false;
  type: any = 'Chưa đăng ký'
  isCreateRoute = false;
  constructor(private userService: UserService,
              private messageService: MessageService,
              private notify: NotificationService,
              public token: TokenService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.loadEnrollments();
    this.router.events.subscribe(() => {
      const currentUrl = this.router.url;
      this.isCreateRoute = currentUrl.includes('/dang-ky-hoc/them-moi');
    });
  }

  onCheckboxChange() {
    this.isAnySelected = this.enrollments.some((e: { selected: any; }) => e.selected);
  }

  loadEnrollments() {
    if (this.token.getRoleFromToken() == 'ROLE_USER') {
      this.userService.getFilterEnrollments(this.type).subscribe(data => {
        this.enrollments = data;
        this.enrollments.forEach((item: { selected: boolean; }) => {
          item.selected = false;
        })
      });
    } else if (this.token.getRoleFromToken() == 'ROLE_ADMIN') {
      this.userService.getAllEnrollments().subscribe(data => {
        this.enrollments = data;
        this.enrollments.forEach((item: { selected: boolean; }) => {
          item.selected = false;
        })
      });
    }

  }

  registerSelectedCourses() {
    const selected = this.enrollments
      .filter((e: any) => e.selected).map((e: any) => e.enrollId);

    if (selected.length === 0) return;

    this.userService.registerEnrollment(selected).subscribe({
      next: (res) => {
        this.notify.showSuccess(res.message);
        this.loadEnrollments();
      },
      error: (err) => {
        this.notify.showFail(err.message);
      }
    });
  }

  getDataSearch() {
    this.userService.getFilterEnrollments(this.type).subscribe(res => {
      this.enrollments = res;
    })
  }

  // navigate(){
  //   this.router.navigate(['them-moi']);
  // }

}

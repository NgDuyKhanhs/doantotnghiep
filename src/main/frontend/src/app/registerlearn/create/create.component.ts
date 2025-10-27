import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {DomSanitizer} from "@angular/platform-browser";
import {NotificationService} from "../../service/notification.service";
import {Router} from "@angular/router";
import { DatePipe } from '@angular/common';
@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.scss']
})
export class CreateComponent implements OnInit {
  teachers: any = [];
  selectedTeacher: any = {};
  courses: any = []
  selectedCourse: any = {};
  classes: any = []
  previewUrl: string | ArrayBuffer | null = null;
  imageFiles: File[] = [];
  imagePreviewUrls: any = [];
  enrollmentDTO: any = {};
  fromDate: any;
  toDate: any;
  available: any = 0;
  locked: any = true;
  constructor(private userService: UserService,
              private sanitizer: DomSanitizer,
              private notify: NotificationService,
              private router: Router,
              private datePipe: DatePipe
              ) {
  }

  ngOnInit(): void {
    this.selectedCourse.user = {}
  }

  // getListTeacher() {
  //   if (this.teachers.length == 0) {
  //     this.userService.getAllTeachers().subscribe(res => {
  //       this.teachers = res;
  //     })
  //   }
  // }

  getListCourse() {
    if (this.courses.length == 0) {
      this.userService.getAllCurriculum().subscribe(res => {
        this.courses = res;
      })
    }
  }

  register() {
    this.enrollmentDTO.available = this.available;
    this.enrollmentDTO.lockWhenFull = this.locked;
    this.enrollmentDTO.courseId = this.toDate;
    this.enrollmentDTO.startTime = this.datePipe.transform(this.fromDate, 'yyyy-MM-dd HH:mm:ss');
    this.enrollmentDTO.endTime = this.datePipe.transform(this.toDate, 'yyyy-MM-dd HH:mm:ss');
    if (this.selectedCourse != null) {
      this.enrollmentDTO.courseId = this.selectedCourse.courseId;
    }

    this.userService.uploadEnrollment(this.enrollmentDTO).subscribe({
      next: (res) => {
        this.notify.showSuccess(res.message);
        setTimeout(()=> {
          this.router.navigate(['dang-ky-hoc']);
        },2000)
      },
      error: (err) => {
        this.notify.showFail(err.message);
      }
    });
  }
}

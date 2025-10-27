import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {DomSanitizer} from "@angular/platform-browser";
import {NotificationService} from "../../service/notification.service";
import {Router} from "@angular/router";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-new-chaper',
  templateUrl: './new-chaper.component.html',
  styleUrls: ['./new-chaper.component.scss']
})
export class NewChaperComponent implements OnInit{
  teachers: any = [];
  selectedTeacher: any = {};
  courseDTO: any = {};
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
  selectedClass : any;
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

  getListTeacher() {
    if (this.teachers.length == 0) {
      this.userService.getAllTeachers().subscribe(res => {
        this.teachers = res;
      })
    }
  }

  getListClasses() {
    if (this.classes.length == 0) {
      this.userService.viewAllClasses().subscribe(res => {
        this.classes = res;
      })
    }
  }

  // getListCourse() {
  //   if (this.courses.length == 0) {
  //     this.userService.getAllCurriculum().subscribe(res => {
  //       this.courses = res;
  //     })
  //   }
  // }



  private convertToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        resolve(reader.result as string);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];

    if (file) {
      this.imageFiles.push(file);
      const objectURL = URL.createObjectURL(file);
      this.imagePreviewUrls = [
        this.sanitizer.bypassSecurityTrustUrl(objectURL) as string
      ];
    }
  }

  async create() {
    if (this.imageFiles.length > 0) {
      this.courseDTO.banner = await this.convertToBase64(this.imageFiles[0]);
    }
    this.courseDTO.userId = this.selectedTeacher.id;
    this.courseDTO.classId = this.selectedClass.id;
    this.courseDTO.name = this.selectedCourse.name;
    this.courseDTO.credits = this.selectedCourse.credits
    this.userService.uploadCourse(this.courseDTO).subscribe(res => {
      if(res.message.includes("thành công")) {
        this.notify.showSuccess(res.message);
        setTimeout(() => {
          this.router.navigate(['/chuong-trinh-hoc']);
        },500)
      }
    })
  }
}

import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.scss']
})
export class CreateComponent implements OnInit {
  teachers: any = [];
  selectedTeacher: any = {};
  courses: any = []
  selectedCourse: any= {};
  classes: any = []
  previewUrl: string | ArrayBuffer | null = null;
  imageFiles: File[] = [];
  imagePreviewUrls: any = [];
  uploadEnrollmentReq: any = {};
  constructor(private userService: UserService, private sanitizer: DomSanitizer,) {
  }

  ngOnInit(): void {
  }

  getListTeacher() {
    if (this.teachers.length == 0) {
      this.userService.getAllTeachers().subscribe(res => {
        this.teachers = res;
      })
    }
  }

  getListCourse() {
    if (this.courses.length == 0) {
      this.userService.getAllCurriculum().subscribe(res => {
        this.courses = res;
      })
    }
  }

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
      this.imageFiles = [file];
      const objectURL = URL.createObjectURL(file);
      this.imagePreviewUrls = [
        this.sanitizer.bypassSecurityTrustUrl(objectURL) as string
      ];
    }
  }

  // register() {
  //   if(this.selectedCourse != null) {
  //     this.uploadEnrollmentReq.courseDTO = this.selectedCourse;
  //   }
  //   this.userService.uploadEnrollment()
  // }
}

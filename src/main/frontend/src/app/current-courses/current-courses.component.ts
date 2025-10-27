import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-current-courses',
  templateUrl: './current-courses.component.html',
  styleUrls: ['./current-courses.component.scss']
})
export class CurrentCoursesComponent implements OnInit {
  courses: any = [];
  constructor(private router: Router,
              private userService: UserService) {
  }

  navigate(idEnroll: any, idTeacher: any) {
    this.router.navigate([`/chi-tiet-khoa-hoc/${idEnroll}/${idTeacher}/noi-dung`]);
  }
  ngOnInit() {
    this.userService.getRegisteredCourses().subscribe(res => {
      this.courses = res;
      console.log(res)
    })
  }

}

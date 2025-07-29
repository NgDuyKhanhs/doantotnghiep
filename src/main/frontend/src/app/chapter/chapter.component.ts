import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {TokenService} from "../service/token.service";

@Component({
  selector: 'app-chapter',
  templateUrl: './chapter.component.html',
  styleUrls: ['./chapter.component.scss']
})
export class ChapterComponent implements OnInit{
  page?: number | any;
  itemsPerPage: any
  courses: any = {};
  dataShow: any[] = [];
  constructor(private userService: UserService,
              public token:TokenService) {
  }

  ngOnInit(): void {
    this.page = 1
    this.itemsPerPage = 10;
    this.getAllCourse();
  }

  getAllCourse() {
    this.userService.getAllCurriculum().subscribe(res => {
      this.courses = res;
      this.pageable();
    })
  }
  loadPage(page: any) {
    this.page = page;
    this.pageable();
  }

  pageable() {
    this.dataShow = [];
    for (let i = (this.page - 1) * this.itemsPerPage; i < this.courses.length; i++) {
      if (i < this.page * this.itemsPerPage) {
        this.dataShow.push(this.courses[i]);
      }
    }
  }
}

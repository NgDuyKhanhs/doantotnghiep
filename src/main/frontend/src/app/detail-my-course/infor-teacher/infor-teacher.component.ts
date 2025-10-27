import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../service/user.service";

@Component({
  selector: 'app-infor-teacher',
  templateUrl: './infor-teacher.component.html',
  styleUrls: ['./infor-teacher.component.scss']
})
export class InforTeacherComponent implements OnInit {
  id: any;
  teacher: any = {};
  constructor(private route: ActivatedRoute,
              private router: Router,
              private userService: UserService) {
  }

  ngOnInit() {
    this.route.parent?.params.subscribe(params => {
      this.id = params['idTeacher'];
    })
    this.getInfoTeacher();
  }

  getInfoTeacher() {
    this.userService.getInfoTeacherByID(this.id).subscribe(res => {
      this.teacher = res;
    })
  }

}

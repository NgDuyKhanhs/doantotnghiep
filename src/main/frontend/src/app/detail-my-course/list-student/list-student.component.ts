import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../../service/user.service";

@Component({
  selector: 'app-list-student',
  templateUrl: './list-student.component.html',
  styleUrls: ['./list-student.component.scss']
})
export class ListStudentComponent implements OnInit{
  id: any;
  users: any = [];
  constructor(private route: ActivatedRoute,
              private router: Router,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.route.parent?.params.subscribe(params => {
      this.id = params['idEnroll'];
    })
    this.getListUser();
  }
  getListUser(){
    this.userService.getListUserFromEnrollment(this.id).subscribe(res => {
      this.users = res;
    })
  }
}

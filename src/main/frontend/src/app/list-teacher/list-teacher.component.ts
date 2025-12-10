import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {RefModalService} from "../ref-modal.service";
import {CreateAssignmentsComponent} from "../modals/create-assignments/create-assignments.component";
import {DetailInfoComponent} from "../modals/detail-info/detail-info.component";

@Component({
  selector: 'app-list-teacher',
  templateUrl: './list-teacher.component.html',
  styleUrls: ['./list-teacher.component.scss']
})
export class ListTeacherComponent implements OnInit{
  users: any = [];
  className: any = 'Công nghệ thông tin';
  refModal: any;
  constructor(private userService: UserService,
              private refModalService: RefModalService,) {
  }
  openTabView(id: any) {
    this.refModal = this.refModalService.open(id, DetailInfoComponent, null, false, null, null, null, null, null, true);
  }
  ngOnInit() {
    this.filterListUsers();
  }

  filterListUsers() {
    this.userService.getAllTeachers().subscribe(res => {
      this.users = res;
    })
  }
}

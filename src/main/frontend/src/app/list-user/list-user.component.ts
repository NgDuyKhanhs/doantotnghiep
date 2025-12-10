import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {DetailInfoComponent} from "../modals/detail-info/detail-info.component";
import {RefModalService} from "../ref-modal.service";

@Component({
  selector: 'app-list-user',
  templateUrl: './list-user.component.html',
  styleUrls: ['./list-user.component.scss']
})
export class ListUserComponent implements OnInit {
  users: any = [];
  className: any = 'Công nghệ thông tin';
  refModal: any;

  constructor(private userService: UserService, private refModalService: RefModalService,) {
  }

  ngOnInit() {
    this.filterListUsers();
  }

  filterListUsers() {
    this.userService.getListUser(this.className).subscribe(res => {
      this.users = res;
    })
  }

  openTabView(id: any) {
    this.refModal = this.refModalService.open(id, DetailInfoComponent, null, false, null, null, null, null, null, true);
  }
}

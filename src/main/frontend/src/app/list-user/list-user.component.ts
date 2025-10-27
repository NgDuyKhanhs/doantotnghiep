import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-list-user',
  templateUrl: './list-user.component.html',
  styleUrls: ['./list-user.component.scss']
})
export class ListUserComponent implements OnInit{
  users: any = [];
  className: any = 'Công nghệ thông tin';
  constructor(private userService: UserService) {
  }

  ngOnInit() {
    this.filterListUsers();
  }

  filterListUsers() {
    this.userService.getListUser(this.className).subscribe(res => {
      this.users = res;
    })
  }
}

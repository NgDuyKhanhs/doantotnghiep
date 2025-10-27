import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {TokenService} from "../../service/token.service";

@Component({
  selector: 'app-action',
  templateUrl: './action.component.html',
  styleUrls: ['./action.component.scss']
})
export class ActionComponent implements OnInit{
  syslog: any;
  syslogs: any = [];
  currentRole: any;
  user : any;
  selectedRows: any[] = [];
  selectedRow: any;
  itemsPerPage: any
  page?: number | any;
  dataShow: any[] = [];
  isEditing = false;
  constructor(public userService: UserService,
              private router: Router,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              public tokenService: TokenService) {
  }

  ngOnInit(): void {
    this.currentRole = this.tokenService.getRoleFromToken();
    this.page = 1
    this.itemsPerPage = 10;
    this.user = this.route.parent?.snapshot.data['user'];
    console.log(this.user)
    this.getSysLog()
  }

  getSysLog() {
     if (this.currentRole == 'ROLE_USER' || this.currentRole == 'ROLE_TEACHER') {
      this.userService.getSysLog().subscribe(res => {
        this.syslogs = res;
        this.dataShow = this.syslogs
        console.log(this.dataShow)
        this.pageable()
        // @ts-ignore
      })
    }

  }

  pageable() {
    this.dataShow = [];
    for (let i = (this.page - 1) * this.itemsPerPage; i < this.syslogs.length; i++) {
      if (i < this.page * this.itemsPerPage) {
        this.dataShow.push(this.syslogs[i]);
      }
    }
  }
  loadPage(page: any) {
    this.page = page;
    this.pageable();
  }

  selectRow(object: any, evt: any, objects: any[]) {
    this.selectedRows = [];
    this.selectedRows.push(object);
    this.selectedRow = object;
  }
  formatDate(millis: number): string {
    const date = new Date(millis);
    return date.toLocaleDateString('vi-VN');
  }
  formatTime(millis: number): string {
    const date = new Date(millis);
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
  getStatusClass(status: number): string {
    switch (status) {
      case 0: return 'status-success';    // Thành công
      case 1: return 'status-failed';     // Thất bại
      case 2: return 'status-pending';    // Đang chờ
      case 3: return 'status-processing'; // Đang thực hiện
      case 4: return 'status-warn'; // Đang thực hiện
      default: return '';
    }
  }

}

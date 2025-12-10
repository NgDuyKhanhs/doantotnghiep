import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../service/user.service";

@Component({
  selector: 'app-detail-info',
  templateUrl: './detail-info.component.html',
  styleUrls: ['./detail-info.component.scss']
})
export class DetailInfoComponent implements OnInit{
  data: any;
  user: any = {};
  showPopup: number | null = null;
  popupPosition = { top: 0, left: 0 };
  teachers: any = [];
    constructor(public activeModal: NgbActiveModal,
                private userService: UserService) {
    }

  ngOnInit(): void {
      this.userService.getUserByID(this.data).subscribe(res =>{
        this.user = res;
      })
  }
  getViolationLabel(type: string): string {
    const labelMap: { [key: string]: string } = {
      'OUT_SCREEN': 'Rời khỏi màn hình',
      'PRINT_SCREEN': 'Chụp màn hình',
      'EXIT_FULLSCREEN': 'Thoát toàn màn hình',
      'VISIBILITY_HIDDEN': 'Ẩn tab',
      'BLUR': 'Mất focus',
    };
    return labelMap[type] || type;
  }
  togglePopup(index: number, event: MouseEvent) {
    if (this.showPopup === index) {
      this.closePopup();
    } else {
      this.showPopup = index;
      this.getListTeacher();
      const rect = (event.target as HTMLElement).getBoundingClientRect();
      this.popupPosition = {
        top: rect.bottom + window.scrollY + 5,
        left: rect.left + window.scrollX - 200 // Hiển thị bên trái icon
      };
    }
  }
  getListTeacher() {
    if (this.teachers.length == 0) {
      this.userService.getAllTeachers().subscribe(res => {
        this.teachers = res.filter((teacher: { id: any; }) => teacher.id !== this.data);
      });
    }
  }
  closePopup() {
    this.showPopup = null;
  }
}

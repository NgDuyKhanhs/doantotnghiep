import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../service/user.service";
import {finalize} from "rxjs";
import {NotificationService} from "../../service/notification.service";

@Component({
  selector: 'app-detail-info',
  templateUrl: './detail-info.component.html',
  styleUrls: ['./detail-info.component.scss']
})
export class DetailInfoComponent implements OnInit{
  @Input() user1!: { anys: any[] };
  activeIndex: number | null = null;


  // trạng thái UI per row keyed by report id
  decreaseAmount: Record<number, number> = {};      // tổng lần đã bấm (số điểm cần hạ)
  loading: Record<number, boolean> = {};
  data: any;
  user: any = {};
  showPopup: number | null = null;
  popupPosition = { top: 0, left: 0 };
  teachers: any = [];
    constructor(public activeModal: NgbActiveModal,
                private userService: UserService,
                private notify: NotificationService) {
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
  onDashClick(report: any,score: any, index: number, event?: MouseEvent) {
    if (event) event.stopPropagation();
    this.decreaseAmount = score;
    // nếu đang active cùng hàng -> tăng decreaseAmount
    if (this.activeIndex === index) {
      this.decreaseAmount[index] = (this.decreaseAmount[index] ?? 0) + 1;
      return;
    }

    // đổi sang hàng khác -> reset hàng trước đó
    if (this.activeIndex != null && this.activeIndex !== index) {
      this.decreaseAmount[this.activeIndex] = 0;
    }

    // set active hàng bấm
    this.activeIndex = index;
    this.decreaseAmount[index] = (this.decreaseAmount[index] ?? 0) + 1;
  }

  // undo 1 lần giảm (chỉ cho active row)
  undoDash(report: any, index: number, event?: MouseEvent) {
    if (event) event.stopPropagation();
    if (this.activeIndex !== index) return;
    this.decreaseAmount[index] = Math.max(0, (this.decreaseAmount[index] ?? 0) - 1);
    if (this.decreaseAmount[index] === 0) {
      this.activeIndex = null;
    }
  }

  // lưu thay đổi: chỉ lưu cho active row
  saveDecrease(report: any, index: number) {
    if (this.activeIndex !== index) return;
    const amount = this.decreaseAmount[index] ?? 0;
    if (!amount || amount <= 0) return;

    this.loading[index] = true;

    // dùng report.id nếu có, ngược lại backend phải chấp nhận dùng email hoặc khác
    const idForApi = report.id ?? null;

    this.userService.decreaseScore(idForApi ?? 0, amount, report.email ?? '').pipe(
      finalize(() => {
        this.loading[index] = false;
      })
    ).subscribe({
      next: (res: any) => {
        // cập nhật điểm thực tế
        report.score = Math.max(0, report.score - amount);
        // reset trạng thái
        this.decreaseAmount[index] = 0;
        this.activeIndex = null;
      },
      error: (err) => {
        console.error('Decrease error', err);
      }
    });
  }

  // hủy (reset) các thay đổi tạm cho active row
  cancelDecrease(report: any, index: number, event?: MouseEvent) {
    if (event) event.stopPropagation();
    if (this.activeIndex !== index) return;
    this.decreaseAmount[index] = 0;
    this.activeIndex = null;
  }

  // helper hiển thị điểm tạm (score minus decreaseAmount)
  displayedScore(report: any, index: number): number {
    const dec = this.decreaseAmount[index] ?? 0;
    return Math.max(0, report.score - dec);
  }

  // helper class/colour (giữ logic trước đó)
  scoreColor(score: number) {
    return score >= 8 ? 'green' : score >= 5 ? 'orange' : 'red';
  }
}

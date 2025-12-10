import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActionType, UserService} from "../../service/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer} from "@angular/platform-browser";
import {NotificationService} from "../../service/notification.service";
import {NgxSpinnerService} from "ngx-spinner";
import {TokenService} from "../../service/token.service";
import {RefModalService} from "../../ref-modal.service";
import {CreateAssignmentsComponent} from "../../modals/create-assignments/create-assignments.component";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-assignments',
  templateUrl: './assignments.component.html',
  styleUrls: ['./assignments.component.scss']
})
export class AssignmentsComponent implements OnInit, OnDestroy{
  pdfFiles: any = [];
  currentAssignments: any = [];
  id: any;
  pdfReview: any = [];
  pdfSelected: any = []
  enrollmentDTO: any = {};
  collectPDF: { name: any; blob: any }[] = [];
  currentRole: any;
  syslog: any;
  pdfUrl: any;
  refModal: any
  answersJson: any;
  private sub?: Subscription;
  selectedType = 'Chưa nộp';
  readonly TYPES = [
    { value: 'Chưa nộp', label: 'Chưa nộp' },
    { value: 'Đã nộp', label: 'Đã nộp' }
  ];
   currentTab = "Bài tập"
   tabs = [
    { value: 'Bài tập'},
    { value: 'Vi phạm'}
  ];

   history: any = [];
  constructor(private userService: UserService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private notify: NotificationService,
              private spinner: NgxSpinnerService,
              private tokenService: TokenService,
              private refModalService: RefModalService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.currentRole = this.tokenService.getRoleFromToken();
    this.route.parent?.params.subscribe(params => {
      this.id = params['idEnroll'];
    });
    this.loadAssignments();
  }
  onTypeChange(type: string): void {
    this.selectedType = type;
    this.loadAssignments();
  }
  private loadAssignments(): void {
    if (this.sub) {
      this.sub.unsubscribe();
    }

    this.sub = this.userService.getAssignmentById(this.id, this.selectedType)
      .subscribe({
        next: (res) => {
          this.currentAssignments = res ?? [];
        },
        error: (err) => {
          this.currentAssignments = [];
          this.notify.showFail('Lỗi khi lấy dữ liệu');
        }
      });
  }

  remove(name: any) {
  }
  ngOnDestroy(): void {
    if (this.sub) this.sub.unsubscribe();
  }
  openTabCreate() {
    this.refModal = this.refModalService.open(this.id, CreateAssignmentsComponent, null, false, null, null, null, null, null, true);
  }
  openTabDo(id :any, idEnroll: any){
    this.userService.start(id, [], ActionType.START).subscribe((session) => {
      localStorage.setItem('examSession', JSON.stringify(session));
      this.router.navigate([`bai-tap/${id}/lam-bai/${idEnroll}`]);
    },error => {
        this.notify.showFail('Vui lòng hoàn thành trước khi bắt đầu bài mới.');
    });

  }

  getViolationIcon(type: string): string {
    const iconMap: { [key: string]: string } = {
      'OUT_SCREEN': 'bi-box-arrow-right',
      'PRINT_SCREEN': 'bi-camera-fill',
      'EXIT_FULLSCREEN': 'bi-arrows-angle-contract',
      'VISIBILITY_HIDDEN': 'bi-eye-slash-fill',
      'BLUR': 'bi-window',
    };
    return iconMap[type] || 'bi-exclamation-triangle-fill';
  }
  formatTime(seconds: number): string {
    if (!seconds) return '0s';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;

    const parts = [];
    if (h > 0) parts.push(`${h}h`);
    if (m > 0) parts.push(`${m}m`);
    if (s > 0 || parts.length === 0) parts.push(`${s}s`);

    return parts.join(' ');
  }

  getScoreClass(score: number): string {
    if (score >= 8) return 'text-success';
    if (score >= 5) return 'text-warning';
    return 'text-danger';
  }

  getProgressBarClass(score: number): string {
    if (score >= 8) return 'bg-success';
    if (score >= 5) return 'bg-warning';
    return 'bg-danger';
  }

  getScorePercent(score: number): number {
    return (score / 10) * 100;
  }
  loadHistory(assignmentId: number): void {
    this.userService.getHistory(assignmentId).subscribe({
      next: (res) => {
        this.history = res;
      },
      error: (err) => {
        this.notify.showFail('Không tìm thấy lịch sử làm bài');
      }
    });
  }
  navigateTab(tab: any, id?: any){
    this.currentTab = tab;
    if (tab == 'Vi phạm') {
        this.loadHistory(id);
    }
  }
  navigate(id: any){
    this.router.navigate([`danh-sach-nop-bai/${id}`]);
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
  getBadgeClass(type: string): string {
    const typeMap: { [key: string]: string } = {
      'OUT_SCREEN': 'bg-danger',
      'PRINT_SCREEN': 'bg-danger',
      'EXIT_FULLSCREEN': 'bg-warning text-dark',
      'VISIBILITY_HIDDEN': 'bg-info text-dark',
      'BLUR': 'bg-secondary',
    };
    return typeMap[type] || 'bg-light text-dark';
  }
}

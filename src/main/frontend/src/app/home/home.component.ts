import {AfterViewInit, Component, HostListener, OnInit, Renderer2} from '@angular/core';
import {ActionType, UserService} from "../service/user.service";
import {Router} from "@angular/router";
import {NotificationService} from "../service/notification.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit{
  // Phiên đang làm bài tập
  activeSession: any | null = null;

  // Danh sách bài tập chưa làm
  pendingAssignments: any[] = [];

  constructor(private userService: UserService, private router: Router, private notify: NotificationService) {

  }

  ngOnInit(): void {
    this.loadActiveSession();
    this.loadPendingAssignments();
  }

  // Load phiên làm bài tập hiện tại từ localStorage hoặc API
  loadActiveSession(): void {
    // TODO: Thay thế bằng API call thực tế trong tương lai
    const savedSession = localStorage.getItem('examSession');
    if (savedSession) {
      console.log(savedSession)
      this.activeSession = JSON.parse(savedSession);
      this.activeSession!.startTime = new Date(this.activeSession!.startTime);
    }
  }

  // Load danh sách bài tập chưa làm
  loadPendingAssignments(): void {
    // TODO: Thay thế bằng API call thực tế trong tương lai
    this.userService.findUnsubmitted().subscribe(res => {
      console.log(res)
      this.pendingAssignments = res.sort((a: any, b: any) => a.startTime.getTime() - b.startTime.getTime());
    })

  }

  continueAssignment(): void {
    if (this.activeSession) {
    this.openTabDoContinue(this.activeSession.assignmentId, this.activeSession.enrollId);

    }
  }

  // Bắt đầu làm bài tập mới
  startAssignment(assignment: any, id: any, enrollId: any): void {
    this.activeSession = {
      assignmentId: assignment.id,
      enrollId: assignment.enrollId,
      assignmentTitle: assignment.title,
      subject: assignment.subject,
      startTime: new Date(),
      progress: 0
    };
    this.openTabDo(id, enrollId);
  }
  openTabDo(id :any, idEnroll: any){
    this.userService.start(id, [], ActionType.START).subscribe((session) => {
      localStorage.setItem('examSession', JSON.stringify(session));
      this.router.navigate([`bai-tap/${id}/lam-bai/${idEnroll}`]);
    },error => {
      this.notify.showFail('Vui lòng hoàn thành trước khi bắt đầu bài mới.');
    });

  }

  openTabDoContinue(id :any, idEnroll: any){
      this.router.navigate([`bai-tap/${id}/lam-bai/${idEnroll}`]);

  }
  // Tính thời gian đã làm
  getSessionDuration(): string {
    if (!this.activeSession) return '';

    const now = new Date().getTime();
    const start = new Date(this.activeSession.startTime).getTime();
    const diff = Math.floor((now - start) / 1000 / 60); // minutes

    if (diff < 60) {
      return `${diff} phút`;
    } else {
      const hours = Math.floor(diff / 60);
      const minutes = diff % 60;
      return `${hours}h ${minutes}p`;
    }
  }

  // Format ngày hết hạn thành label thân thiện
  formatDueDate(date: Date): string {
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    const due = new Date(date);
    due.setHours(0, 0, 0, 0);
    const diffTime = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Hôm nay';
    if (diffDays === 1) return 'Ngày mai';
    if (diffDays < 0) return 'Quá hạn';
    return `${diffDays} ngày nữa`;
  }

  // Kiểm tra xem có gấp không (hôm nay hoặc ngày mai)
  isUrgent(date: Date): boolean {
    const label = this.formatDueDate(date);
    return label === 'Hôm nay' || label === 'Ngày mai' || label === 'Quá hạn';
  }

}

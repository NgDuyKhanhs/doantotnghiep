import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {NotificationService} from "../service/notification.service";

@Component({
  selector: 'app-list-violation',
  templateUrl: './list-violation.component.html',
  styleUrls: ['./list-violation.component.scss']
})
export class ListViolationComponent implements OnInit{
  submissions: any[] = [];
  filteredSubmissions: any[] = [];
  assignmentId : any;

  filterStatus = '';
  searchText = '';
  loading = false;
  error = '';

  statusOptions = [
    { value: '', label: 'Tất cả' },
    { value: 'SUBMITTED', label: 'Đã nộp' },
    { value: 'IN_PROGRESS', label: 'Đang làm' },
  ];

  // Statistics
  totalSubmissions = 0;
  submittedCount = 0;
  inProgressCount = 0;
  averageScore = 0;

  constructor(
    private submissionService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.assignmentId = this.route.snapshot.paramMap.get('id');
    if (!this.assignmentId) {
      this.error = 'Thiếu assignmentId';
      this.notify.showFail('Thiếu thông tin bài tập');
      return;
    }
    this.loadSubmissions();
  }

  loadSubmissions(): void {
    this.loading = true;
    this.error = '';

    this.submissionService.getSubmissionList(this.assignmentId, this.filterStatus)
      .subscribe({
        next: (res) => {
          this.submissions = res;
          this.calculateStatistics();
          this.applyFilters();
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || err?.message || 'Không thể tải danh sách bài nộp';
          this.loading = false;
          this.notify.showFail(this.error);
        }
      });
  }

  calculateStatistics(): void {
    this.totalSubmissions = this.submissions.length;
    this.submittedCount = this.submissions.filter(s => s.status === 'SUBMITTED').length;
    this.inProgressCount = this.submissions.filter(s => s.status === 'IN_PROGRESS').length;

    const submitted = this.submissions.filter(s => s.status === 'SUBMITTED');
    if (submitted.length > 0) {
      const totalScore = submitted.reduce((sum, s) => sum + s.score, 0);
      this.averageScore = totalScore / submitted.length;
    } else {
      this.averageScore = 0;
    }
  }

  onFilterChange(): void {
    this.loadSubmissions();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    let result = [...this.submissions];

    // Filter theo search text (tên hoặc email)
    if (this.searchText.trim()) {
      const search = this.searchText.toLowerCase();
      result = result.filter(s =>
        s.studentName?.toLowerCase().includes(search) ||
        s.studentEmail?.toLowerCase().includes(search)
      );
    }

    this.filteredSubmissions = result;
  }

  viewDetail(userId: number): void {
    this.router.navigate(['/submission-history/user', userId], {
      queryParams: { assignmentId: this.assignmentId }
    });
  }

  getScoreClass(score: number): string {
    if (score >= 8) return 'text-success';
    if (score >= 5) return 'text-warning';
    return 'text-danger';
  }

  getScoreBadgeClass(score: number): string {
    if (score >= 8) return 'bg-success';
    if (score >= 5) return 'bg-warning';
    return 'bg-danger';
  }

  getStatusBadge(status: string): string {
    return status === 'SUBMITTED' ? 'bg-success' : 'bg-warning text-dark';
  }

  getStatusIcon(status: string): string {
    return status === 'SUBMITTED' ? 'bi-check-circle-fill' : 'bi-hourglass-split';
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

  exportToExcel(): void {
    // TODO: Implement export Excel logic
    this.notify.showSuccess('Chức năng xuất Excel đang được phát triển');
  }

  refresh(): void {
    this.filterStatus = '';
    this.searchText = '';
    this.loadSubmissions();
  }
}

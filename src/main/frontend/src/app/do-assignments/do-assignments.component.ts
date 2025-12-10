import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActionType, UserService} from "../service/user.service";
import {NotificationService} from "../service/notification.service";
import {ActivatedRoute, Router} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {WatermarkService} from "../watermark.service";
import {TokenService} from "../service/token.service";

@Component({
  selector: 'app-do-assignments',
  templateUrl: './do-assignments.component.html',
  styleUrls: ['./do-assignments.component.scss']
})
export class DoAssignmentsComponent implements OnInit, OnDestroy{
  assignment!: any;

  // Mỗi phần tử: { questionId: number, selectedChoiceId: number | null }
  answers: any[] = [];
  currentQuestionIndex: number = 0;

  // Timer
  timeRemaining: number = 0; // seconds
  private timerId: any; // dùng clearInterval thay vì Subscription
  isTimeUp: boolean = false;

  id: any;

  // UI State
  isSubmitting: boolean = false;
  showConfirmDialog: boolean = false;
  warningShown: boolean = false;

  autoSaveInterval: any;
  examSession: any;
  private saveTimer?: any;
  idEnroll: any
  // Dùng để hợp nhất dữ liệu session (Redis) với danh sách câu hỏi khi 2 luồng load song song
  private pendingSessionAnswers: any[] | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private notify: NotificationService,
    private spinner: NgxSpinnerService,
    private watermark: WatermarkService,
    private tokenService: TokenService
  ) {}

  async ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.idEnroll = this.route.snapshot.paramMap.get('idEnroll');
    await this.watermark.initUser({
      id: this.tokenService.getIDUserFromToken(),
      email: this.tokenService.getEmailFormToken(),
      assignmentID: this.id
    });
    try { await document.documentElement.requestFullscreen(); } catch {}

    this.loadAssignment();
    this.loadExamSession();
  }

  ngOnDestroy(): void {
    if (this.autoSaveInterval) clearInterval(this.autoSaveInterval);
    this.stopTimer();
    if (this.saveTimer) clearTimeout(this.saveTimer);
    this.watermark.hideOverlay();
  }

  // ============ LOAD SESSION ============

  loadExamSession() {
    const idNum = Number(this.id);
    const cachedRaw = localStorage.getItem('examSession');
    const cached = cachedRaw ? JSON.parse(cachedRaw) : null;

    // Luôn ưu tiên lấy từ server để đồng bộ thời gian/answers
    this.userService.start(idNum, [], ActionType.REFRESH).subscribe({
      next: (session) => {
        if (!session) {
          // Không có session trên server
          if (cached && Number(cached.assignmentId) === idNum) {
            this.timeRemaining = cached.remainingSeconds;
            // Lưu tạm trả lời từ cache; sẽ hợp nhất sau khi assignment có
            this.pendingSessionAnswers = Array.isArray(cached.answersJson) ? cached.answersJson : [];
            this.tryHydrateAnswersFromSession(); // hợp nhất nếu assignment đã sẵn sàng
            this.startTimer();
            this.startAutoSave();
          } else {
            // Không có cache => khởi tạo START
            this.userService.start(idNum, [], ActionType.START).subscribe((newSession) => {
              this.timeRemaining = newSession.remainingSeconds;
              localStorage.setItem('examSession', JSON.stringify(newSession));
              // START: answers rỗng, chờ loadAssignment tạo khung answers
              this.pendingSessionAnswers = [];
              this.tryHydrateAnswersFromSession();
              this.startTimer();
              this.startAutoSave();
            });
          }
          return;
        }

        // Có session hợp lệ từ Redis
        this.timeRemaining = session.remainingSeconds;
        localStorage.setItem('examSession', JSON.stringify(session));

        // Lưu tạm để hợp nhất theo questionId sau khi assignment sẵn sàng
        this.pendingSessionAnswers = Array.isArray(session.answersJson) ? session.answersJson : [];
        this.tryHydrateAnswersFromSession(); // nếu assignment đã có, sẽ hợp nhất ngay

        this.startTimer();
        this.startAutoSave();
      },
      error: () => {
        // Fallback: dùng cache nếu có
        if (cached && Number(cached.assignmentId) === idNum) {
          this.timeRemaining = cached.remainingSeconds;
          this.pendingSessionAnswers = Array.isArray(cached.answersJson) ? cached.answersJson : [];
          this.tryHydrateAnswersFromSession();
          this.startTimer();
          this.startAutoSave();
        } else {
          this.notify.showFail('Không tải được phiên làm bài. Vui lòng thử lại.');
        }
      }
    });
  }

  // ============ LOAD ASSIGNMENT ============

  loadAssignment(): void {
    const idNum = Number(this.id);
    this.userService.getDetailAssignmentById(idNum).subscribe(res => {
      this.assignment = res;
      this.buildAnswersSkeleton();
      // Nếu đã có dữ liệu session pending thì hợp nhất ngay
      this.tryHydrateAnswersFromSession();
    });
  }

  // Tạo mảng answers trống dựa trên danh sách câu hỏi
  private buildAnswersSkeleton(): void {
    if (!this.assignment || !Array.isArray(this.assignment.questions)) {
      this.answers = [];
      return;
    }
    this.answers = this.assignment.questions.map((q: any) => ({
      questionId: q.id,
      selectedChoiceId: null
    }));
  }

  // Hợp nhất answers từ session (Redis/cache) vào mảng answers hiện có theo questionId
  private mergeAnswersFromSession(sessionAnswers: any[]): void {
    if (!Array.isArray(this.answers) || this.answers.length === 0) return;
    if (!Array.isArray(sessionAnswers)) return;

    const map = new Map<number, any>();
    for (const a of sessionAnswers) {
      // Chấp nhận cả null/0 tuỳ backend, ưu tiên giá trị hiện có trong session
      map.set(Number(a.questionId), (a.selectedChoiceId ?? null));
    }

    this.answers = this.answers.map(a => {
      const selected = map.has(Number(a.questionId)) ? map.get(Number(a.questionId)) : a.selectedChoiceId;
      return { questionId: a.questionId, selectedChoiceId: selected };
    });

    // Khử trùng lặp nếu trước đó có lỗi tạo phần tử
    this.dedupeAnswers();
  }

  // Thử hợp nhất nếu assignment đã sẵn sàng
  private tryHydrateAnswersFromSession(): void {
    if (this.assignment && Array.isArray(this.assignment.questions) && this.assignment.questions.length > 0) {
      // Đảm bảo đã có skeleton
      if (!this.answers || this.answers.length !== this.assignment.questions.length) {
        this.buildAnswersSkeleton();
      }
      if (this.pendingSessionAnswers) {
        this.mergeAnswersFromSession(this.pendingSessionAnswers);
      }
      // Áp UI cho câu hiện tại (HTML đang dựa vào currentAnswer.*)
      this.applyAnswersToUI(this.answers);
    }
  }

  // ============ AUTOSAVE ============

  startAutoSave(): void {
    const idNum = Number(this.id);
    if (this.autoSaveInterval) clearInterval(this.autoSaveInterval);

    this.autoSaveInterval = setInterval(() => {
      // Có thể bỏ qua tick nếu answers chưa sẵn sàng
      if (!this.answers) return;

      this.userService.start(idNum, this.answers, ActionType.UPDATE).subscribe({
        next: (res) => {
          // Đồng bộ lại answers từ server (nếu server có hợp nhất)
          const serverAnswers = Array.isArray(res?.answersJson) ? res.answersJson : this.answers;
          // Hợp nhất theo questionId để giữ đúng alignment với HTML
          this.mergeAnswersFromSession(serverAnswers);
          // Cập nhật cache để F5 vẫn có dữ liệu
          localStorage.setItem('examSession', JSON.stringify(res));
        },
      });
    }, 10000);
  }

  // ============ UI APPLY (không gán vào getter) ============

  // Đổ dữ liệu đã lưu cho câu hiện tại (theo currentQuestionIndex)
  applyAnswersToUI(answers: any[]) {
    if (!this.currentQuestion) return;

    const qid = this.currentQuestion.id;
    const idx = this.currentQuestionIndex;

    // Tìm đáp án đã lưu cho câu hiện tại theo questionId
    const selected =
      (answers ?? []).find(a => Number(a.questionId) === Number(qid))?.selectedChoiceId ?? null;

    // Đảm bảo phần tử mảng cho câu hiện tại tồn tại và đúng questionId
    if (!this.answers[idx] || Number(this.answers[idx].questionId) !== Number(qid)) {
      this.answers[idx] = { questionId: qid, selectedChoiceId: null };
    }

    // Cập nhật trực tiếp vào mảng gốc (không gán vào getter)
    this.answers[idx].selectedChoiceId = selected;
  }

  // ============ TIMER ============

  startTimer() {
    // Clear trước nếu đang chạy
    if (this.timerId) clearInterval(this.timerId);

    this.timerId = setInterval(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        this.stopTimer();
        this.timeUp();
      }
    }, 1000);
  }

  stopTimer(): void {
    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = undefined;
    }
  }

  timeUp(): void {
    this.isTimeUp = true;
    this.stopTimer();
    this.submitAssignment();
  }

  // ============ GETTERS PHỤC VỤ HTML HIỆN TẠI ============

  get currentQuestion(): any {
    return this.assignment?.questions?.[this.currentQuestionIndex];
  }

  get currentAnswer(): any {
    return this.answers?.[this.currentQuestionIndex];
  }

  get formattedTime(): string {
    const hours = Math.floor(this.timeRemaining / 3600);
    const minutes = Math.floor((this.timeRemaining % 3600) / 60);
    const seconds = this.timeRemaining % 60;

    return `${this.pad(hours)}:${this.pad(minutes)}:${this.pad(seconds)}`;
  }

  pad(num: number): string {
    return num.toString().padStart(2, '0');
  }

  // ============ HANDLERS ============

  // HTML đang gọi (click)="selectAnswer(choice.id, currentQuestion.id)"
  selectAnswer(choiceId: number, questionId: number): void {
    const idx = this.answers.findIndex(a => Number(a.questionId) === Number(questionId));
    if (idx >= 0) {
      // Cập nhật phần tử đúng questionId (không dựa vào index hiển thị)
      this.answers[idx] = { ...this.answers[idx], selectedChoiceId: choiceId };
    } else {
      // Nếu vì lý do nào đó chưa có phần tử, thêm mới
      this.answers.push({ questionId, selectedChoiceId: choiceId });
      this.dedupeAnswers();
    }
  }

  // Dự phòng nếu có nơi khác dùng
  onAnswerChange(questionId: number, choiceId: number | null) {
    const idx = this.answers.findIndex(a => Number(a.questionId) === Number(questionId));
    if (idx >= 0) this.answers[idx].selectedChoiceId = choiceId;
    else this.answers.push({ questionId, selectedChoiceId: choiceId });

    // Debounce rồi gọi UPDATE lưu Redis
    if (this.saveTimer) clearTimeout(this.saveTimer);
    const idNum = Number(this.id);
    this.saveTimer = setTimeout(() => {
      this.userService.start(idNum, this.answers, ActionType.UPDATE).subscribe(res => {
        const serverAnswers = Array.isArray(res?.answersJson) ? res.answersJson : this.answers;
        this.mergeAnswersFromSession(serverAnswers);
        localStorage.setItem('examSession', JSON.stringify(res));
      });
    }, 400);
  }

  goToQuestion(index: number): void {
    this.currentQuestionIndex = index;
    // Khi đổi câu hỏi, áp lại UI cho câu hiện tại từ mảng answers
    this.applyAnswersToUI(this.answers);
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.applyAnswersToUI(this.answers);
    }
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.assignment.questions.length - 1) {
      this.currentQuestionIndex++;
      this.applyAnswersToUI(this.answers);
    }
  }

  getAnsweredCount(): number {
    return this.answers.filter(a => a.selectedChoiceId !== null).length;
  }

  openConfirmDialog(): void {
    const unanswered = this.assignment.questions.length - this.getAnsweredCount();
    if (unanswered > 0) {
      const confirm = window.confirm(
        `Bạn còn ${unanswered} câu chưa trả lời.\nBạn có chắc chắn muốn nộp bài?`
      );
      if (!confirm) return;
    }
    this.showConfirmDialog = true;
  }

  closeConfirmDialog(): void {
    this.showConfirmDialog = false;
  }

  submitAssignment(): void {
    this.isSubmitting = true;
    this.stopTimer();
    this.spinner.show()
    this.userService.submitAssignment(this.assignment.id,this.answers).subscribe({
      next: (response) => {
        this.notify.showSuccess('Nộp bài thành công!');
        setTimeout(() => {
          this.router.navigate(['/danh-sach-hoc-phan-hien-tai']);
          this.spinner.hide()
        }, 1000);
      },
      error: (error) => {
        this.notify.showFail('Nộp bài thất bại');
        this.spinner.hide();
        this.isSubmitting = false;
      }
    });
  }

  exitAssignment(): void {
    const answered = this.getAnsweredCount();
    if (answered > 0) {
      const confirm = window.confirm(
        'Bạn có chắc chắn muốn thoát?'
      );
      if (!confirm) return;
    }

    this.stopTimer();
    this.router.navigate(['/chi-tiet-khoa-hoc', 'bai-tap']);
  }


  private dedupeAnswers() {
    const map = new Map<number, any>();
    for (const a of this.answers) {
      map.set(Number(a.questionId), a);
    }
    this.answers = Array.from(map.values());
  }

  protected readonly String = String;
}

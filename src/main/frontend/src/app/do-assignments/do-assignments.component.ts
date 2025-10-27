import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../service/user.service";
import {NotificationService} from "../service/notification.service";
import {NgxSpinnerService} from "ngx-spinner";
import {ActivatedRoute, Router} from "@angular/router";
import {interval} from "rxjs";

@Component({
  selector: 'app-do-assignments',
  templateUrl: './do-assignments.component.html',
  styleUrls: ['./do-assignments.component.scss']
})
export class DoAssignmentsComponent implements OnInit, OnDestroy{
  assignment!: any;
  answers: any[] = [];
  currentQuestionIndex: number = 0;

  // Timer
  timeRemaining: number = 0; // seconds
  timerSubscription?: any;
  isTimeUp: boolean = false;
  id: any;
  // UI State
  isSubmitting: boolean = false;
  showConfirmDialog: boolean = false;
  warningShown: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id');
    this.loadAssignment();
    this.loadExamSession();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }
  loadExamSession() {
    const session = JSON.parse(localStorage.getItem('examSession') || '{}');
    if (session && session.assignmentId === this.id) {
      this.timeRemaining = session.remainingSeconds;
      this.startTimer();
    } else {
      this.userService.start(this.id).subscribe((newSession) => {
        this.timeRemaining = newSession.remainingSeconds;
        this.startTimer();
      });
    }
  }
  loadAssignment(): void {
    this.userService.getDetailAssignmentById(this.id).subscribe(res => {
      this.assignment = res;
    })
  }

  initializeAnswers(): void {
    if (this.assignment && this.assignment.questions) {
      for (const q of this.assignment.questions) {
        this.answers.push({
          questionId: q.id,
          selectedChoiceId: null
        });
      }
    }
    console.log(this.answers)
  }

  startTimer() {
    const timer = setInterval(() => {
      this.timeRemaining--;
      if (this.timeRemaining <= 0) {
        clearInterval(timer);
        this.timeUp();
      }
    }, 1000);
  }

  stopTimer(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  timeUp(): void {
    this.isTimeUp = true;
    this.stopTimer();
    this.submitAssignment();
  }

  get currentQuestion(): any {
    return this.assignment.questions[this.currentQuestionIndex];
  }

  get currentAnswer(): any {
    return this.answers[this.currentQuestionIndex];
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

  selectAnswer(choiceId: number): void {
    this.currentAnswer.selectedChoiceId = choiceId;
  }

  goToQuestion(index: number): void {
    this.currentQuestionIndex = index;
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.assignment.questions.length - 1) {
      this.currentQuestionIndex++;
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

    const submissionData = {
      assignmentId: this.assignment.id,
      answers: this.answers.filter(a => a.selectedChoiceId !== null),
      submittedAt: new Date().toISOString()
    };

    console.log('Submitting:', submissionData);

    // TODO: Gọi API để submit
    // this.assignmentService.submitAssignment(submissionData).subscribe({
    //   next: (response) => {
    //     alert('✅ Nộp bài thành công!');
    //     this.router.navigate(['/assignments/result', response.id]);
    //   },
    //   error: (error) => {
    //     alert('❌ Có lỗi xảy ra: ' + error.message);
    //     this.isSubmitting = false;
    //   }
    // });

    setTimeout(() => {
      this.notify.showWarn('✅ Nộp bài thành công!');
      this.router.navigate(['/assignments']);
    }, 1000);
  }

  exitAssignment(): void {
    const answered = this.getAnsweredCount();
    if (answered > 0) {
      const confirm = window.confirm(
        'Bạn đã trả lời ' + answered + ' câu hỏi.\n' +
        'Nếu thoát, tiến trình sẽ không được lưu.\n' +
        'Bạn có chắc chắn muốn thoát?'
      );
      if (!confirm) return;
    }

    this.stopTimer();
    this.router.navigate(['/chi-tiet-khoa-hoc', 'bai-tap']);
  }
  protected readonly String = String;
}

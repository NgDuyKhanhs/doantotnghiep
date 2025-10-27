import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {RefModalService} from "../../ref-modal.service";
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {UserService} from "../../service/user.service";
import {NotificationService} from "../../service/notification.service";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-create-assignments',
  templateUrl: './create-assignments.component.html',
  styleUrls: ['./create-assignments.component.scss']
})
export class CreateAssignmentsComponent implements OnInit {
  @Output() assignmentCreated = new EventEmitter<any>();

  assignmentForm!: FormGroup;
  submitted = false;
  data: any;
  constructor(
    private fb: FormBuilder,
    public activeModal: NgbActiveModal,
    private userService: UserService,
    private notify: NotificationService,
    private spinner: NgxSpinnerService
  ) {
  }

  ngOnInit(): void {
    this.initializeForm();
    this.autoFillSampleData();
  }
  autoFillSampleData(): void {
    const sampleData = {
      title: "Bài tập 1",
      description: "60p",
      questions: [
        {
          text: "Ai là người đẹp trai nhất",
          correctChoiceId: 1,
          choices: [
            { text: "An" },
            { text: "Khánh" },
            { text: "Dương" },
            { text: "Mi" }
          ]
        }
      ]
    };
    this.fillFormFromJSON(sampleData);
  }
  fillFormFromJSON(data: any): void {
    try {
      // Reset form trước
      this.questionsArray.clear();

      // Fill title và description
      this.assignmentForm.patchValue({
        title: data.title || '',
        description: data.description || ''
      });

      // Fill questions và choices
      if (data.questions && Array.isArray(data.questions)) {
        data.questions.forEach((question: any) => {
          const questionGroup = this.fb.group({
            text: [question.text || '', [Validators.required, Validators.minLength(10)]],
            correctChoiceId: [question.correctChoiceId || null, Validators.required],
            choices: this.fb.array([])
          });

          // Fill choices
          if (question.choices && Array.isArray(question.choices)) {
            const choicesArray = questionGroup.get('choices') as FormArray;
            question.choices.forEach((choice: any) => {
              choicesArray.push(this.fb.group({
                text: [choice.text || '', [Validators.required, Validators.minLength(2)]]
              }));
            });
          }

          this.questionsArray.push(questionGroup);
        });
      }

      this.submitted = false;
    } catch (error) {
      console.error('Error filling form:', error);
    }
  }

  initializeForm(): void {
    this.assignmentForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      description: [''],
      startTime: [''],
      endTime: [''],
      duration: [''],
      questions: this.fb.array([])
    });
  }

  get questionsArray(): FormArray {
    return this.assignmentForm.get('questions') as FormArray;
  }

  getChoicesArray(qIndex: number): FormArray {
    return this.questionsArray.at(qIndex).get('choices') as FormArray;
  }

  addQuestion(): void {
    const questionGroup = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(10)]],
      correctChoiceId: [null, Validators.required],
      choices: this.fb.array([])
    });
    this.questionsArray.push(questionGroup);
  }

  addChoice(qIndex: number): void {
    const choiceGroup = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(2)]]
    });
    this.getChoicesArray(qIndex).push(choiceGroup);
  }

  removeQuestion(qIndex: number): void {
    this.questionsArray.removeAt(qIndex);
  }

  removeChoice(qIndex: number, cIndex: number): void {
    const choicesArray = this.getChoicesArray(qIndex);
    const question = this.questionsArray.at(qIndex);
    const currentCorrectId = question.get('correctChoiceId')?.value;

    if (currentCorrectId === cIndex) {
      question.patchValue({ correctChoiceId: null });
    }
    else if (currentCorrectId !== null && currentCorrectId > cIndex) {
      question.patchValue({ correctChoiceId: currentCorrectId - 1 });
    }

    choicesArray.removeAt(cIndex);
  }

  getFieldName(key: string): string {
    const fieldNames: {[key: string]: string} = {
      'title': 'Tiêu đề bài tập',
      'description': 'Mô tả',
      'startTime': 'Ngày bắt đầu',
      'endTime': 'Ngày kết thúc',
      'duration': 'Thời gian làm bài'
    };
    return fieldNames[key] || key;
  }

  setCorrectChoice(qIndex: number, cIndex: number): void {
    const question = this.questionsArray.at(qIndex);
    question.patchValue({ correctChoiceId: cIndex });
  }
  getFormValidationErrors(): string[] {
    const errors: string[] = [];

    if (this.assignmentForm.errors?.['dateRange']) {
      errors.push('Ngày kết thúc phải sau ngày bắt đầu');
    }

    // Kiểm tra các trường cơ bản
    Object.keys(this.assignmentForm.controls).forEach(key => {
      const control = this.assignmentForm.get(key);

      if (control && control.invalid && key !== 'questions') {
        const fieldName = this.getFieldName(key);

        if (control.errors?.['required']) {
          errors.push(`${fieldName} là bắt buộc`);
        }
        if (control.errors?.['minlength']) {
          errors.push(`${fieldName} quá ngắn (tối thiểu ${control.errors['minlength'].requiredLength} ký tự)`);
        }
        if (control.errors?.['min']) {
          errors.push(`${fieldName} phải lớn hơn 0`);
        }
      }
    });

    // Kiểm tra questions
    if (this.questionsArray.length === 0) {
      errors.push('Phải có ít nhất 1 câu hỏi');
    } else {
      this.questionsArray.controls.forEach((question, qIndex) => {
        const qGroup = question as FormGroup;

        if (qGroup.get('text')?.invalid) {
          errors.push(`Câu ${qIndex + 1}: Nội dung câu hỏi không hợp lệ`);
        }

        const choicesArray = qGroup.get('choices') as FormArray;
        if (choicesArray.length === 0) {
          errors.push(`Câu ${qIndex + 1}: Chưa có lựa chọn`);
        } else if (choicesArray.length < 2) {
          errors.push(`Câu ${qIndex + 1}: Phải có ít nhất 2 lựa chọn`);
        }

        if (qGroup.get('correctChoiceId')?.value === null) {
          errors.push(`Câu ${qIndex + 1}: Chưa chọn đáp án đúng`);
        }

        choicesArray.controls.forEach((choice, cIndex) => {
          if (choice.get('text')?.invalid) {
            errors.push(`Câu ${qIndex + 1} - Lựa chọn ${String.fromCharCode(65 + cIndex)}: Nội dung không hợp lệ`);
          }
        });
      });
    }

    return errors.length > 0 ? errors : ['Có lỗi không xác định'];
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.assignmentForm.valid) {
      this.spinner.show();
      const formData = this.assignmentForm.value;

      const assignmentData: any = {
        title: formData.title,
        description: formData.description,
        startTime: formData.startTime,
        endTime: formData.endTime,
        duration: formData.duration,
        questions: formData.questions.map((q: any) => ({
          text: q.text,
          correctChoiceId: q.correctChoiceId,
          choices: q.choices.map((c: any) => ({
            text: c.text
          }))
        }))
      };
      assignmentData.enrollId = this.data;
      this.assignmentCreated.emit(assignmentData);
      this.spinner.show();
      this.userService.uploadAssignment(assignmentData).subscribe({
        next: (response) => {
          this.notify.showSuccess("Tạo bài tập thành công")
          this.activeModal.close(assignmentData);
          this.spinner.hide();
        },
        error: (error) => {
          this.spinner.hide();
        }
      });

    } else {
      const errors = this.getFormValidationErrors();
      this.notify.showFail('Vui lòng kiểm tra lại các trường bắt buộc:\n' + errors.join('\n'));
    }
  }

  onPrint(): void {
    window.print();
  }

  onReset(): void {
    this.assignmentForm.reset();
    this.questionsArray.clear();
    this.submitted = false;
  }

  getQuestionNumber(index: number): string {
    return String.fromCharCode(65 + index);
  }
}

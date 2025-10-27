import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DomSanitizer} from "@angular/platform-browser";
import {NotificationService} from "../../service/notification.service";
import {NgxSpinnerService} from "ngx-spinner";
import {TokenService} from "../../service/token.service";
import {RefModalService} from "../../ref-modal.service";
import {CreateAssignmentsComponent} from "../../modals/create-assignments/create-assignments.component";

@Component({
  selector: 'app-assignments',
  templateUrl: './assignments.component.html',
  styleUrls: ['./assignments.component.scss']
})
export class AssignmentsComponent implements OnInit{
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
    this.getAssignmentByEnrollID();
  }
  getAssignmentByEnrollID() {
    this.userService.getAssignmentById(this.id).subscribe(res => {
      // @ts-ignore
      this.currentAssignments = res
    })

  }


  remove(name: any) {
  }

  openTabCreate() {
    this.refModal = this.refModalService.open(this.id, CreateAssignmentsComponent, null, false, null, null, null, null, null, true);
  }

  openTabDo(id :any){
    this.userService.start(id).subscribe((session) => {
      localStorage.setItem('examSession', JSON.stringify(session));
      this.router.navigate([`bai-tap/${id}/lam-bai`]);
    },error => {
        this.notify.showFail('Vui lòng hoàn thành trước khi bắt đầu bài mới.');
    });

  }
}

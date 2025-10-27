import {Component, OnInit} from '@angular/core';
import {UserService} from "../../service/user.service";
import {ActivatedRoute} from "@angular/router";
import {DomSanitizer} from "@angular/platform-browser";
import {NotificationService} from "../../service/notification.service";
import {NgxSpinnerService} from "ngx-spinner";
import {TokenService} from "../../service/token.service";

@Component({
  selector: 'app-content',
  templateUrl: './content.component.html',
  styleUrls: ['./content.component.scss']
})
export class ContentComponent implements OnInit {
  pdfFiles: any = [];
  currentPDFFiles: any = [];
  id: any;
  pdfReview: any = [];
  pdfSelected: any = []
  enrollmentDTO: any = {};
  collectPDF: { name: any; blob: any }[] = [];
  currentRole: any;
  syslog: any;
  pdfUrl: any;
  constructor(private userService: UserService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private notify: NotificationService,
              private spinner: NgxSpinnerService,
              private tokenService: TokenService) {
  }

  ngOnInit(): void {
    this.currentRole = this.tokenService.getRoleFromToken();
    this.route.parent?.params.subscribe(params => {
      this.id = params['idEnroll'];
    });
    this.getPdfFilesByEnrollID();
  }
  getPdfFilesByEnrollID() {
    this.userService.getPdfFilesByEnrollID(this.id).subscribe(res => {
      // @ts-ignore
      this.currentPDFFiles = res
    })

  }
  onFilesSelected(event: any): void {
    const files: File[] = event.target.files;
    if (files.length > 0) {
      this.pdfSelected = [];
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        this.pdfFiles.push(file);
        const objectURL = URL.createObjectURL(file);
        this.pdfReview.push(this.sanitizer.bypassSecurityTrustUrl(objectURL) as string);
        this.collectPDF.push({
          name: file.name,
          blob: this.pdfReview[this.pdfReview.length - 1].changingThisBreaksApplicationSecurity
        });
      }
      console.log(this.collectPDF)
    }
  }
  private convertToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        resolve(reader.result as string);
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  async uploadPDF() {
    if (this.pdfFiles.length > 0) {
      this.enrollmentDTO.pdfFiles = [];
      this.pdfSelected = [];
      for (let file of this.pdfFiles) {
        try {
          const base64String = await this.convertToBase64(file);
          const pdfDto = {
            pdfFile: base64String,
            nameFile: file.name,
            createdAt: new Date().toISOString()
          };
          this.pdfSelected.push(pdfDto);
        } catch (error) {
          this.notify.showFail('Không thể chuyển qua base64');
          return;
        }
      }
      this.enrollmentDTO.pdfFiles = this.pdfSelected;
      this.enrollmentDTO.enrollId = this.id;
    }
    this.spinner.show()
    this.userService.uploadPDF(this.enrollmentDTO).subscribe(res => {
      this.syslog = this.userService.setSysLog('Tải lên pdf', '', 0)
      this.userService.saveSysLog(this.syslog).subscribe(res =>{
      })
      setTimeout(() => {
        this.collectPDF = [];
        this.getPdfFilesByEnrollID();
        this.spinner.hide();
      },500)

    })
  }

  remove(name: any) {
    // @ts-ignore
    this.collectPDF = this.collectPDF.filter(file => file.name !== name);
  }


  formatTime(millis: number): string {
    const date = new Date(millis);
    return date.toLocaleDateString('vi-VN');
  }

  viewPdf(id: any, url: any) {
    this.spinner.show();
    this.userService.getSignedUrl(id, url).subscribe({
      next: (signedUrl: string) => {
        // Mở PDF ra tab mới
        window.open(signedUrl, '_blank');

        this.spinner.hide();
      },
      error: () => {
        this.notify.showFail("Không có quyền xem pdf");
        this.spinner.hide();
      }
    });
  }

}

import {Injectable} from '@angular/core';
import {MessageService} from 'primeng/api';

@Injectable({
  providedIn: 'root', // Đảm bảo service được chia sẻ toàn ứng dụng
})
export class NotificationService {
  constructor(private messageService: MessageService) {
  }

  showSuccess(detail: any) {
    this.messageService.add({severity: 'success', summary: 'Thao tác', detail});
  }

  showFail(detail: any) {
    this.messageService.add({severity: 'error', summary: 'Thao tác', detail});
  }

  showWarn(detail: any) {
    this.messageService.add({severity: 'warn', summary: 'Thao tác', detail});
  }

  showInfo(detail: any) {
    this.messageService.add({severity: 'info', summary: 'Thao tác', detail});
  }
}

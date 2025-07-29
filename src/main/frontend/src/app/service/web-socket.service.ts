import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private subject: Subject<MessageEvent> | undefined;
  private ws: WebSocket | undefined;

  constructor() {}

  /**
   * Kết nối WebSocket và trả về Subject để giao tiếp
   * @param url URL của WebSocket server
   */
  public connect(url: string): Subject<MessageEvent> {
    if (!this.subject || this.ws?.readyState === WebSocket.CLOSED) {
      this.subject = this.create(url);
      console.log('Successfully connected to: ' + url);
    }
    return this.subject;
  }

  /**
   * Ngắt kết nối WebSocket
   */
  public disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.subject = undefined;
      console.log('Successfully disconnected');
    }
  }

  /**
   * Tạo một WebSocket kết hợp với RxJS Subject
   * @param url URL của WebSocket server
   */
  private create(url: string): Subject<MessageEvent> {
    this.ws = new WebSocket(url);

    const observable = new Observable<MessageEvent>((obs) => {
      this.ws!.onmessage = obs.next.bind(obs); // Nhận tin nhắn từ server
      this.ws!.onerror = obs.error.bind(obs); // Xử lý lỗi
      this.ws!.onclose = obs.complete.bind(obs); // Kết nối đóng
      return () => {
        this.ws!.close(); // Đóng WebSocket khi không còn subscriber
      };
    });

    const observer = {
      next: (data: Object) => {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(JSON.stringify(data)); // Gửi tin nhắn
        }
      },
    };

    return Subject.create(observer, observable);
  }
}

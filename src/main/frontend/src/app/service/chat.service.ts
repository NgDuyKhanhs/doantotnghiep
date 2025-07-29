import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { WebSocketService } from './web-socket.service';

export interface Message {
  id?: any; // ID tin nhắn (có thể không bắt buộc)
  userId: any; // ID của người gửi
  sender: any; // Tên người gửi
  message: any; // Nội dung tin nhắn
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private readonly URL = 'ws://localhost:8080/ws'; // Đường dẫn WebSocket
  public messages: Subject<Message> | undefined; // Subject để giao tiếp với WebSocket

  constructor(private websocketService: WebSocketService) {}

  /**
   * Kết nối tới WebSocket và ánh xạ dữ liệu nhận được
   */
  public connect(): void {
    this.messages = <Subject<Message>>(
      this.websocketService.connect(this.URL).pipe(
        map((response: MessageEvent): Message => {
          const content = JSON.parse(response.data);
          console.log('Phản hồi từ ws:', content);

          return {
            id: content.id || null,
            userId: content.userId,
            sender: content.sender,
            message: content.message,
          };
        })
      )
    );
  }

  /**
   * Ngắt kết nối WebSocket
   */
  public disconnect(): void {
    this.websocketService.disconnect();
  }
}

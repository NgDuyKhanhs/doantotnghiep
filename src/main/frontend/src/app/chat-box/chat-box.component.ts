import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { WebSocketService } from '../service/web-socket.service';
import { ChatService, Message } from '../service/chat.service';

@Component({
  selector: 'app-chat-box',
  templateUrl: './chat-box.component.html',
  styleUrls: ['./chat-box.component.scss'],
})
export class ChatBoxComponent implements OnInit{
  daysOfWeek = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
  daysInMonth = 31;
  startDay = 1; // Monday

  isMobile = window.innerWidth < 768;

  ngOnInit() {
    window.addEventListener('resize', () => {
      this.isMobile = window.innerWidth < 768;
    });
  }

  getCalendarGrid(): (number | null)[] {
    const grid: (number | null)[] = [];
    for (let i = 0; i < this.startDay; i++) grid.push(null);
    for (let i = 1; i <= this.daysInMonth; i++) grid.push(i);
    return grid;
  }

  getDayOfWeekLabel(index: number): string {
    return this.daysOfWeek[index % 7];
  }
}

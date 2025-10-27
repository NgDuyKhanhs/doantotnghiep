import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  constructor(public cookieService: CookieService) {}
  private tokenSubject = new BehaviorSubject<string | null>(null);
  // updateToken(token: any) {
  //   this.cookieService.set('accessToken', token);
  //   this.tokenSubject.next();  // Cập nhật token ngay lập tức
  // }
  getTokenUpdates(): Observable<any> {
    return this.tokenSubject.asObservable();  // Lắng nghe các thay đổi của token
  }
  private decodeToken(): any {
    const accessToken = this.cookieService.get('accessToken');
    if (accessToken) {
      try {
        const tokenData = JSON.parse(atob(accessToken.split('.')[1]));
        return tokenData;
      } catch (e) {
        console.error('Invalid token format', e);
        return null;
      }
    }
    return null;
  }

  getIDUserFromToken() {
    const tokenData = this.decodeToken();
    if (tokenData && tokenData.userId) {
      return tokenData.userId;
    }
    return null; // Trả về null nếu không tìm thấy
  }

  getRoleFromToken(): any {
    const tokenData = this.decodeToken();
    if (tokenData && tokenData.roles && tokenData.roles.length > 0) {
      return tokenData.roles[0];
    }
    return null; // Trả về null nếu không tìm thấy
  }

  getEmailFormToken() {
    const tokenData = this.decodeToken();
    if (tokenData && tokenData.sub) {
      return tokenData.sub;
    }
    return null; // Trả về null nếu không tìm thấy
  }
}

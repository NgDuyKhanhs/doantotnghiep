import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {CookieService} from "ngx-cookie-service";
import {Observable} from "rxjs";
import {TokenService} from "./token.service";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseURL = "http://localhost:8080/api/v1/auth";
  constructor(private http: HttpClient,
              private router: Router,
              private cookieService: CookieService,
              private tokenService: TokenService
             ) { }

  loginUser(user: any): Observable<any> {
    return this.http.post<any>(`${this.baseURL}/login`, user, { withCredentials: true });
  }
  register(user: any): Observable<any> {
    return this.http.post<any>(`${this.baseURL}/register`, user);
  }
  activeToken(activeToken: any): Observable<any> {
    return this.http.post<any>(`${this.baseURL}/active`, activeToken);
  }
  logout() {
    return this.http.post<any>(`${this.baseURL}/logout`, {}, { withCredentials: true });
  }
  authenticated() {
    return !(this.tokenService.getEmailFormToken() == null)
  }

  refreshToken(): Observable<any> {
    return this.http.post<any>(`${this.baseURL}/refresh-token`, {}, {withCredentials: true});
  }

}

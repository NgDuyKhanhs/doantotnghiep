import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Router} from "@angular/router";
import {CookieService} from "ngx-cookie-service";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseURL = "http://localhost:8080/api/v1/users";
  private baseURL1 = "http://localhost:8080/api/v1/admin";
  constructor(private http: HttpClient,
              private router: Router,
              private cookieService: CookieService,
  ) {
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.baseURL}`);
  }

  getAllCurriculum(): Observable<any> {
    return this.http.get(`${this.baseURL}/get-curriculum`);
  }

  viewAllClasses(): Observable<any> {
    return this.http.get<any>(`${this.baseURL}/get-all-classes`);
  }

  getAllEnrollments(): Observable<any> {
    return this.http.get<any>(`${this.baseURL1}/get-all-enrollments`);
  }
  getAllTeachers(): Observable<any> {
    return this.http.get<any>(`${this.baseURL1}/get-list-teacher`);
  }


  getFilterEnrollments(type: string): Observable<any> {
    const params = new HttpParams().set('type', type);
    return this.http.get<any>(`${this.baseURL}/get-filter-enrollments`, { params });
  }

  registerEnrollment(enrollmentDTOS: any[]) {
    return this.http.post<any>(`${this.baseURL}/register-enrollment`, enrollmentDTOS);
  }

  uploadEnrollment(uploadEnrollmentReq: any[]) {
    return this.http.post<any>(`${this.baseURL1}/upload-enrollment`, uploadEnrollmentReq);
  }
}

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
  syslog : any = {};
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

  uploadEnrollment(enrollmentDTO: any) {
    return this.http.post<any>(`${this.baseURL1}/upload-enrollment`, enrollmentDTO);
  }

  getListUser(className: any): Observable<any>{
    const params = new HttpParams().set('className', className);
    return this.http.get<any>(`${this.baseURL1}/get-list-users`, { params });
  }
  uploadCourse(courseDTO: any){
    return this.http.post<any>(`${this.baseURL1}/upload-course`, courseDTO)
  }

  getRegisteredCourses() {
    return this.http.get<any>(`${this.baseURL}/get-list-registered`);
  }

  getInfoTeacherByID(id: any) {
    const params = new HttpParams().set('id', id);
    return this.http.get<any>(`${this.baseURL}/get-info-teacher-by-id`, {params});
  }
  getListUserFromEnrollment(id: any) {
    const params = new HttpParams().set('id', id);
    return this.http.get<any>(`${this.baseURL}/get-list-student-from-enrollment`, {params});
  }
  getPdfFilesByEnrollID(id: any) {
    const params = new HttpParams().set('id', id);
    return this.http.get<any>(`${this.baseURL}/get-pdfs-by-enrollId`, {params});
  }
  uploadPDF(enrollmentDTO:any) {
    return this.http.post<any>(`${this.baseURL1}/upload-pdf`, enrollmentDTO)
  }

  saveSysLog(syslog: any){
    return this.http.post(`${this.baseURL}/save-sys-log`, syslog)
  }

  setSysLog(action: any,description: any, status: any){
    this.syslog.action = action;
    this.syslog.description = description;
    this.syslog.status = status;
    return this.syslog;
  }

  getSysLog(){
    return this.http.get(`${this.baseURL}/syslogs-user`)
  }

  convertStatus(status: number): string {
    switch (status) {
      case 0:
        return 'Thành công';
      case 1:
        return 'Thất bại';
      case 2:
        return 'Đang chờ';
      case 3:
        return 'Đang thực hiện';
      case 4:
        return 'Cảnh báo';
      default:
        return 'Không xác định';
    }
  }

  getSignedUrl(pdfId: any, url: any): Observable<string> {
    return this.http.get(`${this.baseURL}/view/${pdfId}`, {
      params: { url },
      responseType: 'text'
    });
  }
  uploadAssignment(createAssignmentDTO: any) {
    return this.http.post<any>(`${this.baseURL1}/upload-assignment`, createAssignmentDTO)
  }

  getAssignmentById(id: any): Observable<any> {
    return this.http.get(`${this.baseURL1}/get-assignment-by-${id}`);
  }

  getDetailAssignmentById(id: any): Observable<any> {
    return this.http.get(`${this.baseURL1}/get-detail-assignment-by-${id}`);
  }
  start(assignmentId: any): Observable<any> {
    const params = new HttpParams().set('assignmentId', assignmentId);
    return this.http.post(`${this.baseURL1}/start`, {}, { params });
  }

}

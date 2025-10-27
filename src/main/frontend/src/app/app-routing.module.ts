import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {RegisterComponent} from "./register/register.component";
import {ChatBoxComponent} from "./chat-box/chat-box.component";
import {ActiveAccountComponent} from "./active-account/active-account.component";
import {ProfileComponent} from "./profile/profile.component";
import {AuthGuard} from "./service/auth.guard";
import {OtherInforsComponent} from "./profile/other-infors/other-infors.component";
import {ActionComponent} from "./profile/action/action.component";
import {SettingsComponent} from "./profile/settings/settings.component";
import {HomeComponent} from "./home/home.component";
import {RegisterlearnComponent} from "./registerlearn/registerlearn.component";
import {ChapterComponent} from "./chapter/chapter.component";
import {CurrentCoursesComponent} from "./current-courses/current-courses.component";
import {DetailMyCourseComponent} from "./detail-my-course/detail-my-course.component";
import {ContentComponent} from "./detail-my-course/content/content.component";
import {InforTeacherComponent} from "./detail-my-course/infor-teacher/infor-teacher.component";
import {ListStudentComponent} from "./detail-my-course/list-student/list-student.component";
import {CreateComponent} from "./registerlearn/create/create.component";
import {ListUserComponent} from "./list-user/list-user.component";
import {NewChaperComponent} from "./chapter/new-chaper/new-chaper.component";
import {UserResolver} from "./service/user.resolver";
import {AssignmentsComponent} from "./detail-my-course/assignments/assignments.component";
import {DoAssignmentsComponent} from "./do-assignments/do-assignments.component";

const routes: Routes = [
  {path: 'trang-chu', component: HomeComponent, data: {animation: 'LoginComponent'}},
  {path: 'dang-nhap', component: LoginComponent, data: {animation: 'LoginComponent'}},
  {path: 'dang-ky', component: RegisterComponent, data: {animation: 'RegisterComponent'}},
  {path: 'thoi-khoa-bieu', component: ChatBoxComponent, data: {animation: 'ChatBoxComponent'}},
  {path: '', redirectTo: 'chat', pathMatch: 'full', data: {animation: 'ChatBoxComponent'}},
  {path: 'active/:token', component: ActiveAccountComponent, data: {animation: 'LoginComponent'}},
  {
    path: 'thong-tin-ca-nhan',
    component: ProfileComponent, data: {animation: 'LoginComponent'},
    resolve: { user: UserResolver },
    children: [
      {path: 'thong-tin-khac', component: OtherInforsComponent, data: {animation: 'LoginComponent'}},
      {path: 'hanh-dong', component: ActionComponent, data: {animation: 'LoginComponent'}},
      {path: 'cai-dat', component: SettingsComponent, data: {animation: 'LoginComponent'}},
    ], canActivate: [AuthGuard]
  },
  {
    path: 'dang-ky-hoc',
    component: RegisterlearnComponent, data: {animation: 'LoginComponent'},
    children: [
      {path: 'them-moi', component: CreateComponent, data: {animation: 'LoginComponent'}},

    ], canActivate: [AuthGuard]
  },

  {
    path: 'chuong-trinh-hoc',
    component: ChapterComponent, data: {animation: 'LoginComponent'},
    children: [
      {path: 'them-moi', component: NewChaperComponent, data: {animation: 'LoginComponent'}},
    ], canActivate: [AuthGuard]
  },
  {path: 'danh-sach-hoc-phan-hien-tai', component: CurrentCoursesComponent, data: {animation: 'LoginComponent'}},
  {
    path: 'chi-tiet-khoa-hoc/:idEnroll/:idTeacher',
    component: DetailMyCourseComponent, data: {animation: 'LoginComponent'},
    children: [
      {path: 'noi-dung', component: ContentComponent, data: {animation: 'LoginComponent'}},
      {path: 'bai-tap', component: AssignmentsComponent, data: {animation: 'LoginComponent'}},
      {path: 'giang-vien', component: InforTeacherComponent, data: {animation: 'LoginComponent'}},
      {path: 'danh-sach-hoc-vien', component: ListStudentComponent, data: {animation: 'LoginComponent'}},
    ], canActivate: [AuthGuard]
  },
  {path:'danh-sach-sinh-vien', component: ListUserComponent},
  {path: 'bai-tap/:id/lam-bai', component: DoAssignmentsComponent},

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

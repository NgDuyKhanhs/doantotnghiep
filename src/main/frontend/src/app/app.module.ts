import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {DatePipe, NgIf} from "@angular/common";
import {RouterLink, RouterLinkActive} from "@angular/router";
import {BrowserAnimationsModule, provideAnimations} from "@angular/platform-browser/animations";
import {NgxSpinnerModule} from "ngx-spinner";
import {ToastModule} from "primeng/toast";
import {MessageModule} from "primeng/message";
import {MessagesModule} from "primeng/messages";
import {RecaptchaFormsModule, RecaptchaModule} from "ng-recaptcha";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {TokenInterceptor} from "./service/token.interceptor";
import {CookieService} from "ngx-cookie-service";
import {provideToastr} from "ngx-toastr";
import {MessageService} from "primeng/api";
import {AuthGuard} from "./service/auth.guard";
import {Button} from "primeng/button";
import {WebSocketService} from "./service/web-socket.service";
import {ChatService} from "./service/chat.service";
import { ChatBoxComponent } from './chat-box/chat-box.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { ActiveAccountComponent } from './active-account/active-account.component';
import { ProfileComponent } from './profile/profile.component';
import { ActionComponent } from './profile/action/action.component';
import { OtherInforsComponent } from './profile/other-infors/other-infors.component';
import { SettingsComponent } from './profile/settings/settings.component';
import { HomeComponent } from './home/home.component';
import { RegisterlearnComponent } from './registerlearn/registerlearn.component';
import { NavbarComponent } from './navbar/navbar.component';
import { ChapterComponent } from './chapter/chapter.component';
import { CurrentCoursesComponent } from './current-courses/current-courses.component';
import { DetailMyCourseComponent } from './detail-my-course/detail-my-course.component';
import { ContentComponent } from './detail-my-course/content/content.component';
import { InforTeacherComponent } from './detail-my-course/infor-teacher/infor-teacher.component';
import { ListStudentComponent } from './detail-my-course/list-student/list-student.component';
import { CreateComponent } from './registerlearn/create/create.component';
import { ClickOutsideDirective } from './directives/click-outside.directive';
import { ListUserComponent } from './list-user/list-user.component';
import { NewChaperComponent } from './chapter/new-chaper/new-chaper.component';
import { AssignmentsComponent } from './detail-my-course/assignments/assignments.component';
import { CreateAssignmentsComponent } from './modals/create-assignments/create-assignments.component';
import { DoAssignmentsComponent } from './do-assignments/do-assignments.component';
import {AntiCheatDirective} from "./anti-cheat.directive";
import {SharedAntiCheatModule} from "./shared-anti-cheat.module";
import {ListViolationComponent} from "./list-violation/list-violation.component";
import {ListTeacherComponent} from "./list-teacher/list-teacher.component";
import {DetailInfoComponent} from "./modals/detail-info/detail-info.component";
@NgModule({
  declarations: [
    AppComponent,
    ChatBoxComponent,
    RegisterComponent,
    LoginComponent,
    ActiveAccountComponent,
    ProfileComponent,
    ActionComponent,
    OtherInforsComponent,
    SettingsComponent,
    HomeComponent,
    RegisterlearnComponent,
    NavbarComponent,
    ChapterComponent,
    CurrentCoursesComponent,
    DetailMyCourseComponent,
    ContentComponent,
    InforTeacherComponent,
    ListStudentComponent,
    CreateComponent,
    ClickOutsideDirective,
    ListUserComponent,
    NewChaperComponent,
    AssignmentsComponent,
    CreateAssignmentsComponent,
    DoAssignmentsComponent,
    ListViolationComponent,
    ListTeacherComponent,
    DetailInfoComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    NgIf,
    RouterLink,
    RouterLinkActive,
    BrowserAnimationsModule,
    NgbModule,
    NgxSpinnerModule.forRoot({type: 'ball-clip-rotate'}),
    ToastModule,
    MessageModule,
    MessagesModule,
    RecaptchaModule,
    RecaptchaFormsModule,
    Button,
    DatePipe,
    ReactiveFormsModule,
    SharedAntiCheatModule
  ],
  providers: [{provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true}, CookieService,provideToastr(),AuthGuard, provideAnimations(),MessageService, WebSocketService, ChatService, DatePipe],
  bootstrap: [AppComponent]
})
export class AppModule { }

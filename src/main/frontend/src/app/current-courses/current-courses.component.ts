import {Component} from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-current-courses',
  templateUrl: './current-courses.component.html',
  styleUrls: ['./current-courses.component.scss']
})
export class CurrentCoursesComponent {
  constructor(private router: Router) {
  }

  navigate() {
    this.router.navigate(['chi-tiet-khoa-hoc/1'])
  }

}

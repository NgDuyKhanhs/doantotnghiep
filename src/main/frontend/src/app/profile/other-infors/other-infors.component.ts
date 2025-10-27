import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-other-infors',
  templateUrl: './other-infors.component.html',
  styleUrls: ['./other-infors.component.scss']
})
export class OtherInforsComponent implements OnInit{
  user: any;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.user = this.route.parent?.snapshot.data['user'];
    console.log(this.user)
  }
}

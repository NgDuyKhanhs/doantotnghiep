import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AntiCheatDirective} from "./anti-cheat.directive";

@NgModule({
  imports: [CommonModule, AntiCheatDirective],
  declarations: [],
  exports: [AntiCheatDirective],
})
export class SharedAntiCheatModule {}

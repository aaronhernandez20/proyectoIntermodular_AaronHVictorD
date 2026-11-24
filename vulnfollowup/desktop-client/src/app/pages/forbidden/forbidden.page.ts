import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { lockClosedOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** Shown when a role tries to open a page it is not allowed to see. */
@Component({
  selector: 'app-forbidden',
  templateUrl: './forbidden.page.html',
  styleUrls: ['./forbidden.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class ForbiddenPage {
  constructor() {
    addIcons({ lockClosedOutline });
  }
}

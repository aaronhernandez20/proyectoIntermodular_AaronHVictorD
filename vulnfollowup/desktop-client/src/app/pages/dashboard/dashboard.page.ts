import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { speedometerOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** Dashboard by severity and status (Phase 3). */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.page.html',
  styleUrls: ['./dashboard.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class DashboardPage {
  constructor() {
    addIcons({ speedometerOutline });
  }
}

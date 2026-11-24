import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { bugOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** Vulnerability list, filters, form and remediation (Phase 2, CU3-CU5). */
@Component({
  selector: 'app-vulnerabilities',
  templateUrl: './vulnerabilities.page.html',
  styleUrls: ['./vulnerabilities.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class VulnerabilitiesPage {
  constructor() {
    addIcons({ bugOutline });
  }
}

import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { serverOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** Asset management (Phase 2, CU2). */
@Component({
  selector: 'app-assets',
  templateUrl: './assets.page.html',
  styleUrls: ['./assets.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class AssetsPage {
  constructor() {
    addIcons({ serverOutline });
  }
}

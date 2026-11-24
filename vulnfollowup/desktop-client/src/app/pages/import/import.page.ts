import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { cloudUploadOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** JSON import (CU1), ADMIN and ANALYST only. */
@Component({
  selector: 'app-import',
  templateUrl: './import.page.html',
  styleUrls: ['./import.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class ImportPage {
  constructor() {
    addIcons({ cloudUploadOutline });
  }
}

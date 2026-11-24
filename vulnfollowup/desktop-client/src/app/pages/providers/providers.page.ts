import { Component } from '@angular/core';
import { addIcons } from 'ionicons';
import { businessOutline } from 'ionicons/icons';
import { PagePlaceholderComponent } from '../../components/page-placeholder/page-placeholder.component';

/** ICT providers and report per provider (Phase 3, SGE). */
@Component({
  selector: 'app-providers',
  templateUrl: './providers.page.html',
  styleUrls: ['./providers.page.scss'],
  imports: [PagePlaceholderComponent],
})
export class ProvidersPage {
  constructor() {
    addIcons({ businessOutline });
  }
}

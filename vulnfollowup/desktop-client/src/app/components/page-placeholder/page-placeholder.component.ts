import { Component, input } from '@angular/core';
import {
  IonButtons,
  IonCard,
  IonCardContent,
  IonContent,
  IonHeader,
  IonIcon,
  IonMenuButton,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { addIcons } from 'ionicons';
import { constructOutline } from 'ionicons/icons';

/** Page frame (header + menu button) used by the sections that are built in the next phases. */
@Component({
  selector: 'app-page-placeholder',
  templateUrl: './page-placeholder.component.html',
  styleUrls: ['./page-placeholder.component.scss'],
  // display: contents → ion-header/ion-content behave as direct children of the routed .ion-page
  host: { style: 'display: contents' },
  imports: [TranslatePipe, IonHeader, IonToolbar, IonButtons, IonMenuButton, IonTitle, IonContent, IonCard, IonCardContent, IonIcon],
})
export class PagePlaceholderComponent {
  readonly titleKey = input.required<string>();
  readonly descriptionKey = input.required<string>();
  readonly icon = input('construct-outline');
  readonly headingKey = input('placeholder.title');

  constructor() {
    addIcons({ constructOutline });
  }
}

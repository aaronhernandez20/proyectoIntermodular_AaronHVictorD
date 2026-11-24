import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  IonButton,
  IonChip,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonMenu,
  IonMenuToggle,
  IonRouterOutlet,
  IonSplitPane,
  IonTitle,
  IonToolbar,
} from '@ionic/angular';
import { TranslatePipe } from '@ngx-translate/core';
import { addIcons } from 'ionicons';
import {
  bugOutline,
  businessOutline,
  cloudUploadOutline,
  languageOutline,
  logOutOutline,
  personCircleOutline,
  serverOutline,
  shieldCheckmarkOutline,
  speedometerOutline,
} from 'ionicons/icons';
import { AuthService } from '../../core/services/auth.service';
import { Role } from '../../models';
import { LanguageService } from '../../core/services/language.service';

interface MenuItem {
  path: string;
  labelKey: string;
  icon: string;
  roles: Role[];
}

const MENU: MenuItem[] = [
  { path: '/dashboard', labelKey: 'menu.dashboard', icon: 'speedometer-outline', roles: ['ADMIN', 'ANALYST', 'VIEWER'] },
  { path: '/vulnerabilities', labelKey: 'menu.vulnerabilities', icon: 'bug-outline', roles: ['ADMIN', 'ANALYST', 'VIEWER'] },
  { path: '/assets', labelKey: 'menu.assets', icon: 'server-outline', roles: ['ADMIN', 'ANALYST', 'VIEWER'] },
  { path: '/providers', labelKey: 'menu.providers', icon: 'business-outline', roles: ['ADMIN', 'ANALYST', 'VIEWER'] },
  { path: '/import', labelKey: 'menu.import', icon: 'cloud-upload-outline', roles: ['ADMIN', 'ANALYST'] },
];

/** Main layout: side menu (filtered by role) + header with user, language and logout. */
@Component({
  selector: 'app-shell',
  imports: [
    RouterLink,
    RouterLinkActive,
    TranslatePipe,
    IonSplitPane,
    IonMenu,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonList,
    IonMenuToggle,
    IonItem,
    IonIcon,
    IonLabel,
    IonFooter,
    IonChip,
    IonButton,
    IonRouterOutlet,
  ],
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss'],
})
export class ShellComponent {
  readonly auth = inject(AuthService);
  readonly lang = inject(LanguageService);

  readonly menu = computed(() => MENU.filter((item) => this.auth.hasRole(...item.roles)));

  constructor() {
    addIcons({
      speedometerOutline,
      bugOutline,
      serverOutline,
      businessOutline,
      cloudUploadOutline,
      logOutOutline,
      languageOutline,
      personCircleOutline,
      shieldCheckmarkOutline,
    });
  }

  logout(): void {
    this.auth.logout();
  }
}

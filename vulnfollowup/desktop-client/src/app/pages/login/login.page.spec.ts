import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular';
import { provideTranslateService } from '@ngx-translate/core';

import { LoginPage } from './login.page';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), provideHttpClient(), provideIonicAngular(), provideTranslateService()],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not send the form when the fields are empty', () => {
    component.submit();
    expect(component.form.invalid).toBe(true);
    expect(component.showError('username')).toBe(true);
    expect(component.showError('password')).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('requires at least 6 characters in the password', () => {
    component.form.setValue({ username: 'analyst', password: '123' });
    expect(component.form.controls.password.hasError('minlength')).toBe(true);
  });
});

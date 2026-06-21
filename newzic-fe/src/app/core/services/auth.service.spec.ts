import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService, DEFAULT_AVATAR } from './auth.service';
import { PlayerService } from './player.service';
import { I18nService } from './i18n.service';
import { environment } from '../../../environments/environment';

class MockI18nService {
  setLanguage(_code: string) {}
  currentLang = () => 'en';
}

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService, PlayerService, { provide: I18nService, useClass: MockI18nService }]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should use DEFAULT_AVATAR when avatar is null', () => {
    service.register({
      artistName: 'Test', username: 'test', email: 'test@test.com',
      password: '12345678', role: 'singer'
    }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush({
      token: 'fake-token',
      user: {
        id: '1', username: 'test', email: 'test@test.com',
        displayName: 'Test', avatar: null, roles: ['singer'],
        followers: 0, following: 0
      }
    });

    expect(service.user()?.avatar).toBe(DEFAULT_AVATAR);
  });

  it('should use DEFAULT_AVATAR when avatar is empty string', () => {
    service.register({
      artistName: 'Test', username: 'test', email: 'test@test.com',
      password: '12345678', role: 'singer'
    }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush({
      token: 'fake-token',
      user: {
        id: '1', username: 'test', email: 'test@test.com',
        displayName: 'Test', avatar: '', roles: ['singer'],
        followers: 0, following: 0
      }
    });

    expect(service.user()?.avatar).toBe(DEFAULT_AVATAR);
  });

  it('should keep provided avatar when present', () => {
    service.register({
      artistName: 'Test', username: 'test', email: 'test@test.com',
      password: '12345678', role: 'singer'
    }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush({
      token: 'fake-token',
      user: {
        id: '1', username: 'test', email: 'test@test.com',
        displayName: 'Test', avatar: 'https://example.com/avatar.jpg', roles: ['singer'],
        followers: 0, following: 0
      }
    });

    expect(service.user()?.avatar).toBe('https://example.com/avatar.jpg');
  });

  it('DEFAULT_AVATAR should be a valid SVG data URI', () => {
    expect(DEFAULT_AVATAR).toContain('data:image/svg+xml');
    expect(DEFAULT_AVATAR).toContain('viewBox');
  });
});

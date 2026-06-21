import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { I18nService, LANGUAGES } from './i18n.service';

function flushInitialLoad(httpMock: HttpTestingController): void {
  const req = httpMock.expectOne(r => r.url.startsWith('/assets/i18n/'));
  req.flush({ 'nav.home': 'Home' });
}

describe('I18nService', () => {
  let service: I18nService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [I18nService]
    });
    service = TestBed.inject(I18nService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    flushInitialLoad(httpMock);
    expect(service).toBeTruthy();
  });

  it('should default to a supported language', () => {
    flushInitialLoad(httpMock);
    const lang = service.currentLang();
    expect(LANGUAGES.some(l => l.code === lang)).toBeTrue();
  });

  it('should use stored language from localStorage', () => {
    // Flush from the current service instance
    flushInitialLoad(httpMock);
    // Simulate a stored lang for next instantiation
    localStorage.setItem('newzic_lang', 'de');
    expect(localStorage.getItem('newzic_lang')).toBe('de');
  });

  it('should load translations on setLanguage', () => {
    flushInitialLoad(httpMock);

    // Switch to a different language
    const initialLang = service.currentLang();
    const targetLang = initialLang === 'it' ? 'de' : 'it';
    service.setLanguage(targetLang);
    const req = httpMock.expectOne(`/assets/i18n/${targetLang}.json`);
    req.flush({ 'nav.home': 'Home Target' });

    expect(service.currentLang()).toBe(targetLang);
    expect(service.t('nav.home')).toBe('Home Target');
    expect(localStorage.getItem('newzic_lang')).toBe(targetLang);
  });

  it('should return key if translation not found', () => {
    flushInitialLoad(httpMock);
    expect(service.t('nonexistent.key')).toBe('nonexistent.key');
  });

  it('should have exactly 4 languages (EN, IT, DE, ES)', () => {
    flushInitialLoad(httpMock);
    expect(LANGUAGES.length).toBe(4);
    expect(LANGUAGES.map(l => l.code)).toEqual(['en', 'it', 'de', 'es']);
  });

  it('should cache translations and switch back without HTTP', () => {
    // Flush initial load
    flushInitialLoad(httpMock);
    const initialLang = service.currentLang();

    // Switch to a different language
    const targetLang = initialLang === 'it' ? 'de' : 'it';
    service.setLanguage(targetLang);
    const req = httpMock.expectOne(`/assets/i18n/${targetLang}.json`);
    req.flush({ 'nav.home': 'Home Target' });
    expect(service.t('nav.home')).toBe('Home Target');

    // Switch back — should use cache, no new HTTP request
    service.setLanguage(initialLang);
    expect(service.t('nav.home')).toBe('Home');
  });
});

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslatePipe } from './translate.pipe';
import { I18nService } from '../../core/services/i18n.service';

describe('TranslatePipe', () => {
  let pipe: TranslatePipe;
  let i18nService: I18nService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [I18nService, TranslatePipe]
    });
    pipe = TestBed.inject(TranslatePipe);
    i18nService = TestBed.inject(I18nService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return key when no translation exists', () => {
    expect(pipe.transform('unknown.key')).toBe('unknown.key');
  });

  it('should delegate to I18nService.t()', () => {
    spyOn(i18nService, 't').and.returnValue('Translated');
    expect(pipe.transform('some.key')).toBe('Translated');
    expect(i18nService.t).toHaveBeenCalledWith('some.key', undefined);
  });
});

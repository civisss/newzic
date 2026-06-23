import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Language {
  code: string;
  label: string;
  flag: string;
}

export const LANGUAGES: Language[] = [
  { code: 'en', label: 'English', flag: '🇬🇧' },
  { code: 'it', label: 'Italiano', flag: '🇮🇹' },
  { code: 'de', label: 'Deutsch', flag: '🇩🇪' },
  { code: 'es', label: 'Español', flag: '🇪🇸' },
];

type Translations = Record<string, string>;

@Injectable({ providedIn: 'root' })
export class I18nService {
  private _currentLang = signal<string>(this.getInitialLang());
  private _translations: Translations = {};
  private cache = new Map<string, Translations>();

  readonly currentLang = this._currentLang.asReadonly();
  readonly languages = LANGUAGES;

  readonly currentLanguage = computed(() =>
    LANGUAGES.find(l => l.code === this._currentLang()) ?? LANGUAGES[0]
  );

  constructor(private http: HttpClient) {
    this.loadTranslations(this._currentLang());
  }

  t(key: string, params?: Record<string, string | number>): string {
    let value = this._translations[key] ?? key;
    if (params) {
      Object.keys(params).forEach(k => {
        value = value.replace(new RegExp(`{{\\s*${k}\\s*}}`, 'g'), String(params[k]));
      });
    }
    return value;
  }

  setLanguage(code: string, saveToServer = false): void {
    this._currentLang.set(code);
    localStorage.setItem('newzic_lang', code);
    this.loadTranslations(code);
    if (saveToServer) {
      this.http.patch(`${environment.apiUrl}/users/me`, { preferredLanguage: code }).subscribe();
    }
  }

  private loadTranslations(lang: string): void {
    const cached = this.cache.get(lang);
    if (cached) {
      this._translations = cached;
      return;
    }
    this.http.get<Translations>(`/assets/i18n/${lang}.json`).subscribe({
      next: (data) => {
        this.cache.set(lang, data);
        this._translations = data;
      },
      error: () => {
        if (lang !== 'en') {
          this.loadTranslations('en');
        }
      }
    });
  }

  private getInitialLang(): string {
    const stored = localStorage.getItem('newzic_lang');
    if (stored && LANGUAGES.some(l => l.code === stored)) return stored;
    const browserLang = navigator.language?.substring(0, 2) ?? 'en';
    if (LANGUAGES.some(l => l.code === browserLang)) return browserLang;
    return 'en';
  }
}

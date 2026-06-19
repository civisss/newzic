import { Injectable, signal, computed } from '@angular/core';

export interface Language {
  code: string;
  label: string;
  flag: string;
}

export const LANGUAGES: Language[] = [
  { code: 'en', label: 'English', flag: '🇬🇧' },
  { code: 'it', label: 'Italiano', flag: '🇮🇹' },
  { code: 'es', label: 'Español', flag: '🇪🇸' },
  { code: 'fr', label: 'Français', flag: '🇫🇷' },
  { code: 'de', label: 'Deutsch', flag: '🇩🇪' },
  { code: 'pt', label: 'Português', flag: '🇧🇷' },
];

@Injectable({ providedIn: 'root' })
export class I18nService {
  private _currentLang = signal<string>(this.getStoredLang());

  readonly currentLang = this._currentLang.asReadonly();
  readonly languages = LANGUAGES;

  readonly currentLanguage = computed(() =>
    LANGUAGES.find(l => l.code === this._currentLang()) ?? LANGUAGES[0]
  );

  setLanguage(code: string): void {
    this._currentLang.set(code);
    localStorage.setItem('newzic_lang', code);
  }

  private getStoredLang(): string {
    return localStorage.getItem('newzic_lang') ?? 'en';
  }
}

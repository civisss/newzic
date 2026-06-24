import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { tap, catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('newzic_token');
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req).pipe(
    catchError(err => {
      if (err.status === 401 || err.status === 403) {
        localStorage.removeItem('newzic_token');
        localStorage.removeItem('newzic_user');
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};

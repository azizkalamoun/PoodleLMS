import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

interface SnackBarConfig {
  duration?: number;
  horizontalPosition?: 'start' | 'center' | 'end';
  verticalPosition?: 'top' | 'bottom';
}

@Injectable({
  providedIn: 'root',
})
export class SnackBarService {
  private defaultDuration = 3000;
  private longDuration = 5000;

  constructor(private snackBar: MatSnackBar) {}

  /**
   * Show a success notification
   */
  success(message: string, config?: SnackBarConfig): void {
    this.show(message, 'success-snackbar', config?.duration ?? this.defaultDuration, config);
  }

  /**
   * Show an error notification
   */
  error(message: string, config?: SnackBarConfig): void {
    this.show(message, 'error-snackbar', config?.duration ?? this.longDuration, config);
  }

  /**
   * Show a warning notification
   */
  warning(message: string, config?: SnackBarConfig): void {
    this.show(message, 'warning-snackbar', config?.duration ?? this.defaultDuration, config);
  }

  /**
   * Show an info notification
   */
  info(message: string, config?: SnackBarConfig): void {
    this.show(message, 'info-snackbar', config?.duration ?? this.defaultDuration, config);
  }

  /**
   * Internal method to show snackbar with configuration
   */
  private show(
    message: string,
    panelClass: string,
    duration: number,
    config?: SnackBarConfig,
  ): void {
    this.snackBar.open(message, 'Close', {
      duration,
      panelClass: [panelClass],
      horizontalPosition: config?.horizontalPosition ?? 'end',
      verticalPosition: config?.verticalPosition ?? 'bottom',
    });
  }
}

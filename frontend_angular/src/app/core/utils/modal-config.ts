import { MatDialogConfig } from '@angular/material/dialog';

/**
 * Modal configuration utility for responsive dialogs across the application
 * Provides consistent sizing and scrolling behavior for different screen sizes
 */
export class ModalConfig {
  /**
   * Get responsive dialog configuration
   * Automatically adjusts maxHeight and maxWidth based on viewport
   */
  static getResponsiveConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    const baseConfig: MatDialogConfig = {
      backdropClass: 'backdrop-blur',
      panelClass: 'responsive-dialog',
      disableClose: false,
      autoFocus: true,
      ...options,
    };

    return baseConfig;
  }

  /**
   * Configuration for small dialogs (confirm, alerts)
   * Max width: 400px, Max height: 60vh
   */
  static getSmallDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '400px',
      maxHeight: '60vh',
      width: '90%',
      ...options,
    });
  }

  /**
   * Configuration for medium dialogs (simple forms)
   * Max width: 500px, Max height: 70vh
   */
  static getMediumDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '500px',
      maxHeight: '70vh',
      width: '90%',
      ...options,
    });
  }

  /**
   * Configuration for large dialogs (complex forms, course details)
   * Max width: 700px, Max height: 80vh
   */
  static getLargeDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '700px',
      maxHeight: '80vh',
      width: '90%',
      ...options,
    });
  }

  /**
   * Configuration for extra-large dialogs (course editor with many sections)
   * Max width: 900px, Max height: 85vh
   */
  static getExtraLargeDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '900px',
      maxHeight: '85vh',
      width: '95%',
      ...options,
    });
  }

  /**
   * Configuration for full-screen dialogs (course builder, analytics)
   * Max width: 95vw, Max height: 90vh
   */
  static getFullScreenDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '95vw',
      maxHeight: '90vh',
      width: '95vw',
      ...options,
    });
  }

  /**
   * Configuration for notification/modal dialogs
   * Optimized for list content with scrolling
   */
  static getListDialogConfig(
    options?: Partial<MatDialogConfig>,
  ): MatDialogConfig {
    return this.getResponsiveConfig({
      maxWidth: '600px',
      maxHeight: '80vh',
      width: '90%',
      ...options,
    });
  }
}

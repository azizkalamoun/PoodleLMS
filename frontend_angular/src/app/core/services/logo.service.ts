import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface LogoConfig {
  logoImageUrl: string | null;
  useDefaultLogo: boolean;
}

/**
 * Service for managing logo initialization and loading logic
 * Handles fallback from PNG to SVG to default Material icon
 * Can be used across admin-layout, employee-layout, and other components
 */
@Injectable({
  providedIn: 'root',
})
export class LogoService {
  private logoConfigSubject = new BehaviorSubject<LogoConfig>({
    logoImageUrl: null,
    useDefaultLogo: false,
  });

  public logoConfig$: Observable<LogoConfig> =
    this.logoConfigSubject.asObservable();

  constructor() {
    this.initializeLogos();
  }

  /**
   * Initialize logos with fallback chain: PNG → SVG → Default Icon
   * Stores the result in an observable that components can subscribe to
   */
  private initializeLogos(): void {
    this.loadLogo();
  }

  /**
   * Load the logo with intelligent fallback
   * First tries PNG, then SVG, then uses default Material icon
   */
  private loadLogo(): void {
    const pngUrl = '/assets/images/logo.png';
    const svgUrl = '/assets/images/logo.svg';

    const img = new Image();

    img.onload = () => {
      // PNG loaded successfully
      this.logoConfigSubject.next({
        logoImageUrl: pngUrl,
        useDefaultLogo: false,
      });
    };

    img.onerror = () => {
      // PNG failed, try SVG
      this.tryLoadSvg(svgUrl);
    };

    img.src = pngUrl;
  }

  /**
   * Try to load SVG as fallback
   */
  private tryLoadSvg(svgUrl: string): void {
    const svgImg = new Image();

    svgImg.onload = () => {
      // SVG loaded successfully
      this.logoConfigSubject.next({
        logoImageUrl: svgUrl,
        useDefaultLogo: false,
      });
    };

    svgImg.onerror = () => {
      // Both PNG and SVG failed, use default Material icon
      this.useDefaultLogo();
    };

    svgImg.src = svgUrl;
  }

  /**
   * Use the default Material icon when no custom logo is available
   */
  private useDefaultLogo(): void {
    this.logoConfigSubject.next({
      logoImageUrl: null,
      useDefaultLogo: true,
    });
  }

  /**
   * Get the current logo configuration synchronously
   */
  getLogoConfig(): LogoConfig {
    return this.logoConfigSubject.value;
  }

  /**
   * Reset and reload logos
   */
  reloadLogos(): void {
    this.loadLogo();
  }
}

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface Breadcrumb {
  label: string;
  route?: string;
}

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="mb-6 animate-fade-in">
      <nav
        *ngIf="getBreadcrumbs().length > 0"
        class="flex items-center gap-1.5 text-sm text-surface-500 mb-2"
        aria-label="Breadcrumb"
      >
        <ng-container *ngFor="let crumb of getBreadcrumbs(); let last = last">
          <a
            *ngIf="crumb.route && !last"
            [routerLink]="crumb.route"
            class="hover:text-surface-800 transition-colors cursor-pointer"
            >{{ crumb.label }}</a
          >
          <span
            *ngIf="!crumb.route || last"
            [class.text-surface-800]="last"
            [class.font-medium]="last"
            >{{ crumb.label }}</span
          >
          <span
            *ngIf="!last"
            class="material-icons-outlined !text-base text-surface-300"
            >chevron_right</span
          >
        </ng-container>
      </nav>
      <div
        class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4"
      >
        <div>
          <h1 class="text-xl font-semibold text-surface-900 tracking-tight">
            {{ title }}
          </h1>
          <p *ngIf="subtitle" class="text-sm text-surface-500 mt-0.5">
            {{ subtitle }}
          </p>
        </div>
        <div class="flex items-center gap-3">
          <ng-content></ng-content>
        </div>
      </div>
    </div>
  `,
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() breadcrumbs: string[] | Breadcrumb[] = [];

  getBreadcrumbs(): Breadcrumb[] {
    return this.breadcrumbs.map((crumb) => {
      if (typeof crumb === 'string') {
        return { label: crumb };
      }
      return crumb;
    });
  }
}

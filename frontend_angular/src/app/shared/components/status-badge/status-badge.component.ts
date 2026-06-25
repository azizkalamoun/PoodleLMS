import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span
      class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium leading-none"
      [ngClass]="colorClass"
    >
      <span
        *ngIf="dot"
        class="w-1.5 h-1.5 rounded-full"
        [ngClass]="dotClass"
      ></span>
      {{ label }}
    </span>
  `,
})
export class StatusBadgeComponent {
  @Input() status: string = '';
  @Input() dot = true;

  get label(): string {
    return this.status.replace(/_/g, ' ');
  }

  get colorClass(): string {
    switch (this.status?.toUpperCase()) {
      case 'PUBLISHED':
      case 'PASSED':
      case 'COMPLETED':
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
      case 'DRAFT':
      case 'PENDING':
        return 'bg-slate-100 text-slate-600 border border-slate-200';
      case 'IN_PROGRESS':
      case 'ENROLLED':
        return 'bg-blue-50 text-blue-700 border border-blue-200';
      case 'FAILED':
      case 'OVERDUE':
      case 'EXPIRED':
        return 'bg-red-50 text-red-700 border border-red-200';
      case 'ROLE_ADMIN':
        return 'bg-violet-50 text-violet-700 border border-violet-200';
      case 'ROLE_EMPLOYEE':
        return 'bg-sky-50 text-sky-700 border border-sky-200';
      default:
        return 'bg-gray-100 text-gray-600 border border-gray-200';
    }
  }

  get dotClass(): string {
    switch (this.status?.toUpperCase()) {
      case 'PUBLISHED':
      case 'PASSED':
      case 'COMPLETED':
      case 'ACTIVE':
        return 'bg-emerald-500';
      case 'DRAFT':
      case 'PENDING':
        return 'bg-slate-400';
      case 'IN_PROGRESS':
      case 'ENROLLED':
        return 'bg-blue-500';
      case 'FAILED':
      case 'OVERDUE':
      case 'EXPIRED':
        return 'bg-red-500';
      case 'ROLE_ADMIN':
        return 'bg-violet-500';
      case 'ROLE_EMPLOYEE':
        return 'bg-sky-500';
      default:
        return 'bg-gray-400';
    }
  }
}

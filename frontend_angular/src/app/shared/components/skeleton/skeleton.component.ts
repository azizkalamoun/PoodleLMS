import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [ngSwitch]="type">
      <!-- Table Skeleton -->
      <div *ngSwitchCase="'table'" class="space-y-3 p-5">
        <!-- Header Row -->
        <div class="flex gap-4 pb-4 border-b border-surface-100">
          <div
            *ngFor="let col of cols"
            class="skeleton h-5 rounded"
            [style.width]="col + '%'"
          ></div>
        </div>
        <!-- Data Rows -->
        <div *ngFor="let row of rows" class="flex gap-4 py-3">
          <div
            *ngFor="let col of cols"
            class="skeleton h-4 rounded"
            [style.width]="col + '%'"
          ></div>
        </div>
      </div>

      <!-- Card Skeleton -->
      <div *ngSwitchCase="'card'" class="p-6 space-y-4">
        <div class="skeleton h-5 w-2/5 rounded-lg"></div>
        <div class="space-y-3 py-2">
          <div class="skeleton h-4 w-full rounded"></div>
          <div class="skeleton h-4 w-11/12 rounded"></div>
          <div class="skeleton h-4 w-10/12 rounded"></div>
        </div>
        <div class="pt-4 flex gap-2">
          <div class="skeleton h-10 w-24 rounded-lg"></div>
          <div class="skeleton h-10 flex-1 rounded-lg"></div>
        </div>
      </div>

      <!-- KPI Skeleton -->
      <div
        *ngSwitchCase="'kpi'"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
      >
        <div
          *ngFor="let item of rows"
          class="p-6 space-y-3 border border-surface-100 rounded-xl"
        >
          <!-- Icon placeholder -->
          <div class="flex items-start justify-between">
            <div>
              <div class="skeleton h-4 w-24 rounded mb-3"></div>
              <div class="skeleton h-10 w-20 rounded-lg"></div>
            </div>
            <div class="skeleton w-14 h-14 rounded-2xl"></div>
          </div>
          <!-- Description -->
          <div class="skeleton h-3 w-32 rounded"></div>
        </div>
      </div>

      <!-- List Skeleton -->
      <div *ngSwitchCase="'list'" class="space-y-4 p-5">
        <div
          *ngFor="let row of rows"
          class="flex items-center gap-4 pb-4 border-b border-surface-50"
        >
          <div class="skeleton w-12 h-12 rounded-full flex-shrink-0"></div>
          <div class="flex-1 space-y-3">
            <div class="skeleton h-4 w-1/3 rounded-lg"></div>
            <div class="skeleton h-3 w-2/3 rounded"></div>
            <div class="skeleton h-3 w-1/2 rounded"></div>
          </div>
          <div class="skeleton w-20 h-6 rounded-full flex-shrink-0"></div>
        </div>
      </div>

      <!-- Analytics Skeleton (for dashboard analytics) -->
      <div *ngSwitchCase="'analytics'" class="space-y-6">
        <!-- Chart skeleton -->
        <div class="p-6 border border-surface-100 rounded-xl space-y-4">
          <div class="skeleton h-5 w-1/3 rounded-lg mb-4"></div>
          <div class="space-y-3">
            <div
              *ngFor="let item of [1, 2, 3, 4, 5]"
              class="flex items-center gap-3"
            >
              <div class="skeleton h-2 flex-1 rounded-full"></div>
              <div class="skeleton h-4 w-12 rounded"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Skeleton (for user/profile cards) -->
      <div *ngSwitchCase="'profile'" class="text-center space-y-4 p-6">
        <!-- Avatar -->
        <div class="flex justify-center">
          <div class="skeleton w-20 h-20 rounded-full"></div>
        </div>
        <!-- Name -->
        <div class="space-y-2">
          <div class="skeleton h-5 w-2/3 rounded-lg mx-auto"></div>
          <div class="skeleton h-3 w-1/2 rounded mx-auto"></div>
        </div>
        <!-- Bio -->
        <div class="space-y-2 pt-2">
          <div class="skeleton h-3 w-full rounded"></div>
          <div class="skeleton h-3 w-5/6 rounded mx-auto"></div>
        </div>
      </div>

      <!-- Form Skeleton (for form inputs) -->
      <div *ngSwitchCase="'form'" class="space-y-4 p-6">
        <div *ngFor="let field of rows" class="space-y-2">
          <div class="skeleton h-4 w-1/4 rounded"></div>
          <div class="skeleton h-12 w-full rounded-lg"></div>
        </div>
        <div class="flex gap-3 pt-4">
          <div class="skeleton h-10 w-24 rounded-lg"></div>
          <div class="skeleton h-10 flex-1 rounded-lg"></div>
        </div>
      </div>

      <!-- Text Skeleton (default) -->
      <div *ngSwitchDefault class="space-y-3">
        <div
          *ngFor="let row of rows"
          class="skeleton h-4 rounded"
          [style.width]="getRandomWidth()"
        ></div>
      </div>
    </div>
  `,
})
export class SkeletonComponent {
  @Input() type:
    | 'table'
    | 'card'
    | 'kpi'
    | 'list'
    | 'text'
    | 'analytics'
    | 'profile'
    | 'form' = 'text';
  @Input() count = 5;

  get rows(): number[] {
    return Array(this.count).fill(0);
  }

  get cols(): string[] {
    return ['15', '25', '20', '15', '25'];
  }

  getRandomWidth(): string {
    const widths = ['60%', '80%', '45%', '70%', '55%', '75%'];
    return widths[Math.floor(Math.random() * widths.length)];
  }
}

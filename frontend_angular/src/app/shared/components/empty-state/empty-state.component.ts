import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="flex flex-col items-center justify-center py-16 text-center animate-fade-in"
    >
      <div
        class="w-16 h-16 rounded-2xl bg-surface-100 flex items-center justify-center mb-4"
      >
        <span class="material-icons-outlined text-surface-400 !text-3xl">{{
          icon
        }}</span>
      </div>
      <h3 class="text-base font-semibold text-surface-800 mb-1">{{ title }}</h3>
      <p class="text-sm text-surface-500 max-w-sm">{{ message }}</p>
      <div class="mt-5">
        <ng-content></ng-content>
      </div>
    </div>
  `,
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'No data found';
  @Input() message = 'There is nothing to display right now.';
}

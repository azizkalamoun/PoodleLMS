import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface FormDialogData {
  title: string;
  submitText?: string;
  cancelText?: string;
  icon?: string;
  content?: any; // Will hold the form content/template
}

@Component({
  selector: 'app-form-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="flex flex-col h-full p-6">
      <!-- Header (Fixed) -->
      <div
        class="flex items-center gap-3 pb-4 border-b border-surface-200 flex-shrink-0"
      >
        <div
          class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center"
        >
          <span class="material-icons-outlined text-brand-600">{{
            data.icon || 'edit'
          }}</span>
        </div>
        <h2 class="text-lg font-semibold text-surface-900">{{ data.title }}</h2>
      </div>

      <!-- Content (Scrollable) -->
      <div class="flex-1 overflow-y-auto my-6 pr-2 custom-scrollbar">
        <ng-content></ng-content>
      </div>

      <!-- Footer (Fixed) -->
      <div
        class="flex items-center gap-3 pt-4 border-t border-surface-200 flex-shrink-0"
      >
        <button
          type="button"
          class="flex-1 px-4 py-2.5 text-sm font-medium text-surface-700 bg-white border border-surface-300 rounded-lg hover:bg-surface-50 transition-colors"
          (click)="onCancel()"
        >
          {{ data.cancelText || 'Cancel' }}
        </button>
        <button
          type="submit"
          class="flex-1 px-4 py-2.5 text-sm font-medium text-white bg-brand-600 rounded-lg hover:bg-brand-700 transition-colors"
          (click)="onSubmit()"
        >
          {{ data.submitText || 'Save' }}
        </button>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }

      ::ng-deep .custom-scrollbar {
        scrollbar-width: thin;
        scrollbar-color: #cbd5e0 transparent;
      }

      ::ng-deep .custom-scrollbar::-webkit-scrollbar {
        width: 6px;
      }

      ::ng-deep .custom-scrollbar::-webkit-scrollbar-track {
        background: transparent;
      }

      ::ng-deep .custom-scrollbar::-webkit-scrollbar-thumb {
        background-color: #cbd5e0;
        border-radius: 3px;
      }

      ::ng-deep .custom-scrollbar::-webkit-scrollbar-thumb:hover {
        background-color: #a0aec0;
      }
    `,
  ],
})
export class FormDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<FormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FormDialogData,
  ) {}

  onSubmit(): void {
    this.dialogRef.close('submit');
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

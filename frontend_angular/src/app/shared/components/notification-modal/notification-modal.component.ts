import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NotificationService, Notification } from '../../../core/services';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="p-4">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold">All Notifications</h2>
        <div class="flex items-center justify-center h-9 w-9">
          <button
            mat-icon-button
            (click)="close()"
            aria-label="Close"
            class="!h-9 !w-9"
          >
            <mat-icon>close</mat-icon>
          </button>
        </div>
      </div>

      <div
        *ngIf="notifications && notifications.length > 0; else empty"
        class="space-y-2 max-h-[60vh] overflow-auto pr-2"
      >
        <div
          *ngFor="let n of notifications"
          class="p-3 rounded shadow-sm flex items-start justify-between"
          [ngClass]="{ 'bg-blue-50': !n.isRead, 'bg-white': n.isRead }"
        >
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <strong class="truncate">{{ n.title }}</strong>
              <span class="text-xs text-muted">{{
                n.createdAt | date: 'short'
              }}</span>
            </div>
            <p class="text-sm text-surface-700 mt-1 truncate">
              {{ n.message }}
            </p>
            <div class="mt-2 flex gap-2">
              <button
                mat-stroked-button
                size="small"
                color="primary"
                *ngIf="!n.isRead"
                (click)="markAsRead(n, $event)"
                [disabled]="notificationService.isLoading(n.id)"
              >
                <mat-progress-spinner
                  *ngIf="notificationService.isLoading(n.id)"
                  mode="indeterminate"
                  diameter="16"
                  strokeWidth="2"
                ></mat-progress-spinner>
                <span *ngIf="!notificationService.isLoading(n.id)"
                  >Mark as read</span
                >
              </button>
              <button
                mat-stroked-button
                size="small"
                color="warn"
                (click)="delete(n, $event)"
                [disabled]="notificationService.isLoading(n.id)"
              >
                <mat-progress-spinner
                  *ngIf="notificationService.isLoading(n.id)"
                  mode="indeterminate"
                  diameter="16"
                  strokeWidth="2"
                ></mat-progress-spinner>
                <span *ngIf="!notificationService.isLoading(n.id)">Delete</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <ng-template #empty>
        <div class="p-6 text-center text-sm text-muted">No notifications</div>
      </ng-template>

      <div class="mt-4 text-right">
        <button mat-button color="primary" (click)="close()">Close</button>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        min-width: 360px;
        max-width: 720px;
      }
      .text-muted {
        color: rgba(0, 0, 0, 0.6);
      }
    `,
  ],
})
export class NotificationModalComponent implements OnDestroy {
  notifications: Notification[] = [];
  sub: Subscription | null = null;

  constructor(
    private dialogRef: MatDialogRef<NotificationModalComponent>,
    public notificationService: NotificationService,
  ) {
    // Subscribe to notifications
    this.sub = this.notificationService.notifications$.subscribe(
      (n) => (this.notifications = n || []),
    );
  }

  close(): void {
    this.dialogRef.close();
  }

  markAsRead(n: Notification, event?: Event): void {
    if (event) event.stopPropagation();
    this.notificationService.markNotificationAsRead(n.id);
  }

  delete(n: Notification, event?: Event): void {
    if (event) event.stopPropagation();
    this.notificationService.removeNotification(n.id);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}

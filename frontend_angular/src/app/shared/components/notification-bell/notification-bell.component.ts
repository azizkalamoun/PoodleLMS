import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';
import { ViewChild } from '@angular/core';
import { NotificationService, Notification } from '../../../core/services';
import { NotificationModalComponent } from '../notification-modal/notification-modal.component';
import { SnackBarService } from '../../../core/services';
import { ModalConfig } from '../../../core/utils/modal-config';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatMenuModule,
    MatDialogModule,
    NotificationModalComponent,
    MatListModule,
    MatDividerModule,
    MatTooltipModule,
    DatePipe,
  ],
  template: `
    <!-- Bell Button with Badge -->
    <button
      mat-icon-button
      #menuTrigger="matMenuTrigger"
      [matMenuTriggerFor]="notificationMenu"
      class="relative"
      (click)="onBellClick()"
      aria-label="Notifications"
    >
      <mat-icon
        [matBadge]="unreadCount"
        matBadgeColor="warn"
        matBadgeSize="small"
        [matBadgeHidden]="unreadCount === 0"
        class="text-surface-600 hover:text-surface-900 transition-colors"
      >
        notifications
      </mat-icon>
    </button>

    <!-- Notification Dropdown Menu -->
    <mat-menu #notificationMenu="matMenu" class="notification-menu">
      <!-- Header -->
      <div class="px-4 py-3 border-b border-surface-200">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold text-surface-900">Notifications</h3>
          <button
            *ngIf="unreadCount > 0"
            mat-icon-button
            matTooltip="Mark all as read"
            (click)="markAllAsRead()"
            class="!h-8 !w-8"
          >
            <mat-icon class="text-xs">done_all</mat-icon>
          </button>
        </div>
      </div>

      <!-- Notifications List -->
      <div class="max-h-96 overflow-y-auto">
        <ng-container
          *ngIf="
            notifications && notifications.length > 0;
            else noNotifications
          "
        >
          <button
            mat-menu-item
            *ngFor="let notification of notifications"
            (click)="onNotificationClick(notification)"
            [ngClass]="{
              'bg-blue-50': !notification.isRead,
              'bg-white': notification.isRead,
            }"
            class="w-full !h-auto !justify-start px-4 py-3 text-left border-b border-surface-100 hover:bg-surface-50 transition-colors"
          >
            <div class="flex gap-3 w-full">
              <!-- Icon based on notification type -->
              <div class="flex-shrink-0 mt-0.5">
                <mat-icon
                  class="text-base"
                  [ngClass]="getNotificationIcon(notification.type)"
                >
                  {{ getNotificationIconName(notification.type) }}
                </mat-icon>
              </div>

              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start gap-2">
                  <p
                    class="text-xs font-semibold text-surface-900 line-clamp-1"
                    [ngClass]="{ 'font-bold': !notification.isRead }"
                  >
                    {{ notification.title }}
                  </p>
                  <span
                    *ngIf="!notification.isRead"
                    class="flex-shrink-0 w-2 h-2 rounded-full bg-blue-500 mt-1.5"
                  ></span>
                </div>
                <p class="text-xs text-surface-500 line-clamp-2 mt-0.5">
                  {{ notification.message }}
                </p>
                <p class="text-xs text-surface-400 mt-1">
                  {{ notification.createdAt | date: 'short' }}
                </p>
              </div>

              <!-- Actions -->
              <div class="flex-shrink-0 flex gap-1">
                <button
                  *ngIf="!notification.isRead"
                  mat-icon-button
                  matTooltip="Mark as read"
                  (click)="markAsRead(notification, $event)"
                  class="!h-7 !w-7"
                >
                  <mat-icon class="text-xs">done</mat-icon>
                </button>
                <button
                  mat-icon-button
                  matTooltip="Delete"
                  (click)="deleteNotification(notification, $event)"
                  class="!h-7 !w-7"
                >
                  <mat-icon class="text-xs text-red-500">delete</mat-icon>
                </button>
              </div>
            </div>
          </button>
        </ng-container>

        <ng-template #noNotifications>
          <div class="px-4 py-8 text-center">
            <mat-icon class="text-surface-300 text-4xl block mb-2"
              >notifications_none</mat-icon
            >
            <p class="text-sm text-surface-500">No notifications yet</p>
          </div>
        </ng-template>
      </div>

      <!-- Footer -->
      <div class="px-4 py-2 border-t border-surface-200 text-center">
        <button
          mat-button
          *ngIf="notifications && notifications.length > 0"
          (click)="viewAllNotifications()"
          class="text-xs text-blue-600 hover:text-blue-700"
        >
          View All
        </button>
      </div>
    </mat-menu>
  `,
  styles: [
    `
      :host {
        ::ng-deep .notification-menu {
          max-width: 400px !important;
          min-width: 380px !important;
        }
      }

      .text-blue-50 {
        background-color: rgb(239, 246, 255);
      }

      .text-blue-500 {
        color: rgb(59, 130, 246);
      }

      .text-blue-600 {
        color: rgb(37, 99, 235);
      }

      .text-blue-700 {
        color: rgb(29, 78, 216);
      }

      .text-red-500 {
        color: rgb(239, 68, 68);
      }

      :host {
        display: flex;
        align-items: center;
        height: 100%;
      }

      :host ::ng-deep .mat-mdc-icon-button {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 36px;
        width: 36px;
      }
    `,
  ],
})
export class NotificationBellComponent implements OnInit {
  notifications: Notification[] = [];
  unreadCount = 0;

  @ViewChild('menuTrigger') menuTrigger!: MatMenuTrigger;

  constructor(
    private notificationService: NotificationService,
    private notify: SnackBarService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    console.log('NotificationBell component initializing...');

    // Subscribe to notifications
    this.notificationService.notifications$.subscribe((notifications) => {
      console.log('Notifications updated:', notifications);
      this.notifications = notifications || [];
    });

    // Subscribe to unread count
    this.notificationService.unreadCount$.subscribe((count) => {
      console.log('🔢 Unread count updated:', count);
      this.unreadCount = count;
    });

    // Load initial data
    console.log('Loading initial notification data...');
    this.notificationService.loadNotifications();
    this.notificationService.loadUnreadCount();

    console.log('NotificationBell component initialized');
  }

  onBellClick(): void {
    // Refresh notifications when menu opens
    console.log('Bell clicked - refreshing notifications');
    this.notificationService.loadNotifications();

    // Explicitly open the menu
    if (this.menuTrigger) {
      console.log('Opening notification menu');
      setTimeout(() => {
        this.menuTrigger.openMenu();
      }, 0);
    } else {
      console.warn('Menu trigger not found');
    }
  }

  onNotificationClick(notification: Notification): void {
    if (!notification.isRead) {
      this.markAsRead(notification);
    }
  }

  markAsRead(notification: Notification, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.notificationService.markNotificationAsRead(notification.id);
  }

  markAllAsRead(): void {
    this.notificationService.markAllNotificationsAsRead();
    this.notify.success('All notifications marked as read');
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.notificationService.removeNotification(notification.id);
    this.notify.success('Notification deleted');
  }

  viewAllNotifications(): void {
    // Open the notifications modal
    const ref = this.dialog.open(
      NotificationModalComponent,
      ModalConfig.getListDialogConfig(),
    );

    // Optional: add/remove a body class while dialog open (not required because styles.scss has cdk-overlay-dark-backdrop styling)
    ref
      .afterOpened()
      .subscribe(() => document.body.classList.add('notification-modal-open'));
    ref
      .afterClosed()
      .subscribe(() =>
        document.body.classList.remove('notification-modal-open'),
      );
  }

  getNotificationIcon(type: string): string {
    return 'text-surface-600';
  }

  getNotificationIconName(type: string): string {
    switch (type) {
      case 'COURSE_ASSIGNED':
        return 'school';
      case 'DEADLINE_REMINDER':
        return 'schedule';
      case 'COURSE_OVERDUE':
        return 'warning';
      case 'QCM_RESULT':
        return 'assignment';
      case 'CERTIFICATE_READY':
        return 'card_membership';
      case 'SYSTEM_ALERT':
        return 'info';
      default:
        return 'notifications';
    }
  }
}

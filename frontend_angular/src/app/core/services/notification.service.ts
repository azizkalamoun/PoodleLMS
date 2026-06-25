import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Notification {
  id: number;
  title: string;
  message: string;
  type:
    | 'COURSE_ASSIGNED'
    | 'DEADLINE_REMINDER'
    | 'COURSE_OVERDUE'
    | 'QCM_RESULT'
    | 'CERTIFICATE_READY'
    | 'SYSTEM_ALERT';
  createdAt: string;
  isRead: boolean;
  readAt?: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);

  public notifications$ = this.notificationsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Track in-flight operations for optimistic UI
  private loadingIds = new Set<number>();

  isLoading(id: number): boolean {
    return this.loadingIds.has(id);
  }

  /**
   * Get all notifications for the logged-in employee
   */
  getMyNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/me`);
  }

  /**
   * Get unread notifications for the logged-in employee
   */
  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/me/unread`);
  }

  /**
   * Get count of unread notifications
   */
  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/me/unread-count`);
  }

  /**
   * Mark a single notification as read
   */
  /**
   * Mark a single notification as read.
   * Returns the updated notification and the authoritative unreadCount.
   */
  markAsRead(
    notificationId: number,
  ): Observable<{ notification: Notification; unreadCount: number }> {
    return this.http.put<{ notification: Notification; unreadCount: number }>(
      `${this.apiUrl}/${notificationId}/read`,
      {},
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/read-all`, {});
  }

  /**
   * Delete a notification
   */
  deleteNotification(notificationId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${notificationId}`);
  }

  /**
   * Load notifications and update internal state
   */
  loadNotifications(): void {
    console.log('Loading notifications from API...');
    this.getMyNotifications().subscribe({
      next: (notifications) => {
        console.log('Notifications loaded successfully:', notifications);
        this.notificationsSubject.next(notifications);
        // Don't update unread count here - let loadUnreadCount() handle it
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        if (error.status === 401) {
          console.warn('User not authenticated');
        } else if (error.status === 403) {
          console.warn('User not authorized');
        } else if (error.status === 0) {
          console.warn('Network error or backend not responding');
        }
        this.notificationsSubject.next([]);
      },
    });
  }

  /**
   * Load unread count and update internal state
   */
  loadUnreadCount(): void {
    console.log('Loading unread count...');
    this.getUnreadCount().subscribe({
      next: (count) => {
        console.log('Unread count loaded:', count);
        this.unreadCountSubject.next(count);
      },
      error: (error) => {
        console.error('Error loading unread count:', error);
        this.unreadCountSubject.next(0);
      },
    });
  }

  /**
   * Update unread count based on current notifications
   */
  private updateUnreadCount(): void {
    const notifications = this.notificationsSubject.value;
    const unreadCount = notifications.filter((n) => !n.isRead).length;
    this.unreadCountSubject.next(unreadCount);
  }

  /**
   * Mark notification as read and update state
   */
  markNotificationAsRead(notificationId: number): void {
    console.log('Marking notification as read:', notificationId);

    // Optimistic UI: mark locally and show loader
    const notifications = this.notificationsSubject.value;
    const updatedOptimistic = notifications.map((n) =>
      n.id === notificationId
        ? { ...n, isRead: true, readAt: new Date().toISOString() }
        : n,
    );
    this.notificationsSubject.next(updatedOptimistic);
    this.loadingIds.add(notificationId);

    this.markAsRead(notificationId).subscribe({
      next: (resp) => {
        // Update notification with server-provided object if available
        const current = this.notificationsSubject.value;
        const merged = current.map((n) =>
          n.id === notificationId
            ? { ...(resp.notification ?? n), isRead: true }
            : n,
        );
        this.notificationsSubject.next(merged);
        // Set authoritative unread count returned by server
        if (typeof resp.unreadCount === 'number') {
          this.unreadCountSubject.next(resp.unreadCount);
        } else {
          // Fallback to fetch
          this.loadUnreadCount();
        }
        this.loadingIds.delete(notificationId);
        console.log('Notification marked as read:', notificationId);
      },
      error: (error) => {
        console.error('Error marking notification as read:', error);
        // Rollback optimistic change
        const current = this.notificationsSubject.value;
        const rolledBack = current.map((n) =>
          n.id === notificationId
            ? { ...n, isRead: false, readAt: undefined }
            : n,
        );
        this.notificationsSubject.next(rolledBack);
        this.loadingIds.delete(notificationId);
        // Ensure authoritative count
        this.loadUnreadCount();
      },
    });
  }

  /**
   * Mark all notifications as read and update state
   */
  markAllNotificationsAsRead(): void {
    console.log('Marking all notifications as read...');
    this.markAllAsRead().subscribe({
      next: () => {
        const notifications = this.notificationsSubject.value;
        const updated = notifications.map((n) => ({
          ...n,
          isRead: true,
          readAt: new Date().toISOString(),
        }));
        this.notificationsSubject.next(updated);
        // Refresh authoritative unread count
        this.loadUnreadCount();
        console.log('All notifications marked as read');
      },
      error: (error) => {
        console.error('Error marking all as read:', error);
      },
    });
  }

  /**
   * Delete notification and update state
   */
  removeNotification(notificationId: number): void {
    console.log('Removing notification:', notificationId);
    const notifications = this.notificationsSubject.value;
    const updatedOptimistic = notifications.filter(
      (n) => n.id !== notificationId,
    );
    this.notificationsSubject.next(updatedOptimistic);
    this.loadingIds.add(notificationId);

    this.deleteNotification(notificationId).subscribe({
      next: () => {
        // Refresh authoritative unread count
        this.loadUnreadCount();
        this.loadingIds.delete(notificationId);
        console.log('Notification deleted:', notificationId);
      },
      error: (error) => {
        console.error('Error deleting notification:', error);
        // Rollback: re-add (simple strategy: reload notifications)
        this.loadNotifications();
        this.loadingIds.delete(notificationId);
      },
    });
  }
}

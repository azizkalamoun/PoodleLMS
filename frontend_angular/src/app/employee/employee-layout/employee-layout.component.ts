import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';
import { NotificationService } from '../../core/services/notification.service';
import { LogoService, LogoConfig } from '../../core/services/logo.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-employee-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    NotificationBellComponent,
  ],
  templateUrl: './employee-layout.component.html',
})
export class EmployeeLayoutComponent implements OnInit {
  collapsed = signal(false);
  currentUser = this.authService.getCurrentUser();
  logoConfig: LogoConfig = { logoImageUrl: null, useDefaultLogo: false };

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/employee/dashboard' },
    { label: 'My Courses', icon: 'menu_book', route: '/employee/courses' },
    {
      label: 'Certificates',
      icon: 'workspace_premium',
      route: '/employee/certificates',
    },
  ];

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private logoService: LogoService,
  ) {}

  ngOnInit(): void {
    // Subscribe to logo configuration updates
    this.logoService.logoConfig$.subscribe((config) => {
      this.logoConfig = config;
    });

    // Load notifications on component init
    this.notificationService.loadNotifications();
    this.notificationService.loadUnreadCount();
  }

  toggleSidebar(): void {
    this.collapsed.set(!this.collapsed());
  }

  getUserInitials(): string {
    const u = this.currentUser;
    if (!u) return '?';
    return (u.firstName?.charAt(0) || '') + (u.lastName?.charAt(0) || '');
  }

  logout(): void {
    this.authService.logout();
  }
}

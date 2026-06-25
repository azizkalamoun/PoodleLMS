import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';
import { AnalyticsService } from '../../core/services/analytics.service';
import { AnalyticsOverview } from '../../models';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    RouterModule,
    SkeletonComponent,
    PageHeaderComponent,
  ],
  templateUrl: './admin-dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
  overview: AnalyticsOverview | null = null;
  loading = true;
  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Dashboard' },
  ];

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.analyticsService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        console.error('Failed to load analytics overview');
      },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatExpansionModule } from '@angular/material/expansion';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AnalyticsService } from '../../core/services/analytics.service';
import {
  AnalyticsOverview,
  CourseAnalytics,
  OverdueEnrollment,
  FailedQuestion,
} from '../../models';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-admin-analytics',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatTableModule,
    MatTabsModule,
    MatExpansionModule,
    NgChartsModule,
    SkeletonComponent,
    PageHeaderComponent,
    EmptyStateComponent,
  ],
  templateUrl: './admin-analytics.component.html',
})
export class AdminAnalyticsComponent implements OnInit {
  loading = true;
  overview: AnalyticsOverview | null = null;
  courseAnalytics: CourseAnalytics[] = [];
  overdueEnrollments: OverdueEnrollment[] = [];
  failedQuestions: FailedQuestion[] = [];
  breadcrumbs = [
    { label: 'Admin', route: '/admin/dashboard' },
    { label: 'Analytics' },
  ];

  sectionsExpanded = {
    kpi: true,
    charts: false,
    overdue: false,
    failed: false,
  };

  overdueColumns = [
    'employeeName',
    'courseTitle',
    'departmentName',
    'deadline',
  ];
  failedColumns = [
    'questionText',
    'courseTitle',
    'sectionTitle',
    'failureRate',
  ];

  // Chart configs
  barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true },
    },
    scales: {
      y: { beginAtZero: true, max: 100 },
    },
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true },
    },
    scales: {
      y: { beginAtZero: true, max: 100 },
    },
  };

  doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: true,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          padding: 20,
          font: { size: 12 },
        },
      },
    },
  };

  completionChartData: ChartData<'line'> = { labels: [], datasets: [] };
  passRateChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  avgScoreChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  enrollmentChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.loading = true;
    let completed = 0;
    const checkDone = () => {
      completed++;
      if (completed >= 4) this.loading = false;
    };

    this.analyticsService.getOverview().subscribe({
      next: (data) => {
        this.overview = data;
        checkDone();
      },
      error: () => checkDone(),
    });

    this.analyticsService.getCourseAnalytics().subscribe({
      next: (data) => {
        this.courseAnalytics = data;
        this.buildCharts(data);
        checkDone();
      },
      error: () => checkDone(),
    });

    this.analyticsService.getOverdueEnrollments().subscribe({
      next: (data) => {
        this.overdueEnrollments = data;
        checkDone();
      },
      error: () => checkDone(),
    });

    this.analyticsService.getMostFailedQuestions().subscribe({
      next: (data) => {
        this.failedQuestions = data;
        checkDone();
      },
      error: () => checkDone(),
    });
  }

  private buildCharts(data: CourseAnalytics[]): void {
    const labels = data.map((c) => c.courseTitle);

    this.completionChartData = {
      labels,
      datasets: [
        {
          data: data.map((c) => c.completionRate),
          label: 'Completion %',
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          fill: true,
          tension: 0.4,
          borderWidth: 2,
          pointRadius: 4,
          pointBackgroundColor: '#3b82f6',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
        },
      ],
    };

    this.passRateChartData = {
      labels,
      datasets: [
        {
          data: data.map((c) => c.passRate),
          label: 'Pass Rate %',
          backgroundColor: '#22c55e',
          borderColor: '#16a34a',
        },
      ],
    };

    this.avgScoreChartData = {
      labels,
      datasets: [
        {
          data: data.map((c) => c.averageScore),
          label: 'Average Score',
          backgroundColor: '#8b5cf6',
          borderColor: '#7c3aed',
          borderWidth: 1,
        },
      ],
    };

    this.enrollmentChartData = {
      labels,
      datasets: [
        {
          data: data.map((c) => c.enrollmentCount),
          backgroundColor: [
            '#3b82f6',
            '#22c55e',
            '#f59e0b',
            '#ef4444',
            '#8b5cf6',
            '#ec4899',
            '#06b6d4',
            '#84cc16',
            '#f97316',
            '#6366f1',
          ],
          borderColor: '#fff',
          borderWidth: 2,
        },
      ],
    };
  }
}

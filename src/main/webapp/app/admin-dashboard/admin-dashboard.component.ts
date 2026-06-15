import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

interface AdminStats {
  totalUsers: number;
  totalDoctors: number;
  totalPatients: number;
  cardiacPatients: number;
  diabetesPatients: number;
  respiratoryPatients: number;
  anomaliesToday: number;
}

@Component({
  selector: 'jhi-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss',
})
export default class AdminDashboardComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  stats: AdminStats | null = null;

  ngOnInit(): void {
    this.http.get<AdminStats>(this.appConfig.getEndpointFor('api/admin/dashboard-stats')).subscribe({
      next: data => (this.stats = data),
    });
  }
}

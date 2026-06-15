import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { PatientDashboardService, DashboardData, Reading } from './patient-dashboard.service';
import { PatientMonitorWebSocketService } from '../core/tracker/patient-monitor-websocket.service';
import { AccountService } from '../core/auth/account.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

interface VitalConfig {
  key: string;
  label: string;
  unit: string;
  thresholds: { low?: number; high?: number; criticalLow?: number; criticalHigh?: number };
  description: string;
  normalRange: string;
  color: string;
}

@Component({
  selector: 'jhi-patient-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './patient-dashboard.component.html',
  styleUrl: './patient-dashboard.component.scss',
})
export default class PatientDashboardComponent implements OnInit, OnDestroy {
  private dashboardService = inject(PatientDashboardService);
  private wsService = inject(PatientMonitorWebSocketService);
  private accountService = inject(AccountService);

  data: DashboardData | null = null;
  loading = true;
  error = false;
  selectedInterval = '1d';
  intervals = [
    { value: '1h', label: '1 Ora' },
    { value: '1d', label: '1 Zi' },
    { value: '7d', label: '7 Zile' },
    { value: '30d', label: '30 Zile' },
  ];

  private charts: Chart[] = [];
  private wsSub: Subscription | null = null;
  private notifSub: Subscription | null = null;
  private reloadTimeout: any = null;
  vitalConfigs: VitalConfig[] = [];

  ngOnInit(): void {
    this.loadData();
    this.setupWebSocket();
  }

  ngOnDestroy(): void {
    this.destroyCharts();
    this.wsSub?.unsubscribe();
    this.notifSub?.unsubscribe();
    if (this.reloadTimeout) clearTimeout(this.reloadTimeout);
    this.wsService.disconnect();
  }

  private setupWebSocket(): void {
    this.wsService.connect();

    this.accountService.identity().subscribe(account => {
      if (account) {
        this.wsService.subscribeToNotifications(account.login);
        this.wsService.subscribeToDashboardUpdates();
      }
    });

    this.wsSub = this.wsService.medicalData$.subscribe(() => {
      this.debouncedReload();
    });

    this.notifSub = this.wsService.dashboardUpdates$.subscribe(() => {
      this.debouncedReload();
    });
  }

  private debouncedReload(): void {
    if (this.reloadTimeout) return; // already scheduled
    this.reloadTimeout = setTimeout(() => {
      this.reloadTimeout = null;
      this.loadData();
    }, 5000);
  }

  selectInterval(interval: string): void {
    this.selectedInterval = interval;
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = false;
    this.dashboardService.getDashboardData(this.selectedInterval).subscribe({
      next: data => {
        this.data = data;
        this.vitalConfigs = this.getVitalConfigs(data.patientType);
        this.loading = false;
        setTimeout(() => this.createCharts(), 100);
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  getStatusColor(config: VitalConfig, value: number | null | undefined): string {
    if (value == null) return 'secondary';
    if (config.thresholds.criticalHigh && value >= config.thresholds.criticalHigh) return 'danger';
    if (config.thresholds.criticalLow && value <= config.thresholds.criticalLow) return 'danger';
    if (config.thresholds.high && value >= config.thresholds.high) return 'warning';
    if (config.thresholds.low && value <= config.thresholds.low) return 'warning';
    return 'success';
  }

  getLatestValue(key: string): number | null {
    if (!this.data?.latest) return null;
    return (this.data.latest as any)[key] ?? null;
  }

  private getVitalConfigs(patientType: string): VitalConfig[] {
    const common: VitalConfig[] = [
      {
        key: 'heartRate',
        label: 'Puls',
        unit: 'bpm',
        thresholds: { low: 60, high: 100, criticalLow: 50, criticalHigh: 120 },
        description: 'Frecventa cardiaca.',
        normalRange: '60-100 bpm',
        color: '#e74c3c',
      },
      {
        key: 'spo2',
        label: 'SpO2',
        unit: '%',
        thresholds: { low: 93, criticalLow: 90 },
        description: 'Saturatia de oxigen din sange.',
        normalRange: '95-100%',
        color: '#3498db',
      },
      {
        key: 'temperature',
        label: 'Temperatura',
        unit: '°C',
        thresholds: { low: 35.5, high: 37.5, criticalHigh: 38.5 },
        description: 'Temperatura corporala.',
        normalRange: '36.1-37.2°C',
        color: '#e67e22',
      },
      {
        key: 'systolicBp',
        label: 'Tensiune Sistolica',
        unit: 'mmHg',
        thresholds: { low: 90, high: 140, criticalHigh: 180 },
        description: 'Presiunea sistolica.',
        normalRange: '90-140 mmHg',
        color: '#9b59b6',
      },
      {
        key: 'respiratoryRate',
        label: 'Respiratie',
        unit: 'resp/min',
        thresholds: { low: 10, high: 25 },
        description: 'Numarul de respiratii pe minut.',
        normalRange: '12-20 resp/min',
        color: '#1abc9c',
      },
    ];
    const cardiac: VitalConfig[] = [
      {
        key: 'hrv',
        label: 'HRV',
        unit: 'ms',
        thresholds: { criticalLow: 15, low: 30 },
        description: 'Variabilitatea ritmului cardiac.',
        normalRange: '30-70 ms',
        color: '#f39c12',
      },
      {
        key: 'qtInterval',
        label: 'Interval QT',
        unit: 'ms',
        thresholds: { high: 470, criticalHigh: 500 },
        description: 'Durata repolarizarii ventriculare.',
        normalRange: '350-450 ms',
        color: '#c0392b',
      },
      {
        key: 'bnp',
        label: 'BNP',
        unit: 'pg/mL',
        thresholds: { high: 400, criticalHigh: 2000 },
        description: 'Peptid natriuretic cerebral.',
        normalRange: '<100 pg/mL',
        color: '#8e44ad',
      },
    ];
    const diabetes: VitalConfig[] = [
      {
        key: 'bloodGlucose',
        label: 'Glicemie',
        unit: 'mg/dL',
        thresholds: { criticalLow: 54, low: 70, high: 180, criticalHigh: 250 },
        description: 'Nivelul zaharului din sange.',
        normalRange: '70-140 mg/dL',
        color: '#e74c3c',
      },
    ];
    const respiratory: VitalConfig[] = [
      {
        key: 'fev1',
        label: 'FEV1',
        unit: '% baseline',
        thresholds: { criticalLow: 30, low: 50 },
        description: 'Volumul expirator fortat.',
        normalRange: '>80% din baseline',
        color: '#27ae60',
      },
      {
        key: 'etco2',
        label: 'EtCO2',
        unit: 'mmHg',
        thresholds: { high: 50, criticalHigh: 70 },
        description: 'CO2 la sfarsitul expiratiei.',
        normalRange: '35-45 mmHg',
        color: '#d35400',
      },
    ];

    switch (patientType) {
      case 'CARDIAC':
        return [...common, ...cardiac];
      case 'DIABETES':
        return [...common, ...diabetes];
      case 'RESPIRATORY':
        return [...common, ...respiratory];
      default:
        return common;
    }
  }

  private destroyCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  private createCharts(): void {
    this.destroyCharts();
    if (!this.data) return;

    const aggregated = this.aggregateSeries(this.data.series, this.selectedInterval);

    for (const config of this.vitalConfigs) {
      const canvas = document.getElementById(`chart-${config.key}`) as HTMLCanvasElement;
      if (!canvas) continue;

      const values = aggregated.map(r => (r as any)[config.key]).filter((v: any) => v != null);
      const labels = aggregated.filter(r => (r as any)[config.key] != null).map(r => this.formatTimestamp(r.timestamp));
      const pointColors = aggregated.filter(r => (r as any)[config.key] != null).map(r => (r.isAnomaly ? '#e74c3c' : 'transparent'));
      const pointRadius = aggregated.filter(r => (r as any)[config.key] != null).map(r => (r.isAnomaly ? 5 : 1));

      const datasets: any[] = [
        {
          label: config.label,
          data: values,
          borderColor: config.color,
          backgroundColor: config.color + '20',
          fill: true,
          tension: 0.3,
          borderWidth: 2,
          pointBackgroundColor: pointColors,
          pointRadius,
          pointHoverRadius: 5,
        },
      ];

      if (config.thresholds.high)
        datasets.push({
          label: `Limita (${config.thresholds.high})`,
          data: Array(values.length).fill(config.thresholds.high),
          borderColor: '#f39c12',
          borderDash: [5, 5],
          borderWidth: 1,
          pointRadius: 0,
          fill: false,
        });
      if (config.thresholds.low)
        datasets.push({
          label: `Limita (${config.thresholds.low})`,
          data: Array(values.length).fill(config.thresholds.low),
          borderColor: '#f39c12',
          borderDash: [5, 5],
          borderWidth: 1,
          pointRadius: 0,
          fill: false,
        });
      if (config.thresholds.criticalHigh)
        datasets.push({
          label: `Critic (${config.thresholds.criticalHigh})`,
          data: Array(values.length).fill(config.thresholds.criticalHigh),
          borderColor: '#e74c3c',
          borderDash: [3, 3],
          borderWidth: 1,
          pointRadius: 0,
          fill: false,
        });
      if (config.thresholds.criticalLow)
        datasets.push({
          label: `Critic (${config.thresholds.criticalLow})`,
          data: Array(values.length).fill(config.thresholds.criticalLow),
          borderColor: '#e74c3c',
          borderDash: [3, 3],
          borderWidth: 1,
          pointRadius: 0,
          fill: false,
        });

      const chart = new Chart(canvas, {
        type: 'line',
        data: { labels, datasets },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: true, position: 'bottom', labels: { boxWidth: 10, font: { size: 10 }, color: '#64748b', padding: 12 } },
            tooltip: {
              backgroundColor: '#1e293b',
              titleColor: '#e2e8f0',
              bodyColor: '#94a3b8',
              borderColor: 'rgba(255,255,255,0.1)',
              borderWidth: 1,
              cornerRadius: 8,
              padding: 10,
            },
          },
          scales: {
            x: {
              ticks: { maxTicksLimit: 8, font: { size: 10 }, color: '#475569' },
              grid: { display: false },
              border: { color: 'rgba(255,255,255,0.06)' },
            },
            y: {
              ticks: { font: { size: 10 }, color: '#475569' },
              grid: { color: 'rgba(255,255,255,0.04)' },
              border: { color: 'rgba(255,255,255,0.06)' },
            },
          },
          interaction: { intersect: false, mode: 'index' },
        },
      });
      this.charts.push(chart);
    }
  }

  private aggregateSeries(series: Reading[], interval: string): Reading[] {
    if (interval === '1h' || interval === '1d') return series;
    const bucketMinutes = interval === '7d' ? 60 : 240;
    const buckets = new Map<number, Reading[]>();
    for (const r of series) {
      const ts = new Date(r.timestamp).getTime();
      const bucketKey = Math.floor(ts / (bucketMinutes * 60 * 1000));
      if (!buckets.has(bucketKey)) buckets.set(bucketKey, []);
      buckets.get(bucketKey)!.push(r);
    }
    const result: Reading[] = [];
    for (const key of Array.from(buckets.keys()).sort()) {
      const group = buckets.get(key)!;
      const avg: any = { timestamp: group[Math.floor(group.length / 2)].timestamp, isAnomaly: false, anomalyScore: 0 };
      const numericKeys = [
        'heartRate',
        'spo2',
        'temperature',
        'systolicBp',
        'diastolicBp',
        'respiratoryRate',
        'hrv',
        'qtInterval',
        'bnp',
        'bloodGlucose',
        'fev1',
        'etco2',
      ];
      for (const k of numericKeys) {
        const vals = group.map((r: any) => r[k]).filter((v: any) => v != null);
        avg[k] = vals.length > 0 ? Math.round((vals.reduce((a: number, b: number) => a + b, 0) / vals.length) * 10) / 10 : null;
      }
      avg.isAnomaly = group.some(r => r.isAnomaly);
      if (avg.isAnomaly) avg.anomalyScore = Math.max(...group.filter(r => r.isAnomaly).map(r => r.anomalyScore ?? 0));
      result.push(avg as Reading);
    }
    return result;
  }

  private formatTimestamp(ts: string): string {
    const d = new Date(ts);
    if (this.selectedInterval === '1h' || this.selectedInterval === '1d') {
      return d.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
    }
    return (
      d.toLocaleDateString('ro-RO', { day: '2-digit', month: '2-digit' }) +
      ' ' +
      d.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })
    );
  }
}

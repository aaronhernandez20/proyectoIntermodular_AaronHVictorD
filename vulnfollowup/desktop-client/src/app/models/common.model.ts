import { Severity, VulnStatus } from './enums.model';

/** Spring Data Page format. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** Error body returned by the backend for every error response. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[];
}

export interface DashboardSummary {
  total: number;
  bySeverity: Record<Severity, number>;
  byStatus: Record<VulnStatus, number>;
  overdue: number;
}

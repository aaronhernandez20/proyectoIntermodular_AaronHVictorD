import { JobStatus, Severity, VulnStatus } from './enums.model';

/** Paginated response of the API. */
export interface Page<T> {
  content: T[];
  /** 0-based page number. */
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Error body returned by the backend for every error response. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  /** Only in validation errors (400): field → message. */
  fields?: Record<string, string>;
}

/** GET /api/dashboard/summary */
export interface DashboardSummary {
  total: number;
  bySeverity: Record<Severity, number>;
  byStatus: Record<VulnStatus, number>;
  overdue: number;
}

/** POST /api/imports/vulnerabilities and POST /api/reports → 202 */
export interface JobAccepted {
  jobId: string;
}

/** GET /api/imports/{jobId} */
export interface ImportJob {
  status: JobStatus;
  imported: number;
  errors: string[];
}

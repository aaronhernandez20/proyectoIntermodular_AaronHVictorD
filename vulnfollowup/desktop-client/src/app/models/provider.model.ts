import { AssetRef } from './asset.model';
import { Criticality, Severity, VulnStatus } from './enums.model';

/** GET /api/providers (ICT providers, DORA) */
export interface Provider {
  id: number;
  name: string;
  serviceProvided: string;
  criticality: Criticality;
  contractRef: string;
  nextReviewDate: string;
  contactName: string;
  contactEmail: string;
  /** Business rule calculated by the backend. */
  atRisk: boolean;
  assets: AssetRef[];
}

/** POST /api/providers · PUT /api/providers/{id} (ADMIN only) */
export interface ProviderRequest {
  name: string;
  serviceProvided: string;
  criticality: Criticality;
  contractRef: string;
  nextReviewDate: string;
  contactName: string;
  contactEmail: string;
  assetIds: number[];
}

/** GET /api/providers/{id}/report: pending vulnerabilities, most severe first. */
export interface ProviderReport {
  provider: string;
  criticality: Criticality;
  atRisk: boolean;
  riskReasons: string[];
  pending: {
    code: string;
    title: string;
    cvssScore: number;
    severity: Severity;
    status: VulnStatus;
    asset: string;
    targetDate: string | null;
  }[];
}

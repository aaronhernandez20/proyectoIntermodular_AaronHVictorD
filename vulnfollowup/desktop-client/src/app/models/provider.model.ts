import { AssetRef } from './asset.model';
import { Criticality, Severity, VulnStatus } from './enums.model';

export interface Provider {
  id: number;
  name: string;
  service: string;
  criticality: Criticality;
  contractRef: string;
  reviewDate: string;
  contact: string;
  assets: AssetRef[];
  atRisk: boolean;
}

export interface ProviderRequest {
  name: string;
  service: string;
  criticality: Criticality;
  contractRef: string;
  reviewDate: string;
  contact: string;
  assetIds: number[];
}

export interface ProviderReport {
  provider: Pick<Provider, 'id' | 'name' | 'criticality' | 'reviewDate'>;
  atRisk: boolean;
  riskReasons: string[];
  pending: {
    id: number;
    code: string;
    title: string;
    score: number;
    severity: Severity;
    status: VulnStatus;
    targetDate: string | null;
    assetName: string;
  }[];
}

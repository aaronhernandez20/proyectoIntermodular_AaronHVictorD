import { AssetStatus, AssetType, Environment } from './enums.model';

export interface Asset {
  id: number;
  name: string;
  type: AssetType;
  owner: string;
  environment: Environment;
  status: AssetStatus;
  providerIds: number[];
}

export type AssetRequest = Omit<Asset, 'id' | 'providerIds'>;

/** Short reference used inside other resources. */
export interface AssetRef {
  id: number;
  name: string;
}

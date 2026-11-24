import { AssetStatus, AssetType, Environment, Exposure } from './enums.model';

/** GET /api/assets and GET /api/assets/{id} */
export interface Asset {
  id: number;
  name: string;
  type: AssetType;
  owner: string;
  environment: Environment;
  status: AssetStatus;
  exposure: Exposure;
  providerId: number | null;
  providerName: string | null;
  /** Optimistic-locking counter: send it back in PUT. */
  version: number;
  createdAt: string;
  updatedAt: string;
}

/** POST /api/assets (ADMIN, ANALYST) */
export interface AssetRequest {
  name: string;
  type: AssetType;
  owner: string;
  environment: Environment;
  /** Optional: INTERNAL by default. */
  exposure?: Exposure;
  /** Optional: linked if a provider with that name exists. */
  providerName?: string | null;
}

/** PUT /api/assets/{id} (ADMIN, ANALYST): all the fields + the version read in the GET. */
export interface AssetUpdateRequest {
  name: string;
  type: AssetType;
  owner: string;
  environment: Environment;
  exposure: Exposure;
  status: AssetStatus;
  version: number;
}

/** Filters of GET /api/assets (all optional; owner is a partial match). */
export interface AssetFilters {
  type?: AssetType;
  environment?: Environment;
  status?: AssetStatus;
  owner?: string;
}

/** Short reference used inside other resources. */
export interface AssetRef {
  id: number;
  name: string;
}

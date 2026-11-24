// Enumerations shared with the backend (see docs/api/README.md).
export type Role = 'ADMIN' | 'ANALYST' | 'VIEWER';
export type VulnStatus = 'OPEN' | 'PENDING_REMEDIATION' | 'RESOLVED' | 'CLOSED';
export type Severity = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type CvssVersion = '3.1' | '4.0';
export type AssetType = 'API' | 'SERVER' | 'DATABASE' | 'WEB_APP' | 'MOBILE_APP' | 'NETWORK' | 'OTHER';
export type Environment = 'PRODUCTION' | 'STAGING' | 'TEST' | 'DEVELOPMENT';
export type AssetStatus = 'ACTIVE' | 'INACTIVE';
/** Internet-facing assets get more operational priority with the same CVSS. */
export type Exposure = 'INTERNET' | 'INTERNAL';
export type Criticality = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type JobStatus = 'RUNNING' | 'DONE' | 'FAILED';

export const ROLES: readonly Role[] = ['ADMIN', 'ANALYST', 'VIEWER'];
export const VULN_STATUSES: readonly VulnStatus[] = ['OPEN', 'PENDING_REMEDIATION', 'RESOLVED', 'CLOSED'];
export const SEVERITIES: readonly Severity[] = ['NONE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
export const CVSS_VERSIONS: readonly CvssVersion[] = ['3.1', '4.0'];
export const ASSET_TYPES: readonly AssetType[] = ['API', 'SERVER', 'DATABASE', 'WEB_APP', 'MOBILE_APP', 'NETWORK', 'OTHER'];
export const ENVIRONMENTS: readonly Environment[] = ['PRODUCTION', 'STAGING', 'TEST', 'DEVELOPMENT'];
export const EXPOSURES: readonly Exposure[] = ['INTERNET', 'INTERNAL'];
export const CRITICALITIES: readonly Criticality[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

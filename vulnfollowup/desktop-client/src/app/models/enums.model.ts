// Enumerations shared with the backend (see docs/api/api-contract.md).
export type Role = 'ADMIN' | 'ANALYST' | 'VIEWER';
export type VulnStatus = 'OPEN' | 'PENDING_REMEDIATION' | 'RESOLVED' | 'CLOSED';
export type Severity = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type CvssVersion = '3.1' | '4.0';
export type AssetType = 'API' | 'SERVER' | 'DATABASE' | 'WEB_APP' | 'NETWORK' | 'OTHER';
export type Environment = 'PRODUCTION' | 'STAGING' | 'DEVELOPMENT';
export type AssetStatus = 'ACTIVE' | 'INACTIVE';
export type Criticality = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export const ROLES: readonly Role[] = ['ADMIN', 'ANALYST', 'VIEWER'];
export const VULN_STATUSES: readonly VulnStatus[] = ['OPEN', 'PENDING_REMEDIATION', 'RESOLVED', 'CLOSED'];
export const SEVERITIES: readonly Severity[] = ['NONE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
export const CVSS_VERSIONS: readonly CvssVersion[] = ['3.1', '4.0'];

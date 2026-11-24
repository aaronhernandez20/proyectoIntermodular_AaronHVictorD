import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { CvssVersion, Severity } from '../../models';

/**
 * CVSS Base rules used by VulnFollowUp (statement, section 3).
 * We do NOT calculate the score from the vector (the official FIRST calculator is used);
 * we only check that version, vector, score and severity are coherent.
 * The backend applies the same rules: this client-side check is only for usability.
 */

/** Base vector of CVSS v3.1: the 8 base metrics in their official order. */
const VECTOR_31 =
  /^CVSS:3\.1\/AV:[NALP]\/AC:[LH]\/PR:[NLH]\/UI:[NR]\/S:[UC]\/C:[NLH]\/I:[NLH]\/A:[NLH]$/;

/** Base vector of CVSS v4.0: the 11 base metrics in their official order. */
const VECTOR_40 =
  /^CVSS:4\.0\/AV:[NALP]\/AC:[LH]\/AT:[NP]\/PR:[NLH]\/UI:[NPA]\/VC:[HLN]\/VI:[HLN]\/VA:[HLN]\/SC:[HLN]\/SI:[HLN]\/SA:[HLN]$/;

/** Qualitative severity scale (statement, table 2). */
export function severityFromScore(score: number): Severity {
  if (score === 0) return 'NONE';
  if (score < 4) return 'LOW';
  if (score < 7) return 'MEDIUM';
  if (score < 9) return 'HIGH';
  return 'CRITICAL';
}

/** Score between 0.0 and 10.0 with at most one decimal. */
export function isValidScore(score: unknown): score is number {
  if (typeof score !== 'number' || Number.isNaN(score)) return false;
  if (score < 0 || score > 10) return false;
  return Math.abs(score * 10 - Math.round(score * 10)) < 1e-9;
}

/** Version that a vector declares in its prefix, or null if it has no known prefix. */
export function versionFromVector(vector: string): CvssVersion | null {
  if (vector.startsWith('CVSS:3.1/')) return '3.1';
  if (vector.startsWith('CVSS:4.0/')) return '4.0';
  return null;
}

export function isValidVector(version: CvssVersion, vector: string): boolean {
  return (version === '3.1' ? VECTOR_31 : VECTOR_40).test(vector);
}

export interface CvssInput {
  version: CvssVersion | null;
  vector: string | null;
  score: number | null;
  severity?: Severity | null;
}

/** Error keys (also translation keys under "cvss.errors"). */
export type CvssErrorKey =
  | 'versionRequired'
  | 'vectorRequired'
  | 'versionMismatch'
  | 'vectorFormat'
  | 'scoreRange'
  | 'severityMismatch';

/** Returns the list of coherence errors; an empty list means the data is valid. */
export function validateCvss(input: CvssInput): CvssErrorKey[] {
  const errors: CvssErrorKey[] = [];
  const vector = (input.vector ?? '').trim();

  if (!input.version) errors.push('versionRequired');
  if (!vector) errors.push('vectorRequired');

  if (input.version && vector) {
    const declared = versionFromVector(vector);
    if (declared !== null && declared !== input.version) {
      errors.push('versionMismatch');
    } else if (!isValidVector(input.version, vector)) {
      errors.push('vectorFormat');
    }
  }

  if (!isValidScore(input.score)) {
    errors.push('scoreRange');
  } else if (input.severity && input.severity !== severityFromScore(input.score)) {
    errors.push('severityMismatch');
  }

  return errors;
}

/**
 * Validator for a FormGroup with the controls cvssVersion, cvssVector, cvssScore
 * (and optionally severity; the API derives it from the score anyway).
 * Errors are returned as { cvss: ['versionMismatch', ...] }.
 */
export function cvssGroupValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const raw = group.value as {
      cvssVersion?: CvssVersion | null;
      cvssVector?: string | null;
      cvssScore?: number | string | null;
      severity?: Severity | null;
    };
    const score =
      raw.cvssScore === null || raw.cvssScore === undefined || raw.cvssScore === '' ? null : Number(raw.cvssScore);
    const errors = validateCvss({
      version: raw.cvssVersion ?? null,
      vector: raw.cvssVector ?? null,
      score,
      severity: raw.severity ?? null,
    });
    return errors.length ? { cvss: errors } : null;
  };
}

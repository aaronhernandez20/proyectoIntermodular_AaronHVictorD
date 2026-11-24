import { FormControl, FormGroup } from '@angular/forms';
import {
  cvssGroupValidator,
  isValidScore,
  isValidVector,
  severityFromScore,
  validateCvss,
  versionFromVector,
} from './cvss.util';

const V31_CRITICAL = 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H';
const V40_CRITICAL = 'CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N';

describe('CVSS utilities', () => {
  describe('severityFromScore (table 2 of the statement)', () => {
    it('maps every boundary of the qualitative scale', () => {
      expect(severityFromScore(0)).toBe('NONE');
      expect(severityFromScore(0.1)).toBe('LOW');
      expect(severityFromScore(3.9)).toBe('LOW');
      expect(severityFromScore(4.0)).toBe('MEDIUM');
      expect(severityFromScore(6.9)).toBe('MEDIUM');
      expect(severityFromScore(7.0)).toBe('HIGH');
      expect(severityFromScore(8.9)).toBe('HIGH');
      expect(severityFromScore(9.0)).toBe('CRITICAL');
      expect(severityFromScore(10)).toBe('CRITICAL');
    });
  });

  describe('isValidScore', () => {
    it('accepts 0.0 to 10.0 with one decimal', () => {
      expect(isValidScore(0)).toBe(true);
      expect(isValidScore(9.8)).toBe(true);
      expect(isValidScore(10)).toBe(true);
    });
    it('rejects out of range, extra decimals and non numbers', () => {
      expect(isValidScore(-0.1)).toBe(false);
      expect(isValidScore(10.1)).toBe(false);
      expect(isValidScore(9.85)).toBe(false);
      expect(isValidScore(NaN)).toBe(false);
      expect(isValidScore('9.8')).toBe(false);
    });
  });

  describe('vectors', () => {
    it('detects the version from the prefix', () => {
      expect(versionFromVector(V31_CRITICAL)).toBe('3.1');
      expect(versionFromVector(V40_CRITICAL)).toBe('4.0');
      expect(versionFromVector('AV:N/AC:L')).toBeNull();
    });
    it('validates the base metrics of each version', () => {
      expect(isValidVector('3.1', V31_CRITICAL)).toBe(true);
      expect(isValidVector('4.0', V40_CRITICAL)).toBe(true);
      expect(isValidVector('3.1', 'CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H')).toBe(false); // A missing
      expect(isValidVector('3.1', 'CVSS:3.1/AV:X/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H')).toBe(false); // bad value
    });
  });

  describe('validateCvss', () => {
    it('accepts the example of the statement (3.1, 9.8, critical)', () => {
      expect(
        validateCvss({ version: '3.1', vector: V31_CRITICAL, score: 9.8, severity: 'CRITICAL' }),
      ).toEqual([]);
    });

    it('rejects version 4.0 with a vector that starts with CVSS:3.1 (statement example)', () => {
      expect(validateCvss({ version: '4.0', vector: V31_CRITICAL, score: 9.8, severity: 'CRITICAL' })).toEqual([
        'versionMismatch',
      ]);
    });

    it('rejects severity LOW with score 9.8 (statement example)', () => {
      expect(validateCvss({ version: '3.1', vector: V31_CRITICAL, score: 9.8, severity: 'LOW' })).toEqual([
        'severityMismatch',
      ]);
    });

    it('reports missing fields', () => {
      expect(validateCvss({ version: null, vector: '', score: null })).toEqual([
        'versionRequired',
        'vectorRequired',
        'scoreRange',
      ]);
    });
  });

  describe('cvssGroupValidator', () => {
    const build = (version: string, vector: string, score: number | string, severity: string) =>
      new FormGroup(
        {
          cvssVersion: new FormControl(version),
          cvssVector: new FormControl(vector),
          score: new FormControl(score),
          severity: new FormControl(severity),
        },
        { validators: cvssGroupValidator() },
      );

    it('is valid for coherent data (score as text from an input)', () => {
      expect(build('4.0', V40_CRITICAL, '9.3', 'CRITICAL').valid).toBe(true);
    });

    it('exposes the error list under the "cvss" key', () => {
      const form = build('4.0', V31_CRITICAL, 9.8, 'CRITICAL');
      expect(form.errors).toEqual({ cvss: ['versionMismatch'] });
    });
  });
});

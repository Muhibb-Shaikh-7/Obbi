# Security Policy for Obby

## Overview

Obby takes security seriously and implements multiple layers of protection for your sensitive data.
This document outlines our security practices and how we protect your notes.

## Encryption Features

### 1. Note-Level Encryption

**Algorithm**: AES-256-GCM (Galois/Counter Mode)

- Industry-standard authenticated encryption
- Provides both confidentiality and integrity
- Military-grade security

**Key Derivation**: PBKDF2-HMAC-SHA256

- 65,536 iterations (NIST recommended minimum)
- 256-bit key length
- Protects against brute-force attacks

**Implementation Details**:

- Each note has a unique 32-byte random salt
- Each encryption operation uses a unique 12-byte IV (Initialization Vector)
- Password is never stored - only the user knows it
- No password recovery mechanism - forgotten passwords cannot be recovered

**Usage**:

- Encrypt individual notes with a password
- Each note can have a different password
- Encrypted notes cannot be searched or edited until decrypted

### 2. Hidden Folder with Password Protection 🆕

**Overview**:
The Hidden Folder feature provides a secure, password-protected space for sensitive notes with
advanced security measures.

**Security Architecture**:

#### Password Hashing

- **Algorithm**: PBKDF2-HMAC-SHA256
- **Iterations**: 150,000 (significantly exceeds NIST recommendations)
- **Key Length**: 256 bits
- **Salt**: 32-byte cryptographically secure random salt per password
- **Storage**: Only the hash and salt are stored - never the password itself

#### Secure Storage

- **Encryption**: Android EncryptedSharedPreferences with AES256_GCM
- **Key Management**: Android Keystore (hardware-backed when available)
- **Encryption Scheme**:
    - Keys: AES256_SIV
    - Values: AES256_GCM

#### Authentication & Access Control

**Lockout Policy**:

- Maximum 5 failed attempts before lockout
- First lockout: 2 minutes
- Subsequent lockouts: Exponential backoff (2x multiplier)
- Example: 5 failures = 2 min, 10 failures = 4 min, 15 failures = 8 min

**Auto-Lock**:

- Hidden folder automatically locks after 2 minutes of inactivity
- Manual lock available via lock button
- Locks immediately when app is backgrounded or closed

#### Password Requirements

**Minimum Complexity**:

- At least 8 characters
- Cannot be all digits
- Should include mix of:
    - Lowercase letters
    - Uppercase letters
    - Numbers
    - Special characters

**Password Strength Indicator**:

- Real-time feedback during password creation
- Strength levels: Weak (0-40%), Medium (40-70%), Strong (70-100%)
- Based on length and character variety

#### Recovery Mechanism

**Recovery Phrase**:

- Optional 12-word BIP39-compatible recovery phrase
- Generated from a cryptographically secure word list
- Hashed and stored securely (same as password)
- Can be used to reset password if forgotten
- **Only shown once** - must be written down

**Important**: Without a recovery phrase, forgotten passwords cannot be recovered!

## Security Best Practices

### For Users

**Password Management**:

1. Use unique, strong passwords (12+ characters recommended)
2. Never reuse passwords from other services
3. Consider using a password manager
4. Set up recovery phrase immediately after creating password
5. Store recovery phrase in a secure location (not digitally)

**Hidden Folder Usage**:

1. Create password immediately when prompted
2. Write down recovery phrase and store securely
3. Lock folder manually before leaving device unattended
4. Regularly change password (via Change Password option)
5. Don't share password or recovery phrase

**General Security**:

1. Enable device lock screen (PIN, pattern, biometric)
2. Keep Android OS up to date
3. Install apps only from trusted sources
4. Review app permissions regularly
5. Back up notes regularly (encrypted backups recommended)

### For Developers

**Code Security**:

1. All password operations use CharArray (cleared after use)
2. Constant-time comparison for password verification
3. No password logging or telemetry
4. Secure random number generation for salts and IVs
5. Proper key derivation with high iteration counts

**Data Protection**:

1. Android Keystore integration for key management
2. EncryptedSharedPreferences for sensitive data
3. No data transmission - fully offline
4. Secure deletion of sensitive data
5. Memory cleared after cryptographic operations

## Threat Model

### Protected Against

✅ **Offline Attacks**:

- High iteration PBKDF2 protects against brute force
- Unique salts prevent rainbow table attacks
- Strong encryption protects note content

✅ **Unauthorized Access**:

- Password required for hidden folder access
- Lockout policy prevents automated attacks
- Auto-lock prevents casual snooping

✅ **Data Extraction**:

- Encrypted storage prevents raw database access
- Android Keystore hardware-backed protection
- Root access may compromise (see limitations)

### Limitations

⚠️ **Not Protected Against**:

1. **Physical Device Compromise**:
    - If device is rooted, encryption keys may be extractable
    - Physical memory dumps could expose unencrypted data
    - Screen recording malware could capture password entry

2. **User Errors**:
    - Weak passwords can be brute-forced
    - Password written on device can be found
    - Shoulder surfing during password entry

3. **Advanced Attacks**:
    - Zero-day Android vulnerabilities
    - Malware with root privileges
    - Supply chain attacks on device firmware

4. **No Password Recovery**:
    - Forgotten passwords without recovery phrase = permanent loss
    - No "reset password" via email or cloud service
    - This is a feature, not a bug (true privacy)

## Vulnerability Reporting

If you discover a security vulnerability in Obby, please report it responsibly:

1. **Do NOT** open a public issue
2. Contact the maintainers privately (see CONTRIBUTING.md)
3. Provide detailed information:
    - Description of the vulnerability
    - Steps to reproduce
    - Potential impact
    - Suggested fix (if any)

We take all security reports seriously and will respond promptly.

## Cryptographic Implementation Details

### Password Hashing Flow

```
User Password (CharArray)
  ↓
PBKDF2-HMAC-SHA256 (150,000 iterations)
  ↓
256-bit Hash
  ↓
Base64 Encode
  ↓
EncryptedSharedPreferences (AES256_GCM)
  ↓
Android Keystore
```

### Password Verification Flow

```
User Input Password
  ↓
Hash with Stored Salt (PBKDF2)
  ↓
Constant-Time Comparison with Stored Hash
  ↓
Grant/Deny Access + Update Attempt Counter
```

### Key Storage Architecture

```
Password Hash & Salt
  ↓
EncryptedSharedPreferences
  ↓
Master Key (AES256_GCM)
  ↓
Android Keystore
  ↓
Hardware Security Module (if available)
```

## Compliance & Standards

**Cryptographic Standards**:

- NIST SP 800-132 (PBKDF2 recommendations)
- NIST SP 800-38D (GCM mode)
- OWASP guidelines for password storage

**Android Security**:

- Android Security Best Practices
- Android Keystore usage guidelines
- EncryptedSharedPreferences implementation

## Future Enhancements

Planned security improvements:

1. ✅ Password-protected hidden folder (Implemented)
2. 🔄 Biometric authentication integration (Planned)
3. 🔄 Hardware security key support (Planned)
4. 🔄 Encrypted backups with key export (Planned)
5. 🔄 Duress password (locks and hides sensitive data) (Planned)

## Audit & Testing

**Security Testing**:

- Unit tests for password hashing
- Integration tests for encryption flows
- UI tests for authentication mechanisms
- Manual security review of cryptographic code

**No External Audits Yet**:

- Obby has not undergone professional security audit
- Cryptographic implementations use standard libraries
- Use at your own risk for sensitive data

## Disclaimer

Obby provides strong encryption and security features, but **no system is 100% secure**.

- Use for appropriate threat models
- Understand limitations
- Don't rely solely on app-level security
- Complement with device-level security

**The developers of Obby are not responsible for data loss, unauthorized access, or any other
security incidents.**

## License

This security policy is part of the Obby project and is licensed under the MIT License.

---

**Last Updated**: December 2024
**Version**: 2.0 (Password-Protected Hidden Folder)

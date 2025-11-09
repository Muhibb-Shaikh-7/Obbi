# Hidden Folder Quick Start Guide

## What is the Hidden Folder?

The Hidden Folder is a password-protected space within Obby where you can store sensitive notes that
require a password to access. It uses enterprise-grade encryption to keep your private notes secure.

## Key Features

✅ **Military-grade encryption** (PBKDF2 with 150,000 iterations)  
✅ **Auto-lock** after 2 minutes of inactivity  
✅ **Lockout protection** after 5 failed attempts  
✅ **Password strength indicator**  
✅ **Optional recovery phrase** (12 words)  
✅ **Change password** anytime from within the folder

---

## First-Time Setup

### Step 1: Access Hidden Folder

1. Open Obby
2. Tap the **menu icon** (☰) in the top-left
3. Select **"Hidden Folder"** (marked with a 🔒 icon)

### Step 2: Create Password

You'll see the "Create Password" dialog:

1. **Enter a strong password** (minimum 8 characters)
    - Use a mix of uppercase, lowercase, numbers, and symbols
    - Check the strength indicator (aim for "Strong")

2. **Confirm password** by re-entering it

3. **Optional: Setup Recovery Phrase**
    - Tap "Setup Now" if you want a recovery option
    - Write down the 12 words shown
    - ⚠️ **IMPORTANT**: This is shown only ONCE!
    - Store it somewhere safe (NOT on your device)

4. Tap **"Create Password"**

### Step 3: Add Notes to Hidden Folder

To hide a note:

1. From the notes list, **long-press** on any note
2. Tap the **menu icon** (⋮) or use multi-select
3. Select **"Hide"**
4. The note is now in your Hidden Folder

---

## Daily Usage

### Accessing Hidden Notes

1. Open the drawer (☰ menu)
2. Tap **"Hidden Folder"**
3. Enter your password
4. Tap **"Unlock"**

Your hidden notes are now visible. They will **auto-lock after 2 minutes** of inactivity.

### Manually Locking

Tap the **lock icon** (🔒) in the top-right corner anytime to lock the folder.

### Reading/Editing Hidden Notes

- Tap any note to open and edit it
- All standard features work (markdown, links, etc.)
- The folder stays unlocked while you're active

---

## Security Features

### Failed Attempts & Lockout

- **5 failed attempts** = 2-minute lockout
- **10 failed attempts** = 4-minute lockout
- **15 failed attempts** = 8-minute lockout
- Pattern continues with exponential backoff

During lockout, you'll see:

- Countdown timer
- Number of failed attempts
- "Try again in X seconds"

### Auto-Lock

The folder automatically locks:

- After **2 minutes** of inactivity
- When you navigate away from Hidden Folder
- When you close the app
- When you lock your device

### Password Requirements

Your password must:

- Be at least **8 characters** long
- Not be all numbers
- Include a variety of character types

**Tip**: Use a password manager to generate and store strong passwords!

---

## Managing Your Password

### Changing Password

From inside the unlocked Hidden Folder:

1. Tap the **menu icon** (⋮) in the top-right
2. Select **"Change Password"**
3. Enter your **current password**
4. Enter your **new password**
5. Confirm the new password
6. Tap **"Change Password"**

### Setting Up Recovery Phrase (After Creation)

If you skipped recovery during setup:

1. Unlock Hidden Folder
2. Tap the **menu icon** (⋮)
3. Select **"Setup Recovery Phrase"**
4. Write down the 12 words
5. Check "I have written down my recovery phrase"
6. Tap **"I've Saved It"**

⚠️ **This is your only backup if you forget your password!**

### Using Recovery Phrase

If you forget your password:

1. Try to unlock Hidden Folder
2. Tap **"Forgot Password?"**
3. Enter your **12-word recovery phrase**
4. Set a **new password**
5. Tap **"Recover"**

⚠️ **Without a recovery phrase, forgotten passwords CANNOT be recovered!**

---

## Tips & Best Practices

### Password Tips

✅ **DO:**

- Use a unique password (not used anywhere else)
- Make it at least 12 characters
- Include uppercase, lowercase, numbers, symbols
- Use a password manager
- Set up recovery phrase immediately

❌ **DON'T:**

- Use personal info (birthdays, names)
- Write it on a note in the app
- Share it with anyone
- Use the same password as other services

### Recovery Phrase Tips

✅ **DO:**

- Write it on paper immediately
- Store it in a secure location (safe, bank vault)
- Keep it offline (don't save digitally)
- Verify you wrote it correctly

❌ **DON'T:**

- Screenshot it
- Email it to yourself
- Store it in cloud storage
- Show it to others

### General Security

✅ **DO:**

- Lock your device with PIN/pattern/biometric
- Keep Android up to date
- Lock Hidden Folder when leaving device
- Regular password changes (every 3-6 months)

❌ **DON'T:**

- Leave device unlocked in public
- Let others watch you enter password
- Use predictable passwords
- Forget to lock before lending device

---

## Troubleshooting

### "Locked out. Try again in X seconds"

**Cause:** Too many failed password attempts  
**Solution:** Wait for the countdown to finish, then try again with the correct password

**Prevention:**

- Double-check password before submitting
- Use recovery phrase if you're unsure
- Write down password immediately after creation

### "Incorrect password"

**Cause:** Wrong password entered  
**Solution:**

1. Try again carefully (check Caps Lock)
2. If you have a recovery phrase, use "Forgot Password?"
3. If no recovery phrase, password cannot be recovered

**Prevention:**

- Always set up recovery phrase
- Store password securely
- Test password right after creation

### "Hidden folder locked"

**Cause:** Auto-lock timer expired (2 minutes)  
**Solution:** Simply re-enter your password to unlock

**Prevention:** Manual lock before letting timer expire

### "Forgot password and no recovery phrase"

**Cause:** Password forgotten with no recovery setup  
**Solution:** ⚠️ **Password cannot be recovered**

Your options:

1. Keep trying passwords you might have used
2. Accept that notes are permanently inaccessible

**Prevention:** ALWAYS set up recovery phrase!

---

## FAQs

### Q: Can I change the auto-lock timeout?

**A:** Currently it's fixed at 2 minutes. Configurable timeouts are planned for a future update.

### Q: Can I use biometric (fingerprint/face) instead of password?

**A:** Not yet, but biometric authentication is planned for a future release.

### Q: What happens if I uninstall the app?

**A:** Your password and hidden notes are permanently deleted. Back up important notes before
uninstalling!

### Q: Are hidden notes backed up?

**A:** Hidden notes are part of regular backups, but they remain encrypted. The password is NOT
backed up for security.

### Q: Can someone with root access see my hidden notes?

**A:** Potentially yes. Root access can compromise Android's security. See SECURITY.md for details.

### Q: Is this really secure?

**A:** Yes! We use industry-standard PBKDF2-HMAC-SHA256 with 150,000 iterations and Android
Keystore. See SECURITY.md for technical details.

### Q: Can I have multiple hidden folders with different passwords?

**A:** Currently, there's one Hidden Folder per app. Multiple folders are not supported yet.

### Q: What if I want to unhide a note?

**A:** Unlock the folder, long-press the note, and select "Unhide". It returns to your normal notes
list.

---

## Privacy Notice

✅ **What Obby does:**

- Stores password hash locally (never the actual password)
- Uses hardware-backed encryption (Android Keystore)
- Keeps everything offline (no internet required)
- Never logs or transmits passwords

✅ **What Obby does NOT do:**

- Send passwords to any server
- Store passwords in plain text
- Provide "reset password via email"
- Track or analyze your usage

**Your privacy is our priority. All data stays on YOUR device.**

---

## Need Help?

- **Security Details:** See [SECURITY.md](SECURITY.md)
- **Bug Reports:** Open an issue on GitHub
- **Questions:** Check [README.md](README.md)

---

**Stay Secure! 🔒**

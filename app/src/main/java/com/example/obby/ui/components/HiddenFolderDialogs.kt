package com.example.obby.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.obby.util.PasswordHasher

@Composable
fun CreatePasswordDialog(
    onDismiss: () -> Unit,
    onCreate: (password: String, confirmPassword: String) -> Unit,
    onSetupRecoveryPhrase: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val passwordStrength = PasswordHasher.calculatePasswordStrength(password)
    val (isValid, errorMessage) = if (password.isNotEmpty()) {
        PasswordHasher.checkPasswordComplexity(password)
    } else {
        true to null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Hidden Folder Password",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Create a strong password to protect your hidden notes. " +
                            "This password cannot be recovered if forgotten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValid && password.isNotEmpty()
                )

                // Password strength indicator
                if (password.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Password Strength",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                when {
                                    passwordStrength < 40 -> "Weak"
                                    passwordStrength < 70 -> "Medium"
                                    else -> "Strong"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    passwordStrength < 40 -> Color.Red
                                    passwordStrength < 70 -> Color(0xFFFF9800)
                                    else -> Color.Green
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { passwordStrength / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                passwordStrength < 40 -> Color.Red
                                passwordStrength < 70 -> Color(0xFFFF9800)
                                else -> Color.Green
                            },
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Confirm password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        "Passwords do not match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                HorizontalDivider()

                // Recovery phrase option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Recovery Phrase",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Set up later (recommended)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onSetupRecoveryPhrase) {
                        Text("Setup Now")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(password, confirmPassword) },
                enabled = password.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        password == confirmPassword &&
                        isValid
            ) {
                Text("Create Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UnlockHiddenFolderDialog(
    onDismiss: () -> Unit,
    onUnlock: (password: String) -> Unit,
    failedAttempts: Int = 0,
    onForgotPassword: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Unlock Hidden Folder") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Enter your password to access hidden notes",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (failedAttempts > 0) {
                    Text(
                        "Failed attempts: $failedAttempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextButton(
                    onClick = onForgotPassword,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUnlock(password) },
                enabled = password.isNotEmpty()
            ) {
                Text("Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChange: (oldPassword: String, newPassword: String, confirmPassword: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOldPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val passwordStrength = PasswordHasher.calculatePasswordStrength(newPassword)
    val (isValid, errorMessage) = if (newPassword.isNotEmpty()) {
        PasswordHasher.checkPasswordComplexity(newPassword)
    } else {
        true to null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Enter your current password and choose a new one",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Old password
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = if (showOldPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOldPassword = !showOldPassword }) {
                            Icon(
                                if (showOldPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showOldPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // New password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = if (showNewPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValid && newPassword.isNotEmpty()
                )

                // Password strength
                if (newPassword.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Strength",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                when {
                                    passwordStrength < 40 -> "Weak"
                                    passwordStrength < 70 -> "Medium"
                                    else -> "Strong"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    passwordStrength < 40 -> Color.Red
                                    passwordStrength < 70 -> Color(0xFFFF9800)
                                    else -> Color.Green
                                }
                            )
                        }
                        LinearProgressIndicator(
                            progress = { passwordStrength / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = when {
                                passwordStrength < 40 -> Color.Red
                                passwordStrength < 70 -> Color(0xFFFF9800)
                                else -> Color.Green
                            },
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Confirm password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text(
                        "Passwords do not match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onChange(oldPassword, newPassword, confirmPassword) },
                enabled = oldPassword.isNotEmpty() &&
                        newPassword.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        newPassword == confirmPassword &&
                        isValid
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecoveryPhraseDialog(
    recoveryPhrase: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { }, // Don't allow dismissal without confirmation
        icon = {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Recovery Phrase") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "⚠️ IMPORTANT: Write this down and store it safely!",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "This 12-word recovery phrase is the ONLY way to recover access if you forget your password. " +
                            "It will only be shown once.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            recoveryPhrase,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmed,
                        onCheckedChange = { confirmed = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "I have written down my recovery phrase",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmed
            ) {
                Text("I've Saved It")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = confirmed
            ) {
                Text("Skip for Now")
            }
        }
    )
}

@Composable
fun RecoveryDialog(
    onDismiss: () -> Unit,
    onRecover: (recoveryPhrase: String, newPassword: String, confirmPassword: String) -> Unit
) {
    var recoveryPhrase by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recover Access") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Enter your 12-word recovery phrase and set a new password",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = recoveryPhrase,
                    onValueChange = { recoveryPhrase = it },
                    label = { Text("Recovery Phrase (12 words)") },
                    minLines = 3,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = if (showNewPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text(
                        "Passwords do not match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRecover(recoveryPhrase, newPassword, confirmPassword) },
                enabled = recoveryPhrase.isNotBlank() &&
                        newPassword.isNotEmpty() &&
                        confirmPassword.isNotEmpty() &&
                        newPassword == confirmPassword
            ) {
                Text("Recover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LockedOutDialog(
    remainingSeconds: Int,
    attemptCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Temporarily Locked") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Too many failed attempts ($attemptCount)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Please wait $remainingSeconds seconds before trying again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ManagePinDialog(
    isPinCurrentlyEnabled: Boolean,
    onVerifyCurrentPin: (String) -> Boolean,
    onSetNewPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onDismiss: () -> Unit
) {
    // Modes:
    // When disabled: 0 = Enter new PIN, 1 = Confirm new PIN
    // When enabled: 0 = Options ("Change PIN" or "Disable"), 10 = Enter Old PIN to change, 11 = Enter New PIN, 12 = Confirm New PIN, 20 = Enter Old PIN to disable
    var step by remember { mutableStateOf(if (isPinCurrentlyEnabled) 0 else 1) }

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (step) {
                    0 -> if (isPinCurrentlyEnabled) "App PIN Lock" else "Set App PIN"
                    1 -> "Set 4-Digit PIN"
                    2 -> "Confirm 4-Digit PIN"
                    10 -> "Enter Current PIN"
                    11 -> "Enter New 4-Digit PIN"
                    12 -> "Confirm New PIN"
                    20 -> "Enter Current PIN to Disable"
                    else -> "App Lock"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step == 0 && isPinCurrentlyEnabled) {
                    Text(
                        text = "App PIN Lock is currently active. Every time you open MySpend, you will need to enter your PIN.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            oldPin = ""
                            errorText = null
                            step = 10
                        },
                        modifier = Modifier.fillMaxWidth().testTag("change_pin_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change PIN")
                    }

                    Button(
                        onClick = {
                            oldPin = ""
                            errorText = null
                            step = 20
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("disable_pin_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Turn Off PIN Lock")
                    }
                } else if (step == 1 || step == 11) {
                    Text(
                        text = "Choose a 4-digit PIN for locking MySpend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                newPin = it
                                errorText = null
                            }
                        },
                        label = { Text("4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = errorText != null,
                        supportingText = { if (errorText != null) Text(errorText!!, color = MaterialTheme.colorScheme.error) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                    )
                } else if (step == 2 || step == 12) {
                    Text(
                        text = "Re-enter your 4-digit PIN to confirm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                confirmPin = it
                                errorText = null
                            }
                        },
                        label = { Text("Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = errorText != null,
                        supportingText = { if (errorText != null) Text(errorText!!, color = MaterialTheme.colorScheme.error) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                    )
                } else if (step == 10 || step == 20) {
                    Text(
                        text = "Please enter your current 4-digit PIN to verify your identity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                oldPin = it
                                errorText = null
                            }
                        },
                        label = { Text("Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = errorText != null,
                        supportingText = { if (errorText != null) Text(errorText!!, color = MaterialTheme.colorScheme.error) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_current_pin")
                    )
                }
            }
        },
        confirmButton = {
            when (step) {
                1, 11 -> {
                    Button(
                        onClick = {
                            if (newPin.length != 4) {
                                errorText = "PIN must be exactly 4 digits"
                            } else {
                                confirmPin = ""
                                errorText = null
                                step = if (step == 1) 2 else 12
                            }
                        },
                        modifier = Modifier.testTag("pin_next_button")
                    ) {
                        Text("Next")
                    }
                }
                2, 12 -> {
                    Button(
                        onClick = {
                            if (confirmPin != newPin) {
                                errorText = "PINs do not match. Try again."
                            } else {
                                onSetNewPin(newPin)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("pin_save_button")
                    ) {
                        Text("Save PIN")
                    }
                }
                10 -> {
                    Button(
                        onClick = {
                            if (onVerifyCurrentPin(oldPin)) {
                                newPin = ""
                                errorText = null
                                step = 11
                            } else {
                                errorText = "Incorrect PIN"
                            }
                        },
                        modifier = Modifier.testTag("verify_old_pin_button")
                    ) {
                        Text("Next")
                    }
                }
                20 -> {
                    Button(
                        onClick = {
                            if (onVerifyCurrentPin(oldPin)) {
                                onDisablePin()
                                onDismiss()
                            } else {
                                errorText = "Incorrect PIN"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_disable_pin_button")
                    ) {
                        Text("Turn Off")
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        },
        dismissButton = {
            if (step != 0) {
                TextButton(
                    onClick = {
                        if (isPinCurrentlyEnabled) step = 0 else onDismiss()
                    }
                ) {
                    Text("Back")
                }
            }
        }
    )
}

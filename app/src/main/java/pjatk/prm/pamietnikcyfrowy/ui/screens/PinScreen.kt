package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pjatk.prm.pamietnikcyfrowy.R

enum class PinMode { SETTING, UNLOCKING, RECOVERY }

@Composable
fun PinScreen(
    mode: PinMode,
    pinValue: String,
    questionValue: String,
    answerValue: String,
    savedQuestion: String?,
    errorMessage: String?,
    onPinChange: (String) -> Unit,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onActionClick: () -> Unit,
    onForgotPinClick: () -> Unit,
    onBackToUnlockClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (mode) {
                    PinMode.SETTING -> stringResource(R.string.pin_setting_title)
                    PinMode.UNLOCKING -> stringResource(R.string.pin_unlocking_title)
                    PinMode.RECOVERY -> stringResource(R.string.pin_recovery_title)
                },
                style = MaterialTheme.typography.headlineSmall
            )

            if (mode == PinMode.SETTING) {
                OutlinedTextField(
                    value = pinValue, onValueChange = onPinChange, modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pin_label_setting)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                )
                OutlinedTextField(
                    value = questionValue, onValueChange = onQuestionChange, modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pin_question_label)) }, singleLine = true
                )
                OutlinedTextField(
                    value = answerValue, onValueChange = onAnswerChange, modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pin_answer_label)) }, singleLine = true
                )
            } else if (mode == PinMode.UNLOCKING) {
                OutlinedTextField(
                    value = pinValue, onValueChange = onPinChange, modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pin_label)) }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = errorMessage != null
                )
            } else if (mode == PinMode.RECOVERY) {
                Text(
                    text = stringResource(R.string.pin_recovery_question, savedQuestion ?: ""),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = answerValue, onValueChange = onAnswerChange, modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.pin_recovery_answer_label)) }, singleLine = true,
                    isError = errorMessage != null
                )
            }

            if (errorMessage != null) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(onClick = onActionClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    when (mode) {
                        PinMode.SETTING -> stringResource(R.string.pin_btn_save_security)
                        PinMode.UNLOCKING -> stringResource(R.string.pin_btn_unlock)
                        PinMode.RECOVERY -> stringResource(R.string.pin_btn_reset)
                    }
                )
            }

            if (mode == PinMode.UNLOCKING) {
                TextButton(onClick = onForgotPinClick) { Text(stringResource(R.string.pin_btn_forgot)) }
            } else if (mode == PinMode.RECOVERY) {
                TextButton(onClick = onBackToUnlockClick) { Text(stringResource(R.string.pin_btn_back_to_login)) }
            }
        }
    }
}
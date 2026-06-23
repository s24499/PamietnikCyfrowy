package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinScreen(
    pinValue: String,
    onPinChange: (String) -> Unit,
    onUnlockClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wprowadź PIN",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pinValue,
                onValueChange = onPinChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("PIN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onUnlockClick, modifier = Modifier.fillMaxWidth()) {
                Text("Otwórz pamiętnik")
            }
        }
    }
}
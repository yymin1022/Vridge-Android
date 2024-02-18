package com.gdsc_cau.vridge.ui.voicelist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import com.gdsc_cau.vridge.ui.main.Greeting

@Composable
fun VoiceListScreen(
    padding: PaddingValues,
    onRecordClick: () -> Unit,
    onVoiceClick: (String) -> Unit
) {
    Column {
        Button(onClick = { onVoiceClick("1") }) {
            Greeting(name = "VoiceListScreen")
        }

        Button(onClick = { onRecordClick() }) {
            Greeting(name = "RecordScreen")
        }
    }
}
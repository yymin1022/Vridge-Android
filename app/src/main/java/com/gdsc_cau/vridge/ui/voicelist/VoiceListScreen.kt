package com.gdsc_cau.vridge.ui.voicelist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdsc_cau.vridge.R
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.ui.record.RecordState
import com.gdsc_cau.vridge.ui.record.VoiceSettingDialog
import com.gdsc_cau.vridge.ui.theme.OnPrimaryLight
import com.gdsc_cau.vridge.ui.theme.Primary
import com.gdsc_cau.vridge.ui.theme.White
import com.gdsc_cau.vridge.ui.util.TopBarType
import com.gdsc_cau.vridge.ui.util.VridgeTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VoiceListRoute(
    onVoiceClick: (Voice) -> Unit,
    onRecordClick: () -> Unit,
    onHideBottomBar: (Boolean) -> Unit,
    onShowErrorSnackBar: (Throwable?) -> Unit,
    viewModel: VoiceListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is VoiceListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is VoiceListUiState.Empty -> {
                EmptyVoiceList(onRecordClick)
            }

            is VoiceListUiState.Success -> {
                GridVoiceList(
                    (uiState as VoiceListUiState.Success).voiceList,
                    onRecordClick,
                    { list, name, pitch -> viewModel.synthesize(list, name, pitch) },
                    onVoiceClick,
                    onHideBottomBar
                )
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.errorFlow.collectLatest { throwable -> onShowErrorSnackBar(throwable) }
    }
}

@Composable
fun EmptyVoiceList(onRecordClick: () -> Unit) {
    VridgeTopBar(title = "Make your voice", type = TopBarType.NONE)
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(stringResource(id = R.string.empty_voice_list))
        ElevatedButton(
            onClick = onRecordClick,
            colors = ButtonDefaults.elevatedButtonColors(containerColor = Primary, contentColor = White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(stringResource(R.string.btn_add_voice), modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
fun GridVoiceList(
    voices: List<Voice>,
    onRecordClick: () -> Unit,
    onSynthClick: (List<String>, String, Float) -> Unit,
    onVoiceClick: (Voice) -> Unit,
    onHideBottomBar: (Boolean) -> Unit
) {
    val selectedIds = rememberSaveable { mutableStateOf(emptySet<String>()) }
    val inSelectionMode = rememberSaveable { mutableStateOf(false) }

    val voiceName = remember { mutableStateOf("") }
    val sliderPosition = remember { mutableFloatStateOf(-6f) }
    val showDialog = remember { mutableStateOf(false) }

    BackHandler {
        if (inSelectionMode.value) {
            inSelectionMode.value = false
            selectedIds.value = emptySet()
        }
    }

    LaunchedEffect(inSelectionMode.value) {
        onHideBottomBar(inSelectionMode.value.not())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VridgeTopBar(title = "Choose your voice", type = TopBarType.NONE)
        LazyVerticalGrid(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f),
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(voices.size, key = { it }) { index ->
                val selected = selectedIds.value.contains(voices[index].vid)
                VoiceListItem(
                    voices[index],
                    selected,
                    Modifier.clickable(enabled = voices[index].status) {
                        if (inSelectionMode.value) {
                            if (selected) {
                                selectedIds.value -= voices[index].vid
                            } else if (selectedIds.value.size < 2) {
                                selectedIds.value += voices[index].vid
                            }
                        } else {
                            onVoiceClick(voices[index])
                        }
                    }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (inSelectionMode.value) {
                Button(
                    onClick = { showDialog.value = true },
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    enabled = selectedIds.value.size == 2
                ) {
                    Text(
                        "Synth ( ${selectedIds.value.size} / 2 )",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                Button(
                    onClick = onRecordClick,
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        "Add",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                Button(
                    onClick = { inSelectionMode.value = true },
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(
                        "Synth",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        VoiceSettingDialog(
            isShowingDialog = showDialog.value,
            text = voiceName.value,
            onTextChanged = { voiceName.value = it },
            sliderPosition = sliderPosition.floatValue,
            onSliderChanged = { sliderPosition.floatValue = it },
            onConfirmRequest = {
                onSynthClick(selectedIds.value.toList(), voiceName.value, sliderPosition.value)
            },
            onDismissRequest = { showDialog.value = false }
        )
    }
}

@Composable
fun VoiceListItem(
    voice: Voice,
    selected: Boolean,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
    ) {
        Surface {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = OnPrimaryLight,
                    modifier = modifier
                        .padding(16.dp)
                        .fillMaxSize()
                )
            }

            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = voice.name, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview
@Composable
fun EmptyVoiceListPreview() {
    EmptyVoiceList {}
}

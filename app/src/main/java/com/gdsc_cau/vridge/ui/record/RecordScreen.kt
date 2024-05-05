package com.gdsc_cau.vridge.ui.record

import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.gdsc_cau.vridge.R
import com.gdsc_cau.vridge.ui.theme.Black
import com.gdsc_cau.vridge.ui.theme.Grey2
import com.gdsc_cau.vridge.ui.theme.Grey4
import com.gdsc_cau.vridge.ui.theme.Primary
import com.gdsc_cau.vridge.ui.theme.VridgeTheme
import com.gdsc_cau.vridge.ui.theme.White
import com.gdsc_cau.vridge.ui.util.LoadingDialog
import com.gdsc_cau.vridge.ui.util.TopBarType
import com.gdsc_cau.vridge.ui.util.VridgeTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RecordRoute(
    onBackClick: () -> Unit,
    onShowErrorSnackBar: (Throwable?) -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    viewModel.setFileName(LocalContext.current.externalCacheDir?.absolutePath ?: "")

    val recorder = if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
        MediaRecorder(LocalContext.current)
    } else {
        MediaRecorder()
    }

    when (uiState) {
        is RecordUiState.Loading -> {
            CircularProgressIndicator()
        }

        is RecordUiState.Success -> {
            RecordScreen(
                uiState as RecordUiState.Success,
                recorder,
                onBackClick,
                viewModel::getNext,
                viewModel::startRecord,
                viewModel::stopRecord,
                viewModel::startPlay,
                viewModel::stopPlay,
                viewModel::finishRecord
            )
        }
    }

    LaunchedEffect(true) {
        viewModel.errorFlow.collectLatest {
            onShowErrorSnackBar(it)
        }
    }
}

@Composable
fun RecordScreen(
    uiState: RecordUiState.Success,
    recorder: MediaRecorder?,
    onBackClick: () -> Unit,
    onClickNext: () -> Unit,
    onStartRecord: (MediaRecorder) -> Unit,
    onStopRecord: () -> Unit,
    onStartPlay: () -> Unit,
    onStopPlay: () -> Unit,
    onFinishRecord: (String, Float) -> Unit
) {
    val voiceName = remember { mutableStateOf("") }
    val sliderPosition = remember { mutableFloatStateOf(-6f) }

    Column(
        modifier =
        Modifier
            .fillMaxSize(),

    ) {
        VridgeTopBar(
            title = stringResource(R.string.record_title),
            type = TopBarType.CLOSE,
            onBackClick = onBackClick
        )
        RecordDataView(
            idx = "${uiState.index} / ${uiState.size}",
            data = uiState.text
        )
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            RecordButton(uiState.state == RecordState.RECORDING) {
                if (it && recorder != null) onStartRecord(recorder) else onStopRecord()
            }
        }
        RecordNavigator(
            uiState.state == RecordState.PLAYING,
            uiState.state == RecordState.RECORDED,
            uiState.state == RecordState.RECORDED && uiState.index == uiState.size,
            { if (it) onStartPlay() else onStopPlay() },
            onClickNext
        )
        LoadingDialog(isShowingDialog = uiState.state == RecordState.LOADING)
        VoiceSettingDialog(
            isShowingDialog = uiState.state == RecordState.FINISHING,
            text = voiceName.value,
            onTextChanged = { voiceName.value = it },
            sliderPosition = sliderPosition.floatValue,
            onSliderChanged = { sliderPosition.floatValue = it },
            onConfirmRequest = {
                onFinishRecord(voiceName.value, sliderPosition.floatValue)
            },
            onDismissRequest = onStopPlay
        )
    }

    LaunchedEffect(uiState.state) {
        if (uiState.state == RecordState.FINISHED) {
            onBackClick()
        }
    }
}

@Composable
fun RecordDataView(idx: String, data: String) {
    Column(
        modifier = Modifier
    ) {
        RecordDataIndex(idx = idx)
        RecordDataCard(data = data)
    }
}

@Composable
fun RecordDataIndex(idx: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
    ) {
        Text(
            fontSize = 25.sp,
            color = Black,
            text = idx
        )
    }
}

@Composable
fun RecordDataCard(data: String) {
    ElevatedCard(
        colors =
        CardDefaults.elevatedCardColors(
            containerColor = Grey4,
            contentColor = Black
        ),
        elevation =
        CardDefaults.cardElevation(
            defaultElevation = 5.dp
        ),
        modifier =
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(all = 30.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Text(
                fontSize = 20.sp,
                text = data,
                softWrap = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun RecordButton(isRecording: Boolean, onClickRecord: (Boolean) -> Unit) {
    val lottieAnimatable = rememberLottieAnimatable()
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.anim_lottie_loading)
    )

    LaunchedEffect(isRecording) {
        lottieAnimatable.animate(
            composition = lottieComposition,
            clipSpec = LottieClipSpec.Frame(0, 1200),
            initialProgress = 0f
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
        Modifier
            .fillMaxSize()
    ) {
        ElevatedButton(
            colors =
            ButtonDefaults.elevatedButtonColors(
                containerColor = Primary,
                contentColor = White
            ),
            modifier =
            Modifier
                .height(130.dp)
                .width(130.dp),
            shape = CircleShape,
            onClick = {
                onClickRecord(isRecording.not())
            }
        ) {
            if (isRecording) {
                LottieAnimation(
                    composition = lottieComposition,
                    contentScale = ContentScale.FillHeight,
                    iterations = LottieConstants.IterateForever
                )
            } else {
                Icon(
                    painter =
                    painterResource(
                        id = R.drawable.ic_mic
                    ),
                    contentDescription = "Record Button"
                )
            }
        }
    }
}

@Composable
fun RecordNavigator(
    isPlaying: Boolean,
    clickable: Boolean,
    isFinish: Boolean,
    onClickPlay: (Boolean) -> Unit,
    onClickNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier =
        Modifier
            .fillMaxWidth()
    ) {
        RecordNavigateButton(
            text = if (isPlaying.not()) stringResource(id = R.string.record_btn_play) else stringResource(id = R.string.record_btn_stop),
            clickable
        ) {
            onClickPlay(isPlaying.not())
        }
        RecordNavigateButton(
            text = if (isFinish) stringResource(id = R.string.record_btn_finish) else stringResource(id = R.string.record_btn_next),
            clickable
        ) {
            onClickNext()
        }
    }
}

@Composable
fun RecordNavigateButton(text: String, clickable: Boolean, onBtnClicked: () -> Unit) {
    ElevatedButton(
        enabled = true,
        colors =
        ButtonDefaults.elevatedButtonColors(
            containerColor = if (clickable) Primary else Grey2,
            contentColor = White
        ),
        modifier =
        Modifier
            .padding(all = 20.dp)
            .width(150.dp),
        onClick = { if (clickable) onBtnClicked() }
    ) {
        Text(
            modifier = Modifier,
            fontSize = 20.sp,
            text = text
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenIdlePreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 1,
                size = 3,
                state = RecordState.IDLE
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenRecordedDonePreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 3,
                size = 3,
                state = RecordState.RECORDED
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenRecordedPreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 1,
                size = 3,
                state = RecordState.RECORDED
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenPlayingPreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 1,
                size = 3,
                state = RecordState.PLAYING
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenLoadingPreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 1,
                size = 3,
                state = RecordState.LOADING
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecordScreenFinishPreview() {
    VridgeTheme {
        RecordScreen(
            uiState = RecordUiState.Success(
                text = "Hello, World!",
                index = 3,
                size = 3,
                state = RecordState.FINISHING
            ),
            recorder = null,
            onBackClick = {},
            onClickNext = {},
            onStartRecord = {},
            onStopRecord = {},
            onStartPlay = {},
            onStopPlay = {},
            onFinishRecord = { _, _ -> }
        )
    }
}

enum class RecordState {
    IDLE,
    RECORDING,
    RECORDED,
    PLAYING,
    LOADING,
    FINISHING,
    FINISHED
}

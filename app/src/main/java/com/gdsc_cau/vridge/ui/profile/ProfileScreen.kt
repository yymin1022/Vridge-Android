package com.gdsc_cau.vridge.ui.profile

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdsc_cau.vridge.R
import com.gdsc_cau.vridge.data.models.User
import com.gdsc_cau.vridge.ui.login.LoginActivity
import com.gdsc_cau.vridge.ui.theme.Grey3
import com.gdsc_cau.vridge.ui.util.TopBarType
import com.gdsc_cau.vridge.ui.util.VridgeTopBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileScreen(
    onShowErrorSnackBar: (Throwable?) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (profileUiState) {
        is ProfileUiState.Loading -> ProfileLoading()
        is ProfileUiState.Success -> ProfileContent(
            (profileUiState as ProfileUiState.Success).user,
            (profileUiState as ProfileUiState.Success).isLoggedOut,
            { viewModel.signOut() },
            { viewModel.unregister() }
        )
    }

    LaunchedEffect(true) {
        viewModel.errorFlow.collectLatest { throwable -> onShowErrorSnackBar(throwable) }
    }
}

@Composable
fun ProfileLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ProfileContent(user: User, isLoggedOut: Boolean, onClickLogout: () -> Unit, onClickUnregister: () -> Unit) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        VridgeTopBar(title = "Profile", type = TopBarType.NONE)
        ProfileList(profileData = user, onClickLogout, onClickUnregister)
    }

    LaunchedEffect(key1 = isLoggedOut) {
        if (isLoggedOut) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            (context as Activity).finish()
        }
    }
}

@Composable
fun ProfileList(profileData: User, onClickLogout: () -> Unit, onClickUnregister: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        ProfileListItem(
            title = stringResource(id = R.string.profile_list_item_name),
            content = profileData.name
        )
        ProfileListDivider()
        ProfileListItem(
            title = stringResource(id = R.string.profile_list_item_email),
            content = profileData.email
        )
        ProfileListDivider()
        ProfileListItem(
            title = stringResource(id = R.string.profile_list_item_synthesize_cnt),
            content = profileData.cntVoice.toString()
        )
        ProfileListDivider()
        ProfileListItem(
            title = stringResource(id = R.string.profile_list_item_signout_title),
            content = stringResource(id = R.string.profile_list_item_signout_description),
            clickable = true,
            onClick = onClickLogout
        )
        ProfileListDivider()
        ProfileListItem(
            title = stringResource(id = R.string.profile_list_item_delete_title),
            content = stringResource(id = R.string.profile_list_item_delete_description),
            onClick = onClickUnregister
        )
    }
}

@Composable
fun ProfileListDivider() {
    Divider(
        color = Grey3
    )
}

@Composable
fun ProfileListItem(
    title: String,
    content: String,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clickable(enabled = clickable) { onClick() }
            .fillMaxWidth()
            .padding(all = 15.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            fontSize = 17.sp
        )
    }
}

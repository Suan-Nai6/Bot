package com.yogurt.xiaoye.huluxiarobot


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.yogurt.xiaoye.huluxiarobot.ui.theme.葫芦BotTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import java.io.File
import java.security.MessageDigest

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)

        setContent {
            葫芦BotTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginActivity()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginActivity() {
    //FeatureThatRequiresCameraPermission()
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setStatusBarColor(Color.Transparent,true)
    }
    var showDialog by remember { mutableStateOf(false) }
    var key by remember { mutableStateOf("") }
    Scaffold(
        content = { LoginContent() }, // 传入一个 @Composable 函数作为内容区域
        floatingActionButton = { // 传入一个 @Composable 函数作为浮动操作按钮
            ExtendedFloatingActionButton(
                text = { Text(text = "使用Key登录") },
                icon = { Icon(
                    painter = painterResource(id = R.drawable.baseline_login_24),
                    contentDescription = "使用Key登录"
                ) },
                onClick = { showDialog = true }// 给 ExtendedFloatingActionButton 设置对齐方式为左下角
            )
        }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "确认".uppercase())
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("取消".uppercase())
                }
            },
            title = { Text(text = "请输入账号Key") },
            text = {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("请输入账号Key") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable // 封装一个 @Composable 函数作为内容区域的函数，并改名为 LoginContent
fun LoginContent() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.huluxialogo), contentDescription = "葫芦侠logo",
                modifier = Modifier.size(75.dp))
            Column {
                Text(
                    stringResource(R.string.huluxia),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp)
                Text(
                    stringResource(R.string.login),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        OutlinedTextField(value = username,
            onValueChange =  { username = it },
            label = { Text("请输入手机号") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next)
        )
        OutlinedTextField(value = password,
            onValueChange =  { password = it },
            label = { Text("请输入密码") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(30.dp))
        val context = LocalContext.current
        Button(onClick = {
            var loginsession = login(username, password)
            Toast.makeText(context, loginsession, Toast.LENGTH_LONG).show()
        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(
                Icons.Filled.Done,
                contentDescription = "Localized description",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = "登录")
        }
    }
}

fun login(username: String, password: String): String {
    val loginstatus = File("data.txt").exists()
    if (loginstatus) {
        val loginstatus1 = File("logininfo.txt").readText()
        val response = Json.decodeFromString<Response>(loginstatus1)
        val key = response._key
        return "已登录，无需再次登录！\n${key}"
    } else {
        val loginapireslut = loginAPI(username, password)
        // 在网络请求成功后，写入文件
        //File("logininfo.txt").writeText(loginapireslut)
        return "登录结果：${loginapireslut}"
    }
}

/*@Composable
private fun FeatureThatRequiresCameraPermission() {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    when {
        cameraPermissionState.hasPermission -> {
            Text("相机已授权")
        }
        else -> {
            Column {
                val textToShow = if (cameraPermissionState.shouldShowRationale) {
                    "欲用相机，必先授权"
                } else {
                    "请授权"
                }
                Text(textToShow)
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("授权")
                }
            }
        }
    }
}*/


@Serializable
data class Response(
    val msg: String,
    val _key: String,
    val user: User,
    val session_key: String,
    val status: Int
)

@Serializable
data class User(
    val userID: Int,
    val role: Int,
    val nick: String,
    val avatar: String,
    val birthday: Long,
    val age: Int,
    val gender: Int,
    val level: Int,
    val isgold: Int,
    val identityTitle: String?,
    val identityColor: Int,
    val needSetPassword: Int,
    val needSetUserInfo: Int
)


fun loginAPI(username: String?, userpassword: String?): String {
    val OkhttpRun = OkhttpApiClient()
    val passwd = md5("${userpassword}")
    return "a"+OkhttpRun.run("http://floor.huluxia.com/account/login/ANDROID/4.0?device_code=[d]&password=${passwd}&login_type=2&account=$username")
}

/** md5加密 */
fun md5(content: String): String {
    val hash = MessageDigest.getInstance("MD5").digest(content.toByteArray())
    val hex = StringBuilder(hash.size * 2)
    for (b in hash) {
        var str = Integer.toHexString(b.toInt())
        if (b < 0x10) {
            str = "0$str"
        }
        hex.append(str.substring(str.length -2))
    }
    return hex.toString()
}


// 定义一个函数，用于启动一个协程
/*fun makeRequest(url: String) {
    // 使用launch构建器，在ViewModelScope中启动一个协程
    viewModelScope.launch {
        // 使用withContext函数，在IO线程中执行网络请求
        val response = withContext(Dispatchers.IO) {
            run(url) // 调用你之前定义的run函数
        }
        // 在主线程中更新StateFlow对象
        _result.value = response
    }
}*/

/*fun run1(url: String): String {
    var client = OkHttpClient()
    val request: Request = Request.Builder()
        .url(url)
        .build()
    try {
        client.newCall(request).execute().use { response ->
            return response.body!!.string()
        }
    } catch (e: Exception) {
        // 打印异常信息或者记录日志
        e.printStackTrace()
        // 返回一个错误信息
        return "网络请求失败：${e.message}"
    }
}*/

class OkhttpApiClient {
    fun run(url: String): String {
            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .url(url)
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    return response.body?.string() ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return "网络请求失败：${e.message}"
            }
    }
}

    @Preview(showBackground = true)
    @Composable
    fun LoginPreview() {
        MaterialTheme { // 使用Compose中默认的MaterialTheme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                LoginActivity()
            }
        }
    }

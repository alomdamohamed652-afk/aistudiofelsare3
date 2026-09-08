package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*

@Composable
fun AuthScreen(
    onLogin: (identifier: String, password: String) -> Unit,
    onRegister: (name: String, phone: String, email: String, password: String, confirmPassword: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var showForgotInfo by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Text(text = "⚡", fontSize = 42.sp) }

        Spacer(modifier = Modifier.height(12.dp))
        Text("فالسريع", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text("اطلب.. يوصلك فالسريع ⚡", style = MaterialTheme.typography.titleMedium, color = BrandPrimaryDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(28.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = BrandPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("تسجيل الدخول", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("إنشاء حساب", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium) })
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppInput(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = "رقم الهاتف أو البريد الإلكتروني",
                    placeholder = "01xxxxxxxxx أو email@example.com",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    testTag = "auth_login_identifier_input"
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppInput(
                    value = password,
                    onValueChange = { password = it },
                    label = "كلمة المرور",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "إظهار كلمة المرور")
                        }
                    },
                    testTag = "auth_login_password_input"
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showForgotInfo = true }) { Text("نسيت كلمة المرور؟", color = BrandPrimary, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(8.dp))
                AppButton(
                    text = "تسجيل الدخول",
                    onClick = { onLogin(identifier, password) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_login_submit_button"
                )
            }
        } else {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppInput(value = regName, onValueChange = { regName = it }, label = "الاسم بالكامل", placeholder = "مثال: أحمد محمود", leadingIcon = Icons.Default.Person, testTag = "auth_reg_name_input")
                Spacer(modifier = Modifier.height(14.dp))
                AppInput(value = regPhone, onValueChange = { regPhone = it }, label = "رقم الهاتف", placeholder = "01xxxxxxxxx", leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), testTag = "auth_reg_phone_input")
                Spacer(modifier = Modifier.height(14.dp))
                AppInput(value = regEmail, onValueChange = { regEmail = it }, label = "البريد الإلكتروني (اختياري)", leadingIcon = Icons.Default.Email, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), testTag = "auth_reg_email_input")
                Spacer(modifier = Modifier.height(14.dp))
                AppInput(value = regPassword, onValueChange = { regPassword = it }, label = "كلمة المرور", leadingIcon = Icons.Default.Lock, visualTransformation = PasswordVisualTransformation(), testTag = "auth_reg_password_input")
                Spacer(modifier = Modifier.height(14.dp))
                AppInput(value = regConfirmPassword, onValueChange = { regConfirmPassword = it }, label = "تأكيد كلمة المرور", leadingIcon = Icons.Default.Lock, visualTransformation = PasswordVisualTransformation(), testTag = "auth_reg_confirm_password_input")
                Spacer(modifier = Modifier.height(20.dp))
                AppButton(
                    text = "إنشاء حساب جديد ⚡",
                    onClick = { onRegister(regName, regPhone, regEmail, regPassword, regConfirmPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_reg_submit_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("استعادة كلمة المرور عبر OTP سيتم تفعيلها مع نظام التحقق النهائي.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showForgotInfo) {
        AlertDialog(
            onDismissRequest = { showForgotInfo = false },
            title = { Text("استعادة كلمة المرور", fontWeight = FontWeight.Bold) },
            text = { Text("استعادة كلمة المرور متوقفة مؤقتًا حتى إضافة نظام OTP النهائي للحسابات.") },
            confirmButton = { TextButton(onClick = { showForgotInfo = false }) { Text("حسنًا") } }
        )
    }
}

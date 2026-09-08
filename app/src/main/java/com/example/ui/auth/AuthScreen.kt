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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*

@Composable
fun AuthScreen(
    onLogin: (identifier: String, password: String) -> Unit,
    onRegister: (name: String, phone: String, email: String, password: String, confirmation: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    // Forgot password dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPhone by remember { mutableStateOf("") }
    var forgotStep by remember { mutableIntStateOf(1) } // 1: phone, 2: code, 3: new pass

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

        // Brand Hero
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⚡", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "فالسريع",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Text(
            text = "اطلب.. يوصلك فالسريع ⚡",
            style = MaterialTheme.typography.titleMedium,
            color = BrandPrimaryDark,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Segmented Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = BrandPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "تسجيل الدخول",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "إنشاء حساب",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            // Login Form
            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppInput(
                    value = phoneOrEmail,
                    onValueChange = { phoneOrEmail = it },
                    label = "رقم الهاتف أو البريد الإلكتروني",
                    placeholder = "01xxxxxxxxx",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    testTag = "auth_login_phone_input"
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
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "إظهار كلمة المرور"
                            )
                        }
                    },
                    testTag = "auth_login_password_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberSession,
                            onCheckedChange = { rememberSession = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandPrimary)
                        )
                        Text(
                            text = "تذكر الجلسة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text(
                            text = "نسيت كلمة المرور؟",
                            color = BrandPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppButton(
                    text = "تسجيل الدخول",
                    onClick = { onLogin(phoneOrEmail, password) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_login_submit_button"
                )
            }
        } else {
            // Register Form
            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppInput(
                    value = regName,
                    onValueChange = { regName = it },
                    label = "الاسم بالكامل",
                    placeholder = "مثال: أحمد محمود",
                    leadingIcon = Icons.Default.Person,
                    testTag = "auth_reg_name_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppInput(
                    value = regPhone,
                    onValueChange = { regPhone = it },
                    label = "رقم الهاتف",
                    placeholder = "01xxxxxxxxx",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    testTag = "auth_reg_phone_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppInput(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = "البريد الإلكتروني (اختياري)",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    testTag = "auth_reg_email_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppInput(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = "كلمة المرور",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    testTag = "auth_reg_password_input"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppInput(
                    value = regConfirmPassword,
                    onValueChange = { regConfirmPassword = it },
                    label = "تأكيد كلمة المرور",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    testTag = "auth_reg_confirm_password_input"
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppButton(
                    text = "إنشاء حساب جديد ⚡",
                    onClick = { onRegister(regName, regPhone, regEmail, regPassword, regConfirmPassword) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_reg_submit_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))


    }

    // Forgot Password Flow Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotStep = 1
            },
            title = {
                Text(
                    text = when (forgotStep) {
                        1 -> "استعادة كلمة المرور"
                        2 -> "كود التحقق (OTP)"
                        else -> "تعيين كلمة مرور جديدة"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    when (forgotStep) {
                        1 -> {
                            Text(
                                "أدخل رقم هاتفك المسجل وسنرسل لك كود التحقق.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AppInput(
                                value = forgotPhone,
                                onValueChange = { forgotPhone = it },
                                label = "رقم الهاتف",
                                placeholder = "01xxxxxxxxx",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        2 -> {
                            Text(
                                "تم إرسال كود التحقق المكون من 4 أرقام إلى هاتفك.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            var otpCode by remember { mutableStateOf("1234") }
                            AppInput(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = "كود التحقق",
                                placeholder = "1234",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        3 -> {
                            Text(
                                "أدخل كلمة المرور الجديدة لحسابك.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            var newPass by remember { mutableStateOf("") }
                            AppInput(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = "كلمة المرور الجديدة",
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = when (forgotStep) {
                        1 -> "إرسال الكود"
                        2 -> "تحقق"
                        else -> "حفظ والدخول"
                    },
                    onClick = {
                        if (forgotStep < 3) {
                            forgotStep++
                        } else {
                            showForgotPasswordDialog = false
                            forgotStep = 1
                            // Password reset requires a verified backend and is not performed locally.
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotPasswordDialog = false
                    forgotStep = 1
                }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

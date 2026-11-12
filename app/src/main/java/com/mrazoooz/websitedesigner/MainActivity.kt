package com.mrazoooz.websitedesigner

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.webkit.WebSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WebsiteDesignerScreen() }
    }
}
private val Ink = Color(0xFF172033)
private val Accent = Color(0xFF536DFE)
private val Page = Color(0xFFF4F6FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteDesignerScreen() {
    var feature by remember { mutableStateOf("") }
    var design by remember { mutableStateOf("") }
    var interaction by remember { mutableStateOf("") }
    val html = remember(feature, design, interaction) {
        WebsiteBuilder.build(feature, design, interaction)
    }
    MaterialTheme(colorScheme = lightColorScheme(primary = Accent, background = Page)) {
        Scaffold(topBar = {
            TopAppBar(title = {
                Column {
                    Text("مصمم المواقع الذكي", fontWeight = FontWeight.Bold, color = Ink)
                    Text("أنشئ موقعك بالوصف — بدون API", fontSize = 11.sp, color = Color.Gray)
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
        }, containerColor = Page) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("اكتب التعديلات التي تريدها", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                DescriptionField("١. إضافة ميزة", "مثال: أضف زر تسجيل الدخول أو قسم للأسعار", feature) { feature = it }
                DescriptionField("٢. تحسين التصميم", "مثال: اجعل التصميم زجاجياً وألوانه خضراء", design) { design = it }
                DescriptionField("٣. التفاعل", "مثال: عند الضغط أظهر رسالة ترحيب", interaction) { interaction = it }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("معاينة الموقع", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Surface(color = Color(0xFFE4F7EC), shape = RoundedCornerShape(50)) {
                        Text("● مباشر", color = Color(0xFF16834A), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
                Surface(Modifier.fillMaxWidth().height(510.dp), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 3.dp) {
                    LiveWebsitePreview(html)
                }
                OutlinedButton(onClick = { feature = ""; design = ""; interaction = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text("إعادة ضبط الأوصاف")
                }
                Text("يعمل هذا الإصدار محلياً بقواعد جاهزة، ويفهم أوصافاً شائعة مثل الأزرار والأقسام والألوان والرسائل، وليس محادثة ذكاء اصطناعي عامة.",
                    color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
    }
}
@Composable
private fun DescriptionField(title: String, hint: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value, onValueChange, placeholder = { Text(hint, fontSize = 12.sp, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4, shape = RoundedCornerShape(13.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFFDCE1EC),
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
    }
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LiveWebsitePreview(html: String) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(android.graphics.Color.WHITE)
            loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }, update = { it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null) })
}
private object WebsiteBuilder {
    fun build(feature: String, design: String, interaction: String): String {
        val f = feature.lowercase()
        val d = design.lowercase()
        val i = interaction.lowercase()
        val green = listOf("أخضر", "خضراء", "green").any { d.contains(it) }
        val purple = listOf("بنفسجي", "بنفسجية", "purple").any { d.contains(it) }
        val dark = listOf("داكن", "مظلم", "أسود", "dark").any { d.contains(it) }
        val glass = listOf("زجاج", "شفاف", "glass").any { d.contains(it) }
        val primary = when { green -> "#16865B"; purple -> "#7654C8"; orange -> "#E87932"; red -> "#D94B55"; else -> "#536DFE" }
        val bg = if (dark) "#111827" else "#F3F5FA"\n        val radius = if (rounded) "26px" else "17px"\n        val shadowStyle = if (shadow) "0 14px 35px rgba(0,0,0,.16)" else "0 5px 18px rgba(20,30,50,.07)"\n        val pageBackground = if (gradient) "linear-gradient(135deg,$bg,${if (dark) "#26344D" else "#E5EAFE"})" else bg
        val card = if (dark) "#202B3D" else "#FFFFFF"
        val text = if (dark) "#F8FAFC" else "#172033"
        val muted = if (dark) "#B8C2D3" else "#667085"
        val title = when { f.contains("متجر") -> "متجري الإلكتروني"; f.contains("مدرس") || f.contains("تعليم") -> "منصة التعلم"; f.contains("مطعم") -> "مطعمي"; else -> "موقعي الجديد" }
        val hasLogin = listOf("تسجيل الدخول", "دخول", "login").any { f.contains(it) }
        val hasPricing = listOf("أسعار", "السعر", "اشتراك", "pricing").any { f.contains(it) }
        val hasContact = listOf("تواصل", "اتصال", "contact").any { f.contains(it) }
        val hasAbout = listOf("من نحن", "نبذة", "about").any { f.contains(it) }
        val hasImage = listOf("صورة", "image").any { f.contains(it) }
        val buttonText = if (hasLogin) "تسجيل الدخول" else "ابدأ الآن"
        val action = when {
            listOf("رسالة", "تنبيه", "alert", "ترحيب").any { i.contains(it) } -> "alert('مرحباً بك! تم تنفيذ التفاعل بنجاح.')"
            listOf("تغيير اللون", "لون الزر", "change color").any { i.contains(it) } -> "document.querySelector('.main-btn').style.background='#E76F51';"
            listOf("إخفاء", "اخف", "hide").any { i.contains(it) } -> "document.querySelector('#extra').style.display='none';"
            else -> "document.querySelector('#status').textContent='تم الضغط على الزر بنجاح!';"
        }
        val extras = buildString {
            if (hasLogin) append("<div class=\"feature\"><h3>تسجيل الدخول</h3><input placeholder=\"البريد الإلكتروني\"><input placeholder=\"كلمة المرور\" type=\"password\"><button class=\"main-btn\" onclick=\"alert('هذه معاينة لنموذج الدخول')\">دخول</button></div>")
            if (hasPricing) append("<div class=\"feature\"><h3>الأسعار</h3><p>الخطة الأساسية — مجاناً</p><p>الخطة المتقدمة — قريباً</p></div>")
            if (hasContact) append("<div class=\"feature\"><h3>تواصل معنا</h3><p>يسعدنا استقبال رسالتك</p><input placeholder=\"اكتب رسالتك\"><button class=\"main-btn\" onclick=\"alert('تم إرسال الرسالة التجريبية')\">إرسال</button></div>")
            if (hasAbout) append("<div class=\"feature\"><h3>من نحن</h3><p>نقدم تجربة بسيطة وسهلة الاستخدام.</p></div>")
            if (hasImage) append("<div class=\"feature image-placeholder\">مساحة الصورة الرئيسية</div>")
        }
        val glassCss = if (glass) "backdrop-filter:blur(12px);border:1px solid rgba(255,255,255,.22);" else ""
        val safeTitle = title.replace("<", "").replace(">", "")
        return """<!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<style>*{box-sizing:border-box}body{margin:0;padding:22px 14px;background:$bg;color:$text;font-family:Arial,sans-serif}
header{text-align:center;padding:22px 12px;border-radius:20px;background:$card;$glassCss}h1{font-size:25px;margin:0 0 9px}
p{line-height:1.8;color:$muted;font-size:14px}.main-btn{border:0;border-radius:12px;padding:13px 22px;background:$primary;color:white;font-weight:bold;font-size:14px;cursor:pointer;width:100%;margin-top:10px}
.feature{background:$card;$glassCss;box-shadow:$shadowStyle;margin-top:14px;padding:18px;border-radius:$radius}.feature h3{margin:0 0 8px;color:$text}
.feature input{width:100%;padding:12px;margin-top:9px;border:1px solid #cbd5e1;border-radius:10px;font-size:14px}
.image-placeholder{height:115px;display:grid;place-items:center;color:$muted;background:rgba(130,140,160,.13)}#status{text-align:center;color:$primary;font-weight:bold;min-height:25px;margin-top:12px}</style></head>
<body><header><h1>$title</h1><p>تم إنشاء هذه الصفحة من وصفك. عدّل الحقول لترى النتيجة.</p>$buttonHtml</header>
<div id="extra">$extras</div><div id="status"></div></body></html>"""
    }
}
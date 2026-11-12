package com.mrazoooz.websitedesigner

import kotlin.math.exp
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Kotlin-only website design engine.
 *
 * The small neural network classifies a brief into a site category. A structured
 * generator then creates editable starter HTML/CSS/JS. This is an experimental
 * on-device model, not a general-purpose LLM and it does not call a hosted API.
 */
object WebsiteAIEngine {
    private data class Sample(val text: String, val label: String)

    private class TinyIntentNetwork {
        private val samples = listOf(
            Sample("متجر تسوق منتجات سلة شراء متجر الكتروني", "shop"),
            Sample("بيع منتجات اسعار سلة مشتريات", "shop"),
            Sample("مطعم وجبات طعام قائمة الطعام حجز طاولة", "restaurant"),
            Sample("مقهى قهوة مشروبات اطلب الان", "restaurant"),
            Sample("مدرسة تعليم دورات دروس منصة تعلم", "education"),
            Sample("جامعة طالب معلم كورسات", "education"),
            Sample("معرض اعمال شخصي سيرة ذاتية portfolio", "portfolio"),
            Sample("مصمم مبرمج مشاريعي خبراتي", "portfolio"),
            Sample("شركة خدمات حلول فريق عمل من نحن", "business"),
            Sample("مؤسسة اعمال تواصل معنا خدمات", "business"),
            Sample("صفحة هبوط منتج جديد ابدأ الان", "landing"),
            Sample("موقع بسيط صفحة رئيسية ترحيب", "landing")
        )
        private val labels = samples.map { it.label }.distinct()
        private val vocab = samples.flatMap { tokenize(it.text) }.distinct().sorted()
        private val index = vocab.withIndex().associate { it.value to it.index }
        private val hidden = 20
        private val random = Random(73)
        private val w1 = Array(hidden) { DoubleArray(vocab.size) { random.nextDouble(-0.18, 0.18) } }
        private val b1 = DoubleArray(hidden)
        private val w2 = Array(labels.size) { DoubleArray(hidden) { random.nextDouble(-0.18, 0.18) } }
        private val b2 = DoubleArray(labels.size)

        init { train() }

        fun predict(text: String): String {
            val input = vector(text)
            if (input.all { it == 0.0 }) return "landing"
            val h = DoubleArray(hidden) { j ->
                tanh(b1[j] + input.indices.sumOf { i -> w1[j][i] * input[i] })
            }
            val scores = DoubleArray(labels.size) { k ->
                b2[k] + h.indices.sumOf { j -> w2[k][j] * h[j] }
            }
            val best = scores.indices.maxByOrNull { scores[it] } ?: 0
            return labels[best]
        }

        private fun train() {
            val training = samples.map { vector(it.text) to labels.indexOf(it.label) }
            repeat(260) {
                training.shuffled(random).forEach { (x, target) ->
                    val h = DoubleArray(hidden) { j ->
                        tanh(b1[j] + x.indices.sumOf { i -> w1[j][i] * x[i] })
                    }
                    val logits = DoubleArray(labels.size) { k ->
                        b2[k] + h.indices.sumOf { j -> w2[k][j] * h[j] }
                    }
                    val max = logits.maxOrNull() ?: 0.0
                    val e = DoubleArray(logits.size) { exp(logits[it] - max) }
                    val sum = e.sum().coerceAtLeast(1e-12)
                    val gradOut = DoubleArray(e.size) { e[it] / sum }
                    gradOut[target] -= 1.0
                    val gradHidden = DoubleArray(hidden)
                    for (k in labels.indices) {
                        for (j in 0 until hidden) {
                            gradHidden[j] += w2[k][j] * gradOut[k]
                            w2[k][j] -= 0.055 * gradOut[k] * h[j]
                        }
                        b2[k] -= 0.055 * gradOut[k]
                    }
                    for (j in 0 until hidden) {
                        val g = gradHidden[j] * (1.0 - h[j] * h[j])
                        for (i in x.indices) w1[j][i] -= 0.055 * g * x[i]
                        b1[j] -= 0.055 * g
                    }
                }
            }
        }

        private fun vector(text: String): DoubleArray {
            val result = DoubleArray(vocab.size)
            tokenize(text).forEach { token -> index[token]?.let { result[it] += 1.0 } }
            val total = result.sum().coerceAtLeast(1.0)
            return DoubleArray(result.size) { result[it] / total }
        }

        private fun tokenize(value: String) = normalize(value).split(Regex("\\s+"))
            .filter { it.length > 1 }
    }

    private val network by lazy { TinyIntentNetwork() }

    fun build(feature: String, design: String, interaction: String): String {
        if (feature.isBlank() && design.isBlank() && interaction.isBlank()) return ""
        val brief = listOf(feature, design, interaction).joinToString(" ")
        val normalized = normalize(brief)
        val category = network.predict(brief)
        val title = when (category) {
            "shop" -> "متجري الإلكتروني"
            "restaurant" -> "مطعمي"
            "education" -> "منصة التعلم"
            "portfolio" -> "معرض أعمالي"
            "business" -> "موقع الشركة"
            else -> extractTitle(feature)
        }
        val dark = has(normalized, "داكن", "مظلم", "اسود", "dark")
        val glass = has(normalized, "زجاج", "زجاجي", "شفاف", "glass")
        val gradient = has(normalized, "متدرج", "تدرج", "gradient")
        val rounded = has(normalized, "دائري", "مستدير", "ناعمه", "rounded")
        val primary = when {
            has(normalized, "اخضر", "خضراء") -> "#16865B"
            has(normalized, "بنفسجي", "بنفسجيه") -> "#7654C8"
            has(normalized, "برتقالي", "برتقالية") -> "#E87932"
            has(normalized, "احمر", "حمراء") -> "#D94B55"
            else -> "#536DFE"
        }
        val bg = if (dark) "#111827" else "#F3F5FA"
        val card = if (dark) "#202B3D" else "#FFFFFF"
        val text = if (dark) "#F8FAFC" else "#172033"
        val muted = if (dark) "#B8C2D3" else "#667085"
        val radius = if (rounded) "26px" else "17px"
        val shadow = if (has(normalized, "ظل", "ظلال", "shadow")) "0 14px 35px rgba(0,0,0,.16)" else "0 5px 18px rgba(20,30,50,.07)"
        val pageBg = if (gradient) "linear-gradient(135deg,$bg,#DDE6FF)" else bg

        val sections = buildList {
            add("الرئيسية")
            if (has(normalized, "منتجات", "خدمات", "متجر", "قائمة الطعام", "قائمه الطعام")) add("الخدمات والمنتجات")
            if (has(normalized, "اسعار", "السعر", "اشتراك", "باقات")) add("الأسعار")
            if (has(normalized, "من نحن", "نبذه", "فريق")) add("من نحن")
            if (has(normalized, "تواصل", "اتصال", "رساله", "بريد")) add("تواصل معنا")
            if (has(normalized, "تسجيل", "دخول", "حساب")) add("تسجيل الدخول")
        }.distinct()
        val sectionsHtml = sections.drop(1).joinToString("") { section ->
            "<section class='feature'><h2>${escape(section)}</h2><p>أضف تفاصيل ${escape(section)} هنا.</p></section>"
        }
        val nav = sections.joinToString("") { "<a href='#main'>${escape(it)}</a>" }
        val action = when {
            has(normalize(interaction), "رساله", "تنبيه", "ترحيب", "alert") -> "alert('مرحباً بك!')"
            has(normalize(interaction), "تغيير اللون", "لون الزر") -> "document.querySelector('.main-btn').style.filter='brightness(.85)'"
            else -> "document.getElementById('status').textContent='تم تنفيذ الأمر بنجاح!'"
        }
        val button = if (interaction.isNotBlank() || feature.isNotBlank()) {
            "<button class='main-btn' onclick=\"$action\">ابدأ الآن</button>"
        } else ""
        val glassCss = if (glass) "backdrop-filter:blur(12px);border:1px solid rgba(255,255,255,.25);" else ""
        val safeTitle = escape(title)
        return """
<!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>$safeTitle</title>
<style>
*{box-sizing:border-box}body{margin:0;padding:18px;background:$pageBg;color:$text;font-family:Arial,sans-serif;line-height:1.7}
nav{display:flex;flex-wrap:wrap;justify-content:space-between;gap:12px;padding:16px;background:$card;border-radius:$radius;box-shadow:$shadow;$glassCss}
nav strong{color:$primary;font-size:19px}nav a{color:$text;text-decoration:none;font-size:12px;margin:4px}
main{max-width:950px;margin:22px auto}.hero,.feature{background:$card;$glassCss;box-shadow:$shadow;border-radius:$radius;padding:22px;margin-bottom:14px}
.hero{text-align:center;padding:44px 20px}h1{font-size:clamp(26px,6vw,42px);margin:0 0 10px}
p{color:$muted}.main-btn{border:0;border-radius:12px;padding:13px 24px;background:$primary;color:white;font-weight:bold;cursor:pointer}
.feature input{width:100%;padding:12px;border:1px solid #cbd5e1;border-radius:10px;margin:6px 0}
#status{min-height:24px;color:$primary;font-weight:bold}
</style></head><body><nav><strong>$safeTitle</strong><div>$nav</div></nav>
<main id="main"><section class="hero"><h1>$safeTitle</h1>
<p>تم إنشاء مسودة موقعك بواسطة محرك Kotlin العصبي التجريبي.</p>$button<p id="status" aria-live="polite"></p></section>
$sectionsHtml</main></body></html>
        """.trimIndent()
    }

    private fun extractTitle(feature: String): String {
        val clean = feature.trim().take(48)
        return if (clean.isBlank()) "موقعي الجديد" else clean
    }

    private fun normalize(value: String) = value.lowercase()
        .replace(Regex("[أإآٱ]"), "ا").replace("ة", "ه").replace("ى", "ي")
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")

    private fun has(text: String, vararg terms: String) = terms.any { normalize(it) in text }

    private fun escape(value: String) = value.replace("&", "&amp;")
        .replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;")
}

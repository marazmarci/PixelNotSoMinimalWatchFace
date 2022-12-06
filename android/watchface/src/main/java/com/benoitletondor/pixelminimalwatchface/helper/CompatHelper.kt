/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.helper

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ComplicationText
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.benoitletondor.pixelminimalwatchface.Device
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.common.helper.dpToPx
import com.benoitletondor.pixelminimalwatchface.model.Storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private val galaxyWatch4AODBuggyWearOSVersions = setOf(
    "EVA8",
    "EVA9",
)

val isGalaxyWatch4AODBuggyWearOSVersion = Device.isSamsungGalaxyWatch && Build.VERSION.INCREMENTAL.takeLast(4) in galaxyWatch4AODBuggyWearOSVersions
val isGalaxyWatch4CalendarBuggyWearOSVersion = Device.isSamsungGalaxyWatch && (
    Build.VERSION.SECURITY_PATCH.startsWith("2022-01") ||
    Build.VERSION.SECURITY_PATCH.startsWith("2022-02") ||
    Build.VERSION.SECURITY_PATCH.startsWith("2022-03") ||
    Build.VERSION.SECURITY_PATCH.startsWith("2022-04")
)

val hasWidgetFrozenBug = Device.isWearOS3
val hasAmbientDisplayGoingOffBug = Device.isPixelWatch

fun Context.getTopAndBottomMargins(): Float {
    return when {
        Device.isOppoWatch -> dpToPx(5).toFloat()
        Device.isSamsungGalaxyWatchBigScreen(this) -> dpToPx(29).toFloat()
        Device.isSamsungGalaxyWatch -> dpToPx(26).toFloat()
        Device.isWearOS3 -> dpToPx(24).toFloat()
        else -> resources.getDimension(R.dimen.screen_top_and_bottom_margin)
    }
}

private val timeDateFormatter24h = SimpleDateFormat("HH:mm", Locale.US)
private val timeDateFormatter12h = SimpleDateFormat("h:mm", Locale.US)

private var heartRateIcon: Icon? = null

fun ComplicationData.sanitize(
    context: Context,
    storage: Storage,
    watchFaceComplicationId: Int,
    providerInfo: ComplicationProviderInfo?,
): ComplicationData {
    try {
        if (!Device.isSamsungGalaxyWatch) {
            return this
        }

        if (providerInfo == null) {
            return this
        }

        if (type == ComplicationData.TYPE_EMPTY) {
            return this
        }

        return when {
            providerInfo.isSamsungHeartRateProvider() -> {
                val shortText = context.getSamsungHeartRateData() ?: "?"

                val builder = ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                    .setTapAction(tapAction)
                    .setShortText(ComplicationText.plainText(shortText))
                    .setIcon(heartRateIcon ?: kotlin.run {
                        val icon =  Icon.createWithResource(context, R.drawable.ic_heart_complication)
                        heartRateIcon = icon
                        icon
                    })
                builder.build()
            }
            providerInfo.isSamsungHealthBadComplicationData(context) -> {
                ComplicationData.Builder(this)
                    .setTapAction(
                        PendingIntent.getActivity(
                            context,
                            0,
                            Intent().apply {
                                component = getSamsungHealthHomeComponentName()
                            },
                            PendingIntent.FLAG_IMMUTABLE,
                        )
                    )
                    .build()
            }
            providerInfo.isSamsungCalendarBuggyProvider() -> {
                val nextEvent = context.getNextCalendarEvent() ?: return this
                val isLargeWidget = PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID == watchFaceComplicationId

                val builder = ComplicationData.Builder(if (isLargeWidget) { ComplicationData.TYPE_LONG_TEXT } else { ComplicationData.TYPE_SHORT_TEXT })
                    .setTapAction(PendingIntent.getActivity(
                        context,
                        0,
                        Intent().apply {
                            component = getSamsungCalendarHomeComponentName()
                        },
                        PendingIntent.FLAG_IMMUTABLE,
                    ))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_calendar_complication))

                if (isLargeWidget) {
                    builder.setLongText(ComplicationText.plainText(nextEvent.title))
                } else {
                    val eventDate = Date(nextEvent.startTimestamp)
                    val formattedTime = if (storage.getUse24hTimeFormat()) {
                        timeDateFormatter24h.format(eventDate)
                    } else {
                        timeDateFormatter12h.format(eventDate)
                    }
                    builder.setShortText(ComplicationText.plainText(formattedTime))
                }

                builder.build()
            }
            else -> this
        }
    } catch (t: Throwable) {
        Log.e("PixelWatchFace", "Error while sanitizing complication data", t)
        return this
    }
}

private fun ComplicationProviderInfo.isSamsungHealthBadComplicationData(context: Context): Boolean {
    val sHealthVersion = try {
        context.getShealthAppVersion()
    } catch (e: Throwable) {
        return false
    }

    return when {
        sHealthVersion == S_HEALTH_6_20_0_016  -> isSamsungDailyActivityBuggyProvider() ||
            isSamsungStepsProvider() ||
            isSamsungSleepProvider() ||
            isSamsungWaterSleepProvider()
        sHealthVersion >= S_HEALTH_6_21_0_051 -> isSamsungDailyActivityBuggyProvider()
        else -> false
    }
}

private fun ComplicationProviderInfo.isSamsungDailyActivityBuggyProvider(): Boolean {
    return appName in samsungHealthAppNames && providerName in dailyActivityProviderNames
}

fun ComplicationProviderInfo.isSamsungCalendarBuggyProvider(): Boolean {
    return isGalaxyWatch4CalendarBuggyWearOSVersion
        && appName in oneUIWatchHomeAppNames
        && providerName in calendarProviderNames
}

private fun ComplicationProviderInfo.isSamsungStepsProvider(): Boolean {
    return appName in samsungHealthAppNames && providerName in stepsProviderNames
}

private fun ComplicationProviderInfo.isSamsungSleepProvider(): Boolean {
    return appName in samsungHealthAppNames && providerName in sleepProviderNames
}

private fun ComplicationProviderInfo.isSamsungWaterSleepProvider(): Boolean {
    return appName in samsungHealthAppNames && providerName in waterProviderNames
}

fun ComplicationProviderInfo.isSamsungHeartRateProvider(): Boolean {
    return appName in samsungHealthAppNames && providerName in heartRateProviderNames
}

private fun Context.getShealthAppVersion(): Long {
    val packageInfo = packageManager.getPackageInfo(S_HEALTH_PACKAGE_NAME, 0)
    return PackageInfoCompat.getLongVersionCode(packageInfo)
}

@SuppressLint("NewApi", "Range")
private fun Context.getNextCalendarEvent(): CalendarEvent? {
    try {
        val uri = "content://$S_CALENDAR_PACKAGE_NAME.watch/nextEvents"

        contentResolver.query(
            Uri.parse(uri),
            null,
            Bundle().apply { putInt(ContentResolver.QUERY_ARG_LIMIT, 1) },
            null,
        )?.use { query ->
            if (query.count == 0) {
                return null
            }

            query.moveToFirst()
            val eventTitle = query.getString(query.getColumnIndex("title"))
            val eventStartTimestamp = query.getLong(query.getColumnIndex("begin"))

            return CalendarEvent(eventTitle, eventStartTimestamp)
        }

        return null
    } catch (e: Exception) {
        Log.e("CompatHelper", "Error while getting next event", e)
        return null
    }
}

private data class CalendarEvent(
    val title: String,
    val startTimestamp: Long,
)

private fun Context.getSamsungHeartRateData(): String? {
    val uri = "content://$S_HEALTH_PACKAGE_NAME.healthdataprovider"

    val bundle = contentResolver.call(Uri.parse(uri), "heart_rate", null, null)
    if (bundle != null) {
        val error = bundle.getString("error")
        if (error != null) {
            return null
        }

        val data = bundle.getString("data") ?: return null
        val json = JSONObject(data)
        val hr = json.optDouble("value", -1.0)
        return if (hr > 0) {
            hr.roundToInt().toString()
        } else {
            null
        }
    }

    return null
}

fun Context.watchSamsungHeartRateUpdates(): Flow<Unit> = callbackFlow {
    val uri = "content://$S_HEALTH_PACKAGE_NAME.healthdataprovider/"
    val heartRateMethod = "heart_rate"

    fun unregister(observer: ContentObserver) {
        contentResolver.unregisterContentObserver(observer)
        contentResolver.call(Uri.parse(uri), heartRateMethod, "unregister", null)
    }

    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)

            trySendBlocking(Unit)
                .onFailure {
                    unregister(this)
                }
        }
    }

    contentResolver.call(Uri.parse(uri), heartRateMethod, "register", null)
    contentResolver.registerContentObserver(Uri.parse(uri), true, observer)

    awaitClose { unregister(observer) }
}

private fun getSamsungHealthHomeComponentName() = ComponentName(
    S_HEALTH_PACKAGE_NAME,
    "com.samsung.android.wear.shealth.app.home.HomeActivity"
)

private fun getSamsungCalendarHomeComponentName() = ComponentName(
    S_CALENDAR_PACKAGE_NAME,
    "com.samsung.android.app.calendar.view.daily.DailyActivity"
)

private const val S_HEALTH_PACKAGE_NAME = "com.samsung.android.wear.shealth"
private const val S_CALENDAR_PACKAGE_NAME = "com.samsung.android.calendar"
private const val S_HEALTH_6_20_0_016 = 6200016L
private const val S_HEALTH_6_21_0_051 = 6210051L

private val oneUIWatchHomeAppNames = setOf(
    "One UI Watch Home",
    "شاشة One UI الرئيسية للساعة",
    "One UI ঘড়ীৰ হোম",
    "One UI Saat Əsas ekranı",
    "Галоўны экран One UI для гадз.",
    "Начало на часовник с One UI",
    "One UI ঘড়ির হোম",
    "One UI ঘড়ির হোম",
    "Početni ekran za One UI Watch",
    "Inici de rellotge One UI",
    "One UI pro chytré hodinky",
    "Startside for One UI til ur",
    "One UI-Uhr-Startbildschirm",
    "Αρχική ρολογιού One UI",
    "One UI Watch Home",
    "Inicio de One UI Watch",
    "Inicio de One UI Watch",
    "One UI kella avaekraan",
    "One UI erlojuen pant. nagusia",
    "صفحه اصلی ساعت مچی One UI",
    "One UI -kellon etusivu",
    "Écran d'accueil de montre One UI",
    "Écran d'accueil de montre One UI",
    "Baile Uaireadóra One UI",
    "Inicio de reloxo One UI",
    "One UI ઘડિયાળનું હોમ",
    "One UI घड़ी होम",
    "Početni zaslon za One UI Watch",
    "One UI kezdőképernyő az órán",
    "One UI Ժամացույցի Հիմն. էկրան",
    "Beranda One UI Watch",
    "Heimaskjár One UI-úrs",
    "Home di One UI Watch",
    "בית One UI לשעון",
    "One UI時計ホーム",
    "One UI საათის საწყისი გვერდი",
    "One UI сағат-ң Бастапқы пернесі",
    "គេហ One UI Watch",
    "One UI Watch ಹೋಮ್",
    "One UI Watch 홈",
    "One UI Үй Көзөмөлү",
    "One UI Watch Home",
    "„One UI“ laikrodžio pradžia",
    "One UI pulksteņa sākuma ekrāns",
    "One UI Watch Home",
    "One UI വാച്ച് ഹോം",
    "One UI Цагийн Гэр",
    "One UI घड्याळ होम",
    "One UI Watch Home",
    "One UI Watch Home",
    "One UI Watch Home",
    "One UI-klokkestart",
    "One UI वाच होम",
    "One UI वाच होम",
    "Startpagina One UI horloge",
    "One UI ଘଣ୍ଟା ହୋମ୍",
    "One UI Watch ਹੋਮ",
    "Ekran główny One UI do zegarka",
    "Ekran główny One UI do zegarka",
    "Tela inicial do One UI Watch",
    "One UI Watch Home",
    "Tasta Acasă a ceasului One UI",
    "One UI Watch Home",
    "One UI ඔරලෝසු නිවහන",
    "One UI ඔරලෝසු නිවහන",
    "Domovská obr. One UI pre hodinky",
    "Domača stran One UI za uro",
    "Baza One UI për orën",
    "One UI početni ekran sata",
    "Hemskärm för One UI-klocka",
    "One UI வாட்ச் முகப்பு",
    "One UI గడియారం హోమ్",
    "Экрани асосии One UI барои соат",
    "หน้าหลัก One UI Watch",
    "One UI sagat öýi",
    "One UI Watch Home",
    "One UI Watch Ana ekranı",
    "One UI Watch Home",
    "One UI واچ ہوم",
    "One UI Soat asosiy tugmasi",
    "Trang chủ One UI Watch",
    "One UI 手表主屏幕",
    "One UI Watch Home",
    "One UI 手錶首頁",
)

private val samsungHealthAppNames = setOf(
    "Samsung Health",
    "三星健康",
    "S 健康",
    "S Health",
    "Sབདེ་ཐང་།",
    "삼성 헬스",
    "S헬스",
)

private val waterProviderNames = setOf(
    "Water",
    "ماء",
    "পানী",
    "Su",
    "Вада",
    "Вода",
    "পানি",
    "জল",
    "ཆུ།",
    "Voda",
    "Aigua",
    "Voda",
    "Vand",
    "Wasser",
    "Νερό",
    "Water",
    "Agua",
    "Agua",
    "Vesi",
    "Ura",
    "آب",
    "Vesi",
    "Eau",
    "Eau",
    "Uisce",
    "Auga",
    "પાણી",
    "पानी",
    "Voda",
    "Víz",
    "Ջուր",
    "Air",
    "Vatn",
    "Acqua",
    "מים",
    "水分",
    "წყალი",
    "Су",
    "ទឹក",
    "ನೀರು",
    "물",
    "Суу",
    "ນໍ້າ",
    "Vanduo",
    "Ūdens",
    "Вода",
    "വാട്ടര്‍",
    "Ус",
    "पाणी",
    "Air",
    "ရေ",
    "ေရ",
    "Vann",
    "जल",
    "जल",
    "Water",
    "ଜଳ",
    "ਪਾਣੀ",
    "Woda",
    "Woda",
    "Água",
    "Água",
    "Apă",
    "Вода",
    "ජලය",
    "ජලය",
    "Voda",
    "Voda",
    "Ujë",
    "Voda",
    "Vatten",
    "நீர்",
    "నీరు",
    "Об",
    "น้ำ",
    "Suw",
    "Tubig",
    "Su",
    "سۇ",
    "Вода",
    "پانی",
    "Suv",
    "Nước",
    "水",
    "水",
    "水",
)

private val sleepProviderNames = setOf(
    "Sleep",
    "النوم",
    "নিদ্ৰা",
    "Yuxu",
    "Сон",
    "Сън",
    "ঘুম",
    "নিদ্রা",
    "གཉིད་མལ།",
    "Spavanje",
    "Repòs",
    "Spánek",
    "Søvn",
    "Schlaf",
    "Ύπνος",
    "Sleep",
    "Sueño",
    "Sueño",
    "Magamine",
    "Lo",
    "خواب",
    "Uni",
    "Sommeil",
    "Sommeil",
    "Codladh",
    "Sono",
    "નિદ્રા",
    "निद्रा",
    "Spavanje",
    "Alvás",
    "Քուն",
    "Tidur",
    "Svefn",
    "Sonno",
    "שינה",
    "睡眠",
    "ძილი",
    "Ұйқы",
    "គេង",
    "ನಿದ್ರೆ",
    "수면",
    "Уйку",
    "ນອນຫຼັບ",
    "Miegas",
    "Miegs",
    "Спиење",
    "ഉറക്കം",
    "Унтлага",
    "झोप",
    "Tidur",
    "အိပ်စက်ခြင်း",
    "အိပ္စက္ျခင္း",
    "Søvn",
    "शयन",
    "शयन",
    "Slaap",
    "ଶୟନ",
    "ਨੀਂਦ",
    "Sen",
    "Spanie",
    "Sono",
    "Dormir",
    "Somn",
    "Сон",
    "නින්ද",
    "නින්ද",
    "Spánok",
    "Spanje",
    "Gjumë",
    "Spavanje",
    "Sömn",
    "உறக்கம்",
    "నిద్ర స్థితి",
    "Хоб",
    "การนอนหลับ",
    "Uky",
    "Pagtulog",
    "Uyku",
    "睡眠",
    "Сон",
    "سلیپ",
    "Uyqu",
    "Ngủ",
    "睡眠",
    "睡眠",
    "睡眠",
)

private val stepsProviderNames = setOf(
    "Steps",
    "الخطوات",
    "খোজ",
    "Addımlar",
    "Крокі",
    "Крачки",
    "পদক্ষেপ",
    "পদক্ষেপগুলি",
    "གོམ་གྲངས་འཇལ",
    "Koraci",
    "Passes",
    "Kroky",
    "Skridt",
    "Schritte",
    "Βήματα",
    "Steps",
    "Pasos",
    "Sammud",
    "Pausoak",
    "قدمها",
    "Askeleet",
    "Pas",
    "Pas",
    "Céim",
    "Pasos",
    "પગલાં",
    "कदम",
    "Koraci",
    "Lépések",
    "Քայլեր",
    "Langkah",
    "Skref",
    "Passi",
    "צעדים",
    "歩",
    "ნაბიჯები",
    "Қадамдар",
    "ជំហាន",
    "ಹೆಜ್ಜೆಗಳು",
    "걸음 수",
    "Кадамдар",
    "ກ້າວ",
    "Žingsniai",
    "Soļi",
    "Чекори",
    "ചുവടുകൾ",
    "Алхам",
    "पाऊले",
    "Langkah",
    "ခြေလှမ်းများ",
    "ေျခလွမ္းမ်ား",
    "Skritt",
    "चरणहरू",
    "चरणहरू",
    "Stappen",
    "ପାଦଗୁଡିକ",
    "ਕਦਮ",
    "Kroki",
    "Krokōw",
    "Passos",
    "Passos",
    "Pași",
    "Шаги",
    "පියවර",
    "පියවර",
    "Kroky",
    "Koraki",
    "Hapat",
    "Koraci",
    "Steg",
    "காலடிகள்",
    "అడుగులు",
    "Қадамҳо",
    "ก้าว",
    "Ädimler",
    "Hakbang",
    "Adım",
    "计步",
    "Кроки",
    "مراحل",
    "Qadaml",
    "Các bước",
    "计步",
    "步數",
    "步數",
)

private val dailyActivityProviderNames = setOf(
    "Daily Activity",
    "النشاط اليومي",
    "দৈনিক কাৰ্যকলাপ",
    "Gündəlik fəaliyyət",
    "Дзённая актыўнасць",
    "Дневна активност",
    "দৈনিক অ্যাক্টিভিটি",
    "দৈনিক ক্রিয়াকলাপ",
    "ཉིན་རེའི་འགུལ་སྐྱོད་ཚད།",
    "Dnevna aktivnost",
    "Activitat diària",
    "Denní aktivita",
    "Daglig aktivitet",
    "Tägliche Aktivität",
    "Ημερήσια δραστηριότητα",
    "Daily activity",
    "Actividad diaria",
    "Igapäevane tegevus",
    "Eguneroko jarduera",
    "فعالیت روزانه",
    "Päivittäinen aktiviteetti",
    "Activité quotidienne",
    "Activité quotidienne",
    "Gníomhaíocht laethúil",
    "Actividade diaria",
    "દૈનિક પ્રવૃત્તિ",
    "प्रतिदिन की गतिविधि",
    "Dnevna aktivnost",
    "Napi tevékenység",
    "Օրական գործունեություն",
    "Aktivitas harian",
    "Dagleg hreyfing",
    "Attività giornaliera",
    "פעילות יומית",
    "1日の活動",
    "ყოველდღიური აქტივობა",
    "Күнделікті әрекет",
    "សកម្មភាព​ប្រចាំថ្ងៃ",
    "ದೈನಂದಿನ ಚಟುವಟಿಕೆ",
    "일일 활동",
    "Күнүмдүк иш-аракет",
    "ກິດຈະກຳປະຈຳວັນ",
    "Kasdienė veikla",
    "Dienas aktivitātes",
    "Дневна активност",
    "ദൈനംദിന പ്രവർത്തനം",
    "Өдөр бүрийн үйл хөдлөл",
    "दररोजची क्रिया",
    "Aktiviti harian",
    "နေ့စဉ် လှုပ်ရှားမှု",
    "ေန႔စဥ္ လႈပ္ရွားမႈ",
    "Daglig aktivitet",
    "दैनिक क्रियाकलाप",
    "दैनिक क्रियाकलाप",
    "Dagelijkse activiteit",
    "ଦୈନିକ କାର୍ଯ୍ୟକଳାପ",
    "ਰੋਜ਼ਾਨਾ ਗਤੀਵਿਧੀ",
    "Dzienna aktywność",
    "Dziynno aktywnoś",
    "Atividade diária",
    "Actividade diária",
    "Activitate zilnică",
    "Активность",
    "දිනපතා ක්‍රියාකාරකම",
    "දිනපතා ක්‍රියාකාරකම",
    "Denná aktivita",
    "Dnevna dejavnost",
    "Aktiviteti ditor",
    "Dnevna aktivnost",
    "Daglig aktivitet",
    "தினசரி செயல்பாடு",
    "రోజువారీ కార్యాచరణ",
    "Фаъолияти ҳаррӯза",
    "กิจกรรมประจำวัน",
    "Günlük işjeňligi",
    "Pang-araw-araw na aktibidad",
    "Günlük etkinlik",
    "كۈندىلىك ھەرىكەت",
    "Фізичні навантаження за день",
    "روزانہ کی سرگرمی",
    "Kundalik faoliyat",
    "Hoạt động hàng ngày",
    "每日活动量",
    "每日運動量",
    "每日活動",
)

private val heartRateProviderNames = setOf(
    "Heartrate",
    "سرعة ضربات القلب",
    "হৃদ হাৰ",
    "Ürək ritmi",
    "Част. пул.",
    "Сър. ритъм",
    "হৃদস্পন্দনের হার",
    "হৃদয. হার",
    "སྙིང་འཕར་ཚད།",
    "Puls",
    "Ritme card",
    "Srd. tep",
    "Puls",
    "Puls",
    "Καρ. παλμ.",
    "Heart rate",
    "FC",
    "RC",
    "Süd. löög.",
    "Bihotz frek.",
    "ضربان قلب",
    "Syke",
    "Cardio",
    "Fréq. car.",
    "Croíráta",
    "Ritmo car.",
    "હૃદય દર",
    "हृदय गति",
    "Otk. srca",
    "Pulzus",
    "Սրտխփ. հճխ",
    "Dnyt jntng",
    "Púls",
    "Freq. card.",
    "דופק לב",
    "心拍数",
    "პულსი",
    "Жүрек соғ.",
    "អត្រាបេះដូង",
    "ಹೃದಯ ಬಡಿತದ ದರ",
    "심박수",
    "Жүрөк согушу",
    "ອັດຕາຫົວໃຈເຕັ້ນ",
    "Šird. rit.",
    "Sirds rit.",
    "Пулс",
    "ഹൃദയമിടി.",
    "Зүрхний цохилт",
    "हृदय गती",
    "Kdr jntung",
    "နှလုံး ခုန်နှုန်း",
    "ႏွလုံး ခုန္ႏႈန္း",
    "Puls",
    "हृदय गति",
    "हृदय गति",
    "Hartslag",
    "ହାର୍ଟ୍ ରେଟ୍",
    "ਦਿਲ ਦੀ ਗਤੀ",
    "Puls",
    "Tyntno",
    "Freq. car.",
    "Freq. card.",
    "Puls",
    "Пульс",
    "හෘද වේගය",
    "හෘද වේගය",
    "Srdcový tep",
    "Srč. utrip",
    "Rr. zemrës",
    "Puls",
    "Puls",
    "இ.து.விகி.",
    "హృదయ స్పందన రేటు",
    "Тапиши дил",
    "อัตราการเต้นหัวใจ",
    "Ýürek ritm",
    "Heart rate",
    "Klp atş hz",
    "心率",
    "Пульс",
    "شرح قلب",
    "Yurak puls",
    "Nhịp tim",
    "心率",
    "心跳率",
)

private val calendarProviderNames = setOf(
    "التقويم",
    "কেলেণ্ডাৰ",
    "Təqvim",
    "Каляндар",
    "Календар",
    "ক্যালেন্ডার",
    "ক্যালেন্ডার",
    "Kalendar",
    "Calendari",
    "Kalendář",
    "Kalender",
    "Kalender",
    "Ημερολόγιο",
    "Calendar",
    "Calendario",
    "Calendario",
    "Kalender",
    "Egutegia",
    "تقویم",
    "Kalenteri",
    "Calendrier",
    "Calendrier",
    "Féilire",
    "Calendario",
    "કૅલેન્ડર",
    "कैलेंडर",
    "Kalendar",
    "Naptár",
    "Օրացույց",
    "Kalender",
    "Dagatal",
    "Calendario",
    "לוח שנה",
    "カレンダー",
    "კალენდარი",
    "Күнтізбе",
    "ប្រតិទិន",
    "ಕ್ಯಾಲೆಂಡರ್",
    "캘린더",
    "Календарь",
    "ປະຕິທິນ",
    "Kalendorius",
    "Kalendārs",
    "Календар",
    "കലണ്ടര്‍",
    "Хуанли",
    "दिनदर्शिका",
    "Kalendar",
    "ပြက္ခဒိန်",
    "ျပကၡဒိန္",
    "Kalender",
    "पात्रो",
    "पात्रो",
    "Agenda",
    "କ୍ୟାଲେଣ୍ଡର୍",
    "ਕੈਲੇਂਡਰ",
    "Kalendarz",
    "Kalyndŏrz",
    "Calendário",
    "Agenda",
    "Calendar",
    "Календарь",
    "දිනදර්ශනය",
    "දිනදර්ශනය",
    "Kalendár",
    "Koledar",
    "Kalendari",
    "Kalendar",
    "Kalender",
    "நாட்காட்டி",
    "క్యాలెండర్",
    "Тақвим",
    "ปฏิทิน",
    "Senenama",
    "Kalendaryo",
    "Takvim",
    "Календар",
    "کیلنڈر",
    "Kalendar",
    "Lịch",
    "日历",
    "日曆",
    "日曆",
)
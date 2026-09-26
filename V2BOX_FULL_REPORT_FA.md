# گزارش کامل توسعهٔ V2BOX تا ۲۶ سپتامبر ۲۰۲۶

**مخزن:** [mtpali/v2box](https://github.com/mtpali/v2box)

**نسخهٔ برنامه:** `1`؛ شناسهٔ بسته: `com.v2box.mobiletina`
**وضعیت این بازبینی:** کد رابط تصویری در شاخهٔ `main` ثبت شده، هر دو APK ساخته و در [Release جدید `v1-preview-ui`](https://github.com/mtpali/v2box/releases/tag/v1-preview-ui) منتشر شده‌اند.

## کارهای انجام‌شده

1. سورس `2dust/v2rayNG` نسخهٔ `2.2.6` با تاریخچه و مجوز GPL وارد شد. برای رفتار اتصال و اشتراک از `mtpali/MobileTinaVPN` و برای روش ساخت از `mtpali/v2rayNG` استفاده شد؛ فایل اجرایی این مراجع در برنامه قرار نگرفت.
2. نام نمایشی `V2BOX`، package و namespace برابر `com.v2box.mobiletina`، `versionCode=1` و `versionName=1` تنظیم شد. منابع برنامه به انگلیسی و فارسی محدود و جهت کلی پنجره LTR شد.
3. سه صفحهٔ **Home، Configs و Settings** و ناوبری ثابت پایین ساخته شدند. Home وضعیت و زمان اتصال، ارسال/دریافت، Smart Connect، Routing و پیوند اینستاگرام `mobile.tina2` را نشان می‌دهد.
4. Smart Connect از سرویس واقعی اندازه‌گیری پینگ استفاده می‌کند؛ کمترین مقدار مثبت را انتخاب می‌کند، تا ۲۵ ثانیه منتظر می‌ماند و قابل لغو است. اگر خاموش باشد، همان سرور انتخاب‌شده متصل می‌شود. پس از پایان انتخاب، سنجش لغو می‌شود.
5. مرتب‌سازی خودکار پس از پینگ و به‌روزرسانی اشتراک هنگام ورود به‌صورت پیش‌فرض فعال شدند. کلیدهای **Smart Connect، Auto Sort و Update Subscription** در ابتدای Settings قرار گرفتند. Root Mode و LAN Sharing از تنظیمات نمایشی برداشته شدند؛ Sniffing به‌طور پیش‌فرض خاموش است.
6. هدر `subscription-userinfo` از پاسخ موفق سرویس‌دهنده خوانده می‌شود. مقدارهای upload، download، total و expire ذخیره می‌شوند؛ حجم باقی‌مانده، تاریخ انقضا و روزهای باقی‌مانده فقط وقتی سرویس‌دهنده این اطلاعات را بدهد نشان داده می‌شوند. در صورت چند اشتراک، اشتراک سرور انتخاب‌شده یا گروه باز ترجیح دارد؛ محاسبهٔ باقیمانده در برابر سرریز محافظت شده است.
7. تصویر ارسالی کاربر، **شش اسکرین‌شات Android، سه اسکرین‌شات iOS و آیکون PNG**، بازیابی و بررسی شدند. رنگ‌ها و ساختار صفحهٔ Android بر اساس زمینهٔ مشکی، کارت‌های خاکستری تیره، انتخاب فیروزه‌ای و داک اتصال پایین بازسازی شدند. حالت روشن با زمینهٔ خاکستری بسیار روشن و کارت سفید از تصاویر iOS الهام گرفت. حالت تاریک پیش‌فرض است و کاربر می‌تواند حالت روشن را انتخاب کند.
8. Home با عنوان بزرگ V2BOX و کارت اطلاعات، Configs با سربرگ QR/ستاره/افزودن/منو، کارت‌های عمودی Local و Subscription، نوار حجم و روزهای باقی‌مانده، و ردیف‌های سرور با برچسب پروتکل، نشان پینگ و عمل‌های ویرایش/اشتراک‌گذاری/حذف پیاده شد. Settings با دسته‌های دارای آیکون، ردیف Device ID قابل کپی و کلیدهای سریع بازطراحی شد. دکمهٔ مرتب‌سازی و پینگ از صفحهٔ Configs قابل اجرا هستند.
9. فایل `icon.png` ارسالی **بدون تغییر محتوا** در منابع برنامه جایگزین آیکون موقت شد. هش SHA-256 آن و فایل داخل سورس یکسان است: `18f026ca534470782b31a7cd66deda32564a33810d3f0cd7f1defbdcb9bf50be`.
10. پیوند `mobile.tina2` ابتدا با برنامهٔ Instagram و سپس با نشانی وب باز می‌شود؛ About و منوی کناری هم به آن اشاره می‌کنند.
11. GitHub Actions با Java 21، Android SDK/NDK، ساخت hev tunnel، کتابخانهٔ Xray ثابت‌شده و Gradle برای هر دو ABI آماده شد. تأیید `aapt` شناسهٔ بسته و نسخه، و تأیید `unzip` کتابخانهٔ بومی ویژهٔ هر ABI را بررسی می‌کنند. فایل `SHA256SUMS` در نسخهٔ منتشرشده تولید می‌شود.
12. توسعهٔ پایه با [PR شمارهٔ ۱](https://github.com/mtpali/v2box/pull/1) در `main` ادغام شد. برای رفع خرابی لینک‌های موقت، APKهای نسخهٔ قبلی در [Release عمومی `v1-preview`](https://github.com/mtpali/v2box/releases/tag/v1-preview) منتشر شدند. بازطراحی تصویری در کامیت‌های [`4e9d3c9`](https://github.com/mtpali/v2box/commit/4e9d3c9c5820489467c04bccb4ce3752a9b25766) و [`6f70f6a`](https://github.com/mtpali/v2box/commit/6f70f6a37972f1bec888f11f9ab1031af953f0c1) ثبت شده است.

## آزمون و وضعیت ساخت

- تمام XMLهای منابع بدون خطای ساختاری خوانده شدند، ارجاع‌های تازهٔ رنگ/رشته/آیکون بررسی شدند و `git diff --check` موفق بود.
- [ساخت پایهٔ قبلی](https://github.com/mtpali/v2box/actions/runs/36222227077) موفق و APKهای قبلی در `v1-preview` منتشر شده بودند.
- [ساخت اول بازطراحی](https://github.com/mtpali/v2box/actions/runs/36236193837) خطای `resource style/V2Box not found` داشت؛ نام style اصلاح شد. [ساخت بازبینی دوم](https://github.com/mtpali/v2box/actions/runs/36236411082) با نتیجهٔ `success` پایان یافت: Gradle هر دو معماری را ساخت، `aapt` بسته و نسخه را بررسی کرد، کتابخانهٔ native ABI درست بررسی شد و Release منتشر شد.
- در این محیط شبیه‌ساز/دستگاه Android در دسترس نیست. همسانی پیکسلی خروجی اجرایی و عملکرد اتصال روی دستگاه هنوز آزموده نشده است.

## مسیرهای مهم سورس

| مسیر | نقش |
| --- | --- |
| `V2rayNG/app/src/main/res/layout/activity_main.xml` | Home، Configs، Settings و داک پایین |
| `V2rayNG/app/src/main/res/layout/item_recycler_main.xml` | ظاهر ردیف سرور |
| `V2rayNG/app/src/main/res/layout/layout_v2box_group_card.xml` | کارت Local و Subscription |
| `V2rayNG/app/src/main/res/drawable-nodpi/v2box_source_icon.png` | آیکون اصلی ارسالی |
| `V2rayNG/app/src/main/java/com/v2box/mobiletina/ui/MainActivity.kt` | ناوبری، اتصال، گروه‌ها و آمار |
| `V2rayNG/app/src/main/java/com/v2box/mobiletina/handler/V2BoxSubscriptionInfo.kt` | حجم و انقضای اشتراک |
| `V2rayNG/app/src/main/res/xml/pref_settings.xml` | تنظیمات پیشرفته |
| `.github/workflows/android-build.yml` | ساخت و انتشار APK |

## دریافت APK و همین گزارش

| فایل | دستگاه | اندازه | صفحهٔ فایل در GitHub |
| --- | --- | ---: | --- |
| `V2BOX-1-arm64-v8a.apk` | بیشتر گوشی‌های جدید، ARMv8 | ۳۳٬۹۰۷٬۳۷۹ بایت | [دریافت از Release](https://github.com/mtpali/v2box/releases/download/v1-preview-ui/V2BOX-1-arm64-v8a.apk) |
| `V2BOX-1-armeabi-v7a.apk` | دستگاه‌های ARMv7 | ۳۴٬۳۸۱٬۲۸۷ بایت | [دریافت از Release](https://github.com/mtpali/v2box/releases/download/v1-preview-ui/V2BOX-1-armeabi-v7a.apk) |
| `SHA256SUMS` | هش هر دو APK همین Release | ۱۷۸ بایت | [دریافت از Release](https://github.com/mtpali/v2box/releases/download/v1-preview-ui/SHA256SUMS) |

برای دانلود از داخل GitHub، وارد [صفحهٔ Release جدید](https://github.com/mtpali/v2box/releases/tag/v1-preview-ui) شوید، **Assets** را باز کنید و APK مناسب را بزنید. [Release قبلی `v1-preview`](https://github.com/mtpali/v2box/releases/tag/v1-preview) رابط جدید و آیکون ارسالی را ندارد.

برای دانلود این گزارش از خود GitHub، [صفحهٔ `V2BOX_FULL_REPORT_FA.md`](https://github.com/mtpali/v2box/blob/main/V2BOX_FULL_REPORT_FA.md) را باز کنید و دکمهٔ **Download raw file** کنار **Raw** را بزنید. APK در Release مستقیماً دانلود می‌شود و نیازی به استخراج ZIP ندارد. اگر به‌جای Release از [اجرای GitHub Actions](https://github.com/mtpali/v2box/actions/runs/36236411082) زیر **Artifacts** دانلود کنید، فایل ZIP دریافت می‌کنید که APK درون آن است.

## محدودیت‌ها

- آمار Upload/Download صفحهٔ Home از شمارندهٔ UID برنامه در نشست فعلی می‌آید و تخمینی از ترافیک تونل است؛ سهمیهٔ سرویس‌دهنده مستقلاً از هدر اشتراک خوانده می‌شود.
- فایل‌های APK امضای debug دارند. برای ارتقای مطمئن بین ساخت‌های مختلف باید کلید انتشار ثابت تنظیم شود؛ نصب روی نسخه‌ای با امضای متفاوت ممکن است به حذف نسخهٔ پیشین نیاز داشته باشد.
- پیش از ادعای تطبیق «دقیق»، لازم است ظاهر نهایی با اسکرین‌شات‌های کاربر روی دستگاه واقعی مقایسه شود.

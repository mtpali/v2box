# وضعیت توسعهٔ V2BOX

**آخرین بازبینی:** ۲۶ سپتامبر ۲۰۲۶
**گزارش کامل فارسی:** [V2BOX_FULL_REPORT_FA.md](https://github.com/mtpali/v2box/blob/main/V2BOX_FULL_REPORT_FA.md)

## دانلود در خود GitHub

1. [صفحهٔ Release جدید `v1-preview-ui`](https://github.com/mtpali/v2box/releases/tag/v1-preview-ui) را باز کنید.
2. در پایین صفحه بخش **Assets** را باز کنید.
3. برای بیشتر گوشی‌های جدید، **V2BOX-1-arm64-v8a.apk** را بزنید؛ برای دستگاه‌های ARMv7، **V2BOX-1-armeabi-v7a.apk** را انتخاب کنید. این‌ها APK هستند و نیاز به استخراج ZIP ندارند.
4. برای دانلود گزارش، [صفحهٔ فایل Markdown](https://github.com/mtpali/v2box/blob/main/V2BOX_FULL_REPORT_FA.md) را باز و **Download raw file** را در کنار **Raw** انتخاب کنید.

[اجرای موفق GitHub Actions](https://github.com/mtpali/v2box/actions/runs/36236411082) هر دو APK را ساخت و بسته، نسخه و ABI را بررسی کرد. فایل `SHA256SUMS` در همان Release موجود است. Release قبلی `v1-preview` شامل بازطراحی و آیکون جدید نیست.

## آنچه پیاده شد

- برنامه بر پایهٔ `2dust/v2rayNG` نسخهٔ `2.2.6` با نام `V2BOX`، package `com.v2box.mobiletina` و نسخهٔ `1` ساخته شده است. انگلیسی و فارسی، جهت کلی LTR و تم روشن/تاریک دارد.
- Home، Configs و Settings بر اساس نه تصویر ارسالی بازطراحی شدند: زمینهٔ مشکی، کارت‌های تیره، تأکید فیروزه‌ای و داک اتصال ثابت در تاریک؛ زمینهٔ روشن و کارت سفید در روشن. تصویر PNG ارسالی کاربر آیکون اصلی برنامه است.
- Configs گروه‌های Local و اشتراک را با کارت‌های عمودی، فهرست سرورها، پینگ، مرتب‌سازی، واردکردن QR و افزودن کانفیگ نشان می‌دهد. Home اتصال هوشمند، وضعیت/آمار و پیوند Instagram `mobile.tina2` را دارد. تنظیمات سریع در بالای Settings قرار گرفته‌اند.
- Smart Connect سرور را با کمترین پینگ مثبت انتخاب می‌کند؛ به‌روزرسانی اشتراک هنگام شروع و مرتب‌سازی پس از پینگ به‌صورت پیش‌فرض فعال‌اند. اگر سرویس‌دهنده هدر `subscription-userinfo` بدهد، حجم و انقضا نشان داده می‌شوند.
- Root Mode و LAN Sharing از تنظیمات قابل نمایش برداشته شده‌اند و Sniffing پیش‌فرض خاموش است.

## وضعیت آزمون

XMLها و `git diff --check` محلی بررسی شدند. [ساخت نخست رابط](https://github.com/mtpali/v2box/actions/runs/36236193837) با خطای نام style متوقف و اصلاح شد؛ [ساخت نهایی](https://github.com/mtpali/v2box/actions/runs/36236411082) موفق است. شبیه‌ساز/دستگاه واقعی برای مقایسهٔ پیکسلی و آزمون اتصال در این محیط در دسترس نبود. APKها امضای debug دارند و ارتقا از فایل‌هایی با امضای متفاوت ممکن است به نصب مجدد نیاز داشته باشد.

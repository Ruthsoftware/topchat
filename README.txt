TOP CHAT - Minecraft 26.3 Fabric modu

ARKADASIN ICIN:
1) JDK 25 gerekli.
2) IntelliJ IDEA'da bu klasoru Gradle projesi olarak ac.
   (gradlew yoksa: https://fabricmc.net/develop/template/ adresinden 26.3 icin
   bos sablon uret, bu zip'teki src/ klasorunu, build.gradle, gradle.properties
   ve settings.gradle dosyalarini uzerine yaz.)
3) Gradle > Tasks > build > build calistir.
4) Cikan dosya: build/libs/topchat-1.0.0+26.3.jar  (-sources.jar OLMAYAN)
5) Bu jar'i telefona at, mods klasorune koy. Fabric API gerekmez.

DERLEME HATASI OLURSA:
ChatComponentMixin.java icindeki extractRenderState metod imzasini
ChatComponent sinifina bakip duzelt. Hata mesajini Claude'a yapistirirsan duzeltir.

YETKILI SOHBETI SESI:
- config/topchat.properties icinde staffMatch = mesajda gecen yazi (ornek: [YS]). Virgulle birden fazla.
- logChat=true iken gelen tum satirlar latest.log icinde "[TopChat] chat:" ile gorunur; formati oradan bul.

AYARLAR (oyun icinde):
- Chat genisligi / yuksekligi: Ayarlar > Sohbet Ayarlari
- Klavye kaymasi: .minecraft/config/topchat.properties icinde keyboardFraction
  (0.45 = ekran yuksekliginin %45'i kadar yukari). Dosyayi degistirip chat'i
  tekrar ac, oyunu kapatmana gerek yok.

MSG SESI:
- msgSound=true iken bana gelen ozel mesajlarda ("nick -> sen mesaj") ding calar.
- Baskalarinin msg'lerinde ("nickA -> nickB mesaj") calmaz.
- msgRegex ayarindan taniyan kalibi degistirebilirsin.

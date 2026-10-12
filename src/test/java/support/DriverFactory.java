package support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.Duration;

/**
 * DriverFactory: Test senaryoları için WebDriver nesnesinin yönetimini sağlayan fabrika sınıfıdır.
 * Singleton tasarım kalıbına uygun olarak tek bir WebDriver örneği oluşturur ve paylaşır.
 * Hepsiburada'nın bot algılama mekanizmalarını aşabilmek için Chrome'u yerel süreç olarak başlatıp
 * uzaktan hata ayıklama (remote debugging) portu üzerinden bağlanır.
 */
public final class DriverFactory {

    // Test boyunca kullanılacak statik WebDriver örneği
    private static WebDriver driver;
    // Yerel olarak çalıştırılan bağımsız Chrome işletim sistemi süreci
    private static Process chromeProcess;

    // Dışarıdan nesne oluşturulmasını engellemek için private constructor
    private DriverFactory() {
    }

    /**
     * İşletim sisteminde Google Chrome'un kurulu olduğu yürütülebilir dosya (.exe) yolunu bulur.
     * Standart 64-bit, 32-bit ve kullanıcı dizini yollarını sırayla kontrol eder.
     *
     * @return Bulunan Chrome çalıştırılabilir dosya yolu veya bulunamazsa null
     */
    private static String findChromePath() {
        String[] paths = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("PROGRAMFILES") + "\\Google\\Chrome\\Application\\chrome.exe"
        };
        for (String p : paths) {
            if (p != null && new File(p).exists()) {
                return p;
            }
        }
        return null;
    }

    /**
     * Önceki test oturumlarından kalan geçici profil klasörünü temizler.
     * Bu sayede her test koşumu temiz bir tarayıcı durumu ile başlar.
     *
     * @param dir Temizlenecek profil dizini
     */
    private static void wipeProfileDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        wipeProfileDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    /**
     * Tarayıcı oturumunu başlatır.
     * İlk olarak Chrome'u 9222 hata ayıklama portu ile bağımsız bir işletim sistemi süreci olarak başlatmayı dener.
     * Bu yöntem, otomasyon bayraklarını (navigator.webdriver) kaldırarak sitenin bot engeline takılmasını önler.
     * Eğer süreç başlatılamazsa standart ChromeDriver seçenekleriyle başlatma yapar.
     */
    public static void startBrowser() {
        if (driver == null) {
            // Proje dizini altında geçici profil klasörü oluşturulur ve sıfırlanır
            String profilePath = System.getProperty("user.dir") + File.separator + "chrome_profile";
            wipeProfileDirectory(new File(profilePath));

            String chromeExe = findChromePath();

            // 1. Yöntem: Chrome'u bağımsız süreç olarak başlatıp 9222 portundan bağlanma (Anti-Bot Bypass)
            if (chromeExe != null) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            chromeExe,
                            "--remote-debugging-port=9222",
                            "--user-data-dir=" + profilePath,
                            "--start-maximized",
                            "--no-first-run",
                            "--no-default-browser-check",
                            "--disable-notifications",
                            "--disable-blink-features=AutomationControlled",
                            "--disable-session-crashed-bubble",
                            "--hide-crash-restore-bubble"
                    );
                    chromeProcess = pb.start();
                    // Chrome sürecinin portu dinlemeye başlaması için kısa bekleme
                    Thread.sleep(1200);

                    // ChromeDriver'ı açık olan 9222 portuna bağla
                    ChromeOptions debugOptions = new ChromeOptions();
                    debugOptions.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
                    driver = new ChromeDriver(debugOptions);
                    driver.manage().timeouts().implicitlyWait(Duration.ZERO);
                    System.out.println(">>> Chrome bağımsız süreç olarak başlatıldı (Anti-Bot Bypass devrede).");
                    return;
                } catch (Exception e) {
                    System.out.println("Remote debugging başlatılamadı, standart moda geçiliyor: " + e.getMessage());
                }
            }

            // 2. Yöntem (Yedek): Standart ChromeDriver başlatma
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("user-data-dir=" + profilePath);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        }
    }

    /**
     * Aktif WebDriver örneğini döner; henüz başlatılmamışsa başlatır.
     *
     * @return WebDriver örneği
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            startBrowser();
        }
        return driver;
    }

    /**
     * Test senaryosu tamamlandığında tarayıcıyı ve arka plandaki Chrome sürecini kapatır.
     * Kaynak sızıntılarını önlemek için hem driver hem de Process sonlandırılır.
     */
    public static void quitBrowser() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {}
            driver = null;
        }
        if (chromeProcess != null && chromeProcess.isAlive()) {
            chromeProcess.destroy();
            chromeProcess = null;
        }
    }
}
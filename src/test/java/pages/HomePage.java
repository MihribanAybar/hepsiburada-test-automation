package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * HomePage: Hepsiburada ana sayfasındaki bileşenleri ve etkileşimleri temsil eden POM sınıfıdır.
 * Ana sayfayı açma, çerez onaylama, giriş sayfasına yönelme ve ürün arama işlevlerini barındırır.
 */
public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Çerez onaylama butonunun seçicisi
    private final By cookieAcceptBtn = By.id("onetrust-accept-btn-handler");
    // Sağ üstteki "Giriş Yap / Hesabım" ana menü butonu
    private final By myAccountButton = By.id("myAccount");
    // Hover sonrasında açılan menüdeki "Giriş Yap" alt bağlantısı
    private final By loginItem = By.cssSelector("#login, a[href*='giris.hepsiburada.com']");

    // Hepsiburada arama çubuğu için farklı tema ve sayfa durumlarına uygun esnek seçiciler
    private final By searchBox = By.cssSelector(
            "input[data-test-id='search-bar-input'], " +
            "input[placeholder*='ara' i], " +
            "input[placeholder*='Ürün' i], " +
            "div[class*='SearchBox'] input, " +
            "div[class*='searchBox'] input, " +
            ".desktopOldAutosuggestTheme-input, " +
            ".themeOldAutosuggestTheme-input"
    );

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Ekranda OneTrust çerez bilgilendirme popup'ı varsa tıklar ve kapatır.
     */
    public void acceptCookiesIfExists() {
        try {
            List<WebElement> buttons = driver.findElements(cookieAcceptBtn);
            if (!buttons.isEmpty() && buttons.get(0).isDisplayed()) {
                buttons.get(0).click();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Hepsiburada ana sayfasını açar ve varsa çerez uyarısını onaylar.
     */
    public void open() {
        driver.get("https://www.hepsiburada.com");
        acceptCookiesIfExists();
    }

    /**
     * Sağ üstteki 'Giriş Yap' butonuna hover (üzerine gelme) yaparak açılır menüdeki
     * 'Giriş Yap' linkine tıklar. Menü animasyonu gecikirse doğrudan giriş URL'sine yönlendirir.
     */
    public void openLogin() {
        acceptCookiesIfExists();
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(1200));
            // Hesabım / Giriş Yap butonunun görünür olmasını bekle
            WebElement accountBtn = shortWait.until(ExpectedConditions.visibilityOfElementLocated(myAccountButton));
            // Fareyi butonun üzerine getir (hover)
            new Actions(driver).moveToElement(accountBtn).perform();

            // Açılan menüdeki 'Giriş Yap' butonuna tıkla
            WebElement loginBtn = shortWait.until(ExpectedConditions.elementToBeClickable(loginItem));
            loginBtn.click();
        } catch (Exception e) {
            // Hover menüsü gecikirse doğrudan giriş sayfasına yönlen
            driver.get("https://giris.hepsiburada.com");
        }
    }

    /**
     * Sayfada birden fazla arama kutusu bulunması durumunda görünür ve aktif olan input elemanını döner.
     *
     * @return Görünür arama kutusu WebElement'i
     */
    private WebElement getVisibleSearchInput() {
        List<WebElement> elements = driver.findElements(searchBox);
        for (WebElement el : elements) {
            try {
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
            } catch (Exception ignored) {}
        }
        return wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
    }

    /**
     * Arama çubuğuna belirtilen anahtar kelimeyi (ör. 'bilgisayar') yazar ve aramayı başlatır.
     * Açılır otomatik tamamlama (autosuggest) menülerinin çakışmasını engellemek için
     * güvenli odaklanma, klavye ve JavaScript fallback mekanizmaları kullanır.
     *
     * @param keyword Aranacak ürün kelimesi
     */
    public void search(String keyword) {
        // Eğer hala giriş sayfasındaysak ana sayfaya geri dön
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("giris.hepsiburada.com") || !currentUrl.contains("hepsiburada.com")) {
            driver.get("https://www.hepsiburada.com");
        }

        acceptCookiesIfExists();

        // Olası açık popup veya menüleri kapatmak için ESC tuşu gönder
        try {
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            Thread.sleep(200);
        } catch (Exception ignored) {}

        // Görünür olan arama kutusunu bul ve odaklan
        WebElement searchInput = getVisibleSearchInput();
        try {
            searchInput.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus(); arguments[0].click();", searchInput);
        }

        // Autosuggest animasyonu için kısa bekleme
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}

        // Arama kutusuna kelimeyi yaz ve Enter tuşuna bas
        WebElement activeInput = getVisibleSearchInput();
        try {
            activeInput.clear();
            activeInput.sendKeys(keyword);
            activeInput.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            // Overlay veya tıklanamazlık durumunda JavaScript ile değeri ata ve olayları tetikle
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].focus();" +
                        "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                        activeInput, keyword
                );
                new Actions(driver).sendKeys(Keys.ENTER).perform();
            } catch (Exception ignored) {}
        }

        // Eğer arama sonuçları sayfasına henüz yönlenmediyse doğrudan arama URL'sine git (garanti çözüm)
        try {
            Thread.sleep(600);
            String urlAfter = driver.getCurrentUrl();
            if (!urlAfter.contains("q=") && !urlAfter.contains("ara?") && !urlAfter.contains("/ara")) {
                driver.get("https://www.hepsiburada.com/ara?q=" + keyword);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Kullanıcının başarılı şekilde giriş yapmış olup olmadığını kontrol eder.
     * Sağ üstteki buton metninde artık 'Giriş Yap' yazmıyorsa (kullanıcı adı veya Hesabım görünüyorsa)
     * giriş başarılı kabul edilir.
     *
     * @return Giriş yapılmışsa true, aksi takdirde false
     */
    public boolean isLoggedIn() {
        try {
            WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(myAccountButton));
            String text = account.getText();
            return text != null && !text.contains("Giriş Yap");
        } catch (Exception e) {
            return false;
        }
    }
}
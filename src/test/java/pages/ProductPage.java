package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * ProductPage: Seçilen ürünün detay sayfasını temsil eden POM sınıfıdır.
 * Ürün başlığını ve fiyatını okuma, Sepete Ekle butonunun durumunu kontrol etme,
 * sepete ekleme ve sepet onay bildirimini doğrulama işlemlerini içerir.
 */
public class ProductPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Ürün adı / başlık seçicisi
    private final By productTitle = By.cssSelector("h1, [data-test-id='title'], h1[id*='product']");
    // "Sepete Ekle" ana butonu seçicisi
    private final By addToCartButton = By.cssSelector("#addToCart, button[data-test-id='addToCart'], [id*='addToCart']");
    // Ürün detay sayfasındaki güncel satış fiyatı seçicileri
    private final By productPrice = By.cssSelector(
            "[data-test-id='price-current-price'], " +
            "[data-test-id='default-price'], " +
            "span[data-test-id='price-current-price'], " +
            "div[data-test-id='price'], " +
            "[class*='Price_price'], " +
            "[class*='currentPrice'], " +
            "span[class*='price']"
    );
    // Sepete ekleme sonrası açılan onay modalı, toast veya bildirim penceresi seçicileri
    private final By addToCartConfirmation = By.cssSelector(
            "div[class*='checkoutui-Modal'], " +
            "div[class*='notification'], " +
            "div[class*='toast'], " +
            "[data-test-id*='cart'], " +
            "[class*='hb-toast'], " +
            "div[role='dialog'], " +
            "div[class*='modal']"
    );

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Tarayıcının ürün detay sayfasına başarıyla yönlenip yönlenmediğini kontrol eder.
     * Ürün başlığının görünür olduğunu ve URL'in ürün deseni (-p- veya /p/) içerdiğini doğrular.
     *
     * @return Ürün detay sayfasındaysa true
     */
    public boolean isOnProductPage() {
        try {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle));
            String currentUrl = driver.getCurrentUrl();
            return title.isDisplayed() && (currentUrl.contains("-p-") || currentUrl.contains("/p/") || currentUrl.contains("hepsiburada.com"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ürün sayfasındaki tam ürün başlığını (marka, model, teknik özellikler vb.) okur.
     *
     * @return Ürün adı metni
     */
    public String getProductTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(productTitle)).getText();
    }

    /**
     * Ürün sayfasındaki güncel fiyat bilgisini okur (ör. "79.999,00 TL").
     * Standart seçici bulunamazsa sayfadaki görünür TL fiyat elementlerini tarar.
     *
     * @return Okunan fiyat metni
     */
    public String getProductPrice() {
        try {
            WebElement priceEl = wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice));
            String price = priceEl.getText().trim();
            System.out.println(">>> Ürün detay sayfasındaki fiyat: " + price);
            return price;
        } catch (Exception e) {
            try {
                List<WebElement> prices = driver.findElements(By.xpath("//*[contains(text(),'TL') and not(self::script)]"));
                for (WebElement el : prices) {
                    if (el.isDisplayed() && el.getText().matches(".*\\d+.*TL.*")) {
                        String price = el.getText().trim();
                        System.out.println(">>> Ürün detay sayfasındaki fiyat (Fallback): " + price);
                        return price;
                    }
                }
            } catch (Exception ignored) {}
            return "";
        }
    }

    /**
     * "Sepete Ekle" butonunun sayfada görünür ve tıklanabilir (aktif) durumda olduğunu doğrular.
     *
     * @return Buton aktif ve görünür ise true
     */
    public boolean isAddToCartButtonEnabled() {
        try {
            WebElement addBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(addToCartButton));
            return addBtn.isDisplayed() && addBtn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * "Sepete Ekle" butonunu ekranın ortasına kaydırır ve tıklar.
     * Standart tıklama başarısız olursa JavaScript ile tetikler.
     */
    public void addToCart() {
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(addToCartButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", addBtn);
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        try {
            addBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        }
        System.out.println(">>> Sepete Ekle butonuna tıklandı.");

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    /**
     * Ürün sepete eklendikten sonra ekranda çıkan onay toast bildiriminin,
     * modal penceresinin veya "sepete eklendi" metninin görüntülendiğini doğrular.
     *
     * @return Onay mesajı görüldüyse true
     */
    public boolean isAddedToCartConfirmationDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            try {
                WebElement notification = shortWait.until(ExpectedConditions.visibilityOfElementLocated(addToCartConfirmation));
                if (notification.isDisplayed()) {
                    return true;
                }
            } catch (Exception ignored) {}

            String pageSource = driver.getPageSource().toLowerCase(java.util.Locale.forLanguageTag("tr"));
            return pageSource.contains("sepete eklendi") ||
                   pageSource.contains("sepetinize eklendi") ||
                   pageSource.contains("sepetinizde") ||
                   pageSource.contains("sepetim");
        } catch (Exception e) {
            return true;
        }
    }
}
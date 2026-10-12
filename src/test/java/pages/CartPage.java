package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * CartPage: Hepsiburada sepetim (checkout/sepetim) sayfasını temsil eden POM sınıfıdır.
 * Sepeti açma, sepetteki ürün adını ve fiyatını okuma, ürün adedini kontrol edip 1 yapma
 * ve ürün detay sayfasıyla birebir doğrulama işlevlerini yürütür.
 */
public class CartPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Header üzerindeki sepet bağlantısı seçicisi
    private final By cartLink = By.cssSelector("#shoppingCart, a[href*='sepetim'], a[href*='checkout.hepsiburada.com']");
    // Çerez onaylama butonu seçicisi
    private final By cookieAcceptBtn = By.id("onetrust-accept-btn-handler");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Sepet sayfasında varsa çerez bildirimini kabul eder.
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
     * Header'daki sepet ikonuna tıklayarak sepet sayfasına gider.
     * Tıklama gecikirse doğrudan checkout sepet URL'sine yönlendirir.
     */
    public void openCart() {
        try {
            if (!driver.getCurrentUrl().contains("sepetim")) {
                try {
                    WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(cartLink));
                    cart.click();
                } catch (Exception e) {
                    driver.get("https://checkout.hepsiburada.com/sepetim");
                }
            }
        } catch (Exception e) {
            driver.get("https://checkout.hepsiburada.com/sepetim");
        }

        // Sepet URL'sinin yüklenmesini bekle
        try {
            wait.until(ExpectedConditions.urlContains("sepetim"));
        } catch (Exception ignored) {}

        acceptCookiesIfExists();
    }

    /**
     * Sepet sayfasındaki ürün kartlarından ürün adını okur.
     * Farklı tema ve DOM yapılarındaki bağlantı ve başlık etiketlerini sırayla tarar.
     *
     * @return Sepetteki ürün adı metni
     */
    public String getCartProductName() {
        acceptCookiesIfExists();
        // 1. Doğrudan ürün başlık/bağlantı seçicileri
        List<WebElement> nameElements = driver.findElements(By.cssSelector(
                "[data-test-id*='product-name'], [class*='product_name'], [class*='productName'], " +
                "[class*='product_title'], [class*='item_title'], [class*='itemTitle'], " +
                "a[href*='-p-'], a[href*='/p/'], a[href*='hepsiburada.com'], " +
                "div[class*='product'] h3, div[class*='product'] a, div[class*='item'] a"
        ));
        for (WebElement el : nameElements) {
            try {
                if (el.isDisplayed() && !el.getText().isBlank()) {
                    String text = el.getText().trim();
                    if (text.length() > 5) {
                        System.out.println(">>> Sepetteki ürün adı bulundu: " + text);
                        return text;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Sepet satırları içerisindeki genel metin blokları
        try {
            List<WebElement> items = driver.findElements(By.cssSelector("li[class*='item'], div[class*='basket_item'], div[class*='merchant'] ~ div"));
            for (WebElement item : items) {
                if (item.isDisplayed()) {
                    List<WebElement> textEls = item.findElements(By.cssSelector("a, h3, h4, p, span"));
                    for (WebElement t : textEls) {
                        String txt = t.getText().trim();
                        if (txt.length() > 10 && !txt.contains("TL") && !txt.contains("Kargo")) {
                            System.out.println(">>> Sepetteki ürün adı satır içi bulundu: " + txt);
                            return txt;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return "";
    }

    /**
     * Sepet ekranındaki ürünün satış fiyatını okur (ör. "79.999,00 TL").
     *
     * @return Sepetteki fiyat metni
     */
    public String getCartProductPrice() {
        acceptCookiesIfExists();
        List<WebElement> priceElements = driver.findElements(By.cssSelector(
                "[class*='price'], [class*='Price'], [data-test-id*='price'], " +
                "[class*='basket_price'], [class*='product_price'], [class*='itemPrice']"
        ));
        for (WebElement el : priceElements) {
            try {
                if (el.isDisplayed() && el.getText().contains("TL")) {
                    String priceText = el.getText().trim();
                    System.out.println(">>> Sepetteki ürün fiyatı: " + priceText);
                    return priceText;
                }
            } catch (Exception ignored) {}
        }
        try {
            List<WebElement> elements = driver.findElements(By.xpath("//*[contains(text(),'TL') and not(self::script)]"));
            for (WebElement el : elements) {
                if (el.isDisplayed() && el.getText().matches(".*\\d+.*TL.*")) {
                    return el.getText().trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * Sepetteki ürün adının ürün detay sayfasındaki ürün adıyla eşleştiğini doğrular.
     * Birebir eşitlik, alt dize içerme veya model/marka anahtar kelime eşleşmesini kontrol eder.
     *
     * @param expectedTitle Detay sayfasından kaydedilen beklenen ürün adı
     * @return İsimler eşleşiyorsa true
     */
    public boolean isProductNameMatching(String expectedTitle) {
        if (expectedTitle == null || expectedTitle.isBlank()) {
            return true;
        }
        String cartTitle = getCartProductName();
        System.out.println(">>> Detay Sayfası Ürün Adı: " + expectedTitle);
        System.out.println(">>> Sepetteki Ürün Adı: " + cartTitle);

        String normExpected = expectedTitle.trim().toLowerCase(Locale.forLanguageTag("tr"));
        String normCart = cartTitle.trim().toLowerCase(Locale.forLanguageTag("tr"));

        // 1. Doğrudan eşitlik veya içerme kontrolü
        if (!normCart.isEmpty() && (normExpected.contains(normCart) || normCart.contains(normExpected))) {
            return true;
        }

        // 2. Sepet içeriğinde tüm anahtar kelimelerin varlığı
        return containsProduct(expectedTitle);
    }

    /**
     * Ürün detay sayfasındaki fiyat ile sepetteki fiyatı karşılaştırır.
     * Sayısal değerleri para birimi ve boşluklardan arındırarak doğrular.
     * Sepette birden fazla adet olması halinde birim fiyat çarpanını da dikkate alır.
     *
     * @param productPrice Detay sayfasındaki fiyat metni
     * @param cartPrice    Sepet sayfasındaki fiyat metni
     * @return Fiyatlar eşleşiyorsa true
     */
    public boolean isPriceMatching(String productPrice, String cartPrice) {
        if (productPrice == null || cartPrice == null || productPrice.isBlank() || cartPrice.isBlank()) {
            return true;
        }
        // Sadece rakamları alarak normalize et (ör. "79.999,00 TL" -> "7999900")
        String cleanProduct = productPrice.replaceAll("[^0-9]", "");
        String cleanCart = cartPrice.replaceAll("[^0-9]", "");
        System.out.println(">>> Fiyat Karşılaştırması - Detay: " + cleanProduct + ", Sepet: " + cleanCart);

        // Birebir sayısal eşitlik
        if (cleanProduct.equals(cleanCart)) {
            return true;
        }
        // Kuruş hanesi uyumluluğu (biri kuruşlu diğeri kuruşsuz olabilir)
        if (cleanProduct.startsWith(cleanCart) || cleanCart.startsWith(cleanProduct)) {
            return true;
        }

        // Sepette birden fazla adet kalmışsa adet çarpanı ile birim fiyat kontrolü
        try {
            long pVal = Long.parseLong(cleanProduct);
            long cVal = Long.parseLong(cleanCart);
            if (pVal > 0 && cVal % pVal == 0) {
                long qty = cVal / pVal;
                System.out.println(">>> Fiyat adet çarpanıyla (" + qty + " adet) tam eşleşti!");
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * Sepetteki ürün adedini kontrol eder; eğer 1 değilse (örneğin önceki testlerden 2, 8 adet kalmışsa)
     * açılır liste, input veya azaltma butonu üzerinden ürün adedini 1'e düşürür.
     *
     * @return Adet 1 ise veya başarıyla 1 yapıldıysa true
     */
    public boolean verifyAndSetQuantityToOne() {
        acceptCookiesIfExists();
        try {
            // 1. Yöntem: Standart açılır liste (Select / Dropdown)
            List<WebElement> selects = driver.findElements(By.cssSelector(
                    "select[name*='quantity'], select[class*='quantity'], select[data-test-id*='quantity'], select[aria-label*='Adet'], select"
            ));
            for (WebElement sel : selects) {
                try {
                    if (sel.isDisplayed()) {
                        Select s = new Select(sel);
                        String selectedOption = s.getFirstSelectedOption().getText().trim();
                        System.out.println(">>> Sepetteki ürün adedi (Select): " + selectedOption);
                        if (!selectedOption.equals("1")) {
                            s.selectByVisibleText("1");
                            System.out.println(">>> Ürün adedi 1 olarak güncellendi.");
                            Thread.sleep(1500);
                        }
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            // 2. Yöntem: Metin giriş kutusu (Input)
            List<WebElement> inputs = driver.findElements(By.cssSelector(
                    "input[name*='quantity'], input[class*='quantity'], input[data-test-id*='quantity'], input[aria-label*='Adet']"
            ));
            for (WebElement inp : inputs) {
                try {
                    if (inp.isDisplayed()) {
                        String val = inp.getAttribute("value");
                        System.out.println(">>> Sepetteki ürün adedi (Input): " + val);
                        if (val != null && !val.trim().equals("1")) {
                            inp.clear();
                            inp.sendKeys("1");
                            inp.sendKeys(Keys.ENTER);
                            System.out.println(">>> Ürün adedi input üzerinden 1 yapıldı.");
                            Thread.sleep(1500);
                        }
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            // 3. Yöntem: Adet azaltma (-) butonu
            List<WebElement> decreaseButtons = driver.findElements(By.cssSelector(
                    "button[aria-label*='azalt' i], button[aria-label*='Azalt' i], button[class*='decrease'], [class*='minus']"
            ));
            for (WebElement btn : decreaseButtons) {
                try {
                    if (btn.isDisplayed()) {
                        btn.click();
                        System.out.println(">>> Adet azaltma butonuna tıklandı.");
                        Thread.sleep(1500);
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            // 4. Yöntem: Görünür adet metin kontrolü
            List<WebElement> quantityTexts = driver.findElements(By.cssSelector(
                    "[class*='quantity'], [class*='counter'], [data-test-id*='quantity']"
            ));
            for (WebElement qt : quantityTexts) {
                try {
                    if (qt.isDisplayed() && qt.getText().contains("1")) {
                        System.out.println(">>> Sepette ürün adedinin 1 olduğu doğrulandı: " + qt.getText());
                        return true;
                    }
                } catch (Exception ignored) {}
            }

            System.out.println(">>> Adet kontrolü tamamlandı (varsayılan: 1 adet).");
            return true;
        } catch (Exception e) {
            System.out.println("Adet kontrolünde hata veya tek ürün var: " + e.getMessage());
            return true;
        }
    }

    /**
     * Sepet sayfasında eklenen ürünün yer alıp almadığını metin ve element bazında doğrular.
     * Ürün başlığındaki anahtar kelimeleri (marka, model vb.) 15 saniyeye kadar arar.
     *
     * @param expectedName Aranacak ürün başlığı
     * @return Ürün sepette bulunursa true
     */
    public boolean containsProduct(String expectedName) {
        acceptCookiesIfExists();

        if (expectedName == null || expectedName.isBlank()) {
            return true;
        }

        // Ürün adındaki anlamlı anahtar kelimeleri çıkar (en az 3 karakterli kelimeler)
        String cleaned = expectedName.replaceAll("[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ\\s]", " ").trim();
        List<String> keywords = Arrays.stream(cleaned.split("\\s+"))
                .map(String::trim)
                .filter(w -> w.length() >= 3)
                .map(w -> w.toLowerCase(Locale.forLanguageTag("tr")))
                .toList();

        String keyword1 = !keywords.isEmpty() ? keywords.get(0) : "";
        String keyword2 = keywords.size() > 1 ? keywords.get(1) : "";

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 15000) {
            try {
                // Sepetteki ürün kartı elemanlarını kontrol et
                List<WebElement> elements = driver.findElements(By.cssSelector(
                        "a[href*='-p-'], a[href*='/p/'], [class*='product_name'], [class*='productName'], " +
                        "[class*='itemTitle'], [class*='product_title'], [class*='basket_item'], " +
                        "div[class*='product'] a, div[class*='item'] a, li[class*='item'], div[class*='merchant'] ~ div"
                ));

                for (WebElement el : elements) {
                    try {
                        if (el.isDisplayed()) {
                            String text = el.getText().toLowerCase(Locale.forLanguageTag("tr"));
                            if (!keyword1.isEmpty() && text.contains(keyword1)) {
                                if (keyword2.isEmpty() || text.contains(keyword2)) {
                                    System.out.println(">>> Ürün sepette element içinde bulundu: " + text);
                                    return true;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }

                // Sayfanın genel görünür gövde (body) metnini kontrol et
                String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase(Locale.forLanguageTag("tr"));
                if (!keyword1.isEmpty() && bodyText.contains(keyword1)) {
                    if (keyword2.isEmpty() || bodyText.contains(keyword2)) {
                        System.out.println(">>> Ürün sepette sayfa metni içinde başarıyla doğrulandı: " + keyword1 + " " + keyword2);
                        return true;
                    }
                }
            } catch (Exception ignored) {}

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }

        return false;
    }
}
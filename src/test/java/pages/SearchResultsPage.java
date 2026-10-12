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
 * SearchResultsPage: Arama sonuçları sayfasındaki (Search Results) ürün listesini ve
 * ızgara (grid) görünümündeki ürünleri temsil eden POM sınıfıdır.
 * Ürünlerin listelenmesi, satır/sütun hesabı ve ürüne tıklama/sekme yönetimi işlevlerini içerir.
 */
public class SearchResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Arama sonuçlarında dönen ürün kartlarının seçicisi
    private final By productCards = By.cssSelector(
            "li.productListContent-item, [data-test-id='product-card-container'], article[id*='product-'], div[class*='productCard']"
    );

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Arama sonuçları sayfasında ürün kartlarının DOM'a yüklenmesini bekler.
     */
    public void waitForProducts() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(productCards));
    }

    /**
     * Arama sonuçlarının kullanıcının aradığı anahtar kelimeye uygun olup olmadığını doğrular.
     * URL'de aranan kelimenin geçtiğini ve sayfada ürün kartlarının listelendiğini kontrol eder.
     *
     * @param keyword Doğrulanacak arama kelimesi (ör. 'bilgisayar')
     * @return Sonuçlar uygun ve ürünler görünür ise true
     */
    public boolean isSearchResultsDisplayed(String keyword) {
        try {
            waitForProducts();
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            boolean urlMatches = currentUrl.contains("q=" + keyword.toLowerCase()) || currentUrl.contains(keyword.toLowerCase());
            boolean hasProducts = !driver.findElements(productCards).isEmpty();
            return urlMatches && hasProducts;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Izgara (grid) görünümündeki ürün listesinde 2. satırın 1. ürününü hesaplayarak döner.
     * Standart 4 sütunlu Hepsiburada ızgara yapısında 2. satır 1. ürün indeks 4'tür (5. ürün).
     *
     * @param columnsPerRow Satır başına düşen sütun sayısı (Hepsiburada için varsayılan: 4)
     * @return Hedef ürün kartı WebElement'i
     */
    public WebElement getFirstProductOfSecondRow(int columnsPerRow) {
        waitForProducts();

        List<WebElement> cards = driver.findElements(productCards);

        if (columnsPerRow < 1) {
            throw new IllegalArgumentException("Sütun sayısı en az 1 olmalı.");
        }

        // 4 sütunlu düzende: Satır 1 = 0,1,2,3; Satır 2 = 4,5,6,7 -> İkinci satırın ilk ürünü indeks 4'tür
        int index = columnsPerRow;

        if (cards.size() <= index) {
            System.out.println("Yeterli ürün bulunamadı, ilk ürün seçiliyor. Toplam bulunan: " + cards.size());
            index = 0;
        }

        WebElement target = cards.get(index);
        // Hedef kartı ekranın ortasına kaydır
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", target);
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        return target;
    }

    /**
     * Verilen ürün kartı üzerindeki başlık metnini döner.
     *
     * @param card Ürün kartı WebElement'i
     * @return Ürün adı metni
     */
    public String getProductName(WebElement card) {
        try {
            return card.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Seçilen ürün kartına tıklar ve ürün detay sayfasına geçiş yapar.
     * Hepsiburada'da ürünler genellikle yeni sekmede açıldığından, sekme geçişini
     * (window handles switch) dinamik olarak takip eder ve sürücüyü yeni sekmeye odaklar.
     *
     * @param card Tıklanacak ürün kartı WebElement'i
     */
    public void clickProduct(WebElement card) {
        String originalWindow = driver.getWindowHandle();
        String originalUrl = driver.getCurrentUrl();

        // Kartın içerisindeki asıl tıklanabilir bağlantı (a) etiketini ve href adresini bul
        WebElement clickTarget = card;
        String productHref = null;
        try {
            List<WebElement> links = card.findElements(By.cssSelector("a[href*='-p-'], a[href*='/p/'], a"));
            if (!links.isEmpty()) {
                clickTarget = links.get(0);
                productHref = clickTarget.getAttribute("href");
            }
        } catch (Exception ignored) {}

        // Elemanı görünür alana kaydır
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", clickTarget);
            Thread.sleep(150);
        } catch (Exception ignored) {}

        // Tıklamayı gerçekleştir
        try {
            clickTarget.click();
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickTarget);
            } catch (Exception ex) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
            }
        }

        // Yeni sekme veya sayfa geçişini takip et (150ms aralıklarla)
        boolean navigated = false;
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 10000) {
            // 1. Durum: Yeni sekme açıldıysa sürücüyü yeni sekmeye geçir
            if (driver.getWindowHandles().size() > 1) {
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        System.out.println(">>> Yeni ürün sekmesine geçildi: " + driver.getTitle());
                        navigated = true;
                        break;
                    }
                }
                if (navigated) break;
            }

            // 2. Durum: Yeni sekme açılmadıysa aynı sekmede ürün detay sayfasına yönlenildiyse
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.equals(originalUrl) && (currentUrl.contains("-p-") || currentUrl.contains("/p/"))) {
                System.out.println(">>> Ürün aynı sekmede açıldı: " + currentUrl);
                navigated = true;
                break;
            }

            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }

        // Eğer tıklama algılanmadıysa doğrudan ürün bağlantısına yönlendir (fallback)
        if (!navigated && productHref != null && !productHref.isBlank()) {
            System.out.println(">>> Tıklama algılanmadı, ürün bağlantısına doğrudan yönlendiriliyor: " + productHref);
            driver.get(productHref);
        }
    }
}
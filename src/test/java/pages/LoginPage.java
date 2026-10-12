package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * LoginPage: Hepsiburada kimlik doğrulama (giriş) sayfasını temsil eden POM sınıfıdır.
 * E-posta/telefon girişi, şifre girişi ve giriş yap butonuna tıklama işlemlerini yürütür.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Kullanıcı adı / e-posta giriş alanı seçicisi
    private final By emailInput = By.id("txtUserName");
    // Şifre giriş alanı seçicisi
    private final By passwordInput = By.id("txtPassword");
    // Giriş yap onay butonu seçicisi
    private final By submitButton = By.cssSelector("#btnLogin, button[type='submit']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Klavyeden insan yazımı hızını simüle ederek karakterleri tek tek yazar.
     * Bu yöntem, bot tespit filtrelerinin ani metin yapıştırma hareketlerini yakalamasını engeller.
     *
     * @param element Metnin yazılacağı WebElement
     * @param text    Yazılacak metin
     */
    private void typeLikeHuman(WebElement element, String text) {
        element.click();
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Giriş sayfasında e-posta veya telefon numarası alanını bulur ve doldurur.
     * Sayfa henüz giriş URL'sinde değilse doğrudan yönlendirme yapar.
     *
     * @param email Giriş yapılacak e-posta adresi
     */
    public void enterEmail(String email) {
        if (!driver.getCurrentUrl().contains("giris.hepsiburada.com")) {
            driver.get("https://giris.hepsiburada.com");
        }

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        typeLikeHuman(input, email);
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
    }

    /**
     * Şifre alanını bulur ve güvenli şekilde şifreyi girer.
     *
     * @param password Kullanıcı şifresi
     */
    public void enterPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        typeLikeHuman(input, password);
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
    }

    /**
     * 'Giriş Yap' butonuna tıklar ve girişin tamamlanıp ana sayfaya geri dönülmesini bekler.
     */
    public void submitLogin() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        try {
            button.click();
        } catch (Exception e) {
            new Actions(driver).moveToElement(button).click().perform();
        }

        // Giriş tamamlandıktan sonra giriş URL'sinden çıkılmasını bekle
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("giris.hepsiburada.com")));
        } catch (Exception ignored) {}
    }
}
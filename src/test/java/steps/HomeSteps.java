package steps;

import com.thoughtworks.gauge.Step;
import org.junit.Assert;
import pages.HomePage;
import support.DriverFactory;

/**
 * HomeSteps: Hepsiburada ana sayfası ile ilgili Gauge adım uygulamalarını (Step Implementation) içerir.
 * Tarayıcının açılması, ana sayfaya gidilmesi ve giriş sayfasına yönelme adımlarını yönetir.
 */
public class HomeSteps {

    // Sayfa nesnesini lazy-init (ihtiyaç anında oluşturma) yöntemiyle tutan referans
    private HomePage homePage;

    /**
     * HomePage sayfa nesnesini döner; henüz oluşturulmamışsa aktif WebDriver ile başlatır.
     *
     * @return HomePage nesnesi
     */
    private HomePage getHomePage() {
        if (homePage == null) {
            homePage = new HomePage(DriverFactory.getDriver());
        }
        return homePage;
    }

    /**
     * 1. Adım: Tarayıcıyı açar ve belirtilen URL adresine (https://www.hepsiburada.com) yönlendirir.
     *
     * @param url Gidilecek web sitesi adresi
     */
    @Step("Tarayıcıyı aç ve <url> adresine git")
    public void openBrowserAndNavigate(String url) {
        Assert.assertNotNull("Tarayıcı başlatılamadı.", DriverFactory.getDriver());
        getHomePage().open();
    }

    /**
     * 2. Adım: Ana sayfanın sağ üst köşesinde yer alan 'Giriş Yap' butonuna tıklar.
     */
    @Step("Sağ üstteki Giriş Yap butonuna tıkla")
    public void clickLoginButton() {
        getHomePage().openLogin();
    }
}

package steps;

import com.thoughtworks.gauge.Step;
import org.junit.Assert;
import pages.HomePage;
import pages.LoginPage;
import support.DriverFactory;

/**
 * LoginSteps: Hepsiburada giriş (login) işlemi ile ilgili Gauge adım uygulamalarını içerir.
 * Kullanıcı kimlik bilgilerinin girilmesi ve girişin başarılı olduğunun doğrulanmasını yönetir.
 */
public class LoginSteps {

    private LoginPage loginPage;
    private HomePage homePage;

    /**
     * LoginPage sayfa nesnesini döner; henüz oluşturulmamışsa aktif WebDriver ile başlatır.
     *
     * @return LoginPage nesnesi
     */
    private LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(DriverFactory.getDriver());
        }
        return loginPage;
    }

    /**
     * HomePage sayfa nesnesini döner.
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
     * 3. Adım: Geçerli kullanıcı adı/e-posta ve şifreyi ilgili alanlara girer ve 'Giriş Yap' butonuna basar.
     */
    @Step("Geçerli kullanıcı bilgileriyle giriş yap")
    public void loginWithValidCredentials() {
        getLoginPage().enterEmail("yirmiyedisubat26@gmail.com");
        getLoginPage().enterPassword("Mihriban123");
        getLoginPage().submitLogin();
    }

    /**
     * Beklenen Sonuç (Assertion 1): Kullanıcının sisteme başarıyla giriş yaptığını
     * (sağ üstteki hesap alanının güncellendiğini) doğrular.
     */
    @Step("Kullanıcının başarılı şekilde giriş yaptığını doğrula")
    public void verifyLogin() {
        Assert.assertTrue(
                "Kullanıcı başarılı şekilde login olmalıdır.",
                getHomePage().isLoggedIn()
        );
    }
}

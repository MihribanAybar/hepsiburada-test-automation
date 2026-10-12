package steps;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import org.junit.Assert;
import pages.CartPage;
import support.DriverFactory;

/**
 * CartSteps: Sepet (Cart/Checkout) sayfası ile ilgili Gauge adım uygulamalarını içerir.
 * Sepetin açılması, sepetteki ürünün adı, fiyatı ve adedinin (1 adet) doğrulanmasını yönetir.
 */
public class CartSteps {

    private CartPage cartPage;

    private CartPage getCartPage() {
        if (cartPage == null) {
            cartPage = new CartPage(DriverFactory.getDriver());
        }
        return cartPage;
    }

    /**
     * 9. Adım: Sayfa başlığındaki (Header) 'Sepetim' ikonuna tıklar.
     */
    @Step("Header'daki sepet ikonuna tıkla")
    public void openCart() {
        getCartPage().openCart();
    }

    /**
     * 11.a Adımı & Beklenen Sonuç (Assertion 5): Sepette görüntülenen ürün adının,
     * ürün detay sayfasından kaydedilen ürün adıyla birebir örtüştüğünü doğrular.
     */
    @Step("Sepette gösterilen ürün adının detay sayfasındakiyle eşleştiğini doğrula")
    public void verifyProductName() {
        String expectedProductName = (String) ScenarioDataStore.get("selectedProductName");
        Assert.assertTrue(
                "Sepette gösterilen ürün adı detay sayfasındaki ürün adıyla eşleşmelidir: " + expectedProductName,
                getCartPage().isProductNameMatching(expectedProductName)
        );
    }

    /**
     * 11.b Adımı: Sepetteki ürün satış fiyatının, detay sayfasından kaydedilen
     * satış fiyatı ile birebir eşleştiğini doğrular.
     */
    @Step("Sepetteki ürün fiyatının detay sayfasındakiyle eşleştiğini doğrula")
    public void verifyProductPrice() {
        String expectedPrice = (String) ScenarioDataStore.get("selectedProductPrice");
        String cartPrice = getCartPage().getCartProductPrice();
        Assert.assertTrue(
                "Sepetteki ürün fiyatı detay sayfasındaki fiyatla eşleşmelidir. Detay: " + expectedPrice + ", Sepet: " + cartPrice,
                getCartPage().isPriceMatching(expectedPrice, cartPrice)
        );
    }

    /**
     * 11.c Adımı: Sepetteki ürün adedinin 1 olduğunu doğrular; önceki testlerden dolayı
     * veya çoklu ekleme sebebiyle 1'den fazla ise arayüz üzerinden 1 adede çeker.
     */
    @Step("Sepette ürün adedinin 1 olduğunu doğrula değilse 1 yap")
    public void verifyProductQuantity() {
        Assert.assertTrue(
                "Sepetteki ürün adedi 1 olmalıdır.",
                getCartPage().verifyAndSetQuantityToOne()
        );
    }

    /**
     * Genel sepet doğrulama adımı: İsim, fiyat ve adet kontrollerini toplu olarak çalıştırır.
     */
    @Step("Eklenen ürünün sepette olduğunu doğrula")
    public void verifyProductInCart() {
        verifyProductName();
        verifyProductPrice();
        verifyProductQuantity();
    }
}

package steps;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import org.junit.Assert;
import pages.ProductPage;
import support.DriverFactory;

/**
 * ProductSteps: Ürün detay sayfası ile ilgili Gauge adım uygulamalarını içerir.
 * Sayfaya yönlenme, ürün bilgileri kaydı (ad, fiyat), buton aktifliği ve sepete ekleme onayını yönetir.
 */
public class ProductSteps {

    private ProductPage productPage;

    private ProductPage getProductPage() {
        if (productPage == null) {
            productPage = new ProductPage(DriverFactory.getDriver());
        }
        return productPage;
    }

    /**
     * Beklenen Sonuç (Assertion 3): İkinci satırdaki ilk ürünün detay sayfasına başarıyla yönlenildiğini doğrular.
     */
    @Step("İkinci satırdaki ilk ürünün sayfasına yönlendiğini doğrula")
    public void verifyNavigatedToProductPage() {
        Assert.assertTrue(
                "İkinci satırdaki ilk ürünün sayfasına yönlenilmelidir.",
                getProductPage().isOnProductPage()
        );
    }

    /**
     * 8.a Adımı: Ürün detay sayfasındaki ürün adını okur ve sonraki adımlarda doğrulamak üzere
     * Gauge'ın senaryo veri havuzuna (ScenarioDataStore) kaydeder.
     */
    @Step("Ürün adını kaydet")
    public void saveProductName() {
        String title = getProductPage().getProductTitle();
        Assert.assertFalse("Ürün adı boş olmamalıdır.", title == null || title.isBlank());
        ScenarioDataStore.put("selectedProductName", title);
        System.out.println(">>> Kaydedilen Ürün Adı: " + title);
    }

    /**
     * 8.b Adımı: Ürün detay sayfasındaki güncel satış fiyatını okur ve senaryo veri havuzuna kaydeder.
     */
    @Step("Ürün fiyatını kaydet")
    public void saveProductPrice() {
        String price = getProductPage().getProductPrice();
        ScenarioDataStore.put("selectedProductPrice", price);
        System.out.println(">>> Kaydedilen Ürün Fiyatı: " + price);
    }

    /**
     * 8.c Adımı: 'Sepete Ekle' butonunun sayfada görünür ve tıklanabilir (aktif) olduğunu doğrular.
     */
    @Step("Sepete Ekle butonunun aktif olduğunu doğrula")
    public void verifyAddToCartButtonEnabled() {
        Assert.assertTrue(
                "Sepete Ekle butonu aktif ve tıklanabilir olmalıdır.",
                getProductPage().isAddToCartButtonEnabled()
        );
    }

    /**
     * 8. Adım: 'Sepete Ekle' butonuna tıklar.
     */
    @Step("Sepete Ekle butonuna tıkla")
    public void clickAddToCart() {
        getProductPage().addToCart();
    }

    /**
     * 8.b Adımı & Beklenen Sonuç (Assertion 4): Ürünün sepete eklendiğine dair
     * bildirim/onay mesajının görüntülendiğini doğrular.
     */
    @Step("Sepete eklendiğine dair onay mesajını gör")
    public void verifyAddToCartConfirmation() {
        Assert.assertTrue(
                "Ürünün sepete eklendiğine dair onay mesajı görülmelidir.",
                getProductPage().isAddedToCartConfirmationDisplayed()
        );
    }
}

package steps;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import pages.HomePage;
import pages.SearchResultsPage;
import support.DriverFactory;

/**
 * SearchSteps: Ürün arama, arama sonuçlarını değerlendirme ve listeden ürün seçme adımlarını içerir.
 */
public class SearchSteps {

    private HomePage homePage;
    private SearchResultsPage searchPage;

    private HomePage getHomePage() {
        if (homePage == null) {
            homePage = new HomePage(DriverFactory.getDriver());
        }
        return homePage;
    }

    private SearchResultsPage getSearchPage() {
        if (searchPage == null) {
            searchPage = new SearchResultsPage(DriverFactory.getDriver());
        }
        return searchPage;
    }

    /**
     * 4. Adım: Arama kutusuna belirtilen kelimeyi (ör. 'bilgisayar') yazar ve Enter ile aratır.
     *
     * @param keyword Aranacak ürün anahtar kelimesi
     */
    @Step("Arama kutusuna <keyword> yaz ve ara")
    public void searchProduct(String keyword) {
        getHomePage().search(keyword);
    }

    /**
     * 5. Adım & Beklenen Sonuç (Assertion 2): Arama sonuçlarının kullanıcı girdisine uygun olduğunu
     * ve ürün listesinin ekranda yüklendiğini doğrular.
     */
    @Step("Arama sonuçlarının yüklendiğini ve ürün listesinin görünür olduğunu doğrula")
    public void verifySearchResults() {
        Assert.assertTrue(
                "Arama sonuçları 'bilgisayar' aramasına uygun olmalı ve ürün listesi görüntülenmelidir.",
                getSearchPage().isSearchResultsDisplayed("bilgisayar")
        );
    }

    /**
     * 6. ve 7. Adım: Arama sonuçları ızgarasını (grid) değerlendirerek 2. satırın 1. ürününü bulur,
     * ürün adını geçici olarak saklar ve ürüne tıklayarak detay sayfasına geçiş yapar.
     */
    @Step("İkinci satırdaki ilk ürünü seç")
    public void selectFirstProductOfSecondRow() {
        // 4 sütunlu Hepsiburada ızgara yapısında 2. satırın ilk ürünü indeks 4'tür (5. element)
        WebElement product = getSearchPage().getFirstProductOfSecondRow(4);
        String name = getSearchPage().getProductName(product);
        if (name != null && !name.isBlank()) {
            ScenarioDataStore.put("selectedProductName", name);
        }
        getSearchPage().clickProduct(product);
    }
}

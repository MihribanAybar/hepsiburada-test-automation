package steps;

import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.AfterScenario;
import support.DriverFactory;

/**
 * HookImpl: Gauge yaşam döngüsü kancalarını (lifecycle hooks) yöneten sınıftır.
 * Senaryo başlamadan önce ve bittikten sonra yapılması gereken hazırlık ve temizlik işlemlerini yürütür.
 */
public class HookImpl {

    /**
     * Her senaryo çalışmadan önce otomatik olarak devreye girer.
     * Tarayıcı oturumunu başlatır ve hazır hale getirir.
     */
    @BeforeScenario
    public void setUp() {
        DriverFactory.startBrowser();
    }

    /**
     * Her senaryo tamamlandıktan sonra (başarılı veya başarısız) otomatik olarak çalışır.
     * Tarayıcıyı ve ilişkili işletim sistemi süreçlerini güvenli şekilde kapatır.
     */
    @AfterScenario
    public void tearDown() {
        DriverFactory.quitBrowser();
    }
}

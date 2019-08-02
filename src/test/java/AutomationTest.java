import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;


public class AutomationTest extends TestListenerAdapter {

    private WebDriver driver = null;
    private JavascriptExecutor js = null;


    @Test(description = "open page", priority = 0)
    public void openPage() {
        System.setProperty("webdriver.chrome.driver", "./src/test/webdriver/chromedriver.exe");
        this.driver = new ChromeDriver();
        this.js = (JavascriptExecutor) driver;

        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        driver.get("https://demostore.x-cart.com");
        driver.manage().window().maximize();

        String pageTitle = driver.getTitle();
        String pageTitleExpected = "X-Cart Demo store company > Catalog";

        Assert.assertEquals(pageTitle, pageTitleExpected);

    }

    @Test(description = "search product", dependsOnMethods = {"openPage"})
    public void searchProduct() {
        driver.findElement(By.name("substring")).sendKeys("Rooster Mug");
        driver.findElement(By.name("substring")).sendKeys(Keys.RETURN);
        driver.findElement(By.linkText("Rooster Mug in White [Related products]")).click();

        String productName = driver.findElement(By.xpath("//*[@class='fn title'][contains(text(),'Rooster Mug in White [Related products]')]")).getAttribute("innerHTML");
        String productNameExpected = "Rooster Mug in White [Related products]";

        String productPrice = driver.findElement(By.xpath("//*[@class='price product-price'][contains(text(),'$')]")).getAttribute("innerHTML");
        String productPriceExpected = "$19.99";

        Assert.assertEquals(productName, productNameExpected);
        Assert.assertEquals(productPrice, productPriceExpected);
    }

    @Test(description = "add cart", dependsOnMethods = {"searchProduct"})
    public void addCart() {
        WebElement addCart = driver.findElement(By.xpath("//*[@class='btn  regular-button regular-main-button add2cart submit']//*[text()='Add to cart']"));
        js.executeScript("arguments[0].click()", addCart);

        String productAdd = driver.findElement(By.xpath("//*[@class='item-name'][contains(text(),' ')]")).getAttribute("innerHTML");
        String productAddExpected = "Rooster Mug in White [Related products]";

        Assert.assertEquals(productAdd, productAddExpected);
    }

    @Test(description = "checkout", dependsOnMethods = {"addCart"})
    public void checkout() {
        WebElement checkout = driver.findElement(By.xpath("//*[@class='regular-main-button checkout']//*[text()='Checkout']"));
        js.executeScript("arguments[0].click()", checkout);
        driver.findElement(By.id("email")).sendKeys("adm@adm.ad");
        WebElement continueButton = driver.findElement(By.xpath("//*[@class='btn  regular-button anonymous-continue-button submit']//*[text()='Continue']"));
        js.executeScript("arguments[0].click()", continueButton);
        driver.findElement(By.id("shippingaddress-firstname")).sendKeys("Fulano");
        driver.findElement(By.id("shippingaddress-lastname")).sendKeys("Beltrano");
        driver.findElement(By.id("shippingaddress-street")).sendKeys("123 st", Keys.TAB);
        Select state = new Select(driver.findElement(By.id("shippingaddress-state-id")));
        state.selectByVisibleText("Rio Grande do Sul");

        String totalPrice = driver.findElement(By.xpath("//*[@class='order-total'][contains(text(),'$')]")).getAttribute("innerHTML").replaceAll("\\s+", "");
        String totalPriceExpected = "$25.84";
        Assert.assertEquals(totalPrice, totalPriceExpected);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test(description = "payment", dependsOnMethods = {"checkout"})
    public void payment() throws InterruptedException {
        WebElement chooseShipping = driver.findElement(By.xpath("//*[text()='Choose shipping']"));
        js.executeScript("arguments[0].click()", chooseShipping);
        WebElement proceedToPayment = driver.findElement(By.xpath("//*[text()='Proceed to payment']"));

        String name = driver.findElement(By.xpath("//div[@class='address-box']/span[@class='address-field address-field_firstname']")).getAttribute("innerHTML") +
                " " + driver.findElement(By.xpath("//div[@class='address-box']/span[@class='address-field address-field_lastname']")).getAttribute("innerHTML");
        String nameExpected = "Fulano Beltrano";
        String address = driver.findElement(By.xpath("//div[@class='address-box']/span[@class='address-field address-field_street']")).getAttribute("innerHTML");
        String addressExpected = "123 st";

        Assert.assertEquals(name, nameExpected);
        Assert.assertEquals(address, addressExpected);

        js.executeScript("arguments[0].click()", proceedToPayment);
        driver.findElement(By.id("pmethod2")).click();
        Thread.sleep(4000);
        WebElement placeOrder = driver.findElement(By.xpath("//*[text()='Place order']"));
        js.executeScript("arguments[0].click()", placeOrder);
        driver.close();
    }

}

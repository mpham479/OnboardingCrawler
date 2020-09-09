import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromeWebDriver {
    public ChromeWebDriver(){

    }

    public org.openqa.selenium.WebDriver setupDriver(){

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("headless");
        options.addArguments("window-size=1920,1080");
        options.addArguments("--hide-scrollbars");

        System.setProperty("webdriver.chrome.driver", CrawlerController.chromeDriverLocation);

        //create new webdriver instance
        org.openqa.selenium.WebDriver driver = new ChromeDriver(options);

        //go to url
        driver.get(CrawlerController.baseUrl + "userHome.do?&tenantName=" + CrawlerController.tenant);

        new WebDriverWait(driver,10).until(
                ExpectedConditions.textToBePresentInElementValue(
                        driver.findElement(By.name("requestedUrl")),
                        "/userHome.do?tenantName=" + CrawlerController.tenant + "&"
                )
        );
        //set username and password
        driver.findElement(By.name("loginname")).sendKeys(CrawlerController.username);
        ((JavascriptExecutor) driver).executeScript("document.getElementById(\"passwordField\").value = '" + CrawlerController.password + "'");
        //driver.findElement(By.name("password")).sendKeys(password);
        //driver.findElement(By.name("tenantName")).sendKeys(tenant);

        //click submit button when clickable
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.className("loginSubmitButton"))).click();

        return driver;
    }
}

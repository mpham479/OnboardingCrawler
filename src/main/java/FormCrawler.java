import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")

public class FormCrawler {
  static Map<String, Script> scripts = CrawlerController.scripts;
  static Map<String, CustomField> customFields = CrawlerController.customFieldSystemIds;
  static WebDriver driver;
  static CrawlerProgressData data;
  static int totalNumberofScripts = 0;
  static int currentScriptNumber = 0;
  static double iteration = 0;
  static double currentCalculatedPercentage = 0;
  static int currentPercentageRounded = 0;
  static StringBuilder sb = new StringBuilder();

  /**
   * Constructor
   */
  public FormCrawler() {

  }

  public static void startFormCrawling(CrawlerProgressData progressData) throws InterruptedException, IOException {

    checkInterrupted();

    //set data
    data = progressData;

    //create new webdriver instance
    driver = new ChromeWebDriver().setupDriver();

    //go to url
    driver.get(CrawlerController.baseUrl + "showForms.do?method=prepare&tenantName=" + CrawlerController.tenant);

    new WebDriverWait(driver, 100).until(ExpectedConditions.jsReturnsValue("return document.getElementsByClassName('pagebanner')[0].innerText.substring(0,document.getElementsByClassName('pagebanner')[0].innerText.indexOf(' items found'))"));

    //check if all javascript has finished
    if (driver instanceof JavascriptExecutor) {

      String numForms = String.valueOf(((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagebanner')[0].innerText.substring(0,document.getElementsByClassName('pagebanner')[0].innerText.indexOf(' items found'))"));

      //ajax call
      String javascript = "$.ajax({\n" +
          "\t\t\turl : \"/wpm/searchAction.do\",\n" +
          "\t\t\ttype : 'post',\n" +
          "\t\t\tscriptCharset: \"utf-8\",\n" +
          "\t\t\t//contentType: \"text/html;charset=UTF-8\",\n" +
          "\t\t\tsuccess : function(htmlContent, textStatus){\n" +
          "\t\t\t\twindow.htmlFormUsage = htmlContent\n" +
          "\t\t\t},\n" +
          "\t\t\tdata: app.addGlobalParams(\"phrase=&entity=Form&entity=Form&fields[0].name=name&pageSize=" + numForms + "&formName=searchForm&wpmTableId=&customLayout=&shortSearch=&ajaxRequest=true\"),\n" +
          "\t\t\tdataType: 'html',\n" +
          "\t\t\tbeforeSend : function (jqXJR, settings){\n" +
          "                jqXJR.setRequestHeader('X-CSRF-TOKEN', app.csrfToken);\n" +
          "\t\t\t\treturn true;\n" +
          "\t\t\t},\n" +
          "\t\t\tcomplete : function (jqXHR, textStatus){\n" +
          "\t\t\t\treturn true;\n" +
          "\t\t\t}\n" +
          "\t\t});";

      //populate with global variable with new value with javascript
      ((JavascriptExecutor) driver).executeScript(javascript);

      String result = waitForElement("window.htmlFormUsage");
      parseForms(result);
    }
  }

  private static void parseForms(String htmlToBeParsed) throws InterruptedException, IOException {

    //get document
    Document doc = Jsoup.parse(htmlToBeParsed);

    //get cf table
    Element formTable = doc.getElementById("formTable");

    //get rows
    Elements rows = formTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");

    //got data, now display progress bar
    data.formLoadingPanel.setVisible(false);
    data.formProgressPanel.setVisible(true);

    //specify percentage
    data.formProgress.setString("0% Starting...");

    StringBuilder sb = new StringBuilder();
    String format = "\r[%-100s]%d%%\t\t|\t(%d/%d)\t%s";

    double iteration = 100 / (double) rows.size();
    double currentCalculatedPercentage = 0;
    int currentPercentageRounded = 0;
    double counter = 0;

    //iterate through rows
    String formEditHtml = "";
    for (Element row : rows) {

      //check if interrupted
      checkInterrupted();

      //iterate counter
      counter++;

      //make a new custom field
      Form form = new Form();

      //get id
      Elements tds = row.getElementsByTag("td");
      String onclick = tds.get(0).getElementsByTag("a").get(0).attr("onclick");
      String id = onclick.substring(onclick.indexOf("('") + 2, onclick.indexOf("')"));

      //ajax call to get name, system id, and description
      String editAjax = "$.ajax({\n" +
          "\t\t\turl : \"showForms.do\",\n" +
          "\t\t\ttype : 'post',\n" +
          "\t\t\tscriptCharset: \"utf-8\",\n" +
          "\t\t\t//contentType: \"text/html;charset=UTF-8\",\n" +
          "\t\t\tsuccess : function(htmlContent, textStatus){\n" +
          "\t\t\t\twindow.htmlFormEdit = htmlContent\n" +
          "\t\t\t},\n" +
          "\t\t\tdata: app.addGlobalParams(\"method=edit&formId=" + id + "&ajaxRequest=true\"),\n" +
          "\t\t\tdataType: 'html',\n" +
          "\t\t\tbeforeSend : function (jqXJR, settings){\n" +
          "                jqXJR.setRequestHeader('X-CSRF-TOKEN', app.csrfToken);\n" +
          "\t\t\t\treturn true;\n" +
          "\t\t\t},\n" +
          "\t\t\tcomplete : function (jqXHR, textStatus){\n" +
          "\t\t\t\treturn true;\n" +
          "\t\t\t}\n" +
          "\t\t});";
      ((JavascriptExecutor) driver).executeScript(editAjax);

      //get results from ajax call
      String editResults = waitForElementNoEquals("window.htmlFormEdit", formEditHtml);
      formEditHtml = editResults;
      String alteredEditResults = editResults.substring(editResults.indexOf("<form name="));
      Document completeDoc = Jsoup.parse(alteredEditResults);

      //get name, system id, and description
      Element basicDataRows = completeDoc.getElementById("tableEditUserType");
      String name = basicDataRows.getElementsByAttributeValue("name", "name").first().val();
      String systemId = basicDataRows.getElementsByAttributeValue("name", "systemid").first().val();
      String description = basicDataRows.getElementsByAttributeValue("name", "description").first().val();

      form.setName(name);
      form.setSystemid(systemId);
      form.setDescription(description);
      form.setId(id);

      getFormActionUsages(form, "");

      getFormCustomFields(form);

      //save custom field to list
      CrawlerController.forms.put(systemId, form);

      //calculate progress bar and display
      currentCalculatedPercentage = iteration * counter;

      if (currentCalculatedPercentage > currentPercentageRounded) {
        while (currentCalculatedPercentage > currentPercentageRounded) {
          currentPercentageRounded += 1;
        }
      }
      //System.out.print(String.format(format,sb,currentPercentageRounded,(int)counter,rows.size(),name));

      //set progress bar
      data.formProgress.setValue(currentPercentageRounded);
      data.formProgress.setString(String.format("%d%% (%d/%d)", currentPercentageRounded, (int) counter, rows.size()));
      data.formList.append(name + "\n");
      data.formScroll.getViewport().setViewPosition(new Point(0, data.formList.getDocument().getLength()));

    }

    //color tab to signify finished
    data.processes.setBackgroundAt(data.processes.indexOfComponent(data.formData), Color.GREEN);

    //close driver
    driver.close();
  }

  private static void getFormCustomFields(Form form) throws IOException {
    //go to url
    driver.get(CrawlerController.baseUrl + "formDesigner.do?method=prepare&type=5&formId=" + form.getId() + "&tenantName=" + CrawlerController.tenant);

    new WebDriverWait(driver, 100).until(ExpectedConditions.jsReturnsValue("return document.getElementsByClassName(\"fdTable\")[0].getElementsByTagName(\"tr\").length\n"));

    Document doc = Jsoup.parse(driver.getPageSource());

    //get list of fields
    Elements fieldList = doc.getElementsByAttributeValue("screentype", "FIELD");

    //iterate through list of fields
    for (Element field : fieldList) {
      //get systemid
      String systemid = field.attr("systemid").substring(0, field.attr("systemid").indexOf("_0"));

      //add custom field usage
      CustomField cf = new CustomField();
      if (!nullCheck(systemid).isEmpty()) {
        cf.setSystemId(systemid);
        form.addUsesCustomFields(systemid, cf);
      }
    }

    //hide unnecessary factors for screenshot
    ((JavascriptExecutor) driver).executeScript("document.body.style.overflow = 'hidden';");  //scroll bar
    ((JavascriptExecutor) driver).executeScript("document.getElementsByClassName(\"fdPaletteDiv\")[0].style.display = 'none';");  //field overlay
    ((JavascriptExecutor) driver).executeScript("document.getElementById(\"walkme-player\").style.display = 'none';");  //help overlay

    //show system ids
    ((JavascriptExecutor) driver).executeScript("MenuManager.toggleSystemIds()");

    //take screenshot
    Screenshot screenshot = new AShot()
        .shootingStrategy(ShootingStrategies.viewportPasting(500))
        .takeScreenshot(driver, driver.findElement(By.id("srcLayout")));
    form.setScreenshot(screenshot);

    //return to previous screen
    //this doesn't seem very efficient, but do we care?
    driver.get(CrawlerController.baseUrl + "showForms.do?method=prepare&tenantName=" + CrawlerController.tenant);

    new WebDriverWait(driver, 100).until(ExpectedConditions.jsReturnsValue("return document.getElementsByClassName('pagebanner')[0].innerText.substring(0,document.getElementsByClassName('pagebanner')[0].innerText.indexOf(' items found'))"));

  }

  private static void getFormActionUsages(Form form, String page) {
    String id = form.getId();

    //get previous value
    String previousResult = String.valueOf(((JavascriptExecutor) driver).executeScript("return window.formActionUsage"));

    //create params
    String params = "method=showUsage&formId=" + id + "&ajaxRequest=true";

    if (!page.isEmpty()) {
      params += "&" + page;
    }

    //build ajax call
    String ajaxCall = "$.ajax({\n" +
        "\t\t\turl : \"/wpm/showForms.do\",\n" +
        "\t\t\ttype : 'post',\n" +
        "\t\t\tscriptCharset: \"utf-8\",\n" +
        "\t\t\t//contentType: \"text/html;charset=UTF-8\",\n" +
        "\t\t\tsuccess : function(htmlContent, textStatus){\n" +
        "\t\t\t\twindow.formActionUsage = htmlContent;\n" +
        "\t\t\t},\n" +
        "\t\t\tdata: app.addGlobalParams(\"" + params + "\"),\n" +
        "\t\t\tdataType: 'html',\n" +
        "\t\t\tbeforeSend : function (jqXJR, settings){\n" +
        "                jqXJR.setRequestHeader('X-CSRF-TOKEN', app.csrfToken);\n" +
        "\t\t\t\treturn true;\n" +
        "\t\t\t},\n" +
        "\t\t\tcomplete : function (jqXHR, textStatus){\n" +
        "\t\t\t\treturn true;\n" +
        "\t\t\t}\n" +
        "\t\t});";

    //populate with global variable with new value with javascript
    String temp = String.valueOf(((JavascriptExecutor) driver).executeScript(ajaxCall));

    String result = waitForElementNoEquals("window.formActionUsage", previousResult);

    Document doc = Jsoup.parse(result);

    //determine if there are multiple tabs
    Elements tbody = doc.getElementsByTag("tbody");
    Elements rows = new Elements();

    if (!tbody.isEmpty()) {
      rows = tbody.get(0).getElementsByTag("tr");
    }

    //go through tabs
    for (Element row : rows) {
      //get data
      Elements tds = row.getElementsByTag("td");
      String action = nullCheck(tds.get(0).getElementsByTag("a").get(0).text());
      String description = nullCheck(tds.get(1).text());
      String workflow = nullCheck(tds.get(2).text());
      String startStatus = nullCheck(tds.get(3).text());
      String endStatus = nullCheck(tds.get(4).text());

      //only care for rows with start and end statuses
      if (startStatus != "" && endStatus != "") {

        //save data
        Map<String, String> map = new HashMap<>();
        map.put(String.valueOf(FORMACTIONUSAGEHEADERS.of("0")), action);
        map.put(String.valueOf(FORMACTIONUSAGEHEADERS.of("1")), description);
        map.put(String.valueOf(FORMACTIONUSAGEHEADERS.of("2")), workflow);
        map.put(String.valueOf(FORMACTIONUSAGEHEADERS.of("3")), startStatus);
        map.put(String.valueOf(FORMACTIONUSAGEHEADERS.of("4")), endStatus);

        //add to form
        form.addActionUsages(map);

        //add workflow to form
        String workflowName = map.get("Workflow");
        Workflow tempWorkflow = new Workflow();
        form.addUsedInWorkflows(workflowName, tempWorkflow);
      }
    }
    //skip this if pages have already been found
    if (page.isEmpty()) {
      Elements links = doc.getElementsByClass("pagelinks");
      if (!links.isEmpty()) {
        Elements getlastInstances = links.last().select("a[href*='formUsageUrl?']");
        int pageNumbers = 0;
        String url = null;

        for (Element link : getlastInstances) {
          int currentPage = Integer.valueOf(link.attr("href").substring(link.attr("href").indexOf("=") + 1));
          String currentUrl = link.attr("href").substring(link.attr("href").indexOf("?") + 1, link.attr("href").indexOf("="));
          for (Element imgChild : link.children()) {
            if (imgChild.attr("src").equalsIgnoreCase("/wpm/img/displaytag/last.gif")) {
              //save the page and the url
              pageNumbers = currentPage;
              url = currentUrl;
            }
          }
        }

        for (int x = 2; x <= pageNumbers; x++) {
          getFormActionUsages(form, url + "=" + x);
        }
      }
    }

  }

  private static String waitForElement(String elementName) {

    int counter = 0;
    int max = 30;
    //wait for async to finish
    while (true) {
      //get value
      String script = "return " + elementName;
      String tempValue = String.valueOf(((JavascriptExecutor) driver).executeScript(script));

      if (tempValue != null && tempValue != "null") {
        return tempValue;
      } else {
        counter++;
        if (counter > max) {
          //throw exception
          throw new RuntimeException("Error waiting for " + elementName + ".");
        } else {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static String waitForElementNoEquals(String elementName, String diffString) {

    int counter = 0;
    int max = 20;
    //wait for async to finish
    while (true) {
      //get value
      String script = "return " + elementName;
      String tempValue = String.valueOf(((JavascriptExecutor) driver).executeScript(script));

      if (!tempValue.equalsIgnoreCase(diffString) && tempValue != null && tempValue != "null") {
        return tempValue;
      } else {
        counter++;
        if (counter > max) {
          System.out.println("Previous: " + diffString);
          System.out.println("Current: " + tempValue);
          //throw exception
          throw new RuntimeException("Error getting a different value from previous.");
        } else {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static String nullCheck(String string) {
    if (string == null || string.equalsIgnoreCase("null")) {
      return "";
    } else {
      return string;
    }
  }

  private static void checkInterrupted() throws InterruptedException {
    if (CrawlerController.interrupted) {
      try {
        //close driver
        driver.close();
      } catch (Exception e) {

      }

      //throw exception for thread
      throw new InterruptedException("Interrupted");
    }
  }
}


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class CustomFieldCrawler {

    public static CrawlerProgressData data;
    public static WebDriver driver;

    public CustomFieldCrawler(){

    }

    public static void startCustomFieldCrawling(CrawlerProgressData progressData) throws InterruptedException {

        checkInterrupted();

        //initialize data
        data = progressData;

        //create new webdriver instance
        driver = new ChromeWebDriver().setupDriver();

        //go to url
        driver.get(CrawlerController.baseUrl + "showCustomFields.do?method=prepare&tenantName=" + CrawlerController.tenant);

        //wait until script custom fields table is created
        //new WebDriverWait(driver,100).until(ExpectedConditions.presenceOfElementLocated(By.id("customField")));

        //wait until page banner is created
        //new WebDriverWait(driver,100).until(ExpectedConditions.presenceOfElementLocated(By.className("pagebanner")));

        new WebDriverWait(driver,100).until(ExpectedConditions.jsReturnsValue("return document.getElementsByClassName('pagebanner')[0].innerText.substring(0,document.getElementsByClassName('pagebanner')[0].innerText.indexOf(' items found'))"));

        //check if all javascript has finished
        if(driver instanceof JavascriptExecutor){

            String numCustomFields = String.valueOf(((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName('pagebanner')[0].innerText.substring(0,document.getElementsByClassName('pagebanner')[0].innerText.indexOf(' items found'))"));

            //ajax call
            String javascript = "$.ajax({\n" +
                    "url : \"/wpm/searchAction.do\",\n" +
                    "type : 'post',\n" +
                    "scriptCharset: \"utf-8\",\n" +
                    "success : function(htmlContent, textStatus){\n" +
                    "window.htmlCustomFieldUsage = htmlContent\n" +
                    "},\n" +
                    "data: app.addGlobalParams(\"resultAttributeName=customfields&entityFormName=entityCFSearchForm&target=cf_setup&refreshMethod=cfListRefresh&pageSize=" + numCustomFields + "&filterProperties%5B0%5D.name=fieldType&fields%5B0%5D.name=name&fields%5B1%5D.name=systemId&fields%5B2%5D.name=label&fields%5B3%5D.value=false&phrase=&entity=AllFields&entity=CustomField&ajaxRequest=true\"),\n" +
                    "dataType: 'html',\n" +
                    "beforeSend : function (jqXJR, settings){\n" +
                    "                jqXJR.setRequestHeader('X-CSRF-TOKEN', app.csrfToken);\n" +
                    "  return true;\n" +
                    "},\n" +
                    "complete : function (jqXHR, textStatus){\n" +
                    "return true;\n" +
                    "}\n" +
                    "});";

            //populate with global variable with new value with javascript
            ((JavascriptExecutor) driver).executeScript(javascript);

            String result = waitForElement("window.htmlCustomFieldUsage");
            parseCustomFields(result);
        }

    }

    private static void parseCustomFields(String htmlToBeParsed) throws InterruptedException {

        //get document
        Document doc = Jsoup.parse(htmlToBeParsed);

        //get cf table
        Element cfTable = doc.getElementById("customField");

        //get rows
        Elements rows = cfTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");

        //got data, now display progress bar
        data.customFieldLoadingPanel.setVisible(false);
        data.customFieldProgressPanel.setVisible(true);

        //specify percentage
        data.customFieldProgress.setString("0% Starting...");

        StringBuilder sb  =  new StringBuilder();
        String format = "\r[%-100s]%d%%\t\t|\t(%d/%d)\t%s";

        double iteration = 100/(double)rows.size();
        double currentCalculatedPercentage = 0;
        int currentPercentageRounded = 0;
        double counter = 0;

        //iterate through rows
        for(Element row : rows){

            //check if interrupted
            checkInterrupted();

            //iterate counter
            counter++;

            //make a new custom field
            CustomField customField = new CustomField();

            Elements tds = row.getElementsByTag("td");
            String id = tds.get(0).getElementsByTag("div").get(0).attr("id");
            String name = tds.get(0).getElementsByTag("div").get(0).attr("title");
            String systemId = tds.get(1).getElementsByTag("div").get(0).attr("title");
            String type = tds.get(2).text();
            String label = tds.get(3).text();

            customField.setName(name);
            customField.setSystemId(systemId);
            customField.setId(id);
            customField.setType(type);
            customField.setLabel(label);

            //save custom field to list
            CrawlerController.customFieldNames.put(name,customField);
            CrawlerController.customFieldSystemIds.put(systemId,customField);

            getCustomFieldActionUsages(customField, "");

            //calculate progress bar and display
            currentCalculatedPercentage = iteration*counter;

            if(currentCalculatedPercentage > currentPercentageRounded){
                while(currentCalculatedPercentage > currentPercentageRounded){
                    currentPercentageRounded += 1;
                }
            }
            //System.out.print(String.format(format,sb,currentPercentageRounded,(int)counter,rows.size(),name));

            //set progress bar
            data.customFieldProgress.setValue(currentPercentageRounded);
            data.customFieldProgress.setString(String.format("%d%% (%d/%d)",currentPercentageRounded,(int)counter, rows.size()));
            data.customFieldList.append(name + "\n");
            data.customFieldScroll.getViewport().setViewPosition(new Point(0,data.customFieldList.getDocument().getLength()));

        }

        //color tab to signify finished
        data.processes.setBackgroundAt(data.processes.indexOfComponent(data.customFieldData),Color.GREEN);

        //close driver
        driver.close();
    }

    private static void getCustomFieldActionUsages(CustomField customField, String page){

        String id = customField.getId();

        //get previous value
        String previousResult = String.valueOf(((JavascriptExecutor) driver).executeScript("return window.htmlScriptUsage"));

        //create params
        String params = "method=actions&cfId=" + id + "&ajaxRequest=true";

        if(!page.isEmpty()){
            params += "&" + page;
        }

        //ajax call
        String javascript = "$.ajax({\n" +
                "url : \"/wpm/customFieldUsage.do\",\n" +
                "type : 'post',\n" +
                "scriptCharset: \"utf-8\",\n" +
                "success : function(htmlContent, textStatus){\n" +
                "window.htmlScriptUsage = \"<div>" + id + "</div>\" + htmlContent;\n" +
                "},\n" +
                "data: app.addGlobalParams('" + params + "'),\n" +
                "dataType: 'html',\n" +
                "beforeSend : function (jqXJR, settings){\n" +
                "                jqXJR.setRequestHeader('X-CSRF-TOKEN', app.csrfToken);\n" +
                "  return true;\n" +
                "},\n" +
                "complete : function (jqXHR, textStatus){\n" +
                "return true;\n" +
                "}\n" +
                "});";

        //populate with global variable with new value with javascript
        String temp = String.valueOf(((JavascriptExecutor) driver).executeScript(javascript));

        String result = waitForElementNoEquals("window.htmlCustomFieldUsage",previousResult);


        Document doc = Jsoup.parse(result);

        //determine if there are multiple tabs
        Elements tbody = doc.getElementsByTag("tbody");
        Elements rows = new Elements();

        if(!tbody.isEmpty()){
            rows = tbody.get(0).getElementsByTag("tr");
        }

        //go through tabs
        for(Element row : rows) {
            //get data
            Elements tds = row.getElementsByTag("td");
            String action = nullCheck(tds.get(0).getElementsByTag("a").get(1).text());
            String description = nullCheck(tds.get(1).text());
            String workflow = nullCheck(tds.get(2).text());
            String startStatus = nullCheck(tds.get(3).text());
            String endStatus = nullCheck(tds.get(4).text());

            //only care for rows with start and end statuses
            if (startStatus != "" && endStatus != "") {

                //save data
                Map<String, String> map = new HashMap<>();
                map.put(String.valueOf(CFACTIONUSAGEHEADERS.of("0")), action);
                map.put(String.valueOf(CFACTIONUSAGEHEADERS.of("1")), description);
                map.put(String.valueOf(CFACTIONUSAGEHEADERS.of("2")), workflow);
                map.put(String.valueOf(CFACTIONUSAGEHEADERS.of("3")), startStatus);
                map.put(String.valueOf(CFACTIONUSAGEHEADERS.of("4")), endStatus);

                //add to custom field
                customField.addActionUsage(map);

                /*
                //add to workflow usages
                try {
                    String workflowName = map.get("Workflow");
                    CrawlerController.workflowNames.get(workflowName).addUsesCustomFields(customField.getSystemId(), customField);
                } catch (NullPointerException e) {
                    System.out.println("No workflow associated with: " + map + " for custom field: " + customField.getName());
                }
                */
                //add workflow to custom field
                String workflowName = map.get("Workflow");
                Workflow tempWorkflow = new Workflow();
                customField.addUsedInWorkflow(workflowName,tempWorkflow);
            }
        }
        //skip this if pages have already been found
        if(page.isEmpty()){
            Elements links = doc.getElementsByClass("pagelinks");
            if(!links.isEmpty()) {
                Elements getlastInstances = links.last().select("a[href*='customFieldUsageUrl?']");
                int pageNumbers = 0;
                String url = null;

                for(Element link : getlastInstances) {
                    int currentPage = Integer.valueOf(link.attr("href").substring(link.attr("href").indexOf("=") + 1));
                    String currentUrl = link.attr("href").substring(link.attr("href").indexOf("?") + 1, link.attr("href").indexOf("="));
                    for(Element imgChild : link.children()) {
                        if (imgChild.attr("src").equalsIgnoreCase("/wpm/img/displaytag/last.gif")) {
                            //save the page and the url
                            pageNumbers = currentPage;
                            url = currentUrl;
                        }
                    }
                }

                for (int x = 2; x <= pageNumbers; x++) {
                    getCustomFieldActionUsages(customField, url + "=" + x);
                }
            }
        }

    }

    private static String waitForElement(String elementName){

        int counter = 0;
        int max = 30;
        //wait for async to finish
        while(true){
            //get value
            String tempValue = String.valueOf(((JavascriptExecutor) driver).executeScript("return " + elementName));

            if(tempValue != null && tempValue != "null"){
                return tempValue;
            }else{
                counter++;
                if(counter > max){
                    //throw exception
                    throw new RuntimeException("Error waiting for " + elementName + ".");
                }else{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String waitForElementNoEquals(String elementName, String diffString){

        int counter = 0;
        int max = 20;
        //wait for async to finish
        while(true){
            //get value
            String tempValue = String.valueOf(((JavascriptExecutor) driver).executeScript("return window.htmlScriptUsage"));

            if(!tempValue.equalsIgnoreCase(diffString)){
                return tempValue;
            }else{
                counter++;
                if(counter > max){
                    System.out.println("Previous: " + diffString);
                    System.out.println("Current: " + tempValue);
                    //throw exception
                    throw new RuntimeException("Error getting a different value from previous.");
                }else{
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String nullCheck(String string){
        if(string == null || string.equalsIgnoreCase("null")){
            return "";
        }else{
            if(string.trim().isEmpty()){
                return "";
            }else{
                return string;
            }
        }
    }

    private static void checkInterrupted() throws InterruptedException {
        if(CrawlerController.interrupted){
            try{
                //close driver
                driver.close();
            }catch(Exception e){

            }

            //throw exception for thread
            throw new InterruptedException("Interrupted");
        }
    }
}

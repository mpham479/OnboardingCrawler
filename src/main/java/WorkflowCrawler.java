import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Map;

public class WorkflowCrawler {

    public WorkflowCrawler(){

    }

    public static void startWorkflowCrawling() throws InterruptedException {

        System.out.println("Starting Workflow Crawling");
        System.out.println();

        //create new webdriver instance
        WebDriver driver = CrawlerController.driver;

        //go to url
        driver.get(CrawlerController.baseUrl + "sp/workflow/list");

        //wait until script fancytree is created
        new WebDriverWait(driver,20).until(ExpectedConditions.presenceOfElementLocated(By.className("tableSection")));

        //check if all javascript has finished
        if(driver instanceof JavascriptExecutor){

            //get tbody length and start match
            String lenOfPage = String.valueOf(((JavascriptExecutor) driver).executeScript("document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0]" +
                    ".scrollTo(0,document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight);" +
                    "return document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight"));
            Boolean match = false;
            Integer iteratorCounter = 0;
            Integer maxCounter = 20;

            while(!match){
                Thread.sleep(3000);
                String lastCount = lenOfPage;
                lenOfPage = String.valueOf(((JavascriptExecutor) driver).executeScript("document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0]" +
                        ".scrollTo(0,document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight);" +
                        "return document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight"));
                if(lastCount.equalsIgnoreCase(lenOfPage) && lenOfPage != "0"){
                    match = true;
                }
            }

            Integer dataRows = Integer.parseInt(((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName(\"data-row\").length").toString());

            StringBuilder sb  =  new StringBuilder();
            String format = "\r[%-100s]%d%%\t\t|\t(%d/%d)\t%s";

            double iteration = 100/dataRows.doubleValue();
            double currentCalculatedPercentage = 0;
            int currentPercentageRounded = 0;

            for(int i = 0; i < dataRows; i++){
                //data variables
                String name;
                String description;
                String systemId;

                name = String.valueOf(((JavascriptExecutor) driver).executeScript(
                        "return document.getElementsByClassName(\"data-row\")[" + i + "].getElementsByClassName(\"ui-cell-data\")[0].firstChild.textContent"
                ));
                description = String.valueOf(((JavascriptExecutor) driver).executeScript(
                        "return document.getElementsByClassName(\"data-row\")[" + i + "].getElementsByClassName(\"ui-cell-data\")[1].firstChild.textContent"
                ));
                systemId  = String.valueOf(((JavascriptExecutor) driver).executeScript(
                        "return document.getElementsByClassName(\"data-row\")[" + i + "].getElementsByClassName(\"ui-cell-data\")[2].firstChild.textContent"
                ));

                Workflow workflow = new Workflow();
                workflow.setName(name);
                workflow.setDescription(description);
                workflow.setSystemId(systemId);

                CrawlerController.workflowSystemIds.put(systemId,workflow);
                CrawlerController.workflowNames.put(name,workflow);

                //calculate progress bar and display
                currentCalculatedPercentage = iteration*(i+1);
                if(currentCalculatedPercentage > (currentPercentageRounded+1)){
                    while(currentCalculatedPercentage > currentPercentageRounded+1){
                        sb.append("|");
                        currentPercentageRounded += 1;
                    }
                }
                if(i == (dataRows-1)){
                    //account for completion
                    sb.append("|");
                    currentPercentageRounded += 1;
                    format = "\r[%-100s]%d%% done!\n\n";
                }
                System.out.print(String.format(format,sb,currentPercentageRounded,i+1,dataRows,name));

            }
        }
    }
}

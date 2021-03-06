import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class WorkflowCrawler {

    public static WebDriver driver;

    public WorkflowCrawler(){

    }

    public static void startWorkflowCrawling(CrawlerProgressData data) throws Exception {
        checkInterrupted();
        //create new webdriver instance
        driver = new ChromeWebDriver().setupDriver();

        //go to url
        driver.get(CrawlerController.baseUrl + "sp/workflow/list");

        //wait until script fancytree is created
        //new WebDriverWait(driver,200).until(ExpectedConditions.presenceOfElementLocated(By.className("tableSection")));
        new WebDriverWait(driver,100).until(ExpectedConditions.jsReturnsValue("document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0]" +
                ".scrollTo(0,document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight);" +
                "return document.getElementsByClassName(\"tableSection\")[0].getElementsByTagName(\"tbody\")[0].scrollHeight"));

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

            //got data, now display progress bar
            data.workflowLoadingPanel.setVisible(false);
            data.workflowProgressPanel.setVisible(true);

            Integer dataRows = Integer.parseInt(((JavascriptExecutor) driver).executeScript("return document.getElementsByClassName(\"data-row\").length").toString());

            //specify percentage
            data.workflowProgress.setString("0% Starting...");

            StringBuilder sb  =  new StringBuilder();
            String format = "\r[%-100s]%d%%\t\t|\t(%d/%d)\t%s";

            double iteration = 100/dataRows.doubleValue();
            double currentCalculatedPercentage = 0;
            int currentPercentageRounded = 0;

            for(int i = 0; i < dataRows; i++){

            //check if interrupted
            checkInterrupted();

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
                        currentPercentageRounded += 1;
                    }
                }
                if(i == (dataRows-1)){
                    //account for completion
                    currentPercentageRounded += 1;
                }
                //System.out.print(String.format(format,sb,currentPercentageRounded,i+1,dataRows,name));

                //set progress bar
                data.workflowProgress.setValue(currentPercentageRounded);
                data.workflowProgress.setString(String.format("%d%% (%d/%d)",currentPercentageRounded,i+1, dataRows));
                data.workflowList.append(name + "\n");
                data.workflowScroll.getViewport().setViewPosition(new Point(0,data.workflowList.getDocument().getLength()));

            }
        }

        //color tab to signify finished
        data.processes.setBackgroundAt(data.processes.indexOfComponent(data.workflowData),Color.GREEN);

        //close driver
        driver.close();
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

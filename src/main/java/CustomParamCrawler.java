import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.util.List;

@SuppressWarnings("Duplicates")
public class CustomParamCrawler {
    public static void startCustomParamCrawling(CrawlerProgressData data) throws InterruptedException {

        //bring to front
        CrawlerController.progressFrame.toFront();

        System.out.println("Starting Custom Param Crawling");
        System.out.println();

        //switch to this panel
        data.processes.setEnabledAt(1,true);
        data.processes.setSelectedComponent(data.customParamData);

        //create new webdriver instance
        WebDriver driver = CrawlerController.driver;

        //go to url
        driver.get(CrawlerController.baseUrl + "sp/customparameters?tenantName=" + CrawlerController.tenant);

        //wait until script fancytree is created
        new WebDriverWait(driver,20).until(ExpectedConditions.presenceOfElementLocated(By.tagName("wf-local-param-list")));

        //check if all javascript has finished
        if(driver instanceof JavascriptExecutor){

            data.customParamProgress.setString("0% Starting...");

            //get number of custom params
            Integer dataRows = Integer.parseInt(((JavascriptExecutor) driver).executeScript("" +
                    "var counter = 0; " +
                    "var list = document.getElementsByTagName(\"wf-local-param-list\"); " +
                    "for(var i = 0; i < list.length; i++){" +
                    "   var divs = list[i].getElementsByClassName(\"form-group\");" +
                    "   if(divs.length > 0){" +
                    "       counter += divs.length" +
                    "   }" +
                    "} " +
                    "return counter").toString());

            StringBuilder sb  =  new StringBuilder();
            String format = "\r[%-100s]%d%%\t\t|\t(%d/%d)\t%s";

            double iteration = 100/dataRows.doubleValue();
            double currentCalculatedPercentage = 0;
            int currentPercentageRounded = 0;
            int counter = 0;

            List<WebElement>  outerHtml = driver.findElements(By.tagName("wf-local-param-list"));

            for(int i = 0; i < outerHtml.size(); i++) {

                //get element
                WebElement outerDiv = outerHtml.get(i);

                //get category
                String category;
                category = outerDiv.findElement(By.tagName("h3")).getText();

                List<WebElement> innerDivs = outerDiv.findElements(By.className("form-group"));

                //go through form groups
                for(int j = 0; j < innerDivs.size(); j++) {
                    //progress bar
                    counter++;

                    //data variables
                    String name;
                    String type;
                    String description;

                    //get values
                    description = nullCheck(innerDivs.get(j).findElement(By.className("local-param-description-label")).getText());
                    name = nullCheck(innerDivs.get(j).findElement(By.tagName("input")).getAttribute("id"));
                    type = nullCheck(innerDivs.get(j).findElement(By.tagName("input")).getAttribute("type"));

                    //create custom param object and save data
                    CustomParam cp = new CustomParam();

                    cp.setCategory(category);
                    cp.setName(name);
                    cp.setType(type);
                    cp.setDescription(description);

                    //add to map of custom params
                    CrawlerController.customParams.put(name,cp);

                    //calculate progress bar and display
                    currentCalculatedPercentage = iteration*counter;

                    if(currentCalculatedPercentage > currentPercentageRounded){
                        while(currentCalculatedPercentage > currentPercentageRounded){
                            sb.append("|");
                            currentPercentageRounded += 1;
                        }
                    }
                    if(counter == dataRows){
                        //account for completion
                        format = "\r[%-100s]%d%%\t\t|\t(%d/%d) done!\n\n";
                    }
                    System.out.print(String.format(format,sb,currentPercentageRounded,counter,dataRows,name));

                    //set progress bar
                    data.customParamProgress.setValue(currentPercentageRounded);
                    data.customParamProgress.setString(String.format("%d%% (%d/%d)",currentPercentageRounded,counter, dataRows));
                    data.customParamList.append(name + "\n");
                    data.customParamScroll.getViewport().setViewPosition(new Point(0,data.customParamList.getDocument().getLength()));
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
}

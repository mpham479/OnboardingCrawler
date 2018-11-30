import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class ScriptCrawler {

    static Map<String, Script> scripts = CrawlerController.scripts;
    static Map<String, CustomField> customFields = CrawlerController.customFieldSystemIds;
    static WebDriver driver;
    static CrawlerProgressData data;
    static int totalNumberofScripts = 0;
    static int currentScriptNumber = 0;
    static double iteration = 0;
    static double currentCalculatedPercentage = 0;
    static int currentPercentageRounded = 0;
    static StringBuilder sb  =  new StringBuilder();

    /**
     * Constructor
     */
    public ScriptCrawler() {

    }

    public static void startScriptCrawling(CrawlerProgressData progressData) throws InterruptedException {

        checkInterrupted();

        //set data
        data = progressData;

        //create new webdriver instance
        driver = new ChromeWebDriver().setupDriver();

        //go to url
        driver.get(CrawlerController.baseUrl + "showScripts.do?method=prepare&tenantName=" + CrawlerController.tenant);

        //wait until script fancytree is created
        new WebDriverWait(driver,100).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("tree")));

        //check if all javascript has finished
        if(driver instanceof JavascriptExecutor){

            //run javascript to expand all script folders
            ((JavascriptExecutor) driver).executeScript("$('#tree').fancytree('getTree').visit(function(node){node.setExpanded(true);})");

            //get children under root node
            String rootChildren = "return $('#tree').fancytree('getTree').getRootNode().children[0]";
            Integer rootChildrenSize = Integer.parseInt(((JavascriptExecutor) driver).executeScript(rootChildren + ".children.length").toString());

            //initialize previous script
            String previousScriptId = null;

            //get number of all values
            totalNumberofScripts = Integer.parseInt(String.valueOf(((JavascriptExecutor) driver).executeScript("var numscripts = 0; " +
                    "$('#tree').fancytree('getTree').visit(function(node){" +
                    "if(node.folder != true && node.data.draftScriptId == null){numscripts++}" +
                    "}); " +
                    "return numscripts")));

            //got data, now display progress bar
            data.scriptLoadingPanel.setVisible(false);
            data.scriptProgressPanel.setVisible(true);

            //define iterations
            iteration = 100/(double)totalNumberofScripts;

            //specify percentage
            data.customFieldProgress.setString("0% Starting...");

            //iterate through all children in root
            for(int x = 0; x < rootChildrenSize; x++){
                //set test flag
                //if the folder is a test folder
                boolean testFlag = false;

                //get child node
                String childRoot = rootChildren + ".children[" + x + "]";

                //check if draft, those will be ignored
                if(!"true".equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript(childRoot + ".data.isDraft")))){
                    //check if folder
                    if("true".equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript(childRoot + ".folder")))){
                        previousScriptId = navigateFolder(childRoot, testFlag, (x == rootChildrenSize-1), previousScriptId);
                    }else{
                        previousScriptId = navigateScripts(childRoot,testFlag, (x == rootChildrenSize-1), previousScriptId);
                    }
                }

                //check if thread should stop
                checkInterrupted();
            }

        }

        //color tab to signify finished
        data.processes.setBackgroundAt(data.processes.indexOfComponent(data.scriptData),Color.GREEN);

        //close driver
        driver.close();

    }

    public static String navigateFolder(String root, boolean testFlag, boolean lastFolder, String previousScriptId) throws InterruptedException {

        //check if thread should stop
        checkInterrupted();

        //get folder name
        String folderName = String.valueOf(((JavascriptExecutor) driver).executeScript(root + ".title"));

        String changingScriptId = previousScriptId;

        //set test flag if folder is labelled test
        if(folderName.contains("Test")) {
            testFlag = true;
        }

        try {


            //need to go one more
            Integer folderSize = Integer.parseInt(((JavascriptExecutor) driver).executeScript(root + ".children.length").toString());

            for (int x = 0; x < folderSize; x++) {
                String newRoot = root + ".children[" + x + "]";

                //check if draft, those will be ignored
                if (!"true".equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript(newRoot + ".data.isDraft")))) {
                    //check if folder
                    if ("true".equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript(newRoot + ".folder")))) {
                        //go through new folder
                        navigateFolder(newRoot, testFlag, (lastFolder && x == folderSize - 1), changingScriptId);
                    } else {
                        //get script data
                        changingScriptId = navigateScripts(newRoot, testFlag, (lastFolder && x == folderSize - 1), changingScriptId);
                    }
                }
            }

        }catch(Exception e){
            System.out.println("Error getting data for folder: " + folderName + ". " + e.getMessage());
        }

        //returns last script id
        return changingScriptId;

    }

    public static String navigateScripts(String root, boolean testFlag, boolean lastScript, String previousScriptId) throws InterruptedException {

        //check if thread should stop
        checkInterrupted();

        //vars
        String scriptName = "";
        String scriptSystemId = "";
        String scriptDesctipion = "";
        String type = "";
        String scriptCode = "";

        //get id
        String id = String.valueOf(((JavascriptExecutor) driver).executeScript(root + ".data.id"));

        ((JavascriptExecutor) driver).executeScript("$.ajax({\n" +
                "        url: 'showScripts.do',\n" +
                "        type: 'post',\n" +
                "        scriptCharset: \"utf-8\",\n" +
                "        //contentType: \"text/html;charset=UTF-8\",\n" +
                "        success: function(htmlContent, textStatus) {\n" +
                "            currentScript = eval(\"(\" + eval(\"(\\\"\" + htmlContent.substring(50,htmlContent.indexOf(\"\\\", \\\"true\\\");\\n\\tif (modalDivs == null || modalDivs.length == 0){\")) + \"\\\")\") + \")\")\n" +
                "        },\n" +
                "        data: app.addGlobalParams('method=edit&fromScriptIDE=true&scriptId=" + id + "&checkForDraft=false&ajaxRequest=true'),\n" +
                "        dataType: 'html',\n" +
                "        beforeSend: function(jqXJR, settings) {\n" +
                "        },\n" +
                "        complete: function(jqXHR, textStatus) {\n" +
                "        }\n" +
                "    });");

        //wait until javascript does not return null
        new WebDriverWait(driver,20).until(ExpectedConditions.jsReturnsValue("return currentScript"));

        if(!lastScript) {
            //loop until current script changes
            for (int counter = 0; counter < 10; counter++) {
                if (id.equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.id")))) {
                    break;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            //check if current script changed
            if (!id.equalsIgnoreCase(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.id")))) {
                throw new RuntimeException("CurrentScript does not change.");
            }
        }

        //get currentscript data
        scriptName = nullCheck(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.name")));
        scriptSystemId = nullCheck(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.systemId")));
        scriptDesctipion = nullCheck(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.description")));
        type = nullCheck(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.type")));
        scriptCode = nullCheck(String.valueOf(((JavascriptExecutor) driver).executeScript("return currentScript.code")));

        //create a new script instance
        Script script;

        if(scripts.containsKey(scriptSystemId)) {
            //get script
            script = scripts.get(scriptSystemId);
        }else{
            //new script
            script = new Script();
            scripts.put(scriptSystemId,script);
        }

        //set data
        script.setId(id);
        script.setName(scriptName);
        script.setSystemId(scriptSystemId);
        script.setDescription(scriptDesctipion);
        script.setType(String.valueOf(SCRIPTTYPES.of(type)));
        script.setCode(scriptCode);

        //check if test scripts
        if(!testFlag && !scriptName.toLowerCase().contains("test")){
            //get workflows used
            getWorkflowsUsed(scriptSystemId,script);

            //get usages
            saveWhereScriptsUsed(script, "");

            //get custom param usages
            getCustomParamsUsed(scriptCode,scriptSystemId,script);

            //get script usage
            getUtilScriptsUsed(scriptCode,scriptSystemId,script);
            getScriptGetsUsed(scriptCode,scriptSystemId,script);
            getScriptExecutesUsed(scriptCode,scriptSystemId,script);
            getAsyncTasksUsed(scriptCode,scriptSystemId,script);

            //get custom field usage
            getCaseGetCustomField(scriptCode,scriptSystemId,script);
            getCaseGetCustomFieldvalue(scriptCode,scriptSystemId,script);
            getCaseSetCustomField(scriptCode,scriptSystemId,script);
            getFormGetField(scriptCode,scriptSystemId,script);
            getFormGetTableController(scriptCode,scriptSystemId,script);
            getFormGetValue(scriptCode,scriptSystemId,script);
            getFormRefreshElement(scriptCode,scriptSystemId,script);
            getFormSetValue(scriptCode,scriptSystemId,script);

        }

        //advance progress bar
        currentScriptNumber++;
        currentCalculatedPercentage = currentScriptNumber*iteration;

        //check if percentage should be moved up
        if(currentCalculatedPercentage > currentPercentageRounded){
            while(currentCalculatedPercentage > currentPercentageRounded){
                currentPercentageRounded += 1;
            }
        }

        //System.out.print(String.format(format,sb,currentPercentageRounded,currentScriptNumber,totalNumberofScripts,script.name));

        //set progress bar
        data.scriptProgress.setValue(currentPercentageRounded);
        data.scriptProgress.setString(String.format("%d%% (%d/%d)",currentPercentageRounded,currentScriptNumber, totalNumberofScripts));
        data.scriptList.append(script.name + "\n");
        data.scriptScroll.getViewport().setViewPosition(new Point(0,data.scriptList.getDocument().getLength()));

        //return script id
        return id;
    }

    public static void getWorkflowsUsed(String scriptSystemId, Script script){
        //check if scheduled task
        if(script.getType().equalsIgnoreCase("Scheduled_Task")){
            try{
                //get name
                String workflowSysId = script.getName().substring(0,script.getName().indexOf(" ")).replaceAll("/[&\\/\\\\#,+()$~%.'\":*?<>{} _-]/g","");

                /*
                //check if workflow exists
                for(Map.Entry<String, Workflow> workflow : CrawlerController.workflowSystemIds.entrySet()){
                    if(workflow.getKey().equalsIgnoreCase(workflowSysId)){
                        //add to script
                        script.addUsedInWorkflow(workflow.getKey(),workflow.getValue());

                        //add to workflow
                        workflow.getValue().addUsedScripts(scriptSystemId,script);
                        break;
                    }
                }
                */
                //add to script

                Workflow workflow = new Workflow();
                if(!nullCheck(workflowSysId).isEmpty() && !script.getUsedInWorkflowSysId().containsKey(workflowSysId)){
                    workflowSysId = workflowSysId.toLowerCase();
                    workflow.setSystemId(workflowSysId);
                    script.addUsedInWorkflowSysId(workflowSysId,workflow);
                }
            }catch(Exception e){
                System.out.println("Could not get scheduled task to workflow association.");
            }
        }
    }

    public static void getCustomParamsUsed(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("resp.getAppParam("); index >= 0; index = scriptCode.indexOf("resp.getAppParam(",index+1)){
            try {
                String usedCustomParamName = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(")", index) - 1
                );
                //add custom param
                CustomParam param = new CustomParam();
                if (!nullCheck(usedCustomParamName).isEmpty() && !script.getUsesCustomParams().containsKey(usedCustomParamName)) {
                    param.setName(usedCustomParamName);
                    script.addUsesCustomParams(usedCustomParamName, param);
                }
                /*
                //get custom param
                if(CrawlerController.customParams.containsKey(usedCustomParamName)){
                    CustomParam cp = CrawlerController.customParams.get(usedCustomParamName);

                    //add to custom param
                    cp.addUsedByScripts(scriptSystemId,script);

                    //add to script
                    script.addUsesCustomParams(cp.getName(),cp);
                };
                */

            }catch(Exception e){
                System.out.println("Custom Param " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid custom param.");
            }
        }
    }

    public static void getUtilScriptsUsed(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("resp.importScript("); index >= 0; index = scriptCode.indexOf("resp.importScript(",index+1)){
            try {
                String usedScriptSystemId = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(")", index) - 1
                );

                //save scripts
                saveScriptUsages(scriptSystemId, usedScriptSystemId, script);

            }catch(Exception e){
                System.out.println("Script " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid script.");
            }
        }
    }

    public static void getScriptGetsUsed(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("resp.script.get("); index >= 0; index = scriptCode.indexOf("resp.script.get(",index+1)){
            try {
                String usedScriptSystemId = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(")", index) - 1
                );

                //save scripts
                saveScriptUsages(scriptSystemId, usedScriptSystemId, script);

            }catch(Exception e){
                System.out.println("Script " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid script.");
            }
        }
    }

    public static void getScriptExecutesUsed(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("resp.script.execute("); index >= 0; index = scriptCode.indexOf("resp.script.execute(",index+1)){
            try{
                String usedScriptSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index) + 1,
                        scriptCode.indexOf(")",index)
                );
                if(usedScriptSystemId.contains("'") || usedScriptSystemId.contains("\"")){
                    //save scripts
                    usedScriptSystemId = usedScriptSystemId.substring(1,usedScriptSystemId.length()-1);
                    saveScriptUsages(scriptSystemId, usedScriptSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + usedScriptSystemId);
                }

            }catch(Exception e){
                System.out.println("Script " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid script.");
            }
        }
    }

    public static void getAsyncTasksUsed(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("resp.asyncTask.execute("); index >= 0; index = scriptCode.indexOf("resp.asyncTask.execute(",index+1)){
            try{
                String usedScriptSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index) + 2,
                        scriptCode.indexOf(",",index) - 1
                );

                //save scripts
                saveScriptUsages(scriptSystemId, usedScriptSystemId, script);

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid script.");
            }
        }
    }

    public static void getFormGetValue(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("form.getValue("); index >= 0; index = scriptCode.indexOf("form.getValue(",index+1)){
            try{
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index)+2,
                        scriptCode.indexOf(")",index)-1
                );

                //add custom field
                CustomField cf = new CustomField();
                if(!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)){
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId,cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getFormRefreshElement(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("form.refreshElement("); index >= 0; index = scriptCode.indexOf("form.refreshElement(",index+1)){
            try{
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index)+2,
                        scriptCode.indexOf(")",index)-1
                );

                //add custom field
                CustomField cf = new CustomField();
                if(!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)){
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId,cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getFormGetTableController(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("form.getTableController("); index >= 0; index = scriptCode.indexOf("form.getTableController(",index+1)){
            try{
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index)+2,
                        scriptCode.indexOf(")",index) - 1
                );

                //add custom field
                CustomField cf = new CustomField();
                if(!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)){
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId,cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getFormGetField(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("form.getField("); index >= 0; index = scriptCode.indexOf("form.getField(",index+1)){
            try {
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(")", index) - 1
                );

                //add custom field
                CustomField cf = new CustomField();
                if (!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)) {
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId, cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */
            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getFormSetValue(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf("form.setValue("); index >= 0; index = scriptCode.indexOf("form.setValue(",index+1)){
            try{
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index)+2,
                        scriptCode.indexOf(",",index) -1
                );

                //add custom field
                CustomField cf = new CustomField();
                if(!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)){
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId,cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getCaseGetCustomFieldvalue(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf(".getCustomFieldValue("); index >= 0; index = scriptCode.indexOf(".getCustomFieldValue(",index+1)){
            try{
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(",index)+2,
                        scriptCode.indexOf(")",index) - 1
                );

                //add custom field
                CustomField cf = new CustomField();
                if(!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)){
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId,cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */

            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getCaseSetCustomField(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf(".setCustomField("); index >= 0; index = scriptCode.indexOf(".setCustomField(",index+1)){
            try {
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(",", index) - 1
                );

                //add custom field
                CustomField cf = new CustomField();
                if (!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)) {
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId, cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */
            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    public static void getCaseGetCustomField(String scriptCode, String scriptSystemId, Script script){
        //go through each instance
        for(int index = scriptCode.indexOf(".getCustomField("); index >= 0; index = scriptCode.indexOf(".getCustomField(",index+1)){
            try {
                String customFieldSystemId = scriptCode.substring(
                        scriptCode.indexOf("(", index) + 2,
                        scriptCode.indexOf(")", index) - 1
                );

                //add custom field
                CustomField cf = new CustomField();
                if (!nullCheck(customFieldSystemId).isEmpty() && !script.getUsesCustomFields().containsKey(customFieldSystemId)) {
                    cf.setSystemId(customFieldSystemId);
                    script.addUsesCustomFields(customFieldSystemId, cf);
                }

                /*
                if(customFieldSystemId.contains("'") || customFieldSystemId.contains("\"")){
                    //save scripts
                    customFieldSystemId = customFieldSystemId.substring(1,customFieldSystemId.length()-1);
                    saveCustomFieldUsages(scriptSystemId, customFieldSystemId, script);
                }else{
                    //error here
                    //System.out.println("Not saved: " + customFieldSystemId);
                }
                */
            }catch(Exception e){
                System.out.println("Field " + scriptCode.substring(scriptCode.indexOf("(", index), scriptCode.indexOf(")", index)) + " is not a valid field.");
            }
        }
    }

    private static void saveScriptUsages(String scriptSystemId, String usedScriptSystemId, Script script){
        Script usedScript;

        //check if already made and stored in scripts map
        if(scripts.containsKey(usedScriptSystemId)){
            //add the current script to the usedBy list in the mentioned script
            scripts.get(usedScriptSystemId).addUsedByScripts(scriptSystemId,script);

            //add the mentioned script to the current script
            script.addUsedScripts(usedScriptSystemId,scripts.get(usedScriptSystemId));

            //add workflows
            for(Map.Entry<String, Workflow> workflow : script.getUsedInWorkflowName().entrySet()){
                scripts.get(usedScriptSystemId).addUsedInWorkflowName(workflow.getKey(),workflow.getValue());
            }

            for(Map.Entry<String, Workflow> workflow : script.getUsedInWorkflowSysId().entrySet()){
                scripts.get(usedScriptSystemId).addUsedInWorkflowSysId(workflow.getKey(),workflow.getValue());
            }


        }else{
            usedScript = new Script();
            scripts.put(usedScriptSystemId,usedScript);

            //set id
            usedScript.setSystemId(usedScriptSystemId);

            //add the current script to the usedBy list in the mentioned script
            usedScript.addUsedByScripts(scriptSystemId,script);

            //add the mentioned script to the current script
            script.addUsedScripts(usedScriptSystemId,usedScript);

            //add workflows
            for(Map.Entry<String, Workflow> workflow : script.getUsedInWorkflowName().entrySet()){
                usedScript.addUsedInWorkflowName(workflow.getKey(),workflow.getValue());
            }

            for(Map.Entry<String, Workflow> workflow : script.getUsedInWorkflowSysId().entrySet()){
                usedScript.addUsedInWorkflowSysId(workflow.getKey(),workflow.getValue());
            }
        }
    }

    private static void saveWhereScriptsUsed(Script script, String page){
        if(script.getType().equalsIgnoreCase("workflow") ||
                script.getType().equalsIgnoreCase("form")) {

            String usage = (script.getType().equalsIgnoreCase("workflow"))?"usage":"formScriptUsage";

            //get previous value
            String previousResult = String.valueOf(((JavascriptExecutor) driver).executeScript("return window.htmlScriptUsage"));

            if(!page.isEmpty()){
                usage += "&" + page;
            }

            String javascript = "$.ajax({\n" +
                    "        url: 'showScripts.do',\n" +
                    "        type: 'post',\n" +
                    "        scriptCharset: \"utf-8\",\n" +
                    "        //contentType: \"text/html;charset=UTF-8\",\n" +
                    "        success: function(htmlContent, textStatus) {\n" +
                    "            window.htmlScriptUsage = \"<div>" + script.getId() + page + "</div>\" + htmlContent;\n" +
                    "        },\n" +
                    "        data: app.addGlobalParams(\"method=" + usage + "&scriptId=" + script.getId() + "&ajaxRequest=true\"),\n" +
                    "        dataType: 'html',\n" +
                    "        beforeSend: function(jqXJR, settings) {\n" +
                    "\t\t\treturn true;\n" +
                    "        },\n" +
                    "        complete: function(jqXHR, textStatus) {\n" +
                    "\t\t\treturn true;\n" +
                    "        }\n" +
                    "    });";

            //populate with new value
            ((JavascriptExecutor) driver).executeScript(javascript);

            int counter = 0;
            int max = 100;
            //wait for async to finish
            while(true){
                //get value
                String tempValue = String.valueOf(((JavascriptExecutor) driver).executeScript("return window.htmlScriptUsage"));

                if(!tempValue.equalsIgnoreCase(previousResult)){
                    previousResult = tempValue;
                    break;
                }else{
                    counter++;
                    if(counter > max){
                        //throw exception
                        throw new RuntimeException("Error getting script usages for " + script.getName() + " Page: " + page + ". Ajax: \n" + javascript + ". \n" +
                        "Temp Value: " + tempValue + "\n" +
                        "Previous Value: " + previousResult);
                    }else{
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            Document doc = Jsoup.parse(previousResult);

            //determine if there are multiple tabs
            Elements tabContainer = doc.getElementsByClass("tabs-container");
            Elements tabs = new Elements();


            if(tabContainer.isEmpty() && script.getType().equalsIgnoreCase("Workflow")){ //workflow script
                //check if there is data
                if(doc.getElementById("actionDefn") != null){
                    tabs.add(doc.getElementById("actionDefn"));
                }
            }else{  //form script
                tabs = tabContainer.get(0).children();
            }

            //go through tabs
            for(Element tab : tabs){
                //tab name

                String tabId = tab.attr("id");

                //get data
                Elements rows = tab.getElementsByClass("odd");
                rows.addAll(tab.getElementsByClass("even"));

                for(int x = 0; x < rows.size(); x++){
                    //initialize values
                    Map<String,String> map = new HashMap<>();
                    Element row = rows.get(x);
                    Elements tableDatas = row.getElementsByTag("td");
                    String workflowName;
                    String workflowSystemId;


                    //check if there is data to pull
                    if(tableDatas.size()> 1){

                        //get action
                        if(script.getType().equalsIgnoreCase("workflow")){
                            //add workflow name
                            map.put(String.valueOf(WORKFLOWUSAGEHEADERS.of("0")),tableDatas.get(0).getElementsByTag("a").text());
                        }else if(script.getType().equalsIgnoreCase("form")){
                            //add workflow name
                            map.put(String.valueOf(FORMUSAGEHEADERS.of("0")),tableDatas.get(0).getElementsByTag("a").text());
                        }else{
                            //check for table controller has already been mentioned
                            for(Map.Entry<String, CustomField> entry : customFields.entrySet()){
                                if(entry.getValue().getName().equalsIgnoreCase(tableDatas.get(0).text())){

                                    map.put("Name",entry.getValue().getName());
                                    map.put("System Id",entry.getValue().getSystemId());

                                    entry.getValue().addUsedByScripts(script.getSystemId(),script);

                                    //add to script usages
                                    script.addTcUsage(map);

                                    //add to workflow usages
                                    try{
                                        String tcName = map.get("Name");
                                        //get table controller custom field
                                        //get workflows where it is used
                                        //add script to those workflows
                                        //CrawlerController.workflowNames.get(workflowName).addUsedScripts(script.getSystemId(),script);
                                    }catch(NullPointerException e){
                                        //error here
                                        //System.out.println("No workflow associated with: " + map + " for script: " + script.getName());
                                    }
                                }
                            }

                            //continue because we don't care about the rest for this scenario
                            continue;

                        }

                        //go through each td
                        //the -1 is to ignore the remove action at the end
                        for(int index = 1; index < tableDatas.size()-1; index++){
                            if(script.getType().equalsIgnoreCase("workflow")){
                                map.put(String.valueOf(WORKFLOWUSAGEHEADERS.of(String.valueOf(index))),tableDatas.get(index).text());
                            }else{
                                map.put(String.valueOf(FORMUSAGEHEADERS.of(String.valueOf(index))),tableDatas.get(index).text());
                            }

                        }

                        if(!StringUtils.isEmpty(map.get("Workflow")) && !StringUtils.isEmpty(map.get("StartStatus")) && !StringUtils.isEmpty(map.get("EndStatus"))){
                            //add to script usages
                            script.addActionUsage(map);

                            //add to workflow usages
                            try{
                                //add script usages as well
                                workflowName = map.get("Workflow");
                                //Workflow tempWorkflow = CrawlerController.workflowNames.get(workflowName);
                                //tempWorkflow.addUsedScripts(script.getSystemId(),script);
                                Workflow tempWorkflow = new Workflow();
                                tempWorkflow.setName(workflowName);
                                script.addUsedInWorkflowName(tempWorkflow.getName(),tempWorkflow);

                                /*
                                //go through custom fields
                                for (String cfSysId : script.getUsesCustomFields().keySet()){
                                    //save if not in the workflow yet
                                    if(!tempWorkflow.getUsesCustomFields().containsKey(cfSysId)){
                                        tempWorkflow.addUsesCustomFields(cfSysId,script.getUsesCustomFields().get(cfSysId));
                                    }
                                }
                                */
                            }catch(NullPointerException e){
                                //error here
                                //System.out.println("No workflow associated with: " + map + " for script: " + script.getName());
                            }
                        }
                    }
                }

                //skip this if pages have already been found
                if(page.isEmpty()){
                    Elements links = tab.getElementsByClass("pagelinks");
                    if(!links.isEmpty()) {
                        Elements getlastInstances = links.last().select("a[href*='scriptUsageUrl?']");
                        int pageNumbers = 0;
                        String url = null;

                        for (Element link : getlastInstances) {
                            int currentPage = Integer.valueOf(link.attr("href").substring(link.attr("href").indexOf("=") + 1, link.attr("href").length()));
                            String currentUrl = link.attr("href").substring(link.attr("href").indexOf("?") + 1, link.attr("href").indexOf("="));
                            for (Element imgChild : link.children()) {
                                if (imgChild.attr("src").equalsIgnoreCase("/wpm/img/displaytag/last.gif")) {
                                    //save the page and the url
                                    pageNumbers = currentPage;
                                    url = currentUrl;
                                }
                            }
                        }

                        for (int z = 2; z <= pageNumbers; z++) {
                            saveWhereScriptsUsed(script, url + "=" + String.valueOf(z));
                        }
                    }
                }
            }
        }
    }

    private static String nullCheck(String string){
        if(string == null || string.equalsIgnoreCase("null")){
            return "";
        }else{
            return string;
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

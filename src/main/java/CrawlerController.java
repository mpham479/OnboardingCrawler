import freemarker.template.TemplateException;
import freemarker.template.Version;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;

public class CrawlerController {



    //create new webdriver instance
    public static WebDriver driver;
    public static Map<String,Script> scripts = new TreeMap<>();
    public static Map<String,CustomField> customFieldNames = new TreeMap<>();
    public static Map<String,CustomField> customFieldSystemIds = new TreeMap<>();
    public static Map<String,Workflow> workflowNames = new TreeMap<>();
    public static Map<String,Workflow> workflowSystemIds = new TreeMap<>();
    public static Map<String,CustomParam> customParams = new TreeMap<>();
    public static String chromeDriverLocation = System.getProperty("user.dir") + "\\chromedriver.exe";
    public static String baseUrl = "https://demo.webcomserver.com/wpm/";
    public static String username = "mpham";
    public static String password = "Welcome@1";
    public static String tenant = "cald_onboarding";
    public static Boolean fileBasedLinks = true;    //if false, changes links from "file:..." to articleUrl
    public static Boolean usedInClickHelp = true;
    public static String articleBaseUrl = "/articles/project-onboarding-standards/";
    public static String imgBaseUrl = "/resources/Storage/project-onboarding-standards/documentation/";
    public static String replaceSpacesInUrlsWith = "-";

    public CrawlerController(){

    }

    public static void main (String args[]) throws IOException, InterruptedException {

        //make documentation folder
        File folder = new File(System.getProperty("user.dir") + "\\documentation\\");
        FileUtils.deleteDirectory(folder);
        FileUtils.forceMkdir(folder);

        //add images
        File source = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\images");
        File dest = new File(System.getProperty("user.dir") + "\\documentation\\images");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //make workflow folder
        FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\workflows"));

        //make custom params folder
        FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\customparams"));

        //make custom fields folder
        FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\customfields"));

        //make scripts folder
        FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\scripts"));

        System.setProperty("webdriver.chrome.driver",chromeDriverLocation);
        driver  = new ChromeDriver();
        setupDriver();
        //crawl workflows
        WorkflowCrawler workflowCrawler = new WorkflowCrawler();
        workflowCrawler.startWorkflowCrawling();

        //crawl custom params
        CustomParamCrawler customParamCrawler = new CustomParamCrawler();
        customParamCrawler.startCustomParamCrawling();

        //crawl custom fields
        CustomFieldCrawler customFieldCrawler = new CustomFieldCrawler();
        //customFieldCrawler.startCustomFieldCrawling();

        //crawl scripts
        ScriptCrawler scriptCrawler = new ScriptCrawler();
        scriptCrawler.startScriptCrawling();

        driver.close();

        //fill out workflow script data
        for(Workflow workflow : workflowNames.values()){
            Map<String,Script> usesScripts = new HashMap(workflow.getUsesScripts());
            //go through list of scripts used by the workflow
            workflowIterator(workflow, usesScripts);
        }

        // 1. Configure FreeMarker
        //
        // You should do this ONLY ONCE, when your application starts,
        // then reuse the same Configuration object elsewhere.

        Configuration cfg = new Configuration();

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(CrawlerController.class, "/templates");

        // Some other recommended settings:
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setIncompatibleImprovements(new Version(2,3,20));

        // 2. Proccess template(s)
        //
        // You will do this for several times in typical applications.

        // 2.1. Prepare the template input:

        Map<String, Object> allWorkflowsInput = new HashMap<String,Object>();

        allWorkflowsInput.put("title", "Workflows");
        allWorkflowsInput.put("name", "Workflows");
        allWorkflowsInput.put("workflows",workflowNames);
        allWorkflowsInput.put("fileBasedLinks",fileBasedLinks);
        allWorkflowsInput.put("articleBaseUrl",articleBaseUrl);
        allWorkflowsInput.put("imgBaseUrl",imgBaseUrl);
        allWorkflowsInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
        allWorkflowsInput.put("usedInClickHelp",usedInClickHelp);

        Map<String, Object> allCustomParamsInput = new HashMap<String,Object>();

        allCustomParamsInput.put("title", "Custom Params");
        allCustomParamsInput.put("name", "Custom Params");
        allCustomParamsInput.put("customparams",customParams);
        allCustomParamsInput.put("fileBasedLinks",fileBasedLinks);
        allCustomParamsInput.put("articleBaseUrl",articleBaseUrl);
        allCustomParamsInput.put("imgBaseUrl",imgBaseUrl);
        allCustomParamsInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
        allCustomParamsInput.put("usedInClickHelp",usedInClickHelp);

        Map<String, Object> allCustomFieldsInput = new HashMap<String,Object>();

        allCustomFieldsInput.put("title", "Custom Fields");
        allCustomFieldsInput.put("name", "Custom Fields");
        allCustomFieldsInput.put("customfields",customFieldSystemIds);
        allCustomFieldsInput.put("fileBasedLinks",fileBasedLinks);
        allCustomFieldsInput.put("articleBaseUrl",articleBaseUrl);
        allCustomFieldsInput.put("imgBaseUrl",imgBaseUrl);
        allCustomFieldsInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
        allCustomFieldsInput.put("usedInClickHelp",usedInClickHelp);

        Map<String, Object> allScriptsInput = new HashMap<String, Object>();

        allScriptsInput.put("title", "Script List");
        allScriptsInput.put("name", "Scripts");
        allScriptsInput.put("scripts",scripts);
        allScriptsInput.put("fileBasedLinks",fileBasedLinks);
        allScriptsInput.put("articleBaseUrl",articleBaseUrl);
        allScriptsInput.put("imgBaseUrl",imgBaseUrl);
        allScriptsInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
        allScriptsInput.put("usedInClickHelp",usedInClickHelp);

        // 2.2. Get the template

        Template allWorkflowsTemplate = cfg.getTemplate("AllWorkflows.ftl");
        Template allCustomParamsTemplate = cfg.getTemplate("AllCustomParams.ftl");
        Template allScriptsTemplate = cfg.getTemplate("AllScripts.ftl");
        Template allCustomFieldsTemplate = cfg.getTemplate("AllCustomFields.ftl");
        Template customParamTemplate = cfg.getTemplate("CustomParam.ftl");
        Template scriptTemplate = cfg.getTemplate("Script.ftl");
        Template workflowTemplate = cfg.getTemplate("Workflow.ftl");
        Template customFieldTemplate = cfg.getTemplate("CustomField.ftl");

        //workflow file writer
        Writer workFlowListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflowList.html"));
        try {
            allWorkflowsTemplate.process(allWorkflowsInput, workFlowListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            workFlowListFileWriter.close();
        }

        //custom param file writer
        Writer customParamListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparamList.html"));
        try {
            allCustomParamsTemplate.process(allCustomParamsInput, customParamListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            customParamListFileWriter.close();
        }

        //custom field file writer
        Writer customFieldListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfieldList.html"));
        try {
            allCustomFieldsTemplate.process(allCustomFieldsInput, customFieldListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            customFieldListFileWriter.close();
        }

        //script file writer
        Writer scriptListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\scriptList.html"));
        try {
            allScriptsTemplate.process(allScriptsInput, scriptListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            scriptListFileWriter.close();
        }

        //get workflows file writer
        for(Map.Entry<String, Workflow> workflowEntrySet : workflowNames.entrySet()){

            Workflow workflow = workflowEntrySet.getValue();

            Map<String, Object> workflowInput = new HashMap<String, Object>();

            workflowInput.put("name", workflow.getName());
            workflowInput.put("systemId", workflow.getSystemId());
            workflowInput.put("description",workflow.getDescription());
            workflowInput.put("usesScripts",workflow.getUsesScripts());
            workflowInput.put("usesCustomFields",workflow.getUsesCustomFields());

            workflowInput.put("fileBasedLinks",fileBasedLinks);
            workflowInput.put("articleBaseUrl",articleBaseUrl);
            workflowInput.put("imgBaseUrl",imgBaseUrl);
            workflowInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
            workflowInput.put("usedInClickHelp",usedInClickHelp);

            Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflows\\" + workflow.getSystemId() + ".html"));
            try {
                workflowTemplate.process(workflowInput, writer);
            } catch (TemplateException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
        }

        //get custom params file writer
        for(Map.Entry<String, CustomParam> cpMap : customParams.entrySet()){

            CustomParam cp = cpMap.getValue();

            System.out.println(cp.getName() + " : " + cp.getUsedInWorkflows());

            if(!StringUtils.isEmpty(cp.getName())){
                Map<String, Object> cpInput = new HashMap<String, Object>();

                cpInput.put("name", cp.getName());
                cpInput.put("type",cp.getType());
                cpInput.put("description",cp.getDescription());
                cpInput.put("usedByScripts",cp.getUsedByScripts());
                cpInput.put("usedInWorkflows",cp.getUsedInWorkflows());

                cpInput.put("fileBasedLinks",fileBasedLinks);
                cpInput.put("articleBaseUrl",articleBaseUrl);
                cpInput.put("imgBaseUrl",imgBaseUrl);
                cpInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
                cpInput.put("usedInClickHelp",usedInClickHelp);
                cpInput.put("workflowNames",workflowNames);
                cpInput.put("workflowSystemIds",workflowSystemIds);

                Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparams\\" + cp.getName() + ".html"));
                try {
                    customParamTemplate.process(cpInput, writer);
                } catch (TemplateException e) {
                    e.printStackTrace();
                } finally {
                    writer.close();
                }
            }
        }

        //get custom fields file writer
        for(Map.Entry<String, CustomField> cfMap : customFieldSystemIds.entrySet()){

            CustomField cf = cfMap.getValue();

            if(!StringUtils.isEmpty(cf.getName())){
                Map<String, Object> cfInput = new HashMap<String, Object>();

                cfInput.put("name", cf.getName());
                cfInput.put("systemId", cf.getSystemId());
                cfInput.put("type",cf.getType());
                cfInput.put("description",cf.getDescription());
                cfInput.put("label",cf.getLabel());
                cfInput.put("usedByScripts",cf.getUsedByScripts());
                cfInput.put("actionUsages",cf.getActionUsages());

                cfInput.put("fileBasedLinks",fileBasedLinks);
                cfInput.put("articleBaseUrl",articleBaseUrl);
                cfInput.put("imgBaseUrl",imgBaseUrl);
                cfInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
                cfInput.put("usedInClickHelp",usedInClickHelp);
                cfInput.put("workflowNames",workflowNames);
                cfInput.put("workflowSystemIds",workflowSystemIds);

                Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfields\\" + cf.getSystemId() + ".html"));
                try {
                    customFieldTemplate.process(cfInput, writer);
                } catch (TemplateException e) {
                    e.printStackTrace();
                } finally {
                    writer.close();
                }
            }
        }

        //get scripts file writer
        for(Map.Entry<String, Script> scriptMap : scripts.entrySet()){

            Script script = scriptMap.getValue();

            System.out.println(script.getName());

            Map<String, Object> scriptInput = new HashMap<String, Object>();

            scriptInput.put("name", script.getName());
            scriptInput.put("systemId", script.getSystemId());
            scriptInput.put("type",script.getType());
            scriptInput.put("description",script.getDescription());
            scriptInput.put("allScripts",scripts);
            scriptInput.put("usesScripts",script.getUsesScripts());
            scriptInput.put("usedByScripts",script.getUsedByScripts());
            scriptInput.put("usesCustomFields",script.getUsesCustomFields());
            scriptInput.put("actionUsages",script.getActionUsages());

            //format code for clickhelp
            if(script.getCode() != null && !fileBasedLinks && usedInClickHelp){
                scriptInput.put("code", script.getCode()
                        .replaceAll("&","&amp;")
                        .replaceAll("©","&copy;")
                        .replaceAll("\t","&#9;")
                        .replaceAll(">","&gt;")
                        .replaceAll("<","&lt;")
                        .replaceAll("\"","&quot;")
                        .replaceAll("\\$","&dollar;")
                        //.replaceAll("&dollar;\n&dollar;","")
                );
            }else{
                scriptInput.put("code", script.getCode());
            }

            scriptInput.put("fileBasedLinks",fileBasedLinks);
            scriptInput.put("articleBaseUrl",articleBaseUrl);
            scriptInput.put("imgBaseUrl",imgBaseUrl);
            scriptInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
            scriptInput.put("usedInClickHelp",usedInClickHelp);
            scriptInput.put("workflowNames",workflowNames);
            scriptInput.put("workflowSystemIds",workflowSystemIds);

            Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\scripts\\" + script.getSystemId() + ".html"));
            try {
                scriptTemplate.process(scriptInput, writer);
            } catch (TemplateException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
        }

    }

    public static void setupDriver(){
        //create new webdriver instance
        WebDriver driver = CrawlerController.driver;

        //go to url
        driver.get(baseUrl + "userHome.do?&tenantName=" + tenant);

        new WebDriverWait(driver,10).until(
                ExpectedConditions.textToBePresentInElementValue(
                        driver.findElement(By.name("requestedUrl")),
                         "/userHome.do?tenantName=" + tenant + "&"
                )
        );

        //set username and password
        driver.findElement(By.name("loginname")).sendKeys(username);
        ((JavascriptExecutor) driver).executeScript("document.getElementById(\"passwordInput\").value = '" + password + "'");
        //driver.findElement(By.name("password")).sendKeys(password);
        //driver.findElement(By.name("tenantName")).sendKeys(tenant);

        //click submit button when clickable
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.className("btn"))).click();

    }

    private static void workflowIterator(Workflow workflow, Map<String, Script> scripts){

        for(Map.Entry<String,Script> script : scripts.entrySet()){
            //add script to workflow
            workflow.addUsedScripts(script.getKey(),script.getValue());

            //get custom params
            for(Map.Entry<String, CustomParam> cp : script.getValue().getUsesCustomParams().entrySet()){
                //add custom field to workflow
                workflow.addUsesCustomParams(cp.getKey(),cp.getValue());

                //add workflow usage to custom fields
                cp.getValue().setUsedInWorkflows(workflow.getSystemId(),workflow);
            }

            //get custom fields
            for(Map.Entry<String, CustomField> cf : script.getValue().getUsesCustomFields().entrySet()){
                //add custom field to workflow
                workflow.addUsesCustomFields(cf.getKey(),cf.getValue());

                //add workflow usage to custom fields
                cf.getValue().setUsedInWorkflow(workflow.getSystemId(),workflow);
            }

            if(!script.getValue().usesScripts.isEmpty()){
                workflowIterator(workflow,script.getValue().usesScripts);
            }

        }
    }
}

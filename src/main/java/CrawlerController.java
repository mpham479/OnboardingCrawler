import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import freemarker.template.TemplateException;
import freemarker.template.Version;
import javafx.scene.layout.Border;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

@SuppressWarnings("Duplicates")
public class CrawlerController {
    //create new webdriver instance
    public static WebDriver driver;
    public static Map<String,Script> scripts = new TreeMap<>();
    public static Map<String,CustomField> customFieldNames = new TreeMap<>();
    public static Map<String,CustomField> customFieldSystemIds = new TreeMap<>();
    public static Map<String,Workflow> workflowNames = new TreeMap<>();
    public static Map<String,Workflow> workflowSystemIds = new TreeMap<>();
    public static Map<String,CustomParam> customParams = new TreeMap<>();
    public static String chromeDriverLocation = System.getProperty("user.dir") + "\\src\\main\\resources\\driver\\chromedriver.exe";
    public static String baseUrl = "https://demo.webcomserver.com/wpm/";
    public static String username = "mpham";
    public static String password = "Welcome@1";
    public static String tenant = "cald_onboarding";
    public static String saveDirectory = System.getProperty("user.dir");
    public static Boolean fileBasedLinks = true;    //if false, changes links from "file:..." to articleUrl
    public static Boolean usedInClickHelp = true;
    public static String articleBaseUrl = "/articles/project-onboarding-standards/";
    public static String imgBaseUrl = "/resources/Storage/project-onboarding-standards/documentation/";
    public static String replaceSpacesInUrlsWith = "-";

    public static JFrame frame;
    public static JFrame progressFrame;

    public CrawlerController() throws IOException {
        //check if settings.txt exists
        File settings = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt");
        if(settings.exists()){
            //go through it
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                //add to variables
                String variable = line.split("=")[0];
                String value = line.split("=")[1];

                switch (variable.toUpperCase()){
                    case "BASEURL":
                        baseUrl = value;
                        break;
                    case "USERNAME":
                        username = value;
                        break;
                    case "PASSWORD":
                        password = value;
                        break;
                    case "TENANT":
                        tenant = value;
                        break;
                    case "SAVEDIRECTORY":
                        saveDirectory = value;
                        break;
                    case "FILEBASEDLINKS":
                        fileBasedLinks = Boolean.valueOf(value);
                        break;
                    case "USEDINCLICKHELP":
                        usedInClickHelp = Boolean.valueOf(value);
                        break;
                    case "ARTICLEBASEURL":
                        articleBaseUrl = value;
                        break;
                    case "IMGBASEURL":
                        imgBaseUrl = value;
                        break;
                }
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() throws IOException {
        CrawlerGatherData data = new CrawlerGatherData();

        //Create and set up the window.
        frame = new JFrame("Documentation Crawler Data");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        //frame.setSize(width/4, height/2);
        frame.pack();
        frame.setMinimumSize(new Dimension(500,480));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e) {
                driver.close();
            }
        });

        data.chooseSaveDirectoryTextField.setUI(new JTextFieldHintUI("Choose Save Directory", Color.gray));

        /*Set up field actions*/
        //file chooser
        data.chooseButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(!data.chooseSaveDirectoryTextField.getText().isEmpty()){
                    jFileChooser.setCurrentDirectory(new File(data.chooseSaveDirectoryTextField.getText()));
                }
                int returnVal = jFileChooser.showOpenDialog((Component)e.getSource());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = jFileChooser.getSelectedFile();
                    try {
                        String fileName = file.toString();
                        data.chooseSaveDirectoryTextField.setText(fileName);
                        fieldCheckerData(data);

                    } catch (Exception ex) {
                        System.out.println("problem accessing file "+file.getAbsolutePath());
                    }
                }
                else {
                    System.out.println("File access cancelled by user.");
                }
            }
        });
        data.chooseButton.setText("Choose...");

        //add yes/no actions
        data.fileReferenceYes.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.linkReferencesPanel.setVisible(false);

                fieldCheckerData(data);
            }
        });

        data.fileReferenceYes.setText("Yes");
        data.fileReferenceYes.setFocusPainted(false);

        data.fileReferenceNo.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.linkReferencesPanel.setVisible(true);

                fieldCheckerData(data);
            }
        });
        data.fileReferenceNo.setText("No");
        data.fileReferenceNo.setFocusPainted(false);

        //add yes/no actions
        data.clickHelpYes.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fieldCheckerData(data);
            }
        });
        data.clickHelpYes.setText("Yes");
        data.clickHelpYes.setFocusPainted(false);

        data.clickHelpNo.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fieldCheckerData(data);
            }
        });
        data.clickHelpNo.setText("No");
        data.clickHelpNo.setFocusPainted(false);

        //document listener to run field checker
        DocumentListener dL = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fieldCheckerData(data);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fieldCheckerData(data);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fieldCheckerData(data);
            }
        };

        //add document listener to fields
        data.urlInput.getDocument().addDocumentListener(dL);
        data.usernameInput.getDocument().addDocumentListener(dL);
        data.passwordInput.getDocument().addDocumentListener(dL);
        data.tenantInput.getDocument().addDocumentListener(dL);
        data.chooseSaveDirectoryTextField.getDocument().addDocumentListener(dL);
        data.articleUrlInput.getDocument().addDocumentListener(dL);
        data.imageUrlInput.getDocument().addDocumentListener(dL);

        //add submit button click
        data.submitButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //set data
                baseUrl = data.urlInput.getText();
                username = data.usernameInput.getText();
                password = data.passwordInput.getText();
                tenant = data.tenantInput.getText();
                saveDirectory = data.chooseSaveDirectoryTextField.getText();
                fileBasedLinks = data.fileReferenceYes.isSelected();
                usedInClickHelp = data.clickHelpYes.isSelected();
                articleBaseUrl = data.articleUrlInput.getText();
                imgBaseUrl = data.imageUrlInput.getText();

                //jframe to use specified class
                CrawlerProgressData progressData = new CrawlerProgressData();

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            startCrawling(progressData);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }).start();

                //open new frame, close this one
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //Turn off metal's use of bold fonts
                        UIManager.put("swing.boldMetal", Boolean.FALSE);
                        try {
                            frame.setVisible(false);
                            createAndShowProgressGUI(progressData);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        data.submitButton.setText("Submit");

        //set up initial data
        data.urlInput.setText(baseUrl);
        data.usernameInput.setText(username);
        data.passwordInput.setText(password);
        data.tenantInput.setText(tenant);
        data.chooseSaveDirectoryTextField.setText(saveDirectory);

        if(fileBasedLinks != null){
            data.fileReferenceYes.setSelected(fileBasedLinks);
            data.fileReferenceNo.setSelected(!fileBasedLinks);
        }

        if(usedInClickHelp != null){
            data.clickHelpYes.setSelected(usedInClickHelp);
            data.clickHelpYes.setSelected(!usedInClickHelp);
        }

        data.articleUrlInput.setText(articleBaseUrl);
        data.imageUrlInput.setText(imgBaseUrl);

        if(data.fileReferenceYes.isSelected() || fileBasedLinks == null){
            //hide contents
            data.linkReferencesPanel.setVisible(false);
        }else{
            //show contents
            data.linkReferencesPanel.setVisible(true);
        }



        //Add content to the window.
        frame.add(data.mainPanel);

        //Display the window.
        frame.setVisible(true);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowProgressGUI(CrawlerProgressData data) throws IOException {

        //Create and set up the window.
        progressFrame = new JFrame("Documentation Crawler Progression");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        //frame.setSize(width/4, height/2);
        progressFrame.pack();
        progressFrame.setMinimumSize(new Dimension(500, 480));
        progressFrame.setLocationRelativeTo(null);
        progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        progressFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                driver.close();
            }
        });

        //set progress data
        data.workflowProgress.setValue(0);
        data.workflowProgress.setString("Not started yet");
        data.workflowProgress.setStringPainted(true);
        data.customParamProgress.setValue(0);
        data.customParamProgress.setString("Not started yet");
        data.customParamProgress.setStringPainted(true);
        data.customFieldProgress.setValue(0);
        data.customFieldProgress.setString("Not started yet");
        data.customFieldProgress.setStringPainted(true);
        data.scriptProgress.setValue(0);
        data.scriptProgress.setString("Not started yet");
        data.scriptProgress.setStringPainted(true);

        data.processes.setEnabledAt(1, false);  //custom params
        data.processes.setEnabledAt(2, false);  //custom fields
        data.processes.setEnabledAt(3, false);  //scripts

        progressFrame.add(data.crawlerProgress);
        progressFrame.setVisible(true);

    }

    public static void main (String args[]){

        //close previous instances
        try {
            Process p = Runtime.getRuntime().exec("cmd.exe /c Start /b " + System.getProperty("user.dir") + "/src/main/resources/driver/killChromeDriver.bat");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                try {
                    createAndShowGUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void startCrawling(CrawlerProgressData progressData) throws IOException, InterruptedException {
        //make documentation folder
        //File folder = new File(System.getProperty("user.dir") + "\\documentation\\");
        File folder = new File(saveDirectory + "\\Onboarding Documentation\\");
        FileUtils.deleteDirectory(folder);
        FileUtils.forceMkdir(folder);

        //add images
        File source = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\images");
        //File dest = new File(System.getProperty("user.dir") + "\\documentation\\images");
        File dest = new File(saveDirectory + "\\Onboarding Documentation\\images");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //make workflow folder
        //FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\workflows"));
        FileUtils.forceMkdir(new File(saveDirectory + "\\Onboarding Documentation\\workflows"));

        //make custom params folder
        //FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\customparams"));
        FileUtils.forceMkdir(new File(saveDirectory + "\\Onboarding Documentation\\customparams"));

        //make custom fields folder
        //FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\customfields"));
        FileUtils.forceMkdir(new File(saveDirectory + "\\Onboarding Documentation\\customfields"));

        //make scripts folder
        //FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\scripts"));
        FileUtils.forceMkdir(new File(saveDirectory + "\\Onboarding Documentation\\scripts"));

        try {

            driver = new ChromeWebDriver().setupDriver();

            //crawl workflows
            WorkflowCrawler workflowCrawler = new WorkflowCrawler();
            workflowCrawler.startWorkflowCrawling(progressData);

            //crawl custom params
            CustomParamCrawler customParamCrawler = new CustomParamCrawler();
            customParamCrawler.startCustomParamCrawling(progressData);

            //crawl custom fields
            CustomFieldCrawler customFieldCrawler = new CustomFieldCrawler();
            customFieldCrawler.startCustomFieldCrawling(progressData);

            //crawl scripts
            ScriptCrawler scriptCrawler = new ScriptCrawler();
            scriptCrawler.startScriptCrawling(progressData);

            driver.close();
        }catch(Exception e1){
            //close everything and log errors
            try{
                driver.close();
            }catch(Exception e2){

            }
            e1.printStackTrace();
            System.exit(0);
        }

        //successfully crawled designated url
        //save settings to file
        PrintWriter writeText = new PrintWriter(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt","UTF-8");
        writeText.println("baseUrl=" + baseUrl);
        writeText.println("username=" + username);
        writeText.println("password=" + password);
        writeText.println("tenant=" + tenant);
        writeText.println("saveDirectory=" + saveDirectory);
        writeText.println("fileBasedLinks=" + String.valueOf(fileBasedLinks));
        writeText.println("usedInClickHelp=" + String.valueOf(usedInClickHelp));
        writeText.println("articleBaseUrl=" + articleBaseUrl);
        writeText.println("imgBaseUrl=" + imgBaseUrl);
        //writeText.println("replaceSpacesInUrlsWith=" + replaceSpacesInUrlsWith);
        writeText.close();

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
        //Writer workFlowListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflowList.html"));
        Writer workFlowListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\workflowList.html"));
        try {
            allWorkflowsTemplate.process(allWorkflowsInput, workFlowListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            workFlowListFileWriter.close();
        }

        //custom param file writer
        //Writer customParamListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparamList.html"));
        Writer customParamListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customparamList.html"));
        try {
            allCustomParamsTemplate.process(allCustomParamsInput, customParamListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            customParamListFileWriter.close();
        }

        //custom field file writer
        //Writer customFieldListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfieldList.html"));
        Writer customFieldListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customfieldList.html"));
        try {
            allCustomFieldsTemplate.process(allCustomFieldsInput, customFieldListFileWriter);
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            customFieldListFileWriter.close();
        }

        //script file writer
        //Writer scriptListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\scriptList.html"));
        Writer scriptListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\scriptList.html"));
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
            workflowInput.put("usesCustomParams",workflow.getUsesCustomParams());

            workflowInput.put("fileBasedLinks",fileBasedLinks);
            workflowInput.put("articleBaseUrl",articleBaseUrl);
            workflowInput.put("imgBaseUrl",imgBaseUrl);
            workflowInput.put("replaceSpacesInUrlsWith",replaceSpacesInUrlsWith);
            workflowInput.put("usedInClickHelp",usedInClickHelp);

            //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflows\\" + workflow.getSystemId() + ".html"));
            Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\workflows\\" + workflow.getSystemId() + ".html"));
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

                //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparams\\" + cp.getName() + ".html"));
                Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customparams\\" + cp.getName() + ".html"));
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

                //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfields\\" + cf.getSystemId() + ".html"));
                Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customfields\\" + cf.getSystemId() + ".html"));
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
            scriptInput.put("usesCustomParams",script.getUsesCustomParams());

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

            //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\scripts\\" + script.getSystemId() + ".html"));
            Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\scripts\\" + script.getSystemId() + ".html"));
            try {
                scriptTemplate.process(scriptInput, writer);
            } catch (TemplateException e) {
                e.printStackTrace();
            } finally {
                writer.close();
            }
        }

        //exit system
        System.exit(0);
    }

    private static void setupDriver(){



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

    private static void fieldCheckerData(CrawlerGatherData crawlerGatherData){
        //set button to not enabled
        crawlerGatherData.submitButton.setEnabled(false);

        Map<String, JTextField> textFields = new HashMap<>();
        textFields.put("urlInput",crawlerGatherData.urlInput);
        textFields.put("usernameInput", crawlerGatherData.usernameInput);
        textFields.put("passwordInput", crawlerGatherData.passwordInput);
        textFields.put("tenantInput", crawlerGatherData.tenantInput);
        textFields.put("chooseSaveDirectoryTextField", crawlerGatherData.chooseSaveDirectoryTextField);
        textFields.put("articleUrlInput", crawlerGatherData.articleUrlInput);
        textFields.put("imageUrlInput", crawlerGatherData.imageUrlInput);

        Map<String, JRadioButton> buttons = new HashMap<>();
        buttons.put("fileReferenceYes", crawlerGatherData.fileReferenceYes);
        buttons.put("fileReferenceNo", crawlerGatherData.fileReferenceNo);
        buttons.put("clickHelpYes", crawlerGatherData.clickHelpYes);
        buttons.put("clickHelpNo", crawlerGatherData.clickHelpNo);

        Boolean checkForLinks = false;

        if(buttons.get("fileReferenceYes").isSelected()){
            checkForLinks = false;
        }else if(buttons.get("fileReferenceNo").isSelected()){
            checkForLinks = true;

            //check clickhelp buttons
            if(!buttons.get("clickHelpYes").isSelected() && !buttons.get("clickHelpNo").isSelected()){
                return;
            }
        }else{
            //nothing was selected
            return;
        }

        //go through all text components
        for(Map.Entry<String, JTextField> textField : textFields.entrySet()){

            //check if it should be checked
            if(checkForLinks){
                //check everything
                if(textField.getValue().getText().isEmpty()){
                    //field missing, don't do anything
                    return;
                }
            }else{
                //skip url stuff
                if(!textField.getKey().equalsIgnoreCase("articleUrlInput") && !textField.getKey().equalsIgnoreCase("imageUrlInput")){
                    //check everything
                    if(textField.getValue().getText().isEmpty()){
                        //field missing, don't do anything
                        return;
                    }
                }
            }
        }

        //enable submit button because we got to the end
        crawlerGatherData.submitButton.setEnabled(true);
    }

    public static class JTextFieldHintUI extends BasicTextFieldUI implements FocusListener {
        private String hint;
        private Color  hintColor;

        public JTextFieldHintUI(String hint, Color hintColor) {
            this.hint = hint;
            this.hintColor = hintColor;
        }

        private void repaint() {
            if (getComponent() != null) {
                getComponent().repaint();
            }
        }

        @Override
        protected void paintSafely(Graphics g) {
            // Render the default text field UI
            super.paintSafely(g);
            // Render the hint text
            JTextComponent component = getComponent();
            if (component.getText().length() == 0 && !component.hasFocus()) {
                g.setColor(hintColor);
                int padding = (component.getHeight() - component.getFont().getSize()) / 2;
                int inset = 3;
                g.drawString(hint, inset, component.getHeight() - padding - inset);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            repaint();
        }

        @Override
        public void installListeners() {
            super.installListeners();
            getComponent().addFocusListener(this);
        }

        @Override
        public void uninstallListeners() {
            super.uninstallListeners();
            getComponent().removeFocusListener(this);
        }
    }
}



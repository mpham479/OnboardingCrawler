import javax.imageio.ImageIO;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

@SuppressWarnings("Duplicates")
public class CrawlerController {
  //create new webdriver instance
  public static Integer totalScriptLines = 0;
  public static Map<String,Script> scripts = new TreeMap<>();
  public static Map<String,CustomField> customFieldNames = new TreeMap<>();
  public static Map<String,CustomField> customFieldSystemIds = new TreeMap<>();
  public static Map<String,Workflow> workflowNames = new TreeMap<>();
  public static Map<String,Workflow> workflowSystemIds = new TreeMap<>();
  public static Map<String,CustomParam> customParams = new TreeMap<>();
  public static Map<String, Form> forms = new TreeMap<>();
  public static String chromeDriverLocation = System.getProperty("user.dir") + "\\src\\main\\resources\\driver\\chromedriver.exe";
  public static String baseUrl;
  public static String username;
  public static String password;
  public static String tenant;
  public static String saveDirectory;
  public static Boolean fileBasedLinks;    //if false, changes links from "file:..." to articleUrl
  public static Boolean usedInClickHelp;
  public static String articleBaseUrl = "/articles/project-onboarding-standards/";
  public static String imgBaseUrl = "/resources/Storage/project-onboarding-standards/documentation/";
  public static String baseExt = "aspx";
  public static String replaceSpacesInUrlsWith = "-";
  public static WorkflowCrawler workflowCrawler;
  public static CustomParamCrawler customParamCrawler;
  public static CustomFieldCrawler customFieldCrawler;
  public static ScriptCrawler scriptCrawler;
  public static FormCrawler formCrawler;
  public static Boolean interrupted = false;

  public static JFrame frame;
  public static JFrame progressFrame;

  public CrawlerController() throws IOException {
  }

  public static void populateSettingsData() throws IOException {
    //check if settings.txt exists
    File settings = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt");
    if(settings.exists()){
      //go through it
      BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        try{
          //add to variables
          String variable = line.split("=")[0];
          String value = line.split("=")[1];

          switch (variable.toUpperCase()) {
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
            case "BASEEXT":
              baseExt = value;
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
        } catch (Exception e) {

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
    populateSettingsData();

    CrawlerGatherData data = new CrawlerGatherData();

    //Create and set up the window.
    frame = new JFrame("Documentation Crawler Data (BETA)");
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int height = screenSize.height;
    int width = screenSize.width;
    //frame.setSize(width/4, height/2);
    frame.pack();
    frame.setMinimumSize(new Dimension(500,600));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        } else {
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
    data.extInput.getDocument().addDocumentListener(dL);
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
        password = String.valueOf(data.passwordInput.getPassword());
        //password = data.passwordInput.getText();
        tenant = data.tenantInput.getText();
        baseExt = data.extInput.getText();
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
    data.extInput.setText(baseExt);
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
    progressFrame = new JFrame("Documentation Crawler Progression (BETA)");
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
        try {
          interrupted = true;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
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
    data.formProgress.setValue(0);
    data.formProgress.setString("Not started yet");
    data.formProgress.setStringPainted(true);

    //remove highlight on tab selection
    data.processes.setFocusable(false);

    //loading image
    ImageIcon image = new ImageIcon(System.getProperty("user.dir") + "\\src\\main\\resources\\images\\catLoading2.gif");

    data.workflowProgressPanel.setVisible(false);
    data.workflowLoadingPanel.setVisible(true);
    data.workflowLoadingImage.add(new JLabel(image,JLabel.CENTER));

    data.customParamProgressPanel.setVisible(false);
    data.customParamLoadingPanel.setVisible(true);
    data.customParamLoadingImage.add(new JLabel(image,JLabel.CENTER));

    data.customFieldProgressPanel.setVisible(false);
    data.customFieldLoadingPanel.setVisible(true);
    data.customFieldLoadingImage.add(new JLabel(image,JLabel.CENTER));

    data.scriptProgressPanel.setVisible(false);
    data.scriptLoadingPanel.setVisible(true);
    data.scriptLoadingImage.add(new JLabel(image,JLabel.CENTER));

    data.formProgressPanel.setVisible(false);
    data.formLoadingPanel.setVisible(true);
    data.formLoadingImage.add(new JLabel(image, JLabel.CENTER));

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
          //createAndShowProgressGUI(new CrawlerProgressData());
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

    //make forms folder
    //FileUtils.forceMkdir(new File(System.getProperty("user.dir")+ "\\documentation\\forms"));
    FileUtils.forceMkdir(new File(saveDirectory + "\\Onboarding Documentation\\forms"));

    workflowCrawler = new WorkflowCrawler();
    customParamCrawler = new CustomParamCrawler();
    customFieldCrawler = new CustomFieldCrawler();
    scriptCrawler = new ScriptCrawler();
    formCrawler = new FormCrawler();

    try {
      ExecutorService es = Executors.newCachedThreadPool();

      //crawl workflows
      es.execute(new Runnable() {
        public void run() {

          try {
            workflowCrawler.startWorkflowCrawling(progressData);
          }catch(InterruptedException ie){

            Highlighter highlighter = progressData.workflowList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.workflowList.getText().length();

            String errorMessage = "There was an error in another tab. Overall process will stop.";

            //set error message
            progressData.workflowList.append(errorMessage);

            try {
              highlighter.addHighlight(currentIndex,currentIndex + errorMessage.length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.workflowLoadingPanel.setVisible(false);
            progressData.workflowProgressPanel.setVisible(true);
          }catch (Exception e1) {
            //set interrupted true
            interrupted = true;

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.workflowData),Color.RED);

            Highlighter highlighter = progressData.workflowList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.workflowList.getText().length();

            //set error message
            progressData.workflowList.append(e1.getMessage());

            try {
              highlighter.addHighlight(currentIndex,currentIndex + e1.getMessage().length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.workflowLoadingPanel.setVisible(false);
            progressData.workflowProgressPanel.setVisible(true);
          }
        }
      });

      //crawl custom fields
      es.execute(new Runnable() {
        public void run() {
          try {
            customFieldCrawler.startCustomFieldCrawling(progressData);
          }catch(InterruptedException ie){
            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.customFieldData),Color.RED);

            Highlighter highlighter = progressData.customFieldList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.customFieldList.getText().length();

            String errorMessage = "There was an error in another tab. Overall process will stop.";

            //set error message
            progressData.customFieldList.append(errorMessage);

            try {
              highlighter.addHighlight(currentIndex,currentIndex + errorMessage.length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.customFieldLoadingPanel.setVisible(false);
            progressData.customFieldProgressPanel.setVisible(true);
          }catch (Exception e1) {
            //set interrupted true
            interrupted = true;

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.customFieldData),Color.RED);

            Highlighter highlighter = progressData.customFieldList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.customFieldList.getText().length();

            //set error message
            progressData.customFieldList.append(e1.getMessage());

            try {
              highlighter.addHighlight(currentIndex,currentIndex + e1.getMessage().length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.customFieldLoadingPanel.setVisible(false);
            progressData.customFieldProgressPanel.setVisible(true);
          }
        }
      });

      //crawl scripts
      es.execute(new Runnable() {
        public void run() {
          try {
            scriptCrawler.startScriptCrawling(progressData);
          }catch(InterruptedException ie){

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.scriptData),Color.RED);

            Highlighter highlighter = progressData.scriptList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.scriptList.getText().length();

            String errorMessage = "There was an error in another tab. Overall process will stop.";

            //set error message
            progressData.scriptList.append(errorMessage);

            try {
              highlighter.addHighlight(currentIndex,currentIndex + errorMessage.length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.scriptLoadingPanel.setVisible(false);
            progressData.scriptProgressPanel.setVisible(true);
          }catch (Exception e1) {
            //set interrupted true
            interrupted = true;

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.scriptData),Color.RED);

            Highlighter highlighter = progressData.scriptList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.scriptList.getText().length();

            //set error message
            progressData.scriptList.append(e1.getMessage());

            try {
              highlighter.addHighlight(currentIndex,currentIndex + e1.getMessage().length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.scriptLoadingPanel.setVisible(false);
            progressData.scriptProgressPanel.setVisible(true);
          }
        }
      });

      //crawl custom params
      es.execute(new Runnable() {
        public void run() {
          try {
            customParamCrawler.startCustomParamCrawling(progressData);
          }catch(InterruptedException ie){

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.customParamData),Color.RED);

            Highlighter highlighter = progressData.customParamList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.customParamList.getText().length();

            String errorMessage = "There was an error in another tab. Overall process will stop.";

            //set error message
            progressData.customParamList.append(errorMessage);

            try {
              highlighter.addHighlight(currentIndex,currentIndex + errorMessage.length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.customParamLoadingPanel.setVisible(false);
            progressData.customParamProgressPanel.setVisible(true);
          }catch (Exception e1) {
            //set interrupted true
            interrupted = true;

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.customParamData),Color.RED);

            Highlighter highlighter = progressData.customParamList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.customParamList.getText().length();

            //set error message
            progressData.customParamList.append(e1.getMessage());

            try {
              highlighter.addHighlight(currentIndex,currentIndex + e1.getMessage().length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.customParamLoadingPanel.setVisible(false);
            progressData.customParamProgressPanel.setVisible(true);
          }
        }
      });

      //crawl forms
      es.execute(new Runnable() {
        public void run() {
          //highlight errored tab
          progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.formData),Color.RED);

          Highlighter highlighter = progressData.formList.getHighlighter();
          Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
          int currentIndex = progressData.formList.getText().length();

          String msg = "Form documenting disabled due to Workflow changes.";
          progressData.formList.append(msg);

          try {
            highlighter.addHighlight(currentIndex, currentIndex + msg.length(), painter);
          } catch (BadLocationException e) {
            e.printStackTrace();
          }

          //hide loading, show progress list since this is where the error will be
          progressData.formLoadingPanel.setVisible(false);
          progressData.formProgressPanel.setVisible(true);
        }
      });

      /*
      //crawl forms
      es.execute(new Runnable() {
        public void run() {
          try {
            formCrawler.startFormCrawling(progressData);
          } catch (InterruptedException ie) {

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.formData), Color.RED);

            Highlighter highlighter = progressData.formList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.formList.getText().length();

            String errorMessage = "There was an error in another tab. Overall process will stop.";

            //set error message
            progressData.formList.append(errorMessage);

            try {
              highlighter.addHighlight(currentIndex, currentIndex + errorMessage.length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.formLoadingPanel.setVisible(false);
            progressData.formProgressPanel.setVisible(true);
          } catch (Exception e1) {
            //set interrupted true
            interrupted = true;

            //highlight errored tab
            progressData.processes.setBackgroundAt(progressData.processes.indexOfComponent(progressData.formData), Color.RED);

            Highlighter highlighter = progressData.formList.getHighlighter();
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
            int currentIndex = progressData.formList.getText().length();

            //set error message
            progressData.formList.append(e1.getMessage());

            try {
              highlighter.addHighlight(currentIndex, currentIndex + e1.getMessage().length(), painter);
            } catch (BadLocationException e) {
              e.printStackTrace();
            }

            //hide loading, show progress list since this is where the error will be
            progressData.formLoadingPanel.setVisible(false);
            progressData.formProgressPanel.setVisible(true);
          }
        }
      });*/

      es.shutdown();
      boolean finished = es.awaitTermination(30, TimeUnit.MINUTES);

      if (finished && !interrupted) {
        //successfully crawled designated url
        //save settings to file
        PrintWriter writeText = new PrintWriter(System.getProperty("user.dir") + "\\src\\main\\resources\\settings\\settings.txt", "UTF-8");
        writeText.println("baseUrl=" + baseUrl);
        writeText.println("username=" + username);
        writeText.println("password=" + password);
        writeText.println("tenant=" + tenant);
        writeText.println("saveDirectory=" + saveDirectory);
        writeText.println("fileBasedLinks=" + String.valueOf(fileBasedLinks));
        writeText.println("usedInClickHelp=" + String.valueOf(usedInClickHelp));
        writeText.println("articleBaseUrl=" + articleBaseUrl);
        writeText.println("imgBaseUrl=" + imgBaseUrl);
        writeText.println("baseExt=" + baseExt);
        //writeText.println("replaceSpacesInUrlsWith=" + replaceSpacesInUrlsWith);
        writeText.close();

        //fill out custom field data
        for(Map.Entry<String, CustomField> cfMap : customFieldNames.entrySet()){
          CustomField cf = cfMap.getValue();
          HashMap<String, Workflow> wfList = new HashMap<>(cf.getUsedInWorkflow());
          //go through workflows
          for(Map.Entry<String, Workflow> wfMap : wfList.entrySet()){
            //check if workflow exists
            if(workflowNames.containsKey(wfMap.getKey())){
              //get workflow
              Workflow wf = workflowNames.get(wfMap.getKey());

              //add to custom field workflows list
              cf.addUsedInWorkflow(wfMap.getKey(),wf);

              //add to workflow custom field list
              wf.addUsesCustomFields(cf.getSystemId(),cf);
            }else{
              //remove from custom field workflow list
              cf.removeUsedInWorkflow(wfMap.getKey());
            }


          }
        }

        //fill out forms data
        for (Map.Entry<String, Form> formMap : forms.entrySet()) {
          Form form = formMap.getValue();

          //go through workflows
          HashMap<String, Workflow> wfList = new HashMap<>(form.getUsedInWorkflows());
          for (Map.Entry<String, Workflow> wfMap : wfList.entrySet()) {
            //check if workflow exists
            if (workflowNames.containsKey(wfMap.getKey())) {
              //get workflow
              Workflow wf = workflowNames.get(wfMap.getKey());

              //add to custom field workflows list
              form.addUsedInWorkflows(wfMap.getKey(), wf);

              //add to workflow custom field list
              wf.addUsesForms(form.getSystemid(), form);
            } else {
              //remove from custom field workflow list
              form.removeUsedInWorkflow(wfMap.getKey());
            }
          }

          //go through custom fields
          HashMap<String, CustomField> cfList = new HashMap<>(form.getUsesCustomFields());
          for (Map.Entry<String, CustomField> cfMap : cfList.entrySet()) {
            //check if custom field exists
            if (customFieldSystemIds.containsKey(cfMap.getKey())) {
              //get custom field
              CustomField cf = customFieldSystemIds.get(cfMap.getKey());

              //add to form custom field list
              form.addUsesCustomFields(cfMap.getKey(), cf);

              //add to custom field form list
              cf.addUsedInForms(form.getSystemid(), form);
            } else {
              //remove from form custom field list
              form.removeUsesCustomFields(cfMap.getKey());
            }
          }

          //add screenshots to images folder
          form.setScreenshotSrc(saveDirectory + "\\Onboarding Documentation\\images");

        }

        //fill out workflow script data
        for(Script script : scripts.values()) {
          //go through list of scripts
          scriptIterator(script);
        }

        buildDocumentation();
      }else{
        System.out.println("not finished");
      }

    }catch(Exception e1){
            /*
            //close everything and log errors
            try{
                try {
                    Process p = Runtime.getRuntime().exec("cmd.exe /c Start /b " + System.getProperty("user.dir") + "/src/main/resources/driver/killChromeDriver.bat");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch(Exception e2){

            }
            */
      //close all drivers
      closeAllDrivers();

      e1.printStackTrace();
    }
  }

  private static void buildDocumentation() throws IOException {
    // 1. Configure FreeMarker
    //
    // You should do this ONLY ONCE, when your application starts,
    // then reuse the same Configuration object elsewhere.

    Configuration cfg = new Configuration();

    // Where do we load the templates from:
    cfg.setDirectoryForTemplateLoading(new File(System.getProperty("user.dir") + "/src/main/resources/templates"));
    //cfg.setClassForTemplateLoading(CrawlerController.class, "/templates");

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
    allWorkflowsInput.put("workflows", workflowNames);
    allWorkflowsInput.put("fileBasedLinks", fileBasedLinks);
    allWorkflowsInput.put("articleBaseUrl", articleBaseUrl);
    allWorkflowsInput.put("imgBaseUrl", imgBaseUrl);
    allWorkflowsInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
    allWorkflowsInput.put("usedInClickHelp", usedInClickHelp);
    allWorkflowsInput.put("baseExt", baseExt);

    Map<String, Object> allCustomParamsInput = new HashMap<String,Object>();

    allCustomParamsInput.put("title", "Custom Params");
    allCustomParamsInput.put("name", "Custom Params");
    allCustomParamsInput.put("customparams", customParams);
    allCustomParamsInput.put("fileBasedLinks", fileBasedLinks);
    allCustomParamsInput.put("articleBaseUrl", articleBaseUrl);
    allCustomParamsInput.put("imgBaseUrl", imgBaseUrl);
    allCustomParamsInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
    allCustomParamsInput.put("usedInClickHelp", usedInClickHelp);
    allCustomParamsInput.put("baseExt", baseExt);

    Map<String, Object> allCustomFieldsInput = new HashMap<String,Object>();

    allCustomFieldsInput.put("title", "Custom Fields");
    allCustomFieldsInput.put("name", "Custom Fields");
    allCustomFieldsInput.put("customfields", customFieldSystemIds);
    allCustomFieldsInput.put("fileBasedLinks", fileBasedLinks);
    allCustomFieldsInput.put("articleBaseUrl", articleBaseUrl);
    allCustomFieldsInput.put("imgBaseUrl", imgBaseUrl);
    allCustomFieldsInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
    allCustomFieldsInput.put("usedInClickHelp", usedInClickHelp);
    allCustomFieldsInput.put("baseExt", baseExt);

    Map<String, Object> allScriptsInput = new HashMap<String, Object>();

    allScriptsInput.put("title", "Script List");
    allScriptsInput.put("name", "Scripts");
    allScriptsInput.put("scripts", scripts);
    allScriptsInput.put("fileBasedLinks", fileBasedLinks);
    allScriptsInput.put("articleBaseUrl", articleBaseUrl);
    allScriptsInput.put("imgBaseUrl", imgBaseUrl);
    allScriptsInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
    allScriptsInput.put("usedInClickHelp", usedInClickHelp);
    allScriptsInput.put("baseExt", baseExt);

    Map<String, Object> allFormsInput = new HashMap<String, Object>();

    allFormsInput.put("title", "Form List");
    allFormsInput.put("name", "Forms");
    allFormsInput.put("forms", forms);
    allFormsInput.put("fileBasedLinks", fileBasedLinks);
    allFormsInput.put("articleBaseUrl", articleBaseUrl);
    allFormsInput.put("imgBaseUrl", imgBaseUrl);
    allFormsInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
    allFormsInput.put("usedInClickHelp", usedInClickHelp);
    allFormsInput.put("baseExt", baseExt);

    // 2.2. Get the template

    Template allWorkflowsTemplate = cfg.getTemplate("AllWorkflows.ftl");
    Template allCustomParamsTemplate = cfg.getTemplate("AllCustomParams.ftl");
    Template allScriptsTemplate = cfg.getTemplate("AllScripts.ftl");
    Template allCustomFieldsTemplate = cfg.getTemplate("AllCustomFields.ftl");
    Template allFormsTemplate = cfg.getTemplate("AllForms.ftl");
    Template customParamTemplate = cfg.getTemplate("CustomParam.ftl");
    Template scriptTemplate = cfg.getTemplate("Script.ftl");
    Template workflowTemplate = cfg.getTemplate("Workflow.ftl");
    Template customFieldTemplate = cfg.getTemplate("CustomField.ftl");
    Template formTemplate = cfg.getTemplate("Form.ftl");

    //workflow file writer
    //Writer workFlowListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflowList.html"));
    Writer workFlowListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\workflowList." + baseExt));
    try {
      allWorkflowsTemplate.process(allWorkflowsInput, workFlowListFileWriter);
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      workFlowListFileWriter.close();
    }

    //custom param file writer
    //Writer customParamListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparamList.html"));
    Writer customParamListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customparamList." + baseExt));
    try {
      allCustomParamsTemplate.process(allCustomParamsInput, customParamListFileWriter);
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      customParamListFileWriter.close();
    }

    //custom field file writer
    //Writer customFieldListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfieldList.html"));
    Writer customFieldListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customfieldList." + baseExt));
    try {
      allCustomFieldsTemplate.process(allCustomFieldsInput, customFieldListFileWriter);
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      customFieldListFileWriter.close();
    }

    //script file writer
    //Writer scriptListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\scriptList.html"));
    Writer scriptListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\scriptList." + baseExt));
    try {
      allScriptsTemplate.process(allScriptsInput, scriptListFileWriter);
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      scriptListFileWriter.close();
    }

    /*
    //form file writer
    //Writer formListFileWriter = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\formList.html"));
    Writer formListFileWriter = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\formList." + baseExt));
    try {
      allFormsTemplate.process(allFormsInput, formListFileWriter);
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      formListFileWriter.close();
    }
    */

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
      workflowInput.put("usesForms", workflow.getUsesForms());

      workflowInput.put("fileBasedLinks", fileBasedLinks);
      workflowInput.put("articleBaseUrl", articleBaseUrl);
      workflowInput.put("imgBaseUrl", imgBaseUrl);
      workflowInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
      workflowInput.put("usedInClickHelp", usedInClickHelp);
      workflowInput.put("baseExt", baseExt);

      //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\workflows\\" + workflow.getSystemId() + ".html"));
      Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\workflows\\" + workflow.getSystemId() + "." + baseExt));
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

      if(!StringUtils.isEmpty(cp.getName())){
        Map<String, Object> cpInput = new HashMap<String, Object>();

        cpInput.put("name", cp.getName());
        cpInput.put("type",cp.getType());
        cpInput.put("description",cp.getDescription());
        cpInput.put("usedByScripts",cp.getUsedByScripts());
        cpInput.put("usedInWorkflows",cp.getUsedInWorkflows());

        cpInput.put("fileBasedLinks", fileBasedLinks);
        cpInput.put("articleBaseUrl", articleBaseUrl);
        cpInput.put("imgBaseUrl", imgBaseUrl);
        cpInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
        cpInput.put("usedInClickHelp", usedInClickHelp);
        cpInput.put("workflowNames", workflowNames);
        cpInput.put("workflowSystemIds", workflowSystemIds);
        cpInput.put("baseExt", baseExt);

        //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customparams\\" + cp.getName() + ".html"));
        Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customparams\\" + cp.getName().replaceAll("\\.", "-") + "." + baseExt));
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
        cfInput.put("usedInWorkflow",cf.getUsedInWorkflow());
        cfInput.put("usedInForm", cf.getUsedInForms());

        cfInput.put("fileBasedLinks", fileBasedLinks);
        cfInput.put("articleBaseUrl", articleBaseUrl);
        cfInput.put("imgBaseUrl", imgBaseUrl);
        cfInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
        cfInput.put("usedInClickHelp", usedInClickHelp);
        cfInput.put("workflowNames", workflowNames);
        cfInput.put("workflowSystemIds", workflowSystemIds);
        cfInput.put("baseExt", baseExt);

        //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\customfields\\" + cf.getSystemId() + ".html"));
        Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\customfields\\" + cf.getSystemId() + "." + baseExt));
        try {
          customFieldTemplate.process(cfInput, writer);
        } catch (TemplateException e) {
          e.printStackTrace();
        } finally {
          writer.close();
        }
      }
    }

    System.out.println("Total script lines: " + totalScriptLines.toString() + " in " + scripts.entrySet().size() + " scripts");

    //get form file writer
    /*
    for (Map.Entry<String, Form> formMap : forms.entrySet()) {

      Form form = formMap.getValue();

      if (!StringUtils.isEmpty(form.getName())) {
        Map<String, Object> formInput = new HashMap<String, Object>();

        formInput.put("name", form.getName());
        formInput.put("systemid", form.getSystemid());
        formInput.put("description", form.getDescription());
        formInput.put("actionUsages", form.getActionUsages());
        formInput.put("usedInWorkflow", form.getUsedInWorkflows());
        formInput.put("usesCustomFields", form.getUsesCustomFields());
        formInput.put("formImage", form.getScreenshot().toString());

        formInput.put("fileBasedLinks", fileBasedLinks);
        formInput.put("articleBaseUrl", articleBaseUrl);
        formInput.put("imgBaseUrl", imgBaseUrl);
        formInput.put("replaceSpacesInUrlsWith", replaceSpacesInUrlsWith);
        formInput.put("usedInClickHelp", usedInClickHelp);
        formInput.put("workflowNames", workflowNames);
        formInput.put("workflowSystemIds", workflowSystemIds);
        formInput.put("baseExt", baseExt);

        //Writer writer = new FileWriter(new File(System.getProperty("user.dir") + "\\documentation\\forms\\" + form.getSystemid() + ".html"));
        Writer writer = new FileWriter(new File(saveDirectory + "\\Onboarding Documentation\\forms\\" + form.getSystemid() + "." + baseExt));
        try {
          formTemplate.process(formInput, writer);
        } catch (TemplateException e) {
          e.printStackTrace();
        } finally {
          writer.close();
        }
      }
    }
    */

    //exit system
    System.exit(0);
  }

  private static void scriptIterator(Script script){

    //list of everything
    HashMap<String, Workflow> tempWorkflowNames = new HashMap<>(script.getUsedInWorkflowName());
    HashMap<String, Workflow> tempWorkflowSysIds = new HashMap<>(script.getUsedInWorkflowSysId());
    HashMap<String, CustomParam> tempCustomParams = new HashMap<>(script.getUsesCustomParams());
    HashMap<String, CustomField> tempCustomFields = new HashMap<>(script.getUsesCustomFields());

    //add workflows
    for(Map.Entry<String, Workflow> tempWorkflow : tempWorkflowNames.entrySet()){
      //check if the workflow name is used
      if(workflowNames.containsKey(tempWorkflow.getKey())){
        //add to two lists
        script.addUsedInWorkflowName(workflowNames.get(tempWorkflow.getKey()).getName(),workflowNames.get(tempWorkflow.getKey()));
        script.addUsedInWorkflowSysId(workflowNames.get(tempWorkflow.getKey()).getSystemId(),workflowNames.get(tempWorkflow.getKey()));

        //add to workflow
        workflowNames.get(tempWorkflow.getKey()).addUsedScripts(script.getSystemId(),script);
      }else{
        script.removeUsedInWorkflowName(tempWorkflow.getKey());
      }
    }
    for(Map.Entry<String, Workflow> tempWorkflow : tempWorkflowSysIds.entrySet()){
      if(workflowSystemIds.containsKey(tempWorkflow.getKey())){
        //add to two lists
        script.addUsedInWorkflowName(workflowSystemIds.get(tempWorkflow.getKey()).getName(),workflowSystemIds.get(tempWorkflow.getKey()));
        script.addUsedInWorkflowSysId(workflowSystemIds.get(tempWorkflow.getKey()).getSystemId(),workflowSystemIds.get(tempWorkflow.getKey()));
      }else{
        script.removeUsedInWorkflowSysId(tempWorkflow.getKey());
      }
    }

    //update temp workflows
    tempWorkflowNames = new HashMap<>(script.getUsedInWorkflowName());
    tempWorkflowSysIds = new HashMap<>(script.getUsedInWorkflowSysId());

    //add custom params
    for(Map.Entry<String, CustomParam> tempCustomParam : tempCustomParams.entrySet()){
      if(customParams.containsKey(tempCustomParam.getKey())){
        //add to script
        script.addUsesCustomParams(customParams.get(tempCustomParam.getKey()).getName(),customParams.get(tempCustomParam.getKey()));

        //add to custom param
        customParams.get(tempCustomParam.getKey()).addUsedByScripts(script.getSystemId(),script);

        //add custom params to workflows and vice versa
        for(Map.Entry<String, Workflow> tempWorkflow : tempWorkflowSysIds.entrySet()){
          String wfSysId = tempWorkflow.getKey();
          if(workflowSystemIds.containsKey(tempWorkflow.getKey())){

            Workflow wf = workflowSystemIds.get(tempWorkflow.getKey());
            wf.addUsesCustomParams(tempCustomParam.getKey(),customParams.get(tempCustomParam.getKey()));
            customParams.get(tempCustomParam.getKey()).setUsedInWorkflows(wfSysId,wf);
          }
        }
      }else{
        script.removeUsesCustomParams(tempCustomParam.getKey());
      }
    }

    //add custom fields
    for(Map.Entry<String, CustomField> tempCustomField : tempCustomFields.entrySet()){

      if(customFieldSystemIds.containsKey(tempCustomField.getKey())){
        //add to script
        script.addUsesCustomFields(customFieldSystemIds.get(tempCustomField.getKey()).getSystemId(),customFieldSystemIds.get(tempCustomField.getKey()));

        //add to custom field
        customFieldSystemIds.get(tempCustomField.getKey()).addUsedByScripts(script.getSystemId(),script);

        //add custom params to workflows and vice versa
        for(Map.Entry<String, Workflow> tempWorkflow : tempWorkflowNames.entrySet()){
          String wfName = tempWorkflow.getKey();
          if(workflowNames.containsKey(tempWorkflow.getKey())){
            Workflow wf = workflowNames.get(tempWorkflow.getKey());
            wf.addUsesCustomFields(tempCustomField.getKey(),customFieldSystemIds.get(tempCustomField.getKey()));
            customFieldSystemIds.get(tempCustomField.getKey()).addUsedInWorkflow(wfName,wf);
          }
        }
      }else{
        script.removeUsesCustomFields(tempCustomField.getKey());
      }
    }
  }

  private static void fieldCheckerData(CrawlerGatherData crawlerGatherData){
    //set button to not enabled
    crawlerGatherData.submitButton.setEnabled(false);

    Map<String, JTextField> textFields = new HashMap<>();
    textFields.put("urlInput", crawlerGatherData.urlInput);
    textFields.put("usernameInput", crawlerGatherData.usernameInput);
    textFields.put("passwordInput", crawlerGatherData.passwordInput);
    textFields.put("tenantInput", crawlerGatherData.tenantInput);
    textFields.put("chooseSaveDirectoryTextField", crawlerGatherData.chooseSaveDirectoryTextField);
    textFields.put("articleUrlInput", crawlerGatherData.articleUrlInput);
    textFields.put("imageUrlInput", crawlerGatherData.imageUrlInput);
    textFields.put("baseExt", crawlerGatherData.extInput);

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

  private static void closeAllDrivers(){
    try{
      workflowCrawler.driver.close();
    }catch(Exception exception){

    }

    try{
      customParamCrawler.driver.close();
    }catch(Exception exception){

    }

    try{
      customFieldCrawler.driver.close();
    }catch(Exception exception){

    }

    try{
      scriptCrawler.driver.close();
    }catch(Exception exception){

    }
  }
}



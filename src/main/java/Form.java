import ru.yandex.qatools.ashot.Screenshot;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

enum FORMACTIONUSAGEHEADERS {
  Action("0"),
  Description("1"),
  Workflow("2"),
  StartStatus("3"),
  EndStatus("4");

  private static final Map<String, FORMACTIONUSAGEHEADERS> map = new HashMap<>(values().length, 1);

  static {
    for (FORMACTIONUSAGEHEADERS headers : values()) map.put(headers.header, headers);
  }

  private final String header;

  private FORMACTIONUSAGEHEADERS(String header) {
    this.header = header;
  }

  public static FORMACTIONUSAGEHEADERS of(String numeric) {
    FORMACTIONUSAGEHEADERS result = map.get(numeric);
    if (result == null) {
      throw new IllegalArgumentException("Invalid numeric type: " + numeric);
    }
    return result;
  }
}

public class Form {

  private String id;
  private String name;
  private String systemid;
  private String description;
  private Screenshot screenshot;
  private String screenshotSrc;
  private HashMap<String, CustomField> usesCustomFields = new HashMap<>();   //systemid, customfield
  private HashMap<String, Workflow> usedInWorkflows = new HashMap<>();   //name, workflow
  private HashSet<Map<String, String>> actionUsages = new HashSet<>();

  /**
   * Constructor
   */
  public Form() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSystemid() {
    return systemid;
  }

  public void setSystemid(String systemid) {
    this.systemid = systemid;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Screenshot getScreenshot() {
    return screenshot;
  }

  public void setScreenshot(Screenshot screenshot) {
    this.screenshot = screenshot;
  }

  public String getScreenshotSrc() {
    return screenshotSrc;
  }

  public void setScreenshotSrc(String imgSrcLocation) throws IOException {
    File imageFile = new File(imgSrcLocation + "\\" + this.getSystemid() + ".png");
    ImageIO.write(this.screenshot.getImage(), "PNG", imageFile);
    System.out.println(imageFile.getPath());
  }

  public String getScreenshotBase64() throws IOException {
    //encode screenshot
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(this.screenshot.getImage(), "PNG", baos);
    byte[] imageBytes = baos.toByteArray();
    String imageBase64 = new BASE64Encoder().encode(imageBytes);
    return imageBase64;
  }

  public HashMap<String, CustomField> getUsesCustomFields() {
    return usesCustomFields;
  }

  public void addUsesCustomFields(String customFieldSystemId, CustomField customField) {
    this.usesCustomFields.put(customFieldSystemId, customField);
  }

  public HashMap<String, Workflow> getUsedInWorkflows() {
    return usedInWorkflows;
  }

  public void addUsedInWorkflows(String workflowName, Workflow workflow) {
    this.usedInWorkflows.put(workflowName, workflow);
  }

  public HashSet<Map<String, String>> getActionUsages() {
    return actionUsages;
  }

  public void addActionUsages(Map<String, String> actionUsages) {
    this.actionUsages.add(actionUsages);
  }

  public void removeUsedInWorkflow(String wfName) {
    this.usedInWorkflows.remove(wfName);
  }

  public void removeUsesCustomFields(String cfSystemid) {
    this.usesCustomFields.remove(cfSystemid);
  }
}

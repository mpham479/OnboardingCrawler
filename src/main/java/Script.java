import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

enum SCRIPTTYPES{
    Workflow("1"),
    Async("9"),
    Scheduled_Task("3"),
    Form("6"),
    Unit_Test("7"),
    Utils("11"),
    Web_Action("5"),
    Exception("10");

    private static final Map<String, SCRIPTTYPES> map = new HashMap<>(values().length, 1);

    static {
        for (SCRIPTTYPES st : values()) map.put(st.type, st);
    }

    private final String type;

    private SCRIPTTYPES(String type) {
        this.type = type;
    }

    public static SCRIPTTYPES of(String numeric) {
        SCRIPTTYPES result = map.get(numeric);
        if (result == null) {
            throw new IllegalArgumentException("Invalid numeric type: " + numeric);
        }
        return result;
    }
}

enum FORMUSAGEHEADERS{
    ActionName("0"),
    ActionType("1"),
    Description("2"),
    Workflow("3"),
    StartStatus("4"),
    EndStatus("5"),
    WhenExecuted("6");

    private static final Map<String, FORMUSAGEHEADERS> map = new HashMap<>(values().length, 1);

    static {
        for (FORMUSAGEHEADERS headers : values()) map.put(headers.header, headers);
    }

    private final String header;

    private FORMUSAGEHEADERS(String header) {
        this.header = header;
    }

    public static FORMUSAGEHEADERS of(String numeric) {
        FORMUSAGEHEADERS result = map.get(numeric);
        if (result == null) {
            throw new IllegalArgumentException("Invalid numeric type: " + numeric);
        }
        return result;
    }
}

enum WORKFLOWUSAGEHEADERS{
    ActionName("0"),
    ActionType("1"),
    Description("2"),
    Workflow("3"),
    StartStatus("4"),
    EndStatus("5");

    private static final Map<String, WORKFLOWUSAGEHEADERS> map = new HashMap<>(values().length, 1);

    static {
        for (WORKFLOWUSAGEHEADERS headers : values()) map.put(headers.header, headers);
    }

    private final String header;

    private WORKFLOWUSAGEHEADERS(String header) {
        this.header = header;
    }

    public static WORKFLOWUSAGEHEADERS of(String numeric) {
        WORKFLOWUSAGEHEADERS result = map.get(numeric);
        if (result == null) {
            throw new IllegalArgumentException("Invalid numeric type: " + numeric);
        }
        return result;
    }
}

public class Script {

    private String id;
    public String name;
    private String systemId;
    private String description;
    private String type;
    private String code;
    public HashMap<String, Script> usesScripts = new HashMap<>();            //systemid, script
    public HashMap<String, Script> usedByScripts = new HashMap<>();            //systemid, script
    private HashMap<String, CustomField> usesCustomFields = new HashMap<>();    //systemid, customfield
    private HashMap<String, CustomParam> usesCustomParams = new HashMap<>();    //name, customparam
    private HashMap<String, Workflow> usedInWorkflowName = new HashMap<>();    //name, workflow
    private HashMap<String, Workflow> usedInWorkflowSysId = new HashMap<>();    //name, workflow
    private HashSet<Map<String,String>> actionUsages = new HashSet<>();
    private HashSet<Map<String,String>> tcUsages = new HashSet<>();

    /**
     * Constructor
     */
    public Script(){

    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getSystemId(){
        return this.systemId;
    }

    public String getDescription(){
        return this.description;
    }

    public String getType(){
        return this.type;
    }

    public String getCode(){
        return this.code;
    }

    public HashMap<String, Script> getUsesScripts(){
        return this.usesScripts;
    }

    public HashMap<String, Script> getUsedByScripts(){
        return this.usedByScripts;
    }

    public HashMap<String, CustomField> getUsesCustomFields(){
        return this.usesCustomFields;
    }

    public HashMap<String, CustomParam> getUsesCustomParams(){
        return this.usesCustomParams;
    }

    public HashSet<Map<String,String>> getActionUsages(){
        return this.actionUsages;
    }

    public HashSet<Map<String,String>> getTcUsages(){
        return this.tcUsages;
    }

    public HashMap<String, Workflow> getUsedInWorkflowName() {
        return usedInWorkflowName;
    }

    public HashMap<String, Workflow> getUsedInWorkflowSysId() {
        return usedInWorkflowSysId;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSystemId(String systemId){
        this.systemId = systemId;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setCode(String code){
        this.code = code;
    }

    public void addUsedScripts(String systemId, Script script){
        this.usesScripts.put(systemId,script);
    }

    public void addUsedByScripts(String systemId, Script script){
        this.usedByScripts.put(systemId,script);
    }

    public void addUsesCustomFields(String systemId, CustomField customField){
        this.usesCustomFields.put(systemId,customField);
    }

    public void addUsesCustomParams(String name, CustomParam customparam){
        this.usesCustomParams.put(name,customparam);
    }

    public void addActionUsage(Map<String,String> usages){
        this.actionUsages.add(usages);
    }

    public void addTcUsage(Map<String,String> usages){
        this.tcUsages.add(usages);
    }

    public void addUsedInWorkflowName(String name, Workflow workflow) {
        this.usedInWorkflowName.put(name, workflow);
    }

    public void addUsedInWorkflowSysId(String sysId, Workflow workflow) {
        this.usedInWorkflowSysId.put(sysId, workflow);
    }

    public void removeUsedScripts(String systemId){
        this.usesScripts.remove(systemId);
    }

    public void removeUsedByScripts(String systemId){
        this.usedByScripts.remove(systemId);
    }

    public void removeUsesCustomFields(String systemId){
        this.usesCustomFields.remove(systemId);
    }

    public void removeUsesCustomParams(String name){
        this.usesCustomParams.remove(systemId);
    }

    public void removeActionUsage(Map<String,String> usages){
        this.actionUsages.remove(usages);
    }

    public void removeTcUsage(Map<String,String> usages){
        this.tcUsages.remove(usages);
    }

    public void removeUsedInWorkflowName(String name) {
        this.usedInWorkflowName.remove(name);
    }

    public void removeUsedInWorkflowSysId(String sysId) {
        this.usedInWorkflowSysId.remove(sysId);
    }
}

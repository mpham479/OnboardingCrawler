import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

enum SCRIPTTYPES{
    Workflow("1"),
    Async("9"),
    Scheduled_Task("3"),
    Form("6"),
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
    public Map<String, Script> usesScripts = new HashMap<>();            //systemid, script
    public Map<String, Script> usedByScripts = new HashMap<>();            //systemid, script
    private Map<String, CustomField> usesCustomFields = new HashMap<>();    //systemid, customfield
    private Map<String, CustomParam> usesCustomParams = new HashMap<>();    //name, customparam
    private Set<Map<String,String>> actionUsages = new HashSet<>();
    private Set<Map<String,String>> tcUsages = new HashSet<>();

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

    public Map<String, Script> getUsesScripts(){
        return this.usesScripts;
    }

    public Map<String, Script> getUsedByScripts(){
        return this.usedByScripts;
    }

    public Map<String, CustomField> getUsesCustomFields(){
        return this.usesCustomFields;
    }

    public Map<String, CustomParam> getUsesCustomParams(){
        return this.usesCustomParams;
    }

    public Set<Map<String,String>> getActionUsages(){
        return this.actionUsages;
    }

    public Set<Map<String,String>> getTcUsages(){
        return this.tcUsages;
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
        this.usesCustomParams.put(systemId,customparam);
    }

    public void addActionUsage(Map<String,String> usages){
        this.actionUsages.add(usages);
    }

    public void addTcUsage(Map<String,String> usages){
        this.tcUsages.add(usages);
    }
}

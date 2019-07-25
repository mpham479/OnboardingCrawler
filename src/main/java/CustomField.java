import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

enum CFACTIONUSAGEHEADERS{
    Action("0"),
    Description("1"),
    Workflow("2"),
    StartStatus("3"),
    EndStatus("4");

    private static final Map<String, CFACTIONUSAGEHEADERS> map = new HashMap<>(values().length, 1);

    static {
        for (CFACTIONUSAGEHEADERS headers : values()) map.put(headers.header, headers);
    }

    private final String header;

    private CFACTIONUSAGEHEADERS(String header) {
        this.header = header;
    }

    public static CFACTIONUSAGEHEADERS of(String numeric) {
        CFACTIONUSAGEHEADERS result = map.get(numeric);
        if (result == null) {
            throw new IllegalArgumentException("Invalid numeric type: " + numeric);
        }
        return result;
    }
}

public class CustomField {
    private String id;
    private String name;
    private String systemId;
    private String description;
    private String type;
    private String label;
    private HashMap<String, Script> usedByScripts = new HashMap<>();   //systemid, script
    private HashMap<String, Workflow> usedInWorkflow = new HashMap<>();   //name, workflow
    private HashMap<String, Form> usedInForms = new HashMap<>();   //systemid, form
    private HashSet<Map<String,String>> actionUsages = new HashSet<>();

    /**
     * Constructor
     */
    public CustomField(){

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

    public String getLabel(){
        return this.label;
    }

    public HashMap<String, Script> getUsedByScripts(){
        return this.usedByScripts;
    }

    public HashSet<Map<String,String>> getActionUsages(){
        return this.actionUsages;
    }

    public HashMap<String, Workflow> getUsedInWorkflow() {
        return this.usedInWorkflow;
    }

    public HashMap<String, Form> getUsedInForms() {
        return this.usedInForms;
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

    public void setLabel(String label){
        this.label = label;
    }

    public void addUsedByScripts(String systemId, Script script){
        this.usedByScripts.put(systemId,script);
    }

    public void addActionUsage(Map<String,String> usages){
        this.actionUsages.add(usages);
    }

    public void addUsedInWorkflow(String name, Workflow workflow){
        this.usedInWorkflow.put(name,workflow);
    }

    public void addUsedInForms(String systemid, Form form) {
        this.usedInForms.put(systemid, form);
    }

    public void removeUsedByScripts(String systemId){
        this.usedByScripts.remove(systemId);
    }

    public void removeActionUsage(Map<String,String> usages){
        this.actionUsages.remove(usages);
    }

    public void removeUsedInWorkflow(String name){
        this.usedInWorkflow.remove(name);
    }
}

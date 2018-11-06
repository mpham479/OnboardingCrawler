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
    private Map<String, Script> usedByScripts = new HashMap<>();   //systemid, script
    private Map<String, Workflow> usedInWorkflow = new HashMap<>();   //systemid, workflow
    private Set<Map<String,String>> actionUsages = new HashSet<>();

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

    public Map<String, Script> getUsedByScripts(){
        return this.usedByScripts;
    }

    public Set<Map<String,String>> getActionUsages(){
        return this.actionUsages;
    }

    public Map<String, Workflow> getUsedInWorkflow() {
        return usedInWorkflow;
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

    public void setUsedInWorkflow(String systemId, Workflow workflow){
        this.usedInWorkflow.put(systemId,workflow);
    }
}

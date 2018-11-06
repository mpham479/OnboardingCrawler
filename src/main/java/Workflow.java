import java.util.HashMap;
import java.util.Map;

public class Workflow {
    private String id;
    private String name;
    private String systemId;
    private String description;
    private Map<String, Script> usesScripts = new HashMap<>();   //systemid, script
    private Map<String, CustomField> usesCustomFieldsSysId  = new HashMap<>();   //name, cf
    private Map<String, CustomParam> usesCustomParams  = new HashMap<>();   //name, cp

    public Workflow(){

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

    public Map<String, Script> getUsesScripts(){
        return this.usesScripts;
    }

    public Map<String, CustomField> getUsesCustomFields(){
        return this.usesCustomFieldsSysId;
    }

    public Map<String, CustomParam> getUsesCustomParams(){
        return this.usesCustomParams;
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

    public void addUsedScripts(String systemId, Script script){
        this.usesScripts.put(systemId,script);
    }

    public void addUsesCustomFields(String systemId, CustomField customField){
        this.usesCustomFieldsSysId.put(systemId,customField);
    }

    public void addUsesCustomParams(String systemId, CustomParam customParam){
        this.usesCustomParams.put(systemId,customParam);
    }

}

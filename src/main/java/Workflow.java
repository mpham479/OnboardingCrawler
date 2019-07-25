import java.util.HashMap;
import java.util.Map;

public class Workflow {
    private String id;
    private String name;
    private String systemId;
    private String description;
    private HashMap<String, Script> usesScripts = new HashMap<>();   //systemid, script
    private HashMap<String, CustomField> usesCustomFieldsSysId  = new HashMap<>();   //name, cf
    private HashMap<String, CustomParam> usesCustomParams  = new HashMap<>();   //name, cp
    private HashMap<String, Form> usesForms = new HashMap<>(); //systemid, form

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

    public HashMap<String, Script> getUsesScripts(){
        return this.usesScripts;
    }

    public HashMap<String, CustomField> getUsesCustomFields(){
        return this.usesCustomFieldsSysId;
    }

    public HashMap<String, CustomParam> getUsesCustomParams(){
        return this.usesCustomParams;
    }

    public HashMap<String, Form> getUsesForms() {
        return usesForms;
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

    public void addUsesForms(String systemId, Form form) {
        this.usesForms.put(systemId, form);
    }

    public void removeUsedScripts(String systemId){
        this.usesScripts.remove(systemId);
    }

    public void removeUsesCustomFields(String systemId){
        this.usesCustomFieldsSysId.remove(systemId);
    }

    public void removeUsesCustomParams(String systemId){
        this.usesCustomParams.remove(systemId);
    }

    public void removeUsesForms(String systemId) {
        this.usesForms.remove(systemId);
    }

}

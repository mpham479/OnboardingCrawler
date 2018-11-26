import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomParam {
    private String name;
    private String description;
    private String type;
    private String category;
    private HashMap<String, Script> usedByScripts = new HashMap<>();   //systemid, script
    private HashMap<String, Workflow> usedInWorkflows = new HashMap<>();   //systemid, workflow

    /**
     * Constructor
     */
    public CustomParam(){

    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public String getType(){
        return this.type;
    }

    public String getCategory(){
        return this.category;
    }

    public HashMap<String, Script> getUsedByScripts(){
        return this.usedByScripts;
    }

    public HashMap<String, Workflow> getUsedInWorkflows() {
        return usedInWorkflows;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setCategory(String category){
        this.type = category;
    }

    public void addUsedByScripts(String systemId, Script script){
        this.usedByScripts.put(systemId,script);
    }

    public void setUsedInWorkflows(String systemId, Workflow workflow){
        this.usedInWorkflows.put(systemId,workflow);
    }

    public void removeUsedByScripts(String systemId){
        this.usedByScripts.remove(systemId);
    }

    public void removeUsedInWorkflows(String systemId){
        this.usedInWorkflows.remove(systemId);
    }
}

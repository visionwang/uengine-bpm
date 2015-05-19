package org.uengine.kernel.bpmn;

import org.metaworks.annotation.Hidden;
import org.metaworks.annotation.Range;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.*;
import org.uengine.util.UEngineUtil;

import java.io.Serializable;
import java.util.*;

public class SubProcess extends ScopeActivity{


    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    protected final static String SUBPROCESS_INST_ID="instanceIdOfSubProcess";
    protected final static String SUBPROCESS_INST_LABELS="labelsOfSubProcess";
    protected final static String SUBPROCESS_INST_ID_COMPLETED="completedInstanceIdOfSPs";
    protected final static String EVENT_ONE_OF_PREV_SP_COMPLETED = "ONE_OF_PREV_SP_COMPLETED";
    public static final String SUB_PROCESS_IS_BEING_INSERTED = "_SubProcessIsBeingInserted";


    public SubProcess(){
        super();
        setName("");
        setDescription("");
        setInstanceId("<%=Instance.Name%>");
    }

    String instanceId;
    @Hidden
    public String getInstanceId() {
        return instanceId;
    }
    public void setInstanceId(String string) {
        instanceId = string;
    }

    String alias;
    @Hidden
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    String definitionId;
    @Hidden
    public String getDefinitionId() {
        return definitionId;
    }
    public void setDefinitionId(String l) {
        definitionId = l;
    }

    //add ryu start
    ProcessDefinition definitionObject;
    @Hidden
    public ProcessDefinition getDefinitionObject() {
        return definitionObject;
    }

    public void setDefinitionObject(ProcessDefinition definition) {
        this.definitionObject = definition;
    }

    /*  String dynamicDefinitionId;
    public String getDynamicDefinitionId()
    {
      return dynamicDefinitionId;
    }
    public void setDynamicDefinitionId(String dynamicDefinitionId)
    {
      this.dynamicDefinitionId = dynamicDefinitionId;
    }
     */
    ProcessVariable dynamicDefinitionIdPV;
    @Hidden
    public ProcessVariable getDynamicDefinitionIdPV() {
        return dynamicDefinitionIdPV;
    }
    public void setDynamicDefinitionIdPV(ProcessVariable dynamicDefinitionIdPV) {
        this.dynamicDefinitionIdPV = dynamicDefinitionIdPV;
    }


    int versionSelectOption = ProcessDefinition.VERSIONSELECTOPTION_CURRENT_PROD_VER;
    @Hidden
    @Range(
            options={
                    "Use the CURRENT production version",
                    "Use the production version AT THE INITIATED TIME",
                    "Use the production version AT THE DESIGNED TIME",
                    "Use the version JUST SELECTED"
            },

            values={
                    ""+ProcessDefinition.VERSIONSELECTOPTION_CURRENT_PROD_VER,
                    ""+ProcessDefinition.VERSIONSELECTOPTION_PROD_VER_AT_INITIATED_TIME,
                    ""+ProcessDefinition.VERSIONSELECTOPTION_PROD_VER_AT_DESIGNED_TIME,
                    ""+ProcessDefinition.VERSIONSELECTOPTION_JUST_SELECTED,
            }
    )
    public int getVersionSelectOption() {
        return versionSelectOption;
    }
    public void setVersionSelectOption(int versionSelectOption) {
        this.versionSelectOption = versionSelectOption;
    }

    ParameterContext[] variableBindings;
    @Hidden
    public ParameterContext[] getVariableBindings() {
        return variableBindings;
    }
    public void setVariableBindings(ParameterContext[] contexts) {
        variableBindings = contexts;
    }

    RoleParameterContext[] roleBindings;
    @Hidden
    public RoleParameterContext[] getRoleBindings() {
        return roleBindings;
    }
    public void setRoleBindings(RoleParameterContext[] contexts) {
        roleBindings = contexts;
    }

    boolean runAndForget;
    @Hidden
    public boolean isRunAndForget() {
        return runAndForget;
    }
    public void setRunAndForget(boolean b) {
        runAndForget = b;
    }

    boolean createAsRootProcess;
    @Hidden
    public boolean isCreateAsRootProcess() {
        return createAsRootProcess;
    }
    public void setCreateAsRootProcess(boolean createAsRootProcess) {
        this.createAsRootProcess = createAsRootProcess;
    }

    TextContext multipleInstanceLabel = TextContext.createInstance();
    @Hidden
    public TextContext getMultipleInstanceLabel() {
        return multipleInstanceLabel;
    }
    public void setMultipleInstanceLabel(TextContext multipleInstanceLabel) {
        this.multipleInstanceLabel = multipleInstanceLabel;
    }

    Role forEachRole;
    @Hidden
    public Role getForEachRole() {
        return forEachRole;
    }
    public void setForEachRole(Role role) {
        forEachRole = role;
    }

    ProcessVariable forEachVariable;
    @Hidden
    public ProcessVariable getForEachVariable() {
        return forEachVariable;
    }
    public void setForEachVariable(ProcessVariable forEachVariable) {
        this.forEachVariable = forEachVariable;
    }

    //  boolean viewAlsoInMainProcess;
    //  	public boolean isViewAlsoInMainProcess() {
    //		return viewAlsoInMainProcess;
    //	}
    //	public void setViewAlsoInMainProcess(boolean viewAlsoInMainProcess) {
    //		this.viewAlsoInMainProcess = viewAlsoInMainProcess;
    //	}

    private Hashtable getUnInitiatedRoleMappings(ProcessInstance instance) throws Exception{
        RoleMapping mainRoleMapping = getForEachRole().getMapping(instance);
        Hashtable notStartedRoleMappings = new Hashtable();

        String boundRoleName = null;
        for(int i=0; i<getRoleBindings().length; i++){
            if(getRoleBindings()[i].getRole()!=null && getRoleBindings()[i].getRole().equals(getForEachRole())){
                boundRoleName = getRoleBindings()[i].getArgument();
                break;
            }
        }

        Vector subprocessInstanceList = getSubProcesses(instance);
        Hashtable boundRoleMappings = new Hashtable();
        for(int i=0; i < subprocessInstanceList.size();i++){
            ProcessInstance thePI = ((ProcessInstance)subprocessInstanceList.elementAt(i));
            if(thePI.isRunning("")){
                RoleMapping subRoleMapping = thePI.getRoleMapping(boundRoleName);
                boundRoleMappings.put(subRoleMapping.getEndpoint(), subRoleMapping);
            }
        }

        do{
            RoleMapping thisRM = mainRoleMapping.getCurrentRoleMapping();
            thisRM = thisRM.makeSingle();
            String endpoint = thisRM.getEndpoint();

            if(!boundRoleMappings.containsKey(endpoint))
                notStartedRoleMappings.put(endpoint, thisRM);
        }while(mainRoleMapping.next());

        return notStartedRoleMappings;
    }

    private Hashtable getDeletedRoleMappings(ProcessInstance instance) throws Exception{
        RoleMapping mainRoleMapping = getForEachRole().getMapping(instance);

        String boundRoleName = null;
        for(int i=0; i<getRoleBindings().length; i++){
            if(getRoleBindings()[i].getRole() != null && getRoleBindings()[i].getRole().equals(getForEachRole())){
                boundRoleName = getRoleBindings()[i].getArgument();
                break;
            }
        }

        Vector subprocessInstanceList = getSubProcesses(instance);
        Hashtable boundRoleMappings = new Hashtable();
        for(int i=0; i < subprocessInstanceList.size();i++){
            ProcessInstance thePI = ((ProcessInstance)subprocessInstanceList.elementAt(i));
            RoleMapping subRoleMapping = thePI.getRoleMapping(boundRoleName);
            boundRoleMappings.put(subRoleMapping.getEndpoint(), subRoleMapping);
        }

        do{
            RoleMapping thisRM = mainRoleMapping.getCurrentRoleMapping();
            thisRM = thisRM.makeSingle();
            String endpoint = thisRM.getEndpoint();

            if(boundRoleMappings.containsKey(endpoint))
                boundRoleMappings.remove(endpoint);
        }while(mainRoleMapping.next());

        return boundRoleMappings;
    }

    private void stopSubProcessInstance(ProcessInstance instance,Hashtable deletedRoleMappings) throws Exception{
        String boundRoleName = null;
        for(int i=0; i<getRoleBindings().length; i++){
            if(getRoleBindings()[i].getRole() != null && getRoleBindings()[i].getRole().equals(getForEachRole())){
                boundRoleName = getRoleBindings()[i].getArgument();
                break;
            }
        }

        Vector spInstIds = getSubprocessIds(instance);
        Vector spInstLabels = getSubprocessIds(instance, SUBPROCESS_INST_LABELS);
        Vector subprocessInstanceList = getSubProcesses(instance);
        for(int i=0; i < subprocessInstanceList.size();i++){
            ProcessInstance thePI = ((ProcessInstance)subprocessInstanceList.elementAt(i));
            RoleMapping subRoleMapping = thePI.getRoleMapping(boundRoleName);
            if(deletedRoleMappings.containsKey(subRoleMapping.getEndpoint())){
                thePI.stop();
                spInstIds.remove(i);
                spInstLabels.remove(i);
            }

        }

        setSubprocessIds(instance, spInstIds, SUBPROCESS_INST_ID);
        setSubprocessIds(instance, spInstLabels, SUBPROCESS_INST_LABELS);
    }
    public void refreshMultipleInstance(ProcessInstance instance)throws Exception{
        Hashtable unInitiatedRoleMappings = getUnInitiatedRoleMappings(instance);
        Hashtable deletedRoleMappings = getDeletedRoleMappings(instance);

        //------- delete instance
        if(deletedRoleMappings.size()!=0)
            stopSubProcessInstance(instance,deletedRoleMappings);

        if(unInitiatedRoleMappings.size()==0){
            return ;
        }

        //------- add instance
        boolean isConnectedMultipleSubProcesses = false;
        SubProcessActivity connectedPrevSubProcessActivity = null;
        try{
            if(getPreviousActivities()!=null && getPreviousActivities().get(0) instanceof SubProcessActivity){
                connectedPrevSubProcessActivity = (SubProcessActivity)getPreviousActivities().get(0);
                if(connectedPrevSubProcessActivity.getForEachRole() == getForEachRole() && connectedPrevSubProcessActivity.getForEachVariable() == getForEachVariable()){
                    isConnectedMultipleSubProcesses = true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //todo
        isConnectedMultipleSubProcesses = false;

        if(isConnectedMultipleSubProcesses && STATUS_RUNNING.equals(getStatus(instance))){
            return;
        }

        Vector subprocessInstances = new Vector();
        Vector subprocessInstanceIds = new Vector();
        Vector subprocessLabels = new Vector();

        ProcessVariableValue definitionIdPVV = null;
        if(getDynamicDefinitionIdPV()!=null){
            definitionIdPVV = getDynamicDefinitionIdPV().getMultiple(instance, "");
        }

        if(getForEachRole()!=null){ //if there is configured forEachRole, initiate sub processes for each role mapping

            instance.addDebugInfo("[SubProcessActivity] Splitting multiple instances with Role: ", getForEachRole());

            RoleMapping roleMapping = getForEachRole().getMapping(instance);
            if(roleMapping==null) throw new UEngineException("Mapping for the 'forEachRole'(" + getForEachRole().getDisplayName() + ") is not set yet. \nselect * from bpm_rolemapping where instanceid=" + instance.getInstanceId() + " and rolename='" + getForEachRole() + "' ");

            instance.addDebugInfo("  the mappings are: ", roleMapping);

            ProcessVariableValue forEachValue = null;
            if(getForEachVariable()!=null){
                forEachValue = getForEachVariable().getMultiple(instance, "");
            }

            do{
                RoleMapping thisRM = roleMapping.getCurrentRoleMapping();
                thisRM = thisRM.makeSingle();
                String endpoint = thisRM.getEndpoint();

                Serializable thisValue = null;
                if(forEachValue!=null){
                    thisValue = forEachValue.getValue();
                    forEachValue.next();
                }
                String thisDefinitionVersionId = null;
                if(definitionIdPVV!=null){
                    thisDefinitionVersionId = (String) definitionIdPVV.getValue();
                    definitionIdPVV.next();
                }

                if(unInitiatedRoleMappings.containsKey(endpoint)){
                    ProcessInstance thePI = initiateSubProcess(thisDefinitionVersionId,instance, thisRM, thisValue, isConnectedMultipleSubProcesses, roleMapping.getCursor());
                    subprocessInstances.add(thePI);
                    subprocessInstanceIds.add(thePI.getInstanceId());
                    subprocessLabels.add(thisRM.getResourceName());
                }
            }while(roleMapping.next());

        }


        String boundRoleName = null;
        for(int i=0; i<getRoleBindings().length; i++){
            if(getRoleBindings()[i].getRole() != null && getRoleBindings()[i].getRole().equals(getForEachRole())){
                boundRoleName = getRoleBindings()[i].getArgument();
                break;
            }
        }

        Vector subprocessInstanceList = getSubProcesses(instance);
        for(int i=0; i < subprocessInstanceList.size();i++){
            ProcessInstance thePI = ((ProcessInstance)subprocessInstanceList.elementAt(i));
            RoleMapping subRoleMapping = thePI.getRoleMapping(boundRoleName);

            subprocessInstanceIds.add(thePI.getInstanceId());
            subprocessLabels.add(subRoleMapping.getResourceName());
        }

        setSubprocessIds(instance, subprocessInstanceIds, SUBPROCESS_INST_ID);
        setSubprocessIds(instance, subprocessLabels, SUBPROCESS_INST_LABELS);

        if(!isConnectedMultipleSubProcesses)
            for(int i=0; i<subprocessInstances.size(); i++){
                ProcessInstance thePI = ((ProcessInstance)subprocessInstances.elementAt(i));

                thePI.execute();
            }

        if(isRunAndForget()){
            fireComplete(instance);
        }
    }

    protected void executeActivity(ProcessInstance instance)
            throws Exception {

        boolean isConnectedMultipleSubProcesses = false;
        SubProcessActivity connectedPrevSubProcessActivity = null;
        try{
            if(getPreviousActivities()!=null && getPreviousActivities().get(0) instanceof SubProcessActivity){
                connectedPrevSubProcessActivity = (SubProcessActivity)getPreviousActivities().get(0);
                if(connectedPrevSubProcessActivity.getForEachRole() == getForEachRole() && connectedPrevSubProcessActivity.getForEachVariable() == getForEachVariable()){
                    isConnectedMultipleSubProcesses = true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //todo
        isConnectedMultipleSubProcesses = false;

        if(isConnectedMultipleSubProcesses && STATUS_RUNNING.equals(getStatus(instance))){
            return;
        }

        Vector subprocessInstances = new Vector();
        Vector subprocessInstanceIds = new Vector();
        Vector subprocessLabels = new Vector();

        ProcessVariableValue definitionIdPVV = null;
        if(getDynamicDefinitionIdPV()!=null){
            definitionIdPVV = getDynamicDefinitionIdPV().getMultiple(instance, "");
        }


        instance.getProcessTransactionContext().setSharedContext("_SubProcessIsBeingInserted", new Boolean(true));

        if(getForEachRole()!=null){ //if there is configured forEachRole, initiate sub processes for each role mapping

            instance.addDebugInfo("[SubProcessActivity] Splitting multiple instances with Role: ", getForEachRole());

            RoleMapping roleMapping = getForEachRole().getMapping(instance);
            if(roleMapping==null) throw new UEngineException("Mapping for the 'forEachRole'(" + getForEachRole().getDisplayName() + ") is not set yet. \n Please check if there exist bound user by query: select * from bpm_rolemapping where instanceid=" + instance.getInstanceId() + " and rolename='" + getForEachRole() + "' ;");

            instance.addDebugInfo("  the mappings are: ", roleMapping);

            ProcessVariableValue forEachValue = null;
            if(getForEachVariable()!=null){
                forEachValue = getForEachVariable().getMultiple(instance, "");
            }

            do{
                RoleMapping thisRM = roleMapping.getCurrentRoleMapping();
                thisRM = thisRM.makeSingle();

                if(thisRM.getEndpoint().equals(thisRM.getResourceName()))
                    thisRM.fill(instance);

                Serializable thisValue = null;
                if(forEachValue!=null){
                    thisValue = forEachValue.getValue();
                    forEachValue.next();
                }

                String thisDefinitionVersionId = null;
                if(definitionIdPVV!=null){
                    thisDefinitionVersionId = (String) definitionIdPVV.getValue();
                    definitionIdPVV.next();
                }

                ProcessInstance thePI = initiateSubProcess(thisDefinitionVersionId, instance, thisRM, thisValue, isConnectedMultipleSubProcesses, roleMapping.getCursor());

                subprocessInstances.add(thePI);
                subprocessInstanceIds.add(thePI.getInstanceId());

                subprocessLabels.add(thisRM.toString());
            }while(roleMapping.next());

        }else if(getForEachVariable()!=null){

            instance.addDebugInfo("[SubProcessActivity] Splitting multiple instances with Variable: ", getForEachVariable());

            ProcessVariableValue pvv = getForEachVariable().getMultiple(instance, "");
            instance.addDebugInfo("  values are: ", pvv);

            do{
                Serializable theValue = pvv.getValue();

                String thisDefinitionVersionId = null;
                if(definitionIdPVV!=null){
                    thisDefinitionVersionId = (String) definitionIdPVV.getValue();
                    definitionIdPVV.next();
                }

                ExecutionScopeContext oldEsc = instance.getExecutionScopeContext();

                ExecutionScopeContext esc = instance.issueNewExecutionScope(this, this, "sub");
                instance.setExecutionScopeContext(esc);

                ProcessInstance thePI = initiateSubProcess(thisDefinitionVersionId, instance, null, theValue, isConnectedMultipleSubProcesses, pvv.getCursor());

                subprocessInstances.add(thePI);
                subprocessInstanceIds.add(thePI.getInstanceId() + "@" + esc.getExecutionScope());
                subprocessLabels.add(theValue);

                instance.setExecutionScopeContext(oldEsc);

            }while(pvv.next());

        }else{
            String thisDefinitionVersionId = null;
            if(definitionIdPVV!=null){
                thisDefinitionVersionId = (String) definitionIdPVV.getValue();
            }

            ProcessInstance thePI = initiateSubProcess(thisDefinitionVersionId, instance, null, null, false, 0);


            subprocessInstances.add(thePI);
            subprocessInstanceIds.add(thePI.getInstanceId());
        }

        setSubprocessIds(instance, subprocessInstanceIds, SUBPROCESS_INST_ID);
        setSubprocessIds(instance, subprocessLabels, SUBPROCESS_INST_LABELS);

        instance.getProcessTransactionContext().setSharedContext(SUB_PROCESS_IS_BEING_INSERTED, null);


//        if(!isConnectedMultipleSubProcesses)
//            for(int i=0; i<subprocessInstances.size(); i++){
//                ProcessInstance thePI = ((ProcessInstance)subprocessInstances.elementAt(i));
//                thePI.execute();
//            }
//
        if(isRunAndForget() || getUncompletedSubProcessIds(instance).isEmpty()){
            super.fireComplete(instance);
        }
    }

    public boolean onSubProcessReturn(ProcessInstance instance, ProcessInstance subProcessInstance) throws Exception{

        String executionScope = subProcessInstance.getExecutionScopeContext().getExecutionScope();
        instance.setExecutionScope(instance.getMainExecutionScope());

        if(isRunAndForget()){

            Hashtable optionsForVariableMapping = new Hashtable();
            optionsForVariableMapping.put("ptc", instance.getProcessTransactionContext());

            Vector spIds = new Vector();
            spIds.add(subProcessInstance.getInstanceId());

            Map subProcesses = new Hashtable();
            subProcesses.put(subProcessInstance.getInstanceId(), subProcessInstance);

            applyVariableBindings(instance, spIds, subProcesses, optionsForVariableMapping);
            applyRoleBindings(instance, spIds, subProcesses, optionsForVariableMapping);

            return false;

        }else{

            Vector completedSPIds = getSubprocessIds(instance, SUBPROCESS_INST_ID_COMPLETED);
            completedSPIds.add(subProcessInstance.getInstanceId() + "@" + executionScope);
            setSubprocessIds(instance, completedSPIds, SUBPROCESS_INST_ID_COMPLETED);


            Set spIdSet = getUncompletedSubProcessIds(instance);

            boolean isConnectedMultipleSubProcesses = false;
            SubProcessActivity connectedNextSubProcessActivity = null;
            try{
                int whereIAm = ((ComplexActivity)getParentActivity()).getChildActivities().indexOf(this);
                Activity nextActivity = (Activity)((ComplexActivity)getParentActivity()).getChildActivities().get(whereIAm + 1);
                if(nextActivity instanceof SubProcessActivity){
                    connectedNextSubProcessActivity = (SubProcessActivity)nextActivity;
                    if(connectedNextSubProcessActivity.getForEachRole() == getForEachRole() && connectedNextSubProcessActivity.getForEachVariable() == getForEachVariable()){
                        isConnectedMultipleSubProcesses = true;

                    }
                }
            }catch(Exception e){
                //e.printStackTrace();
            }
//            //todo
//            isConnectedMultipleSubProcesses = false;
//
//            if(isConnectedMultipleSubProcesses){
//                connectedNextSubProcessActivity.onEvent(EVENT_ONE_OF_PREV_SP_COMPLETED, instance, new Integer(orderOfCurrentlyCompletedSubProcessInstance));
//            }

            return (spIdSet.isEmpty());

        }
    }

    private Set getUncompletedSubProcessIds(ProcessInstance instance) throws Exception {

        Vector completedSPIds = getSubprocessIds(instance, SUBPROCESS_INST_ID_COMPLETED);

        Vector spIds = getSubprocessIds(instance, SUBPROCESS_INST_ID);

        Set spIdSet = new HashSet();
        for(int i=0; i<spIds.size(); i++){
            spIdSet.add(spIds.elementAt(i));
        }

        for(int i=0; i<completedSPIds.size(); i++){
            spIdSet.remove(completedSPIds.elementAt(i));
        }

        return spIdSet;
    }


    protected void applyVariableBindings(ProcessInstance instance, Vector spIds, Map subProcesses, Map options) throws Exception{

        if(variableBindings!=null)
        for(int i=0; i<variableBindings.length; i++){
          ParameterContext vb = variableBindings[i];
          if(vb.getVariable()==null ||
              (vb.getDirection()!=null && vb.getDirection().equals(ParameterContext.DIRECTION_IN))
          ) continue;

          instance.set("", vb.getVariable().getName(), null);
        }

        if(spIds!=null)
        for(int indexOfSP=0; indexOfSP<spIds.size(); indexOfSP++){
          String subProcessId = (String)spIds.elementAt(indexOfSP);

          ParameterContext[] variableBindings = getVariableBindings();
          if(variableBindings!=null)
          for(int i=0; i<variableBindings.length; i++){
            ParameterContext vb = variableBindings[i];
            if(vb.getVariable()==null ||
                (vb.getDirection()!=null && vb.getDirection().equals(ParameterContext.DIRECTION_IN))
            ) continue;

            if(!subProcesses.containsKey(subProcessId)){
              ProcessInstance sp = ProcessInstance.create().getInstance(subProcessId, options);
              subProcesses.put(subProcessId, sp);
            }
            ProcessInstance subProcessInstance = (ProcessInstance)subProcesses.get(subProcessId);

            try{
                boolean join = false;
                if(vb instanceof SubProcessParameterContext){
                    SubProcessParameterContext spParameterContext = (SubProcessParameterContext)vb;
                    join = spParameterContext.isSplit();
                }

                join = join || vb.getVariable() == getForEachVariable();

              if(join){
                   Serializable valueOfSP = subProcessInstance.get("", vb.getArgument().getText());
                   instance.add("", vb.getVariable().getName(), valueOfSP, indexOfSP);//process multiple pv
              }else{
                ProcessVariableValue valueOfSP = subProcessInstance.getMultiple("", vb.getArgument().getText());
                valueOfSP.setName(vb.getVariable().getName());
                instance.set("", valueOfSP);
              }
            }catch(Exception e){
              UEngineException richException = new UEngineException("Error when to set the value ["+vb.getVariable()+"] from returned subprocess", e);
              richException.setActivity(this);
              richException.setInstance(instance);
              throw richException;
            }
          }
        }
    }

    protected void afterComplete(ProcessInstance instance) throws Exception {

        //apply the values from the sub process
        Hashtable options = new Hashtable();
        options.put("ptc", instance.getProcessTransactionContext());

        Vector spIds = getSubprocessIds(instance, SUBPROCESS_INST_ID);

        Map subProcesses = new Hashtable();

        if(!isRunAndForget()){
            applyVariableBindings(instance, spIds, subProcesses, options);
            applyRoleBindings(instance, spIds, subProcesses, options);
        }

        super.afterComplete(instance);
    }

    protected void applyRoleBindings(ProcessInstance instance, Vector spIds, Map subProcesses, Map options) throws Exception{

        RoleParameterContext[] roleBindings = getRoleBindings();
        if(roleBindings!=null)
            for(int i=0; i<roleBindings.length; i++){
                RoleParameterContext rb = roleBindings[i];
                if(rb.getRole()==null ||
                        (rb.getDirection()!=null && rb.getDirection().equals(RoleParameterContext.DIRECTION_IN))
                        ) continue;

                RoleMapping mergingRM = null;
                for(int indexOfSP=0; indexOfSP<spIds.size(); indexOfSP++){
                    String subProcessId = (String)spIds.elementAt(indexOfSP);
                    if(!subProcesses.containsKey(subProcessId)){
                        ProcessInstance sp = ProcessInstance.create().getInstance(subProcessId, options);
                        subProcesses.put(subProcessId, sp);
                    }
                    ProcessInstance subProcessInstance = (ProcessInstance)subProcesses.get(subProcessId);

                    Role subRole = subProcessInstance.getProcessDefinition().getRole(rb.getArgument());

                    if(subRole==null){
                        throw new UEngineException("Failed to map up to main process since the sub process [" + subProcessInstance.getProcessDefinition() + "]'s role [" + rb.getArgument() + "] has been removed.");
                    }

                    RoleMapping rmOfSP = subProcessInstance.getProcessDefinition().getRole(rb.getArgument()).getMapping(subProcessInstance);
					/*if(rmOfSP!=null){
          rmOfSP.makeSingle();
        }*/

                    if(mergingRM==null)
                        mergingRM = rmOfSP;
                    else
                        mergingRM.replaceCurrentRoleMapping(rmOfSP);

                    if(indexOfSP != spIds.size()-1) mergingRM.moveToAdd();
                }

                if(mergingRM!=null)
                    instance.putRoleMapping(rb.getRole().getName(), mergingRM);
            }
    }

    public Vector getSubProcesses(ProcessInstance instance) throws Exception{
        Hashtable options = new Hashtable();
        options.put("ptc", instance.getProcessTransactionContext());

        Vector spIds = getSubprocessIds(instance, SUBPROCESS_INST_ID);
        Vector subProcesses = new Vector();

        for(int indexOfSP=0; indexOfSP<spIds.size(); indexOfSP++){
            String subProcessId = (String)spIds.elementAt(indexOfSP);

            ProcessInstance sp = ProcessInstance.create().getInstance(subProcessId, options);
            subProcesses.add(sp);
        }

        return subProcesses;
    }


    protected ProcessInstance initiateSubProcess(String realDefinitionId, ProcessInstance instance, RoleMapping currentRoleMapping, Serializable currentVariableValue, boolean isConnectedMultipleSubProcesses, int mappingIndex) throws Exception{
        String subProcessInstanceName = evaluateContent(instance, getInstanceId()).toString();

        super.executeActivity(instance);

        return instance;
    }


    protected void transferValues(ProcessInstance instance, ProcessInstance subProcessInstance, RoleMapping currentRoleMapping, Serializable currentVariableValue, int mappingIndex) throws Exception{
        //transfer the values of variables
        if(variableBindings !=null)
            for(int i=0; i<variableBindings.length; i++){
                ParameterContext pvpc = variableBindings[i];
                if(pvpc.getVariable()==null || pvpc.getArgument()==null
                        || (pvpc.getDirection()!=null && pvpc.getDirection().equals(ParameterContext.DIRECTION_OUT))
                        )
                    continue;

                Object val;
                if(getForEachVariable()!=null && getForEachVariable() == pvpc.getVariable()){
                    val = currentVariableValue;
                    instance.addDebugInfo("[SubProcessActivity] transferring main process' variable: ", getForEachVariable());
                    instance.addDebugInfo(" to sub process' variable: ", pvpc.getArgument().getText());
                }else{
                    ProcessVariableValue pvv = pvpc.getVariable().getMultiple(instance, "");

                    if(pvv==null)
                        val=null;
                    else{
                        if((pvpc instanceof SubProcessParameterContext) && ((SubProcessParameterContext)pvpc).isSplit()){
                            if(pvv.size() > mappingIndex){
                                pvv.setCursor(mappingIndex);
                            }else{
                                pvv.setCursor(pvv.size() - 1); //move to the last element
                            }

                            val = pvv.getValue();
                        }else{
                            if(pvv.size()>1){
                                val = pvv;
                            }else
                                val = pvv.getValue();
                        }

                    }

                    instance.addDebugInfo("[SubProcessActivity] transferring main process' variable: ", pvpc.getVariable());
                    instance.addDebugInfo(" to sub process' variable: ", pvpc.getArgument().getText());
                }

                subProcessInstance.set("", pvpc.getArgument().getText(), (Serializable)val);
            }
        //transfer the mapping of roles
        if(roleBindings !=null)
            for(int i=0; i<roleBindings.length; i++){
                RoleParameterContext rpc = roleBindings[i];
                if(rpc.getRole()==null || rpc.getArgument()==null
                        || (rpc.getDirection()!=null && rpc.getDirection().equals(RoleParameterContext.DIRECTION_OUT))
                        )
                    continue;

                RoleMapping roleMapping;
                if(getForEachRole()!=null && rpc.getRole() == getForEachRole()){
                    roleMapping = currentRoleMapping;
                    instance.addDebugInfo("[SubProcessActivity] transferring main process' role: ", getForEachRole());
                    instance.addDebugInfo(" to sub process' role: ", rpc.getArgument());
                }else{
                    roleMapping = rpc.getRole().getMapping(instance, this);

                    if(roleMapping!=null){
                        if(rpc.isSplit()){
                            RoleMapping newRoleMapping = RoleMapping.create();
                            newRoleMapping.setName(roleMapping.getName());

                            int count = 0;
                            do {
                                if (count == mappingIndex){
                                    newRoleMapping.setEndpoint(roleMapping.getEndpoint());
                                    //						newRoleMapping.fill(instance);
                                    newRoleMapping.moveToAdd();
                                } else if (mappingIndex < count) {
                                    break;
                                }
                                count++;
                            }while(roleMapping.next());

                            //				if(roleMapping.size() > mappingIndex){
                            //					roleMapping.setCursor(mappingIndex);
                            //				}else{
                            //					roleMapping.setCursor(roleMapping.size() - 1); //move to the last element
                            //				}
                            //
                            //				roleMapping = roleMapping.getCurrentRoleMapping();
                            //				roleMapping.makeSingle();
                            newRoleMapping.beforeFirst();
                            roleMapping=newRoleMapping;
                        }
                    }
                    instance.addDebugInfo("[SubProcessActivity] transferring main process' role: " + rpc.getRole() + " to sub process' role: "+ rpc.getArgument());
                }

                if(roleMapping!=null){
                    RoleMapping anotherRM = (RoleMapping)roleMapping.clone();
                    anotherRM.setName(rpc.getArgument());

                    subProcessInstance.putRoleMapping(anotherRM);
                }
            }
    }

    @Override
    public Map getActivityDetails(ProcessInstance inst, String locale)
            throws Exception {
        Map details = super.getActivityDetails(inst, locale);

        details.put("sub process", getDefinitionId());

        Object instId = getSubprocessIds(inst, SUBPROCESS_INST_ID);
        details.put("instanceId of sub process", instId);

        return details;
    }

    public ValidationContext validate(Map options) {
        // TODO Auto-generated method stub
        ValidationContext vc = super.validate(options);

        if(!UEngineUtil.isNotEmpty(getDefinitionId())){
            vc.add(getActivityLabel() + " Definition is not specified.");
        }

        if(getForEachRole()!=null){
            boolean bExist = false;
            for(int i=0; i<getRoleBindings().length; i++){
                if(getRoleBindings()[i].getRole() == getForEachRole()){
                    bExist = true;
                    break;
                }
            }

            if(!bExist)
                vc.add(getActivityLabel() + " Although For-each-role ("+getForEachRole()+") is specified, there's no binding with the "+getForEachRole()+".");
        }

        return vc;
    }

    public Vector<String> getSubprocessIds(ProcessInstance instance) throws Exception{
        return getSubprocessIds(instance, SUBPROCESS_INST_ID);
    }

    public Vector<String> getSubprocessLabels(ProcessInstance instance) throws Exception{
        return getSubprocessIds(instance, SUBPROCESS_INST_LABELS);
    }

    public Vector<String> getSubprocessIds(ProcessInstance instance, String space) throws Exception{
        String spId = (String)instance.getProperty(getTracingTag(), space);
        String[] spIds = null;
        if(spId!=null && spId.trim().length() > 0)
            spIds = spId.split(",");
        else
            spIds = null;

        Vector<String> vtSpIds = new Vector<String>();
        if(spIds!=null)
            for(int i=0; i<spIds.length; i++){
                vtSpIds.add(spIds[i]);
            }

        return vtSpIds;
    }

    protected void setSubprocessIds(ProcessInstance instance, Vector<String> spIds, String space) throws Exception{
        StringBuffer spId = new StringBuffer();
        for (int i = 0; i < spIds.size(); i++) {
            if (spId.length() > 0) spId.append(",");
            spId.append(spIds.elementAt(i));
        }
        instance.setProperty(getTracingTag(), space, spId.toString());
    }

//    public void reset(ProcessInstance instance) throws Exception {
//        if(!isRunAndForget()){
//            Vector subProcesses = getSubProcesses(instance);
//
//            for(int i = 0; i<subProcesses.size(); i++){
//                ProcessInstance theSP = (ProcessInstance)subProcesses.get(i);
//
//                theSP.getProcessDefinition().compensate(theSP);
//            }
//        }
//
//        super.reset(instance);
//    }
//
//    public void skip(ProcessInstance instance) throws Exception {
//        Vector subProcesses = getSubProcesses(instance);
//
//        for(int i = 0; i<subProcesses.size(); i++){
//            ProcessInstance theSP = (ProcessInstance)subProcesses.get(i);
//
//            theSP.getProcessDefinition().skip(theSP);
//        }
//
//        super.skip(instance);
//    }
//
//    public void stop(ProcessInstance instance) throws Exception {
//        Vector subProcesses = getSubProcesses(instance);
//
//        for(int i = 0; i<subProcesses.size(); i++){
//            ProcessInstance theSP = (ProcessInstance)subProcesses.get(i);
//
//            theSP.getProcessDefinition().stop(theSP);
//        }
//
//        super.stop(instance);
//    }
//
//    public void compensate(ProcessInstance instance) throws Exception {
//        Vector subProcesses = getSubProcesses(instance);
//
//        for(int i = 0; i<subProcesses.size(); i++){
//            ProcessInstance theSP = (ProcessInstance)subProcesses.get(i);
//
//            theSP.getProcessDefinition().compensate(theSP);
//        }
//
//        super.compensate(instance);
//    }

    public String getDefinitionIdOnly(){
        String[] definitionAndVersion = ProcessDefinition.splitDefinitionAndVersionId(getDefinitionId());
        return definitionAndVersion[0];
    }


    public void attachSubProcess(ProcessInstance instance, String subProcessInstanceId, String label) throws Exception{
        Vector currSubProcessIds = getSubprocessIds(instance);
        Vector currSubProcessLabels = getSubprocessLabels(instance);

        currSubProcessIds.add(subProcessInstanceId);
        currSubProcessLabels.add(label);

        setSubprocessIds(instance, currSubProcessIds, SUBPROCESS_INST_ID);
        setSubprocessIds(instance, currSubProcessLabels, SUBPROCESS_INST_LABELS);
    }


    @Override
    public void fireComplete(ProcessInstance instance) throws Exception {

        boolean completable = onSubProcessReturn(instance, instance);


        if(completable && instance.getProcessTransactionContext().getSharedContext(SUB_PROCESS_IS_BEING_INSERTED) == null){
            super.fireComplete(instance);
        }

    }
}
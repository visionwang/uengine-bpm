package org.uengine.modeling.modeler;

import org.metaworks.MetaworksContext;
import org.metaworks.annotation.AutowiredToClient;
import org.metaworks.annotation.Hidden;
import org.metaworks.annotation.Id;
import org.metaworks.dwr.MetaworksRemoteService;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.*;
import org.uengine.kernel.bpmn.*;
import org.uengine.kernel.bpmn.face.ProcessVariablePanel;
import org.uengine.kernel.bpmn.view.EventView;
import org.uengine.kernel.bpmn.view.PoolView;
import org.uengine.kernel.bpmn.view.SequenceFlowView;
import org.uengine.kernel.view.ActivityView;
import org.uengine.kernel.view.RoleView;
import org.uengine.modeling.*;
import org.uengine.modeling.modeler.palette.BPMNPalette;
import org.uengine.util.ActivityFor;
import org.uengine.util.UEngineUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProcessModeler extends DefaultModeler {

    public final static String SUFFIX = ".process";

    ElementViewActionDelegate elementViewActionDelegate;

    @Hidden
    @AutowiredToClient
    public ElementViewActionDelegate getElementViewActionDelegate() {
        return elementViewActionDelegate;
    }

    public void setElementViewActionDelegate(ElementViewActionDelegate elementViewActionDelegate) {
        this.elementViewActionDelegate = elementViewActionDelegate;
    }

    public ProcessVariablePanel getProcessVariablePanel() {
        try {
            return ((BPMNPalette) getPalette()).getProcessVariablePalette().getProcessVariablePanel();
        } catch (Exception e) {
            return null;
        }
    }

    public ProcessModeler() {
        setType(SUFFIX);
        this.setCanvas(new ProcessCanvas(getType()));
        BPMNPalette bpmnPalette = null;
        try {
            bpmnPalette = MetaworksRemoteService.getComponent(BPMNPalette.class);
        } catch (Exception ex) {
            bpmnPalette = new BPMNPalette(getType());
        }
        this.setPalette(bpmnPalette);
    }

    String definitionId;
    @Id
        public String getDefinitionId() {
            return definitionId;
        }
        public void setDefinitionId(String definitionId) {
            this.definitionId = definitionId;
        }


    @Override
    public void setModel(IModel model) throws Exception {
        this.setModel(model, null);
    }

    public void setModel(IModel model, final ProcessInstance instance) throws Exception {
        if (model == null)
            return;


        if (instance == null) {
            setElementViewActionDelegate(new DefaultElementViewActionDelegate());
        }else{
            if(getMetaworksContext()==null){
                setMetaworksContext(new MetaworksContext());
            }

            getMetaworksContext().setWhere("instance");

        }


//        why clone? ---> ElementView is not found if def is reused. problem.
        ProcessDefinition def = (ProcessDefinition) GlobalContext.deserialize(GlobalContext.serialize(model, String.class), String.class);
        ;

        //ProcessDefinition def = (ProcessDefinition) model;
        def.validate(new HashMap());

        final List<ElementView> elementViewList = new ArrayList<ElementView>();
        final List<RelationView> relationViewList = new ArrayList<RelationView>();



        if (def.getRoles() != null) {
            for (Role role : def.getRoles()) {
                if (role.getElementView() != null) {
                    ElementView elementView = role.getElementView();
                    role.setElementView(null);
                    elementView.setElement(role);

                    TextContext text = role.getDisplayName();
                    elementView.setLabel(text.getText());
                    elementViewList.add(elementView);
                }
            }
        }

        if (def.getPools() != null) {
            for (Pool pool : def.getPools()) {
                if (pool.getElementView() != null) {
                    ElementView elementView = pool.getElementView();
                    pool.setElementView(null);
                    elementView.setElement(pool);

                    elementView.setLabel(pool.getDescription());
                    elementViewList.add(elementView);
                }
            }
        }


        /**
         * on Load ProcessDefinition
         * if Acitivity is SubProcesss, get ChildActvities and adding to elementViewList
         */
        ActivityFor addingElemenViewLoop = new ActivityFor() {

            @Override
            public void logic(Activity activity) {


                if (activity instanceof FlowActivity) {
                    ArrayList<SequenceFlow> sequenceFlowList = ((FlowActivity) activity).getSequenceFlows();
                    for (IRelation relation : sequenceFlowList) {
                        SequenceFlow sequenceFlow = (SequenceFlow) relation;
                        SequenceFlowView sequenceFlowView = (SequenceFlowView) sequenceFlow.getRelationView();
                        sequenceFlow.setRelationView(null);

                        if (sequenceFlowView != null) {
                            sequenceFlowView.setRelation(sequenceFlow);
                            relationViewList.add(sequenceFlowView);
                        }
                    }
                }


                ElementView elementView = activity.getElementView();

                activity.setElementView(null); //prevent cyclic reference

                if (elementView == null) {
                    System.err.println("ElementView is not found for activity [" + activity + "]");
                    //TODO: should be generated if elementView is not valid
                    return;
                }

                if (instance != null) {
                    try {
                        elementView.setInstStatus(instance.getStatus(activity.getTracingTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                elementView.setElement(activity);

                if(!(activity instanceof ProcessDefinition))
                    elementViewList.add(elementView);


            }
        };

//		if(def.getRoles()!=null && getRolePanel()!=null) {
//			this.getRolePanel().setRoleList(Arrays.asList(def.getRoles()));
//		}

        if (def.getProcessVariables() != null && getProcessVariablePanel() != null) {
            this.getProcessVariablePanel().setProcessVariableList(new ArrayList<ProcessVariable>());
            this.getProcessVariablePanel().getProcessVariableList().addAll(Arrays.asList(def.getProcessVariables()));
        }

       addingElemenViewLoop.run(def);


        this.getCanvas().setElementViewList(elementViewList);
        this.getCanvas().setRelationViewList(relationViewList);

        def.updateActivitySequence();

        {//setting properties
            setGlobal(def.isGlobal());

        }

        //TODO:  someday must be changed to metaworks can designate multiple canvas instances.
        if(instance!=null){
            for(ElementView elementView : elementViewList){
                elementView.setId("inst_" + elementView.getId());
            }
            for(RelationView relationView : relationViewList){
                relationView.setId("inst_" + relationView.getId());
                relationView.setFrom("inst_" + relationView.getFrom());
                relationView.setTo("inst_" + relationView.getTo());
            }

            getCanvas().setModelerType("inst_" + getCanvas().getModelerType());

        }
    }

    boolean global;
        public boolean isGlobal() {
            return global;
        }
        public void setGlobal(boolean global) {
            this.global = global;
        }




    public IModel createModel() {
        try {
            return makeProcessDefinitionFromCanvas(getCanvas());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void preventDuplicateRoleNames(Role role, Role[] roles) {
        String usedRoleName;
        String ROLE_SUFFIX = "Role";
        int roleIndex = 1;
        String roleName = role.getName();

        if (roleName == null || "".equals(roleName)) {
            roleName = ROLE_SUFFIX + '-' + roleIndex;
        }
        if (roles == null) {
            role.setName(roleName);
            return;
        }

        boolean isUsedRoleName = false;
        for (int i = 0; i < roles.length; i++) {
            usedRoleName = roles[i].getName();
            if (roleName.equals(usedRoleName)) {
                isUsedRoleName = true;
            }
        }

        if (!isUsedRoleName) {
            role.setName(roleName);
            return;
        }

        isUsedRoleName = false;
        String tempRoleName;
        while (!isUsedRoleName) {
            isUsedRoleName = false;
            tempRoleName = ROLE_SUFFIX + '-' + roleIndex;
            for (int i = 0; i < roles.length; i++) {
                usedRoleName = roles[i].getName();
                if (tempRoleName.equals(usedRoleName)) {
                    isUsedRoleName = true;
                }
            }
            if (!isUsedRoleName) {
                roleName = tempRoleName;
                break;
            } else {
                isUsedRoleName = false;
                roleIndex++;
            }
        }
        role.setName(roleName);
    }

    public ProcessDefinition makeProcessDefinitionFromCanvas(Canvas canvas) throws Exception {
        ProcessDefinition def = createEmptyProcessDefinition();

        {//set the properties first.
            def.setGlobal(isGlobal());
        }

        HashMap<String, String> tracingTags = new HashMap<String, String>();

        if (getProcessVariablePanel() != null && getProcessVariablePanel().getProcessVariableList() != null) {
            ProcessVariable processVariables[] = new ProcessVariable[getProcessVariablePanel().getProcessVariableList().size()];
            getProcessVariablePanel().getProcessVariableList().toArray(processVariables);
            def.setProcessVariables(processVariables);
        }


        List<ElementView> parentElementView = new ArrayList<ElementView>();

        for (ElementView elementView : canvas.getElementViewList()) {

            parentElementView.add(elementView);

            //child activities in the getElement() should be removed before rearranging them
            if (elementView.getElement() instanceof FlowActivity) {
                FlowActivity flowActivity = ((FlowActivity) elementView.getElement());

                if (flowActivity.getChildActivities() != null)
                    flowActivity.getChildActivities().clear();
            }

            if (elementView.getElement() instanceof Activity) {
                Activity activity = (Activity) elementView.getElement();
                activity.setName(elementView.getLabel());

                try {
                    long tracingTagNumber = Long.parseLong(activity.getTracingTag());
                    if (def.getActivitySequence() < tracingTagNumber) {
                        def.setActivitySequence(tracingTagNumber);
                    }
                } catch (Exception e) {

                }


            }

        }

        //add roles and pools firstly
        for (ElementView elementView : canvas.getElementViewList()) {

            //bounds wrong positioned elements
            if(elementView.getY() < 0){
                elementView.setY(0);
            }
            if(elementView.getX() < 0){
                elementView.setX(0);
            }
            //end

            if (elementView.getElement() instanceof Role) {

                Role[] roles = null;
                Role role = (Role) elementView.getElement();
                //elementView.setElement(null);
                role.setElementView(elementView);
                role.setName(elementView.getLabel());
                preventDuplicateRoleNames(role, def.getRoles());
                role.setDisplayName(role.getName());

                if (def.getRoles() == null) {
                    roles = new Role[1];
                    roles[0] = role;
                    def.setRoles(roles);

                } else {
                    int prevLength = def.getRoles().length;
                    def.addRole(role);

                    //TODO: enable and introspect why in the future
//					if(prevLength == def.getRoles().length){
//						throw new UEngineException("There are duplicated names among lanes.");
//					}
                }
            } else if (elementView.getElement() instanceof Pool) {

                Pool pool = (Pool) elementView.getElement();
                //elementView.setElement(null);
                pool.setElementView(elementView);
                pool.setName(elementView.getLabel());
                pool.setDescription(elementView.getLabel());

                //Role role = Role.forName(pool.getName());

                if (def.getPools() == null) {
                    List<Pool> pools = new ArrayList<Pool>();
                    def.setPools(pools);
                }

                def.addPool(pool);
                //def.addRole(role);

            }
        }

        //add activities later
        for (ElementView elementView : canvas.getElementViewList()) {

           if (elementView.getElement() instanceof Activity) {
                Activity activity = (Activity) elementView.getElement();
                activity.setName(elementView.getLabel());
                activity.setElementView(elementView);

                FlowActivity parentActivity = findParentActivity(elementView, parentElementView);

                if (parentActivity == null)
                    parentActivity = def;

                //TODO:  if tracingTag collision occurs, issue new id. This may cause undesired operation.
                if (tracingTags.containsKey(activity.getTracingTag()))
                    activity.setTracingTag("" + def.getNextActivitySequence());

                parentActivity.addChildActivity(activity);
                tracingTags.put(activity.getTracingTag(), activity.getTracingTag());


                if (activity instanceof Event) {
                    Activity toAttachActivity = findAttachedActivity(elementView, canvas.getElementViewList());

                    if (toAttachActivity != null)
                        ((Event) activity).setAttachedToRef(toAttachActivity.getTracingTag());
                } else if (activity instanceof HumanActivity) {
                    HumanActivity humanActivity = (HumanActivity) activity;

                    //Pool pool = findParentPool(elementView, canvas.getElementViewList());
                    Role role = findParentRole(elementView, canvas.getElementViewList());

                    if (role != null)
                        humanActivity.setRole(Role.forName(role.getName()));
                } else if (activity instanceof FlowActivity) {
                    ((FlowActivity) activity).setSequenceFlows(null);
                }

               activity.setOutgoingSequenceFlows(new ArrayList<SequenceFlow>());
               activity.setIncomingSequenceFlows(new ArrayList<SequenceFlow>());
            }

        }

        //give tracingTag if null

        final ProcessDefinition finalDef = def;
        ActivityFor giveTracingTagIfNull = new ActivityFor() {

            @Override
            public void logic(Activity activity) {

                if(!UEngineUtil.isNotEmpty(activity.getTracingTag())){
                    finalDef.updateActivitySequence();

                    long newTT = finalDef.getActivitySequence() + 1;
                    activity.setTracingTag(String.valueOf(newTT));
                    finalDef.setActivitySequence(newTT);
                }

            }
        };
        giveTracingTagIfNull.run(def);


        for (RelationView relationView : this.getCanvas().getRelationViewList()) {


            SequenceFlow sequenceFlow = (SequenceFlow) relationView.getRelation();

            //TODO: fix later
            if (sequenceFlow == null)
                continue;

            String sourceRef = relationView.getFrom().substring(0, relationView.getFrom().indexOf("_TERMINAL_"));
            String targetRef = relationView.getTo().substring(0, relationView.getTo().indexOf("_TERMINAL_"));

            // in graphic side, the sourceRef and targetRef indicates the id of view, in the model, the two values must be in tracingt
            for (ElementView elementView : this.getCanvas().getElementViewList()) {
                if (sourceRef.equals(elementView.getId())) {

                    Activity fromActivity = (Activity) elementView.getElement();
                    sequenceFlow.setSourceRef(fromActivity.getTracingTag());
                    relationView.setFrom(elementView.getId() + relationView.TERMINAL_IN_OUT);
                }

                if (targetRef.equals(elementView.getId())) {

                    Activity toActivity = (Activity) elementView.getElement();
                    sequenceFlow.setTargetRef(toActivity.getTracingTag());
                    relationView.setTo(elementView.getId() + relationView.TERMINAL_IN_OUT);
                }

            }

            relationView.setRelation(null);
            sequenceFlow.setRelationView((SequenceFlowView) relationView);

            FlowActivity parentActivity = findParentActivity(relationView, parentElementView);

            if (parentActivity == null)
                parentActivity = def;


            if(sequenceFlow.getTargetRef()==null
                    || sequenceFlow.getSourceRef()==null
                    || sequenceFlow.getTargetRef().equals(sequenceFlow.getSourceRef()))
                    continue;//ignores corrupt sequence flow

            parentActivity.addSequenceFlow(sequenceFlow);

        }

        for (ElementView elementView : canvas.getElementViewList()) {
            elementView.setElement(null);
        }

        def.afterDeserialization();
        return def;
    }


    protected ProcessDefinition createEmptyProcessDefinition() {
        return new ProcessDefinition();
    }

    private Activity findAttachedActivity(ElementView eventView, List<ElementView> elementViews) {
        // eventView size
        double event_x_min = (eventView.getX()) - (Math.abs((eventView.getWidth()) / 2));
        double event_x_max = (eventView.getX()) + (Math.abs((eventView.getWidth()) / 2));
        double event_y_min = (eventView.getY()) - (Math.abs((eventView.getHeight()) / 2));
        double event_y_max = (eventView.getY()) + (Math.abs((eventView.getHeight()) / 2));

        for (ElementView elementView : elementViews) {
            if (!(elementView instanceof EventView)) {
                //if(elementView.getX() != null) {
                // elementView size
                double element_x_min = (elementView.getX()) - (Math.abs((elementView.getWidth()) / 2));
                double element_x_max = (elementView.getX()) + (Math.abs((elementView.getWidth()) / 2));
                double element_y_min = (elementView.getY()) - (Math.abs((elementView.getHeight()) / 2));
                double element_y_max = (elementView.getY()) + (Math.abs((elementView.getHeight()) / 2));

                boolean checkMinX = (element_x_min <= event_x_min) && (event_x_min <= element_x_max);
                boolean checkMaxX = (element_x_min <= event_x_max) && (event_x_max <= element_x_max);

                boolean checkMinY = (element_y_min <= event_y_min) && (event_y_min <= element_y_max);
                boolean checkMaxY = (element_y_min <= event_y_max) && (event_y_max <= element_y_max);

                if ((checkMinX || checkMaxX) && (checkMinY || checkMaxY) && elementView.getElement() instanceof Activity) {
                    return (Activity) elementView.getElement();
                }
                //}
            }
        }
        return null;
    }

    private FlowActivity findParentActivity(Object what, List<ElementView> parentElementView) {

        double maxParentSize = 999999999; //big enough value
        FlowActivity candidateParent = null;

        for (ElementView elementView : parentElementView) {

            if (!(elementView instanceof ActivityView))
                continue;

            double x = 0;
            double y = 0;
            double width = 0;
            double height = 0;
            double leftLine = 0;
            double rightLine = 0;
            double topLine = 0;
            double bottomLine = 0;

            if (what instanceof ElementView) {
                ElementView activityView = (ElementView) what;

                x = (activityView.getX());
                y = (activityView.getY());
                width = (activityView.getWidth());
                height = (activityView.getHeight());

                leftLine = x - width / 2;
                rightLine = x + width / 2;
                topLine = y + height / 2;
                bottomLine = y - height / 2;

            } else if (what instanceof RelationView) {
                RelationView relationView = (RelationView) what;

                x = (relationView.getX());
                y = (relationView.getY());
                width = (relationView.getWidth());
                height = (relationView.getHeight());

                leftLine = x - width / 2;
                rightLine = x + width / 2;
                topLine = y + height / 2;
                bottomLine = y - height / 2;
            } else {
                continue;
            }

            //if(elementView.getX() != null) {
            double p_x = (elementView.getX());
            double p_y = (elementView.getY());
            double p_width = (elementView.getWidth());
            double p_height = (elementView.getHeight());
            double p_leftLine = p_x - p_width / 2;
            double p_rightLine = p_x + p_width / 2;
            double p_topLine = p_y + p_height / 2;
            double p_bottomLine = p_y - p_height / 2;

            if (p_leftLine < leftLine &&
                    p_rightLine > rightLine &&
                    p_topLine > topLine &&
                    p_bottomLine < bottomLine
                    ) { //if the activity is in the parent

                double parentSize = (p_rightLine - p_leftLine) * (p_topLine - p_bottomLine);

                if (parentSize < maxParentSize) { //the smallest one is just the parent not grand parent.
                    candidateParent = (FlowActivity) elementView.getElement();
                    maxParentSize = parentSize;
                }
            }
            //}
        }

        return candidateParent;
    }

    public Pool findParentPool(ElementView elementView, List<ElementView> elementViewList) {
        for (ElementView ev : elementViewList) {
            if (!(ev instanceof PoolView)) {
                continue;
            }

            if (isIn(elementView, ev)) { //TODO
                return (Pool) ev.getElement();
            }
        }

        return null;
    }

    public Role findParentRole(ElementView elementView, List<ElementView> elementViewList) {
        for (ElementView ev : elementViewList) {
            if (!(ev instanceof RoleView)) {
                continue;
            }

            if (!(ev.getId().equals("rootRole"))) {
                if (isIn(elementView, ev)) { //TODO
                    return (Role) ev.getElement();
                }
            }
        }

        return null;
    }

    private boolean isIn(ElementView elem1, ElementView elem2) {
        double x = (elem1.getX());
        double y = (elem1.getY());
        double width = (elem1.getWidth());
        double height = (elem1.getHeight());
        double left = x - (width / 2);
        double right = x + (width / 2);
        double top = y - (height / 2);
        double bottom = y + (height / 2);

        double p_x = (elem2.getX());
        double p_y = (elem2.getY());
        double p_width = (elem2.getWidth());
        double p_height = (elem2.getHeight());
        double p_left = p_x - (p_width / 2);
        double p_right = p_x + (p_width / 2);
        double p_top = p_y - (p_height / 2);
        double p_bottom = p_y + (p_height / 2);

        return (p_left < left &&
                p_right > right &&
                p_top < top &&
                p_bottom > bottom
        );
    }

    public ElementView findConnectedEvent(ElementView elementView, List<RelationView> relationViewList, List<ElementView> elementViewList) {

        String id = null;

        for (RelationView relationView : relationViewList) {
            for (String toedge : elementView.getToEdge().split(",")) {
                if (toedge.equals(relationView.getId())) {
                    id = relationView.getTo().split("_TERMINAL")[0];
                    break;
                }
            }
        }

        for (ElementView ev : elementViewList) {
            if ((ev.getId() != null) && (ev.getId().equals(id))) {
                return ev;
            }
        }

        return null;
    }

}

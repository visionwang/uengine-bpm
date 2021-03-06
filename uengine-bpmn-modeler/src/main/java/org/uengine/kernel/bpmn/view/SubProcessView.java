package org.uengine.kernel.bpmn.view;

import org.metaworks.EventContext;
import org.metaworks.ServiceMethodContext;
import org.metaworks.annotation.AutowiredFromClient;
import org.metaworks.annotation.ServiceMethod;
import org.uengine.kernel.bpmn.face.ProcessVariablePanel;
import org.uengine.kernel.view.ScopeActivityView;

public class SubProcessView extends ScopeActivityView{

    public final static String SHAPE_ID = "OG.shape.bpmn.ScopeActivity";

    public SubProcessView(){
        setShapeId(this.SHAPE_ID);
    }
    @ServiceMethod(callByContent = true, eventBinding = EventContext.EVENT_DBLCLICK, target = ServiceMethodContext.TARGET_POPUP)
    public Object showProperty(@AutowiredFromClient ProcessVariablePanel processVariablePanel) throws Exception {
        return super.showProperty();
    }


}

package org.uengine.social;

import java.util.HashSet;
import java.util.Set;

import org.metaworks.MetaworksContext;
import org.metaworks.annotation.Face;
import org.metaworks.annotation.Range;
import org.metaworks.dwr.MetaworksRemoteService;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.ResourceNavigator;
import org.uengine.modeling.resource.resources.ClassResource;
import org.uengine.modeling.resource.resources.JavaClassResource;
import org.uengine.modeling.resource.resources.URLappResource;
import org.uengine.processadmin.ProcessAdminResourceNavigator;
import org.uengine.processadmin.ResourceControlDelegateForProcessVariableSelector;

/**
 * Created by jangjinyoung on 15. 9. 17..
 */
@Face(ejsPath = "genericfaces/CleanObjectFace.ejs")
public class SocialBPMProcessVariableTypeSelectorPopup {

    String type;
    @Range(options={"Text","Number", "Date","Complex"}, values={"java.lang.String","java.lang.Long", "java.util.Date","org.uengine.contexts.ComplexType"})
    public String getType() {
        return type;
    }
    public void setType(String primitypeTypeName) {
        this.type = primitypeTypeName;
    }


    ResourceNavigator classResourceNavigator;
    public ResourceNavigator getClassResourceNavigator() {
        return classResourceNavigator;
    }
    public void setClassResourceNavigator(ResourceNavigator classResourceNavigator) {
        this.classResourceNavigator = classResourceNavigator;
    }


//    @ServiceMethod(callByContent = true)
//    public void select(@AutowiredFromClient SelectedResource selectedComplexClassResource){
//        SocialBPMProcessVariableTypeSelector socialBPMProcessVariableTypeSelector = new SocialBPMProcessVariableTypeSelector();
//        socialBPMProcessVariableTypeSelector.setSelectedClassName(selectedComplexClassResource.getPath());
//
//
//       // MetaworksRemoteService.wrapReturn(new ToOpener(socialBPMProcessVariableTypeSelector), new Remover(new ModalWindow()));
//
////        return socialBPMProcessVariableTypeSelector;
//        MetaworksRemoteService.wrapReturn(new Remover(new ModalWindow()),socialBPMProcessVariableTypeSelector);
//    }
//

    public SocialBPMProcessVariableTypeSelectorPopup(){
        ProcessAdminResourceNavigator classResourceNavigator = new ProcessAdminResourceNavigator();

        Set<Class> resourceTypes = new HashSet<Class>();
        resourceTypes.add(ClassResource.class);
        resourceTypes.add(URLappResource.class);
        resourceTypes.add(JavaClassResource.class);

        classResourceNavigator.getRoot().filtResources(resourceTypes, false);

        MetaworksRemoteService.autowire(classResourceNavigator);


        {
            DefaultResource primitive = new JavaClassResource();
            primitive.setPath("java.lang.String");
            classResourceNavigator.getRoot().getChildren().add(0, primitive);
        }
        {
            DefaultResource primitive = new JavaClassResource();
            primitive.setPath("java.lang.Long");
            classResourceNavigator.getRoot().getChildren().add(0, primitive);
        }
        {
            DefaultResource primitive = new JavaClassResource();
            primitive.setPath("java.lang.Double");
            classResourceNavigator.getRoot().getChildren().add(0, primitive);
        }

        {
            DefaultResource primitive = new JavaClassResource();
            primitive.setPath(RoleUser.class.getName());
            classResourceNavigator.getRoot().getChildren().add(0, primitive);
        }



        classResourceNavigator.getRoot().setMetaworksContext(new MetaworksContext());
        classResourceNavigator.getRoot().getMetaworksContext().setWhen(MetaworksContext.WHEN_VIEW);

        classResourceNavigator.setResourceControlDelegate(new ResourceControlDelegateForProcessVariableSelector());

        setClassResourceNavigator(classResourceNavigator);


    }
}

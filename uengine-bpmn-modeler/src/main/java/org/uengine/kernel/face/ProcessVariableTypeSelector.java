package org.uengine.kernel.face;

import org.metaworks.ContextAware;
import org.metaworks.Face;
import org.metaworks.MetaworksContext;
import org.metaworks.annotation.AutowiredFromClient;
import org.metaworks.annotation.NonEditable;
import org.metaworks.annotation.Range;
import org.metaworks.annotation.ServiceMethod;
import org.uengine.contexts.ComplexType;
import org.uengine.modeling.resource.ResourceNavigator;
import org.uengine.modeling.resource.SelectedResource;

/**
 * Created by jangjinyoung on 15. 7. 18..
 */
public class ProcessVariableTypeSelector implements Face<String>{

    String type;
	/**
	 * ProcessAdminContainerResource classRoot;
	 * //@Available(condition = "primitypeTypeName == 'org.uengine.contexts.ComplexType'")
	 * public ProcessAdminContainerResource getClassRoot() {
	 * return classRoot;
	 * }
	 * public void setClassRoot(ProcessAdminContainerResource classRoot) {
	 * this.classRoot = classRoot;
	 * }
	 */
	ResourceNavigator classResourceNavigator;
	String selectedClassName;
	MetaworksContext metaworksContext;
    @Range(options={"Text","Number", "Date","Complex"}, values={"java.lang.String","java.lang.Long", "java.util.Date","org.uengine.contexts.ComplexType"})
        public String getType() {
            return type;
        }
        public void setType(String primitypeTypeName) {
            this.type = primitypeTypeName;
        }



    @Override
    public void setValueToFace(String value) {
//        setOptionNames(new ArrayList<String>());
//        getOptionNames().add("Text");
//        getOptionNames().add("Number");
//        getOptionNames().add("Date");



    }



    @Override
    public String createValueFromFace() {

        return getType();

    }

	public ResourceNavigator getClassResourceNavigator() {
		return this.classResourceNavigator;
	}

	public void setClassResourceNavigator(ResourceNavigator classResourceNavigator) {
		this.classResourceNavigator = classResourceNavigator;
	}

	public String getSelectedClassName() {
		return this.selectedClassName;
	}

	public void setSelectedClassName(String selectedClassName) {
		this.selectedClassName = selectedClassName;
	}

	public MetaworksContext getMetaworksContext() {
		return this.metaworksContext;
	}

	public void setMetaworksContext(MetaworksContext metaworksContext) {
		this.metaworksContext = metaworksContext;
	}

	/**
	 * 
	 * @param selectedComplexClassResource
	 */
	@ServiceMethod(callByContent=true, eventBinding="change", bindingFor="primitypeTypeName")
	public void select(@AutowiredFromClient()
	SelectedResource selectedComplexClassResource) {
		// TODO - implement ProcessVariableTypeSelector.select
		throw new UnsupportedOperationException();
	}

	public ProcessVariableTypeSelector() {
	}
}

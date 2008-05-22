package org.netbeans.modules.iep.editor.model;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;

import org.netbeans.modules.iep.model.lib.TcgComponentType;

public class ModelObjectFactory {

    private static ModelObjectFactory mInstance;
    
    public static ModelObjectFactory getInstance() {
        
        if(mInstance == null) {
            mInstance = new ModelObjectFactory();
        }
        
        return mInstance;
    }
    
    public OperatorComponent createOperatorComponent(String componentPath, IEPModel model) {
        OperatorComponent opComponent = null;
        
        TcgComponentType componentType = TcgModelManager.getTcgComponentType(componentPath);
        if(componentType != null) {
            //opComponent = model.getFactory().createOperator(model);
            opComponent = createComponent(componentType, model);
            opComponent.setType(componentType.getPath());
            List properties = componentType.getPropertyTypeList();
            Iterator it = properties.iterator();
            while(it.hasNext()) {
                TcgPropertyType prop = (TcgPropertyType) it.next();
                if(!prop.isTransient()) {
                    String name = prop.getName();
                    String defaultValue = prop.getDefaultValueAsString();
                    Property property = model.getFactory().createProperty(model);
                    property.setName(name);
                    if(defaultValue != null) {
                        property.setValue(defaultValue);
                    }
                    opComponent.addProperty(property);
                }
            }
            
            /*
            List childTypes = componentType.getComponentTypeList();
            for (int i = 0, j = childTypes.size(); i < j; i++) {
                TcgComponentType childType = (TcgComponentType) childTypes.get(i);
                OperatorComponent cComponent = model.getFactory().createOperator(model);
                opComponent.addChildComponent(cComponent);
            }*/
        }
            
        return opComponent;   
    }
    
    
    
    private OperatorComponent createComponent(TcgComponentType componentType, IEPModel model) {
        OperatorComponent opComponent = null;
    
        if(componentType != null) {
            if(componentType.getPath().endsWith("Input")) {
                opComponent = model.getFactory().createInputOperator(model);
            } else if(componentType.getPath().endsWith("Output")) {
                opComponent = model.getFactory().createOutputOperator(model);
            } else if(componentType.getPath().endsWith("InvokeStream")) {
                                opComponent = model.getFactory().createInvokeStreamOperator(model);
                        } else {
                opComponent = model.getFactory().createOperator(model);
            }
            opComponent.setType(componentType.getPath());
        
        }
        
        return opComponent;
    }
}

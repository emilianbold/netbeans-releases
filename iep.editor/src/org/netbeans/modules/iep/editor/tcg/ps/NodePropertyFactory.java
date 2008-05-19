package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

public class NodePropertyFactory {

    private static NodePropertyFactory mInstance;
    
    public static NodePropertyFactory getInstance() {
        
        if(mInstance == null) {
            mInstance = new NodePropertyFactory();
        }
        
        return mInstance;
    }
    
    public Node.Property getProperty(TcgPropertyType type, OperatorComponent component) {
        Node.Property property = null;
        try {
            if(type != null && component != null) {
                String propName =  type.getName();
            
                Property prop = component.getProperty(type.getName());
                //if a valid property not propertyEditor which we do not save in component
                if(prop != null) {
                    property = SingleTcgComponentNodeProperty.newInstance(prop, component, component.getModel());
                } else if(propName != null && propName.equals("propertyEditor")) {
                    property = TcgComponentNodeProperty.newInstance(type, component, component.getModel());
                } else {
                    property = SingleNonPersistentTcgComponentNodeProperty.newInstance(type, component, component.getModel());
                }
                
            }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        return property;
    }
}

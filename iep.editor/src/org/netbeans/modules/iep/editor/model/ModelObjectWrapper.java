package org.netbeans.modules.iep.editor.model;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;

public class ModelObjectWrapper {

    private OperatorComponent mComponent;
    
    private TcgComponentType mComponentType;
    
    public ModelObjectWrapper(OperatorComponent component, TcgComponentType componentType) {
        this.mComponent = component;
        this.mComponentType = componentType;
    }
    
    public OperatorComponent getComponent() {
        return this.mComponent;
    }
    
    public TcgComponentType getComponentType() {
        return this.mComponentType;
    }
}

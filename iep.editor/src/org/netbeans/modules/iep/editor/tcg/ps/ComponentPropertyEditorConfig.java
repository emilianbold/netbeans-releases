package org.netbeans.modules.iep.editor.tcg.ps;

import java.beans.PropertyEditor;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;

public interface ComponentPropertyEditorConfig extends PropertyEditor {

    void setOperatorComponent(OperatorComponent component);
    
    OperatorComponent getOperatorComponent();
    
    void setPropertyType(TcgPropertyType type);
    
    TcgPropertyType getPropertyType();
    
    IEPModel getModel();
}

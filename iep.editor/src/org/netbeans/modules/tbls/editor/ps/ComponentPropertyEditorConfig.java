package org.netbeans.modules.tbls.editor.ps;

import java.beans.PropertyEditor;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.model.TcgPropertyType;

public interface ComponentPropertyEditorConfig extends PropertyEditor {

    void setOperatorComponent(OperatorComponent component);
    
    OperatorComponent getOperatorComponent();
    
    void setPropertyType(TcgPropertyType type);
    
    TcgPropertyType getPropertyType();
    
    IEPModel getModel();
}

package org.netbeans.modules.tbls.editor.ps;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class DocumentationProperty extends Node.Property  {

    private Component mComponent;
    
    public DocumentationProperty(Class valueType, Component component) {
        super(valueType);
        this.mComponent = component;
        this.setDisplayName(NbBundle.getMessage(DocumentationProperty.class, "Documentation_property_displayName"));
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        Documentation doc = this.mComponent.getDocumentation();
        
        if(doc != null) {
            return doc.getTextContent();
        }
        return "";
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Documentation doc = this.mComponent.getDocumentation();
        IEPModel model = this.mComponent.getModel();
        
        if(val != null) {
            if(!val.toString().equals("")) {
                if(doc != null) {
                    model.startTransaction();
                    doc.setTextContent(val.toString());
                    model.endTransaction();
                } else {
                    doc = model.getFactory().createDocumentation(model);
                    doc.setTextContent(val.toString());
                    model.startTransaction();
                    this.mComponent.setDocumentation(doc);
                    model.endTransaction();
                }
            } else {
                model.startTransaction();
                this.mComponent.setDocumentation(null);
                model.endTransaction();
            }
        } else {
            model.startTransaction();
            this.mComponent.setDocumentation(null);
            model.endTransaction();
        }
    }

}

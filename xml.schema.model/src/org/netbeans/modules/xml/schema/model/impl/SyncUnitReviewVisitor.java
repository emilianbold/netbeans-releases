/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.AllElement;
import org.netbeans.modules.xml.schema.model.AllElementReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.DefaultSchemaVisitor;
import org.netbeans.modules.xml.xam.xdm.SyncUnit;

/**
 *
 * @author Nam Nguyen
 */
public class SyncUnitReviewVisitor extends DefaultSchemaVisitor {
    private SyncUnit unit;
    
    /** Creates a new instance of SyncUnitFillVisitor */
    public SyncUnitReviewVisitor() {
    }
    
    public SyncUnit review(SyncUnit unit) {
        this.unit = unit;
        ((SchemaComponent)unit.getTarget()).accept(this);
        return unit;
    }
    
    static private SchemaModelImpl getSchemaModel(SchemaComponent c) {
        return (SchemaModelImpl) c.getModel();
    }
    
    private void fixSyncUnit(SchemaComponentImpl atarget) {
        unit = new SyncUnit(atarget.getParent());
        unit.addToRemoveList(atarget);
        unit.addToAddList(getSchemaModel(atarget).createComponent(atarget.getParent(), atarget.getPeer()));
    }
    
    public void visit(LocalElement target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.REF) != null) {
            fixSyncUnit(atarget);
        }
    }
    
    public void visit(ElementReference target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.TYPE) != null) {
            fixSyncUnit(atarget);
        }
    }

    public void visit(AllElement target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.REF) != null) {
            fixSyncUnit(atarget);
        }
    }

    public void visit(AllElementReference target) {
        SchemaComponentImpl atarget = (SchemaComponentImpl) target;
        if (atarget.getAttributeValue(SchemaAttributes.TYPE) != null) {
            fixSyncUnit(atarget);
        }
    }
    
}

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.refactoring.xsd;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class RenameSchemaReferenceVisitor extends DefaultVisitor implements WSDLVisitor {
    private ReferenceableSchemaComponent renamedReferenced;
    private String oldName;
    
    /** Creates a new instance of FindUsageVisitor */
    public RenameSchemaReferenceVisitor() {
    }
    
    public void rename(RenameRequest request, UsageGroup usage) throws IOException {
        if (request == null || usage == null || usage.getModel() == null) return;
        if (! (usage.getModel() instanceof WSDLModel)) return;
        if (! (request.getTarget() instanceof ReferenceableSchemaComponent) ||
                request.getOldName() == null) {
            return;
        }

        WSDLModel model = (WSDLModel) usage.getModel();
        boolean startTransaction = ! model.isIntransaction();
        renamedReferenced = (ReferenceableSchemaComponent) request.getRenamedTarget();
        oldName = request.getOldName();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            Collection<Usage> items = usage.getItems();
            for (Usage item : items) {
                if (item.isIncludedInRefactoring() &&
                    item.getComponent() instanceof WSDLComponent) {
                    WSDLComponent referencing = (WSDLComponent)item.getComponent();
                    referencing.accept(this);
                }
            }
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
    private <T extends ReferenceableSchemaComponent> NamedComponentReference<T>
            createReference(Class<T> type, WSDLComponent referencing) {
        if (type.isAssignableFrom(renamedReferenced.getClass())) {
            return referencing.createSchemaReference(type.cast(renamedReferenced), type);
        } else {
            assert false : type.getName()+" is not assignable from "+renamedReferenced.getClass().getName();
            return null;
        }
    }
    
    public void visit(Part part) {
        if (renamedReferenced instanceof GlobalElement &&
            part.getElement().getQName().getLocalPart().equals(oldName)) 
        {
            NamedComponentReference<GlobalElement> ref = createReference(GlobalElement.class, part);
            if (ref != null) {
                part.setElement(ref);
                part.setType(null);
            }
        } else if (renamedReferenced instanceof GlobalType &&
                   part.getType().getQName().getLocalPart().equals(oldName)) 
        {
            NamedComponentReference<GlobalType> ref = createReference(GlobalType.class, part);
            if (ref != null) {
                part.setType(ref);
                part.setElement(null);
            }
        }
    }
}
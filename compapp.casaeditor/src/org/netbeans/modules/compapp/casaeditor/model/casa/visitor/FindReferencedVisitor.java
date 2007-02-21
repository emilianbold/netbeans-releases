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

package org.netbeans.modules.compapp.casaeditor.model.casa.visitor;

import org.netbeans.modules.compapp.casaeditor.model.casa.Casa;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;


/**
 *
 * @author Nam Nguyen
 */
public class FindReferencedVisitor<T extends ReferenceableCasaComponent> 
        extends CasaComponentVisitor.Default {
    private Class<T> type;
    private String localName;
    private T referenced = null;
    private Casa root;
    
    /** Creates a new instance of FindReferencedVisitor */
    public FindReferencedVisitor(Casa root) {
        this.root = root;
    }
    
    public T find(String localName, Class<T> type) {
        this.type = type;
        this.localName = localName;
        visitChildren(root);
        return referenced;
    }
    
    private void checkReference(ReferenceableCasaComponent c, boolean checkChildren) {
         if (type.isAssignableFrom(c.getClass()) && c.getName() != null &&
                 c.getName().equals(localName)) {
             referenced = type.cast(c);
             return;
         } else if (checkChildren) {
             visitChildren(c);
         }
    }
    
    private void visitChildren(CasaComponent c) {
        for (CasaComponent child : c.getChildren()) {
           if (referenced != null) { // before start each visit
                return;
           }
            child.accept(this);
        }
    }
    
    public void visit(CasaEndpoints endpoints) { // TMP FIXME
        visitChildren(endpoints);
    }

    public void visit(CasaEndpoint target) {
        checkReference(target, true);        
    }    
}

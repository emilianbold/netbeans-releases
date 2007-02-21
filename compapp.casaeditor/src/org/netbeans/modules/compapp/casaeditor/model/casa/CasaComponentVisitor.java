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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaEndpointImpl;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;

/**
 *
 * @author jqian
 */
public interface CasaComponentVisitor {
    
    void visit(Casa target);
    void visit(CasaServiceUnits target);
    void visit(CasaServiceEngineServiceUnit target);
    void visit(CasaBindingComponentServiceUnit target);
    void visit(CasaConnections target);
    void visit(CasaConnection target);
    void visit(CasaEndpoints target);
    void visit(CasaEndpoint target);
    void visit(CasaConsumes target);
    void visit(CasaProvides target);
    void visit(CasaPorts target);
    void visit(CasaPort target);
    void visit(CasaPortTypes target);
    void visit(CasaBindings target);
    void visit(CasaServices target);
    void visit(CasaLink target);
    void visit(CasaRegions target);
    void visit(CasaRegion target);
    
    
    /**
     * Default shallow visitor.
     */
    public static class Default implements CasaComponentVisitor {
        
        public void visit(Casa target) {
            visitComponent(target);
        }
        
        public void visit(CasaServiceUnits target) {
            visitComponent(target);
        }
        
        public void visit(CasaServiceEngineServiceUnit target) {
            visitComponent(target);
        }
        
        public void visit(CasaBindingComponentServiceUnit target) {
            visitComponent(target);
        }
        
        public void visit(CasaConnections target) {
            visitComponent(target);
        }
       
        public void visit(CasaConnection target) {
            visitComponent(target);
        }
       
        public void visit(CasaPorts target) {
            visitComponent(target);
        }
       
        public void visit(CasaPort target) {
            visitComponent(target);
        }
       
        public void visit(CasaPortTypes target) {
            visitComponent(target);
        }
        
        public void visit(CasaBindings target) {
            visitComponent(target);
        }
        
        public void visit(CasaServices target) {
            visitComponent(target);
        }

        public void visit(CasaLink target) {
            visitComponent(target);
        }

        public void visit(CasaRegions target) {
            visitComponent(target);
        }

        public void visit(CasaRegion target) {
            visitComponent(target);
        }

        public void visit(CasaEndpoints target) {
            visitComponent(target);
        }
        
        public void visit(CasaEndpoint target) {
            visitComponent(target);
        }
        
        public void visit(CasaConsumes target) {
            visitComponent(target);
        }
        
        public void visit(CasaProvides target) {
            visitComponent(target);
        }
        
        protected void visitComponent(CasaComponent target) {
            ;
        }

    }
//    
//    /**
//     * Deep visitor.
//     */
//    public static class Deep extends Default {
//        protected void visitChild(JBIComponent component) {
//            for (JBIComponent child : component.getChildren()) {
//                child.accept(this);
//            }
//        }
//    }
}
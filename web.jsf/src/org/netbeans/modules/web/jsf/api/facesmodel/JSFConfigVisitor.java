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

package org.netbeans.modules.web.jsf.api.facesmodel;

/**
 *
 * @author Petr Pisl
 */
public interface JSFConfigVisitor {

    void visit(FacesConfig component);
    void visit(ManagedBean component);
    void visit(NavigationRule component);
    void visit(NavigationCase component);
    void visit(Converter component);
    /**
     * Default shallow visitor.
     */
    public static class Default implements JSFConfigVisitor {
        public void visit(FacesConfig component) {
            visitChild();
        }
        public void visit(ManagedBean component) {
            visitChild();
        }
        public void visit(NavigationRule component) {
            visitChild();
        }
        public void visit(NavigationCase component) {
            visitChild();
        }
        public void visit(Converter component) {
            visitChild();
        }
        protected void visitChild() {
        }
    }
    
    /**
     * Deep visitor.
     */
    public static class Deep extends Default {
        protected void visitChild(JSFConfigComponent component) {
            for (JSFConfigComponent child : component.getChildren()) {
                child.accept(this);
            }
        }
    }
    
}

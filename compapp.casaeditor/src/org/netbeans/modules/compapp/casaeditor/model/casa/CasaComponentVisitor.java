/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.compapp.casaeditor.model.casa;

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
    void visit(CasaExtensibilityElement target);
    
    
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
        
        public void visit(CasaExtensibilityElement target) {
            visitComponent(target);
        }
        
        protected void visitComponent(CasaComponent target) {
        }

    }
    
    /**
     * Deep visitor.
     */
    public static class Deep extends Default {
        @Override
        protected void visitComponent(CasaComponent component) {
            for (CasaComponent child : component.getChildren()) {
                child.accept(this);
            }
        }
    }
}
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.axis2.config.model.impl;

import org.netbeans.modules.websvc.axis2.config.model.Axis2;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Component;
import org.netbeans.modules.websvc.axis2.config.model.Axis2ComponentFactory;
import org.netbeans.modules.websvc.axis2.config.model.Axis2QNames;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Visitor;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.JavaGenerator;
import org.netbeans.modules.websvc.axis2.config.model.Libraries;
import org.netbeans.modules.websvc.axis2.config.model.LibraryRef;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

public class Axis2ComponentFactoryImpl implements Axis2ComponentFactory {
    private Axis2ModelImpl model;
    
    public Axis2ComponentFactoryImpl(Axis2ModelImpl model) {
        this.model = model;
    }
    
    public Axis2Component create(Element element, Axis2Component context) {
        if (context == null) {
            if (areSameQName(Axis2QNames.AXIS2, element)) {
                return new Axis2Impl(model, element);
            } else {
                return null;
            }
        } else {
            return new CreateVisitor().create(element, context);
        }
    }
    
    public static boolean areSameQName(Axis2QNames q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
    
    public static class CreateVisitor extends Axis2Visitor.Default {
        Element element;
        Axis2Component created;
        
        Axis2Component create(Element element, Axis2Component context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(Axis2QNames q) {
            return areSameQName(q, element);
        }
        
        @Override
        public void visit(Axis2 context) {
            if (isElementQName(Axis2QNames.SERVICE)) {
                created = new ServiceImpl((Axis2ModelImpl)context.getModel(), element);
            } else if (isElementQName(Axis2QNames.LIBRARIES)) {
                created = new LibrariesImpl((Axis2ModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(Service context) {
            if (isElementQName(Axis2QNames.GENERATE_WSDL)) {
                created = new GenerateWsdlImpl((Axis2ModelImpl)context.getModel(), element);
            } else if (isElementQName(Axis2QNames.JAVA_GENERATOR)) {
                created = new JavaGeneratorImpl((Axis2ModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(Libraries context) {
            if (isElementQName(Axis2QNames.LIBRARY_REF)) {
                created = new LibraryRefImpl((Axis2ModelImpl)context.getModel(), element);
            }
        }
        
    }

    
    public Service createService() {
        return new ServiceImpl(model);
    }

    public GenerateWsdl createGenerateWsdl() {
        return new GenerateWsdlImpl(model);
    }
    
    public JavaGenerator createJavaGenerator() {
        return new JavaGeneratorImpl(model);
    }

    public Libraries createLibraries() {
        return new LibrariesImpl(model);
    }

    public LibraryRef createLibraryRef() {
        return new LibraryRefImpl(model);
    }
    
}

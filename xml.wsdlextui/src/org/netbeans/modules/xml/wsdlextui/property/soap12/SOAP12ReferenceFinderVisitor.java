/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.wsdlextui.property.soap12;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Address;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Body;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Fault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Header;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12HeaderFault;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Operation;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author skini
 */
class SOAP12ReferenceFinderVisitor extends ChildVisitor implements SOAP12Component.Visitor {

    private Referenceable referenced;
    private List<Component> referencers;

    public SOAP12ReferenceFinderVisitor(Referenceable referenced, List<Component> referencers) {
        this.referenced = referenced;
        this.referencers = referencers;
    }

    @Override
    public void visit(ExtensibilityElement ee) {
        if (ee instanceof SOAP12Header) {
            visit((SOAP12Header) ee);
        } else if (ee instanceof SOAP12Body) {
            visit((SOAP12Body) ee);
        } else if (ee instanceof SOAP12Fault) {
            visit((SOAP12Fault) ee);
        } else if (ee instanceof SOAP12HeaderFault) {
            visit((SOAP12HeaderFault) ee);
        }
        visitComponent(ee);
    }

    public void visit(SOAP12Header component) {
        if (referenced instanceof Message) {
            check(component.getMessage(), component);
        } else if (referenced instanceof Part) {
            check(component.getPartRef(), component);
        }
    }

    public void visit(SOAP12Address target) {
    }

    public void visit(SOAP12Binding target) {
    }

    public void visit(SOAP12Body component) {
        if (component.getParts() != null) {
            for (Reference<Part> ref : component.getPartRefs()) {
                check(ref, component);
            }
        }
    }

    public void visit(SOAP12Fault component) {
        check(component.getFault(), component);
    }

    public void visit(SOAP12HeaderFault component) {
        if (referenced instanceof Message) {
            check(component.getMessage(), component);
        } else if (referenced instanceof Part) {
            check(component.getPartRef(), component);
        }
    }

    public void visit(SOAP12Operation target) {
    }

    private <T extends ReferenceableWSDLComponent> void check(NamedComponentReference<T> ref, Component referencing) {
        if (ref == null || !ref.getType().isAssignableFrom(referenced.getClass())) {
            return;
        }

        try {
            if (ref.references(ref.getType().cast(referenced))) {
                referencers.add(referencing);
            }
        } catch (Exception e) {
        }
    }

    private <T extends ReferenceableWSDLComponent> void check(Reference<T> ref, Component referencing) {
        if (ref == null || !ref.getType().isAssignableFrom(referenced.getClass())) {
            return;
        }
        try {
            if (ref.references(ref.getType().cast(referenced))) {
                referencers.add(referencing);
            }
        } catch (Exception e) {
        }
    }
}

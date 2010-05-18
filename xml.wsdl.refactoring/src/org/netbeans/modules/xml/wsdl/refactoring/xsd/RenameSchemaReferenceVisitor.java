/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xml.wsdl.refactoring.xsd;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.DefaultVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 */
public class RenameSchemaReferenceVisitor extends DefaultVisitor implements WSDLVisitor {
    private ReferenceableSchemaComponent renamedReferenced;
    private String oldName;
    private RenameRefactoring request;
    
    /** Creates a new instance of FindUsageVisitor */
    public RenameSchemaReferenceVisitor() {
    }
    
    public void rename(Model mod, Set<RefactoringElementImplementation> elements, RenameRefactoring request) throws IOException {
        if (request == null || elements == null || mod == null) return;
        if (! (mod instanceof WSDLModel)) return;
        
        Referenceable ref = request.getRefactoringSource().lookup(Referenceable.class);
        this.oldName = request.getContext().lookup(String.class);
        
        if (! (ref instanceof ReferenceableSchemaComponent) || oldName  == null) {
            return;
        }

        WSDLModel model = (WSDLModel) mod;
        boolean startTransaction = ! model.isIntransaction();
        renamedReferenced = (ReferenceableSchemaComponent) ref;

        try {
            if (startTransaction) {
                model.startTransaction();
            }
         //   Collection<Usage> items = usage.getItems();
            for (RefactoringElementImplementation item:elements) {
                if (item.isEnabled() &&
                    item.getLookup().lookup(WSDLComponent.class)!=null) {
                    WSDLComponent referencing = item.getLookup().lookup(WSDLComponent.class);
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

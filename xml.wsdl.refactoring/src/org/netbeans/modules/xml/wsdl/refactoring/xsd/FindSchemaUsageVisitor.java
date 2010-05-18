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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringElement;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class FindSchemaUsageVisitor extends ChildVisitor implements WSDLVisitor {
    private ReferenceableSchemaComponent referenced;
    private Model model;
    List<WSDLRefactoringElement> elements = new ArrayList();
    RefactoringSession session;
    XMLRefactoringTransaction transaction;
    
    /** Creates a new instance of FindUsageVisitor */
    public FindSchemaUsageVisitor() {
    }
    
    public List<WSDLRefactoringElement> findUsages(ReferenceableSchemaComponent referenced,
            Definitions wsdl, RefactoringSession session, XMLRefactoringTransaction transaction) {
        this.referenced = referenced;
        this.model = wsdl.getModel();
        this.session = session;
        wsdl.accept(this);
        return elements;
    }
    
    public void visit(Types types) {
        Collection<Schema> schemas = types.getSchemas();
        if (schemas == null || schemas.size() == 0)
            return;
        for (Schema schema : schemas) {
            collectUsage(schema);
        }
        super.visit(types);
    }
    
    public void visit(Part part) {
        try {
            if (referenced instanceof GlobalElement && part.getElement() != null &&
                    part.getElement().references((GlobalElement)referenced) ||
                    referenced instanceof GlobalType && part.getType() != null &&
                    part.getType().references((GlobalType)referenced)) 
            {
                    elements.add(new WSDLRefactoringElement(model, referenced, part));
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        
        }
        super.visit(part);
    }
    
    //extensibility element referencing schema component needs to have own engines
    /*public void visit(ExtensibilityElement ee) {
        collectUsage(ee);
        super.visit(ee);
    }*/
    
    private void collectUsage(Component searchRoot) {
        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(referenced));
        query.getContext().add(searchRoot);
        if(transaction!=null)
            query.getContext().add(transaction);
        // query.getContext().add(Lookups.fixed(searchRoots));
        query.prepare(session);
       
    }
}

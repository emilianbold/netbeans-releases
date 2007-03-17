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

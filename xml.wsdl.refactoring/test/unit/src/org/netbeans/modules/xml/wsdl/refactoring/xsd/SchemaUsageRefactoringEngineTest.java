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

import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.refactoring.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.refactoring.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.refactoring.Util;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author nn136682
 */
public class SchemaUsageRefactoringEngineTest extends TestCase {
  //  SchemaUsageRefactoringEngine instance = new SchemaUsageRefactoringEngine();
    
    public SchemaUsageRefactoringEngineTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaUsageRefactoringEngineTest.class);
        
        return suite;
    }
    
    public void testFindUsages() throws Exception {
        SchemaModel schemaMod = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.OTA);
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        GlobalElement el = (GlobalElement) Util.findGlobalComponentByName(schemaMod.getSchema(), "TravelItinerary");
        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton((Referenceable)el));
        query.getContext().add((Component)model.getDefinitions());
        RefactoringSession session = RefactoringSession.create("test refactor");
        query.prepare(session);
        Collection<RefactoringElement> element = session.getRefactoringElements();
        assertEquals(2, element.size());
       
    }
    
        
    public void testRefactorUsages() throws Exception {
        SchemaModel schemaMod = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.OTA);
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        GlobalElement el = (GlobalElement) Util.findGlobalComponentByName(schemaMod.getSchema(), "TravelItinerary");
        
        String newName = "myItirenary";
       
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(el));
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)el, refactoring);
        refactoring.setNewName(newName);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add((Component)model.getDefinitions());
        refactoring.getContext().add(el.getName());
        RefactoringSession session = RefactoringSession.create("test refactor");
        assertNull(refactoring.prepare(session));
        session.doRefactoring(true);
        
        Part part = model.findComponentByName("itinerary", Part.class);
        assertEquals(newName, part.getElement().getQName().getLocalPart());
        
    }
    
}

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


package org.netbeans.modules.xml.schema;


import java.util.Collection;
import javax.swing.undo.UndoManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Sonali
 */
public class SchemaRefactoringTest extends TestCase {
    public static final String XSD_PO = "PurchaseOrder.xsd";
    public static final String XSD_LA = "LoanApplication.xsd";
    
    private Project project;
    private SchemaModel model;
    private FileObject purchaseOrderFO;
    
    public SchemaRefactoringTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        project = Util.createJavaTestProject();
        purchaseOrderFO = Util.populateProject(
                project, "resources/"+XSD_PO, "src/com/acme/schemas");
        ModelSource ms = Utilities.getModelSource(purchaseOrderFO, true);
        model = SchemaModelFactory.getDefault().getModel(ms);
    }

    protected void tearDown() throws Exception {
        deleteModelFile(purchaseOrderFO);
    }
    
    private static void deleteModelFile(FileObject fo) throws Exception {
        if (fo == null) return;
        SchemaDataObject sdo = (SchemaDataObject) DataObject.find(fo);
        if (sdo != null) {
            SaveCookie save = sdo.getCookie(SaveCookie.class);
            if (save != null) save.save();
        }
        fo.delete();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaRefactoringTest.class);
        return suite;
    }


    
    public void testFindUsages() throws Exception {
        GlobalComplexType poType = model.getSchema().getComplexTypes().iterator().next();
        WhereUsedQuery refactoring = new WhereUsedQuery(Lookups.singleton((Referenceable)poType));
        RefactoringSession result = RefactoringSession.create("Find Usages Test Case");
        assertNull(refactoring.prepare(result));
        Collection<RefactoringElement> items = result.getRefactoringElements();
        RefactoringElement element = items.iterator().next();
        Component usage = element.getLookup().lookup(Component.class);
        assertNotNull(usage.getModel());
        assertEquals("purchaseOrder", ((GlobalElement)usage).getName());       
   
    }
    
    
    public void testFileRefactoring() throws Exception {
        FileObject loanApplicationFO = Util.populateProject(
                project, "resources/"+XSD_LA, "src/com/acme/schemas");
        
        ModelSource ms = Utilities.getModelSource(loanApplicationFO, true);
        SchemaModel laModel = SchemaModelFactory.getDefault().getModel(ms);
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(laModel));
        refactoring.setNewName("newLoanApp");
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)laModel, refactoring);
        refactoring.getContext().add(transaction);
        assertNull(refactoring.preCheck());
        assertNull(refactoring.checkParameters());
        RefactoringSession result = RefactoringSession.create("File rename test");
        assertNull(refactoring.prepare(result));
        assertNull(result.doRefactoring(true));
        assertEquals("newLoanApp", loanApplicationFO.getName());
        assertEquals("newLoanApp.xsd", model.getSchema().getImports().iterator().next().getSchemaLocation());
        deleteModelFile(loanApplicationFO);
        
    }
    
       public void testLocalElementRename() throws Exception {
        FileObject loanApplicationFO = Util.populateProject(
                project, "resources/"+XSD_LA, "src/com/acme/schemas");
        try {
        ModelSource ms = Utilities.getModelSource(loanApplicationFO, true);
        SchemaModel laModel = SchemaModelFactory.getDefault().getModel(ms);
        ReferenceableSchemaComponent addressType = Util.findGlobalComponentByName(laModel.getSchema(), "AddressType");
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(addressType));
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)addressType, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(addressType.getName());
        assertNull(refactoring.preCheck());
        refactoring.setNewName("MyAddressType");
        assertNull(refactoring.checkParameters());
        RefactoringSession result = RefactoringSession.create("Rename test");
        assertNull(refactoring.prepare(result));
        assertNull(result.doRefactoring(true));
        String xpath = "/xs:schema/xs:complexType[@name='ResidenceType']/xs:sequence/xs:element[@name='address']";
        LocalElement le = (LocalElement) Util.findComponent(laModel.getSchema(), xpath);
        assertEquals("MyAddressType", le.getType().getRefString());
        addressType = Util.findGlobalComponentByName(laModel.getSchema(), "MyAddressType");
        assertNotNull(addressType);
        Collection<RefactoringElement> elements = result.getRefactoringElements();
        assertEquals(3, elements.size());
        } finally {        
            deleteModelFile(loanApplicationFO);
        }
    }
       
       public void testRefactorUsagesUndoRedo() throws Exception {
        UndoManager myUndoManager = new UndoManager();
        model.addUndoableEditListener(myUndoManager);
        
        ReferenceableSchemaComponent poType = Util.findGlobalComponentByName(model.getSchema(), "PurchaseOrderType");
      //  RenameRequest request = new RenameRequest(poType, "MyPoType");
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(poType));
        refactoring.setNewName("MyPoType");
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)poType, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(poType.getName());
        assertNull(refactoring.preCheck());
        //List<ErrorItem> errors = RefactoringManager.getInstance().precheckUsages(request);
        //assertEquals(0, errors.size());
        assertNull(refactoring.checkParameters());
        //RefactoringManager.getInstance().process(request);
        RefactoringSession result = RefactoringSession.create("Rename test");
        assertNull(refactoring.prepare(result));
        result.doRefactoring(true);
        GlobalElement ge = (GlobalElement)Util.findGlobalComponentByName(model.getSchema(), "purchaseOrder");
        assertEquals("po:MyPoType", ge.getType().getRefString());
        
        assertFalse("other undomanager should be emptied", myUndoManager.canUndo());
        assertFalse("other undomanager should be emptied", myUndoManager.canRedo());
      //  assertTrue(RefactoringManager.getInstance().canUndo());
        result.undoRefactoring(true); 
        poType = Util.findGlobalComponentByName(model.getSchema(), "PurchaseOrderType");
        assertNotNull(poType);
        assertEquals("po:PurchaseOrderType", ge.getType().getRefString());
        model.startTransaction();
        poType.setId("foo");
        model.endTransaction();
        
        assertTrue("other undomanager should be intact", myUndoManager.canUndo());
    }
       
        public void testMultiFiles() throws Exception {
        FileObject loanApplicationFO = Util.populateProject(
                project, "resources/"+XSD_LA, "src/com/acme/schemas");
        try {
        ModelSource ms = Utilities.getModelSource(loanApplicationFO, true);
        SchemaModel laModel = SchemaModelFactory.getDefault().getModel(ms);
        
        ReferenceableSchemaComponent addressType = Util.findGlobalComponentByName(laModel.getSchema(), "AddressType");
        
        WhereUsedQuery request = new WhereUsedQuery(Lookups.singleton((Referenceable)addressType));
        RefactoringSession result = RefactoringSession.create("Find Usages Test Case");
        assertNull(request.prepare(result));
        Collection<RefactoringElement> items = result.getRefactoringElements();
        assertEquals(3, items.size());
                
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(addressType));
        refactoring.setNewName("MyAddressType");
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)addressType, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(addressType.getName());
        assertNull(refactoring.preCheck());
        
        RefactoringSession session = RefactoringSession.create("Schema Test");
        assertNull(refactoring.prepare(session));
        session.doRefactoring(true);
        Collection<RefactoringElement> elements = session.getRefactoringElements();
        String xpath = "/xs:schema/xs:complexType[@name='ResidenceType']/xs:sequence/xs:element[@name='address']";
        LocalElement le = (LocalElement) Util.findComponent(laModel.getSchema(), xpath);
        assertEquals("MyAddressType", le.getType().getRefString());
        addressType = Util.findGlobalComponentByName(laModel.getSchema(), "MyAddressType");
        assertEquals(3, elements.size());
        
        session.undoRefactoring(true);
        addressType = Util.findGlobalComponentByName(laModel.getSchema(), "AddressType");
        assertNotNull(addressType);
        assertEquals("AddressType", le.getType().getRefString());
        GlobalComplexType poType = (GlobalComplexType) Util.findGlobalComponentByName(model.getSchema(), "PurchaseOrderType");
        Sequence seq = (Sequence) poType.getDefinition();
        assertEquals("la:AddressType", ((LocalElement)seq.getContent().get(1)).getType().getRefString());
        addressType = Util.findGlobalComponentByName(laModel.getSchema(), "AddressType");
       
        session.doRefactoring(true);
        elements = session.getRefactoringElements();
        addressType = Util.findGlobalComponentByName(laModel.getSchema(), "MyAddressType");
        assertEquals(3, elements.size());
        } finally {        
            deleteModelFile(loanApplicationFO);
        }
    }
        
        public void testInvalidDeleteUndoRedo() throws Exception {
        FileObject fo = Util.populateProject(
                project, "resources/newXmlSchema.xsd", "src/com/acme/schemas");
        try {
            ModelSource ms = Utilities.getModelSource(fo, true);
            SchemaModel model = SchemaModelFactory.getDefault().getModel(ms);
            Collection<GlobalElement> ges = model.getSchema().getElements();
            GlobalElement ge = ges.iterator().next();
            GlobalElement copy = (GlobalElement) ge.copy(model.getSchema());
            copy.setName("newName");
          //  DeleteRequest delete = new DeleteRequest(ge);
           // RefactoringManager.getInstance().execute(delete, false);
            SafeDeleteRefactoring refactoring = new SafeDeleteRefactoring(Lookups.singleton((NamedReferenceable)ge));
            XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)ge, refactoring);
            refactoring.getContext().add(transaction);
            RefactoringSession session = RefactoringSession.create("Test invalid Delete Undo Redo");
            refactoring.prepare(session);
            session.doRefactoring(true);
            
            assertTrue(transaction.canUndo());
            model.startTransaction();
            model.getSchema().addElement(copy);
            model.endTransaction();
                       
        } finally {        
            deleteModelFile(fo);
        }
    }
        
        
        public void testRefactorLocalElementUndoRedo() throws Exception {
        GlobalComplexType poType = (GlobalComplexType) Util.findGlobalComponentByName(model.getSchema(), "PurchaseOrderType");
        LocalElement le= poType.getDefinition().getChildren(LocalElement.class).get(0);
        String expected = "shipToAddress";
     
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton((Nameable)le));
        refactoring.setNewName(expected);
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)le, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(le.getName());
        RefactoringSession session = RefactoringSession.create("Test Refactor local element undo/redo");
        refactoring.prepare(session);
        assertNull(session.doRefactoring(true));
        
        assertTrue(transaction.canUndo());
        session.undoRefactoring(true);
        
        assertFalse(transaction.canUndo());
        session.doRefactoring(true);

        
    }
    
     
     public void testMultipleRefactoringEffectsOnUndo() throws Exception {
        UndoManager um = new UndoManager();
        model.addUndoableEditListener(um);
        
        String expected1 = "PurchaseOrderType1";
        String expected2 = "PurchaseOrderType2";
        String expected3 = "PurchaseOrderType3";
        
        GlobalComplexType poType = (GlobalComplexType) Util.findGlobalComponentByName(model.getSchema(), "PurchaseOrderType");
        RenameRefactoring refactoring1 = new RenameRefactoring(Lookups.singleton((Nameable)poType));
        refactoring1.setNewName(expected1);
        XMLRefactoringTransaction transaction1 = new XMLRefactoringTransaction((Referenceable)poType, refactoring1);
        refactoring1.getContext().add(transaction1);
        refactoring1.getContext().add(poType.getName());
        RefactoringSession session1 = RefactoringSession.create("Test schema refactoring");
        refactoring1.checkParameters();
        refactoring1.prepare(session1);
        session1.doRefactoring(true);
        assertEquals(expected1, poType.getName());
        assertTrue(transaction1.canUndo());
        
        model.startTransaction();
        model.getSchema().setVersion("8888");
        model.endTransaction();
        
        assertTrue(um.canUndo());

        RenameRefactoring refactoring2 = new RenameRefactoring(Lookups.singleton((Nameable)poType));
        refactoring2.setNewName(expected2);
        XMLRefactoringTransaction transaction2 = new XMLRefactoringTransaction((Referenceable)poType, refactoring2);
        refactoring2.getContext().add(transaction2);
        refactoring2.getContext().add(poType.getName());
        RefactoringSession session2 = RefactoringSession.create("Test schema refactoring");
        refactoring2.checkParameters();
        refactoring2.prepare(session2);
        session2.doRefactoring(true);
        assertEquals(expected2, poType.getName());
        assertTrue(transaction2.canUndo());

       RenameRefactoring refactoring3 = new RenameRefactoring(Lookups.singleton((Nameable)poType));
        refactoring3.setNewName(expected3);
        XMLRefactoringTransaction transaction3 = new XMLRefactoringTransaction((Referenceable)poType, refactoring3);
        refactoring3.getContext().add(transaction3);
        refactoring3.getContext().add(poType.getName());
        RefactoringSession session3 = RefactoringSession.create("Test schema refactoring");
        refactoring3.checkParameters();
        refactoring3.prepare(session3);
        session3.doRefactoring(true);
        assertEquals(expected3, poType.getName());
        assertTrue(transaction3.canUndo());
        
        model.startTransaction();
        model.getSchema().setVersion("0000");
        model.endTransaction();
        
       assertTrue(um.canUndo());
    }
     
    /* public void testUndoRedoSaveFile() throws Exception {
        FileObject loanApplicationFO = Util.populateProject(
                project, "resources/"+XSD_LA, "src/com/acme/schemas");
        ModelSource ms = Utilities.getModelSource(loanApplicationFO, true);
        SchemaModel laModel = SchemaModelFactory.getDefault().getModel(ms);
        UndoManager um = new UndoManager();
        laModel.addUndoableEditListener(um);

        ReferenceableSchemaComponent addressType = Util.findGlobalComponentByName(laModel.getSchema(), "AddressType");
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton((Nameable)addressType));
        refactoring.setNewName("addressType2");
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)addressType, refactoring);
        transaction.setLocalScope();
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(addressType.getName());
        RefactoringSession session = RefactoringSession.create("Test Refactor local element undo/redo");
        refactoring.prepare(session);
        assertNull(session.doRefactoring(true));

        assertFalse(RefactoringUtil.isDirty(laModel));
        um.undo();
     //   assertTrue(RefactoringUtil.isDirty(laModel));
        
       
        refactoring = new RenameRefactoring(Lookups.singleton((Nameable)addressType));
        refactoring.setNewName("addressType2");
        transaction = new XMLRefactoringTransaction((Referenceable)addressType, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(addressType.getName());
        session = RefactoringSession.create("Test Refactor local element undo/redo");
        refactoring.prepare(session);
        assertNull(session.doRefactoring(true));
        Collection<RefactoringElement> elements = session.getRefactoringElements();
        assertEquals(3, elements.size());

        
        assertFalse(RefactoringUtil.isDirty(laModel));
        assertFalse(RefactoringUtil.isDirty(model));
        
              
        session.undoRefactoring(true);
        assertFalse(RefactoringUtil.isDirty(laModel));
        assertFalse(RefactoringUtil.isDirty(model));
        
        //request = new RenameRequest(addressType, "addressType3");
       // RefactoringManager.getInstance().precheckUsages(request);
       // assertEquals(3, totalItems(request.getUsages()));
       // RefactoringManager.getInstance().redo();
        
        refactoring = new RenameRefactoring(Lookups.singleton((Nameable)addressType));
        refactoring.setNewName("addressType3");
        transaction = new XMLRefactoringTransaction((Referenceable)addressType, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(addressType.getName());
        session = RefactoringSession.create("Test Refactor local element undo/redo");
        refactoring.prepare(session);
        assertNull(session.doRefactoring(true));
        
        assertTrue(RefactoringUtil.isDirty(laModel));
        assertFalse(RefactoringUtil.isDirty(model));
        
    }*/



}

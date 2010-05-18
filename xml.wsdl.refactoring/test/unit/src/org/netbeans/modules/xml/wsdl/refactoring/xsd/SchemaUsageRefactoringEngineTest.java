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

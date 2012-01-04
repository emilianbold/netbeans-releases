/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

/**
 *
 * @author Petr Pisl
 */
public class ModelTest extends JsTestBase {
    
    public ModelTest(String testName) {
        super(testName);
    }
        
    private Model getModel(String file) throws Exception {
        final Model[] globals = new Model[1];
        Source source = getTestSource(getTestFile(file));
        
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                JsParserResult parameter = (JsParserResult) resultIterator.getParserResult();
                Model model = parameter.getModel();
                globals[0] = model;
            }
        });        
        return globals[0];
    }
  
    
    
    public void testObjectName01() throws Exception {
        Model model = getModel("testfiles/model/objectNames01.js");
        assertNotNull(model);
        Collection<? extends Scope>  elements = model.getFileScope().getLogicalElements();
        assertEquals(1, elements.size());
        Collection<? extends ObjectScope> objects = ModelUtils.getObjects(model.getFileScope());
        ObjectScope object = ModelUtils.getFirst(objects);
        assertEquals("Ridic", object.getFQDeclarationName().get(0).getName());
    }
    
    public void testMethods01() throws Exception {
        Model model = getModel("testfiles/model/objectNames01.js");
        assertNotNull(model);
        Collection<? extends Scope>  elements = model.getFileScope().getLogicalElements();
        assertEquals(1, elements.size());
        Collection<? extends ObjectScope> objects = ModelUtils.getObjects(model.getFileScope());
        ObjectScope object = ModelUtils.getFirst(objects);
        FunctionScope method = ModelUtils.getFirst(object.getMethods());
        assertEquals("getName", method.getDeclarationName().getName());
    }
    
    public void testMethods02() throws Exception {
        Model model = getModel("testfiles/model/objectMethods01.js");
        assertNotNull(model);
        Collection<? extends Scope>  elements = model.getFileScope().getLogicalElements();
        assertEquals(1, elements.size());
        Collection<? extends ObjectScope> objects = ModelUtils.getObjects(model.getFileScope());
        ObjectScope object = ModelUtils.getFirst(objects);
        Collection<? extends FunctionScope> methods = object.getMethods();
        assertEquals(2, methods.size());
        final Iterator<? extends FunctionScope> iterator = methods.iterator();
        FunctionScope method = iterator.next();
        assertEquals("getName", method.getDeclarationName().getName());
        method = iterator.next();
        assertEquals("getInfo", method.getDeclarationName().getName());
    }
    
    public void testStaticMethod01() throws Exception {
        Model model = getModel("testfiles/model/staticMethods01.js");
        assertNotNull(model);
        Collection<? extends Scope>  elements = model.getFileScope().getLogicalElements();
        assertEquals(1, elements.size());
        Collection<? extends ObjectScope> objects = ModelUtils.getObjects(model.getFileScope());
        ObjectScope object = ModelUtils.getFirst(objects);
        Collection<? extends FunctionScope> methods = object.getMethods();
        assertEquals(3, methods.size());
        boolean checked = false;
        for (FunctionScope method : methods) {
            if (method.getModifiers().contains(Modifier.STATIC)) {
                checked = true;
                assertEquals("getFormula", method.getDeclarationName().getName());
            }
        }
        assertTrue(checked);
    }
    
    public void testParameters01() throws Exception {
        Model model = getModel("testfiles/model/simpleFunction.js");
        assertNotNull(model);
        Collection<? extends Scope>  elements = model.getFileScope().getLogicalElements();
        assertEquals(1, elements.size());
        FunctionScope function = (FunctionScope)elements.iterator().next();
        assertEquals(3, function.getParameters().size());
        final Iterator<? extends Parameter> iterator = function.getParameters().iterator();
        Parameter param = iterator.next();
        assertEquals("text", param.getDeclaration().getName());
        param = iterator.next();
        assertEquals("name", param.getDeclaration().getName());
        param = iterator.next();
        assertEquals("description", param.getDeclaration().getName());
    }
    
    
    public void testVariables01() throws Exception {
        Model model = getModel("testfiles/model/variables01.js");
        assertNotNull(model);
        FileScope fScope = model.getFileScope();
        Collection<? extends Variable> variables = fScope.getDeclaredVariables();
        assertEquals(3, variables.size());
        
        Variable variable = ModelUtils.getFirst(ModelUtils.getFirst(variables, "address"));
        assertEquals("address", variable.getDeclaration().getName());
        assertEquals(true, variable.isGlobal());
        assertEquals(false, variable.isImplicit());
        
        variable = ModelUtils.getFirst(ModelUtils.getFirst(variables, "country"));
        assertEquals("country", variable.getDeclaration().getName());
        assertEquals(true, variable.isGlobal());
        assertEquals(true, variable.isImplicit());
        
        variable = ModelUtils.getFirst(ModelUtils.getFirst(variables, "telefon"));
        assertEquals("telefon", variable.getDeclaration().getName());
        assertEquals(true, variable.isGlobal());
        assertEquals(true, variable.isImplicit());
        
        ModelElement element = ModelUtils.getFirst(ModelUtils.getFirst(fScope.getElements(), "Address"));
        FunctionScope addressObject = (FunctionScope)element;
        assertEquals("Address", addressObject.getName());
        variables = addressObject.getDeclaredVariables();
        assertEquals(2, variables.size());
        
        variable = ModelUtils.getFirst(ModelUtils.getFirst(variables, "city"));
        assertEquals("city", variable.getDeclaration().getName());
        assertEquals(false, variable.isGlobal());
        assertEquals(false, variable.isImplicit());
        
        variable = ModelUtils.getFirst(ModelUtils.getFirst(variables, "zip"));
        assertEquals("zip", variable.getDeclaration().getName());
        assertEquals(false, variable.isGlobal());
        assertEquals(false, variable.isImplicit());
        
        // testing fields
        Collection<? extends Field> fields = addressObject.getFields();
        assertEquals(2, fields.size());
        
        Field field = ModelUtils.getFirst(ModelUtils.getFirst(fields, "street"));
        assertEquals("street", field.getDeclaration().getName());
        
        field = ModelUtils.getFirst(ModelUtils.getFirst(fields, "id"));
        assertEquals("id", field.getDeclaration().getName());
        
        element = ModelUtils.getFirst(ModelUtils.getFirst(fScope.getLogicalElements(), "MyApp"));
        ObjectScope myApp = (ObjectScope) element;
        assertEquals("MyApp", myApp.getFQDeclarationName().get(0).getName());
        
        field = (Field)ModelUtils.getFirst(ModelUtils.getFirst(myApp.getElements(), "country"));
        assertEquals("country", field.getDeclaration().getName());
    }
    
    
    public void testNamesapces01() throws Exception {
        Model model = getModel("testfiles/model/namespaces01.js");
        assertNotNull(model);
        
        FileScope fScope = model.getFileScope();
        assertEquals(3, fScope.getLogicalElements().size());
        
        ObjectScope object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(fScope.getElements(), "MyContext"));
        assertEquals("MyContext", object.getName());
        assertEquals(3, object.getElements().size());
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "test")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "id")));
        assertEquals(146, object.getOffset());
        
        object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "User"));
        assertEquals(4, object.getElements().size());
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "session")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "firstName")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "lastName")));
        assertEquals(187, object.getOffset());
        
        object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "Address"));
        assertEquals(2, object.getElements().size());
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "street")));
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "town")));
        assertEquals(278, object.getOffset());
        
        object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(fScope.getElements(), "Ns1"));
        assertEquals(1, object.getElements().size());
        object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "Ns2"));
        assertEquals(1, object.getElements().size());
        object = (ObjectScope)ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "Ns3"));
        assertEquals(1, object.getElements().size());
        assertNotNull(ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), "fix")));
        assertEquals(771, object.getOffset());
    }
}

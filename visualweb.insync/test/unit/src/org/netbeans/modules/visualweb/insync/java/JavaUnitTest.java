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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import java.lang.reflect.Modifier;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.visualweb.insync.InsyncTestBase;
import org.netbeans.modules.visualweb.insync.ParserAnnotation;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;

/**
 *
 * @author jdeva
 */
public class JavaUnitTest extends InsyncTestBase {
    public JavaUnitTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(JavaUnitTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    } 

    JavaUnit getJavaUnit() {
        FacesModelSet set = createFacesModelSet();
        FacesModel model = set.getFacesModel(getJavaFile(getPageBeans()[0]));
        model.sync();
        return model.getJavaUnit();
    }
    
    /**
     * Test of destroy method, of class JavaUnit.
     */
    public void testDestroy() {
        System.out.println("destroy");
        JavaUnit instance = getJavaUnit();
        instance.destroy();
    }

    /**
     * Test of sync method, of class JavaUnit.
     */
    public void testSync() {
        System.out.println("sync");
        JavaUnit instance = getJavaUnit();
        boolean result = instance.sync();
        //because the unit has been alreay synced because of getJavaUnit()
        assertEquals(result, false);
        assertEquals(instance.getState(), Unit.State.CLEAN);
        
        //Try syncing again
        instance.setSourceDirty();
        result = instance.sync();
        assertEquals(result, true);
        assertEquals(instance.getState(), Unit.State.CLEAN);
        
        
        //Introduce errors and sync again
        ContextMethod cm =  new ContextMethod(null, "foo", Modifier.PUBLIC,
            Void.TYPE, new Class[]{}, new String[]{});
        Method method = instance.getJavaClass().addMethod(cm);
        method.replaceBody("int i\nin j;");
        instance.setSourceDirty();
        result = instance.sync(); 
        assertEquals(result, true);
        assertEquals(instance.getState(), Unit.State.BUSTED);        
    }

    /**
     * Test of getJavaClass method, of class JavaUnit.
     */
    public void testGetJavaClass() {
        System.out.println("getJavaClass");
        JavaUnit instance = getJavaUnit();
        JavaClass result = instance.getJavaClass();
        assertNotNull(result);
    }

    /**
     * Test of getErrors method, of class JavaUnit.
     */
    public void testGetErrors() {
        System.out.println("getErrors");
        JavaUnit instance = getJavaUnit();
        ParserAnnotation[] expResult = ParserAnnotation.EMPTY_ARRAY;
        ParserAnnotation[] result = instance.getErrors();
        assertEquals(expResult, result);
        
        //Introduce errors and check again
        ContextMethod cm =  new ContextMethod(null, "foo", Modifier.PUBLIC,
            Void.TYPE, new Class[]{}, new String[]{});
        Method method = instance.getJavaClass().addMethod(cm);
        method.replaceBody("int i\nin j;");
        instance.setSourceDirty();
        instance.sync();
        result = instance.getErrors();
        assert(result.length > 0);
    }

    /**
     * Test of ensureImport method, of class JavaUnit.
     */
    public void testEnsureImport() {
        System.out.println("ensureImport");
        String fqn = "com.sun.webui.jsf.component.Button";
        JavaUnit instance = getJavaUnit();
        boolean result = instance.ensureImport(fqn);
        assertEquals(true, result);
        
        //Try again after the import is added
        result = instance.ensureImport(fqn);
        assertEquals(true, result);
        
        //Try importing with same short name but different FQN
        fqn = "test.Button";
        result = instance.ensureImport(fqn);
        assertEquals(false, result);
    }

//    /**
//     * Test of insertUpdate method, of class JavaUnit.
//     */
//    public void testInsertUpdate() {
//        System.out.println("insertUpdate");
//        DocumentEvent e = null;
//        JavaUnit instance = null;
//        instance.insertUpdate(e);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeUpdate method, of class JavaUnit.
//     */
//    public void testRemoveUpdate() {
//        System.out.println("removeUpdate");
//        DocumentEvent e = null;
//        JavaUnit instance = null;
//        instance.removeUpdate(e);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}

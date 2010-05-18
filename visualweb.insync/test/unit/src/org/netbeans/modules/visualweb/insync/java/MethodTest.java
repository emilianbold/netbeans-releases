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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.visualweb.insync.InsyncTestBase;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.EventSet;
import org.netbeans.modules.visualweb.insync.beans.Property;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author jdeva
 */
public class MethodTest extends InsyncTestBase {
    public MethodTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(MethodTest.class);
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

    Method getMethod() {
        return getMethod("_init");
    }
    
    Method getMethod(String name) {
        FileObject fObj = getJavaFile(getPageBeans()[0]);
        JavaClass javaClass = JavaClass.getJavaClass(fObj);
        return javaClass.getMethod(name, new Class[]{});        
    }
    
    /**
     * Test of getJavaClass method, of class Method.
     */
    public void testGetJavaClass() {
        System.out.println("getJavaClass");
        Method instance = getMethod();
        JavaClass result = instance.getJavaClass();
        assertNotNull(result);
    }

    /**
     * Test of getName method, of class Method.
     */
    public void testGetName() {
        System.out.println("getName");
        String name = "_init";
        Method instance = getMethod(name);
        String result = instance.getName();
        assertEquals(name, result);
    }

    /**
     * Test of getElementHandle method, of class Method.
     */
    public void testGetElementHandle() {
        System.out.println("getElementHandle");
        Method instance = getMethod();
        ElementHandle<ExecutableElement> result = instance.getElementHandle();
        assertNotNull(result);
    }
    
    private List<Bean> addBeans() {
        String[] types = {
            "com.sun.data.provider.impl.CachedRowSetDataProvider",
            "com.sun.sql.rowset.CachedRowSetXImpl"
        };
        return createBeans(types);
    }    

//    /**
//     * Test of findPropertyStatement method, of class Method.
//     */
//    public void testFindPropertyStatement() {
//        System.out.println("findPropertyStatement");
//        testAddPropertySetStatements();
//    }

    /**
     * Test of addEventSetStatement method, of class Method.
     */
    public void testAddEventSetStatement() {
        try {
            System.out.println("addEventSetStatement");
            String[] types = {"com.sun.data.provider.impl.CachedRowSetDataProvider"};

            List<Bean> beans = createBeans(types);
            Method instance = getMethod();
            Bean dpBean = beans.get(0);
            String eventSetName = "dataListener";
            EventSet eventSet = dpBean.setEventSet(eventSetName);

            java.lang.reflect.Method m = null;
            Class clazz = Class.forName("org.netbeans.modules.visualweb.insync.beans.EventSet");
            m = clazz.getDeclaredMethod("getAdapterType", new Class[]{});
            m.setAccessible(true);
            Class aType = (Class) m.invoke(eventSet, new Object[]{});
            String adapterClassName;
            if (aType != null) {
                adapterClassName = aType.getName();
            } else {
                adapterClassName = eventSet.getListenerType().getName();
            }

            Statement result = instance.addEventSetStatement(dpBean.getName(), eventSet.getAddListenerMethodName(), adapterClassName);
            assertNotNull(result);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } 
    }

    /**
     * Test of addPropertySetStatements method, of class Method.
     */
    public void testAddPropertySetStatements() {
        System.out.println("addPropertySetStatements");
        String[] types = {
            "com.sun.data.provider.impl.ObjectDataProvider",
            "com.sun.webui.jsf.component.Button"
        };

        List<Bean> beans = createBeans(types);
        Method instance = getMethod();
        Bean dpBean = beans.get(0);
        String propName = "object";
        String propValSrc = beans.get(1).getName();
        dpBean.setProperty(propName, null, propValSrc);
        Property prop = dpBean.getProperty(propName);
        instance.addPropertySetStatements(dpBean);
        
        //Test if the property set statement is added
        Statement stmt = instance.findPropertyStatement(dpBean.getName(), prop.getWriteMethodName());
        assertNotNull(stmt);
        List<Statement> stmts = instance.getPropertySetStatements();
        
        //addPropertySetStatements() should ignore markup properties
        dpBean = beans.get(1);
        propName = "immediate";
        propValSrc = "true";
        dpBean.setProperty(propName, null, propValSrc);
        instance.addPropertySetStatements(dpBean);
        assertEquals(instance.getPropertySetStatements().size(), stmts.size()+1);
    }

    /**
     * Test of addEventSetStatements method, of class Method.
     */
    public void testAddEventSetStatements() {
        System.out.println("addEventSetStatements");
        String[] types = {"com.sun.data.provider.impl.CachedRowSetDataProvider"};
        List<Bean> beans = createBeans(types);
        Method instance = getMethod();
        Bean dpBean = beans.get(0);
        String eventSetName = "data";
        EventSet eventSet = dpBean.setEventSet(eventSetName);
        List<Statement> stmts = instance.getPropertySetStatements();
        
        instance.addEventSetStatements(dpBean);
        
        //Test if the property set statement is indeed added
        Statement stmt = instance.findPropertyStatement(dpBean.getName(), eventSet.getAddListenerMethodName());
        assertNotNull(stmt);
        assertEquals(instance.getPropertySetStatements().size(), stmts.size()+1);
    }

    /**
     * Test of removeStatement method, of class Method.
     */
    public void testRemoveStatement() {
        System.out.println("removeStatement");
        String[] types = {
            "com.sun.data.provider.impl.ObjectDataProvider",
            "com.sun.webui.jsf.component.Button"
        };
        List<Bean> beans = createBeans(types);
        Method instance = getMethod();
        Bean dpBean = beans.get(0);
        String propName = "object";
        String propValSrc = beans.get(1).getName();
        dpBean.setProperty(propName, null, propValSrc);
        Property prop = dpBean.getProperty(propName);
        instance.addPropertySetStatements(dpBean);
        List<Statement> stmts = instance.getPropertySetStatements();
        
        instance.removeStatement(dpBean.getName(), prop.getWriteMethodName());
        
        //Test if the property set statement is removed
        Statement stmt = instance.findPropertyStatement(dpBean.getName(), prop.getWriteMethodName());
        assertNull(stmt);
        assertEquals(instance.getPropertySetStatements().size(), stmts.size()-1);
    }

    /**
     * Test of replaceBody method, of class Method.
     */
    public void testReplaceBody() {
        System.out.println("replaceBody");
        String bodyText = "String temp = new String();";
        Method instance = getMethod();
        instance.replaceBody(bodyText);
        String bodyTextResult = instance.getBodyText();
        assertEquals(bodyText, bodyTextResult.substring(1, bodyTextResult.length()-1).trim());
    }

    /**
     * Test of rename method, of class Method.
     */
    public void testRename() {
        System.out.println("rename");
        Method instance = getMethod();
        String name = "_" + instance.getName();
        instance.rename(name);
        assertEquals(name, instance.getName());
    }

//    /**
//     * Test of update method, of class Method.
//     */
//    public void testUpdate() {
//        System.out.println("update");
//        ContextMethod method = null;
//        Method instance = null;
//        instance.update(method);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of remove method, of class Method.
     */
    public void testRemove() {
        System.out.println("remove");
        JavaClass javaClass = getMethod().getJavaClass();
        String name = "foo";
        Class[] params = new Class[] {String.class};
        ContextMethod cm =  new ContextMethod(null, name, Modifier.PUBLIC,
            Void.TYPE, params, new String[] {"name"});
        Method instance = javaClass.addMethod(cm);        
        instance.remove();
        
        Method method = javaClass.getMethod(name, params);
        assertNull(method);
    }

    /**
     * Test of getPropertySetStatements method, of class Method.
     */
    public void testGetPropertySetStatements() {
        System.out.println("getPropertySetStatements");
        Method instance = getMethod();
        List<Statement> result = instance.getPropertySetStatements();
        assertEquals(result.size(), 0);
        
        String[] types = {
            "com.sun.rave.faces.converter.SqlDateConverter",
           };
        List<Bean> beans = createBeans(types);
        Bean dpBean = beans.get(0);
        String propName = "type";
        String propValSrc = "both";
        dpBean.setProperty(propName, null, propValSrc);
        instance.addPropertySetStatements(dpBean);
        
        result = instance.getPropertySetStatements();
        assertEquals(result.size(), 1);
    }

    /**
     * Test of isConstructor method, of class Method.
     */
    public void testIsConstructor() {
        System.out.println("isConstructor");
        Method instance = getMethod();
        boolean result = instance.isConstructor();
        assertEquals(false, result);
        
        //Try with constructor
        JavaClass javaClass = getMethod().getJavaClass();
        instance = javaClass.getPublicMethod("<init>", new Class[]{});
        result = instance.isConstructor();
        assertEquals(true, result);
    }

    /**
     * Test of hasInitBlock method, of class Method.
     */
    public void testHasInitBlock() {
        System.out.println("hasInitBlock");
        Method instance = getMethod();
        boolean result = instance.hasInitBlock();
        assertEquals(false, result);
        
        //Try with constructor
        JavaClass javaClass = getMethod().getJavaClass();
        instance = javaClass.getPublicMethod("init", new Class[]{});
        result = instance.hasInitBlock();
        assertEquals(true, result);        
    }

    /**
     * Test of getBodyText method, of class Method.
     */
    public void testGetBodyText() {
        System.out.println("getBodyText");
        testReplaceBody();
    }

    /**
     * Test of getElement method, of class Method.
     */
    public void testGetElement() {
        System.out.println("getElement");
        final Method instance = getMethod();
        ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement result = instance.getElement(cinfo);
                assertNotNull(result);
                assertEquals(result.getSimpleName().toString(), instance.getName());
                return null;
            }
        }, instance.getJavaClass().getFileObject());    
    }

//    /**
//     * Test of getCommentText method, of class Method.
//     */
//    public void testGetCommentText() {
//        System.out.println("getCommentText");
//        JavaClass javaClass = getMethod().getJavaClass();
//        final String comment = "Just a comment";
//        ContextMethod cm =  new ContextMethod(null, "foo", Modifier.PUBLIC, Void.TYPE, 
//                new Class[] {String.class}, new String[] {"name"}, null, comment);
//        final Method instance = javaClass.addMethod(cm);                
//        
//        ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
//            public Object run(CompilationInfo cinfo) {
//                ExecutableElement element = instance.execElementHandle.resolve(cinfo);
//                String result = instance.getCommentText(cinfo, cinfo.getTrees().getTree(element));
//                assertNotNull(result);
//                assertEquals(result, comment);
//                return null;
//            }
//        }, instance.getJavaClass().getFileObject());            
//    }

    /**
     * Test of getModifierFlags method, of class Method.
     */
    public void testGetModifierFlags() {
        System.out.println("getModifierFlags");
        JavaClass javaClass = getMethod().getJavaClass();
        ContextMethod cm =  new ContextMethod(null, "foo", Modifier.PROTECTED, Void.TYPE, 
                new Class[] {String.class}, new String[] {"name"});
        final Method instance = javaClass.addMethod(cm);                
        
        ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement element = instance.execElementHandle.resolve(cinfo);
                int result = instance.getModifierFlags(cinfo.getTrees().getTree(element));
                assertNotNull(result);
                assertEquals(result, Modifier.PROTECTED);
                return null;
            }
        }, instance.getJavaClass().getFileObject());                 
    }

//    /**
//     * Test of getCursorPosition method, of class Method.
//     */
//    public void testGetCursorPosition() {
//        System.out.println("getCursorPosition");
//        boolean inserted = false;
//        Method instance = null;
//        int[] expResult = null;
//        int[] result = instance.getCursorPosition(inserted);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}

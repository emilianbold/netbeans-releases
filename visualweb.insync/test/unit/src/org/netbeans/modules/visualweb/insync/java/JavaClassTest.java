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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.ContextMethod;
import java.beans.BeanInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.visualweb.insync.InsyncTestBase;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.beans.Naming;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author jdeva
 */
public class JavaClassTest extends InsyncTestBase {
    public JavaClassTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(JavaClassTest.class);
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

    JavaClass createJavaClass(){
        FileObject fObj = getJavaFile(getPageBeans()[0]);
        return JavaClass.getJavaClass(fObj);
    }

    /* Test of getShortName method, of class JavaClass. */
    public void testGetShortName() {
        System.out.println("getShortName");
        JavaClass instance = createJavaClass();
        String result = instance.getShortName();
        assertNotNull(result);
        assertEquals(result, getPageBeans()[0]);
    } 
    
    /* Test of getName method, of class JavaClass. */
    public void testGetName() {
        System.out.println("getName");
        JavaClass instance = createJavaClass();
        String result = instance.getName();
        assertNotNull(result);
        String expResult = getFQN(getPageBeans()[0]);
        assertEquals(result, expResult);
    }

    /* Test of getPackageName method, of class JavaClass. */
    public void testGetPackageName() {
        System.out.println("getPackageName");
        JavaClass instance = createJavaClass();
        String result = instance.getPackageName();
        assertNotNull(result);
        String expResult = getPackageName(getPageBeans()[0]);
        assertEquals(expResult, result);
    } 

    /* Test of getFileObject method, of class JavaClass. */
    public void testGetFileObject() {
        System.out.println("getFileObject");
        FileObject fObj = getJavaFile(getPageBeans()[0]);
        JavaClass instance = JavaClass.getJavaClass(fObj);
        FileObject result = instance.getFileObject();
        assertEquals(fObj, result);
    } 
    
    /* Test of isSubTypeOf method, of class JavaClass. */    
    public void testIsSubTypeOf() {
        System.out.println("isSubTypeOf");
        JavaClass instance = createJavaClass();
        boolean result = false;
        for(String baseTypeName : FacesModel.managedBeanNames) {
            if(instance.isSubTypeOf(baseTypeName)) {
                result = true;
            }
        }   
        assertEquals(result, true);
    } 

    /* Test of getMethods method, of class JavaClass. */
    public void testGetMethods() {
        System.out.println("getMethods");
        JavaClass instance = createJavaClass();
        List<Method> result = instance.getMethods();
        assertNotNull(result);
        assert(result.size() > 0);
    } 

    /* Test of getMethodNames method, of class JavaClass. */
    public void testGetMethodNames() {
        System.out.println("getMethodNames");
        JavaClass instance = createJavaClass();
        List<Bean> beans = createBeans();
        instance.addBeans(beans);
        Class[] params = null;
        Class retType = null;
        //Try setter for first bean
        params = new Class[] {beans.get(0).getBeanInfo().getBeanDescriptor().getBeanClass()};
        List result = instance.getMethodNames(params, retType);
        assertNotNull(result);
        assertEquals(result.size(), 1);

        //Try getter for second bean
        params = new Class[] {};
        retType = beans.get(1).getBeanInfo().getBeanDescriptor().getBeanClass();
        result = instance.getMethodNames(params, retType);
        assertNotNull(result);
        assertEquals(result.size(), 1);
    } 

    /* Test of getPropertiesNameAndTypes method, of class JavaClass. */
    public void testGetPropertiesNameAndTypes() {
        System.out.println("getPropertiesNameAndTypes");
        JavaClass instance = createJavaClass();
        HashMap result = instance.getPropertiesNameAndTypes();
        assertNotNull(result);
        assert(result.size() > 0);
        List<Bean> beans = createBeans();
        instance.addBeans(beans);
        HashMap resultAfterAdding = instance.getPropertiesNameAndTypes();
        assertNotNull(resultAfterAdding);
        assertEquals(resultAfterAdding.size(), beans.size()+result.size());
    } 

     /* Test of getField method, of class JavaClass. */
    public void testGetField() {
        System.out.println("getField");
        JavaClass instance = createJavaClass();
        List<Bean> beans = createBeans();
        instance.addBeans(beans);
        ElementHandle result = instance.getField(beans.get(0).getName());
        assertNotNull(result);
    }


    private List<Bean> createBeans() {
        String[] types = {
            "com.sun.webui.jsf.component.Button",
            "com.sun.webui.jsf.component.TextField"
        };
        return createBeans(types);
    }
                
    /* Test of addBeans method, of class JavaClass. */
    public void testAddBeans() {
        System.out.println("addBeans");
        List<Bean> beans = createBeans();
        JavaClass instance = createJavaClass();
        instance.addBeans(beans);
    } 

    /* Test of removeBeans method, of class JavaClass. */
    public void testRemoveBeans() {
        System.out.println("removeBeans");
        List<Bean> beans = createBeans();
        JavaClass instance = createJavaClass();
        instance.removeBeans(beans);
    } 

    /* Test of renameProperty method, of class JavaClass. */
    public void testRenameProperty() {
        System.out.println("renameProperty");
        JavaClass instance = createJavaClass();
        List<Bean> beans = createBeans();
        instance.addBeans(beans);
        String name = beans.get(0).getName();
        ElementHandle field = instance.getField(name);
        assertNotNull(field);
        String newName = name + "1";
        List<FileObject> fObjs = new ArrayList<FileObject>();
        for(String beanName : getBeanNames()) {
            fObjs.add(getJavaFile(beanName));
        }
        instance.renameProperty(name, newName, fObjs);

        field = instance.getField(name);
        assertNull(field);
        field = instance.getField(newName);
        assertNotNull(field);
    } 

    /* Test of addMethod method, of class JavaClass. */
    public void testAddMethod() {
        System.out.println("addMethod");
        JavaClass instance = createJavaClass();
        String name = "foo";
        Class[] params = new Class[] {String.class};
        ContextMethod cm =  new ContextMethod(null, name, Modifier.PUBLIC,
            Void.TYPE, params, new String[] {"name"});
        Method result = instance.addMethod(cm);
        assertNotNull(result);
        Method method = instance.getMethod(name, params);
        assertNotNull(method);
        assertEquals(name, method.getName());
    } 

    /* Test of getMethod method, of class JavaClass. */
    public void testGetMethod() {
        System.out.println("getMethod");
        JavaClass instance = createJavaClass();
        List<Bean> beans = createBeans();
        instance.addBeans(beans);
        Class[] params = null;
        //Try setter for first bean
        params = new Class[] {beans.get(0).getBeanInfo().getBeanDescriptor().getBeanClass()};
        String methodName = Naming.setterName(beans.get(0).getName());
        Method result = instance.getMethod(methodName, params);
        assertNotNull(result);

        //Try getter for second bean
        params = new Class[] {};
        methodName = Naming.getterName(beans.get(1).getName());
        result = instance.getMethod(methodName, params);
        assertNotNull(result);
    } 

//    public void testAddDelegatorMethod() {
//        System.out.println("addDelegatorMethod");
//        MethodInfo mInfo = null;
//        JavaClass instance = null;
//        DelegatorMethod expResult = null;
//        DelegatorMethod result = instance.addDelegatorMethod(mInfo);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of addDelegatorMethod method, of class JavaClass. */
//
//    public void testAddEventMethod() {
//        System.out.println("addEventMethod");
//        MethodInfo mInfo = null;
//        JavaClass instance = null;
//        EventMethod expResult = null;
//        EventMethod result = instance.addEventMethod(mInfo);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of addEventMethod method, of class JavaClass. */
//
//
//    public void testGetEventMethod() {
//        System.out.println("getEventMethod");
//        String name = "";
//        Class[] params = null;
//        JavaClass instance = null;
//        EventMethod expResult = null;
//        EventMethod result = instance.getEventMethod(name, params);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getEventMethod method, of class JavaClass. */
//
//    public void testGetDelegatorMethod() {
//        System.out.println("getDelegatorMethod");
//        String name = "";
//        Class[] params = null;
//        JavaClass instance = null;
//        DelegatorMethod expResult = null;
//        DelegatorMethod result = instance.getDelegatorMethod(name, params);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    } /* Test of getDelegatorMethod method, of class JavaClass. */
//

    /* Test of getPublicMethod method, of class JavaClass. */
    public void testGetPublicMethod() {
        System.out.println("getPublicMethod");
        JavaClass instance = createJavaClass();
        String name = "foo";
        Class[] params = new Class[] {String.class};
        ContextMethod cm =  new ContextMethod(null, "foo", Modifier.PUBLIC,
            Void.TYPE, params, new String[] {"name"});
        Method method = instance.addMethod(cm);
        assertNotNull(method);
        Method result = instance.getPublicMethod(name, params);
        assertNotNull(result);
        assertEquals(name, result.getName());

        //remove the public method, and add private method and do the test
        method.remove();
        cm =  new ContextMethod(null, "foo", Modifier.PRIVATE,
            Void.TYPE, params, new String[] {"name"});
        method = instance.addMethod(cm);
        assertNotNull(method);
        result = instance.getPublicMethod(name, params);
        assertNull(result);
    } 

    /* Test of getJavaClass method, of class JavaClass. */
    public void testGetJavaClass() {
        System.out.println("getJavaClass");
        FileObject fObj = getJavaFile(getPageBeans()[0]);
        JavaClass result = JavaClass.getJavaClass(fObj);
        assertNotNull(result);
        assertEquals(result.getShortName(), getPageBeans()[0]);
    } 
}

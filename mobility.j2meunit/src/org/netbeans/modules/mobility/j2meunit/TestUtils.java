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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestUtil.java
 *
 * Created on March 23, 2006, 4:43 PM
 *
 */
package org.netbeans.modules.mobility.j2meunit;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.jmi.javamodel.AnnotationType;
import org.netbeans.jmi.javamodel.Array;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.ParameterizedType;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.TypeReference;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.junit.plugin.JUnitPlugin;

/**
 *
 * @author bohemius
 */
public class TestUtils {
    
    static final String TEST_CLASSNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_test_classname_prefix");                //NOI18N
    static final String TEST_CLASSNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_test_classname_suffix");                //NOI18N
    static final String SUITE_CLASSNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_suite_classname_prefix");               //NOI18N
    static final String SUITE_CLASSNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_suite_classname_suffix");               //NOI18N
    static final boolean GENERATE_TESTS_FROM_TEST_CLASSES = NbBundle.getMessage(
            TestUtils.class,"PROP_generate_tests_from_test_classes").equals("true");    //NOI18N
    static final String TEST_METHODNAME_PREFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_test_method_prefix");                   //NOI18N
    static final String TEST_METHODNAME_SUFFIX = NbBundle.getMessage(
            TestUtils.class,"PROP_test_method_suffix");
    static final String TEST_RUNNER_NAME = "TestRunnerMIDlet";//NOI18N TODO add to bundle
    
    /**
     * Gets all top-level classes from file.
     * @param fo the <code>FileObject</code> to examine
     * @return Collection<JavaClass>, not null
     */
    static Collection getAllClassesFromFile(FileObject fo) {
        if (fo == null) {
            return Collections.EMPTY_LIST;
        }
        
        System.out.println("inspecting fileobject: "+fo.getName());
        Iterator it = JavaModel.getResource(fo).getClassifiers().iterator();
        LinkedList ret = new LinkedList();
        
        while (it.hasNext()) {
            Element e = (Element)it.next();
            if (e instanceof JavaClass) {
                ret.add((JavaClass) e);
            }
        }
        return ret;
    }
    
    static TypeReference getTypeReference(JavaModelPackage pkg, String name) {
        return pkg.getMultipartId().createMultipartId(name, null, Collections.EMPTY_LIST);
    }
    
    static String getTestMethodName(String origMethodName) {
        return TEST_METHODNAME_PREFIX+origMethodName+TEST_METHODNAME_SUFFIX;
    }
    
    static String getOriginalMethodName(String testMethodName) {
        if (TEST_METHODNAME_PREFIX==null || TEST_METHODNAME_PREFIX.equals(""))
            return
            testMethodName.substring(testMethodName.length()-TEST_METHODNAME_SUFFIX.length());
        else if (TEST_METHODNAME_SUFFIX==null || TEST_METHODNAME_SUFFIX.equals(""))
            return
            testMethodName.substring(TEST_METHODNAME_PREFIX.length(),testMethodName.length());
        else
            return
            testMethodName.substring(TEST_METHODNAME_PREFIX.length(),testMethodName.length()-TEST_METHODNAME_SUFFIX.length());
    }
    
    static String getTestClassName(String origClassName) {
        return TEST_CLASSNAME_PREFIX+origClassName+TEST_CLASSNAME_SUFFIX;
    }
    
    static String getFullyQualifiedTestClassName(JavaClass clazz) {
        if (clazz==null)
            return null;
        else
            if (clazz.getResource().getPackageName().equals(""))
                return getTestClassName(clazz.getSimpleName());
            else
                return clazz.getResource().getPackageName()+"."+getTestClassName(clazz.getSimpleName());
    }
    
    static String getFullTestClassFileName(JavaClass clazz) {
        if (clazz==null)
            return null;
        else
            return getFullyQualifiedTestClassName(clazz).replace('.','/')+".java";
    }
    
    static String getTypeNameString(Type type) {
        if (!(type instanceof ClassDefinition)) {     //e.g. primitive types
            return type.getName();
        }
        if (type instanceof Array) {
            return getTypeNameString(((Array) type).getType())
                    + "[]";                                          //NOI18N
        }
        if (!(type instanceof JavaClass)) {
            return type.getName();       //handle unknown Type subinterfaces
        }
        
        return type.getName();        //arbitrary Java class, TODO fix imports or use fully qualified Java name
    }
    
    static String getDefaultValue(Type type) {
        final String typeName = type.getName();
        
        if (typeName.equals("void")) {                               //NOI18N
            return null;
        } else if (typeName.equals("int")) {                         //NOI18N
            return "0";                                                 //NOI18N
        } else if (typeName.equals("float")) {                       //NOI18N
            return "0.0F";                                              //NOI18N
        } else if (typeName.equals("long")) {                        //NOI18N
            return "0L";                                                //NOI18N
        } else if (typeName.equals("double")) {                      //NOI18N
            return "0.0";                                               //NOI18N
        } else if (typeName.equals("boolean")) {                     //NOI18N
            return "true";                                              //NOI18N
        } else if (typeName.equals("java.lang.String")) {            //NOI18N
            return "\"\"";                                              //NOI18N
        } else if (typeName.equals("short")) {                       //NOI18N
            return "0";                                                 //NOI18N
        } else if (typeName.equals("byte")) {                        //NOI18N
            return "0";                                                 //NOI18N
        } else if (typeName.equals("char")) {                        //NOI18N
            return "' '";                                               //NOI18N
        } else {
            assert !(type instanceof PrimitiveType);
            return "null";                                              //NOI18N
        }
    }
    
    static String getParamString(List testParameters) {
        Iterator it=testParameters.iterator();
        StringBuffer result=new StringBuffer("");
        
        while (it.hasNext()) {
            result.append(((Parameter) it.next()).getName());
            if (it.hasNext())
                result.append(",");
        }
        return result.toString();
    }
    
    static boolean isTestable(FileObject fo) {
        if (!(fo.getName().endsWith(TEST_RUNNER_NAME) || fo.getName().endsWith(TEST_CLASSNAME_SUFFIX)))
            return true;
        
        return false;
    }
    
}

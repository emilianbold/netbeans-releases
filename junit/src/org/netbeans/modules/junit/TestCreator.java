/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TestCreator.java
 *
 * Created on January 19, 2001, 1:02 PM
 */

package org.netbeans.modules.junit;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.openide.src.*;
import org.openide.filesystems.*;

/**
 *
 * @author  vstejskal
 * @version 1.0
 */
public class TestCreator extends java.lang.Object {

    /* attributes - private */
    static private final String SUPER_CLASS_NAME                = "TestCase";
    static private final String JUNIT_FRAMEWORK_PACKAGE_NAME    = "junit.framework";
    
    static private final String forbidenMethods[]               = {"main", "suite", "setUp"};
    static private final String SUIT_BLOCK_START                = "--JUNIT:";
    static private final String SUIT_BLOCK_END                  = ":JUNIT--";
    static private final String SUIT_BLOCK_COMMENT              = "//This block was automatically generated and can be regenerated again.\n" +
                                                                  "//Do NOT change lines enclosed by the --JUNIT: and :JUNIT-- tags.\n";
    static private final String SUIT_RETURN_COMMENT             = "//This value MUST ALWAYS be returned from this function.\n";

    /* public methods */

    /** Creates new TestCreator */
    public TestCreator() {
    }

    static public void createTestClass(ClassElement classSource, ClassElement classTarget) throws SourceException {
        SourceElement   srcelSource;
        SourceElement   srcelTarget;
        
        // update the source file of the test class
        srcelSource = classSource.getSource();
        srcelTarget = classTarget.getSource();

        srcelTarget.setPackage(srcelSource.getPackage());
        srcelTarget.addImports(srcelSource.getImports());
        srcelTarget.addImport(new Import(Identifier.create(JUNIT_FRAMEWORK_PACKAGE_NAME), Import.PACKAGE));

        // construct/update test class from the source class
        fillTestClass(classSource, classTarget);
    }
    
    static public void createTestSuit(LinkedList listMembers, String packageName, ClassElement classTarget) throws SourceException {
        SourceElement   srcelTarget;

        // update the source file of the suite class
        srcelTarget = classTarget.getSource();
        
        srcelTarget.setPackage(packageName.length() != 0 ? Identifier.create(packageName) : null);
        srcelTarget.addImport(new Import(Identifier.create(JUNIT_FRAMEWORK_PACKAGE_NAME), Import.PACKAGE));
        
        // construct/update test class from the source class
        fillSuitClass(listMembers, packageName, classTarget);
    }
    
    static public void initialize() {
        // setup the methods filter
        cfg_MethodsFilter = 0;
        cfg_MethodsFilterPackage = JUnitSettings.getDefault().isMembersPackage();
        if (JUnitSettings.getDefault().isMembersProtected()) cfg_MethodsFilter |= Modifier.PROTECTED;
        if (JUnitSettings.getDefault().isMembersPublic()) cfg_MethodsFilter |= Modifier.PUBLIC;
    }
    
    /* private methods */
    static private boolean         cfg_MethodsFilterPackage = true;
    static private int             cfg_MethodsFilter = Modifier.PUBLIC | Modifier.PROTECTED;
    
    static private void addMethods(ClassElement classTest, LinkedList methods) {
        ListIterator    li;
        MethodElement   m;
        
        if (null == methods)
            return;
        
        li = methods.listIterator();
        while (li.hasNext()) {
            m = (MethodElement)li.next();
            try {
                classTest.addMethod(m);
            } catch (SourceException e) {
                // Nothing is done, because it is expected that the method already exist
            }
        }
    }    
    
    static private MethodElement createMainMethod() throws SourceException {
        MethodElement method = new MethodElement();
        method.setName(Identifier.create("main"));
        method.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        method.setReturn(Type.VOID);
        MethodParameter[] params = {new MethodParameter("args", Type.createArray(Type.createFromClass(java.lang.String.class)), false)};
        method.setParameters(params);
        method.setBody("\njunit.textui.TestRunner.run(suite());\n");
        return method;
    }
    
    static private MethodElement createTestClassSuiteMethod(ClassElement classTest) throws SourceException {
        MethodElement method = new MethodElement();
        method.setName(Identifier.create("suite"));
        method.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        method.setReturn(Type.createClass(Identifier.create("Test")));
        method.setBody("\nreturn new TestSuite(" + classTest.getName().getName() + ".class);\n");
        return method;
    }

    static private ConstructorElement createTestConstructor(Identifier className) throws SourceException {
        ConstructorElement constr = new ConstructorElement();
        constr.setName(className);
        constr.setModifiers(Modifier.PUBLIC);
        MethodParameter[] params = {new MethodParameter("testName", Type.createFromClass(java.lang.String.class), false)};
        constr.setParameters(params);
        constr.setBody("\nsuper(testName);\n");
        return constr;
    }

    static private LinkedList createVariantMethods (ClassElement classSource) throws SourceException {
        LinkedList      methodList = new LinkedList();
        MethodElement[] allMethods = classSource.getMethods();                        
        MethodElement   method;
        String          name;
        Identifier      newName;
        StringBuffer    newBody = new StringBuffer();
        
        for (int i = 0; i < allMethods.length; i++) {
            // check modifiers against the settings of test generation
            if ((allMethods[i].getModifiers() & Modifier.PRIVATE) == 0 &&
                ((allMethods[i].getModifiers() & cfg_MethodsFilter) != 0 ||
                ((allMethods[i].getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && cfg_MethodsFilterPackage))) {
               name = allMethods[i].getName().getName();
                if (!isForbiden(name) && (allMethods[i].getModifiers() & Modifier.ABSTRACT) == 0) {
                    method = new MethodElement();
                    newName = Identifier.create("test" + name.substring(0,1).toUpperCase() + name.substring(1));
                    
                    // generate only one test method for overloaded source methods
                    if (!existsMethod(methodList, newName)) {
                        method.setName(newName);
                        method.setModifiers(Modifier.PUBLIC);

                        // generate JavaDoc for test method
                        if (JUnitSettings.getDefault().isJavaDoc()) {
                            method.getJavaDoc().setText("Test of " + name + " method, of class " + classSource.getName().getFullName() + ".");
                        }
                        
                        // generate the body of method
                        newBody.delete(0, newBody.length());
                        newBody.append("\n");
                        if (JUnitSettings.getDefault().isBodyContent()) {
                            // generate default bodies, printing the name of method
                            newBody.append("System.out.println(\"" + newName.getName() + "\");\n");
                        }
                        if (JUnitSettings.getDefault().isBodyComments()) {
                            // generate comments to bodies
                            newBody.append("// Add your test code here.\n");
                        }
                        method.setBody(newBody.toString());
                        method.setReturn(Type.VOID);
                        methodList.add(method);
                    }
                }                                                    
            }
        }                
        return methodList;
    }
    
    static private boolean existsMethod(LinkedList methodList, Identifier id) {
        ListIterator    li;
        MethodElement   m;
        
        li = methodList.listIterator();
        while(li.hasNext()) {
            m = (MethodElement) li.next();
            if (m.getName().equals(id))
                return true;
        }
        return false;
    }

    static private void fillGeneral(ClassElement classTest) throws SourceException {
        ConstructorElement  constr;
        
        // set explicitly supper class and modifiers
        classTest.setSuperclass(Identifier.create(SUPER_CLASS_NAME));
        classTest.setModifiers(Modifier.PUBLIC);

        // remove default ctor, if exists (shouldn't throw exception)
        constr = classTest.getConstructor(new Type[] {});
        if (null != constr)
            classTest.removeConstructor(constr);
        
        //fill classe's constructor
        constr = createTestConstructor(classTest.getName());
        try {
            classTest.addConstructor(constr);
        } catch (SourceException e) {
            // Nothing is done, because it is expected that constructor already exists
        }
    }
    
    static private void fillTestClass(ClassElement classSource, ClassElement classTest) throws SourceException {
        LinkedList methods;
        
        fillGeneral(classTest);
        
        // fill main and suite methods
        methods = new LinkedList();
        methods.add(createMainMethod());
        methods.add(createTestClassSuiteMethod(classTest));
        addMethods(classTest, methods);
        
        // fill methods according to the iface of tested class
        methods = createVariantMethods(classSource);
        addMethods(classTest, methods);
    }

    static private void fillSuitClass(LinkedList listMembers, String packageName, ClassElement classTest) throws SourceException {
        LinkedList      methods;
        MethodElement   method;
        StringBuffer    newBody;
        StringTokenizer oldBody;
        String          line;
        boolean         insideBlock;
        boolean         justBlockExists;
        
        fillGeneral(classTest);
        
        // create main method
        methods = new LinkedList();
        methods.add(createMainMethod());

        // create suite method
        method = classTest.getMethod(Identifier.create("suite"), new Type[] {});
        if (null == method) {
            method = new MethodElement();
            method.setName(Identifier.create("suite"));
        }
        method.setModifiers(Modifier.STATIC | Modifier.PUBLIC);
        method.setReturn(Type.createClass(Identifier.create("Test")));

        // generate the body of suite method
        oldBody = new StringTokenizer(method.getBody(), "\n");
        newBody = new StringBuffer();
        insideBlock = false;
        justBlockExists = false;
        while (oldBody.hasMoreTokens()) {
            line = oldBody.nextToken();
            
            if (-1 != line.indexOf(SUIT_BLOCK_START)) {
                insideBlock = true;
            }
            else if (-1 != line.indexOf(SUIT_BLOCK_END)) {
                // JUst's owned suite block, regenerate it
                insideBlock = false;
                generateSuitBody(classTest.getName().getName(), newBody, listMembers, true);
                justBlockExists = true;
            }
            else if (!insideBlock) {
                newBody.append(line);
                newBody.append("\n");
            }
        }
        
        if (!justBlockExists)
            generateSuitBody(classTest.getName().getName(), newBody, listMembers, false);

        method.setBody(newBody.toString());
        methods.add(method);
        
        // add/update methods to the class
        addMethods(classTest, methods);
    }
    
    static private void generateSuitBody(String testName, StringBuffer body, LinkedList members, boolean alreadyExists) {
        ListIterator    li;
        String          name;
        
        body.append("\n//" + SUIT_BLOCK_START + "\n");
        body.append(SUIT_BLOCK_COMMENT);
        body.append("TestSuite suite = new TestSuite(\"" + testName + "\");\n");
        li = members.listIterator();
        
        while (li.hasNext()) {
            name = (String) li.next();
            body.append("suite.addTest(" + name + ".suite());\n");
        }
        body.append("//" + SUIT_BLOCK_END + "\n");

        if (!alreadyExists) {
            body.append(SUIT_RETURN_COMMENT);
            body.append("return suite;\n");
        }
    }
    
    static private boolean isForbiden(String name) {
        for (int i = 0; i < forbidenMethods.length; i++) {            
            if (forbidenMethods[i].equals(name)) 
              return true;            
        }
        return false;
    }
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import org.netbeans.test.editor.app.core.Test;
import java.io.InputStream;

/**
 *
 * @author  jlahoda
 * @version 
 */
public class GenerateTests extends Object {
    
    private String masterPackage = "org.netbeans.test.editor.app.tests";

    /** Creates new GenerateTests */
    public GenerateTests() {
    }
    
    private void generateGoldenFile(Element test, FileObject file) throws IOException {
	String content = Test.loadString(test, "Golden");
	
	if (content == null) {
	    content = "";
	}
	FileLock lock = null;
	PrintWriter write = null;
	try {
	    lock = file.lock();
	    write = new PrintWriter(file.getOutputStream(lock));
	    write.print(content);
	    write.flush();
	    write.close();
	    write = null;
	} finally {
	    if (lock != null) {
		lock.releaseLock();
	    }
	    if (write != null) {
		    write.close();
	    }
	}
    }
    
    private void generateCall(Element call, PrintWriter result) {
	String file = call.getAttribute("File");
	String action = call.getAttribute("Action");
	
	if (file != null) {
            result.println("\"" + file + "\",");
	} else {
	    result.println("\"\",");
	}
	if (action != null) {
            result.println("\"" + action + "\",");
	} else {
	    result.println("\"\",");
	}
    }
    
    private boolean isCVS(FileObject fo) {
        return fo.isFolder() && "CVS".equals(fo.getName());
    }
    
    private void removeContentExceptCVS() throws IOException {
        FileObject toRemove = TopManager.getDefault().getRepository().find(masterPackage, "AppTestPerformer", "java");
        
        if (toRemove != null)
            toRemove.delete();
        
        toRemove = TopManager.getDefault().getRepository().find(masterPackage + ".data.goldenfiles.AppTestPerformer", null, null);
        
        FileObject[] fo = toRemove.getChildren();
        
        for (int cntr = 0; cntr < fo.length; cntr++) {
            if (!isCVS(fo[cntr]))
                fo[cntr].delete();
        }
    }
    
    private void generateTest(Element test, PrintWriter result) throws IOException {
	String name = test.getAttribute("Name");
        
        System.err.println("name = " + name );
	if (name == null || "".equals(name)) {
	    throw new IllegalArgumentException("Test element has empty Name attribute!");
	}
	
	NodeList nodes = test.getElementsByTagName("Call");
	StringBuffer params = new StringBuffer();
        
        result.println("public void test" + name + "() throws Exception {");
        result.println("PrintWriter ref = null;");
        result.println("PrintWriter log = null;");
        result.println("String[] arguments = new String[] {");
	
	for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
            generateCall((Element)nodes.item(cntr), result);
	}
        
        result.println("};");
        
        result.println("try {");
        result.println("ref = new PrintWriter(getRef());");
        result.println("log = new PrintWriter(getLog());");
        result.println("new CallTestGeneric().runTest(arguments, log, ref);");
        result.println("} finally {");
        result.println("if (ref != null) ref.flush();");
        result.println("if (log != null) log.flush();");
        result.println("}");
        result.println("}");

        FileObject goldenDir = TopManager.getDefault().getRepository().find(masterPackage + ".data.goldenfiles.AppTestPerformer", null, null);
        FileObject goldenFile = goldenDir.createData("test" + name + ".pass");
        
	generateGoldenFile(test, goldenFile);
    }
    
    private void createAllTests(Element tests) throws IOException {
        removeContentExceptCVS();
        FileObject testFileDir  = TopManager.getDefault().getRepository().find(masterPackage, null, null);
        FileObject testFile     = testFileDir.createData("AppTestPerformer", "java");
        FileLock   testFileLock = null;
        PrintWriter result      = null;
        try {
            testFileLock = testFile.lock();
            result = new PrintWriter(testFile.getOutputStream(testFileLock));
            
            result.println("//This file was automaticaly generated. Do not modify.");
            result.println("/*");
            result.println(" *                 Sun Public License Notice");
            result.println(" *");
            result.println(" * The contents of this file are subject to the Sun Public License");
            result.println(" * Version 1.0 (the \"License\"). You may not use this file except in");
            result.println(" * compliance with the License. A copy of the License is available at");
            result.println(" * http://www.sun.com/");
            result.println(" *");
            result.println(" * The Original Code is NetBeans. The Initial Developer of the Original");
            result.println(" * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun");
            result.println(" * Microsystems, Inc. All Rights Reserved.");
            result.println(" */");
            result.println("");
            result.println("package " + masterPackage + ";");
            result.println("import java.io.PrintWriter;");
            result.println("import org.openide.filesystems.*;");
            result.println("import org.netbeans.junit.NbTestCase;");
            result.println("import org.netbeans.junit.NbTestSuite;");

            result.println("public class AppTestPerformer extends NbTestCase {");
            result.println("public AppTestPerformer(String name) {");
            result.println("super(name);");
            result.println("}");
            result.println("public void tearDown() {");
            result.println("compareReferenceFiles();");
            result.println("}");
            NodeList nodes = tests.getElementsByTagName("Test");
            
            for (int cntr = 0; cntr < nodes.getLength(); cntr++) {
                generateTest((Element)nodes.item(cntr), result);
            }
            result.println("}");
        } finally {
            if (result != null)
                result.close();
            if (testFileLock != null)
                testFileLock.releaseLock();
        }
    }
	
    public static final void main(String[] args) {
	if (args.length < 1) {
	    System.err.println("FATAL - First argument has to be generator file!");
            System.err.flush();
	    return;
	}
	
	InputStream input = GenerateTests.class.getResourceAsStream(args[0]);
	
	if (input == null) {
	    System.err.println("FATAL - Generator file " + args[0] + " not found!");
            System.err.flush();
	    return;
	}
	
	try {
	    Document doc = org.openide.xml.XMLUtil.parse(new InputSource(input), false, false, null, null);
	    GenerateTests generator = new GenerateTests();
	    
	    generator.createAllTests((Element) doc.getElementsByTagName("TestList").item(0));
	} catch (IOException e) {
	    e.printStackTrace(System.err);
	} catch (SAXException e) {
	    e.printStackTrace(System.err);
	}
        System.err.flush();
    }

}

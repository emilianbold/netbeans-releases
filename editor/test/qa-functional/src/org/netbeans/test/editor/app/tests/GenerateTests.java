/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.editor.app.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.netbeans.test.editor.app.core.Test;
import java.io.InputStream;
import java.util.Enumeration;
import org.netbeans.test.editor.app.core.TestCallAction;
import org.netbeans.test.editor.app.core.TestSubTest;
import org.netbeans.test.editor.app.util.ParsingUtils;
import org.openide.filesystems.Repository;

/**
 *
 * @author  ehucka
 * @version
 */
public class GenerateTests extends Object {
    
    /** Generates Performer class from <CODE>test</CODE> with package <CODE>packag</CODE>
     * to the <CODE>path</CODE>. Test is stored into <CODE>file</CODE>.
     * @param test source test
     * @param file source test xml file
     * @param path destination java file path
     * @param packag source file package
     * @throws Exception all errors is thrown out
     */
    public static void generateTest(Test test,String file,String path,String packag) throws Exception {
        System.err.println("Starting generate XTest source code for test "+test.getName());
        String name=test.getName()+"Performer";
        if (Character.isLowerCase(name.charAt(0))) {
            name=name.substring(0,1).toUpperCase()+name.substring(2);
        }
        File dir=new File(path);
        File javaFile=new File(dir,name+".java");
        TestCallAction[] tcas=test.getCallActions();
        generateSource(test,javaFile,file,name,packag);
        generateGoldenFiles(dir,name,tcas);
        System.err.println("Source code: "+javaFile.getName()+" generated.");
    }
    
    /** Generates Golden files to <CODE>dir</CODE> with test <CODE>name</CODE>
     * from TestCallAction list <CODE>acts</CODE>.
     * @param dir destination directory
     * @param name name of the test
     * @param acts list of test's call actions
     * @throws Exception all errors is thrown out
     */
    private static void generateGoldenFiles(File dir,String name,TestCallAction[] acts) throws Exception {
        System.err.println("Generate golden files.");
        File goldenDir=new File(dir,"data");
        if (!goldenDir.exists()) {
            goldenDir.mkdir();
        }
        goldenDir=new File(goldenDir,"goldenfiles");
        if (!goldenDir.exists()) {
            goldenDir.mkdir();
        }
        goldenDir=new File(goldenDir,name);
        if (!goldenDir.exists()) {
            goldenDir.mkdir();
        }
        File golden;
        PrintWriter pw;
        for (int i=0;i < acts.length;i++) {
            System.err.println("    Golden file: "+"test"+acts[i].getName()+".pass");
            golden=new File(goldenDir,"test"+acts[i].getName()+".pass");
            pw=new PrintWriter(new FileWriter(golden));
            pw.print(acts[i].getOutput());
            pw.close();
        }
    }
    
    /** Generates source file (.java) from test into source.
     * @param test Source test.
     * @param source destination java file
     * @param file test xml file
     * @param name name of test performer (class)
     * @param packag package of destination java file
     * @throws Exception all errors
     */
    private static void generateSource(Test test,File source,String file,String name,String packag) throws Exception {
        PrintWriter result = null;
        result = new PrintWriter(new FileWriter(source));
        
        result.println("//This file was automaticaly generated. Do not modify.");
        result.println("/*");
        result.println(" * Some License");
        result.println(" */");
        result.println("");
        result.println("package " + packag + ";");
        result.println("import java.io.PrintWriter;");
        result.println("import org.openide.filesystems.*;");
        result.println("import org.netbeans.junit.NbTestCase;");
        result.println("import org.netbeans.junit.NbTestSuite;");
        result.println("import java.io.File;");
        result.println("import org.netbeans.test.editor.LineDiff;\n");
        result.println("\n/**\n *\n * @author "+test.getAuthor());
        result.println(" * @version "+test.getVersion());
        result.println(" * "+test.getComment()+" */\n");
        result.println("public class "+name+" extends NbTestCase {");
        result.println("public "+name+"(String name) {");
        result.println("super(name);");
        result.println("}");
        result.println("public void tearDown() throws Exception {");
        result.println("    assertFile(\"Output does not match golden file.\", getGoldenFile(), new File(getWorkDir(), this.getName() + \".ref\"), new File(getWorkDir(), this.getName() + \".diff\"), new LineDiff(false));");
        result.println("}");
        TestCallAction[] tcas=test.getCallActions();
        
        for (int cntr = 0; cntr < tcas.length; cntr++) {
            generateMethod(tcas[cntr],result,file,packag);
        }
        result.println("}");
        result.close();
    }
    
    /** Generates one test... method for <CODE>TestCallAction</CODE> into result.
     * @param tca TestCallAction
     * @param result destination stream
     * @param file test xml file
     * @param packag source file package
     * @throws Exception all errors
     */
    private static void generateMethod(TestCallAction tca, PrintWriter result, String file, String packag) throws Exception {
        System.err.println("Generate method for action "+tca.getName());
        result.println("\n/**\n * Call Action: "+tca.getName());
        if (tca.getOwner() instanceof TestSubTest) {
            result.println(" * Sub Test: "+((TestSubTest)(tca.getOwner())).getName());
            result.println(" * Author:   "+((TestSubTest)(tca.getOwner())).getAuthor());
            result.println(" * Version:  "+((TestSubTest)(tca.getOwner())).getVersion());
            result.println(" * Comment:  "+((TestSubTest)(tca.getOwner())).getComment());
        }
        result.println(" */\n");
        String name=tca.getName();
        if (Character.isLowerCase(name.charAt(0))) {
            name=name.substring(0,1).toUpperCase()+name.substring(2);
        }
        result.println("public void test" + tca.getName() + "() throws Exception {");
        result.println("PrintWriter ref = null;");
        result.println("PrintWriter log = null;");
        result.println("String[] arguments = new String[] {");
        result.println("\""+generateFileName(file,packag)+"\",");
        result.println("\""+makeCallActionName(tca)+"\"");
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
    }
    
    /** Generates file name to source (parameter). */
    private static String generateFileName(String name, String packag) {
        String n=name.substring(name.lastIndexOf('/')+1);
        if (n == null || n.length() == 0) {
            n=name.substring(name.lastIndexOf('\\')+1);
        }
        return "/"+packag.replace('.','/')+"/"+n;
    }
    
    private static String makeCallActionName(TestCallAction act) {
        if (act.getOwner() instanceof TestSubTest) {
            return act.getOwner()+"."+act.getName();
        } else {
            return act.getName();
        }
    }
}

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
 * TestKit.java
 *
 * Created on 10 May 2006, 15:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.localhistory.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author peter
 */
public final class TestKit {
        
    public static File prepareProject(String category, String project, String project_name) throws Exception {
        //create temporary folder for test
        String folder = "work" + File.separator + "w" + System.currentTimeMillis();
        File file = new File("/tmp", folder); // NOI18N
        file.mkdirs();
        file.mkdirs();
        //PseudoVersioned project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath()); // NOI18N
        new JTextFieldOperator(npnlso, 0).setText(project_name); // NOI18N
        //new JTextFieldOperator(npnlso, 2).setText(folder); // NOI18N
        new NewProjectWizardOperator().finish();
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        return file;
    }

    public static void removeAllData(String projectName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        rootNode.performPopupActionNoBlock("Delete Project");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed(); 
        //TestKit.deleteRecursively(file);
    }
    
    public static void closeProject(String projectName) {
        try {
            Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
            rootNode.performPopupActionNoBlock("Close Project");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        catch (Exception e) {
            
        }    
    }
    
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null)
            return -1;
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            Arrays.sort(expected);
            Arrays.sort(actual);
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }    
            return result;
        }
        return result; 
    }  
    
    public static void createNewPackage(String projectName, String packageName) { 
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();
    }    
    
    public static void createNewElement(String projectName, String packageName, String name) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }    
    
    public static void copyTo(String source, String destination) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source)); 
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
            boolean available = true;
            byte[] buffer = new byte[1024];
            int size;
            try {    
                while (available) {
                    size = bis.read(buffer);
                    if (size != -1) {
                        bos.write(buffer, 0, size);
                    } else {
                        available = false;
                    }                      
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bos.flush();
                bos.close();
                bis.close();
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
    
    public static void printLogStream(PrintStream stream, String message) {
        if (stream != null) {
            stream.println(message);
        }
    }
    
}

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
package org.netbeans.test.cvsmodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Set of utility methods.
 *
 * @author Peter Pis
 */
public class TestKit {
    final static String MODIFIED_COLOR = "#0000FF";
    final static String NEW_COLOR = "#008000";
    final static String CONFLICT_COLOR = "#FF0000";
    final static String IGNORED_COLOR = "#999999";
    
    public static File createTmpFolder(String prefix) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");  // NOI18N
        File tmpFolder = new File(tmpDir);
        // generate unique name for tmp folder
        File tmp = File.createTempFile(prefix, "", tmpFolder);  // NOI18N
        if (tmp.delete() == false) {
            throw new IOException("Can not delete " + tmp);
        };
        if (tmp.mkdirs() == false) {
            throw new IOException("Can not create " + tmp);
        };
        return tmp;
    }

    public static void deleteRecursively(File dir) {
        if (dir.isDirectory()) {
            String[] files = dir.list();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(new File(dir, files[i]));  // RECURSION
            }
        }
        dir.delete();
    }
    
    public static void pseudoVersion(File file) throws Exception {
        File CVSdir = new File(file, "CVS");
        File Entries = new File(CVSdir, "Entries");
        OutputStream out = new FileOutputStream(Entries);
        String data = "D\n" + 
                      "/Main.java/1.1/Wed Sep 14 08:51:41 2005//";
        out.write(data.getBytes("utf8"));
        out.flush();
        out.close();
        //Root
        File Root = new File(CVSdir, "Root");
        out = new FileOutputStream(Root);
        data = ":local:/Projects/CVSrepo";
        out.write(data.getBytes("utf8"));
        out.flush();
        out.close();
        //Repository
        File Repository = new File(CVSdir, "Repository"); 
        out = new FileOutputStream(Repository);
        data = "ForImport/src/forimport";
        out.write(data.getBytes("utf8"));
        out.flush();
        out.close();
    }
    
    public static File prepareProject(String category, String project, String project_name, String edit_file) throws Exception {
        //create temporary folder for test
        String folder = "" + System.currentTimeMillis();
        File file = new File("/tmp", folder); // NOI18N
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
        return file; 
    }
    
    public static void removeAllData(String project_name) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        rootNode.performPopupActionNoBlock("Delete Project");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed(); 
        //TestKit.deleteRecursively(file);
    }
    
    public static void unversionProject(File file, String project_name) throws Exception {
        File folder_CVS = new File(file, project_name + File.separator + "src" + File.separator + project_name.toLowerCase() + File.separator + "CVS");
        //System.out.println("File: " + file);
        TestKit.deleteRecursively(folder_CVS);
        //System.out.println("File: " + file.getAbsolutePath());
        EditorOperator eo = new EditorOperator("Main.java");
        eo.insert("//Comment\n");
        eo.save();
    }
    
    public static void createNewElements(String projectName) {
        String pack = "xx";
        
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java GUI Forms");
        nfwo.selectFileType("JFrame Form");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewJFrame");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
    }

    public static void createNewElementsCommitCvs11(String projectName) {
        String pack = "xx";
        
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass2");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass3");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass4");
        nfnlso.selectPackage(pack);
        nfnlso.finish();
    }

    public static void createNewElementsCommitCvs12(String projectName) {
        String pack = "aa";
        
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("aa");
        nfnlso.finish();

        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("bb");
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass");
        nfnlso.selectPackage("aa");
        nfnlso.finish();
        
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass2");
        nfnlso.selectPackage("aa");
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass");
        nfnlso.selectPackage("bb");
        nfnlso.finish();
        //
        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java Classes");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText("NewClass2");
        nfnlso.selectPackage("bb");
        nfnlso.finish();
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
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        }
        return result; 
    }
    
    public static String getCVSroot(File cvsFolder) {
        String root = new String();
        if (cvsFolder.isDirectory()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(cvsFolder + File.separator + "Root"));
                root = br.readLine();
                if (root == null || root.length() == 0)
                    return "";
                br.close();
            } catch (IOException e) {
                return "";
            }    
        }
        return root;
    }
    
    public static String getColor(String nodeHtmlDisplayName) {
        
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }
    
    public static InputStream getStream(String dir, String protocolName) throws Exception {
        File file = new File(dir, protocolName);
        InputStream in = new FileInputStream(file);    
        return in;
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
}

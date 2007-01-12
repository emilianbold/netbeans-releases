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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;



/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class Utilities {

    public static final String SOURCE_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
    public static final String TEST_PACKAGES = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_test.src.dir");
    public static final String WEB_PAGES = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_DocBase");
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }

    /**
     * Work around issue 35962 (Main menu popup accidentally rolled up)
     * Issue has been fixed for JDK 1.5, so we will use it only for JDK 1.4.X
     */
    public static void workarroundMainMenuRolledUp() {
        if(System.getProperty("java.version").indexOf("1.4") != -1) {
            String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
            String about = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Title");
            
            new ActionNoBlock(helpMenu, null).perform();
            new NbDialogOperator(about).close();
        }
    }
    
    /**
     * Choose ten selected files from JEdit project
     * @return 
     */
    public static String[][] getTenSelectedFiles(){
        String[][] files_path = { 
            {"bsh","Interpreter.java"},
            {"bsh","JThis.java"},
            {"bsh","Name.java"},
            {"bsh","Parser.java"},
            {"bsh","Primitive.java"},
            {"com.microstar.xml","XmlParser.java"},
            {"org.gjt.sp.jedit","BeanShell.java"},
            {"org.gjt.sp.jedit","Buffer.java"},
            {"org.gjt.sp.jedit","EditPane.java"},
            {"org.gjt.sp.jedit","EditPlugin.java"},
            {"org.gjt.sp.jedit","EditServer.java"} 
        };
        
        return files_path;
    }
    
    /**
     * Open ten selected files from JEdit project
     */
    public static void open10FilesFromJEdit(){
        openFiles("jEdit", getTenSelectedFiles());
        new EventTool().waitNoEvent(20000);
    }
    
    /**
     * Open files
     *
     * @param project project which will be used as source for files to be opened
     * @param files_path path to the files to be opened
     */
    public static void openFiles(String project, String[][] files_path){
        Node[] openFileNodes = new Node[files_path.length];
        
        for(int i=0; i<files_path.length; i++) {
                openFileNodes[i] = new Node(new ProjectsTabOperator().getProjectRootNode(project),SOURCE_PACKAGES + '|' +  files_path[i][0] + '|' + files_path[i][1]);
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                // new OpenAction().performAPI(openFileNodes[i]);
        }
            
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder, 
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
    }
    
    /**
     * Copy file f1 to f2
     */
    public static void copyFile(File f1, File f2) throws Exception {
        int data;
        InputStream fis = new BufferedInputStream(new FileInputStream(f1));
        OutputStream fos = new BufferedOutputStream(new FileOutputStream(f2));
        
        while((data=fis.read())!=-1){
            fos.write(data);
        }
        
    }

    /*
     * open a java file in the editor
     */
    public static EditorOperator openJavaFile(){
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),SOURCE_PACKAGES + "|bsh|Parser.java");
        new OpenAction().performAPI(openFile);
        return EditorWindowOperator.getEditor("Parser.java");

    }
    
    /*
     * open small java file in the editor
     */
    public static EditorOperator openSmallJavaFile(){
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"),SOURCE_PACKAGES + "|org.netbeans.test.performance|Main20kB.java");
        new OpenAction().performAPI(openFile);
        return EditorWindowOperator.getEditor("Main20kB.java");

    }
    
    /*
     * open small form file in the editor
     */
    public static FormDesignerOperator openSmallFormFile(){
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"),SOURCE_PACKAGES + "|org.netbeans.test.performance|JFrame20kB.java");
        new OpenAction().performAPI(openFile);
        return new FormDesignerOperator("JFrame20kB");

    }
    
}

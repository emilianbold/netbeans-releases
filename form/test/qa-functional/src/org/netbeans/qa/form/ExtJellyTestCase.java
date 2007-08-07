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

package org.netbeans.qa.form;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Class with helpers for easy creating jemmy/jelly tests
 *
 * @author Jiri Vagner
 */
public abstract class ExtJellyTestCase extends JellyTestCase {

    private static int MY_WAIT_MOMENT = 500;

    public static String TEST_PROJECT_NAME = "SampleProject"; // NOI18N
    public static String TEST_PACKAGE_NAME = "data"; // NOI18N
    public static String DELETE_OBJECT_CONFIRM = "Confirm Object Deletion"; // NOI18N
    /* Skip file (JFrame,Frame, JDialog, ...) delete in the end of each test */
    public Boolean DELETE_FILES = false;

    /** Constructor required by JUnit */
    public ExtJellyTestCase(String testName) {
        super(testName);
    }

    /**
     * Simple console println
     */
    public void p(String msg) {
        System.out.println(msg);
    }

    /**
     * Simple console println
     */
    public void p(Boolean msg) {
        p(String.valueOf(msg));
    }

    /**
     * Simple console println
     */
    public void p(int msg) {
        p(String.valueOf(msg));
    }

    /**
     * Creates new file using NB New File Wizzard
     * @return name of a new file
     * @param project name of project to create file in
     * @param packageName package for a new file
     * @param category category from first step of new file wizzard
     * @param fileType filetype from first step of new file wizzard
     * @param name name prefix of a new file, timestamp will be added to avoid name clash
     */
    private String createFile(String project, String packageName, String category, String fileType, String name) {
        return createFile(project, packageName, category, fileType, name, null);
    }


    public String getTimeStamp() {
        return String.valueOf(new Date().getTime());
    }


    private String createFile(String project, String packageName, String category, String fileType, String name, String beanName) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(project);
        nfwo.selectCategory(category);
        nfwo.selectFileType(fileType);
        nfwo.next();

        String fileName = name + String.valueOf(new Date().getTime());

        if (beanName == null) {
            NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
            nfnlso.txtObjectName().clearText();
            nfnlso.txtObjectName().typeText(fileName);
            nfnlso.setPackage(packageName);
            nfnlso.finish();
        } else {
            NewBeanFormOperator nbfOp = new NewBeanFormOperator();
            nbfOp.txtClassName().clearText();
            nbfOp.txtClassName().typeText(fileName);

            nbfOp.cboPackage().clearText();
            nbfOp.typePackage(packageName);

            nbfOp.next();

            NewBeanFormSuperclassOperator superOp = new NewBeanFormSuperclassOperator();
            superOp.setSuperclass(beanName);
            superOp.finish();
        }

        // following code avoids issue nr. 60418
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(project);
        prn.select();
        Node formnode = new Node(prn, "Source Packages|" + packageName + "|" + fileName); // NOI18N
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        // end of issue code
        return fileName;
    }


    /**
     * Removes file from actual project and actual test package
     */
    public void removeFile(String fileName) {
        if (DELETE_FILES) {
            ProjectsTabOperator project = new ProjectsTabOperator();
            Node node = new Node(project.tree(), TEST_PROJECT_NAME + "|Source Packages|" + TEST_PACKAGE_NAME + "|" + fileName + ".java"); // NOI18N
            DeleteAction act = new DeleteAction();
            act.performPopup(node);

            //new NbDialogOperator(DELETE_OBJECT_CONFIRM).yes();
            NbDialogOperator op = new NbDialogOperator("Safe Delete");
            new JButtonOperator(op, "Refactor").clickMouse();
        }
    }

    /**
     * Adds new bean into palette
     *
     * @param beanFileName
     */
    public void addBean(String beanFileName) {
        Node fileNode = openFile(beanFileName);
        waitAMoment();

        new ActionNoBlock("Tools|Add To Palette...", null).perform(); // NOI18N
        SelectPaletteCategoryOperator op = new SelectPaletteCategoryOperator();
        op.lstPaletteCategories().selectItem(SelectPaletteCategoryOperator.ITEM_BEANS);
        op.ok();

        CompileAction compAct = new CompileAction();
        compAct.perform(fileNode);
        waitAMoment();
    }

    /**
     * Opens file into nb editor
     * @param fileName
     * @return node
     */
    public Node openFile(String fileName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(TEST_PROJECT_NAME);
        prn.select();

        String path = "Source Packages|" + TEST_PACKAGE_NAME + "|" + fileName; // NOI18N
        //p(path);
        Node formnode = new Node(prn, path); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        return formnode;
    }


    public String createBeanFormFile(String beanClassName) {
        return createFile(TEST_PROJECT_NAME, TEST_PACKAGE_NAME, "Swing GUI Forms", "Bean Form", "MyBeanForm", beanClassName); // NOI18N
    }

    /**
     * Creates new JDialog file in project
     * @return new file name
     */
    public String createJDialogFile() {
        return createFile(TEST_PROJECT_NAME, TEST_PACKAGE_NAME, "Swing GUI Forms", "JDialog Form", "MyJDialog"); // NOI18N
    }

    /**
     * Creates new JFrame file in project
     * @return new file name
     */
    public String createJFrameFile() {
        return createFile(TEST_PROJECT_NAME, TEST_PACKAGE_NAME, "Swing GUI Forms", "JFrame Form", "MyJFrame"); // NOI18N
    }

    /**
     * Creates new AWT Frame file in project
     * @return new file name
     */
    public String createFrameFile() {
        return createFile(TEST_PROJECT_NAME, TEST_PACKAGE_NAME, "AWT GUI Forms", "Frame Form", "MyFrame"); // NOI18N
    }

    /**
     * Runs popoup command over node
     * @param popup command, ex.: "Add|Swing|Label"
     * @param node to run action on
     */
    public void runPopupOverNode(String actionName, Node node) {
        Action act = new Action(null, actionName);
        act.setComparator(new Operator.DefaultStringComparator(false, false));
        act.perform(node);
        // p(actionName);
    }

    /**
     * Runs popoup command over node using NoBlockAction
     * @param popup command, ex.: "Add|Swing|Label"
     * @param node to run action on
     */
    public void runNoBlockPopupOverNode(String actionName, Node node) {
        Action act = new ActionNoBlock(null, actionName);
        act.setComparator(new Operator.DefaultStringComparator(false, false));
        act.perform(node);
        // p(actionName);
    }

    /**
     * Runs popup commands over node
     * @param array list of popup commands
     * @param node to run actions on
     */
    public void runPopupOverNode(ArrayList<String> actionNames, Node node, Operator.DefaultStringComparator comparator) {
        for (String actionName : actionNames) {
            Action act = new Action(null, actionName);
            act.setComparator(comparator);
            act.perform(node);
            // p(actionName);
        }
    }

    /**
     * Runs popup commands over node
     * @param array list of popup commands
     * @param node to run actions on
     */
    public void runPopupOverNode(ArrayList<String> actionNames, Node node) {
        runPopupOverNode(actionNames, node, new Operator.DefaultStringComparator(false, false));
    }

    /**
     * Find a substring in a string
     * Test fail() method is called, when code string doesnt contain stringToFind.
     * @param stringToFind string to find
     * @param string to search
     */
    private void findStringInCode(String stringToFind, String code) {
        if (!code.contains(stringToFind)) {
            fail("Missing string \"" + stringToFind + "\" in code."); // NOI18N
        }
    }

    /**
     * Find a strings in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(ArrayList<String> lines, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();
        String code = editor.getText();

        for (String line : lines) {
            findStringInCode(line, code);
        }
        designer.design();
    }

    /**
     * Find a string in a code
     * @param lines array list of strings to find
     * @param designer operator "with text"
     */
    public void findInCode(String stringToFind, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();
        findStringInCode(stringToFind, editor.getText());
        designer.design();
    }

    /**
     * Miss a string in a code
     * Test fail() method is called, when code contains stringToFind string
     * @param stringToFind
     * @param designer operator "with text"
     */
    public void missInCode(String stringToFind, FormDesignerOperator designer) {
        EditorOperator editor = designer.editor();

        if (editor.getText().contains(stringToFind)) {
            fail("String \"" + stringToFind + "\" found in code."); // NOI18N
        }
        designer.design();
    }

    /**
     * Calls Jelly waitNoEvent()
     * @param quiet time (miliseconds)
     */
    public static void waitNoEvent(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }

    /**
     * Calls Jelly waitNoEvent() with MY_WAIT_MOMENT
     */
    public static void waitAMoment() {
        waitNoEvent(MY_WAIT_MOMENT);
    }


    /** Find msg string in file
     *
     * @result boolean
     */
    public static boolean findInFile(String msg, String filePath) {
        String content = getContents(new File(filePath));
        return content.indexOf(msg) != -1;
    }

/**
     * Fetch the entire contents of a text file, and return it in a String.
     * This style of implementation does not throw Exceptions to the caller.
     *
     * @param aFile is a file which already exists and can be read.
     */
    public static String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuffer contents = new StringBuffer();

        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            input = new BufferedReader(new FileReader(aFile));
            String line = null; //not declared within while loop
            /*
             * readLine is a bit quirky :
             * it returns the content of a line MINUS the newline.
             * it returns null only for the END of the stream.
             * it returns an empty String if two newlines appear in a row.
             */
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    //flush and close both "input" and its underlying FileReader
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return contents.toString();
    }

    /** Gets full path to file from "data" package from SampleProject
     * @param short file name with extension (e.g TestFrame.java);
     * @result full path to file (e.g /home/jirka/TestFrame.java)
     */
    public String getFilePathFromDataPackage(String fileName) {
        return getDataDir().getAbsolutePath()
                + File.separatorChar + TEST_PROJECT_NAME
                + File.separatorChar + "src" + File.separatorChar
                + TEST_PACKAGE_NAME + File.separatorChar + fileName; // NOI18N
    }
}

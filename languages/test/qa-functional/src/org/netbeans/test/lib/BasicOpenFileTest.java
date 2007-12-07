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

package org.netbeans.test.lib;

import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.actions.SystemAction;
import org.openide.actions.UndoAction;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
/**
 *
 * @author Jindrich Sedek
 */
public class BasicOpenFileTest extends JellyTestCase {

    private String projectName;
    private EditorOperator operator;
    private Project project;
    
    public BasicOpenFileTest(String str) {
        super(str);
    }

    protected Project openProject(String projectName) {
        this.projectName = projectName;
        File dataDir = getDataDir();
        File projectDir = new File(dataDir, projectName);
        project = (Project) ProjectSupport.openProject(projectDir);
        return project;
    }

    protected EditorOperator openFile(String fileName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Source Packages|files|" + fileName);
        node.select();
        node.performPopupAction("Open");
        operator = new EditorOperator(fileName);
        assertNotNull(operator.getText());
        assertTrue(operator.getText().length() > 0);
        return operator;
    }

    protected EditorOperator openStandaloneTokenFile(String fileName) throws Exception{
        File tokensDir = new File(getDataDir(), "tokens");
        File file = new File(tokensDir, fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(file));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        operator = new EditorOperator(fileName);
        return operator;
    }
    
    protected void edit(String insertion) throws Exception {
        operator.insert(insertion, 1, 1);
        assertTrue(operator.getText().contains(insertion));
        operator.save();
        assertTrue(operator.getText().contains(insertion));
        undo();
        assertFalse(operator.getText().contains(insertion));
    }

    protected void closeFile(){
        EditorOperator.closeDiscardAll();
    }
    
    protected void closeProject(){
        ProjectSupport.closeProject(projectName);
    }
    
    private void undo() throws Exception{
        final UndoAction ua = SystemAction.get(UndoAction.class);
        assertNotNull("Cannot obtain UndoAction", ua);
        while (ua.isEnabled()) {
            SwingUtilities.invokeAndWait(new Runnable(){
                public void run(){
                    ua.performAction();
                }
            });
        }
    }
}

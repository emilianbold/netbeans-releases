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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy.model.project;

import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.TimeoutExpiredException;

/**
 * Class for JSP files.
 */

public class JSPFile extends SourceFile implements ProjectEntry {

    private final static String reformatCode = "Reformat Code";
    
    WebPage webpage = null;
    
    private EditorOperator editor = null;
    
    /**
     * Parent of the jsp file.
     */
    protected ProjectEntry parent;
    
    /**
     * Child project entries of the jsp file.
     */
    protected List childList = new ArrayList();

    /**
     * Creates a new instance of web page.
     * @param path Path to web page in project.
     * @param name Name of web page.
     */
    JSPFile(TreePath path, String name, ProjectEntry parent) {
        super(path, name);
        setParent(parent);
    }
    
    /**
     * Save project entry.
     */
    public void save() {
    }
    
    /**
     * Save project entry as project entry with specified name.
     */
    public void saveAs(String name) {
    }
    
    /**
     * @return Name of the project entry.
     */
    public String getName() {
        return name;
    }

    /**
     * @return TreePath of the project entry.
     */
    public TreePath getTreePath() {
        return path;
    }
    
    /**
     * @return Parent of the jsp file.
     */
    public ProjectEntry getParent() {
        return parent;
    }
    
    /**
     * Set parent of the jsp file.
     */
    private void setParent(ProjectEntry parent) {
        this.parent = parent;
    }
    
    /**
     * @return Child project entries of the jsp file.
     */
    public ProjectEntry[] getChildren() {
        return ((ProjectEntry[]) childList.toArray(new ProjectEntry[childList.size()]));
    }
    
    /**
     * Remove project entry.
     */
    public void delete() {
    }
    
    /**
     * Open jsp file editor.
     */
    private void doOpenJSPEditor() {
        webpage.open();
        editor = webpage.page.getJSPEditor();
        editor.setVerification(false);
        editor.requestFocus();
    }
    
    /**
     * Open jsp code of web page.
     */
    private EditorOperator openJSP() {
        long tmpTimeout = 0;
        try {
            tmpTimeout = RaveWindowOperator.getDefaultRave().getTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
            RaveWindowOperator.getDefaultRave().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 5000);
            if (editor == null) {
                doOpenJSPEditor();
            }
            else {
                try {
                    editor.makeComponentVisible();
                }
                catch(Exception e) {throw new TimeoutExpiredException("");}
            }
        }
        catch (TimeoutExpiredException e) {
            doOpenJSPEditor();
        }
        finally {
            RaveWindowOperator.getDefaultRave().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", tmpTimeout);
        }
        return editor;
    }
    
    /**
     * Gets text from the currently opened jsp file of web page.
     * @return String representing whole content of the jsp file of web page.
     * (including new line characters)
     */
    public String getText() {
        editor = openJSP();
        return editor.getText();
    }

    /**
     * Gets text from specified line.
     * @param lineNumber Number of line.
     * @return String representing content of the line.
     */
    public String getText(int lineNumber) {
        editor = openJSP();
        return editor.getText(lineNumber);
    }

    /**
     * Checks if jsp file of web page contains text specified as parameter text.
     * @param text Text to compare to.
     * @return true if text was found, false otherwise.
     */
    public boolean contains(String text) {
        editor = openJSP();
        return editor.contains(text);
    }

    /**
     * Replaces first occurence of oldText by newText.
     * @param oldText Text to be replaced.
     * @param newText Text to write instead.
     */
    public void replace(String oldText, String newText) {
        editor = openJSP();
        editor.replace(oldText, newText);
    }

    /**
     * Replaced index-th occurence of oldText by newText.
     * @param oldText Text to be replaced
     * @param newText Text to write instead
     * @param index Index of oldText occurence.
     */
    public void replace(String oldText, String newText, int index) {
        editor = openJSP();
        editor.replace(oldText, newText, index);
    }

    /**
     * Insert text to current position. 
     * @param text String to be inserted.
     */
    public void insert(String text) {
        editor = openJSP();
        editor.insert(text);
    }

    /**
     * Inserts text to position specified by line number and column.
     * @param text String to be inserted.
     * @param lineNumber Number of line.
     * @param column Column position.
     */
    public void insert(String text, int lineNumber, int column) {
        editor = openJSP();
        editor.insert(text, lineNumber, column);
    }

    /**
     * Deletes given number of characters from specified possition.
     * @param offset Position inside document.
     * @param length Number of characters to be deleted.
     */
    public void delete(int offset, int length) {
        editor = openJSP();
        editor.delete(offset, length);
    }

    /**
     * Deletes given number of characters from current caret possition.
     * @param length Number of characters to be deleted.
     */
    public void delete(int length) {
        editor = openJSP();
        editor.delete(length);
    }

    /**
     * Delete specified line.
     * @param line Number of line.
     */
    public void deleteLine(int line) {
        editor = openJSP();
        editor.deleteLine(line);
    }

    /**
     * Deletes characters between column1 and column2 on the specified line.
     * @param lineNumber Number of line.
     * @param column1 Column position where to start deleting.
     * @param column2 Column position where to stop deleting.
     */
    public void delete(int lineNumber, int column1, int column2) {
        editor = openJSP();
        editor.delete(lineNumber, column1, column2);
    }
    
    /**
     * Pushs key.
     * @param keyCode Key code (KeyEvent.VK_* value)
     * @param modifiers Modifiers (combination of InputEvent.*_MASK fields)
     */
    public void pushKey(int keyCode, int modifiers) {
        editor = openJSP();
	editor.pushKey(keyCode, modifiers);
    }

    /**
     * Pushs key.
     * @param keyCode Key code (KeyEvent.VK_* value)
     */
    public void pushKey(int keyCode) {
	editor = openJSP();
	editor.pushKey(keyCode);
    }
    
    /**
     * Get position of caret.
     * @return int Position of caret.
     */
    public int getCaretPosition() {
	editor = openJSP();
	return editor.txtEditorPane().getCaretPosition();
    }
    
    /** 
     * Reformat code in JSP editor accordingly to specified rules.
     */
    public void reformatCode() {
	editor = openJSP();
	editor.txtEditorPane().clickForPopup();
        TestUtils.wait(1000);
        new JPopupMenuOperator().pushMenu(reformatCode);
        TestUtils.wait(1000);
    }
}

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

import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.JemmyException;

import javax.swing.tree.TreePath;

/**
 * Common class for all source files.
 */

abstract class SourceFile {

    private final String popupOpen = "Open";
    
    TreePath path;
    String name;

    /**
     * Creates a new instance of web page.
     * @param path Path to web page in project.
     * @param name Name of web page.
     */
    SourceFile(TreePath path, String name) {
        this.path = path;
        this.name = name;
    }
    
    /**
     * Open source file.
     */
    public void open() {
        try {
            ProjectNavigatorOperator prjNav = ProjectNavigatorOperator.showProjectNavigator();
            TestUtils.wait(500);
            String full_path = this.path.toString().substring(1, this.path.toString().length() - 1) + "|" + this.name;
            prjNav.pressPopupItemOnNode(full_path, popupOpen, new Operator.DefaultStringComparator(true, true));
        }
        catch(Exception e) {
            throw new JemmyException("Source file can't be opened!", e);
        }
        TestUtils.wait(500);
    }
    
    /**
     * Close source file.
     */
    public void close() {
    }
    
    /**
     * Gets text from the currently opened source file.
     * @return String representing whole content of the source fiole.
     * (including new line characters)
     */
    public abstract String getText();

    /**
     * Gets text from specified line.
     * @param lineNumber Number of line.
     * @return String representing content of the line.
     */
    public abstract String getText(int lineNumber);

    /**
     * Checks if source file contains text specified as parameter text.
     * @param text Text to compare to.
     * @return true if text was found, false otherwise.
     */
    public abstract boolean contains(String text);

    /**
     * Replaces first occurence of oldText by newText.
     * @param oldText Text to be replaced.
     * @param newText Text to write instead.
     */
    public abstract void replace(String oldText, String newText);

    /**
     * Replaced index-th occurence of oldText by newText.
     * @param oldText Text to be replaced
     * @param newText Text to write instead
     * @param index Index of oldText occurence.
     */
    public abstract void replace(String oldText, String newText, int index);

    /**
     * Insert text to current position. 
     * @param text String to be inserted.
     */
    public abstract void insert(String text);

    /**
     * Inserts text to position specified by line number and column.
     * @param text String to be inserted.
     * @param lineNumber Number of line.
     * @param column Column position.
     */
    public abstract void insert(String text, int lineNumber, int column);

    /**
     * Deletes given number of characters from specified possition.
     * @param offset Position inside document.
     * @param length Number of characters to be deleted.
     */
    public abstract void delete(int offset, int length);

    /**
     * Deletes given number of characters from current caret possition.
     * @param length Number of characters to be deleted.
     */
    public abstract void delete(int length);

    /**
     * Delete specified line.
     * @param line Number of line.
     */
    public abstract void deleteLine(int line);

    /**
     * Deletes characters between column1 and column2 on the specified line.
     * @param lineNumber Number of line.
     * @param column1 Column position where to start deleting.
     * @param column2 Column position where to stop deleting.
     */
    public abstract void delete(int lineNumber, int column1, int column2);
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring;

import java.lang.String;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.modules.test.refactoring.operators.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesClassOperator;
import org.netbeans.modules.test.refactoring.operators.FindUsagesResultOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class FindUsagesTestCase extends RefactoringTestCase {

    public static final String projectName = "RefactoringTest";
    private static final int SEARCH_IN_COMMENTS = 1 << 0;
    private static final int NOT_SEARCH_IN_COMMENTS = 1 << 1;
    private static final int FIND_USAGES = 1 << 2;
    private static final int FIND_DIRECT_SUBTYPES = 1 << 3;
    private static final int FIND_ALL_SUBTYPES = 1 << 4;
    private static final int FIND_OVERRIDING = 1 << 5;
    private static final int SEARCH_FROM_BASECLASS = 1 << 6;
    private static final int SEARCH_IN_ALL_PROJ = 1 << 7;
    private static final int SEARCH_ACTUAL_PROJ = 1 << 8;

    public FindUsagesTestCase(String name) {
        super(name);
    }

    public String getProjectName() {
        return projectName;
    }
        
    private void findUsages(String fileName, int row, int col, int modifiers) {
        openSourceFile("fu", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(row, col);
        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        if ((modifiers & SEARCH_IN_COMMENTS) != 0)
            findUsagesClassOperator.getSearchInComments().setSelected(true);
        if ((modifiers & NOT_SEARCH_IN_COMMENTS) != 0)
            findUsagesClassOperator.getSearchInComments().setSelected(false);
        if ((modifiers & FIND_USAGES) != 0)
            findUsagesClassOperator.getFindUsages().setSelected(true);
        if ((modifiers & FIND_ALL_SUBTYPES) != 0)
            findUsagesClassOperator.getFindAllSubtypes().setSelected(true);
        if ((modifiers & FIND_DIRECT_SUBTYPES) != 0)
            findUsagesClassOperator.getFindDirectSubtypes().setSelected(true);
        if ((modifiers & FIND_OVERRIDING) != 0)
            findUsagesClassOperator.getFindOverridding().setSelected(true);
        if ((modifiers & SEARCH_FROM_BASECLASS) != 0)
            findUsagesClassOperator.getFindFromBaseClass().setSelected(true);
        if ((modifiers & SEARCH_IN_ALL_PROJ) != 0)
            findUsagesClassOperator.setScope(null);
        if ((modifiers & SEARCH_ACTUAL_PROJ) != 0)
            findUsagesClassOperator.setScope(projectName);

        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        FindUsagesResultOperator test = new FindUsagesResultOperator();
        JTree tree = test.getPreviewTree();
        TreeModel model = tree.getModel();
        Object root = model.getRoot();
        System.out.println(root.getClass().getName());
        browseChildren(model, root, 0);
    }

    public void testFUClass() {
        findUsages("FindUsagesClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
    }
    
    public void testSearchInComments() {
        findUsages("SubtypeC", 13, 19, FIND_USAGES | SEARCH_IN_COMMENTS);
    }
    
    public void testFUDirectSubClass() {
        findUsages("FindSubtype", 11, 15, FIND_DIRECT_SUBTYPES | SEARCH_IN_COMMENTS);
    }
    
    public void testFUSubClass() {
        findUsages("FindSubtype", 11, 15, FIND_ALL_SUBTYPES | SEARCH_IN_COMMENTS);
    }
    
    public void testPersistence() {
        String fileName = "FindUsagesClass";
        openSourceFile("fu", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(12, 19);
        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        findUsagesClassOperator.getSearchInComments().setSelected(false);
        findUsagesClassOperator.getFindDirectSubtypes().setSelected(false);
        findUsagesClassOperator.setScope(null);
        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        FindUsagesResultOperator result = new FindUsagesResultOperator();
        new JButtonOperator(result.getRefresh()).pushNoBlock();
        findUsagesClassOperator = new FindUsagesClassOperator();
        ref(findUsagesClassOperator.getSearchInComments().isSelected());
        ref(findUsagesClassOperator.getFindDirectSubtypes().isSelected());
        ref(((JLabel)findUsagesClassOperator.getScope().getSelectedItem()).getText()+"\n");
        findUsagesClassOperator.getSearchInComments().setSelected(true);
        findUsagesClassOperator.getFindUsages().setSelected(true);
        findUsagesClassOperator.setScope(projectName);
        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        result = new FindUsagesResultOperator();
        new JButtonOperator(result.getRefresh()).pushNoBlock();
        findUsagesClassOperator = new FindUsagesClassOperator();
        ref(findUsagesClassOperator.getSearchInComments().isSelected());
        ref(findUsagesClassOperator.getFindUsages().isSelected());
        ref(((JLabel)findUsagesClassOperator.getScope().getSelectedItem()).getText()+"\n");                
    }
    
//    public void testCollapseTree() {
//        
//    }
//    
//    public void testShowLogical() {
//        
//    }
//    
//    public void testShowPhysical() {
//        
//    }
//    
//    public void testNextPrev() {
//        
//    }
//    
//    public void testOpenOnSelecting() {
//        
//    }
//        
//    public void testCancel() {
//        
//    }
//    
//    public void testTabNames() {
//        
//    }
    
    public static void main(String[] args) {        
        TestRunner.run(FindUsagesTestCase.class);
    }
}

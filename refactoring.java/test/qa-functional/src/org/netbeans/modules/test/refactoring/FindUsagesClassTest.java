/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesClassOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;

/**
 *
 * @author Jiri Prox
 */
public class FindUsagesClassTest extends FindUsagesTestCase{

    public FindUsagesClassTest(String name) {
        super(name);
    }
    
    public void testFUClass() {
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
    }

    public void testSearchInComments() {
        findUsages("fu","SubtypeC", 13, 19, FIND_USAGES | SEARCH_IN_COMMENTS);
    }

    public void testFUDirectSubClass() {
        findUsages("fu","FindSubtype", 11, 15, FIND_DIRECT_SUBTYPES | SEARCH_IN_COMMENTS);
    }

    public void testFUSubClass() {
        findUsages("fu","FindSubtype", 11, 15, FIND_ALL_SUBTYPES | SEARCH_IN_COMMENTS);
    }

    public void testPersistence() {
        String fileName = "FUClass";
        openSourceFile("fu", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(12, 19);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        findUsagesClassOperator.getSearchInComments().setSelected(false);
        findUsagesClassOperator.getFindDirectSubtypes().setSelected(false);
        findUsagesClassOperator.setScope(null);
        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        new JButtonOperator(result.getRefresh()).pushNoBlock();
        findUsagesClassOperator = new FindUsagesClassOperator();
        ref(findUsagesClassOperator.getSearchInComments().isSelected());
        ref(findUsagesClassOperator.getFindDirectSubtypes().isSelected());
        ref(((JLabel) findUsagesClassOperator.getScope().getSelectedItem()).getText() + "\n");
        findUsagesClassOperator.getSearchInComments().setSelected(true);
        findUsagesClassOperator.getFindUsages().setSelected(true);
        findUsagesClassOperator.setScope(projectName);
        findUsagesClassOperator.getFind().pushNoBlock();
        new EventTool().waitNoEvent(2000);
        result = RefactoringResultOperator.getFindUsagesResult();
        new JButtonOperator(result.getRefresh()).pushNoBlock();
        findUsagesClassOperator = new FindUsagesClassOperator();
        ref(findUsagesClassOperator.getSearchInComments().isSelected());
        ref(findUsagesClassOperator.getFindUsages().isSelected());
        ref(((JLabel) findUsagesClassOperator.getScope().getSelectedItem()).getText() + "\n");
        findUsagesClassOperator.getCancel().push();
        result.close();
    }

    public void testCollapseTree() {
        setBrowseChild(false);        
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);        
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        int rowCount = result.getPreviewTree().getRowCount();
        ref(rowCount);
        JToggleButtonOperator jtbo = new JToggleButtonOperator(result.getCollapse());
        jtbo.pushNoBlock();
        new EventTool().waitNoEvent(1000);
        rowCount = result.getPreviewTree().getRowCount();
        ref(rowCount);
        jtbo.pushNoBlock();
        new EventTool().waitNoEvent(1000);
        rowCount = result.getPreviewTree().getRowCount();
        ref(rowCount);
    }

    public void testShowLogical() {
        setBrowseChild(false);
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);       
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        JToggleButtonOperator jtbol = new JToggleButtonOperator(result.getLogical());
        JToggleButtonOperator jtbop = new JToggleButtonOperator(result.getPhysical());
        jtbol.pushNoBlock();
        new EventTool().waitNoEvent(1000);
        ref(jtbop.isSelected());
        JTree previewTree = result.getPreviewTree();
        browseChildren(previewTree.getModel(), previewTree.getModel().getRoot(), 0);
        jtbop.pushNoBlock();
        new EventTool().waitNoEvent(1000);
        ref(jtbop.isSelected());
        previewTree = result.getPreviewTree();
        browseChildren(previewTree.getModel(), previewTree.getModel().getRoot(), 0);
    }

    public void testNext() {        // Unstable, ordering can be different
        String fileName = "FindSubtype";
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        EditorOperator.closeDiscardAll();
        setBrowseChild(false);
        findUsages("fu","FindSubtype", 11, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);        
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        JButtonOperator next = new JButtonOperator(result.getNext());
        JTree preview = result.getPreviewTree();
        JTreeOperator jto = new JTreeOperator(preview);
        jto.selectRow(0);
        for (int i = 0; i < 5; i++) {
            next.push();
            int[] selectionRows = preview.getSelectionRows();            
            String file = getFileForSelectedNode(preview);
            EditorOperator edt = new EditorOperator(file);
            String txt = edt.txtEditorPane().getSelectionStart() + " " + edt.txtEditorPane().getSelectionEnd() + " " + edt.txtEditorPane().getSelectedText();
            if (map.get(file) == null) {
                map.put(file, new LinkedList<String>());
            }
            map.get(file).add(txt);
        }
        refMap(map);
    }

    public void testPrev() {        // Unstable, ordering can be different
        Map<String, List<String>> map = new HashMap<String, List<String>>();        
        EditorOperator.closeDiscardAll();
        setBrowseChild(false);
        findUsages("fu","FindSubtype", 11, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        JButtonOperator prev = new JButtonOperator(result.getPrev());
        JTree preview = result.getPreviewTree();
        JTreeOperator jto = new JTreeOperator(preview);
        jto.selectRow(0);
        for (int i = 0; i < 5; i++) {
            prev.push();
            int[] selectionRows = preview.getSelectionRows();            
            String file = getFileForSelectedNode(preview);
            EditorOperator edt = new EditorOperator(file);
            String txt = edt.txtEditorPane().getSelectionStart() + " " + edt.txtEditorPane().getSelectionEnd() + " " + edt.txtEditorPane().getSelectedText();
            if (map.get(file) == null) {
                map.put(file, new LinkedList<String>());
            }
            map.get(file).add(txt);
        }
        refMap(map);
    }

    public void testOpenOnSelecting() {
        setBrowseChild(false);
        findUsages("fu","FindSubtype", 11, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);                        
        RefactoringResultOperator result = RefactoringResultOperator.getFindUsagesResult();
        JTree previewTree = result.getPreviewTree();
        JTreeOperator jto = new JTreeOperator(previewTree);
        jto.selectRow(7);
        String file = getFileForSelectedNode(previewTree);
        TreePath selectionPath = jto.getSelectionPath();
        jto.clickOnPath(selectionPath, 2);
        EditorOperator editor2 = new EditorOperator(file);
    }

    public void testCancel() {
        setBrowseChild(false);
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);
        RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
        int tabCount = furo.getTabCount();
        EditorOperator editor = new EditorOperator("FUClass");
        editor.setCaretPosition(12, 19);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        findUsagesClassOperator.getCancel().push();
        assertEquals(furo.getTabCount(), tabCount);        
    }

    public void testTabNamesClass() {
        setBrowseChild(false);
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        findUsages("fu","FUClass", 12, 19, FIND_USAGES | NOT_SEARCH_IN_COMMENTS);
        setBrowseChild(true);
        RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
        JTabbedPane tabbedPane = furo.getTabbedPane();
        assertNotNull(tabbedPane);
        String title = tabbedPane.getTitleAt(tabbedPane.getTabCount()-1);
        ref(title+"\n");        
        
    }
    
    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(FindUsagesClassTest.class).enableModules(".*").clusters(".*"));
   }
}

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

import javax.swing.JTabbedPane;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.test.refactoring.actions.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesClassOperator;
import org.netbeans.modules.test.refactoring.operators.RefactoringResultOperator;

/**
 *
 * @author Jiri Prox 
 * @author Ralph Ruijs
 */
public class FindUsagesMethodTest extends FindUsagesTestCase {

    public FindUsagesMethodTest(String name) {
        super(name);
    }

    public void testFUMethod() {
        findUsages("fumethod", "Test", 6, 19, FIND_USAGES_METHOD | NOT_SEARCH_IN_COMMENTS | NOT_SEARCH_FROM_BASECLASS);
    }

    public void testFUMethodInComment() {
        findUsages("fumethod", "Test", 6, 19, FIND_USAGES_METHOD | SEARCH_IN_COMMENTS | NOT_SEARCH_FROM_BASECLASS);
    }

    public void testFUOverriding() {
        findUsages("fumethod", "Test", 6, 19, FIND_OVERRIDING | NOT_SEARCH_IN_COMMENTS | NOT_SEARCH_FROM_BASECLASS);
    }

    public void testFUFromBaseClass() {
        findUsages("fumethod", "Test", 6, 19, SEARCH_FROM_BASECLASS | NOT_SEARCH_IN_COMMENTS);
    }

    public void testFUAllOptions() {
        findUsages("fumethod", "Test", 6, 19, FIND_USAGES_METHOD | SEARCH_FROM_BASECLASS | FIND_OVERRIDING | NOT_SEARCH_IN_COMMENTS);
    }

    public void testNoOptions() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(6, 19);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = null;
        try {
            findUsagesClassOperator = new FindUsagesClassOperator();
            findUsagesClassOperator.getFindMethodUsage().setSelected(false);
            findUsagesClassOperator.getFindFromBaseClass().setSelected(false);
            new EventTool().waitNoEvent(500);
            assertEquals(false, findUsagesClassOperator.getFind().isEnabled());
        } finally {
            if (findUsagesClassOperator != null)
                findUsagesClassOperator.getCancel().push();
        }
    }

    public void testCheckboxavailable() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(10, 22);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = null;
        Timeouts timeouts = JemmyProperties.getCurrentTimeouts();
        long origTimeout = timeouts.getTimeout("ComponentOperator.WaitComponentTimeout");
        long currentTimeMillis = System.currentTimeMillis();
        try {
            findUsagesClassOperator = new FindUsagesClassOperator();
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", 3000);
            boolean found = true;
            try {
                findUsagesClassOperator.getFindFromBaseClass();
            } catch (TimeoutExpiredException tee) {
                found = false;
            }
            assertFalse("Check button is avaliable",found);
        } finally {
            System.out.println("Waited "+(System.currentTimeMillis()-currentTimeMillis));
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", origTimeout);
            if (findUsagesClassOperator != null)
                findUsagesClassOperator.getCancel().push();
        }
    }

    public void testCheckboxavailableStatic() {
        final String fileName = "Test";
        openSourceFile("fumethod", fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(14, 28);
        new FindUsagesAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = null;
        Timeouts timeouts = JemmyProperties.getCurrentTimeouts();
        long origTimeout = timeouts.getTimeout("ComponentOperator.WaitComponentTimeout");
        long currentTimeMillis = System.currentTimeMillis();
        try {
            findUsagesClassOperator = new FindUsagesClassOperator();
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", 3000);
            boolean foundOverriding = true;
            try {
                findUsagesClassOperator.getFindOverridding();
            } catch (TimeoutExpiredException tee) {
                foundOverriding = false;
            }
            assertFalse("Check button is avaliable",foundOverriding);
        } finally {
            System.out.println("Waited "+(System.currentTimeMillis()-currentTimeMillis));
            timeouts.setTimeout("ComponentOperator.WaitComponentTimeout", origTimeout);
            if (findUsagesClassOperator != null)
                findUsagesClassOperator.getCancel().push();
        }
    }

    public void testTabName() {
        setBrowseChild(false);
        findUsages("fumethod", "Test", 6, 19, FIND_USAGES_METHOD | NOT_SEARCH_IN_COMMENTS | NOT_SEARCH_FROM_BASECLASS);
        findUsages("fumethod", "Test", 6, 19, FIND_USAGES_METHOD | NOT_SEARCH_IN_COMMENTS | NOT_SEARCH_FROM_BASECLASS);
        setBrowseChild(true);
        RefactoringResultOperator furo = RefactoringResultOperator.getFindUsagesResult();
        JTabbedPane tabbedPane = furo.getTabbedPane();
        assertNotNull(tabbedPane);
        String title = tabbedPane.getTitleAt(tabbedPane.getTabCount()-1);
        ref(title+"\n");
        getRef().flush();
    }

    public void testFUConstructor() {                                
        findUsages("fumethod", "Test", 18, 13, FIND_USAGES_METHOD | NOT_SEARCH_IN_COMMENTS);
    }
    
    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(FindUsagesMethodTest.class).enableModules(".*").clusters(".*"));
   }
}

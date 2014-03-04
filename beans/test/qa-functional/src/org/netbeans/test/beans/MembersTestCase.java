/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.beans;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Stack;
import javax.swing.tree.TreeNode;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.beans.operators.AddProperty;
import org.netbeans.test.beans.operators.Navigator;

/**
 *
 * @author ssazonov
 */
public class MembersTestCase extends BeansTestCase {

    EditorOperator editor;

    public MembersTestCase(String testName) {
        super(testName);
    }

    //check whether navigator shows all components
    public void testA_A() throws Exception {
        try {
            editor = openEditor("members", "Test_A_A");

            new EventTool().waitNoEvent(2000);

            Navigator navigator = new Navigator();

            new EventTool().waitNoEvent(2000);

            navigator.setScopeToMember();

            new EventTool().waitNoEvent(2000);

            String comparison = navigator.compareTree(new String[]{
                "Test_A_A.java",
                "__Test_A_A",
                "____Test_A_A",
                "____getAl",
                "____getName",
                "____setAl",
                "____setName",
                "____al",
                "____name",
                "____C1",
                "______m2",
                "______ActionListenerImpl",
                "________ActionListenerImpl",
                "________actionPerformed",
                "______Annotation",
                "________array",
                "________value",
                "______I1",
                "________m1"});

            if (!comparison.equals("ok")) {
                fail(comparison);
            }

        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }

    //dynamic deletting - check navigator updating
    public void testA_B() throws Exception {
        try {
            editor = openEditor("members", "Test_A_A");
            editor.setCaretPosition(4, 1);

            new EventTool().waitNoEvent(2000);

            Navigator navigator = new Navigator();

            new EventTool().waitNoEvent(2000);

            navigator.setScopeToMember();

            new EventTool().waitNoEvent(2000);

            String comparison = navigator.compareTree(new String[]{
                "Test_A_A.java",
                "__Test_A_A",
                "____Test_A_A",
                "____getAl",
                "____getName",
                "____setAl",
                "____setName",
                "____al",
                "____name",
                "____C1",
                "______m2",
                "______ActionListenerImpl",
                "________ActionListenerImpl",
                "________actionPerformed",
                "______Annotation",
                "________array",
                "________value",
                "______I1",
                "________m1"});

            if (!comparison.equals("ok")) {
                fail(comparison);
            }

            editor.select(23, 33);
            editor.pushKey(KeyEvent.VK_BACK_SPACE);

            new EventTool().waitNoEvent(2000);

            comparison = navigator.compareTree(new String[]{
                "Test_A_A.java",
                "__Test_A_A",
                "____Test_A_A",
                "____getAl",
                "____getName",
                "____setAl",
                "____setName",
                "____al",
                "____name",
                "____C1",
                "______m2",
                "______Annotation",
                "________array",
                "________value",
                "______I1",
                "________m1"});

            if (!comparison.equals("ok")) {
                fail(comparison);
            }

        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }

    //compare selected path to predefined pattern
    public void testA_C() throws Exception {
        try {
            editor = openEditor("members", "Test_A_A");

            new EventTool().waitNoEvent(1000);

            Navigator navigator = new Navigator();

            new EventTool().waitNoEvent(1000);

            navigator.setScopeToMember();

            new EventTool().waitNoEvent(1000);

            editor.setCaretPosition(38, 1);
            new EventTool().waitNoEvent(1000);
            editor.insert("//");
            new EventTool().waitNoEvent(2000);

            String pattern = "[[Test_A_A.java, Test_A_A, C1, ActionListenerImpl, actionPerformed]]";
            String path = navigator.getSelectedPath();

            if (!path.equals(pattern)) {
                fail("path=\"" + path + "\", expected: \"" + pattern + "\"");
            }

        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }

    //compare selected path to predefined pattern
    public void testA_D() throws Exception {
        try {
            editor = openEditor("members", "Test_A_A");

            new EventTool().waitNoEvent(500);

            Navigator navigator = new Navigator();

            new EventTool().waitNoEvent(500);

            if (!navigator.clickTheNode("m2")) {
                fail("Exception while clicking on m2()");
            }
            
            int caretNumber = 26;
            if(editor.getLineNumber() != caretNumber){
                fail("Caret should be at line " + caretNumber);
            }

            new EventTool().waitNoEvent(5000);

        } finally {
            if (editor != null) {
                editor.closeDiscard();
            }
        }
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MembersTestCase.class)
                .enableModules(".*")
                .clusters(".*"));
    }
}

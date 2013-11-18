/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.cc;

import org.netbeans.modules.html.angular.GeneralAngular;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyProperties;

/**
 *
 * @author Vladimir Riha
 */
public class CompletionTest extends GeneralAngular {

    public CompletionTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CompletionTest.class).addTest(
                        "openProject",
                        "testExpression12",
                        "testExpression13",
                        "testExpression17",
                        "testMatchingCCExpression",
                        "testAttribute30",
                        "testAttribute32",
                        "testAttribute33"
                ).enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void openProject() throws Exception {
        startTest();
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects("simpleProject");
        evt.waitNoEvent(2000);
        openFile("index.html", "simpleProject");
        endTest();
    }

    public void testExpression12() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 12);
        endTest();
    }

    public void testExpression13() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 13);
        endTest();
    }

    public void testExpression17() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 17);
        endTest();
    }

    public void testAttribute30() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 30);
        endTest();
    }

    public void testAttribute32() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 32);
        endTest();
    }

    public void testAttribute33() throws Exception {
        startTest();
        testCompletion(new EditorOperator("index.html"), 33);
        endTest();
    }

    public void testMatchingCCExpression() {
        startTest();
        EditorOperator eo = new EditorOperator("index.html");
        eo.setCaretPosition(18, 63);
        eo.typeKey(' ', InputEvent.CTRL_MASK);
        evt.waitNoEvent(500);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, new String[]{"cssColor", "clearContact"});
        checkCompletionDoesntContainItems(cjo, new String[]{"name", "alert"});
        endTest();
    }

    public void testCompletion(EditorOperator eo, int lineNumber) throws Exception {
        waitScanFinished();
        String rawLine = eo.getText(lineNumber);
        int start = rawLine.indexOf("<!--cc;");
        String rawConfig = rawLine.substring(start + 2);
        String[] config = rawConfig.split(";");
        eo.setCaretPosition(lineNumber, Integer.parseInt(config[1]));
        type(eo, config[2]);
        eo.pressKey(KeyEvent.VK_ESCAPE);
        int back = Integer.parseInt(config[3]);
        for (int i = 0; i < back; i++) {
            eo.pressKey(KeyEvent.VK_LEFT);
        }

        eo.typeKey(' ', InputEvent.CTRL_MASK);
        CompletionInfo completion = getCompletion();
        CompletionJListOperator cjo = completion.listItself;
        checkCompletionItems(cjo, config[4].split(","));
        completion.listItself.hideAll();

        if (config[5].length() > 0) {
            String prefix = Character.toString(config[5].charAt(0));
            type(eo, prefix);
            eo.typeKey(' ', InputEvent.CTRL_MASK);
            completion = getCompletion();
            cjo = completion.listItself;
            checkCompletionMatchesPrefix(cjo.getCompletionItems(), prefix);
            evt.waitNoEvent(500);
            cjo.clickOnItem(config[5]);
            eo.pressKey(KeyEvent.VK_ENTER);
            assertTrue("Wrong completion result", eo.getText(lineNumber).contains(config[6].replaceAll("|", "")));
            completion.listItself.hideAll();
        }

        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        String l = eo.getText(eo.getLineNumber());
        for (int i = 0; i < l.length() - 1; i++) {
            eo.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.test.syntax;

import java.awt.event.KeyEvent;
import java.io.File;
import junit.framework.Test;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jindrich Sedek
 */
public class IndentationTest extends CompletionTest {

    private static final String[] START_STEPS = {"<!--CC", "/*CC"};
    private static final String[] END_STEPS = {"-->", "*/"};
    private boolean debugMode = false;

    /** Creates a new instance of IndentationTest */
    public IndentationTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
    }

    public static Test suite() {
        // find folder with test projects and define file objects filter
        File datadir = new IndentationTest(null, null).getDataDir();
        File projectsDir = new File(datadir, "IndentationTestProjects");
        FileObjectFilter filter = new FileObjectFilter() {

                    public boolean accept(FileObject fObject) {
                        String ext = fObject.getExt();
                        String name = fObject.getName();
                return name.startsWith("test") && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext));
                    }
                };
        return RecurrentSuiteFactory.createSuite(IndentationTest.class, projectsDir, filter);
    }

    @Override
    public void runTest() throws Exception {
        try {
            BaseDocument doc = openFile(testFileObj);
            String text = doc.getText(0, doc.getLength());
            Possition actualPossition = getNextPossition(text, 0);
            EditorOperator eOperator;
            while (actualPossition != null) {//go through all cases
                doc.remove(actualPossition.start, actualPossition.len);
                eOperator = new EditorOperator(testFileObj.getName());
                eOperator.txtEditorPane().getCaret().setDot(actualPossition.start);
                eOperator.save();
                eOperator.pushKey(KeyEvent.VK_ENTER);
                eOperator.waitModified(true);
                int shift = eOperator.txtEditorPane().getCaretPosition() - actualPossition.start;
                ref("line " + eOperator.getLineNumber() + ": " + shift);
                if (debugMode) {
                    doc.insertString(eOperator.txtEditorPane().getCaretPosition(), "|", null);
                    Thread.sleep(2000);
                }
                actualPossition = getNextPossition(eOperator.getText(), actualPossition.start + 1);
            }
            if (debugMode) {
                ref(new EditorOperator(testFileObj.getName()).getText());
            }
        } catch (Exception ex) {
            throw new AssertionFailedErrorException(ex);
        }
        ending();
    }

    private Possition getNextPossition(String text, int actual) {
        int minStart = Integer.MAX_VALUE,
         len = -1;
        for (int i = 0; i < START_STEPS.length; i++) {
            int pos = text.indexOf(START_STEPS[i], actual);
            if ((pos != -1) && (pos < minStart)) {
                minStart = pos;
                int minEnd = text.indexOf(END_STEPS[i], actual);
                len = minEnd - minStart + END_STEPS[i].length();
            }
        }
        if (minStart != Integer.MAX_VALUE) {
            return new Possition(minStart, len);
        } else {
            return null;
    }
    }

    private class Possition {

        public int start,  len;

        Possition(int start, int len) {
            this.start = start;
            this.len = len;
        }

        @Override
        public String toString() {
            return "Od " + start + " a delky " + len;
        }
    }
}

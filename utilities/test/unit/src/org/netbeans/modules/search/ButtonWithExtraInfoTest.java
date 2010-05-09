/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvg
 */
public class ButtonWithExtraInfoTest {

    private ArrayList<TestSpec> testSpecList = new ArrayList<TestSpec>();

    @Before
    public void setUp() {
        addTestCase("aaa", "123456789",
                    "aaa ( 123456789 )",
                    "aaa ( 123456789 )");
        addTestCase("aaa", "1234567890",
                    "aaa ( 1234567890 )",
                    "aaa ( 1234567890 )");
        addTestCase("aaa", "12345678901",
                    "aaa ( 12345678901 )",
                    "aaa ( 12345678901 )");
        addTestCase("aaa", "123456789012",
                    "aaa ( 123456789012 )",
                    "aaa ( 123456789012 )");
        addTestCase("aaa", "1234567890123",
                    "aaa ( 1234567890123 )",
                    "aaa ( 1234567890123 )");
        addTestCase("aaa", "12345678901234",
                    "aaa ( 1234567890... )",
                    "aaa ( 12345678901234 )");
        addTestCase("aaa", "123456789012345",
                    "aaa ( 1234567890... )",
                    "aaa ( 123456789012345 )");
        addTestCase("aaa", null,
                    "aaa",
                    null);
    }

    /**
     * Test of setText method, of class ButtonWithExtraInfo.
     */
    @Test
    public void testSetText() {
        for(TestSpec ts: testSpecList) {
            ButtonWithExtraInfo button = new ButtonWithExtraInfo(ts.extraText);
            button.setText(ts.inputText);
            assertEquals(ts.outputText, button.getText());
            assertEquals(ts.toolTipText, button.getToolTipText());
        }
    }
    
    private void addTestCase(String inputText, String extraText, 
                        String outputText, String toolTipText) {
        testSpecList.add(new TestSpec(inputText, extraText, 
                                      outputText, toolTipText));
        
    }

    private class TestSpec {
        private String inputText;
        private String extraText;
        private String outputText;
        private String toolTipText;

        public TestSpec(String inputText, String extraText,
                        String outputText, String toolTipText) {
            this.inputText = inputText;
            this.extraText = extraText;
            this.outputText = outputText;
            this.toolTipText = toolTipText;
        }

    } // TestSpec

}
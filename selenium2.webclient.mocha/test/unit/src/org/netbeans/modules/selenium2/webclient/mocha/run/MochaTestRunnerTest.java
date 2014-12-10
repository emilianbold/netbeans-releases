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
package org.netbeans.modules.selenium2.webclient.mocha.run;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Theofanis Oikonomou
 */
public class MochaTestRunnerTest extends TestCase {
    
    public MochaTestRunnerTest() {
    }
    
    @Test
    public void testFilePatternRegex() throws Exception {
        Pattern pattern = MochaTestRunner.CallStackCallback.FILE_LINE_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29", "/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1425", "29"},
            {"at webdriver.promise.ControlFlow.runInNewFrame_ (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1654:20)", "/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1654", "20"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("FILE"));
            assertEquals(matchingStrings[i][2], matcher.group("LINE"));
            assertEquals(matchingStrings[i][3], matcher.group("COLUMN"));
        }
    }
    
    @Test
    public void testOKRegex() throws Exception {
        Pattern pattern = MochaTestRunner.OK_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"ok 1 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=1234", "1", "Google Search should append query to title", "Google Search", "should append query to title", "1234"},
//            {"not ok 1 Google Search should append query to title", "1", "Google Search should append query to title"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][2], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][3], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][4], matcher.group("TESTCASE"));
            assertEquals(matchingStrings[i][5], matcher.group("DURATION"));
        }
    }
    
    @Test
    public void testOKSkipRegex() throws Exception {
        Pattern pattern = MochaTestRunner.OK_SKIP_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"ok 1 Google Search should append query to title # SKIP -, suite=Google Search, testcase=should append query to title", "1", "Google Search should append query to title", "Google Search", "should append query to title"}
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][2], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][3], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][4], matcher.group("TESTCASE"));
        }
    }
    
    @Test
    public void testNotOKRegex() throws Exception {
        Pattern pattern = MochaTestRunner.NOT_OK_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"not ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=2345", "2", "Google Search should append query to title", "Google Search", "should append query to title", "2345"},
//            {"ok 1 Google Search should append query to title", "1", "Google Search should append query to title"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][2], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][3], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][4], matcher.group("TESTCASE"));
            assertEquals(matchingStrings[i][5], matcher.group("DURATION"));
        }
    }
    
    @Test
    public void testSessionStartRegex() throws Exception {
        Pattern pattern = MochaTestRunner.SESSION_START_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"1..8", "8"},
//            {"ok 1 Google Search should append query to title", "1", "Google Search should append query to title"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("TOTAL"));
        }
    }
    
    @Test
    public void testSessionEndRegex() throws Exception {
        Pattern pattern = MochaTestRunner.SESSION_END_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"tests 8, pass 6, fail 2, skip 1", "8", "6", "2", "1"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("TOTAL"));
            assertEquals(matchingStrings[i][2], matcher.group("PASS"));
            assertEquals(matchingStrings[i][3], matcher.group("FAIL"));
            assertEquals(matchingStrings[i][4], matcher.group("SKIP"));
        }
    }
}

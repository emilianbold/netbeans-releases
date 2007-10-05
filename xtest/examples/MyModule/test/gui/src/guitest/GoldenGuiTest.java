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

package guitest;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;


public class GoldenGuiTest extends JellyTestCase {

    public GoldenGuiTest(String testName) {
        super(testName);
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenGuiTest.class));
    }
    
    public void setUp() {
        /*
         * No special setup needed. JellyTestCase do following initialization:
         *  - Jemmy/jelly output is redirected to jemmy.log file in JUnit working directory
         *  - if an exception is thrown during test execution, screen shot is taken
         *  - all modal dialogs are closed
         *  - wait at least 1000 ms between test cases
         *  - dump xml hierarchy of all components (disabled by default)
         */
    }
    
    /* Simple gui test using golden files. It checks properties of HTTP Server node
     * in the runtime window.
     * If everything works well, contents of golden file is compared at the end
     * with output collected by ref PrintStream into a file. If files differ, test
     * is failed, otherwise it is passed.
     * If something goes wrong, exception is thrown from Jemmy, caught and reported.
     * You can also use assert*() and fail() methods to indicate a failure within the test.
     */
    public void testPart1() throws Exception {
        Node rootNode = RuntimeTabOperator.invoke().getRootNode();
        Node httpNode = new Node(rootNode, "HTTP Server");
        new PropertiesAction().perform(httpNode);
        PropertySheetOperator pso = new PropertySheetOperator("HTTP Server");
        Property p = new Property(pso, "Hosts With Granted Access");
        String value = p.getValue();
        pso.close();
        // write to ref PrintStream to compare with golden file
        ref(value);
        // compare outpup written to ref PrintStream with contents of golden file
        // (data/goldenfiles/guitest/GoldenGuiTest/testPart1.pass)
        compareReferenceFiles();
    }
    
}

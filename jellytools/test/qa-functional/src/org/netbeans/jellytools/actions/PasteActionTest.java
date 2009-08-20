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

package org.netbeans.jellytools.actions;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.PasteAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class PasteActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PasteActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        /*
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PasteActionTest("testPerformPopup"));
        suite.addTest(new PasteActionTest("testPerformMenu"));
        suite.addTest(new PasteActionTest("testPerformAPI"));
        suite.addTest(new PasteActionTest("testPerformShortcut"));
        return suite;
         */
        return createModuleTest(PasteActionTest.class);
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    // "Confirm Object Deletion"
    private static final String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
    private static Node sample1Node;
    private static final String SAMPLE_FILE = "properties.properties";  //NOI18N
    private static final String PASTED_FILE = "properties_1.properties";  //NOI18N
    
    public void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");  // NOI18N
        openDataProjects("SampleProject");
        if(sample1Node == null) {
            sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        }
        new CopyAction().perform(new Node(sample1Node, SAMPLE_FILE));
    }
    
    public void tearDown() {
        Node pastedNode = new Node(sample1Node, PASTED_FILE);
        new DeleteAction().perform(pastedNode); 
        new NbDialogOperator(confirmTitle).yes();
        pastedNode.waitNotPresent();
    }

    /** Test performPopup  */
    public void testPerformPopup() {
        new PasteAction().performPopup(sample1Node);
    }
    
    /** Test performMenu  */
    public void testPerformMenu() {
        new PasteAction().performMenu(sample1Node);
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        new PasteAction().performAPI(sample1Node);
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new PasteAction().performShortcut(sample1Node);
    }
}

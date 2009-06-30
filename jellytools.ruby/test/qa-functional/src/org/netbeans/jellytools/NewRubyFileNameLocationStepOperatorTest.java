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
package org.netbeans.jellytools;

import java.awt.Component;
import java.io.IOException;
import javax.swing.JPanel;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.MainWindowOperator.StatusTextTracer;
import org.netbeans.jellytools.actions.Action;

/**
 * Test of org.netbeans.jellytools.NewFileNameLocationStepOperator.
 * @author tb115823
 */
public class NewRubyFileNameLocationStepOperatorTest extends JellyTestCase {

    public static NewRubyFileNameLocationStepOperator op;

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static String[] tests = new String[] {
        "testInvoke", "testComponents"};
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {        
        return createModuleTest(NewRubyFileNameLocationStepOperatorTest.class, tests);
    }
    
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");        
        

    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewRubyFileNameLocationStepOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() throws Exception {

        openDataProjects("SampleRubyProject");

        NewFileWizardOperator wop = NewFileWizardOperator.invoke();
        wop.selectProject("SampleRubyProject"); //NOI18N

        
        // Ruby
        String rubyLabel = "Ruby"; //TODO: find appropriate bundle and load messages from it
        // Ruby File
        String rubyFileLabel = "Ruby File"; //TODO: find appropriate bundle and load messages from it
        wop.selectCategory(rubyLabel);
        wop.selectFileType(rubyFileLabel);
        wop.next();
        op = new NewRubyFileNameLocationStepOperator();
    }
    
    public void testComponents() {        
        op.txtObjectName().setText("NewObject"); // NOI18N
        assertEquals("Project name not propagated from previous step", "SampleRubyProject", op.txtProject().getText()); // NOI18N        
        op.selectSourceFilesLocation();
        
        String filePath = op.txtCreatedFile().getText();
        assertTrue("Created file path doesn't contain SampleProject.", filePath.indexOf("SampleRubyProject") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain lib folder name.", filePath.indexOf("lib") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain NewObject name.", filePath.indexOf("NewObject") > 0);  //NOI18N
        op.cancel();         
    }
    
}

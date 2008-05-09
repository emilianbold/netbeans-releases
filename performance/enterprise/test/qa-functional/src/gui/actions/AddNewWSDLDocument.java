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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package gui.actions;

import gui.EPUtilities;

import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test Add New WSDL Document
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class AddNewWSDLDocument extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewFileNameLocationStepOperator location;
    
    private int index;
    
    /**
     * Creates a new instance of AddNewWSDLDocument
     * @param testName the name of the test
     */
    public AddNewWSDLDocument(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewWSDLDocument
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewWSDLDocument(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void initialize(){
        index=1;
//        new CloseAllDocumentsAction().performAPI();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
        new EventTool().waitNoEvent(1000);
    }
    
    public void prepare(){
        EPUtilities.getProcessFilesNode("BPELTestProject").select();
        
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory("XML"); //NOI18N
        wizard.selectFileType("WSDL Document"); //NOI18N
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText("WSDLDoc_"+System.currentTimeMillis());
    }
    
    public ComponentOperator open(){
        location.finish();
        return null;
    }
    
    public void close(){
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddNewWSDLDocument("measureTime"));
    }
}

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

package org.netbeans.modules.ws.qaf.designer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;
import org.netbeans.modules.ws.qaf.designer.operators.WsDesignerOperator;

/**
 *
 * @author lukas
 */
public class WebServiceDesignerTest extends WebServicesTestBase {

    public WebServiceDesignerTest(String name) {
        super(name);
    }
    
    @Override
    protected String getProjectName() {
        return "60_webapp"; //NOI18N
    }
    
    public void testAddOperation() {
        String wsName = "EmptyWs";
        openFileInEditor(wsName);
        WsDesignerOperator wdo = new WsDesignerOperator(wsName);
        wdo.addOperation();
        
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_OperationAction");
        NbDialogOperator dialog = new NbDialogOperator(actionName);
        new JTextFieldOperator(dialog, 2).setText("test1");
        new JTextFieldOperator(dialog, 1).setText("String");
        dialog.ok();
//        eo.save();
//        waitForTextInEditor(eo, opName);
    }
    
    public void testRemoveOperation() {
        String wsName = "EmptyWs";
        openFileInEditor(wsName);
        WsDesignerOperator wdo = new WsDesignerOperator(wsName);
        wdo.design();
        wdo.selectOperation("test1");
        wdo.removeOperation();
        NbDialogOperator ndo = new NbDialogOperator("Question");
        ndo.yes();
    }
    
    private void openFileInEditor(String fileName) {
        //XXX:
        //there's some weird bug:
        //if project with webservices is checked out from VCS (cvs)
        //and its class is opened in the editor then there's no
        //web service designer or it is not initialized correctly :(
        Node wsNode = new Node(getProjectRootNode(), "Web Services");
        if (wsNode.isCollapsed()) {
            wsNode.expand();
        }
        //end
        SourcePackagesNode spn = new SourcePackagesNode(getProjectRootNode());
        Node n = new Node(spn, "samples|" + fileName);
        new OpenAction().perform(n);
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new WebServiceDesignerTest("testAddOperation"));
        suite.addTest(new WebServiceDesignerTest("testRemoveOperation"));
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
}

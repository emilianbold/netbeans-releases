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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;

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
        String wsName = "EmptyWs"; //NOI18N
        openFileInEditor(wsName);
        assertEquals(0, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.invokeAddOperation(wsName);
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.webservices.action.Bundle", "LBL_OperationAction");
        NbDialogOperator dialog = new NbDialogOperator(actionName);
        new JTextFieldOperator(dialog, 2).setText("test1"); //NOI18N
        new JTextFieldOperator(dialog, 1).setText("String"); //NOI18N
        dialog.ok();
        try {
            //slow down a bit
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        assertEquals(1, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertTrue(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
        assertTrue(eo.contains("public String test1() {")); //NOI18N
        assertTrue(eo.contains("import javax.jws.WebMethod;")); //NOI18N
    }
    
    public void testRemoveOperation() {
        String wsName = "EmptyWs"; //NOI18N
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeRemoveOperation(wsName, "test1"); //NOI18N
        NbDialogOperator ndo = new NbDialogOperator("Question"); //NOI18N
        ndo.yes();
        assertEquals(0, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertFalse(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
        assertFalse(eo.contains("public String test1() {")); //NOI18N
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
        Node n = new Node(spn, "samples|" + fileName); //NOI18N
        new OpenAction().perform(n);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(
                NbModuleSuite.createConfiguration(WebServiceDesignerTest.class),
                "testAddOperation", //NOI18N
                "testRemoveOperation").enableModules(".*").clusters(".*")); //NOI18N
    }
    
}

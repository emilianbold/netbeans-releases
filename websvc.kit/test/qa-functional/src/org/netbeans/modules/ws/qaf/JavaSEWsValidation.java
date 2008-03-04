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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf;

import java.io.IOException;
import javax.swing.ListModel;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.ws.qaf.WebServicesTestBase.ProjectType;

/**
 *
 * @author jp154641
 */
public class JavaSEWsValidation extends WsValidation {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public JavaSEWsValidation(String name) {
        super(name);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.JAVASE_APPLICATION;
    }

    @Override
    protected String getWsClientProjectName() {
        return "WsClientInJavaSE"; //NOI18N
    }

    @Override
    protected String getWsClientPackage() {
        return "o.n.m.ws.qaf.client.j2se"; //NOI18N
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static TestSuite suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new JavaSEWsValidation("testCreateWsClient")); //NOI18N   
        suite.addTest(new JavaSEWsValidation("testCallWsOperationInJavaMainClass")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testFixClientLibraries")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testWsClientHandlers")); //NOI18N
//        suite.addTest(new EjbWsValidation("testRefreshClient")); //NOI18N
        suite.addTest(new JavaSEWsValidation("testRunWsClientProject")); //NOI18N
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    public void testCallWsOperationInJavaMainClass() {
        final EditorOperator eo = new EditorOperator("Main.java"); //NOI18N
        eo.select("// TODO code application logic here"); //NOI18N
        callWsOperation(eo, "myIntMethod", 18); //NOI18N
        assertTrue("Web service lookup class has not been found", eo.contains(getWsClientLookupCall())); //NOI18N
        assertFalse("@WebServiceRef present", eo.contains("@WebServiceRef")); //NOI18N
    }

    /**
     *  Fix for actual issue 123961, that causes JAX-WS library isn't added into project when adding
     *  JAX-WS Web Service Client
     */
    public void testFixClientLibraries() {
        Node libraries = new Node(getProjectRootNode(), "Libraries"); //NOI18N
        libraries.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_AddLibrary_Action")); //NOI18N
        NbDialogOperator add = new NbDialogOperator("Add Library"); //NOI18N
        JListOperator libs = new JListOperator(add, 0);
        ListModel mLibs = libs.getModel();
        int iCount = mLibs.getSize();
        int libPosition = -1;
        String item = ""; //NOI18N
        for (int i = 0; i < iCount; i++) {
            item = mLibs.getElementAt(i).toString();
            if (item.contains("jaxws21")) { //NOI18N
                libPosition = i;
            }
        }
        if (libPosition > -1) {
            libs.selectItem(libPosition);
            new JButtonOperator(add, "Add Library").push(); //NOI18N
        } else {
            fail("########  JAX-WS 2.1 library not found in the list  #######"); //NOI18N

        }
    }

    /**
     * Since there's not Deploy action for Java Projects, it's Run insteda and output checked
     * @throws java.io.IOException
     */
    public void testRunWsClientProject() throws IOException {
        runProject(getProjectName());
        OutputTabOperator oto = new OutputTabOperator(getProjectName());
        assertTrue(oto.getText().indexOf("Result = []") > -1); //NOI18N
        assertTrue(oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
    }
}

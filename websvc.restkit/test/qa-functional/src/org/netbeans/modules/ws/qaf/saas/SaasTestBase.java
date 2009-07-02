/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ws.qaf.saas;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.PasteAction;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.ws.qaf.rest.RestTestBase;

/**
 *
 * @author lukas
 */
public abstract class SaasTestBase extends RestTestBase {

    public SaasTestBase(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return getSaasServiceID() + "Prj";
    }

    //used as a name prefix for new files
    protected abstract String getSaasServiceID();

    // path|to|node excluding the top level one (Web Services)
    protected abstract String getSaasServiceNodePath();

    public void testJavaDrop() {
        javaDrop();
    }
    
    public void testServletDrop() {
        servletDrop();
    }
    
    public void testJspDrop() {
        jspDrop();
    }
    
    public void testRestDrop() {
        resourceDrop();
    }
    
    //drop into new method
    protected void javaDrop() {
        String javaClassName = getSaasServiceID() + "_Java";
        //Create new Java class
        //Java
        String javaAppLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "Templates/Classes"); //NOI18N
        //Java Class
        String javaFileLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java"); //NOI18N
        createNewFile(getProject(), javaAppLabel, javaFileLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(javaClassName);
        op.setPackage("org.my"); //NOI18N
        op.finish();

        //find editor tab
        EditorOperator eo = new EditorOperator(javaClassName + ".java");
        createMethod(eo);
        drop(getSaasServiceNodePath(), eo);
    }

    //drop before </body> element
    protected void jspDrop() {
        //create new JSP
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //JSP
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/JSP.jsp");
        createNewFile(getProject(), webLabel, servletLabel);
        NewJspFileNameStepOperator op = new NewJspFileNameStepOperator();
        op.setJSPFileName(getSaasServiceID());
        op.finish();
        //find editor tab
        EditorOperator eo = new EditorOperator(getSaasServiceID() + ".jsp");
        //</body>
        eo.setCaretPosition("</body>", true);
        eo.insert("\n");
        eo.setCaretPositionToLine(eo.getLineNumber() - 1);
        drop(getSaasServiceNodePath(), eo);
    }

    //drop into new method
    protected void servletDrop() {
        String servletName = getSaasServiceID() + "_Srv";
        //create a servlet
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //Servlet
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/Servlet.java");
        createNewFile(getProject(), webLabel, servletLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.txtObjectName().clearText();
        op.txtObjectName().typeText(servletName);
        JComboBoxOperator jcbo = new JComboBoxOperator(op, 1);
        jcbo.typeText("org.my"); //NOI18N
        op.finish();
        //find editor tab
        EditorOperator eo = new EditorOperator(servletName + ".java");
        createMethod(eo);
        drop(getSaasServiceNodePath(), eo);
    }

    //always drop at the end of the file
    protected void resourceDrop() {
        //RESTful Web Services from Patterns
        String patternsTypeName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestServicesFromPatterns");
        createNewWSFile(getProject(), patternsTypeName);
        //stick only with Singlton resource for now
        WizardOperator wo = new WizardOperator(patternsTypeName);
        wo.next();
        //set resource package
        JComboBoxOperator jcbo = new JComboBoxOperator(wo, 1);
        jcbo.clearText();
        jcbo.typeText("org.my");
        //set resource name
        JTextFieldOperator jtfo = new JTextFieldOperator(wo, 4);
        jtfo.clearText();
        jtfo.typeText(getSaasServiceID()); //NOI18N
        //resource class name is set to <>Resource
        wo.finish();
        String progressDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RestServicesFromPatternsProgress");
        waitDialogClosed(progressDialogTitle);
        //find editor tab
        EditorOperator eo = new EditorOperator(getSaasServiceID() + "Resource.java");
        eo.setCaretPosition("{", false);
        eo.insert("\n");
        drop(getSaasServiceNodePath(), eo);
    }

    /**
     * @param node path to the saas service (in form of groupName|serviceName|opName)
     * @param fileName name of the target file (must be opened in the editor)
     */
    //TODO: add a callback argument through which one will be able to interact
    //      with a popup dialog which will be shown if the service will have
    //      some "options" to configure and expose it to subclasses (through
    //      abstract method defined here)
    protected void drop(String node, EditorOperator eo) {
        //XXX-this should not be required!!!!!!!!!!!!!
        eo.save();
        //find a particular saas service operation node in services tab
        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        Node wsNode = new Node(rto.getRootNode(), "Web Services|" + node);
        //invoke Copy action on it
        new CopyAction().performAPI(wsNode);
        //XXX-this should not be required!!!!!!!!!!!!!
        //wait for the end of the classpath compilation (just in case)
        ProjectSupport.waitScanFinished();
        //sleep for a while, give other threads time to finish (just to be sure)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
        }
        //invoke paste method in eo
        new PasteAction().performPopup(eo);
    }

    //create new method in some class ()
    protected void createMethod(EditorOperator eo) {
        //set caret position at the beginning of the class and create some
        //dummy method there
        eo.setCaretPosition("{", false);
        eo.insert("\n");
        eo.insert("public void " + getSaasServiceID() + "() {\n");
        eo.insert("\n");
        eo.insert("}\n");
        eo.setCaretPosition(eo.getLineNumber() - 2);
    }
}

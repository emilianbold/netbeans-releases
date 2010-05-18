/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.actions;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.*;
import org.netbeans.modules.jmx.actions.dialog.RegisterMBeanPanel;
import org.openide.cookies.EditorCookie;
import org.openide.text.IndentEngine;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.jmx.common.WizardHelpers;

/**
 * Action used to add MBean registration code to an existing JMX Agent.
 * @author jfdenise
 */
public class RegisterMBeanAction extends NodeAction {
    
    private FileObject fo;
    private DataObject dob;
    private static String TRY = "try { // Register MBean in Platform MBeanServer\n"; // NOI18N
    private static String CATCH = "\n}catch(JMException ex) {\n"+ // NOI18N
            "// TODO handle exception\n" + // NOI18N
            "}"; // NOI18N
    private static final String REGISTRATION_1 = TRY + "ManagementFactory.getPlatformMBeanServer().\n"; // NOI18N
    private static final String REGISTRATION_2 = "registerMBean("; // NOI18N
    private static final String REGISTRATION_3 = ",\nnew ObjectName(\"{0}\"));";// NOI18N
    private static final String REGISTRATION_4 = CATCH;
    private static final String CONSTRUCTOR_COMMENTS = "// TODO Replace {0} Constructor parameters with valid values\n";// NOI18N
    private static final String REFERENCE_COMMENTS = "// TODO provide a {0} reference to StandardMBean constructor\n";// NOI18N
    private static final String MBEAN_COMMENTS = "// TODO provide a {0} reference to registerMBean\n";// NOI18N
    
    /**
     * Creates a new instance of AddAttrAction
     */
    public RegisterMBeanAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }
    
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if (activatedNodes != null && activatedNodes.length == 1 && activatedNodes[0] != null) {
            if (2 != getTargetSourceType(activatedNodes[0]))
                return true;
        }
        return result;
    }
    private int getTargetSourceType(Node node) {
        EditorCookie cookie = (EditorCookie)node.getCookie(EditorCookie.class);
        if (cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return 0;
        } else if (cookie!=null && "text/x-java".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return 1;
        }
        return 2;
    }
    
    protected void performAction(Node[] nodes) {
        try {
            // show configuration dialog
            // when dialog is canceled, escape the action
            final RegisterMBeanPanel cfg = new RegisterMBeanPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            final StringBuffer methodCall = new StringBuffer();
            
            methodCall.append(REGISTRATION_1);
            
            // First handle Imports
            List<String> imports = new ArrayList<String>();
            imports.add("java.lang.management.ManagementFactory");// NOI18N
            imports.add("javax.management.ObjectName");// NOI18N
            imports.add("javax.management.JMException");// NOI18N
            String className = cfg.getClassName();
            if(cfg.getConstructor() != null) {
                imports.add(className);
                if(cfg.getExecutableConstructor().getParameters().size() > 0) {
                    MessageFormat ctrFormat =
                            new MessageFormat(CONSTRUCTOR_COMMENTS);
                    Object[] arg = {WizardHelpers.getClassName(className)};
                    methodCall.append(ctrFormat.format(arg));
                }
            } else {
                if(cfg.standardMBeanSelected()) {
                    MessageFormat refFormat =
                            new MessageFormat(REFERENCE_COMMENTS);
                    Object[] arg = {WizardHelpers.getClassName(className)};
                    methodCall.append(refFormat.format(arg));
                }else {
                    MessageFormat mbeanFormat =
                            new MessageFormat(MBEAN_COMMENTS);
                    Object[] arg = {WizardHelpers.getClassName(className)};
                    methodCall.append(mbeanFormat.format(arg));
                }
                
            }
            
            methodCall.append(REGISTRATION_2);
            
            String ctr = cfg.getConstructor();
            if (cfg.standardMBeanSelected()) {
                imports.add("javax.management.StandardMBean");// NOI18N
                String itf = cfg.getInterfaceName();
                if(itf != null) {
                    imports.add(itf);
                    // Remove package name
                    itf = WizardHelpers.getClassName(itf);
                    itf = itf + ".class";// NOI18N
                }
                methodCall.append("new StandardMBean(" // NOI18N
                        + (ctr == null ? null : "new " + ctr) + ",\n"// NOI18N
                        + itf);
                
                if(cfg.isMXBean())
                    methodCall.append(", true");// NOI18N
                
                methodCall.append(")");// NOI18N
            } else {
                
                methodCall.append((ctr == null ? null : "new " + ctr));// NOI18N
            }
            
            MessageFormat objNameFormat =
                    new MessageFormat(REGISTRATION_3);
            Object[] arg = {cfg.getMBeanObjectName()};
            methodCall.append(objNameFormat.format(arg));
            
            methodCall.append(REGISTRATION_4);
            
            // Add imports
            JavaModelHelper.addImports(imports, cfg.getAgentJavaSource());
            
            // Generate code
            Runnable doUpdateCode = new Runnable() {
                public void run() {
                    try {
                        DataObject dob = cfg.getDataObject();
                        EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                        JEditorPane pane = ec.getOpenedPanes()[0];
                        int pos = pane.getCaretPosition();
                        Document document = pane.getDocument();
                        IndentEngine eng = IndentEngine.find(document);
                        StringWriter textWriter = new StringWriter();
                        Writer indentWriter = eng.createWriter(document, pos, textWriter);
                        
                        indentWriter.write(methodCall.toString());
                        indentWriter.close();
                        String textToInsert = textWriter.toString();
                        
                        try {
                            document.insertString(pos, textToInsert, null);
                        } catch (BadLocationException badLoc) {
                            document.insertString(pos + 1, textToInsert, null);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            SwingUtilities.invokeLater(doUpdateCode);
  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(""); // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(RegisterMBeanAction.class, "LBL_Action_RegisterMBean"); // NOI18N
    }
}

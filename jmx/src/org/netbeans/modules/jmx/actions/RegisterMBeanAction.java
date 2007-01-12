/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
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

/**
 * Action used to add MBean registration code to an existing JMX Agent.
 * @author tl156378
 */
public class RegisterMBeanAction extends NodeAction {
    
    private FileObject fo;
    private DataObject dob;
    private static String TRY = "try { // Register MBean in Platform MBeanServer\n";// NOI18N
    private static String CATCH = "\n}catch(Exception ex) {\n"+// NOI18N
            "// TODO handle exception\n" +// NOI18N
            "}";// NOI18N
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
            RegisterMBeanPanel cfg = new RegisterMBeanPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            String methodCall = null;
            
            // First handle Imports
            List<String> imports = new ArrayList<String>();
            imports.add("java.lang.management.ManagementFactory");// NOI18N
            imports.add("javax.management.ObjectName");// NOI18N
            if(cfg.getConstructor() != null) {
                imports.add(cfg.getClassName());
            }
            String ctr = cfg.getConstructor();
            if (cfg.standardMBeanSelected()) {
                imports.add("javax.management.StandardMBean");// NOI18N
                String itf = cfg.getInterfaceName();
                if(itf != null) {
                    imports.add(itf);
                    // Remove package name
                    itf = WizardHelpers.getClassName(itf);
                    itf = itf + ".class";
                }
                
                methodCall = TRY + 
                       "ManagementFactory.getPlatformMBeanServer().\n"// NOI18N
                       + "registerMBean(new StandardMBean("// NOI18N
                       + (ctr == null ? null : "new " + ctr) + ",\n"// NOI18N
                       +itf +"),\n"// NOI18N
                       + " new ObjectName(\""+cfg.getMBeanObjectName()+"\"));"// NOI18N
                       + CATCH;
                
            } else {
               methodCall = TRY + 
                       "ManagementFactory.getPlatformMBeanServer().\n"// NOI18N
                       + "registerMBean(new " + cfg.getConstructor() + ",\n"// NOI18N
                       + " new ObjectName(\""+cfg.getMBeanObjectName()+"\"));"// NOI18N
                       +CATCH;
            }
            
            // Add imports
            JavaModelHelper.addImports(imports, cfg.getAgentJavaSource());
            
            // Generate code
            DataObject dob = cfg.getDataObject();
            EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
            JEditorPane pane = ec.getOpenedPanes()[0];
            int pos = pane.getCaretPosition();
            Document document = pane.getDocument();
            IndentEngine eng = IndentEngine.find(document);
            StringWriter textWriter = new StringWriter();
            Writer indentWriter = eng.createWriter(document, pos, textWriter);
            
            
            
            indentWriter.write(methodCall);
            indentWriter.close();
            String textToInsert = textWriter.toString();
            
            try {
                document.insertString(pos, textToInsert, null);
            } catch (BadLocationException badLoc) {
                document.insertString(pos + 1, textToInsert, null);
            }
            
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

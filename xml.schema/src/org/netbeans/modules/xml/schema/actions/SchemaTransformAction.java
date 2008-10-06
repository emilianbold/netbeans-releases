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

package org.netbeans.modules.xml.schema.actions;

import java.awt.Cursor;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.abe.wizard.SchemaTransformWizard;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 * An action on the SchemaDataObject node (SchemaNode)
 * to "Transform" the schema (from one design pattern to another)
 *
 * @author Ayub Khan
 */
public class SchemaTransformAction extends CookieAction {
    private static final long serialVersionUID = 1L;
    
    private static final Class[] COOKIE_ARRAY =
            new Class[] {SchemaModelCookie.class};
    
    private static final String EMPTY_DOC =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\"/>";
        
    private SchemaDataObject sdo = null;
    
    /** Creates a new instance of SchemaViewOpenAction */
    public SchemaTransformAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        return isDocumentOpen(activatedNodes);
    }    
    
    protected void performAction(final Node[] node) {
        assert node.length == 1 :
                "Length of nodes array should be 1";
        final TopComponent activatedTC = TopComponent .getRegistry().getActivated();
        if(activatedTC == null)
            return;                        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {                    
                    activatedTC.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    SchemaModel sm = getSchemaModel(node);
                    if (sm != null) {
                        AXIModel am = AXIModelFactory.getDefault().getModel(sm);
                        SchemaTransformWizard wizard = new SchemaTransformWizard(sm);
                        wizard.show();
                        if (!wizard.isCancelled() && sdo != null) {
                            sdo.setModified(true);
                        }
                    }
                } catch (IOException ex) {
                    activatedTC.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                     DialogDisplayer.getDefault().notify(d);                    
                } finally {
                    activatedTC.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
    
    private SchemaModel getSchemaModel(final Node[] node) throws IOException {
        if(node == null || node.length == 0 || node[0] == null)
            return null;
        
        sdo = node[0].getLookup().lookup(SchemaDataObject.class);
        if (sdo == null)
            return null;
        
        SchemaEditorSupport editor = sdo.getSchemaEditorSupport();
        if (editor == null)
            return null;
        
        return editor.getModel();
    }
    
    private boolean isDocumentOpen(Node[] nodes) {
        if(nodes == null || nodes.length != 1)
            return false;
        SchemaDataObject sDO = nodes[0].getLookup().lookup(
                SchemaDataObject.class);
        if (sDO == null)
            return false;
        SchemaEditorSupport editor = sDO.getSchemaEditorSupport();
        if(editor == null)
            return false;
        
        if(editor.getOpenedPanes() == null ||
            editor.getOpenedPanes().length == 0)
            return false;
        
        return true;
    }
        
    public String getName() {
        return NbBundle.getMessage(
                SchemaViewOpenAction.class, "LBL_ApplyDesignPattern_Name");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        
        return false;
    }
    
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return COOKIE_ARRAY;
    }
}

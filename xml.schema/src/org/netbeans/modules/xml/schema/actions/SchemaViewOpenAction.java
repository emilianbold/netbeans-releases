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
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 * An action on the SchemaDataObject node (SchemaNode)
 * to "Open" the schema multiview to the SchemaView tab
 *
 * @author Jeri Lockhart
 */
public class SchemaViewOpenAction extends NodeAction{
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of SchemaViewOpenAction */
    public SchemaViewOpenAction() {
    }
    
    protected void performAction(final Node[] node) {
	if (node == null || node[0] == null){
	    return;
	}
        final TopComponent activatedTC = TopComponent .getRegistry().getActivated();
        if(activatedTC == null)
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    activatedTC.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    SchemaDataObject sdo = node[0].getLookup().lookup(SchemaDataObject.class);
                    if(sdo == null)
                        return;
                    SchemaEditorSupport ses = sdo.getSchemaEditorSupport();
                    ViewComponentCookie svc = sdo.getCookie(ViewComponentCookie.class);
                    if(svc == null)
                        return;
                    if ( ses.getOpenedPanes()==null ||
                         ses.getOpenedPanes().length==0 ) {
                        svc.view(ViewComponentCookie.View.STRUCTURE,
                            sdo.getSchemaEditorSupport().getModel().getSchema());
                    } else {
                        ses.open();
                    }
                    return;
                } catch (IOException ex) {
                    //ErrorManager.getDefault().notify(ex);
                } finally {
                    activatedTC.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                //worst case: default to open cookie
                OpenCookie oc = node[0].getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            }
        });
    }
    
    protected boolean enable(Node[] node) {
	return true;
    }
    
    public String getName() {
	return NbBundle.getMessage(SchemaViewOpenAction.class, "Open");
    }
    
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
	return false;
    }
    
}

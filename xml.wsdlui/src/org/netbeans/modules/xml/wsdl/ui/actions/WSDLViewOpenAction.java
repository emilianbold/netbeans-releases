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

package org.netbeans.modules.xml.wsdl.ui.actions;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLEditorSupport;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Opens the WSDLDataObject in the WSDL editor tree view.
 *
 * @author Jeri Lockhart
 */
public class WSDLViewOpenAction extends NodeAction{
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
	protected void performAction(Node[] node) {
        if (node == null || node[0] == null) {
            return;
        }
        WSDLDataObject sdo = node[0].getLookup().lookup(
                WSDLDataObject.class);
        if (sdo != null) {
            WSDLEditorSupport wes = sdo.getWSDLEditorSupport();
            ViewComponentCookie svc = sdo.getCookie(
                    ViewComponentCookie.class);
            if (svc != null) {
                WSDLModel model= wes.getModel();
                Definitions def = null;
                ViewComponentCookie.View view = ViewComponentCookie.View.STRUCTURE;
                if (model.getState() ==  Model.State.VALID) {
                    def = model.getDefinitions();
                }
                if (def == null) {
                    view = ViewComponentCookie.View.SOURCE;
                }
                
                if (wes.getOpenedPanes() == null ||
                        wes.getOpenedPanes().length == 0) {
                    svc.view(view, def);
                } else {
                    wes.open();
                }                
            	return;
            }
        }
        // default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    @Override
	protected boolean enable(Node[] node) {
        return true;
    }

    @Override
	public String getName() {
        return NbBundle.getMessage(WSDLViewOpenAction.class,
                "WSDLViewOpenAction_Name");
    }

    @Override
	public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
	protected boolean asynchronous() {
        return false;
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.*;
import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.awt.StatusDisplayer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;

import javax.swing.JEditorPane;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.Icon;
import javax.swing.JButton;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Iterator;
import java.io.IOException;

//import com.sun.rave.designer.DesignerTopComp;
//import com.sun.rave.designer.DesignerPane;


//import org.netbeans.modules.websvc.registry.designer.*;
import org.netbeans.modules.websvc.registry.nodes.WebServiceGroupCookie;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;

/**
 * This action will delete a web service group from the server navigator
 * 
 */

public class DeleteWebServiceGroupAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
		// !PW this does not handle multiple selection (or rather, it does, but
		// only acts on the first node, ignoring the remainder.  See also performAction()
		// which is written the same way.
        if(nodes != null && nodes.length > 0 && nodes[0].getCookie(WebServiceGroupCookie.class) != null) {
			return true;
		} else {
			return false;
		}		
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new AddToFormAction.class);
    }

    protected String iconResource() {
        return "org/netbeans/modules/websvc/registry/resources/MyActionIcon.gif";
    }

    public String getName() {
        return NbBundle.getMessage(DeleteWebServiceGroupAction.class, "DELETE");
    }

    protected void performAction(Node[] nodes) {
        if(null != nodes && nodes.length > 0) {
			WebServiceGroupCookie groupCookie = (WebServiceGroupCookie) nodes[0].getCookie(WebServiceGroupCookie.class);
			if(groupCookie != null) {
				WebServiceGroup wsGroup = groupCookie.getWebServiceGroup();
				String groupName = null;
				if(null != wsGroup) {
					groupName = wsGroup.getName();
				}
				String msg = NbBundle.getMessage(DeleteWebServiceGroupAction.class, "WS_DELETE_GROUP") + " " + groupName;
				NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
				Object response = DialogDisplayer.getDefault().notify(d);

				if(null != response && response.equals(NotifyDescriptor.YES_OPTION)) {
					try {
						// !PW DELEGATING_DESTROY bit appears to be turned on for the FilterNodes
						//     that are passed through here.
						nodes[0].destroy();
					} catch(IOException ioe) {
						ErrorManager.getDefault().notify(ioe);
						StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DeleteWebServiceGroupAction.class, "ERROR_DELETING"));
					}
				}
			}
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}

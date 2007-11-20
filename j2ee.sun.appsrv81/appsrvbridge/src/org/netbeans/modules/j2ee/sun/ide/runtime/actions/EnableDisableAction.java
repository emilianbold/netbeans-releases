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
package org.netbeans.modules.j2ee.sun.ide.runtime.actions;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.bridge.apis.Enableable;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;

/**
 *
 *
 */
public class EnableDisableAction extends NodeAction {
    
    private boolean enabled;
    
    
    /**
     *
     *
     */
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes==null){
            return;
        }
        
        for (int i=0;i<activatedNodes.length;i++){
            Node node = activatedNodes[i];
            Lookup lookup = node.getLookup();
            Object obj = lookup.lookup(Enableable.class);
            try {
                if(obj instanceof Enableable) {
                    Enableable enableableObj = (Enableable)obj;
                    if(enableableObj.isEnabled()) {
                        enableableObj.setEnabled(false);
                        enabled = false;
                    } else {
                        enableableObj.setEnabled(true);
                        enabled = true;
                    }
                }
            } catch(java.lang.RuntimeException rex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,rex);
            }
            
            Node parentNode = node.getParentNode();
            if (parentNode != null) {
                RefreshCookie refreshAction = (RefreshCookie) parentNode.getCookie(RefreshCookie.class);
                if (refreshAction != null) {
                    refreshAction.refresh();
                }
            }
        }
        
    }
    
    
    /**
     *
     *
     */
    protected boolean enable(Node[] nodes) {
        if (null == nodes) {
            return false;
        }
        if(nodes.length > 0) {
            Node node = nodes[0];
            Lookup lookup = node.getLookup();
            Object obj = lookup.lookup(Enableable.class);
            
            try {
                if(obj instanceof Enableable) {
                    Enableable enableableObj = (Enableable)obj;
                    if(enableableObj.isEnabled()) {
                        enabled = false;
                    } else {
                        enabled = true;
                    }
                }
            } catch(java.lang.RuntimeException rex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,rex);
            }
        }
        
        return (nodes.length >= 1) ? true : false;
    }
    
    
    /**
     *
     *
     */
    protected boolean asynchronous() {
        return false;
    }
    
    
    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    
    /**
     *
     */
    public String getName() {
        if(enabled) {
            return NbBundle.getMessage(EnableDisableAction.class, "LBL_EnableAction");
        } else {
            return NbBundle.getMessage(EnableDisableAction.class, "LBL_DisableAction");
        }
    }
    
}

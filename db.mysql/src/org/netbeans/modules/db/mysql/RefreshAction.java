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

package org.netbeans.modules.db.mysql;

import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author David Van Couvering
 */
public class RefreshAction extends CookieAction {
    private static final Class[] COOKIE_CLASSES = new Class[] {
        ServerInstance.class
    };

    public RefreshAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(RefreshAction.class).
                getString("LBL_RefreshAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RefreshAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ( activatedNodes.length == 0 ) {
            // this can happen when a delete occurs
            return false;
        }
        
        Node node = activatedNodes[0];
        
        ServerInstance server = node.getCookie(ServerInstance.class);
        if ( server != null && server.isConnected() ) {
            return true;
        }
        
        return false;
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        
        ServerInstance server = node.getCookie(ServerInstance.class);
        
        if ( server.isConnected() ) {
            try {
                server.refreshDatabaseList();
            } catch ( DatabaseException e ) {
                Utils.displayError(
                    NbBundle.getMessage(this.getClass(), 
                        "MSG_UnableToGetDatabaseList"), 
                    e);
            }
        }
    }
}

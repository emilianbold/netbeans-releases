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
package org.netbeans.modules.php.rt.actions;

import java.util.logging.Logger;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.utils.ActionsDialogs;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.NodeAction;


/**
 * This is actually is implementation action.
 * Each provider provides its own nodes and as result provider
 * has full freedom for assigning any action to node.
 * This action is quite common action for Web server host Node and
 * I put it here.
 *
 * This action is designed for working only with AbstractServerNode
 *
 * @author ads
 *
 */
public class DeleteAction extends NodeAction {

    private static final long serialVersionUID = 897647820461070358L;

    private static final String LBL_CONFIRM_DELETE_HOST_MSG = "LBL_Delete_Host_Msg"; // NOI18N
    private static final String LBL_CONFIRM_DELETE_HOST_TITLE = "LBL_Delete_Host_Title"; // NOI18N
    private static final String LBL_DELETE_HOST = "LBL_Delete_Host"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(DeleteAction.class.getName());

    public static DeleteAction findInstance() {
        return SharedClassObject.findObject(DeleteAction.class, true);
    }
    
    public void delete(Node[] nodes) {
        if (enable(nodes)){
            performAction(nodes);
        }
    }
            
    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#enable(org.openide.nodes.Node[])
     */
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length == 0){
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#performAction(org.openide.nodes.Node[])
     */
    @Override
    protected void performAction(Node[] nodes) {
        LOGGER.info("<<<<< DEL nodes cnt = "+nodes.length);
        for (Node node : nodes) {
            LOGGER.info("<<<<< DEL node name  = "+node.getDisplayName());
            Host host = getHost(node);
            if (host instanceof HostImpl) {
                if (userConfirmDelete(host.getDisplayName())) {
                    ((HostImpl) host).remove();
                    WebServerRegistry.getInstance().removeHost(host);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.SystemAction#getHelpCtx()
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.SystemAction#getName()
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(AddHostAction.class, LBL_DELETE_HOST);
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.CallableSystemAction#asynchronous()
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }

    private Host getHost(Node node) {
        if (node instanceof Lookup.Provider) {
            return (Host) ((Lookup.Provider) node).getLookup().lookup( Host.class );
        } else {
            return null;
        }
    }

    private boolean userConfirmDelete(String host) {
        String title = NbBundle.getMessage(DeleteAction.class, LBL_CONFIRM_DELETE_HOST_TITLE, host);
        String msg = NbBundle.getMessage(DeleteAction.class, LBL_CONFIRM_DELETE_HOST_MSG, host);
        return ActionsDialogs.userConfirmOkCancel(title, msg);
    }
}

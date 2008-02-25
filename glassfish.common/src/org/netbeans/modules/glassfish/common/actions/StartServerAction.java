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

package org.netbeans.modules.glassfish.common.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.ServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
public class StartServerAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        performActionImpl(activatedNodes);
    }
    
    private static void performActionImpl(Node[] nodes) {
        GlassfishModule commonSupport = 
                nodes[0].getLookup().lookup(GlassfishModule.class);
        if(commonSupport != null) {
            commonSupport.startServer(null);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null) ? enableImpl(activatedNodes) : false;
    }
    
    private static boolean enableImpl(Node[] nodes) {
        boolean result = true;
        for(int i = 0; i < nodes.length && result; i++) {
            GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
            if(commonSupport != null) {
                result = checkEnableStart(commonSupport);
            } else {
                // No server instance found for this node.
                result = false;
            }
        }
        return result;
    }
    
    private static final boolean checkEnableStart(GlassfishModule commonSupport) {
        return commonSupport.getServerState() == ServerState.STOPPED;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StartServerAction.class, "CTL_StartServerAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ChangeListener {
    
        private static final String ICON = 
                "org/netbeans/modules/glassfish/common/resources/start.png"; // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private Node node;
        
        public OutputAction(Node node) {
            super(NbBundle.getMessage(StartServerAction.class, "LBL_StartOutput"),
                  new ImageIcon(Utilities.loadImage(ICON)));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(StartServerAction.class, "LBL_StartOutputDesc"));
            this.node = node;
            
            // listen for server state changes
            GlassfishModule commonSupport = node.getLookup().lookup(GlassfishModule.class);
            commonSupport.addChangeListener(WeakListeners.change(this, commonSupport));
        }

        public void actionPerformed(ActionEvent e) {
            performActionImpl(new Node[] { node });
        }

        @Override
        public boolean isEnabled() {
            return enableImpl(new Node[] { node });
        }
        
        // --------------------------------------------------------------------
        // ChangeListener interface implementation
        // --------------------------------------------------------------------
        public void stateChanged(ChangeEvent evt) {
            final GlassfishModule commonSupport = node.getLookup().lookup(GlassfishModule.class);
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    firePropertyChange(PROP_ENABLED, null, 
                            commonSupport.getServerState() == ServerState.STOPPED ? 
                            Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
        
    }
    
}

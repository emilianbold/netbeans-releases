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
package org.netbeans.modules.websvc.core.webservices.action;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;


public class WebServiceActionGroup extends NodeAction implements Presenter.Popup
{
    public String getName() {
        return NbBundle.getMessage(WebServiceActionGroup.class, "LBL_WebServiceActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(AddOperationEditorAction.class),
            SystemAction.get(SetSoapVersionEditorAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        Node[] activatedNodes = getActivatedNodes();
        if (activatedNodes.length == 1 && 
            hasWebService()){
            return new LazyMenu();
        }
        JMenuItem i = super.getPopupPresenter();
        i.setVisible(false);
        return i;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
         assert false : "Should never be called: ";
    }
    
    /**
     * See if there is a webservices.xml. If this DD exists, it indicates
     * that a web service exists in the project.
     */    
    private boolean hasWebService() {
        Node[] activatedNodes = getActivatedNodes();
        DataObject dobj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        if(dobj != null) {
            /*
            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(dobj.getPrimaryFile());
            if (jaxWsSupport!=null && jaxWsSupport.getServices().size()>0) return true;
            */
            return (WebServiceActionProvider.getAddOperationAction(dobj.getPrimaryFile()) != null);
            /*        
            WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(dobj.getPrimaryFile());
            if(wsSupport != null) {
                return (wsSupport.getWebservicesDD() != null);
            }*/
        }
        return false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().<Node>toArray(new Node[0]));
        return enable ? this : null;
    }
    
    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(WebServiceActionGroup.this.getName());
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : "Action had no popup presenter: " + action;
                    }
                }
            }
            return super.getPopupMenu();
        }
 
    }
}

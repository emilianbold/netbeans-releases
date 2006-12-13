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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.core.webservices.action;

import javax.swing.*;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
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
        boolean enable = enable((Node[])actionContext.lookup(new Lookup.Template (Node.class)).allInstances().toArray(new Node[0]));
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

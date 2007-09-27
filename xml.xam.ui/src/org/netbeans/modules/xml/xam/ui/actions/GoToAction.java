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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.cookies.GotoCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;

/**
 * Action which provides a means of showing a component in a particular view.
 * Nodes may provide this action in their set of supported actions, but they
 * must also implement the GotoCookie, which provides the set of supported
 * GotoTypes. These types are the means by which the component is shown in
 * one view or another.
 *
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class GoToAction extends CookieAction {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private ActSubMenuModel model = new ActSubMenuModel(null);
    
    public String getName() {
        return model.createName();
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        model.performActionAt(0);
    }

    public JMenuItem getMenuPresenter() {
        return new Actions.SubMenu(this, model, false);
    }

    public JMenuItem getPopupPresenter() {
        return new Actions.SubMenu(this, model, true);
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[] {
            GotoCookie.class
        };
    }

    protected boolean asynchronous() {
        return false;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /** Implementation of Actions.SubMenuModel */
    private class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID = -4273674308662494596L;
        private transient Node lastNode;
        private transient GotoType[] gotoTypes;
        ActSubMenuModel(Lookup lookup) {
        }

        private Node getSelectedNode() {
            if(lastNode != null)
                return lastNode;
            Node[] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
            if (nodes != null && nodes.length == 1 && nodes[0] != lastNode) {
                lastNode = nodes[0];
            }
            return lastNode;
        }
        
        /**
         * Getter for array of activated goto types.
         *
         * @param  activatedNodes  array of activated nodes.
         * @return  array of GotoType.
         */
        private GotoType[] getGotoTypes(Node node) {
            if( (lastNode == node) && (gotoTypes != null))
                return gotoTypes;
            List<GotoType> types = new ArrayList<GotoType>();
            GotoCookie cookie = node.getCookie(GotoCookie.class);
            if (cookie != null) {
                for (GotoType type : cookie.getGotoTypes()) {
                    Component comp = type.getComponent(node);
                    // Return only the types that are going to work properly.
                    ViewComponentCookie.View view  = type.getView();
                    if (XAMUtils.getViewCookie(comp, view) != null) {
                        types.add(type);
                    }
                }
            }            
            gotoTypes = types.toArray(new GotoType[types.size()]);
            return gotoTypes;
        }
        
        private String createName() {
            GotoType[] types = getGotoTypes(getSelectedNode());
            if (types != null && types.length == 1) {
                return NbBundle.getMessage(GoToAction.class,
                        "LBL_GoTo_Name", types[0].getName());
            } else {
                return NbBundle.getMessage(GoToAction.class, "LBL_GoTo");
            }
        }

        public int getCount() {
            return getGotoTypes(getSelectedNode()).length;
        }

        public String getLabel(int index) {
            GotoType[] types = getGotoTypes(getSelectedNode());
            if (types.length <= index) {
                return null;
            } else {
                return types[index].getName();
            }
        }

        public HelpCtx getHelpCtx(int index) {
            GotoType[] types = getGotoTypes(getSelectedNode());
            if (types.length <= index) {
                return null;
            } else {
                return types[index].getHelpCtx();
            }
        }

        public void performActionAt(int index) {
            Node node = getSelectedNode();
            GotoType[] types = getGotoTypes(getSelectedNode());
            if (types.length > index) {
                types[index].show(node);
            }
            this.lastNode = null;
        }

        /** Adds change listener for changes of the model.
         */
        public void addChangeListener(ChangeListener l) {
            add(ChangeListener.class, l);
        }

        /** Removes change listener for changes of the model.
         */
        public void removeChangeListener(ChangeListener l) {
            remove(ChangeListener.class, l);
        }
    }

    /**
     * A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    private final class DelegateAction implements
            Action, Presenter.Menu, Presenter.Popup {
        /** Action to delegate to. */
        private final CookieAction delegate;

        /** Associated model to use. */
        private final ActSubMenuModel model;

        public DelegateAction(CookieAction a, Lookup actionContext) {
            this.delegate = a;
            this.model = new ActSubMenuModel(actionContext);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            model.performActionAt(0);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return model.createName();
            } else {
                return delegate.getValue(key);
            }
        }

        public boolean isEnabled() {
            return model.getCount() > 0;
        }

        public void setEnabled(boolean b) {
        }

        public JMenuItem getMenuPresenter() {
            return new Actions.SubMenu(this, model, false);
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.SubMenu(this, model, true);
        }
    }
}

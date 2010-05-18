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

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }
    
/**
     * Getter for array of activated goto types.
     *
     * @param  activatedNodes  array of activated nodes.
     * @return  array of GotoType.
     */
    private static GotoType[] getGotoTypes(Node[] activatedNodes) {
        List<GotoType> types = new ArrayList<GotoType>();
        if (activatedNodes != null && activatedNodes.length == 1) {
            Node node = activatedNodes[0];
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
        }
        return types.toArray(new GotoType[types.size()]);
    }    

    /** Implementation of Actions.SubMenuModel */
    private class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
        static final long serialVersionUID = -4273674308662494596L;
        
        private transient Lookup lookup;
        
        ActSubMenuModel(Lookup lookup) {
            this.lookup = lookup;
        }

        private Node[] nodes() {
            return WindowManager.getDefault().getRegistry().getCurrentNodes();
        }
        
        private String createName() {
            GotoType[] types = getGotoTypes(nodes());
            if (types != null && types.length == 1) {
                return NbBundle.getMessage(GoToAction.class,
                        "LBL_GoTo_Name", types[0].getName());
            } else {
                return NbBundle.getMessage(GoToAction.class, "LBL_GoTo");
            }
        }

        public int getCount() {
            return nodes()==null?0:getGotoTypes(nodes()).length;
        }

        public String getLabel(int index) {
            GotoType[] types = getGotoTypes(nodes());
            if ( (types == null) || (types.length <= index) ) {
                return null;
            } else {
                return types[index].getName();
            }
        }

        public HelpCtx getHelpCtx(int index) {
            GotoType[] types = getGotoTypes(nodes());
            if ( (types == null) || (types.length <= index) ) {
                return null;
            } else {
                return types[index].getHelpCtx();
            }
        }

        public void performActionAt(int index) {
            Node[] nodes = nodes();
            GotoType[] types = getGotoTypes(nodes);
            if ((types != null) && (types.length > index)) {
                types[index].show(nodes[0]);
            }
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

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


package org.netbeans.modules.visualweb.designer.jsf.action;


import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.w3c.dom.Element;


/**
 * An action which operates on component context represented by component root <code>Element</code>.
 * The subclasses need to implement the <code>isEnabled</code>, <code>perfomAction</code>
 * and also <code>getDisplayName</code> and <code>getIconBase</code> methods
 * taking the <code>Element</code> array as an argument.
 * If they wish to implement different than standard menu, toolbar or popup
 * presenter, they can just override the corresponding method
 * <code>getMenuPresenter</code>, <code>getPopupPresenter</code>
 * or <code>getToolbarPresenter</code>.
 *
 * @author Peter Zavadsky
 */
abstract class AbstractComponentAction extends AbstractAction implements ContextAwareAction {


    /** Name of property elements. */
    protected static final String PROP_ELEMENTS = "elements"; // NOI18N


    /** Creates a new instance of AbstractDesignBeanAction */
    public AbstractComponentAction() {
        // #6485313 To show the display name in the options dialog.
        putValue(NAME, getDisplayName(null));
    }


    /** This method is not used, only the context aware instance are in the game,
     * this instance is only a placeholder in layer (or nodes). */
    public final void actionPerformed(ActionEvent evt) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalStateException("This can't be called directly, it is just a placeholder," // NOI18N
                + " the context aware instance has to be used.")); // NOI18N
    }

    /** Creates the context aware instance working in the specific context. */
    public final Action createContextAwareInstance(Lookup context) {
        return new DelegateAction(this, context);
    }

    /** Gets action display name based on provided beans. */
    protected abstract String getDisplayName(Element[] componentRootElements);

    /** Gets icon base based on provided beans, in the form "com/mycompany/myIcon.gif".
     * @see org.openide.awt.Actions#connect(AbstractButton, Action) */
    protected abstract String getIconBase(Element[] componentRootElements);

    /** Implement in order to enable/disable the action based on provided beans. */
    protected abstract boolean isEnabled(Element[] componentRootElements);

    /** Implement to perform the action based on provided beans. */
    protected abstract void performAction(Element[] componentRootElements);


    // Presenters
    /** Override if you wish to change the default presenter of the context aware action. */
    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result<Node> result) {
        return new Actions.MenuItem(contextAwareAction, true);
    }

    /** Override if you wish to change the default presenter of the context aware action. */
    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result<Node> result) {
        return new Actions.MenuItem(contextAwareAction, false);
    }

    /** Override if you wish to change the default presenter of the context aware action. */
    protected Component getToolbarPresenter(Action contextAwareAction, Lookup.Result<Node> result) {
//        return new Actions.ToolbarButton(contextAwareAction);
        JButton toolbarButton = new JButton();
        Actions.connect(toolbarButton, contextAwareAction);
        return toolbarButton;
    }
    
    private static final JsfForm getJsfForm(Lookup.Result<Node> result) {
        Node[] nodes = getNodes(result);

        for (Node node : nodes) {
            DesignBean designBean = node.getLookup().lookup(DesignBean.class);
            if (designBean == null) {
                continue;
            }
            DesignContext designContext = designBean.getDesignContext();
            if (designContext == null) {
                continue;
            }
            JsfForm jsfForm = JsfForm.findJsfForm(designContext);
            if (jsfForm != null) {
                return jsfForm;
            }
        }
        return null;
    }
    
    protected static final Element[] getElements(Lookup.Result<Node> result) {
        Node[] nodes = getNodes(result);
        List<Element> elements = new ArrayList<Element>();

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
//            Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementFromNode(node);
            Element componentRootElement = JsfSupportUtilities.getComponentRootElementFromNode(node);
            if (componentRootElement != null) {
                elements.add(componentRootElement);
            }
        }
        return elements.toArray(new Element[elements.size()]);
    }

    private static final Node[] getNodes(Lookup.Result<Node> result) {
        Collection<? extends Node> col = result.allInstances();
        return col.toArray(new Node[col.size()]);
    }
    

    /** Context aware implementation. */
    private static class DelegateAction implements Action, LookupListener, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {
        
        /** <code>AbstractDesignBeanAction</code> to delegate to. */
        private final AbstractComponentAction delegate;

        /** <code>Lookup.Result</code> we are associated with. */
        private final Lookup.Result<Node> result;

        /** State of enabled. */
        private boolean enabled = true;

        /** Support for listeners */
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);

        
        public DelegateAction(AbstractComponentAction delegate, Lookup actionContext) {
            this.delegate = delegate;

            this.result = actionContext.lookup(new Lookup.Template<Node>(Node.class));
            this.result.addLookupListener(WeakListeners.create(LookupListener.class, this, this.result));
            resultChanged(null);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent evt) {
            delegate.performAction(getElements(result));
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        public void putValue(String key, Object object) {
            delegate.putValue(key, object);
        }

        public Object getValue(String key) {
            // XXX Delegating display name and icon base to the
            // context sensitive methods.
            if (Action.NAME.equals(key)) {
                return delegate.getDisplayName(getElements(result));
            } else if ("iconBase".equals(key)) { // NOI18N
                return delegate.getIconBase(getElements(result));
            } else {
                return delegate.getValue(key);
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled == enabled) {
                return;
            }
            
            this.enabled = enabled;
            support.firePropertyChange("enabled", !enabled, enabled); // NOI18N
        }

        public void resultChanged(LookupEvent evt) {
            Element[] elements = getElements(result);
            
            setEnabled(delegate.isEnabled(elements));
            
            support.firePropertyChange(PROP_ELEMENTS, null, elements);
        }

        public JMenuItem getMenuPresenter() {
            return delegate.getMenuPresenter(this, result);
        }

        public JMenuItem getPopupPresenter() {
            return delegate.getPopupPresenter(this, result);
        }

        public Component getToolbarPresenter() {
            return delegate.getToolbarPresenter(this, result);
        }
    } // End of DelegateAction.
            
}

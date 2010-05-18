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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import javax.swing.Action;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author treyspiva
 */
public abstract class WidgetContextAction extends CallableSystemAction implements ContextAwareAction
{

    //////////////////////////////////////////////////////////////////
    // ContextAwareAction Implementation
    
    /** 
     * Implements <code>ContextAwareAction</code> interface method.
     *
     * Returns a delegate action that is associated with a specific lookup and
     * extracts the nodes it operates on from it. Otherwise it delegates to the
     * regular WidgetContextAction (especially to {@link #enable} and {@link #performAction} methods).
     * Note: Never call directly methods <code>setEnabled</code> or <code>putValue</code>
     * of this delegate action, it is useless, they are empty. The enablement
     * state of the action is driven by the content of the <code>actionContext</code>.
     *
     * @param actionContext a lookup contains action context, cannot be <code>null</code>
     * @return a delegate action
     */
    public Action createContextAwareInstance(Lookup actionContext)
    {
        WidgetContext context = actionContext.lookup(WidgetContext.class);
        return new DelegateAction(this, context, actionContext);
    }
    
    //////////////////////////////////////////////////////////////////
    // CallableSystemAction Implementation

    /**
     * In the default implementation, calls {@link #performAction(Node[])}. 
     * 
     * @deprecated Do not call this programmatically. Use 
     * {@link #createContextAwareInstance} to pass in a node selection.  Do not 
     * override this method.
     */ 
    @Deprecated
    public void performAction()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //////////////////////////////////////////////////////////////////
    // Widget Action Methods
    
    public abstract void performAction(Lookup lookup, WidgetContext context, Node[] nodes);
    
    public abstract boolean enable(Lookup context, Node[] nodes);
    
    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    static class DelegateAction implements Action, 
                                           LookupListener, 
                                           Presenter.Menu, 
                                           Presenter.Popup, 
                                           Presenter.Toolbar
    {

        private static final Node[] EMPTY_NODE_ARRAY = new Node[0];
        /** action to delegate too */
        private WidgetContextAction delegate;
        /** lookup we are associated with (or null) */
        private org.openide.util.Lookup.Result<Node> result;
        
        private Lookup contextLookup = null;
        
        private WidgetContext context = null;
        
        /** previous state of enabled */
        private boolean enabled = true;
        /** support for listeners */
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        public DelegateAction(WidgetContextAction a, 
                              WidgetContext context,
                              Lookup actionContext)
        {
            this.delegate = a;

            this.contextLookup = actionContext;
            this.context = context;
            
            this.result = actionContext.lookupResult(Node.class);
            this.result.addLookupListener(WeakListeners.create(LookupListener.class, this, this.result));
            resultChanged(null);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString()
        {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Nodes are taken from the lookup if any.
         */
        public final synchronized Node[] nodes()
        {
            if (result != null)
            {
                return result.allInstances().toArray(EMPTY_NODE_ARRAY);
            }
            else
            {
                return EMPTY_NODE_ARRAY;
            }
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            assert java.awt.EventQueue.isDispatchThread() : "Action " + delegate.getClass().getName() + " may not be invoked from the thread " + Thread.currentThread().getName() + ", only the event queue: http://www.netbeans.org/download/4_1/javadoc/OpenAPIs/apichanges.html#actions-event-thread";

            if (delegate.asynchronous() == true)
            {
                Runnable r = new Runnable()
                {

                    public void run()
                    {
                        delegate.performAction(contextLookup, context, nodes());
                    }
                };

                RequestProcessor.getDefault().post(r);
            }
            else
            {
                delegate.performAction(contextLookup, context, nodes());
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            support.removePropertyChangeListener(listener);
        }

        public void putValue(String key, Object o)
        {
        }

        public Object getValue(String key)
        {
            return delegate.getValue(key);
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean b)
        {
        }

        public void resultChanged(LookupEvent ev)
        {
            boolean old = enabled;
            enabled = delegate.enable(contextLookup, nodes());
            support.firePropertyChange(PROP_ENABLED, old, enabled);
        }

        public JMenuItem getMenuPresenter()
        {
            if (isMethodOverridden(delegate, "getMenuPresenter"))
            {
                // NOI18N
                return delegate.getMenuPresenter();
            }
            else
            {
                return new Actions.MenuItem(this, true);
            }
        }

        public JMenuItem getPopupPresenter()
        {
            if (isMethodOverridden(delegate, "getPopupPresenter"))
            {
                // NOI18N
                return delegate.getPopupPresenter();
            }
            else
            {
                return new Actions.MenuItem(this, false);
            }
        }

        @SuppressWarnings("deprecation")
        public Component getToolbarPresenter()
        {
            if (isMethodOverridden(delegate, "getToolbarPresenter"))
            {
                // NOI18N
                return delegate.getToolbarPresenter();
            }
            else
            {
                return new Actions.ToolbarButton(this);
            }
        }

        private boolean isMethodOverridden(WidgetContextAction d, String name)
        {
            try
            {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            }
            catch (java.lang.NoSuchMethodException ex)
            {
                ex.printStackTrace();
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }
    }
    // end of DelegateAction
}

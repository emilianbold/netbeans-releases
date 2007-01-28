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
package org.netbeans.modules.visualweb.insync.live;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import com.sun.rave.designtime.Customizer2;

/**
 *
 * @author  Carl Quinn
 */
public class CustomizeAction extends NodeAction {

    /**
     *
     */
    private static final long serialVersionUID = 3256440300644742713L;

    protected boolean enable(Node[] nodes) {
        // only enable when a single node is selected
        if (nodes.length > 1) {
            return false ;
        } else {
            return true;
        }
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new AddServerAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(CustomizeAction.class, "Customize"); // NOI18N
    }

    public Action createContextAwareInstance(Lookup context) {
        Action a = super.createContextAwareInstance(context);

        // XXX Trying to change the name according to the liveCustomizer.
        Node node = (Node)context.lookup(Node.class);
        if(node instanceof DesignBeanNode) {
            Customizer2 liveCustomizer = ((DesignBeanNode)node).liveCustomizer;

            if(liveCustomizer != null) {
                // XXX Using the hacked action.. since the a.putValue is
                // overlapped in Node.DelegateAction.
                a = new DelegateAction(a, liveCustomizer.getDisplayName());
            }
        }
        return a;
    }

    protected void performAction(Node[] nodes) {
        if (nodes.length > 0 && nodes[0] instanceof DesignBeanNode) {
            DesignBeanNode lbn = (DesignBeanNode)nodes[0];
            Customizer2 liveCustomizer = lbn.getCustomizer2();
            if (liveCustomizer != null) {
                lbn.invokeCustomizer();
            }
        }
    }

    protected boolean asynchronous(){
	return false;
    }

    // XXX Dont use this anywhere else.
    // Hack to be able change the Node.DelegateAction display name.
    // It should be allowed by the NB NodeAction implementation.
    private static class DelegateAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 3617015264743471412L;
        private final Action delegate;

        public DelegateAction(Action delegate, String dispName) {
            this.delegate = delegate;
            putValue(Action.NAME, dispName);
        }

        public Object getValue(String key) {
            if(Action.NAME.equals(key)) {
                return super.getValue(key);
            } else {
                return delegate.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            delegate.actionPerformed(evt);
        }

        public boolean isEnabled() {
            return delegate.isEnabled();
        }
     }
}

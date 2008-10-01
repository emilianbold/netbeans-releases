/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.refactoring.java.callhierarchy;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Pokorsky
 */
final class CallNode extends AbstractNode {
    
    private CallNode() {
        super(Children.LEAF);
    }

    private CallNode(Children children, CallDescriptor desc) {
        super(children, Lookups.singleton(desc));
        setDisplayName(desc.getDisplayName());
    }

    public static CallNode createDefault() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Default.displayName"));
        return node;
    }
    
    public static CallNode createPleaseWait() {
        CallNode node = new CallNode();
        node.setIconBaseWithExtension("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.PleaseWait.displayName"));
        return node;
    }
    
    private static CallNode createCanceled() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Canceled.displayName"));
        
        return node;
    }

    private static CallNode createBroken() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Broken.displayName"));

        return node;
    }
    
    public static CallNode createCall(CallDescriptor desc) {
        CallNode node = new CallNode(desc.isLeaf() ? Children.LEAF : new CallChildren(), desc);
        return node;
    }
    
    public static Node createRoot(CallHierarchyModel model) {
        Call root = null;
        if (model != null) {
            root = model.getRoot();
        }
        return root != null ? createCall(root) : createDefault();
    }

    @Override
    public Image getIcon(int type) {
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        Icon icon = desc != null ? desc.getIcon() : null;
        return icon != null ? ImageUtilities.icon2Image(icon) : super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        String htmlDisplayName = desc != null ? desc.getHtmlDisplayName() : null;
        return htmlDisplayName != null ? htmlDisplayName : super.getHtmlDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        if (desc != null) {
            if (!(desc instanceof Call) || !((Call) desc).getOccurrences().isEmpty()) {
                return new Action[] {new GoToSourceAction(desc)};
            }
        }
        
        return new Action[0];
    }

    @Override
    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        for (Action action : actions) {
            if (action instanceof GoToSourceAction) {
                return action;
            }
        }
        return null;
    }
    
    static final class CallChildren extends Children.Keys<Object> implements Runnable {
        
        private final boolean isOccurrenceView;
        private final AtomicInteger state = new AtomicInteger(0);

        public CallChildren() {
            this(false);
        }

        public CallChildren(boolean isOccurrenceView) {
            this.isOccurrenceView = isOccurrenceView;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            Node parent = this.getNode();
            Call desc = parent != null ? parent.getLookup().lookup(Call.class) : null;
            if (desc == null) {
                return;
            }
            
            if (isOccurrenceView) {
                this.setKeys(desc.getOccurrences());
            } else {
                this.setKeys(new Node[] {CallNode.createPleaseWait()});
                state.set(0);
                desc.getModel().computeCalls(desc, this);
            }
            
        }

        @Override
        protected Node[] createNodes(Object key) {
            Node node = null;
//            System.out.println("... create nodes: " + key);
            if (key instanceof Node) {
                node = (Node) key;
            } else if (key instanceof CallDescriptor) {
                node = CallNode.createCall((CallDescriptor) key);
            } else {
                // XXX log unknown key
                return null;
            }
            return new Node[] {node};
        }

        public void run() {
            if (state.incrementAndGet() == 1) {
                Children.MUTEX.writeAccess(this);
                return;
            }
            
            // runs under Children.MUTEX
            Node parent = this.getNode();
            Call desc = parent.getLookup().lookup(Call.class);
            List<? extends Object> keys;
            if (desc == null) {
                keys = Collections.emptyList();
            } else {
                keys = desc.getReferences();
                if (!isOccurrenceView && (desc.isCanceled() || desc.isBroken())) {
                    ArrayList<Object> temp = new ArrayList<Object>(keys.size() + 1);
                    temp.addAll(keys);
                    temp.add(desc.isBroken() ? CallNode.createBroken() : CallNode.createCanceled());
                    keys = temp;
                }
            }
            this.setKeys(keys);
        }
        
    }
    
    private static final class GoToSourceAction extends AbstractAction {
        
        private CallDescriptor desc;

        public GoToSourceAction(CallDescriptor desc) {
            super(NbBundle.getMessage(GoToSourceAction.class, "GoToSourceAction.displayName"));
            this.desc = desc;
        }
        
        public void actionPerformed(ActionEvent e) {
            desc.open();
        }
        
    }
    
}

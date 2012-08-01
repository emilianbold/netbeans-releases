/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMBreakpoint;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * An action, that adds a DOM breakpoint.
 * 
 * @author Martin Entlicher
 */
@NbBundle.Messages({
    "AddDOMBreakpoint=Add DOM Breakpoint",
    "CTL_BreakOnSubtreeModif=Break on Subtree Modifications",
    "CTL_BreakOnAttributesModif=Break on Attributes Modifications",
    "CTL_BreakOnNodeRemove=Break on Node Removal"
})
@ActionRegistration(displayName="#AddDOMBreakpoint", lazy=false, asynchronous=false)
@ActionID(category="DOM", id="web.javascript.debugger.actions.AddDOMBreakpointAction")
@ActionReference(path="Navigation/DOM/Actions", position=200, separatorBefore=100)
public class AddDOMBreakpointAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        Node[] nodes = getActivatedNodes();
        return new PopupPresenter(nodes, enable(nodes));
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node n : activatedNodes) {
            org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode =
                    n.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            if (domNode == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return Bundle.AddDOMBreakpoint();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("debug.javascript.addDOMBreakpoint");
    }
    
    private static class PopupPresenter extends JMenuItem implements DynamicMenuContent {
        
        private final JCheckBoxMenuItem[] items;

        private PopupPresenter(Node[] activatedNodes, boolean enabled) {
            items = new JCheckBoxMenuItem[] {
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnSubtreeModif()),
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnAttributesModif()),
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnNodeRemove()),
            };
            if (!enabled) {
                for (JComponent c : items) {
                    c.setEnabled(false);
                }
            } else {
                DOMBreakpoint[] domBreakpoints = findDOMBreakpoints();
                org.netbeans.modules.web.webkit.debugging.api.dom.Node[] activatedDomNodes =
                        new org.netbeans.modules.web.webkit.debugging.api.dom.Node[activatedNodes.length];
                for (int i = 0; i < activatedNodes.length; i++) {
                    activatedDomNodes[i] = activatedNodes[i].getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                    bind(items[0], activatedDomNodes[i], DOMBreakpoint.Type.SUBTREE_MODIFIED, domBreakpoints);
                    bind(items[1], activatedDomNodes[i], DOMBreakpoint.Type.ATTRIBUTE_MODIFIED, domBreakpoints);
                    bind(items[2], activatedDomNodes[i], DOMBreakpoint.Type.NODE_REMOVED, domBreakpoints);
                }
            }
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return items;
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return this.items;
        }
        
    }
    
    private static void bind(final JCheckBoxMenuItem cmi, final org.netbeans.modules.web.webkit.debugging.api.dom.Node node,
                             final DOMBreakpoint.Type type, DOMBreakpoint[] domBreakpoints) {
        DOMBreakpoint db = findBreakpointOn(node, domBreakpoints);
        if (db != null) {
            cmi.setSelected(db.getTypes().contains(type));
        }
        final DOMBreakpoint[] dbPtr = new DOMBreakpoint[] { db };
        cmi.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dbPtr[0] == null) {
                    dbPtr[0] = new DOMBreakpoint(DOMNode.findURL(node), DOMNode.create(node));
                    DebuggerManager.getDebuggerManager().addBreakpoint(dbPtr[0]);
                }
                if (cmi.isSelected()) {
                    dbPtr[0].addType(type);
                } else {
                    dbPtr[0].removeType(type);
                }
                if (dbPtr[0].getTypes().isEmpty()) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(dbPtr[0]);
                    dbPtr[0] = null;
                }
            }
        });
    }
    
    private static DOMBreakpoint findBreakpointOn(org.netbeans.modules.web.webkit.debugging.api.dom.Node node,
                                                  DOMBreakpoint[] domBreakpoints) {
        for (DOMBreakpoint db : domBreakpoints) {
            if (node.equals(db.getNode().getNode())) {
                return db;
            }
        }
        return null;
    }
    
    private static DOMBreakpoint[] findDOMBreakpoints() {
        List<DOMBreakpoint> domBreakpoints = new ArrayList<DOMBreakpoint>();
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint b : breakpoints) {
            if (b instanceof DOMBreakpoint) {
                domBreakpoints.add((DOMBreakpoint) b);
            }
        }
        return domBreakpoints.toArray(new DOMBreakpoint[]{});
    }

}

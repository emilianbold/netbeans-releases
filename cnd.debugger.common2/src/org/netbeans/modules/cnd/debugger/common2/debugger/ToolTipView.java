/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Nikolay Koldunov
 */
public final class ToolTipView extends JComponent implements ExplorerManager.Provider {
    private static final ToolTipView INSTANCE = new ToolTipView();
    private static final ExplorerManager manager = new ExplorerManager();
    
    private static RequestProcessor RP = new RequestProcessor(ToolTipView.class.getName());
    
    private ActionListener listener;
    public static int ON_DISPOSE = 0;
    
    public static ToolTipView getDefault() {
        return INSTANCE;
    }

    public ToolTipView() {
        final OutlineView ov = new OutlineView();
        ov.setPropertyColumns("value", "Value"); //NOI18N
        ov.getOutline().getColumnModel().getColumn(0).setHeaderValue("Property"); //NOI18N
        ov.getOutline().setRootVisible(true);

        setLayout(new BorderLayout());
        add(ov, BorderLayout.CENTER);
    }

    public ToolTipView setRootElement(Node node) {
        getExplorerManager().setRootContext(node);
        return this;
    }
    
    public ToolTipView setOnDisposeListener(ActionListener listener) {
        this.listener = listener;
        return this;
    }

    public void showTooltip() {
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        final EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(ep);
        final ToolTipSupport toolTipSupport = eui.getToolTipSupport();
        toolTipSupport.setToolTip(this);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (!aFlag) {
            if (listener != null) {
                listener.actionPerformed(new ActionEvent(this, ON_DISPOSE, null));
                listener = null;
            }
        }
    }
   
    public static final class VariableNode extends AbstractNode {

        private Variable v;
        private static final Map<Variable, VariableNode> variables = new HashMap<Variable, VariableNode>();
        private static WatchModel watchModel = new WatchModel();
    
        public VariableNode(Variable v, Children ch) {
            super(ch);
            this.v = v;
            variables.put(v, this);
        }
        
        public static VariableNode getNodeForVariable(Variable v) {
            return variables.get(v);
        }

        @Override
        public String getDisplayName() {
            return v.getVariableName();
        }

        @Override
        public Image getIcon(int type) {
            String path = "";
            
            try {
                path = watchModel.getIconBaseWithExtension(null, v);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(ex);
            }
            setIconBaseWithExtension(path);
            return super.getIcon(type);
        }

        public void propertyChanged() {
            if (!(getChildren() instanceof VariableNodeChildren)) {
                return;
            }
            if (v.getNumChild() < 1) {
                setChildren(Children.LEAF);
            } else {
                ((VariableNodeChildren) getChildren()).updateKeys();
            }
            
            fireDisplayNameChange("old", "new"); //NOI18N
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            return new Node.PropertySet[]{new VariableNodePropertySet(v)};
        }

        private final class VariableNodePropertySet extends Node.PropertySet {

            private Variable v;

            public VariableNodePropertySet(Variable v) {
                this.v = v;
            }

            @Override
            public Node.Property<?>[] getProperties() {

                Node.Property<?>[] ps = new Node.Property<?>[]{new Node.Property<String>(String.class) {
                        @Override
                        public String getName() {
                            return "value"; //NOI18N
                        }

                        @Override
                        public boolean canRead() {
                            return true;
                        }

                        @Override
                        public String getValue() throws IllegalAccessException, InvocationTargetException {
                            return v.getAsText();
                        }

                        @Override
                        public boolean canWrite() {
                            return false;
                        }

                        @Override
                        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        }
                    }};
                return ps;
            }
        }
    }
    
    public static abstract class VariableNodeChildren extends Children.Keys<Variable> {
        private final Variable var;
        public VariableNodeChildren(Variable v) {
            var = v;
        }
        
        private void updateKeys() {
            // e.g.(?) Explorer view under Children.MUTEX subsequently calls e.g.
            // SuiteProject$Info.getSimpleName() which acquires ProjectManager.mutex(). And
            // since this method might be called under ProjectManager.mutex() write access
            // and updateKeys() --> setKeys() in turn calls Children.MUTEX write access,
            // deadlock is here, so preventing it... (also got this under read access)
            RP.post(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setKeys(var.getChildren());
                        }
                    });
                }
            });
        }

        @Override
        protected Node[] createNodes(Variable key) {
            return new Node[]{new VariableNode(key, Children.LEAF)};
        }
    }
    
}

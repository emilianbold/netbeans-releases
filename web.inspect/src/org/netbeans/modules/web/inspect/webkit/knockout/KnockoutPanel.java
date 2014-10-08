/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit.knockout;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * A panel for Knockout-related information about the inspected page.
 *
 * @author Jan Stola
 */
public class KnockoutPanel extends JPanel implements ExplorerManager.Provider {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(KnockoutPanel.class);
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Page model for this panel. */
    private final WebKitPageModel pageModel;
    /** View that displays Knockout context of the selected node. */
    private OutlineView contextView;

    /**
     * Creates a new {@code KnockoutPanel}.
     */
    public KnockoutPanel(WebKitPageModel pageModel) {
        this.pageModel = pageModel;

        initContextView();
        initComponents();
        if (pageModel == null) {
            messageLabel.setText(NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.messageLabel.noInspection")); // NOI18N
            add(messageLabel);
        } else {
            pageModel.addPropertyChangeListener(new Listener());
            update();
        }
    }

    /**
     * Initializes the context view.
     */
    private void initContextView() {
        contextView = new OutlineView(
                NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.contextView.name")); // NOI18N
        contextView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        contextView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        contextView.addPropertyColumn(
                KnockoutNode.ValueProperty.NAME,
                NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.contextView.value")); // NOI18N

        Outline outline = contextView.getOutline();
        outline.setRootVisible(false);
        TableCellRenderer renderer = outline.getDefaultRenderer(Object.class);
        if (renderer != null) {
            outline.setDefaultRenderer(Object.class, new NoIconRenderer(renderer));
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Updates the panel (according to the current selection).
     */
    final void update() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            });
            return;
        }
        List<? extends Node> selection = pageModel.getSelectedNodes();
        JComponent componentToShow;
        if (selection.isEmpty()) {
            messageLabel.setText(NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.messageLabel.noSelection")); // NOI18N
            componentToShow = messageLabel;
        } else if (selection.size() > 1) {
            messageLabel.setText(NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.messageLabel.noSingleSelection")); // NOI18N
            componentToShow = messageLabel;
        } else {
            Node selectedNode = selection.get(0);
            org.netbeans.modules.web.webkit.debugging.api.dom.Node webKitNode =
                selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            WebKitDebugging webKit = pageModel.getWebKit();
            Node rootNode = new AbstractNode(Children.create(new KnockoutChildFactory(webKit, webKitNode), true));
            getExplorerManager().setRootContext(rootNode);
            expandDataNode();
            componentToShow = contextView;
        }
        if (componentToShow.getParent() == null) {
            removeAll();
            add(componentToShow);
        }
        revalidate();
        repaint();
    }

    /**
     * Expands the {@code $data} node of the binding context.
     */
    private void expandDataNode() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                for (final Node node : manager.getRootContext().getChildren().getNodes(true)) {
                    if ("$data".equals(node.getName())) { // NOI18N
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                contextView.expandNode(node);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();

        messageLabel.setBackground(contextView.getViewport().getView().getBackground());
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageLabel.setEnabled(false);
        messageLabel.setOpaque(true);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener for the changes of the page model.
     */
    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (PageModel.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                update();
            }
        }
        
    }

    /**
     * Renderer that delegates to the given renderer but doesn't paint icons.
     */
    final static class NoIconRenderer implements TableCellRenderer {
        /** The original table cell renderer. */
        private final TableCellRenderer originalRenderer;
        /** Icon used instead of the original icons. */
        private final Icon emptyIcon;

        /**
         * Creates a new {@code NoIconRenderer} for the specified renderer.
         * 
         * @param originalRenderer renderer whose icon should be removed.
         */
        public NoIconRenderer(TableCellRenderer originalRenderer) {
            this.originalRenderer = originalRenderer;
            // Icons with zero width/height are ignored
            this.emptyIcon = new EmptyIcon(1, 1);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (component instanceof JLabel) {
                JLabel label = (JLabel)component;
                if (label.getIcon() != null) {
                    label.setIcon(emptyIcon);
                }
            }
            return component;
        }

    }

    /**
     * Empty (transparent) icon.
     */
    final static class EmptyIcon implements Icon {
        private final int width;
        private final int height;

        /**
         * Creates a new {@code EmptyIcon}.
         * 
         * @param width width of the icon.
         * @param height height of the icon.
         */
        EmptyIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
        
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.netbeans.modules.dlight.util.ui.Renderer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Helper class for implementing dual-pane visualizers.
 *
 * @author Alexey Vladykin
 */
public final class DualPaneSupport<T> extends JSplitPane {

    private static final String SWITCH_TO_LEFT = "switchToLeftComponent"; // NOI18N
    private static final String SWITCH_TO_RIGHT = "switchToRightComponent"; // NOI18N

    private JComponent detailsComponent;
    private Renderer<T> detailsRenderer;
    private final JComponent masterComponent;

    /**
     * Creates new <code>DualPaneSupport</code> for given master component.
     * Users of this constructor should manually add selection listeners
     * to master component.
     *
     * @param masterComponent
     * @param detailsRenderer
     */
    public DualPaneSupport(JComponent masterComponent, Renderer<T> detailsRenderer) {
        super(HORIZONTAL_SPLIT);
        this.detailsRenderer = detailsRenderer;
        this.detailsComponent = null;
        this.masterComponent = masterComponent;
        setResizeWeight(0.7);
        setContinuousLayout(true);
        setLeftComponent(masterComponent);
        setFocusCycleRoot(true);

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), SWITCH_TO_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), SWITCH_TO_RIGHT);

        ActionMap actionMap = getActionMap();
        actionMap.put(SWITCH_TO_LEFT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                focus(getLeftComponent());
            }
        });
        actionMap.put(SWITCH_TO_RIGHT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                focus(getRightComponent());
            }
        });
    }

    private void focus(Component component) {
        while (component instanceof JScrollPane) {
            component = ((JScrollPane)component).getViewport().getView();
        }
        if (component != null) {
            component.requestFocusInWindow();
        }
    }

    @Override
    public void requestFocus() {
        masterComponent.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return masterComponent.requestFocusInWindow();
    }

    public void showDetailsFor(T item) {
        boolean keepDividerPos = (detailsComponent == null) == (item == null);
        detailsComponent = null;
        if (item == null) {
            detailsComponent = new JLabel(NbBundle.getMessage(DualPaneSupport.class, "DualPaneSupport.NoSelection"), JLabel.CENTER); // NOI18N
        } else if (detailsRenderer != null) {
            detailsComponent = detailsRenderer.render(item);
        }
        if (detailsComponent == null) {
            detailsComponent = new JLabel(NbBundle.getMessage(DualPaneSupport.class, "DualPaneSupport.NoDetails"), JLabel.CENTER); // NOI18N
        }
        int oldDividerPos = keepDividerPos? getDividerLocation() : 0;
        setRightComponent(detailsComponent);
        if (keepDividerPos) {
            setDividerLocation(oldDividerPos);
        }
    }

    public static interface DataAdapter<U, V> {
        V convert(U obj);
    }

    public static<V> DualPaneSupport<V> forExplorerManager(
            final JComponent component, final ExplorerManager explorerManager,
            final Renderer<V> detailsRenderer,
            final DataAdapter<Node, V> dataAdapter) {
        final DualPaneSupport<V> dualPaneSupport = new DualPaneSupport<V>(component, detailsRenderer);
        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] selectedNodes = (Node[]) evt.getNewValue();
                    if (selectedNodes != null && 0 < selectedNodes.length) {
                        V data = dataAdapter.convert(selectedNodes[0]);
                        dualPaneSupport.showDetailsFor(data);
                    } else {
                        dualPaneSupport.showDetailsFor(null);
                    }
                }
            }
        });
        dualPaneSupport.showDetailsFor(null);
        return dualPaneSupport;
    }
}

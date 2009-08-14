/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import org.netbeans.modules.dlight.visualizers.api.DetailsRenderer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Helper class for implementing dual-pane visualizers.
 *
 * @author Alexey Vladykin
 */
public final class DualPaneSupport<T> extends JSplitPane {

    private JComponent detailsComponent;
    private DetailsRenderer<T> detailsRenderer;

    /**
     * Creates new <code>DualPaneSupport</code> for given master component.
     * Users of this constructor should manually add selection listeners
     * to master component.
     *
     * @param masterComponent
     * @param detailsRenderer
     */
    public DualPaneSupport(JComponent masterComponent, DetailsRenderer<T> detailsRenderer) {
        super(HORIZONTAL_SPLIT);
        this.detailsRenderer = detailsRenderer;
        this.detailsComponent = null;
        setResizeWeight(0.5);
        setContinuousLayout(true);
        setLeftComponent(masterComponent);
    }

    public void showDetailsFor(T item) {
        boolean keepDividerPos = (detailsComponent == null) == (item == null);
        detailsComponent = null;
        if (item != null && detailsRenderer != null) {
            detailsComponent = detailsRenderer.render(item);
        }
        if (detailsComponent == null) {
            detailsComponent = new JLabel(NbBundle.getMessage(DualPaneSupport.class, "DualPaneSupport.DetailsEmpty"), JLabel.CENTER); // NOI18N
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

    public static<V> DualPaneSupport forExplorerManager(
            final JComponent component, final ExplorerManager explorerManager,
            final DetailsRenderer<V> detailsRenderer,
            final DataAdapter<Node, V> dataAdapter) {
        final DualPaneSupport dualPaneSupport = new DualPaneSupport(component, detailsRenderer);
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
        return dualPaneSupport;
    }
}

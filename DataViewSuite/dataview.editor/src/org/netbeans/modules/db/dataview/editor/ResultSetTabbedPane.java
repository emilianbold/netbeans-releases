/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.db.dataview.editor;

import org.netbeans.modules.db.dataview.output.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;

/**
 * ResultSetTabbedPane
 * 
 * @author Ahimanikya Satapathy
 */
public class ResultSetTabbedPane extends JSplitPane {

    private JTabbedPane tabbedPane;
    private PopupListener listener;
    private ChangeListener listen;
    private JPopupMenu pop;
    private CloseListener closeL;
    private DataViewOutputPanel lastKnownSelection = null;
    private DataViewOutputPanel newSelection;

    public ResultSetTabbedPane(int newOrientation, Component newLeftComponent, JTabbedPane newRightComponent) {
        super(newOrientation);
        setTopComponent(newLeftComponent);
        tabbedPane = newRightComponent;
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    JTabbedPane jp = ((JTabbedPane) e.getSource());
                    newSelection = (DataViewOutputPanel) jp.getSelectedComponent();
                    fire(lastKnownSelection, newSelection);
                }
            }
        };
    }

    private void fire(DataViewOutputPanel formerSelection, DataViewOutputPanel selection) {
        if (formerSelection != selection && selection != null) {
            lastKnownSelection = selection;
        }
    }
    private String nbBundle1 = "Close Tab";
    private String nbBundle2 = "Close All Tabs";
    private String nbBundle3 = "Close Other Tabs";

    private class Close extends AbstractAction {

        public Close() {
            super(nbBundle1);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                removePanel(tabbedPane.getSelectedComponent());
            }

        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(nbBundle2);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAll(tabbedPane);
            }
            tabbedPane.removeAll();
            setBottomComponent(null);
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(nbBundle3);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAllButCurrent(tabbedPane);
            }
        }
    }

    private void closeAllButCurrent(JTabbedPane tabs) {
        Component current = tabs.getSelectedComponent();
        for (Component comp : tabs.getComponents()) {
            if (comp != current) {
                removePanel(comp);
            }
        }
    }

    private void closeAll(JTabbedPane tabs) {
        for (Component comp : tabs.getComponents()) {
            removePanel(comp);
        }
        this.revalidate();
    }

    private class CloseListener implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((Component) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        @Override
        protected void showPopup(MouseEvent e) {
            pop.show(tabbedPane, e.getX(), e.getY());
        }
    }

    public void removePanel(Component panel) {
        if (tabbedPane.getComponentCount() == 0) {
            setBottomComponent(null);
        } else {
            tabbedPane.remove(panel);
            if (tabbedPane.getComponentCount() == 1) {
                Component c = tabbedPane.getSelectedComponent();
                lastKnownSelection = (DataViewOutputPanel) c;
                tabbedPane.removeMouseListener(listener);
                tabbedPane.removePropertyChangeListener(closeL);
                setBottomComponent(null);
                setBottomComponent(c);
                setDividerLocation(200);
                setDividerSize(7);
            }
        }
        this.revalidate();
    }

    public void addPanel(Component panel) {
        if (rightComponent == null) {
            setBottomComponent(panel);
            setDividerLocation(200);
            setDividerSize(7);
            if (panel instanceof DataViewOutputPanel) {
                lastKnownSelection = (DataViewOutputPanel) panel;
            }
        } else if (tabbedPane.getComponentCount() == 0 && lastKnownSelection != panel) {
            Component comp = (Component) lastKnownSelection;
            setBottomComponent(null);
            tabbedPane.addMouseListener(listener);
            tabbedPane.addPropertyChangeListener(closeL);
            tabbedPane.addChangeListener(listen);
            setBottomComponent(tabbedPane);
            setDividerLocation(200);
            setDividerSize(7);

            String name = comp.getName();
            String title = name.substring(0, Math.min(name.length(), 25));
            tabbedPane.addTab(title + "...", null, comp, name); //NOI18N

            name = panel.getName();
            title = name.substring(0, Math.min(name.length(), 25));
            tabbedPane.addTab(title + "...", null, panel, name); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        } else if (lastKnownSelection != panel) {
            String name = panel.getName();
            String title = name.substring(0, Math.min(name.length(), 25));
            tabbedPane.addTab(title + "...", null, panel, name); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        }

        this.validate();
    }
}

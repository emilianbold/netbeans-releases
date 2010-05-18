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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.ui;

import org.openide.awt.HtmlRenderer.Renderer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.view.ListView;
import org.openide.explorer.view.NodeRenderer;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A JList-based list of Nodes which show checkboxes.
 *
 * @author Tim Boudreau
 */
public class CheckboxListView extends ListView {
    public static final String SELECTED = "selected"; //NOI18N
    private final EventHandler ml = new EventHandler();
    private boolean checkboxesEnabled = true;
    private NodeCheckObserver observer;

    public CheckboxListView() {
        setPopupAllowed(false);
        setViewportBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                UIManager.getColor("text"))); //NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //Need to do this here since overriding createList does not work -
        //the list is being created somewhere else; this is the best place
        //to intercept it
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (checkboxesEnabled) {
            enableCheckboxes();
        }
    }
    
    private boolean enabled = true;
    public void setCheckboxesEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setListEnabled(boolean enabled) {
        list.setEnabled(enabled);
    }

    /**
     * Set an observer, which will be notified when nodes are checked or
     * unchecked.
     * @param observer
     */
    public final void setNodeCheckObserver (NodeCheckObserver observer) {
        this.observer = observer;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (checkboxesEnabled) {
            disableCheckboxes();
        }
    }

    public synchronized final void setCheckboxesVisible(boolean val) {
        if (val != checkboxesEnabled) {
            checkboxesEnabled = val;
            if (isDisplayable()) {
                if (val) {
                    enableCheckboxes();
                } else {
                    disableCheckboxes();
                }
            }
        }
    }

    private void enableCheckboxes() {
        list.addMouseListener(ml);
        list.addKeyListener(ml);
        list.setCellRenderer(new CheckboxCellRenderer());
        repaint();
    }

    private void disableCheckboxes() {
        list.removeMouseListener(ml);
        list.removeKeyListener(ml);
        list.setCellRenderer(new NodeRenderer());
        repaint();
    }

    public void setCheckedNodes(Iterable<Node> nodes) {
        clearCheckedNodes();
        for (Node n : nodes) {
            n.setValue(SELECTED, Boolean.TRUE);
        }
        repaint();
    }

    public void clearCheckedNodes() {
        for (Node n : allNodes()) {
            boolean val = Boolean.TRUE.equals(n.getValue(SELECTED));
            if (observer != null) {
                observer.onNodeUnchecked(n);
            }
            n.setValue(SELECTED, Boolean.FALSE);
        }
        repaint();
    }

    private void selectNode(int index) {
        if (!enabled) {
            return;
        }
        if (index < 0) {
            return;
        }
        ExplorerManager.Provider prov = (Provider) SwingUtilities.getAncestorOfClass(ExplorerManager.Provider.class, this);
        if (prov != null) {
            Node[] n = prov.getExplorerManager().getRootContext().getChildren().getNodes();
            if (index < n.length) {
                Boolean val = (Boolean) n[index].getValue(SELECTED);
                boolean wasSet = val != null && val.booleanValue();
                n[index].setValue(SELECTED, !wasSet);
                if (observer != null) {
                    if (!wasSet) {
                        observer.onNodeChecked(n[index]);
                    } else {
                        observer.onNodeUnchecked(n[index]);
                    }
                }
                repaint();
            }
        }
    }

    private Node[] allNodes() {
        ExplorerManager.Provider prov = (Provider)
                SwingUtilities.getAncestorOfClass(ExplorerManager.Provider.class, this);
        return  prov ==  null ? new Node[0] :
            prov.getExplorerManager().getRootContext().getChildren().getNodes();
    }

    private boolean isChecked(int index) {
        if (index < 0) {
            return false;
        }
        Node[] n = allNodes();
        return index > n.length - 1 ? false :
            Boolean.TRUE.equals(n[index].getValue(SELECTED));
    }

    private class EventHandler extends KeyAdapter implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                selectNode(findIndex(e));
                e.consume();
            }
        }

        private int findIndex(MouseEvent e) {
            JList jl = (JList) e.getSource();
            int result = jl.locationToIndex(e.getPoint());
            return result;
        }
        int pressIndex = -1;

        public void mousePressed(MouseEvent e) {
            pressIndex = findIndex(e);
        }

        public void mouseReleased(MouseEvent e) {
            int ix = findIndex(e);
            if (pressIndex == ix) {
                if (e.getX() <= 24) {
                    selectNode(ix);
                    e.consume();
                }
            }
            pressIndex = -1;
        }

        public void mouseEntered(MouseEvent e) {
            //do nothing
        }

        public void mouseExited(MouseEvent e) {
            //do nothing
        }

        @Override
        public void keyTyped(KeyEvent e) {
            JList jl = (JList) e.getSource();
            if (e.getKeyChar() == '\n' && e.getModifiers() == 0) {
                selectNode(jl.getSelectedIndex());
                e.consume();
            }
        }
    }

    private final class CheckboxCellRenderer extends NodeRenderer {

        final WrapperComponent rr = new WrapperComponent();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean sel, boolean cellHasFocus) {
            cellHasFocus = false;
            Component c = super.getListCellRendererComponent(list, value, index, sel, cellHasFocus);
            Renderer r = (Renderer) c;
            r.setIndent(20);
            rr.setSelected(isChecked(index));
            rr.setToPaint(c);
            rr.setEnabled (enabled);
            c.setEnabled (enabled);
            return rr;
        }
    }

    private static class WrapperComponent extends JComponent {

        Component toPaint;
        private final JCheckBox box = new JCheckBox();

        WrapperComponent() {
            add(box);
            box.setBorder(BorderFactory.createEmptyBorder());
        }

        void setToPaint(Component toPaint) {
            if (this.toPaint != toPaint) {
                if (this.toPaint != null) {
                    remove(this.toPaint);
                }
                this.toPaint = toPaint;
                box.setBackground(toPaint.getBackground());
                add(toPaint);
            }
            doLayout();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension a = box.getPreferredSize();
            Dimension b = toPaint == null ? new Dimension(0, 0) : toPaint.getPreferredSize();
            return new Dimension(a.width + b.width, Math.max(a.height, b.height) + 1);
        }

        @Override
        public void doLayout() {
            Dimension a = box.getPreferredSize();
            Dimension size = getPreferredSize();
            Insets ins = getInsets();
            size.height -= ins.top + ins.bottom;
            int x = ins.left;
            int y = a.height == size.height ? ins.top : ins.top + ((size.height / 2) - (a.height / 2));
            box.setBounds(x, y, a.width, a.height);
            x += a.width;
            if (toPaint != null) {
                a = toPaint.getPreferredSize();
                y = a.height == size.height ? ins.top : ins.top + ((size.height / 2) - (a.height / 2));
                toPaint.setBounds(x, y, a.width, a.height);
            }
        }

        void setSelected(boolean val) {
            box.setSelected(val);
        }

        @Override
        public void repaint(int x, int y, int w, int h) {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            //do nothing
        }

        @Override
        public void invalidate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        public void repaint() {
        }

        @Override
        public void validate() {
        }
    }
}

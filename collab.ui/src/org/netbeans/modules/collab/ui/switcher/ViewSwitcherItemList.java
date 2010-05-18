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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.ui.switcher;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import java.beans.BeanInfo;
import java.lang.ref.WeakReference;

/**
 * Specialized JList for palette items (content of a palette category) - having
 * special UI and renderer providing fine-tuned alignment, rollover effect,
 * showing names and different icon size. Used by CategoryPresentPanel.
 *
 * @author Tomas Pavek
 * @author Todd Fast, todd.fast@sun.com (suitability modifications)
 */
public class ViewSwitcherItemList extends JList {
    static final int BASIC_ICONSIZE = BeanInfo.ICON_COLOR_16x16;
    private static WeakReference rendererRef;
    private int rolloverIndex = -1;
    private boolean showNames;
    private int iconSize = BASIC_ICONSIZE;

    /** Constructor. */
    public ViewSwitcherItemList() {
        super(new DefaultListModel());
        setOpaque(false);
        setBorder(new EmptyBorder(2, 2, 2, 1));
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(getItemRenderer());
    }

    public void updateUI() {
        setUI(new ViewSwitcherItemListUI());
        invalidate();
    }

    // ---------
    boolean getShowNames() {
        return showNames;
    }

    void setShowNames(boolean show) {
        if (show != showNames) {
            showNames = show;
            firePropertyChange("cellRenderer", null, null); // NOI18N
        }
    }

    int getIconSize() {
        return iconSize;
    }

    void setIconSize(int size) {
        if (size != iconSize) {
            iconSize = size;
            firePropertyChange("cellRenderer", null, null); // NOI18N
        }
    }

    // --------
    // list item renderer
    private static ListCellRenderer getItemRenderer() {
        ListCellRenderer renderer = (rendererRef == null) ? null : (ListCellRenderer) rendererRef.get();

        if (renderer == null) {
            renderer = new ItemRenderer();
            rendererRef = new WeakReference(renderer);
        }

        return renderer;
    }

    static class ItemRenderer implements ListCellRenderer {
        private JToggleButton button;
        private Border defaultBorder;
        private Border emptyBorder;

        ItemRenderer() {
            button = new JToggleButton();

            String laf = UIManager.getLookAndFeel().getClass().getName();

            if (
                laf.equals("javax.swing.plaf.metal.MetalLookAndFeel") // NOI18N
                     ||laf.equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel")
            ) // NOI18N
             { // for Metal and Windows Look&Feel use toolbar button rendering
                button.setMargin(new Insets(1, 0, 1, 0));

                JToolBar toolbar = new JToolBar();
                toolbar.setRollover(true);
                toolbar.setFloatable(false);
                toolbar.setLayout(new BorderLayout(0, 0));
                toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
                toolbar.add(button);
            } else { // otherwise use normal button with default or empty border
                button.setMargin(new Insets(1, 1, 1, 1));
                defaultBorder = button.getBorder();
                emptyBorder = new EmptyBorder(defaultBorder.getBorderInsets(button));
            }
        }

        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            ViewSwitcherItemList itemList = (ViewSwitcherItemList) list;
            boolean showNames = itemList.getShowNames();
            int iconSize = itemList.getIconSize();

            JComponent rendererComponent = (defaultBorder == null) ? (JComponent) button.getParent() : button;

            ViewSwitcherPane.Item item = (ViewSwitcherPane.Item) value;

            //            Icon icon = (Icon) node.getValue("icon_"+iconSize); // NOI18N
            //            if (icon == null) {
            //                icon = new ImageIcon(node.getIcon(iconSize));
            //                node.setValue("icon_"+iconSize, icon); // NOI18N
            //            }
            Icon icon = (Icon) item.getIcon(); // NOI18N
            button.setIcon(icon);

            button.setText(showNames ? item.getTitle() : null);
            rendererComponent.setToolTipText(
                (item.getDescription() != null) ? item.getDescription().replace('-', '.') : item.getTitle()
            ); // NOI18N

            button.setSelected(isSelected);

            if (defaultBorder == null) { // Windows or Metal

                // let the toolbar UI render the button according to "rollover"
                button.getModel().setRollover((index == itemList.rolloverIndex) && !isSelected);
            } else { // Mac OS X and others - set the border explicitly
                button.setBorder(((index == itemList.rolloverIndex) || isSelected) ? defaultBorder : emptyBorder);
            }

            //			if (icon!=null)
            //			{
            //				button.setHorizontalAlignment(showNames && iconSize == BASIC_ICONSIZE ?
            //									   SwingConstants.LEFT : SwingConstants.CENTER);
            //				button.setHorizontalTextPosition(iconSize == BASIC_ICONSIZE ?
            //									  SwingConstants.RIGHT : SwingConstants.CENTER);
            //				button.setVerticalTextPosition(iconSize == BASIC_ICONSIZE ?
            //									 SwingConstants.CENTER : SwingConstants.BOTTOM);
            //			}
            //			else
            //			{
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);

            //			}
            return rendererComponent;
        }
    }

    // ---------
    // list UI
    static class ViewSwitcherItemListUI extends BasicListUI {
        protected void updateLayoutState() {
            super.updateLayoutState();

            if (list.getLayoutOrientation() == JList.HORIZONTAL_WRAP) {
                Insets insets = list.getInsets();
                int listWidth = list.getWidth() - (insets.left + insets.right);

                if (listWidth >= cellWidth) {
                    int columnCount = listWidth / cellWidth;
                    cellWidth = listWidth / columnCount;
                }
            }
        }

        protected MouseInputListener createMouseInputListener() {
            return new ListMouseInputHandler();
        }

        private int getValidIndex(Point p) {
            int index = locationToIndex(list, p);

            return ((index >= 0) && getCellBounds(list, index, index).contains(p)) ? index : (-1);
        }

        private class ListMouseInputHandler extends MouseInputHandler {
            private int selIndex = -1;

            public void mousePressed(MouseEvent e) {
                if (getValidIndex(e.getPoint()) >= 0) {
                    selIndex = list.getSelectedIndex();
                    super.mousePressed(e);
                }
            }

            public void mouseDragged(MouseEvent e) {
                if (getValidIndex(e.getPoint()) >= 0) {
                    super.mouseDragged(e);
                }
            }

            public void mouseMoved(MouseEvent e) {
                mouseEntered(e);
            }

            public void mouseEntered(MouseEvent e) {
                if (list.isEnabled()) {
                    setRolloverIndex(getValidIndex(e.getPoint()));
                }
            }

            public void mouseExited(MouseEvent e) {
                if (list.isEnabled()) {
                    setRolloverIndex(-1);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (getValidIndex(e.getPoint()) >= 0) {
                    super.mouseReleased(e);

                    if ((selIndex > -1) && (list.getSelectedIndex() == selIndex)) {
                        list.removeSelectionInterval(selIndex, selIndex);
                    }
                }
            }

            private void setRolloverIndex(int index) {
                int oldIndex = ((ViewSwitcherItemList) list).rolloverIndex;

                if (index != oldIndex) {
                    ((ViewSwitcherItemList) list).rolloverIndex = index;

                    if (oldIndex > -1) {
                        Rectangle r = getCellBounds(list, oldIndex, oldIndex);

                        if (r != null) {
                            list.repaint(r.x, r.y, r.width, r.height);
                        }
                    }

                    if (index > -1) {
                        Rectangle r = getCellBounds(list, index, index);

                        if (r != null) {
                            list.repaint(r.x, r.y, r.width, r.height);
                        }
                    }
                }
            }
        }
    }
}

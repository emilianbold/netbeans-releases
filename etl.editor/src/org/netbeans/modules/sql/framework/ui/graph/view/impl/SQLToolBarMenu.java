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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorManager;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory;
import org.openide.nodes.Node;

/**
 * ToolBar drop down Menu for displaying menu of Operators.
 *
 * @author Ritesh Adval
 * @author Girish Patil
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SQLToolBarMenu extends JToggleButton {

    private static final Object _SEPARATOR = new Object();
    private SQLToolBar mToolBar;
    private IOperatorXmlInfoCategory mCategory;
    private IOperatorManager mManager;
    private List mMenuItemModelList;
    private List mMenuItemList;
    private ButtonPopupMenu mButtonMenu;
    private static transient final Logger mLogger = Logger.getLogger(SQLToolBarMenu.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public SQLToolBarMenu(IOperatorXmlInfoCategory catNode, IOperatorManager manager, SQLToolBar toolbar) {
        super();
        mCategory = catNode;
        mManager = manager;
        mToolBar = toolbar;
        mMenuItemModelList = new LinkedList();
        mMenuItemList = new LinkedList();
        Icon paletteIcon = null;
        paletteIcon = mCategory.getIcon();

        if (paletteIcon != null) {
            setIcon(paletteIcon);
        }

        addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (isSelected()) {
                    showButtonMenu();
                } else {
                    mButtonMenu.hide();
                }
            }
        });

        setContentAreaFilled(false);

        setRolloverEnabled(false);
        setMargin(new Insets(1, 5, 0, 12));
        setIconTextGap(2);

        addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                SQLToolBarMenu active = mToolBar.getActiveMenu();
                if ((active != null) && (active != SQLToolBarMenu.this)) {
                    setSelected(true);
                }
            }
        });
        String nbBundle = mLoc.t("BUND330: Operators");
        this.setToolTipText(catNode.getDisplayName() + " " + nbBundle.substring(15));
    }

    public void setToolBar(SQLToolBar group) {
        mToolBar = group;
    }

    public void addNotify() {
        super.addNotify();
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (mButtonMenu == null) {
            createNewButtonPopupMenu(parent);
        } else if (mButtonMenu.getOwner() != parent) {
            createNewButtonPopupMenu(parent);
        }
    }

    private void createNewButtonPopupMenu(Window parent) {
        if (mButtonMenu != null) {
            mButtonMenu.cleanup();
            mButtonMenu.dispose();
        }
        mButtonMenu = new ButtonPopupMenu(parent);
        Iterator menuItemIter = mMenuItemList.iterator();
        while (menuItemIter.hasNext()) {
            Object menuItem = menuItemIter.next();
            if (menuItem == _SEPARATOR) {
                mButtonMenu.addSeparator();
            } else {
                mButtonMenu.addButton((AbstractButton) menuItem);
            }
        }
    }

    public void showButtonMenu(Point location) {
        if (mButtonMenu == null) {
            throw new IllegalStateException("MenuButton has not been initialized yet");
        }
        mButtonMenu.setLocation(location);
        mButtonMenu.pack();
        mButtonMenu.show();
    }

    public void hideButtonMenu() {
        mButtonMenu.hide();
        setSelected(false);
    }

    public void showButtonMenu() {
        Point pt = getLocationOnScreen();
        pt.y += getSize().height;
        showButtonMenu(pt);
    }

    public SQLToolBarMenuItem addMenuItem(IOperatorXmlInfo item) {
        if (mMenuItemModelList.contains(item)) {
            return null;
        }
        mMenuItemModelList.add(item);
        SQLToolBarMenuItem stbmi = createMenuItem(item);
        mMenuItemList.add(stbmi);
        if (mButtonMenu != null) {
            mButtonMenu.addButton((AbstractButton) item);
        }

        return stbmi;
    }

    public int getMenuItemIndex(IOperatorXmlInfo item) {
        return mMenuItemModelList.indexOf(item);
    }

    public SQLToolBarMenuItem findMenuItem(IOperatorXmlInfo itemModel) {
        SQLToolBarMenuItem stbmi = null;
        int index = getMenuItemIndex(itemModel);
        if (index >= 0) {
            stbmi = (SQLToolBarMenuItem) mMenuItemList.get(index);
        }

        return stbmi;
    }

    public void addSeparator() {
        mMenuItemList.add(_SEPARATOR);
        if (mButtonMenu != null) {
            mButtonMenu.addSeparator();
        }
    }

    public void setEnabled(boolean enable) {
        if (this.isEnabled() == enable) {
            return;
        }
        super.setEnabled(enable);
        for (int i = 0; i < getComponentCount(); i++) {
            Component comp = getComponent(i);
            comp.setEnabled(enable);
        }
    }

    private class ButtonPopupMenu extends JWindow {

        private JPanel mButtonList;
        private Border mButtonBorder;
        private int mButtonHeight;
        private ComponentListener mComponentListener;

        public ButtonPopupMenu(Window parent) {
            super(parent);
            mButtonList = new JPanel();
            mButtonList.setBorder(BorderFactory.createLineBorder(Color.black));
            mButtonList.setLayout(new GridBagLayout());

            // The rollover border does not display correctly. It overlaps the
            // button text, which looks bad. So, we expand the button size by
            // giving it a large enough border. This border has a background color
            // to match the button background color. This allows the rollover rectangle
            // to size around the text correctly.
            Color background = mButtonList.getBackground();
            mButtonBorder = new MatteBorder(new Insets(2, 3, 3, 3), background);

            mButtonList.addFocusListener(mFocusListener);
            addFocusListener(mFocusListener);

            getContentPane().add(mButtonList);
            registerDefaultKeyActionBindings();

            mComponentListener = new ComponentListener() {

                public void componentHidden(ComponentEvent e) {
                    SQLToolBarMenu.this.setSelected(false);
                }

                public void componentMoved(ComponentEvent e) {
                    SQLToolBarMenu.this.setSelected(false);
                }

                public void componentResized(ComponentEvent e) {
                    SQLToolBarMenu.this.setSelected(false);
                }

                public void componentShown(ComponentEvent e) {
                    SQLToolBarMenu.this.setSelected(false);
                }
            };
            getOwner().addComponentListener(mComponentListener);

            addSeparator();

            String nbBundle1 = mLoc.t("BUND331: Settings...");
            AbstractButton setupButton = new JButton(nbBundle1.substring(15));
            setupButton.getAccessibleContext().setAccessibleName(nbBundle1.substring(15));
            addButton(1, setupButton);
            mButtonHeight = setupButton.getPreferredSize().height + 2;
            setupButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    mManager.show((Node) mCategory);
                }
            });
        }

        @SuppressWarnings("deprecation")
        @Override
        public void hide() {
            super.hide();
            if (mToolBar.getActiveMenu() == SQLToolBarMenu.this) {
                mToolBar.setActiveMenu(null);
            }
        }
        private ActionListener mActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SQLToolBarMenu.this.setSelected(false);
            }
        };
        private FocusListener mFocusListener = new FocusListener() {

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                        Component win = SwingUtilities.getRoot(owner);
                        if (win != ButtonPopupMenu.this) {
                            SQLToolBarMenu.this.setSelected(false);
                        }
                    }
                });
            }
        };
        private Action mNextFocusAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    if (owner == mButtonList.getComponent(i)) {
                        fMgr.focusNextComponent();
                        return;
                    }
                }
            }
        };
        private Action mPrevFocusAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    if (owner == mButtonList.getComponent(i)) {
                        fMgr.focusPreviousComponent();
                        return;
                    }
                }
            }
        };
        private Action mNextMenuAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                Component parent = SQLToolBarMenu.this.getParent();
                if (parent instanceof SQLToolBar) {
                    SQLToolBar parentGroup = (SQLToolBar) parent;
                    SQLToolBarMenu next = parentGroup.getNextMenu(SQLToolBarMenu.this);
                    if (next != null) {
                        setSelected(false);
                        next.setSelected(true);
                    }
                }
            }
        };
        private Action mPrevMenuAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                Component parent = SQLToolBarMenu.this.getParent();
                if (parent instanceof SQLToolBar) {
                    SQLToolBar parentGroup = (SQLToolBar) parent;
                    SQLToolBarMenu prev = parentGroup.getPreviousMenu(SQLToolBarMenu.this);
                    if (prev != null) {
                        setSelected(false);
                        prev.setSelected(true);
                    }
                }
            }
        };
        private Action mHideAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                SQLToolBarMenu.this.setSelected(false);
            }
        };
        private Action mDefaultButtonAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    Component c = mButtonList.getComponent(i);
                    ;
                    if (owner == c) {
                        ((AbstractButton) c).doClick(0);
                        return;
                    }
                }
            }
        };
        private MouseMotionListener mButtonFocusListener = new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getSource() instanceof Component) {
                    ((Component) e.getSource()).requestFocus();
                }
            }
        };

        @SuppressWarnings("deprecation")
        @Override
        public void show() {
            super.show();
            if (mButtonList.getComponentCount() > 0) {
                mButtonList.getComponent(0).requestFocus();
            }
            mToolBar.setActiveMenu(SQLToolBarMenu.this);
        }

        public void addButton(AbstractButton b) {
            addButton(b, mButtonList.getComponentCount() - 2);
        }

        public void addButton(AbstractButton b, int index) {
            if (index < 0) {
                index = 0;
            } else if (index > mButtonList.getComponentCount() - 2) {
                throw new IndexOutOfBoundsException("component count: " + (mButtonList.getComponentCount() - 2) + " insert index: " + index);
            }
            addButton(index, b);
        }

        private void addButton(int y, AbstractButton b) {
            initButton(b);
            addComponent(y, b);
            if (mButtonHeight > 0) {
                // We size all buttons to have the same height.
                // We also add an extra amount to the width so that the
                // rollover border has enough room to draw itself.
                Dimension bSize = new Dimension(b.getPreferredSize().width + 2, mButtonHeight);
                b.setPreferredSize(bSize);
                b.setMinimumSize(bSize);
            }
        }

        public void removeButton(AbstractButton b) {
            mButtonList.remove(b);
            unInitButton(b);
        }

        public void removeButton(int index) {
            AbstractButton b = (AbstractButton) mButtonList.getComponent(index);
            mButtonList.remove(index);
            unInitButton(b);
        }

        public void addSeparator() {
            Component sepa = new JSeparator(SwingConstants.HORIZONTAL);
            addComponent(mButtonList.getComponentCount() - 1, sepa);
        }

        public void removeAll() {
            int lastComponentIndex = mButtonList.getComponentCount() - 1;
            while (lastComponentIndex >= 0) {
                if (mButtonList.getComponent(lastComponentIndex) instanceof AbstractButton) {
                    removeButton(lastComponentIndex--);
                } else {
                    mButtonList.remove(lastComponentIndex--);
                }
            }
        }

        private void addComponent(int index, Component b) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = GridBagConstraints.RELATIVE;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(0, 10, 0, 10);
            mButtonList.add(b, c, index);
        }

        private void initButton(AbstractButton b) {
            b.setBorder(mButtonBorder);
            b.setRolloverEnabled(false);
            b.setBackground(mButtonList.getBackground());
            b.addFocusListener(mFocusListener);
            b.addMouseMotionListener(mButtonFocusListener);
            b.addActionListener(mActionListener);
        }

        private void unInitButton(AbstractButton b) {
            b.removeFocusListener(mFocusListener);
            b.removeMouseMotionListener(mButtonFocusListener);
            b.removeActionListener(mActionListener);
        }

        private void registerDefaultKeyActionBindings() {
            KeyStroke arrowUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            getRootPane().getActionMap().put("up", mPrevFocusAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowUp, "up");

            KeyStroke arrowDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            getRootPane().getActionMap().put("down", mNextFocusAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowDown, "down");

            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            getRootPane().getActionMap().put("esc", mHideAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "esc");

            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            getRootPane().getActionMap().put("enter", mDefaultButtonAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "enter");

            KeyStroke arrowRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
            getRootPane().getActionMap().put("right", mNextMenuAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowRight, "right");

            KeyStroke arrowLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
            getRootPane().getActionMap().put("left", mPrevMenuAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowLeft, "left");

        }

        public void cleanup() {
            if (mButtonList != null) {
                removeAll();
                getOwner().removeComponentListener(mComponentListener);
                mButtonList.removeFocusListener(mFocusListener);
                mButtonList = null;
                mPrevFocusAction = null;
                mNextFocusAction = null;
                mHideAction = null;
                mDefaultButtonAction = null;
                mNextMenuAction = null;
                mPrevMenuAction = null;
            }
        }
    }

    public void close() {
        if (mButtonMenu != null) {
            mButtonMenu.cleanup();
            mButtonMenu.dispose();
            mButtonMenu = null;
            mToolBar = null;
        }
    }
    Color mDisabledTextColor = null;

    // Draw a small down arrow over the right margin of the button
    public void paint(Graphics g) {
        super.paint(g);
        AbstractButton b = this;
        Dimension size = b.getSize();
        Rectangle viewRect = new Rectangle(size);

        /*
         * XXXXX XVX X
         */
        int px = viewRect.x + viewRect.width - 10;
        int py = viewRect.y + (viewRect.height) / 2;
        Polygon p = new Polygon();
        int cx = 4;
        while (cx >= 0) {
            p.addPoint(px, py);
            p.addPoint(px + cx, py);
            py++;
            px++;
            cx -= 2;
        }
        Color color = Color.black;
        if (!b.isEnabled()) {
            if (mDisabledTextColor == null) {
                mDisabledTextColor = UIManager.getColor("Button.disabledText");
            }
            color = mDisabledTextColor;
        }
        g.setColor(color);
        g.drawPolygon(p);
        g.fillPolygon(p);

    }

    /**
     * Returns JButton item to be displayed on the toolbar.
     *
     * @param itemData
     * @return
     */
    private SQLToolBarMenuItem createMenuItem(IOperatorXmlInfo itemData) {
        SQLToolBarMenuItem menuItem = new SQLToolBarMenuItem(itemData);
        return menuItem;
    }
}

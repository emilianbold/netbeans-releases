/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.core.output2.ui;

import java.awt.event.MouseEvent;
import javax.swing.border.Border;
import org.netbeans.core.output2.Controller;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.awt.TabbedPaneFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import javax.swing.plaf.TabbedPaneUI;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;

/**
 * A panel which, if more than one AbstractOutputTab is added to it, instead
 * adds additional views to an internal tabbed pane.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputWindow extends TopComponent implements ChangeListener, PropertyChangeListener {
    protected JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
    private static final String ICON_PROP = "tabIcon"; //NOI18N
    private JToolBar toolbar = null;
    private JPopupMenu popupMenu;
    
    /** Creates a new instance of AbstractOutputWindow */
    public AbstractOutputWindow() {
        pane.addChangeListener(this);
        pane.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, this);
        setFocusable(true);
        toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setFloatable(false);
        Insets ins = toolbar.getMargin();
        JButton sample = new JButton();
        sample.setBorderPainted(false);
        sample.setOpaque(false);
        sample.setText(null);
        sample.setIcon(new Icon() {
            public int getIconHeight() {
                return 16;
            }
            public int getIconWidth() {
                return 16;
            }
            public void paintIcon(Component c, Graphics g, int x, int y) {
            }
        });
        toolbar.add(sample);
        Dimension buttonPref = sample.getPreferredSize();
        Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
        toolbar.setMinimumSize(minDim);
        toolbar.setPreferredSize(minDim);
        toolbar.remove(sample);
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.WEST);
        toolbar.setBorder(new VariableRightBorder(pane));
        toolbar.setBorderPainted(true);

        popupMenu = new JPopupMenu();
        popupMenu.add(new Close());
        popupMenu.add(new CloseAll());
        popupMenu.add(new CloseOthers());
        pane.addMouseListener(new MouseUtils.PopupMouseAdapter() {

            @Override
            protected void showPopup(MouseEvent evt) {
                popupMenu.show(AbstractOutputWindow.this, evt.getX(), evt.getY());
            }
        });
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        if (TabbedPaneFactory.PROP_CLOSE.equals(pce.getPropertyName())) {
            AbstractOutputTab tab = (AbstractOutputTab) pce.getNewValue();
            closeRequest(tab);
        }
    }
    
    protected abstract void closeRequest(AbstractOutputTab tab);
    
    protected abstract void removed(AbstractOutputTab view);
    
    @Override
    protected void addImpl(Component c, Object constraints, int idx) {
        setFocusable(false);
        Component focusOwner =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean hadFocus = hasFocus() || isAncestorOf(focusOwner);
        
        synchronized (getTreeLock()) {
            if (c instanceof AbstractOutputTab) {
                AbstractOutputTab aop = getInternalTab();
                if (aop != null) {
                    if (aop == c) {
                        return;
                    }
                    super.remove(aop);
                    assert pane.getParent() != this;
                    pane.add(aop);
                    setTabIcon(aop, (Icon)aop.getClientProperty(ICON_PROP));
                    pane.add(c);
                    setTabIcon((AbstractOutputTab)c, (Icon)((AbstractOutputTab)c).getClientProperty(ICON_PROP));
                    
                    super.addImpl(pane, constraints, idx);
                    updateSingletonName(null);
                    revalidate();
                } else if (pane.getParent() == this) {
                    pane.add(c);
                    setTabIcon((AbstractOutputTab) c, (Icon)((AbstractOutputTab)c).getClientProperty(ICON_PROP));
                    revalidate();
                } else {
                    super.addImpl(c, constraints, idx);
                    setTabIcon((AbstractOutputTab) c, (Icon)((AbstractOutputTab)c).getClientProperty(ICON_PROP));
                    setToolbarButtons(((AbstractOutputTab)c).getToolbarButtons());
                    //#48819 - a bit obscure usecase, but revalidate() is call in the if branches above as well..
                    revalidate();
                }
                if (hadFocus) {
                    //Do not call c.requestFocus() directly, it can be
                    //discarded when adding tabs and focus will go to null.
                    //@see AbstractOutputWindow.requestFocus()
                    requestFocus();
                }
                
                return;
            }
            super.addImpl(c, constraints, idx);
        }
        if (getComponentCount() == 2 && getComponent(1) instanceof AbstractOutputTab) {
            updateSingletonName(getComponent(1).getName());
        }
        revalidate();
    }
    
    public final AbstractOutputTab[] getTabs() {
        ArrayList<AbstractOutputTab> al = 
                new ArrayList<AbstractOutputTab>(pane.getParent() == this ? pane.getTabCount() : getComponentCount());
        if (pane.getParent() == this) {
            int tabs = pane.getTabCount();
            for (int i=0; i < tabs; i++) {
                Component c = pane.getComponentAt(i);
                if (c instanceof AbstractOutputTab) {
                    al.add((AbstractOutputTab)c);
                }
            }
        } else {
            Component[] c = getComponents();
            for (int i=0; i < c.length; i++) {
                if (c[i] instanceof AbstractOutputTab) {
                    al.add((AbstractOutputTab)c[i]);
                }
            }
        }
        AbstractOutputTab[] result = new AbstractOutputTab[al.size()];
        return al.toArray(result);
    }
    
    
    @Override
    public void remove(Component c) {
        AbstractOutputTab removedSelectedView = null;
        synchronized (getTreeLock()) {
            if (c.getParent() == pane && c instanceof AbstractOutputTab) {
                if (c == pane.getSelectedComponent()) {
                    if (Controller.LOG) Controller.log("Selected view is being removed: " + c.getName());
                    removedSelectedView = (AbstractOutputTab) c;
                }
                checkWinXPLFBug();
                pane.remove(c);
                if (pane.getTabCount() == 1) {
                    Component comp = pane.getComponentAt(0);
                    pane.remove(comp);
                    super.remove(pane);
                    add(comp);
                    updateSingletonName(c.getName());
                    setToolbarButtons(((AbstractOutputTab)comp).getToolbarButtons());
                    revalidate();
                }
            } else {
                if (c == getSelectedTab()) {
                    removedSelectedView = (AbstractOutputTab) c;
                }
                super.remove(c);
                setToolbarButtons(new JButton[0]);
                updateSingletonName(null);
            }
            if (removedSelectedView != null) {
                fire(removedSelectedView);
            }
        }
        if (c instanceof AbstractOutputTab && c.getParent() == null) {
            removed((AbstractOutputTab) c);
        }
        if (getComponentCount() == 2 && getComponent(1) instanceof AbstractOutputTab) {
            updateSingletonName(getComponent(1).getName());
        }
        revalidate();
        setFocusable(getComponentCount() == 1);
    }
    
    private AbstractOutputTab getInternalTab() {
        Component[] c = getComponents();
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof AbstractOutputTab) {
                return (AbstractOutputTab) c[i];
            }
        }
        return null;
    }
    
    public final AbstractOutputTab getSelectedTab() {
        if (pane.getParent() == this) {
            return (AbstractOutputTab) pane.getSelectedComponent();
        } else {
            return getInternalTab();
        }
    }
    
    public void setSelectedTab(AbstractOutputTab op) {
        assert (op.getParent() == this || op.getParent() == pane);
        if (Controller.LOG) {
            Controller.log("SetSelectedTab: " + op + " parent is " + op.getParent());
        }
        if (pane.getParent() == this && op != pane.getSelectedComponent()) {
            pane.setSelectedComponent(op);
        }
        
        getActionMap().setParent(op.getActionMap());
    }
    
    public void setTabTitle(AbstractOutputTab tab, String name) {
        if (tab.getParent() == pane) {
            int index = pane.indexOfComponent(tab);
            if (Controller.LOG) {
                Controller.log("setTabTitle: #" + index + " '" + pane.getTitleAt(index) + "' -> '" + name + "'");
            }
            pane.setTitleAt(index, name);
        } else if (tab.getParent() == this) {
            updateSingletonName(name);
        }
        tab.setName(name);
    }
    
    public void setTabIcon(AbstractOutputTab tab, Icon icon) {
        if (icon != null) {
            tab.putClientProperty(ICON_PROP, icon);
            if (pane.indexOfComponent(tab) != -1) {
                pane.setIconAt(pane.indexOfComponent(tab), icon);
                pane.setDisabledIconAt(pane.indexOfComponent(tab), icon);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override public void requestFocus() {
        if (!isShowing()) {
            return;
        }
        AbstractOutputTab tab = getSelectedTab();
        if (tab != null && pendingFocusRunnable == null) {
            //Adding the tab may yet need to be processed, so escape the
            //current event loop via invokeLater()
            pendingFocusRunnable = new Runnable() {
                public void run() {
                    AbstractOutputTab tab = getSelectedTab();
                    if (tab != null) {
                        tab.requestFocus();
                    }
                    pendingFocusRunnable = null;
                }
            };
            SwingUtilities.invokeLater(pendingFocusRunnable);
        } else {
            super.requestFocus();
        }
    }
    
    private Runnable pendingFocusRunnable = null;
    
    /**
     * Updates the component name to include the name of a tab.  If passed null
     * arguments, should update the name to the default which does not include the
     * tab name.
     *
     * @param name A name for the tab
     */
    protected abstract void updateSingletonName(String name);
    
    
    private AbstractOutputTab lastKnownSelection = null;
    protected void fire(AbstractOutputTab formerSelection) {
        AbstractOutputTab selection = getSelectedTab();
        if (formerSelection != selection) {
            selectionChanged(formerSelection, selection);
            lastKnownSelection = selection;
            if (selection != null) {
                setToolbarButtons(selection.getToolbarButtons());
            } else {
                setToolbarButtons(new JButton[0]);
            }
        }
    }
    
    private void setToolbarButtons(JButton[] buttons) {
        toolbar.removeAll();
        for (int i = 0; i < buttons.length; i++) {
            toolbar.add(buttons[i]);
        }
        toolbar.revalidate();
        toolbar.repaint();
        
    }
    
    public void stateChanged(ChangeEvent e) {
        if (pane.getSelectedComponent() instanceof AbstractOutputPane) {
            ((AbstractOutputPane) pane.getSelectedComponent()).ensureCaretPosition();
        }
        fire(lastKnownSelection);
    }
    
    protected abstract void selectionChanged(AbstractOutputTab former, AbstractOutputTab current);
    
    private final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()) || //NOI18N
            UIManager.getLookAndFeel().getClass().getSuperclass().getName().indexOf("Synth") != -1; //NOI18N
    /**
     * Overridden to fill in the background color, since Synth/GTKLookAndFeel ignores
     * setOpaque(true).
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=43024
     */
    @Override
    public void paint(Graphics g) {
        if (isGtk) {
            //Presumably we can get this fixed for JDK 1.5.1
            Color c = getBackground();
            if (c == null) {
                c = java.awt.Color.WHITE;
            }
            g.setColor(c);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paint(g);
    }
    
    /**
     * Set next tab relatively to the given tab. If the give tab is the last one
     * the first is selected.
     *
     * @param tab relative tab
     */
    public final void selectNextTab(AbstractOutputTab tab) {
        AbstractOutputTab[] tabs = this.getTabs();
        if (tabs.length > 1) {
            int nextTabIndex = getSelectedTabIndex(tabs, tab) + 1;
            if (nextTabIndex > (tabs.length - 1)) {
                nextTabIndex = 0;
            }
            this.setSelectedTab(tabs[nextTabIndex]);
        }
    }
    
    /**
     * Set previous tab relatively to the given tab. If the give tab is the
     * first one the last is selected.
     *
     * @param tab relative tab
     */
    public final void selectPreviousTab(AbstractOutputTab tab) {
        AbstractOutputTab[] tabs = this.getTabs();
        if (tabs.length > 1) {
            int prevTabIndex = getSelectedTabIndex(tabs, tab) - 1;
            if (prevTabIndex < 0) {
                prevTabIndex = tabs.length - 1;
            }
            this.setSelectedTab(tabs[prevTabIndex]);
        }
    }
    
    private int getSelectedTabIndex(AbstractOutputTab[] tabs, AbstractOutputTab tab) {
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] == tab) {
                return i;
            }
        }
        return -1;
    }
    
    // JDK 1.5, Win L&F - removing a tab causes a relayout and that uses onl data in UI class,
    // causing it to throw ArrayOutofbounds if removing the last one.
    // hacking around it by resetting the bad old data before removal.
    // #56628
    private void checkWinXPLFBug() {
        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOi18N
            TabbedPaneUI ui = pane.getUI();
            try {
                Method method = ui.getClass().getDeclaredMethod("setRolloverTab", new Class[] {Integer.TYPE}); //NOI18N
                if (method != null) {
                    method.setAccessible(true);
                    method.invoke(ui, new Object[] { new Integer(-1) });
                    method.setAccessible(false);
                }
            } catch (Exception exc) {
                // well let's cross fingers and see..
            }
        }
    }
    
    private class VariableRightBorder implements Border {
        
        private JTabbedPane pane;
        
        public VariableRightBorder(JTabbedPane pane) {
            this.pane = pane;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (pane.getParent() != AbstractOutputWindow.this) {
                Color old = g.getColor();
                g.setColor(getColor());
                g.drawLine(x + width - 1, y, x + width - 1, y + height);
                g.setColor(old);
            }
        }
        
        public  Color getColor() {
            if (Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow");
                Color c2 = UIManager.getColor("control");
                return new Color((c1.getRed() + c2.getRed()) / 2,
                        (c1.getGreen() + c2.getGreen()) / 2,
                        (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow");
            }
        }
        
        public Insets getBorderInsets(Component c) {
            if (pane.getParent() == AbstractOutputWindow.this) {
                return new Insets(0,0,0,0);
            }
            return new Insets(0, 0, 0, 2);
        }
        
        public boolean isBorderOpaque() {
            return true;
        }
        
    }

    private void closeOtherTabs() {
        AbstractOutputTab[] tabs = getTabs();
        AbstractOutputTab curTab = getSelectedTab();
        for (int i = 0; i < tabs.length; i++) {
            AbstractOutputTab tab = tabs[i];
            if (tab != curTab) {
                closeRequest(tab);
            }
        }
    }

    private void closeAllTabs() {
        AbstractOutputTab[] tabs = getTabs();
        for (int i = 0; i < tabs.length; i++) {
            closeRequest(tabs[i]);
        }
    }

    private class Close extends AbstractAction {

        public Close() {
            super(NbBundle.getMessage(AbstractOutputWindow.class, "LBL_Close"));
        }

        public void actionPerformed(ActionEvent e) {
             closeRequest(getSelectedTab());
        }
    }

    private class CloseAll extends AbstractAction {

        public CloseAll() {
            super(NbBundle.getMessage(AbstractOutputWindow.class, "LBL_CloseAll"));
        }

        public void actionPerformed(ActionEvent e) {
            closeAllTabs();
        }
    }

    private class CloseOthers extends AbstractAction {

        public CloseOthers() {
            super(NbBundle.getMessage(AbstractOutputWindow.class, "LBL_CloseOthers"));
        }

        public void actionPerformed(ActionEvent e) {
            closeOtherTabs();
        }
    }
}

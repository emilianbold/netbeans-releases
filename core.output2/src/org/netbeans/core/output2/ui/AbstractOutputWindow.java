/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * AbstractOutputWindow.java
 *
 * Created on May 14, 2004, 10:22 PM
 */

package org.netbeans.core.output2.ui;

import org.netbeans.core.output2.Controller;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * A panel which, if more than one AbstractOutputTab is added to it, instead
 * adds additional views to an internal tabbed pane.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputWindow extends TopComponent implements ChangeListener, PropertyChangeListener {
    protected JTabbedPane pane = new CloseButtonTabbedPane();

    /** Creates a new instance of AbstractOutputWindow */
    public AbstractOutputWindow() {
        pane.addChangeListener (this);
        pane.addPropertyChangeListener (CloseButtonTabbedPane.PROP_CLOSE, this);
        setFocusable(true);
        setBackground (UIManager.getColor("text"));
    }

    public void propertyChange (PropertyChangeEvent pce) {
        if (CloseButtonTabbedPane.PROP_CLOSE.equals(pce.getPropertyName())) {
            AbstractOutputTab tab = (AbstractOutputTab) pce.getNewValue();
            closeRequest(tab);
        }
    }

    protected abstract void closeRequest(AbstractOutputTab tab);

    protected abstract void removed (AbstractOutputTab view);

    protected void addImpl(Component c, Object constraints, int idx) {
        setFocusable (false);
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
                    super.remove (aop);
                    assert pane.getParent() != this;
                    pane.add (aop);
                    pane.add (c);
                    
                    super.addImpl (pane, constraints, idx);
                    updateSingletonName(null);
                    revalidate();
                } else if (pane.getParent() == this) {
                    pane.add (c);
                    revalidate();
                } else {
                    super.addImpl (c, constraints, idx);
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
        if (getComponentCount() == 1 && getComponent(0) instanceof AbstractOutputTab) {
            updateSingletonName(getComponent(0).getName());
        }
        revalidate();
    }
    
    public final AbstractOutputTab[] getTabs() {
        ArrayList al = new ArrayList (pane.getParent() == this ? pane.getTabCount() : getComponentCount());
        if (pane.getParent() == this) {
            int tabs = pane.getTabCount();
            for (int i=0; i < tabs; i++) {
                Component c = pane.getComponentAt(i);
                if (c instanceof AbstractOutputTab) {
                    al.add(c);
                }
            }
        } else {
            Component[] c = getComponents();
            for (int i=0; i < c.length; i++) {
                if (c[i] instanceof AbstractOutputTab) {
                    al.add(c[i]);
                }
            }
        }
        AbstractOutputTab[] result = new AbstractOutputTab[al.size()];
        result = (AbstractOutputTab[]) al.toArray(result);
        return result;
    }
    
    
    public void remove (Component c) {
        AbstractOutputTab removedSelectedView = null;
        synchronized (getTreeLock()) {
        if (c.getParent() == pane && c instanceof AbstractOutputTab) {
                if (c == pane.getSelectedComponent()) {
                    if (Controller.log) Controller.log ("Selected view is being removed: " + c.getName());
                    removedSelectedView = (AbstractOutputTab) c;
                }
                pane.remove(c);
                if (pane.getTabCount() == 1) {
                    Component comp = pane.getComponentAt(0);
                    pane.remove(comp);
                    super.remove(pane);
                    add(comp);
                    updateSingletonName(c.getName());
                    revalidate();
                }
            } else {
                if (c == getSelectedTab()) {
                    removedSelectedView = (AbstractOutputTab) c;
                }
                super.remove(c);
                updateSingletonName(null);
            }
            if (removedSelectedView != null) {
                fire(removedSelectedView);
            }
        }
        if (c instanceof AbstractOutputTab && c.getParent() == null) {
            removed ((AbstractOutputTab) c);
        } 
        if (getComponentCount() == 1 && getComponent(0) instanceof AbstractOutputTab) {
            updateSingletonName(getComponent(0).getName());
        }        
        revalidate();
        setFocusable (getComponentCount() == 0);
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
    
    public void setSelectedTab (AbstractOutputTab op) {
        assert (op.getParent() == this || op.getParent() == pane);
        if (Controller.log) {
            Controller.log("SetSelectedTab: " + op + " parent is " + op.getParent());
        }
        if (pane.getParent() == this && op != pane.getSelectedComponent()) {
            pane.setSelectedComponent(op);
        }
        
        getActionMap().setParent (op.getActionMap ());
    }

    public void setTabTitle (AbstractOutputTab tab, String name) {
        if (tab.getParent() == pane) {
            pane.setTitleAt (pane.indexOfComponent(tab), name);
        } else if (tab.getParent() == this) {
            updateSingletonName(name);
        }
        tab.setName(name);
    }
    
    public void requestFocus() {
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

    public void doLayout() {
        Insets ins = getInsets();
        Component c = null;
        
        if (pane.getParent() == this) {
            c = pane;
        } else if (getComponentCount() > 0) {
            c = getComponent(0);
        }
        if (c != null) {
            c.setBounds (ins.left, ins.top, getWidth() - (ins.left + ins.right),
                getHeight() - (ins.top + ins.bottom));
        }
    }

    private AbstractOutputTab lastKnownSelection = null;
    protected void fire(AbstractOutputTab formerSelection) {
        AbstractOutputTab selection = getSelectedTab();
        if (formerSelection != selection) {
            selectionChanged (formerSelection, selection);
            lastKnownSelection = selection;
        }
    }

    public void stateChanged(ChangeEvent e) {
        fire (lastKnownSelection);
    }

    protected abstract void selectionChanged (AbstractOutputTab former, AbstractOutputTab current);
    
    private final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()) || //NOI18N
        UIManager.getLookAndFeel().getClass().getSuperclass().getName().indexOf ("Synth") != -1; //NOI18N
    /**
     * Overridden to fill in the background color, since Synth/GTKLookAndFeel ignores
     * setOpaque(true).
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=43024
     */
    public void paint (Graphics g) {
        if (isGtk) {
            //Presumably we can get this fixed for JDK 1.5.1
            Color c = getBackground();
            if (c == null) c = Color.WHITE;
            g.setColor (c);
            g.fillRect (0, 0, getWidth(), getHeight());
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

    
}

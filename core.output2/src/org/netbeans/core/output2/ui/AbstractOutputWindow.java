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
        setFocusable(false);
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
                    updateSingletonName(null, null);
                    revalidate();
                } else if (pane.getParent() == this) {
                    pane.add (c);
                    revalidate();
                } else {
                    super.addImpl (c, constraints, idx);
                }
                return;
            }
            super.addImpl(c, constraints, idx);
        }
        if (getComponentCount() == 1 && getComponent(0) instanceof AbstractOutputTab) {
            updateSingletonName((AbstractOutputTab) getComponent(0), 
                getComponent(0).getName());
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
                    updateSingletonName((AbstractOutputTab) c, c.getName());
                    revalidate();
                }
            } else {
                if (c == getSelectedTab()) {
                    removedSelectedView = (AbstractOutputTab) c;
                }
                super.remove(c);
                updateSingletonName(null, null);
            }
            if (removedSelectedView != null) {
                fire(removedSelectedView);
            }
        }
        if (c instanceof AbstractOutputTab && c.getParent() == null) {
            removed ((AbstractOutputTab) c);
        } 
        if (getComponentCount() == 1 && getComponent(0) instanceof AbstractOutputTab) {
            updateSingletonName((AbstractOutputTab) getComponent(0), 
                getComponent(0).getName());
        }        
        revalidate();
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
    }

    public void setTabTitle (AbstractOutputTab tab, String name) {
        if (tab.getParent() == pane) {
            pane.setTitleAt (pane.indexOfComponent(tab), name);
        } else if (tab.getParent() == this) {
            updateSingletonName(tab, name);
        }
        tab.setName(name);
    }

    /**
     * Updates the component name to include the name of a tab.  If passed null
     * arguments, should update the name to the default which does not include the
     * tab name.
     *
     * @param tab A tab
     * @param name A name for the tab
     */
    protected abstract void updateSingletonName(AbstractOutputTab tab, String name);

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
}

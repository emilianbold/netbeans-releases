/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.ui;


import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.dnd.DnDConstants;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.windows.TopComponent;


/** Helper class which handles <code>Tabbed</code> component inside
 * <code>ModeComponent</code>.
 *
 * @author  Peter Zavadsky
 */
final class TabbedHandler implements ChangeListener, PropertyChangeListener {

    /** Associated mode container. */
    private final ModeView modeView;
    
    /** Component which plays tabbed. */
    private final Tabbed tabbed;

    /** Ignore own changes. */
    private boolean ignoreChange = false;
    
    
    /** Creates new SimpleContainerImpl */
    public TabbedHandler(ModeView modeView, int kind) {
        this.modeView = modeView;

        // Manages popup showing, maximization and activation.
        TabbedListener.install();

        tabbed = createTabbedComponent(kind);
        // XXX so the focus is not 'fallen out' outside the window.
        // E.g. when switching tabs in mode.
        ((Container)tabbed).setFocusCycleRoot(true);
    }

    
    /** Gets tabbed container on supplied position */
    private Tabbed createTabbedComponent(int kind) {
        Tabbed tabbed;

        // For debbugging of the tabbed component only!
        // XXX Not supported switches over releases.
        boolean oldTabs   = Constants.SWITCH_OLD_TABS;
        boolean oldView   = Constants.SWITCH_OLD_TABS_VIEW;
        boolean oldEditor = Constants.SWITCH_OLD_TABS_EDITOR;
        
        if(kind == Constants.MODE_KIND_EDITOR) {
            if(oldTabs || oldEditor) {
                tabbed = new CloseButtonTabbedPane();
            } else {
                tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
            }
            tabbed.setTabPlacement(JTabbedPane.TOP);
        } else {
            if(oldTabs || oldView) {
                tabbed = new CloseButtonTabbedPane();
            } else {
                tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
            }
            tabbed.setTabPlacement(JTabbedPane.BOTTOM);
        }
        
        tabbed.addChangeListener(this);
        tabbed.addPropertyChangeListener(Tabbed.PROP_TOPCOMPONENT_CLOSED, this);
        
        return tabbed;
    }

    
    public Component getComponent() {
        return (Component)tabbed;
    }

    /** Adds given top component to this container. */
    public void addTopComponent(TopComponent tc, int kind) {
        addTCIntoTab(tc, kind);
    }
    

    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        ignoreChange = true;
        try {
            tabbed.setTopComponents(tcs, selected);
        } finally {
            ignoreChange = false;
        }
    }
    
    /** Adds TopComponent into specified tab. */
    private void addTCIntoTab(TopComponent tc, int kind) {
        
        if(containsTC(tabbed, tc)) {
            return;
        }

        Image icon = tc.getIcon();
        
        try {
            ignoreChange = true;
            String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            if(title == null) {
                title = ""; // NOI18N
            }
            tabbed.addTopComponent(
                title + CloseButtonTabbedPane.TAB_NAME_TRAILING_SPACE,
                icon == null ? null : new ImageIcon(icon),
                tc, tc.getToolTipText());
        } finally {
            ignoreChange = false;
        }
    }
    
    /** Inserts TC into specified atb at specified position. */
    private void insertTCIntoTab(TopComponent tc, int position, int kind) {

        if(containsTC(tabbed, tc)) {
            return;
        }

        // #24864. Check index validity.
        int tabCount = tabbed.getTopComponentCount();
        if(position > tabCount) {
            position = tabCount;
        };
        
        Image icon = tc.getIcon();
        try {
            ignoreChange = true;
            String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            if(title == null) {
                title = ""; // NOI18N
            }
            tabbed.insertTopComponent(
                title + CloseButtonTabbedPane.TAB_NAME_TRAILING_SPACE,
                icon == null ? null : new ImageIcon(icon),
                tc, tc.getToolTipText(), position);
        } finally {
            ignoreChange = false;
        }
        
    }

    /** Checks whether the tabbedPane already contains the component. */
    private static boolean containsTC(Tabbed tabbed, TopComponent tc) {
        return Arrays.asList(tabbed.getTopComponents()).contains(tc);
    }
    
    /** Removes TopComponent from this container. */
    public void removeTopComponent(TopComponent tc) {
        removeTCFromTab(tc); 
    }
    


    /** Removes TC from tab. */
    private void removeTCFromTab (TopComponent tc) {
        if(tabbed.indexOfTopComponent(tc) != -1) {
            try {
                ignoreChange = true;
                tabbed.removeTopComponent(tc);
            } finally {
                ignoreChange = false;
            }

            //Bugfix #27644: Remove reference from TopComponent's accessible context
            //to our tabbed pane.
            tc.getAccessibleContext().setAccessibleParent(null);
        }
    }
    
    /** Called when icon of some component in this multi frame has changed  */
    public void topComponentIconChanged(TopComponent tc) {
        int index = tabbed.indexOfTopComponent(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setIconAt(index, new ImageIcon(tc.getIcon()));
    }
    
    /** Called when the name of some component has changed  */
    public void topComponentNameChanged(TopComponent tc, int kind) {
        int index = tabbed.indexOfTopComponent(tc);
        if (index < 0) {
            return;
        }
        
        String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
        if(title == null) {
            title = ""; // NOI18N
        }
        tabbed.setTitleAt(index, title + CloseButtonTabbedPane.TAB_NAME_TRAILING_SPACE);
    }
    
    public void topComponentToolTipChanged(TopComponent tc) {
        int index = tabbed.indexOfTopComponent(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setToolTipTextAt(index, tc.getToolTipText());
    }
    
    /** Sets selected <code>TopComponent</code>.
     * Ensures GUI components to set accordingly. */
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc == null || tc == getSelectedTopComponent()) {
            return;
        }
        
        if(tabbed.indexOfTopComponent(tc) >= 0) {
            try {
                ignoreChange = true;
                tabbed.setSelectedTopComponent(tc);
            } finally {
                ignoreChange = false;
            }

            if(tabbed instanceof JTabbedPane) {
                // [PENDING] XXX #24422. Workaround to ensure the unselected
                // components are hidden. 
                // Needs to be found out which ensureTCisVisible is at wrong place.
                
                //XXX this may not work anymore, ensureTCisVisible is gone - Tim
                TopComponent[] tcs = tabbed.getTopComponents();
                for(int i = 0; i < tcs.length; i++) {
                    if(tcs[i] != tc && tcs[i].isVisible()) {
                        tcs[i].setVisible(false);
                   }
                }
            }
        }
    }
    
    public TopComponent getSelectedTopComponent() {
        return tabbed.getSelectedTopComponent();
    }

    public TopComponent[] getTopComponents() {
        return tabbed.getTopComponents();
    }

    public void setActive(boolean active) {
        tabbed.setActive(active);
    }
    
    ///////////////////
    // ChangeListener
    public void stateChanged(ChangeEvent evt) {
        if(ignoreChange || evt.getSource() != tabbed) {
            return;
        }
        
        TopComponent selected = tabbed.getSelectedTopComponent();
        modeView.getController().userSelectedTab(modeView, (TopComponent)selected);
    }
    
    ////////////////////
    // PropertyChangeListener
    /** Listens on proeptry TOP_COPOMPONENT_CLOSED and informs user observer. */
    public void propertyChange(PropertyChangeEvent evt) {
        TopComponent tc = (TopComponent)evt.getNewValue();
        modeView.getController().userClosedTopComponent(modeView, tc);
    }
    
    // DnD>>
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
    Point startingPoint, boolean attachingPossible) {
        return tabbed.getIndicationForLocation(location, startingTransfer,
                                            startingPoint, attachingPossible);
    }
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        return tabbed.getConstraintForLocation(location, attachingPossible);
    }
    // DnD<<


}


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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.actions.CloseAllDocumentsAction;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.AWTEventListener;


/** Helper class which handles <code>Tabbed</code> component inside
 * <code>ModeComponent</code>.
 *
 * @author  Peter Zavadsky
 */
final class TabbedHandler implements ChangeListener, ActionListener {

    /** Associated mode container. */
    private final ModeView modeView;
    
    /** Component which plays tabbed. */
    private final Tabbed tabbed;

    /** Ignore own changes. */
    private boolean ignoreChange = false;
    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    private static ActivationManager activationManager = null;

    /** Creates new SimpleContainerImpl */
    public TabbedHandler(ModeView modeView, int kind) {
        this.modeView = modeView;

        synchronized (TabbedHandler.class) {
            if (activationManager == null) {
                activationManager = new ActivationManager();
                Toolkit.getDefaultToolkit().addAWTEventListener(
                    activationManager, AWTEvent.MOUSE_EVENT_MASK);
            }
        }

        tabbed = createTabbedComponent(kind);

        // E.g. when switching tabs in mode.
        ((Container)tabbed).setFocusCycleRoot(true);
    }

    
    /** Gets tabbed container on supplied position */
    private Tabbed createTabbedComponent(int kind) {
        Tabbed tabbed;

        if(kind == Constants.MODE_KIND_EDITOR) {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
        } else {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
        }
        
        tabbed.addChangeListener(this);
        tabbed.addActionListener(this);

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
                title,
                icon == null ? null : new ImageIcon(icon),
                tc, tc.getToolTipText());
        } finally {
            ignoreChange = false;
        }
    }

    /** Checks whether the tabbedPane already contains the component. */
    private static boolean containsTC(Tabbed tabbed, TopComponent tc) {
        return tabbed.indexOf(tc) != -1;
    }
    
    /** Removes TopComponent from this container. */
    public void removeTopComponent(TopComponent tc) {
        removeTCFromTab(tc); 
    }

    /** Removes TC from tab. */
    private void removeTCFromTab (TopComponent tc) {
        if(tabbed.indexOf(tc) != -1) {
            try {
                ignoreChange = true;
                tabbed.removeComponent(tc);
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
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        tabbed.setIconAt(index, new ImageIcon(tc.getIcon()));
    }
    
    /** Called when the name of some component has changed  */
    public void topComponentNameChanged(TopComponent tc, int kind) {
        int index = tabbed.indexOf(tc);
        if (index < 0) {
            return;
        }
        
        String title = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
        if(title == null) {
            title = ""; // NOI18N
        }
        tabbed.setTitleAt (index, title);
    }
    
    public void topComponentToolTipChanged(TopComponent tc) {
        int index = tabbed.indexOf(tc);
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
        
        if(tabbed.indexOf(tc) >= 0) {
            try {
                ignoreChange = true;
                tabbed.setSelectedComponent(tc);
            } finally {
                ignoreChange = false;
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
    
    // DnD>>
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
    Point startingPoint, boolean attachingPossible) {
        return tabbed.getIndicationForLocation(location, startingTransfer,
                                            startingPoint, attachingPossible);
    }
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        return tabbed.getConstraintForLocation(location, attachingPossible);
    }

    public void actionPerformed(ActionEvent e) {
        if (e instanceof TabActionEvent) {
            TabActionEvent tae = (TabActionEvent) e;
            String cmd = tae.getActionCommand();
            tae.consume();
            if (TabbedContainer.COMMAND_CLOSE == cmd) { //== test is safe here
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(tae.getTabIndex());
                if (tc == null) {
                    throw new IllegalStateException ("Component to be closed " +
                        "is null at index " + tae.getTabIndex());
                }
                modeView.getController().userClosedTopComponent(modeView, tc);
            } else if (TabbedContainer.COMMAND_POPUP_REQUEST == cmd) {
                handlePopupMenuShowing(tae.getMouseEvent(), tae.getTabIndex());
            } else if (TabbedContainer.COMMAND_MAXIMIZE == cmd) {
                handleMaximization(tae.getMouseEvent(), tae.getTabIndex());
            } else if (TabbedContainer.COMMAND_CLOSE_ALL == cmd) {
                ActionUtils.closeAllDocuments();
            } else if (TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS == cmd) {
                TopComponent tc = (TopComponent) tabbed.getTopComponentAt(tae.getTabIndex());
                ActionUtils.closeAllExcept(tc);
            }
            //Pin button handling here
        }
    }

    /** Possibly invokes popup menu. */
    public static void handlePopupMenuShowing(MouseEvent e, int idx) {
        Component c = (Component) e.getSource();
        while (c != null && !(c instanceof Tabbed))
            c = c.getParent();
        if (c == null) {
            return;
        }
        final Tabbed tab = (Tabbed) c;

        final Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), (Component)tab);

        final int clickTab = idx;
        if (clickTab < 0) {
            return;
        }

        TopComponent tc = tab.getTopComponentAt(clickTab);
        if(tc == null) {
            return;
        }

        showPopupMenu(
            Utilities.actionsToPopup(tc.getActions(), tc.getLookup()),
            p,
            (Component)tab);
    }

    /** Shows given popup on given coordinations and takes care about the
     * situation when menu can exceed screen limits */
    private static void showPopupMenu (JPopupMenu popup, Point p, Component comp) {
        if (NO_POPUP_PLACEMENT_HACK) {
            popup.show(comp, p.x, p.y);
            return;
        }

        SwingUtilities.convertPointToScreen (p, comp);
        Dimension popupSize = popup.getPreferredSize ();
        Rectangle screenBounds = Utilities.getUsableScreenBounds(comp.getGraphicsConfiguration());

        if (p.x + popupSize.width > screenBounds.x + screenBounds.width) {
            p.x = screenBounds.x + screenBounds.width - popupSize.width;
        }
        if (p.y + popupSize.height > screenBounds.y + screenBounds.height) {
            p.y = screenBounds.y + screenBounds.height - popupSize.height;
        }

        SwingUtilities.convertPointFromScreen (p, comp);
        popup.show(comp, p.x, p.y);
    }

    /** Possibly invokes the (un)maximization. */
    public static void handleMaximization(MouseEvent e, int idx) {
        Component c = (Component) e.getSource();
        while (c != null && !(c instanceof Tabbed))
            c = c.getParent();
        if (c == null) {
            return;
        }
        final Tabbed tab = (Tabbed) c;

        TopComponent tc = tab.getTopComponentAt(idx);
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl)wm.findMode(tc);
        if(mode != null) {
            ModeImpl maximizedMode = wm.getMaximizedMode();
            if(maximizedMode == null) {
                wm.setMaximizedMode(mode);
            } else if(mode == maximizedMode) {
                wm.setMaximizedMode(null);
            }
        }
    }

    /** Well, we can't totally get rid of AWT event listeners - this is what
     * keeps track of the activated mode. */
    private static class ActivationManager implements AWTEventListener {
        public void eventDispatched(AWTEvent e) {
            if(e.getID() == MouseEvent.MOUSE_PRESSED) {
                handleActivation((MouseEvent) e);
            }
        }


    //
    /* XXX(-ttran) when the split container contains two TopComponents say TC1
     * and TC2.  If TC2 itself does not accept focus or the user clicks on one
     * of TC2's child compoennts which does not accept focus, then the whole
     * split container is activated.  It in turn may choose to activate TC1 not
     * TC2.  This is a very annoying problem if TC1 is an Explorer and TC2 is
     * the global property sheet.  The user clicks on the property sheet but
     * the Explorer gets activated which has a different selected node than the
     * one attached to the property sheet at that moment.  The contents of the
     * property sheet is updated immediately after the mouse click.  For more
     * details see <http://www.netbeans.org/issues/show_bug.cgi?id=11149>
     *
     * What follows here is a special hack for mouse click on _any_
     * TopComponent.  The hack will cause the nearest upper TopComponent in the
     * AWT hieararchy to be activated on MOUSE_PRESSED on any of its child
     * components.  This behavior is compatible with all window managers I can
     * imagine.
     */
    private void handleActivation(MouseEvent evt) {
        Component comp = (Component) evt.getSource();

        if (!(comp instanceof Component)) {
            return;
        }

        while (comp != null && !(comp instanceof ModeComponent)) {
            comp = comp.getParent();
        }

        if (comp instanceof ModeComponent) {
            ModeView modeView = ((ModeComponent)comp).getModeView();
            modeView.getController().userActivatedModeView(modeView);
        }
    }

    }
}


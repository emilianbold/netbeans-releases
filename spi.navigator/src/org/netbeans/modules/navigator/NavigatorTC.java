/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.navigator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Navigator TopComponent. Simple visual envelope for navigator graphics
 * content. Behaviour is delegated and separated into NavigatorController.
 *
 * @author Dafe Simonek
 */
class NavigatorTC extends TopComponent {
    
    /** singleton instance */
    private static NavigatorTC instance;
    
    /** A TopComponent which was active in winsys before navigator */
    private Reference lastActivatedRef;
    /** Currently active panel in navigator (or null if empty) */
    private NavigatorPanel selectedPanel;
    /** A list of panels currently available (or null if empty) */
    private List panels;
    /** Controller, controls behaviour and reacts to user actions */
    private NavigatorController controller;
    /** label signalizing no available providers */
    private final JLabel notAvailLbl = new JLabel(
            NbBundle.getMessage(NavigatorTC.class, "MSG_NotAvailable")); //NOI18N
   /** special lookup for naviagtor TC */
    private Lookup navTCLookup;
    
    /** Creates new NavigatorTC, singleton */
    private NavigatorTC() {
        initComponents();
        
        setName(NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator")); //NOI18N
        setIcon(Utilities.loadImage("org/netbeans/modules/navigator/resources/navigator.png")); //NOI18N        
        // accept focus when empty to work correctly in nb winsys
        setFocusable(true);
        // special title for sliding mode
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", getName());
        
        notAvailLbl.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailLbl.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailLbl.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailLbl.setOpaque(true);
        
        // empty initially
        setToEmpty();
    }

    /** Singleton accessor */
    public static final NavigatorTC getInstance () {
        if (instance == null) {
            instance = (NavigatorTC)WindowManager.getDefault().
                        findTopComponent("navigatorTC"); //NOI18N
            if (instance == null) {
                // shouldn't happen under normal conditions
                instance = new NavigatorTC();
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    "Could not locate the navigator component via its winsys id"); //NOI18N
            }
        }
        return instance;
    }

    /** Shows given navigator panel's component
     */
    public void setSelectedPanel (NavigatorPanel panel) {
        int panelIdx = panels.indexOf(panel);
        assert panelIdx != -1 : "Panel to select is not available"; //NOI18N
        
        this.selectedPanel = panel;
        ((CardLayout)contentArea.getLayout()).show(contentArea, String.valueOf(panelIdx));
    }
    
    /** Returns panel currently selected.
     * @return Panel currently selected or null if navigator is empty
     */
    public NavigatorPanel getSelectedPanel () {
        return selectedPanel;
    }
    
    /** List of panels currently contained in navigator component.
     * @return List of NavigatorPanel instances or null if navigator is empty
     */
    public List getPanels () {
        return panels;
    }
    
    /** Sets content of navigator to given panels, selecting the first one
     */ 
    public void setPanels (List panels) {
        this.panels = panels;
        // no panel, so make UI look empty
        if (panels == null || panels.size() == 0) {
            selectedPanel = null;
            setToEmpty();
        } else {
            // clear regular content 
            contentArea.removeAll();
            panelSelector.removeAllItems();
            // fill with new content
            NavigatorPanel curPanel = null;
            int i = 0;
            for (Iterator iter = panels.iterator(); iter.hasNext(); i++) {
                curPanel = (NavigatorPanel)iter.next();
                panelSelector.addItem(curPanel.getDisplayName());
                contentArea.add(curPanel.getComponent(), String.valueOf(i));
                if (i == 0) {
                    selectedPanel = curPanel;
                }
            }
            // show if was hidden
            resetFromEmpty();
        }
    }
    
    /** Returns combo box, UI for selecting proper panels */
    public JComboBox getPanelSelector () {
        return panelSelector;
    }
    
    // Window System related methods >>

    public String preferredID () {
        return "navigatorTC"; //NOI18N
    }

    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }

    /** Overriden to remember top component which was active before Navigator,
     * to be ale to jump back
     */
    public void requestActive () {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(tc != null && tc != this) {
            lastActivatedRef = new WeakReference(tc);
        }
        super.requestActive();
    }
    
    /** Overriden to pass focus directly into content panel */
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        boolean result = false;
        if (selectedPanel != null) {
            result = selectedPanel.getComponent().requestFocusInWindow();
        }
        return result;
    }

    /** Defines nagivator Help ID */
    public HelpCtx getHelpCtx () {
        return new HelpCtx("navigator.java");
    }

    /** Just delegates to controller */
    public void componentOpened () {
        getController().navigatorTCOpened();
    }
    
    /** Just delegates to controller */
    public void componentClosed () {
        getController().navigatorTCClosed();
    }
    
    // << Window system

    
    /** Combines default Lookup of TC with lookup from active navigator
     * panel.
     */
    public Lookup getLookup() {
        if (navTCLookup == null) {
            Lookup defaultLookup = super.getLookup();
            Lookup clientLookup = getController().getPanelLookup();
            navTCLookup = new ProxyLookup(
                    new Lookup [] { defaultLookup, clientLookup }
            ); 
        }
        return navTCLookup;
    }
    
    /*************** private stuff ************/
    
    /** Accessor for controller which controls UI behaviour */
    private NavigatorController getController () {
        if (controller == null) {
            controller = new NavigatorController(this);
        }
        return controller;
    }
    
    /** Removes regular UI content and sets UI to empty state */
    private void setToEmpty () {
        if (notAvailLbl.isShowing()) {
            // already empty
            return;
        }
        remove(panelSelector);
        remove(contentArea);
        add(notAvailLbl, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /** Puts regular UI content back */
    private void resetFromEmpty () {
        if (contentArea.isShowing()) {
            // content already shown
        }
        remove(notAvailLbl);
        add(panelSelector, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        panelSelector = new javax.swing.JComboBox();
        contentArea = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        add(panelSelector, java.awt.BorderLayout.NORTH);

        contentArea.setLayout(new java.awt.CardLayout());

        add(contentArea, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentArea;
    private javax.swing.JComboBox panelSelector;
    // End of variables declaration//GEN-END:variables

    
    
    
}

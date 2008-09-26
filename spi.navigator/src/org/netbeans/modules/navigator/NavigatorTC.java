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

package org.netbeans.modules.navigator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
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
public final class NavigatorTC extends TopComponent {
    
    /** singleton instance */
    private static NavigatorTC instance;
    
    /** Currently active panel in navigator (or null if empty) */
    private NavigatorPanel selectedPanel;
    /** A list of panels currently available (or null if empty) */
    private List<NavigatorPanel> panels;
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
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/navigator/resources/navigator.png")); //NOI18N
        // accept focus when empty to work correctly in nb winsys
        setFocusable(true);
        // special title for sliding mode
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", getName());
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NavigatorTC.class, "ACC_DESC_NavigatorTC")); //NOI18N
        
        notAvailLbl.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailLbl.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        notAvailLbl.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailLbl.setOpaque(true);
        
        getController().installActions();

        // empty initially
        setToEmpty();
            }
        
    /** Singleton accessor, finds instance in winsys structures */
    public static final NavigatorTC getInstance () {
        NavigatorTC navTC = (NavigatorTC)WindowManager.getDefault().
                        findTopComponent("navigatorTC"); //NOI18N
        if (navTC == null) {
            // shouldn't happen under normal conditions
            navTC = privateGetInstance();
            Logger.getAnonymousLogger().warning(
                "Could not locate the navigator component via its winsys id"); //NOI18N
        }
        return navTC;
    }
    
    /** Singleton intance accessor, to be used only from module's layer.xml
     * file, winsys section and as fallback from getInstance().
     *
     * Please don't call directly otherwise.
     */ 
    public static final NavigatorTC privateGetInstance () {
        if (instance == null) {
            instance = new NavigatorTC();
        }
        return instance;
    }

    /** Shows given navigator panel's component
     */
    public void setSelectedPanel (NavigatorPanel panel) {
        int panelIdx = panels.indexOf(panel);
        assert panelIdx != -1 : "Panel to select is not available"; //NOI18N
        
        if (panel.equals(selectedPanel)) {
            return;
        }
        
        this.selectedPanel = panel;
        ((CardLayout)contentArea.getLayout()).show(contentArea, String.valueOf(panelIdx));
        // #93123: follow-up, synchronizing combo selection with content area selection
        panelSelector.setSelectedIndex(panelIdx);
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
    public List<NavigatorPanel> getPanels () {
        return panels;
    }
    
    /** Sets content of navigator to given panels, selecting given one
     * @param panels List of panels
     * @param select Panel to be selected, shown
     */ 
    public void setPanels (List<NavigatorPanel> panels, NavigatorPanel select) {
        this.panels = panels;
        int panelsCount = panels == null ? -1 : panels.size();
        selectedPanel = null;
        // no panel, so make UI look empty
        if (panelsCount <= 0) {
            setToEmpty();
        } else {
            // clear regular content 
            contentArea.removeAll();
            panelSelector.removeAllItems();
            // #63777: hide panel selector when only one panel available
            panelSelector.setVisible(panelsCount != 1);
            // fill with new content
            JComponent curComp = null;
            int i = 0;
            boolean selectFound = false;
            for (NavigatorPanel curPanel : panels) {
                panelSelector.addItem(curPanel.getDisplayName());
                curComp = curPanel.getComponent();
                // for better error report in cases like #68544
                if (curComp == null) {
                    Throwable npe = new NullPointerException(
                            "Method " + curPanel.getClass().getName() +  //NOI18N
                            ".getComponent() must not return null under any condition!"  //NOI18N
                    );
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, npe);
                } else {
                    contentArea.add(curComp, String.valueOf(i));
                }
                if (curPanel == select) {
                    selectFound = true;
                }
                i++;
            }
            if (selectFound) {
                setSelectedPanel(select);
            } else {
                selectedPanel = panels.get(0);
            }
            // show if was hidden
            resetFromEmpty();
        }
    }
    
    /** Returns combo box, UI for selecting proper panels */
    public JComboBox getPanelSelector () {
        return panelSelector;
    }
    
    public JComponent getContentArea () {
        return contentArea;
    }
    
    // Window System related methods >>

    public String preferredID () {
        return "navigatorTC"; //NOI18N
    }

    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }

    /** Overriden to pass focus directly into content panel */
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow () {
        if (selectedPanel != null) {
            return selectedPanel.getComponent().requestFocusInWindow();
        } else {
            return super.requestFocusInWindow();
        }
    }

    @Override
    public void requestFocus() {
        if (selectedPanel != null) {
            selectedPanel.getComponent().requestFocus();
        } else {
            super.requestFocus();
        }
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

    @Override
    public UndoRedo getUndoRedo() {
        return getController().getUndoRedo();
    }
    
    /** Accessor for controller which controls UI behaviour */
    public NavigatorController getController () {
        if (controller == null) {
            controller = new NavigatorController(this);
        }
        return controller;
    }
    
    
    /*************** private stuff ************/
    
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

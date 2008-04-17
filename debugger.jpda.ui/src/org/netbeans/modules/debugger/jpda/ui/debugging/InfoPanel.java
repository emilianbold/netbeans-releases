/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.Utilities;

/**
 *
 * @author  Dan
 */
public class InfoPanel extends javax.swing.JPanel {

    private static int UNIT_HEIGHT = 40;
    private Color hitsPanelColor;
    
    private JButton arrowButton;
    
    /** Creates new form InfoPanel */
    public InfoPanel(TapPanel tapPanel) {
        hitsPanelColor = tapPanel.getBackground();
        
        initComponents();
        
        hitsPanel.setPreferredSize(new Dimension(0, UNIT_HEIGHT - tapPanel.getMinimumHeight()));
        deadlocksPanel.setPreferredSize(new Dimension(0, UNIT_HEIGHT));
        filterPanel.setPreferredSize(new Dimension(0, UNIT_HEIGHT));
        
        initFilterPanel(filterPanel);
        
        deadlocksPanel.setVisible(false);
        arrowButton = createArrowButton();
        hitsInnerPanel.add(arrowButton);
    }

    void refreshBreakpointHits(int hitsCount) {
        hitsLabel.setText("New Breakpoint Hits (" + hitsCount + ")"); // [TODO]
    }

    private JButton createArrowButton() {
        JPopupMenu menu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
            new ImageIcon(Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/unvisited_bpkt_arrow_small_16.png")), menu);
        // JMenuItem item = new JMenuItem(Actions.cutAmpersand((String) getValue(NAME)));//"<html><b>"+Actions.cutAmpersand((String) getValue(NAME))+"</b></html>") {
        JMenuItem item = new JMenuItem("menu item");
        // item.setEnabled(delegate.isEnabled());
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // [TODO]
            }
        });
        button.setPreferredSize(new Dimension(48, button.getPreferredSize().height)); // [TODO]
        button.setFocusable(false);
        // Actions.connect(button, this);
        return button;
    }
    
    private void initFilterPanel (JPanel filterPanel) {
        filterPanel.setBorder(new EmptyBorder(1, 2, 1, 5));
        FiltersDescriptor filtersDesc = FiltersDescriptor.createDebuggingViewFilters();
        // configure toolbar
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        // create toggle buttons
        int filterCount = filtersDesc.getFilterCount();
        ArrayList<JToggleButton> toggles = new ArrayList<JToggleButton>(filterCount);
        JToggleButton toggleButton = null;

        for (int i = 0; i < filterCount; i++) {
            toggleButton = createToggle(filtersDesc, i);
            toggles.add(toggleButton);
        }

        // add toggle buttons
        JToggleButton curToggle;
        Dimension space = new Dimension(3, 0);
        for (int i = 0; i < toggles.size(); i++) {
            curToggle = toggles.get(i);
            // curToggle.addActionListener(this); [TODO]
            toolbar.add(curToggle);
            if (i != toggles.size() - 1) {
                toolbar.addSeparator(space);
            }
        }
        filterPanel.add(toolbar);
    }

    private JToggleButton createToggle (FiltersDescriptor filtersDesc, int index) {
        boolean isSelected = filtersDesc.isSelected(index);
        Icon icon = filtersDesc.getSelectedIcon(index);
        // ensure small size, just for the icon
        JToggleButton toggleButton = new JToggleButton(icon, isSelected);
        Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
        toggleButton.setPreferredSize(size);
        toggleButton.setMargin(new Insets(2,3,2,3));
        toggleButton.setToolTipText(filtersDesc.getTooltip(index));
        toggleButton.setFocusable(false);
        return toggleButton;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hitsPanel = new javax.swing.JPanel();
        hitsInnerPanel = new javax.swing.JPanel();
        infoIcon = new javax.swing.JLabel();
        hitsLabel = new javax.swing.JLabel();
        deadlocksPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        filterPanel = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();

        setLayout(new java.awt.BorderLayout());

        hitsPanel.setBackground(hitsPanelColor);
        hitsPanel.setPreferredSize(new java.awt.Dimension(0, 40));
        hitsPanel.setLayout(new java.awt.BorderLayout());

        hitsInnerPanel.setOpaque(false);

        infoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/info_big.png"))); // NOI18N
        infoIcon.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon.text")); // NOI18N
        hitsInnerPanel.add(infoIcon);

        hitsLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.hitsLabel.text")); // NOI18N
        hitsInnerPanel.add(hitsLabel);

        hitsPanel.add(hitsInnerPanel, java.awt.BorderLayout.CENTER);

        add(hitsPanel, java.awt.BorderLayout.NORTH);

        deadlocksPanel.setBackground(hitsPanelColor);
        deadlocksPanel.setPreferredSize(new java.awt.Dimension(0, 40));
        deadlocksPanel.setLayout(new java.awt.BorderLayout());

        jSeparator1.setBackground(hitsPanelColor);
        deadlocksPanel.add(jSeparator1, java.awt.BorderLayout.PAGE_START);

        add(deadlocksPanel, java.awt.BorderLayout.CENTER);

        filterPanel.setPreferredSize(new java.awt.Dimension(0, 40));
        filterPanel.setLayout(new java.awt.BorderLayout());
        filterPanel.add(jSeparator2, java.awt.BorderLayout.NORTH);

        add(filterPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deadlocksPanel;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JPanel hitsInnerPanel;
    private javax.swing.JLabel hitsLabel;
    private javax.swing.JPanel hitsPanel;
    private javax.swing.JLabel infoIcon;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables

}

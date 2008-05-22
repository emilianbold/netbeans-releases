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
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Dan
 */
public class InfoPanel extends javax.swing.JPanel {

    private static final int PANEL_HEIGHT = 40;
    
    private Color hitsPanelColor;
    private Color deadlockPanelColor;
    private Color filterPanelColor;
    private int tapPanelMinimumHeight;
    private TapPanel tapPanel;
    
    private JButton arrowButton;
    private JPopupMenu arrowMenu;
    private Map<JPDAThread, JMenuItem> threadToMenuItem = new HashMap<JPDAThread, JMenuItem>();
    
    /** Creates new form InfoPanel */
    public InfoPanel(TapPanel tapPanel) {
        this.tapPanel = tapPanel;
        filterPanelColor = tapPanel.getBackground(); // [TODO]
        hitsPanelColor = DebuggingView.hitsColor; // [TODO]
        deadlockPanelColor = hitsPanelColor; // DebuggingView.deadlockColor; // [TODO]
        tapPanelMinimumHeight = tapPanel.getMinimumHeight(); // 8 pixels [TODO]
        
        initComponents();
        
        filterPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
        
        initFilterPanel(filterPanel);
        arrowButton = createArrowButton();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        hitsInnerPanel.add(arrowButton, gridBagConstraints);
        
        hideHitsPanel();
        hideDeadlocksPanel();
    }

    void clearBreakpointHits() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                arrowMenu.removeAll();
                threadToMenuItem.clear();
                hideHitsPanel();
            }
        });
    }

    void removeBreakpointHit(final JPDAThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JMenuItem item = threadToMenuItem.remove(thread);
                if (item == null) {
                    return;
                }
                arrowMenu.remove(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 0) {
                    hideHitsPanel();
                }
            }
        });
    }
    
    void addBreakpointHit(final JPDAThread thread, final int newHitsCount) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (threadToMenuItem.get(thread) != null) {
                    return;
                }
                String displayName;
                try {
                    displayName = DebuggingNodeModel.getDisplayName(thread, false);
                } catch (UnknownTypeException e) {
                    displayName = thread.getName();
                }
                Image image = Utilities.loadImage(DebuggingNodeModel.getIconBase(thread));
                Icon icon = image != null ? new ImageIcon(image) : null;
                JMenuItem item = new JMenuItem(displayName, icon);
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        thread.makeCurrent();
                    }
                });
                threadToMenuItem.put(thread, item);
                arrowMenu.add(item);
                setHitsText(newHitsCount);
                if (newHitsCount == 1) {
                    showHitsPanel();
                }
            }
        });
    }
    
    private void setHitsText(int hitsNumber) {
        String text;
        if (hitsNumber == 1) {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_OneNewHit");
        } else {
            text = NbBundle.getMessage(InfoPanel.class, "LBL_NewHits", hitsNumber);
        }
        hitsLabel.setText(text);
    }
    
    // [TODO]
    boolean isBreakpointHit(JPDAThread thread) {
        return threadToMenuItem.containsKey(thread);
    }

    void setShowDeadlock(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (visible) {
                    showDeadlocksPanel();
                } else {
                    hideDeadlocksPanel();
                }
            }
        });
    }
    
    // **************************************************************************
    
    private void hideHitsPanel() {
        if (!hitsPanel.isVisible()) {
            return;
        }
        hitsPanel.setVisible(false);
        hitsSeparator.setVisible(false);
        if (!deadlocksPanel.isVisible()) {
            filterTopSpacePanel.setVisible(false);
            filterPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
            tapPanel.setBackground(filterPanelColor);
        }
    }
    
    private void showHitsPanel() {
        if (hitsPanel.isVisible()) {
            return;
        }
        if (!deadlocksPanel.isVisible()) {
            hitsTopSpacePanel.setVisible(false);
            hitsPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
            filterPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT));
        } else {
            hitsTopSpacePanel.setVisible(true);
            hitsPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT));
        }
        hitsPanel.setVisible(true);
        hitsSeparator.setVisible(true);
        filterTopSpacePanel.setVisible(true);
        if (!deadlocksPanel.isVisible()) {
            tapPanel.setBackground(hitsPanelColor);
        }
    }
    
    private void hideDeadlocksPanel() {
        if (!deadlocksPanel.isVisible()) {
            return;
        }
        deadlocksPanel.setVisible(false);
        deadlocksSeparator.setVisible(false);
        if (hitsPanel.isVisible()) {
            hitsTopSpacePanel.setVisible(false);
            hitsPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
            tapPanel.setBackground(hitsPanelColor);
        } else {
            filterTopSpacePanel.setVisible(false);
            tapPanel.setBackground(filterPanelColor);
            filterPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
        }
    }
    
    private void showDeadlocksPanel() {
        if (deadlocksPanel.isVisible()) {
            return;
        }
        hitsTopSpacePanel.setVisible(true);
        filterTopSpacePanel.setVisible(true);
        deadlocksPanel.setVisible(true);
        deadlocksSeparator.setVisible(true);
        tapPanel.setBackground(deadlockPanelColor);
        deadlocksPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT - tapPanelMinimumHeight));
        filterPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT));
        hitsPanel.setPreferredSize(new Dimension(0, PANEL_HEIGHT));
    }
    
    private JButton createArrowButton() {
        arrowMenu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
            new ImageIcon(Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/unvisited_bpkt_arrow_small_16.png")), arrowMenu);
        button.setPreferredSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setMaximumSize(new Dimension(40, button.getPreferredSize().height)); // [TODO]
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (arrowMenu.getComponentCount() > 0) {
                    Object item = arrowMenu.getComponent(0);
                    for (Map.Entry<JPDAThread, JMenuItem> entry : threadToMenuItem.entrySet()) {
                        if (entry.getValue() == item) {
                            entry.getKey().makeCurrent();
                        } // if
                    } // for
                } // if
            } // actionPerformed
        });
        return button;
    }
    
    private void initFilterPanel (JPanel filterPanel) {
        filterPanel.setBorder(new EmptyBorder(1, 2, 1, 5));
        final FiltersDescriptor filtersDesc = FiltersDescriptor.createDebuggingViewFilters();
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
        Dimension space = new Dimension(3, 0);
        for (int i = 0; i < toggles.size(); i++) {
            final int index = i;
            final JToggleButton curToggle = toggles.get(i);
            curToggle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    filtersDesc.setSelected(index, curToggle.isSelected());
                }
            });
            toolbar.add(curToggle);
            if (i != toggles.size() - 1) {
                toolbar.addSeparator(space);
            }
        }
        filterPanel.add(toolbar, BorderLayout.CENTER);
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
        filtersDesc.connectToggleButton(index, toggleButton); // [TODO] ?
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
        java.awt.GridBagConstraints gridBagConstraints;

        deadlocksPanel = new javax.swing.JPanel();
        deadlocksBottomSpacePanel = new javax.swing.JPanel();
        deadlocksInnerPanel = new javax.swing.JPanel();
        infoIcon1 = new javax.swing.JLabel();
        deadlocksLabel = new javax.swing.JLabel();
        emptyPanel1 = new javax.swing.JPanel();
        deadlocksSeparator = new javax.swing.JPanel();
        hitsPanel = new javax.swing.JPanel();
        hitsTopSpacePanel = new javax.swing.JPanel();
        hitsBottomSpacePanel = new javax.swing.JPanel();
        hitsInnerPanel = new javax.swing.JPanel();
        infoIcon = new javax.swing.JLabel();
        hitsLabel = new javax.swing.JLabel();
        emptyPanel = new javax.swing.JPanel();
        hitsSeparator = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        filterTopSpacePanel = new javax.swing.JPanel();
        filterBottomSpacePanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        deadlocksPanel.setBackground(deadlockPanelColor);
        deadlocksPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        deadlocksPanel.setLayout(new java.awt.BorderLayout());

        deadlocksBottomSpacePanel.setBackground(java.awt.Color.green);
        deadlocksBottomSpacePanel.setOpaque(false);
        deadlocksBottomSpacePanel.setPreferredSize(new java.awt.Dimension(0, 8));
        deadlocksPanel.add(deadlocksBottomSpacePanel, java.awt.BorderLayout.SOUTH);

        deadlocksInnerPanel.setOpaque(false);
        deadlocksInnerPanel.setPreferredSize(new java.awt.Dimension(0, 16));
        deadlocksInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/wrong_pass.png"))); // NOI18N
        infoIcon1.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        deadlocksInnerPanel.add(infoIcon1, gridBagConstraints);

        deadlocksLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        deadlocksLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.deadlocksLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        deadlocksInnerPanel.add(deadlocksLabel, gridBagConstraints);

        emptyPanel1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        deadlocksInnerPanel.add(emptyPanel1, gridBagConstraints);

        deadlocksPanel.add(deadlocksInnerPanel, java.awt.BorderLayout.CENTER);

        add(deadlocksPanel);

        deadlocksSeparator.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        deadlocksSeparator.setMaximumSize(new java.awt.Dimension(32767, 1));
        deadlocksSeparator.setMinimumSize(new java.awt.Dimension(10, 1));
        deadlocksSeparator.setPreferredSize(new java.awt.Dimension(0, 1));
        add(deadlocksSeparator);

        hitsPanel.setBackground(hitsPanelColor);
        hitsPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        hitsPanel.setLayout(new java.awt.BorderLayout());

        hitsTopSpacePanel.setBackground(java.awt.Color.cyan);
        hitsTopSpacePanel.setOpaque(false);
        hitsTopSpacePanel.setPreferredSize(new java.awt.Dimension(0, 8));
        hitsPanel.add(hitsTopSpacePanel, java.awt.BorderLayout.NORTH);

        hitsBottomSpacePanel.setBackground(java.awt.Color.green);
        hitsBottomSpacePanel.setOpaque(false);
        hitsBottomSpacePanel.setPreferredSize(new java.awt.Dimension(0, 8));
        hitsPanel.add(hitsBottomSpacePanel, java.awt.BorderLayout.SOUTH);

        hitsInnerPanel.setOpaque(false);
        hitsInnerPanel.setLayout(new java.awt.GridBagLayout());

        infoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/resources/info_big.png"))); // NOI18N
        infoIcon.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.infoIcon.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        hitsInnerPanel.add(infoIcon, gridBagConstraints);

        hitsLabel.setText(org.openide.util.NbBundle.getMessage(InfoPanel.class, "InfoPanel.hitsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        hitsInnerPanel.add(hitsLabel, gridBagConstraints);

        emptyPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        hitsInnerPanel.add(emptyPanel, gridBagConstraints);

        hitsPanel.add(hitsInnerPanel, java.awt.BorderLayout.CENTER);

        add(hitsPanel);

        hitsSeparator.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        hitsSeparator.setMaximumSize(new java.awt.Dimension(32767, 1));
        hitsSeparator.setMinimumSize(new java.awt.Dimension(10, 1));
        hitsSeparator.setPreferredSize(new java.awt.Dimension(0, 1));
        add(hitsSeparator);

        filterPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        filterPanel.setLayout(new java.awt.BorderLayout());

        filterTopSpacePanel.setBackground(java.awt.Color.cyan);
        filterTopSpacePanel.setOpaque(false);
        filterTopSpacePanel.setPreferredSize(new java.awt.Dimension(0, 8));
        filterPanel.add(filterTopSpacePanel, java.awt.BorderLayout.NORTH);

        filterBottomSpacePanel.setBackground(java.awt.Color.green);
        filterBottomSpacePanel.setOpaque(false);
        filterBottomSpacePanel.setPreferredSize(new java.awt.Dimension(0, 8));
        filterPanel.add(filterBottomSpacePanel, java.awt.BorderLayout.SOUTH);

        add(filterPanel);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deadlocksBottomSpacePanel;
    private javax.swing.JPanel deadlocksInnerPanel;
    private javax.swing.JLabel deadlocksLabel;
    private javax.swing.JPanel deadlocksPanel;
    private javax.swing.JPanel deadlocksSeparator;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JPanel emptyPanel1;
    private javax.swing.JPanel filterBottomSpacePanel;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JPanel filterTopSpacePanel;
    private javax.swing.JPanel hitsBottomSpacePanel;
    private javax.swing.JPanel hitsInnerPanel;
    private javax.swing.JLabel hitsLabel;
    private javax.swing.JPanel hitsPanel;
    private javax.swing.JPanel hitsSeparator;
    private javax.swing.JPanel hitsTopSpacePanel;
    private javax.swing.JLabel infoIcon;
    private javax.swing.JLabel infoIcon1;
    // End of variables declaration//GEN-END:variables

}

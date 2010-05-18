/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.mappercore;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class FiltersToolBar extends JToolBar implements ActionListener {

    private Mapper mapper;
    
    private JButton expandLeftButton;
    private JButton expandRightButton;
    
    private JToggleButton leftAllButton;
    private JToggleButton leftOutputButton;
    private ButtonGroup leftGroup;
    private JToggleButton rightAllButton;
    private JToggleButton rightInputButton;
    private ButtonGroup rightGroup;
    
    private SwitchLeftFilterAction switchLeftFilterAction;
    private SwitchRightFilterAction switchRightFilterAction;
    
    private ExpandNonEmptyGraphsAction expandNonEmptyGraphsAction;
    private CollapseAllRightAction collapseAllRightAction;
    private CollapseOtherNodesAction collapseOtherNodesAction;
    
    private CollapseAllLeftAction collapseAllLeftAction;
    private ExpandMappedNodesLeftAction expandMappedNodesLeftAction;
    
    private JPopupMenu expandLeftMenu;
    private JPopupMenu expandRightMenu;

    public FiltersToolBar(Mapper mapper) {
        setFloatable(false);
        setBorder(new EmptyBorder(2, 1, 2, 1));

        this.mapper = mapper;

        boolean filterLeft = mapper.isFilterLeft();
        boolean filterRight = mapper.isFilterRight();

        collapseAllLeftAction = new CollapseAllLeftAction();
        expandMappedNodesLeftAction = new ExpandMappedNodesLeftAction();
        
        expandNonEmptyGraphsAction = new ExpandNonEmptyGraphsAction();
        collapseOtherNodesAction = new CollapseOtherNodesAction();
        collapseAllRightAction = new CollapseAllRightAction();
        
        Icon expandActionsIcon = new ImageIcon(getClass()
                .getResource("resources/expandactions.png")); // NOI18N
        expandLeftButton = new JButton(expandActionsIcon);
        expandLeftButton.addActionListener(this);
        expandLeftButton.setFocusable(false);

        expandRightButton = new JButton(expandActionsIcon);
        expandRightButton.addActionListener(this);
        expandRightButton.setFocusable(false);
        
        switchLeftFilterAction = new SwitchLeftFilterAction();
        switchRightFilterAction = new SwitchRightFilterAction();
        
        InputMap inputMap = mapper.getInputMap(
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = mapper.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), 
                "SwitchLeftFilter"); // NOI18N
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), 
                "SwitchRightFilter"); // NOI18N
        
        actionMap.put("SwitchLeftFilter", switchLeftFilterAction); // NOI18N
        actionMap.put("SwitchRightFilter", switchRightFilterAction); // NOI18N
        
        registerMapperAction(expandMappedNodesLeftAction, 
                "ExpandMappedNodesLeftAction"); // NOI18N
        registerMapperAction(collapseAllLeftAction, 
                "СollapseAllLeftAction"); // NOI18N
        registerMapperAction(expandNonEmptyGraphsAction, 
                "ExpandNonEmptyGraphsAction"); // NOI18N
        registerMapperAction(collapseAllRightAction, 
                "CollapseAllRightAction"); // NOI18N
        registerMapperAction(collapseOtherNodesAction, 
                "CollapseOtherNodesAction"); // NOI18N
        
        String leftToolTip = NbBundle.getMessage(getClass(), 
                "TTT_LeftTreeFilter"); // NOI18N
        String rightToolTip = NbBundle.getMessage(getClass(), 
                "TTT_RightTreeFilter"); // NOI18N
        
        leftAllButton = new JToggleButton(NbBundle.getMessage(getClass(), 
                "LBL_LeftTreeFilterAll")); // NOI18N
        leftAllButton.setFocusable(false);
        leftAllButton.setSelected(!filterLeft);
        leftAllButton.setToolTipText(leftToolTip);
        leftAllButton.addActionListener(this);
        leftOutputButton = new JToggleButton(NbBundle.getMessage(getClass(), 
                "LBL_LeftTreeFilterOutput")); // NOI18N
        leftOutputButton.setSelected(filterLeft);
        leftOutputButton.setFocusable(false);
        leftOutputButton.setToolTipText(leftToolTip);
        leftOutputButton.addActionListener(this);
        leftGroup = new ButtonGroup();
        leftGroup.add(leftAllButton);
        leftGroup.add(leftOutputButton);

        rightAllButton = new JToggleButton(NbBundle.getMessage(getClass(), 
                "LBL_RightTreeFilterAll")); // NOI18N
        rightAllButton.setSelected(!filterRight);
        rightAllButton.setFocusable(false);
        rightAllButton.setToolTipText(rightToolTip);
        rightAllButton.addActionListener(this);
        rightInputButton = new JToggleButton(NbBundle.getMessage(getClass(), 
                "LBL_RightTreeFilterInput")); // NOI18N
        rightInputButton.setSelected(filterRight);
        rightInputButton.setFocusable(false);
        rightInputButton.setToolTipText(rightToolTip);
        rightInputButton.addActionListener(this);
        rightGroup = new ButtonGroup();
        rightGroup.add(rightAllButton);
        rightGroup.add(rightInputButton);
        
        configureAccessibleContext(leftAllButton, 
                "LeftTreeFilterAll"); // NOI18N
        configureAccessibleContext(leftOutputButton, 
                "LeftTreeFilterOutput"); // NOI18N
        configureAccessibleContext(rightAllButton, 
                "RightTreeFilterAll"); // NOI18N
        configureAccessibleContext(rightInputButton, 
                "RightTreeFilterInput"); // NOI18N
        configureAccessibleContext(expandLeftButton, 
                "ExpandCollapseLeft"); // NOI18N
        configureAccessibleContext(expandRightButton, 
                "ExpandCollapseRight"); // NOI18N
        
        add(expandLeftButton);
        add(leftAllButton);
        add(leftOutputButton);
        add(rightInputButton);
        add(rightAllButton);
        add(expandRightButton);
    }
    
    private void configureAccessibleContext(JComponent component, String key) {
        AccessibleContext context = component.getAccessibleContext();
        context.setAccessibleName(NbBundle
                .getMessage(getClass(), "ACSN_" + key)); // NOI18N
        context.setAccessibleDescription(NbBundle
                .getMessage(getClass(), "ACSD_" + key)); // NOI18N
    }
    
    private void registerMapperAction(Action action, String key) {
        Object o = action.getValue(Action.ACCELERATOR_KEY);
        if (o instanceof KeyStroke) {
            mapper.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put((KeyStroke) o, key);
            mapper.getActionMap().put(key, action);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == leftAllButton) {
            mapper.setFilter(false, mapper.isFilterRight());
        } else if (source == leftOutputButton) {
            mapper.setFilter(true, mapper.isFilterRight());
        } else if (source == rightAllButton) {
            mapper.setFilter(mapper.isFilterLeft(), false);
        } else if (source == rightInputButton) {
            mapper.setFilter(mapper.isFilterLeft(), true);
        } else if (source == expandLeftButton) {
            showExpandLeftMenu();
        } else if (source == expandRightButton) {
            showExpandRightMenu();
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();

        int w = 0;
        int h = 0;

        int count = 0;

        for (JComponent component : new JComponent[]{leftAllButton,
                    leftOutputButton, rightAllButton, rightInputButton
                }) {
            Dimension size = component.getPreferredSize();
            w = Math.max(w, size.width);
            h = Math.max(h, size.height);
            if (component.isVisible()) {
                count++;
            }
        }
        
        Dimension expandLeftSize = expandLeftButton.getPreferredSize();
        Dimension expandRightSize = expandRightButton.getPreferredSize();
        
        h = Math.max(h, Math.max(expandLeftSize.height, expandRightSize.height));
        int expandWidth = Math.max(expandLeftSize.width, expandRightSize.width);
        
        w = insets.left + expandWidth + 1 + w * count + 4 + 1 + expandWidth 
                + insets.right;
        h = insets.top + h + insets.bottom;

        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();

        Insets insets = getInsets();

        int x1 = insets.left;
        int x2 = width - insets.right;

        int y1 = insets.top;
        int buttonH = height - insets.top - insets.bottom;
        int buttonW = 0;

        for (JComponent component : new JComponent[]{leftAllButton,
                    leftOutputButton, rightAllButton, rightInputButton
                }) {
            buttonW = Math.max(buttonW, component.getPreferredSize().width);
        }

        Dimension expandLeftSize = expandLeftButton.getPreferredSize();
        Dimension expandRightSize = expandRightButton.getPreferredSize();
        int expandWidth = Math.max(expandLeftSize.width, expandRightSize.width);
        
        expandLeftButton.setBounds(x1, y1, expandWidth, buttonH);
        x1 += expandWidth + 1;
        
        if (leftAllButton.isVisible()) {
            leftAllButton.setBounds(x1, y1, buttonW, buttonH);
            x1 += buttonW;
        }

        if (leftOutputButton.isVisible()) {
            leftOutputButton.setBounds(x1, y1, buttonW, buttonH);
            x1 += buttonW;
        }

        int x3 = x2 - expandWidth - 1;
        if (rightAllButton.isVisible()) {
            x3 -= buttonW;
        }
        if (rightInputButton.isVisible()) {
            x3 -= buttonW;
        }
        x3 = Math.max(x3, x1 + 4);

        if (rightInputButton.isVisible()) {
            rightInputButton.setBounds(x3, y1, buttonW, buttonH);
            x3 += buttonW;
        }

        if (rightAllButton.isVisible()) {
            rightAllButton.setBounds(x3, y1, buttonW, buttonH);
            x3 += buttonW + 1;
        }
        
        expandRightButton.setBounds(x3, y1, expandWidth, buttonH);
    }
    
    public void updateButtonsState() {
        boolean filterLeft = mapper.isFilterLeft();
        boolean filterRight = mapper.isFilterRight();
        
        leftAllButton.setSelected(!filterLeft);
        leftOutputButton.setSelected(filterLeft);
        
        rightAllButton.setSelected(!filterRight);
        rightInputButton.setSelected(filterRight);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Color oldColor = g.getColor();
        int x = getWidth() - 1;
        int y = getHeight() - 1;

        g.setColor(getBackground().brighter());
        g.drawLine(0, 0, x, 0);

        g.setColor(getBackground().darker());
        g.drawLine(0, y, x, y);
        g.setColor(oldColor);
    }
    
    private void showExpandLeftMenu() {
        if (expandLeftMenu == null) {
            expandLeftMenu = new JPopupMenu();
            expandLeftMenu.add(expandMappedNodesLeftAction);
            expandLeftMenu.add(collapseAllLeftAction);
        }
        
        expandLeftMenu.show(expandLeftButton, -2, 
                expandLeftButton.getHeight() + 1);
    }
    
    private void showExpandRightMenu() {
        if (expandRightMenu == null) {
            expandRightMenu = new JPopupMenu();
            expandRightMenu.add(expandNonEmptyGraphsAction);
            expandRightMenu.add(collapseAllRightAction);
            expandRightMenu.add(collapseOtherNodesAction);
        }
        
        int menuWidth = expandRightMenu.getPreferredSize().width;
        
        expandRightMenu.show(expandRightButton, 
                expandRightButton.getWidth() - menuWidth + 2, 
                expandRightButton.getHeight() + 1);
    }
    
    private class SwitchLeftFilterAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            mapper.setFilter(!mapper.isFilterLeft(), mapper.isFilterRight());
        }
    }

    private class SwitchRightFilterAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            mapper.setFilter(mapper.isFilterLeft(), !mapper.isFilterRight());
        }
    }
    
    private class ExpandLeftAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showExpandLeftMenu();
        }
    }
    
    private class ExpandRightAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            showExpandRightMenu();
        }
    }
    
    private class ExpandNonEmptyGraphsAction extends AbstractAction {
        ExpandNonEmptyGraphsAction() {
            super(NbBundle.getMessage(FiltersToolBar.class, 
                    "LBL_ExpandNonEmptyGraphs")); // NOI18N
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            mapper.expandNonEmptyGraphs();
        }
    }
    
    private class CollapseOtherNodesAction extends AbstractAction {
        CollapseOtherNodesAction() {
            super(NbBundle.getMessage(FiltersToolBar.class, 
                    "LBL_CollapseOtherNodes")); // NOI18N
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_SUBTRACT, 
                    KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            mapper.hideOtherPathes(1);
        }
    }
    
    private class CollapseAllRightAction extends AbstractAction {
        CollapseAllRightAction() {
            super(NbBundle.getMessage(FiltersToolBar.class, 
                    "LBL_CollapseAllRight")); // NOI18N
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            mapper.collapseAll(1);
        }
    }
    
    private class CollapseAllLeftAction extends AbstractAction {
        CollapseAllLeftAction() {
            super(NbBundle.getMessage(FiltersToolBar.class, 
                    "LBL_CollapseAllLeft")); // NOI18N
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_SUBTRACT, 
                    KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            LeftTree leftTree = mapper.getLeftTree();

            for (int i = leftTree.getRowCount() - 1; i >= 0; i--) {
                leftTree.collapseRow(i);
            }
        }
    }

    private class ExpandMappedNodesLeftAction extends AbstractAction {
        ExpandMappedNodesLeftAction() {
            super(NbBundle.getMessage(FiltersToolBar.class, 
                    "LBL_ExpandMappedNodesLeft")); // NOI18N
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 
                    KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            mapper.expandMappedLeftTreeItems();
        }
        
    }
    
}

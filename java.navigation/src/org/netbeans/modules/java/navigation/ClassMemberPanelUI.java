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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.navigation;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.java.navigation.base.CellRenderer;
import org.netbeans.modules.java.navigation.base.NavigatorJList;
import org.netbeans.modules.java.navigation.base.SearchPanel;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.netbeans.modules.java.navigation.base.TooltipHack;
import org.netbeans.modules.java.navigation.jmi.JUtils.TipHackInvoker;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * UI frontend for navigator panel showing java class members. Consists of
 * list (tree) of members and filters panel.
 *
 * @author Dafe Simonek
 */
final class ClassMemberPanelUI extends JPanel implements TipHackInvoker {

    private JScrollPane pane;
    private TapPanel filtersPanel;
    private JLabel filtersLbl;
    private NavigatorJList content;
    private ClassMemberController controller;
    private SearchPanel searchPanel;
    /** helper variable for recognizing very first paint */
    private boolean firstPaint = true;

    public ClassMemberPanelUI () {
        init();
        controller = new ClassMemberController(this);
    }
    
    private void init () {
        // main content
        pane = new JScrollPane();
        pane.setBorder (BorderFactory.createEmptyBorder());
        pane.setViewportBorder(BorderFactory.createEmptyBorder());
        
        content = new NavigatorJList(pane);

        content.setCellRenderer(new CellRenderer());
        content.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pane.getViewport().setView(content);
        // no scrollbar if abbreviations enabled
        if (Boolean.getBoolean("navigator.string.abbrevs")) {  //NOI18N
            pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        
        // filters
        filtersPanel = new TapPanel();
        filtersLbl = new JLabel(NbBundle.getMessage(ClassMemberPanelUI.class, "LBL_Filter")); //NOI18N
        filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
        filtersPanel.add(filtersLbl);
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText));
        
        // search ability
        searchPanel = new SearchPanel();
        searchPanel.prepare(content);
        
        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
        add(filtersPanel, BorderLayout.SOUTH);
    }
    
    void setFilters (JComponent filtersComponent) {
        filtersPanel.add(filtersComponent);
    }
 
    public NavigatorJList getContent () {
        return content;
    }
    
    /** Overriden to calculate correct row height before first paint */
    public void paint (Graphics g) {
        if (firstPaint) {
            configureRowHeight(g);
        }
        super.paint(g);
    }

    /** Overriden to pass focus directly to main content, which in 
     * turn assures that some element is always selected
     */ 
    public boolean requestFocusInWindow () {
        boolean result = super.requestFocusInWindow();
        content.requestFocusInWindow();
        return result;
    }
    
    /** JUtils.TipHackInvoker implementation, triggers tooltip occurence */
    public void invokeTip (int x, int y) {
        if (!content.isShowing()) {
            return;
        }
        if (content.isTooltipLocValid(x, y)) {
            TooltipHack.invokeTip(content, x, y, Integer.MAX_VALUE);
        }
    }

    /** Sets wait cursor on panel UI area according to given flag */
    public void setBusyState (boolean isBusy) {
        Cursor cursor;
        if (isBusy) {
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        } else {
            cursor = Cursor.getDefaultCursor();
        }
        filtersPanel.setCursor(cursor);
        content.setCursor(cursor);
    }

    /** Called after new content is fully loaded. Keeps inner JList content
     * manageable ky keyboard */
    public void newContentReady () {
        if (content.isFocusOwner()) {
            content.assureSelection();
        }
    }

    /** Calculates row height based on font size */
    private void configureRowHeight (Graphics g) {
        g.setFont(content.getFont());
        FontMetrics fm = g.getFontMetrics ();
        int height = Math.max(16, fm.getHeight());
        content.setFixedCellHeight(height);
        firstPaint = false;
    }

}

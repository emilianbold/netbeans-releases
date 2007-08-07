/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.view.svg;
    
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openide.util.NbBundle;

public class SVGStatusBar extends JPanel {
    public static final int CELL_POSITION = 0;
    public static final int CELL_MODE     = 1;
    public static final int CELL_MAIN     = 2;
    
    public static final String LOCKED           = NbBundle.getMessage(SVGStatusBar.class, "LBL_STATUS_BAR_LOCKED"); //NOI18N
    public static final String UNLOCKED         = NbBundle.getMessage(SVGStatusBar.class, "LBL_STATUS_BAR_UNLOCKED"); //NOI18N
    public static final String TOOLTIP_LOCKED   = NbBundle.getMessage(SVGStatusBar.class, "HINT_STATUS_BAR_LOCKED"); //NOI18N
    public static final String TOOLTIP_UNLOCKED = NbBundle.getMessage(SVGStatusBar.class, "HINT_STATUS_BAR_UNLOCKED"); //NOI18N
       
    private static final String[] POS_MAX_STRINGS = new String[] { "-99999.9:-99999.9" }; // NOI18N
    
    private final Cell [] cells = new Cell[3];

    public SVGStatusBar() {
        super(new GridBagLayout());
        JLabel cell;
        cell = cells[CELL_POSITION] = new Cell(POS_MAX_STRINGS);
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.setToolTipText("SVG coordinates");
        setText(CELL_POSITION, "[-,-]");
        cell = cells[CELL_MODE] = new Cell( new String[] { LOCKED, UNLOCKED });
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cells[CELL_MAIN] = new Cell(null);
        
        refreshPanel();        
    }

    public void setText(int cellIndex, String text) {
        JLabel cell = cells[cellIndex];
        if (cell != null) {
            cell.setText(text);

            switch( cellIndex) {
                case CELL_POSITION:
                //cell.setToolTipText(caretPositionLocaleString);    
                break;
                case CELL_MODE:
                cell.setToolTipText(text.equals(UNLOCKED)? TOOLTIP_UNLOCKED : TOOLTIP_LOCKED);
                break;
                default:
                cell.setToolTipText("".equals(text) ? null : text); //NOI18N
                break;
            }
        }
    }

    private void refreshPanel() {
        // Layout cells
        removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx      = GridBagConstraints.RELATIVE;
        gc.gridy      = 0;
        gc.gridwidth  = cells.length;
        gc.gridheight = 1;
        gc.weighty    = 1.0;

        for (JLabel c : cells) {
            boolean main = cells[CELL_MAIN] == c;
            gc.fill = main ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
            gc.weightx = main ? 1.0 : 0;
            add(c, gc);
        }
        updateCellBorders();
    }

    /** Manages cell borders so that left, right and inner cells have properly
     * assigned borders for various LFs. Borders are special, installed by
     * core into UIManager maps. */
    private void updateCellBorders() {
        int cellCount = cells.length;
        Border innerBorder = (Border)UIManager.get("Nb.Editor.Status.innerBorder"); //NOI18N
        Border leftBorder = (Border)UIManager.get("Nb.Editor.Status.leftBorder"); //NOI18N
        Border rightBorder = (Border)UIManager.get("Nb.Editor.Status.rightBorder"); //NOI18N
        Border onlyOneBorder = (Border)UIManager.get("Nb.Editor.Status.onlyOneBorder"); //NOI18N
        if (cellCount == 0 || innerBorder == null || 
            leftBorder == null || rightBorder == null || onlyOneBorder == null) {
            // don't modify borders at all if some is not available 
            return;
        }
        if (cellCount == 1) {
            // only one cell
            cells[0].setBorder(onlyOneBorder);
            return;
        } else {
            cells[0].setBorder(leftBorder);
            for ( int i = 1; i < cellCount - 1; i++) {
                cells[i].setBorder(innerBorder);
            }
            cells[cellCount-1].setBorder(rightBorder);
        }
    }

    private static class Cell extends JLabel {
        private final Dimension maxDimension = new Dimension();
        private final String[] widestStrings;

        private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

        static final Border CELL_BORDER = 
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1,0,0,0,UIManager.getDefaults().getColor("control")),   // NOI18N
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,1,1,UIManager.getDefaults().getColor("controlHighlight")),   // NOI18N
                        BorderFactory.createLineBorder(UIManager.getDefaults().getColor("controlDkShadow"))   // NOI18N
                    )
                ),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)
            );
        
        Cell(String[] widestStrings) {
            setBorder(CELL_BORDER);
            setOpaque(true);
            this.widestStrings = widestStrings;
            setForeground((Color) UIManager.get("Label.foreground")); //NOI18N
            setBackground((Color) UIManager.get("Label.background")); //NOI18N

            updateSize();
        }

        private void updateSize() {
            Font f = getFont();
            if (f != null) {
                Border b = getBorder();
                Insets ins = (b != null) ? b.getBorderInsets(this) : NULL_INSETS;
                FontMetrics fm = getFontMetrics(f);
                int mw = fm.stringWidth(this.getText());
                maxDimension.height = fm.getHeight() + ins.top + ins.bottom;
                if (widestStrings != null) {
                    for (int i = 0; i < widestStrings.length; i++) {
                        String widestString = widestStrings[i];
                        if (widestString == null){
                            continue;
                        }
                        mw = Math.max(mw, fm.stringWidth(widestString));
                    }
                }
                maxDimension.width = mw + ins.left + ins.right;
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(maxDimension);
        }

        public Dimension getMinimumSize(){
            return new Dimension(maxDimension);
        }
    }
}       

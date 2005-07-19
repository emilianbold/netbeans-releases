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
package org.netbeans.modules.palette.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;


/**
 * Glass pane which is used for paint of a drop line over <code>JComponent</code>.
 *
 * @author  Jiri Rechtacek, S. Aubrecht
 *
 * @see java.awt.dnd.DropTarget
 * @see org.openide.explorer.view.TreeViewDropSupport
 */
final class DropGlassPane extends JPanel {
    static private HashMap map = new HashMap();
    final static private int MIN_X = 0;//5;
    final static private int MIN_Y = 0;//3;
    final static private int MIN_WIDTH = 0;//10;
    final static private int MIN_HEIGTH = 0;//3;
    transient static private Component oldPane;
    transient static private JComponent originalSource;
    transient static private boolean wasVisible;
    Line2D line = null;
    Rectangle prevLineRect = null;

    private DropGlassPane() {
    }

    /** Check the bounds of given line with the bounds of this pane. Optionally
     * calculate the new bounds in current pane's boundary.
     * @param comp
     * @return  */
    synchronized static public DropGlassPane getDefault(JComponent comp) {
        Integer id = new Integer(System.identityHashCode(comp));

        if ((map.get(id)) == null) {
            DropGlassPane dgp = new DropGlassPane();
            dgp.setOpaque(false);
            map.put(id, dgp);
        }

        return (DropGlassPane) map.get(id);
    }

    /** Stores the original glass pane on given tree.
     * @param source the active container
     * @param pane the original glass
     * @param visible was glass pane visible
     */
    static void setOriginalPane( JComponent source, Component pane, boolean visible ) {
        // pending, should throw an exception that original is set already
        oldPane = pane;
        originalSource = source;
        wasVisible = visible;
    }

    /** Is any original glass pane stored?
     * @return true if true; false otherwise
     */
    static boolean isOriginalPaneStored() {
        return oldPane != null;
    }

    /** Sets the original glass pane to the root pane of stored container.
     */
    static void putBackOriginal() {
        if (oldPane == null) {
            // pending, should throw an exception
            return;
        }

        originalSource.getRootPane().setGlassPane(oldPane);
        oldPane.setVisible(wasVisible);
        oldPane = null;
    }

    /** Unset drop line if setVisible to false.
     * @param boolean aFlag new state */
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        if (!aFlag) {
            setDropLine(null);
        }
    }

    /** Set drop line. Given line is used by paint method.
     * @param line drop line */
    public void setDropLine(Line2D line) {
        if( !isValid() )
            return;
        
        if( null != prevLineRect 
            && ((null != line
                && (!prevLineRect.contains( line.getP1() )
                    || !prevLineRect.contains( line.getP2() )))
                || null == line) ) {
            
            repaint( prevLineRect );
        }

        this.line = line;
        Rectangle newLineRect = null;
        if( null != this.line ) {
            checkLineBounds( this.line );
            newLineRect = line.getBounds();
            newLineRect.grow( 5, 5 );
        }
        
        if( null != newLineRect && !newLineRect.equals( prevLineRect ) ) {
            repaint( newLineRect );
        }
        prevLineRect = newLineRect;
    }

    /** Check the bounds of given line with the bounds of this pane. Optionally
     * calculate the new bounds in current pane's boundary.
     * @param line a line for check
     * @return  a line with bounds inside the pane's boundary */
    private Line2D checkLineBounds(Line2D line) {
        Rectangle bounds = getBounds();
        double startPointX;
        double startPointY;
        double endPointX;
        double endPointY;
        
        // check start point
        startPointX = Math.max(line.getX1(), bounds.x + MIN_X);
        startPointY = Math.max(line.getY1(), bounds.y + MIN_Y);

        // check end point
        endPointX = Math.min(line.getX2(), (bounds.x + bounds.width));// - MIN_WIDTH);
        endPointY = Math.min(line.getY2(), (bounds.y + bounds.height) - MIN_HEIGTH);

        // set new bounds
        line.setLine(startPointX, startPointY, endPointX, endPointY);

        return line;
    }

    /** Paint drop line on glass pane.
     * @param Graphics g Obtained graphics */
    public void paint(Graphics g) {
        if (line != null) {
        
            int x1 = (int) line.getX1();
            int x2 = (int) line.getX2();
            int y1 = (int) line.getY1();
            int y2 = (int)line.getY2 ();
                    
            if( y1 == y2 ) {
                // LINE
                g.drawLine(x1 + 2, y1, x2 - 2, y1);
                g.drawLine(x1 + 2, y1 + 1, x2 - 2, y1 + 1);

                // RIGHT
                g.drawLine(x1, y1 - 2, x1, y1 + 3);
                g.drawLine(x1 + 1, y2 - 1, x1 + 1, y1 + 2);

                // LEFT
                g.drawLine(x2, y1 - 2, x2, y1 + 3);
                g.drawLine(x2 - 1, y1 - 1, x2 - 1, y1 + 2);
            } else {
                // LINE
                g.drawLine(x1, y1 + 2, x2, y2 - 2);
                g.drawLine(x1 + 1, y1 + 2, x2 + 1, y2 - 2);

                // RIGHT
                g.drawLine(x1 - 2, y1, x1 + 3, y1);
                g.drawLine(x1 - 1, y1 + 1, x1 + 2, y1 + 1);

                // LEFT
                g.drawLine(x2 - 2, y2, x2 + 3, y2);
                g.drawLine(x2 - 1, y2 - 1, x2 + 2, y2 - 1);
            }
        }
    }
}

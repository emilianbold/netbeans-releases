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

package org.netbeans.modules.palette.ui;

import java.awt.dnd.Autoscroll;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.palette.Category;


/**
 * @author David Kaspar, Jan Stola
 */
class CategoryButton extends JCheckBox implements Autoscroll {

    private static final Icon openedIcon = (Icon)UIManager.get("Tree.expandedIcon"); // NOI18N
    private static final Icon closedIcon = (Icon)UIManager.get("Tree.collapsedIcon"); // NOI18N
    static final Color BK_COLOR = UIManager.getColor("Aqua".equals(UIManager.getLookAndFeel().getID()) // NOI18N
        ? "Table.selectionBackground" : "PropSheet.setBackground"); // NOI18N

    private CategoryDescriptor descriptor;
    private Category category;
    
    private AutoscrollSupport support;

    CategoryButton( CategoryDescriptor descriptor, Category category ) {
        this.descriptor = descriptor;
        this.category = category;

        setBackground( BK_COLOR );
        setFont( getFont().deriveFont( Font.BOLD ) );
        setMargin(new Insets(0, 3, 0, 3));
        if( getBorder() instanceof CompoundBorder ) { // from BasicLookAndFeel
            Dimension pref = getPreferredSize();
            pref.height -= 3;
            setPreferredSize( pref );
        }
        setFocusPainted( false );

        setSelected( false );

        setHorizontalAlignment( SwingConstants.LEFT );
        setHorizontalTextPosition( SwingConstants.RIGHT );
        setVerticalTextPosition( SwingConstants.CENTER );

        updateProperties();

        addActionListener( new ActionListener () {
            public void actionPerformed( ActionEvent e ) {
                boolean opened = !CategoryButton.this.descriptor.isOpened();
                setExpanded( opened );
            }
        });
    }

    void updateProperties() {
        setIcon( closedIcon );
        setSelectedIcon( openedIcon );
        setText( category.getDisplayName() );
        setToolTipText( category.getShortDescription() );
    }
    
    Category getCategory() {
        return category;
    }

    
    /** notify the Component to autoscroll */
    public void autoscroll( Point cursorLoc ) {
        Point p = SwingUtilities.convertPoint( this, cursorLoc, getParent().getParent() );
        getSupport().autoscroll( p );
    }

    /** @return the Insets describing the autoscrolling
     * region or border relative to the geometry of the
     * implementing Component.
     */
    public Insets getAutoscrollInsets() {
        return getSupport().getAutoscrollInsets();
    }
    
    boolean isExpanded() {
        return isSelected();
    }
    
    void setExpanded( boolean expand ) {
        setSelected( expand );
        descriptor.setOpened( expand );
        descriptor.getPalettePanel().computeHeights( expand ? CategoryButton.this.category : null );
        requestFocus ();
    }

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( getParent().getParent() );
        }

        return support;
    }
}

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicListUI;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;

/**
 * Specialized JList for palette items (content of a palette category) - having
 * special UI and renderer providing fine-tuned alignment, rollover effect,
 * showing names and different icon size. Used by CategoryDescriptor.
 *
 * @author Tomas Pavek, S. Aubrecht
 */

public class CategoryList extends JList implements Autoscroll {

    static final String laf = UIManager.getLookAndFeel ().getClass ().getName ();
    static final boolean isMetalLAF = laf.equals ("javax.swing.plaf.metal.MetalLookAndFeel"); // NOI18N
    static final boolean isWindowsLAF = laf.equals ("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); // NOI18N
    static final Color panelBackgroundColor = UIManager.getDefaults ().getColor ("Panel.background");

    private int rolloverIndex = -1;
    private boolean showNames;

    static final int BASIC_ICONSIZE = BeanInfo.ICON_COLOR_16x16;
    private int iconSize = BASIC_ICONSIZE;
    
    private Category category;

    private static WeakReference rendererRef;
    
    private Item draggingItem;
    
    private AutoscrollSupport support;

    /**
     * Constructor.
     */
    CategoryList( Category category ) {
        this.category = category;
        setBackground(panelBackgroundColor);
        setBorder (new EmptyBorder (0, 0, 0, 0));
        setVisibleRowCount (0);
        setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer (getItemRenderer ());
        setLayoutOrientation( HORIZONTAL_WRAP );
    }

    Item getItemAt( int index ) {
        if( index < 0 || index >= getModel().getSize() )
            return null;
        
        return (Item)getModel().getElementAt( index );
    }

    Category getCategory() {
        return category;
    }

    public void updateUI () {
        setUI (new CategoryListUI ());
        invalidate ();
    }

    // Workaround for issue 39037. Due to the following method we can
    // use getPreferredSize() in the implementation of the method
    // getPreferredHeight(). Otherwise we would have to copy the content
    // of getPreferredSize() into the layout manager of the enclosing JScrollPane.
    // We cannot change the width directly through setBounds() method
    // because it would force another repaint.
    Integer tempWidth;

    public int getWidth () {
        return (tempWidth == null) ? super.getWidth () : tempWidth.intValue ();
    }

    // ---------

    boolean getShowNames () {
        return showNames;
    }

    void setShowNames (boolean show) {
        if (show != showNames) {
            showNames = show;
            firePropertyChange ("cellRenderer", null, null); // NOI18N
        }
    }

    int getIconSize () {
        return iconSize;
    }

    void setIconSize (int size) {
        if (size != iconSize) {
            iconSize = size;
            firePropertyChange ("cellRenderer", null, null); // NOI18N
        }
    }

    // workaround for bug 4832765, which can cause the
    // scroll pane to not let the user easily scroll up to the beginning
    // of the list.  An alternative would be to set the unitIncrement
    // of the JScrollBar to a fixed value. You wouldn't get the nice
    // aligned scrolling, but it should work.
    public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction) {
        int row;
        if (orientation == SwingConstants.VERTICAL &&
                direction < 0 && (row = getFirstVisibleIndex ()) != -1) {
            Rectangle r = getCellBounds (row, row);
            if ((r.y == visibleRect.y) && (row != 0)) {
                Point loc = r.getLocation ();
                loc.y--;
                int prevIndex = locationToIndex (loc);
                Rectangle prevR = getCellBounds (prevIndex, prevIndex);

                if (prevR == null || prevR.y >= r.y) {
                    return 0;
                }
                return prevR.height;
            }
        }
        return super.getScrollableUnitIncrement (visibleRect, orientation, direction);
    }

    /**
     * Returns preferred height of the list for the specified <code>width</code>.
     *
     * @return preferred height of the list for the specified <code>width</code>.
     */
    public int getPreferredHeight (int width) {
        return ((CategoryListUI) getUI ()).getPreferredHeight (width);
    }
    
    public void resetRollover() {
        rolloverIndex = -1;
        repaint();
    }
    
    int getColumnCount() {
        if( getModel().getSize() > 0 ) {
            Insets insets = getInsets ();
            int listWidth = getWidth () - (insets.left + insets.right);
            int cellWidth = getCellBounds( 0, 0 ).width;
            if( listWidth >= cellWidth ) {
                return listWidth / cellWidth;
            }
        }
        return 1;
    }

    // --------
    // list item renderer

    private static ListCellRenderer getItemRenderer () {
        ListCellRenderer renderer = rendererRef == null ? null :
                (ListCellRenderer) rendererRef.get ();
        if (renderer == null) {
            renderer = new ItemRenderer ();
            rendererRef = new WeakReference (renderer);
        }
        return renderer;
    }

    static class ItemRenderer implements ListCellRenderer {

        private static JToggleButton button;
        private static Border defaultBorder;

        ItemRenderer () {
            if (button == null) {
                button = new JToggleButton ();

                if (isMetalLAF  ||  isWindowsLAF) { // for Metal and Windows Look&Feel use toolbar button rendering
                    button.setMargin (new Insets (1, 1, 1, 0));
                    JToolBar toolbar = new JToolBar ();
                    toolbar.setRollover (true);
                    toolbar.setFloatable (false);
                    toolbar.setLayout (new BorderLayout (0, 0));
                    toolbar.setBorder (new EmptyBorder (0, 0, 0, 0));
                    toolbar.add (button);
                } else { // otherwise use normal button with default or empty border
                    button.setMargin (new Insets (1, 1, 1, 1));
                    defaultBorder = button.getBorder ();
                }
            }
        }

        public Component getListCellRendererComponent (JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            CategoryList categoryList = (CategoryList) list;
            boolean showNames = categoryList.getShowNames ();
            int iconSize = categoryList.getIconSize ();

            JComponent rendererComponent = defaultBorder == null ?
                    (JComponent) button.getParent () : button;

            Item item = (Item) value;
            Image icon = item.getIcon (iconSize);
            if (icon != null) {
                button.setIcon (new ImageIcon (icon));
            }

            button.setText (showNames ? item.getDisplayName () : null);
            rendererComponent.setToolTipText( item.getShortDescription() ); // NOI18N

            button.setSelected (isSelected);
            if (defaultBorder == null) { // Windows or Metal
                // let the toolbar UI render the button according to "rollover"
                button.getModel ().setRollover (index == categoryList.rolloverIndex && !isSelected);
            } else { // Mac OS X and others - set the border explicitly
                button.setBorder (defaultBorder);
            }
            button.setBorderPainted ((index == categoryList.rolloverIndex) || isSelected);

            button.setHorizontalAlignment (showNames ? SwingConstants.LEFT : SwingConstants.CENTER);
            button.setHorizontalTextPosition (SwingConstants.RIGHT);
            button.setVerticalTextPosition (SwingConstants.CENTER);

            return rendererComponent;
        }
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

    /** Safe getter for autoscroll support. */
    AutoscrollSupport getSupport() {
        if( null == support ) {
            support = new AutoscrollSupport( getParent().getParent() );
        }

        return support;
    }
    
    
    // ---------
    // list UI
    
    static class CategoryListUI extends BasicListUI {

        protected void updateLayoutState () {
            super.updateLayoutState ();

            if (list.getLayoutOrientation () == JList.HORIZONTAL_WRAP) {
                Insets insets = list.getInsets ();
                int listWidth = list.getWidth () - (insets.left + insets.right);
                if (listWidth >= cellWidth) {
                    int columnCount = listWidth / cellWidth;
                    cellWidth = (columnCount == 0) ? 1 : listWidth / columnCount;
                }
            }
        }

        public int getPreferredHeight (int width) {
            ((CategoryList) list).tempWidth = new Integer (width);
            int result;
            try {
                result = (int) getPreferredSize (list).getHeight ();
            } finally {
                ((CategoryList) list).tempWidth = null;
            }
            return result;
        }

        protected MouseInputListener createMouseInputListener () {
            return new ListMouseInputHandler ();
        }

        private class ListMouseInputHandler extends MouseInputHandler {

            public void mouseClicked(MouseEvent e) {
                if( !list.isEnabled() )
                    return;
                
                int selIndex = getValidIndex( e.getPoint() );
                
                if( 1 == e.getClickCount() ) {
                    if( selIndex >= 0 ) {
                        if( list.getSelectedIndex() == selIndex ) {
                            list.clearSelection();
                        } else {
                            list.setSelectedIndex( selIndex );
                        }
                    }
                } else {
                    Item item = (Item)list.getModel().getElementAt( selIndex );
                    item.invokePreferredAction( e, "doubleclick" );
                }
                e.consume();
            }

            public void mousePressed( MouseEvent e ) {
            }

            public void mouseDragged( MouseEvent e ) {
            }

            public void mouseMoved( MouseEvent e ) {
                mouseEntered( e );
            }

            public void mouseEntered( MouseEvent e ) {
                if( list.isEnabled() )
                    setRolloverIndex( getValidIndex( e.getPoint() ) );
            }

            public void mouseExited( MouseEvent e ) {
                if( list.isEnabled() )
                    setRolloverIndex( -1 );
            }

            public void mouseReleased( MouseEvent e ) {
            }

            private void setRolloverIndex (int index) {
                int oldIndex = ((CategoryList) list).rolloverIndex;
                if (index != oldIndex) {
                    ((CategoryList) list).rolloverIndex = index;
                    if (oldIndex > -1) {
                        Rectangle r = getCellBounds (list, oldIndex, oldIndex);
                        if (r != null)
                            list.repaint (r.x, r.y, r.width, r.height);
                    }
                    if (index > -1) {
                        Rectangle r = getCellBounds (list, index, index);
                        if (r != null)
                            list.repaint (r.x, r.y, r.width, r.height);
                    }
                }
            }
        }

        private int getValidIndex (Point p) {
            int index = locationToIndex (list, p);
            return index >= 0 && getCellBounds (list, index, index).contains (p) ?
                    index : -1;
        }
    }
}

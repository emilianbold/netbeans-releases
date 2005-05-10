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

package org.netbeans.core.windows.view.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;


/**
 * Wrapper class for MultiSplitPane's split divider rectangle.
 */
public class MultiSplitDivider implements Accessible {
    
    MultiSplitPane splitPane;
    Rectangle rect = new Rectangle();
    MultiSplitCell first;
    MultiSplitCell second;
    
    Point currentDragLocation;
    int dragMin;
    int dragMax;
    int cursorPositionCompensation;
    
    private AccessibleContext accessibleContext;

    public MultiSplitDivider( MultiSplitPane parent, MultiSplitCell first, MultiSplitCell second ) {
        assert null != parent;
        assert null != first;
        assert null != second;
        this.splitPane = parent;
        this.first = first;
        this.second = second;
        
        reshape();
    }

    boolean isHorizontal() {
        return splitPane.isHorizontalSplit();
    }

    boolean isVertical() {
        return splitPane.isVerticalSplit();
    }
    
    int getDividerSize() {
        return splitPane.getDividerSize();
    }

    boolean containsPoint( Point p ) {
        return rect.contains( p );
    }
    
    void paint( Graphics g ) {
        //the split bar does not paint anything when not being dragged
        //JPanel's background color is used as default
        
        if( null != currentDragLocation ) {
            Color oldColor = g.getColor();
            g.setColor( Color.BLACK );
            if( isHorizontal() ) {
                if( currentDragLocation.x != rect.x ) {
                    g.fillRect( currentDragLocation.x, rect.y, rect.width, rect.height );
                }
            } else {
                if( currentDragLocation.y != rect.y ) {
                    g.fillRect( rect.x, currentDragLocation.y, rect.width, rect.height );
                }
            }
            g.setColor( oldColor );
        }
    }
    
    void startDragging( Point p ) {
        currentDragLocation = new Point( rect.x, rect.y );

        if( isHorizontal() )
            cursorPositionCompensation = p.x - rect.x;
        else
            cursorPositionCompensation = p.y - rect.y;
        
        initDragMinMax();
    }
    
    void dragTo( Point p ) {
        if( isHorizontal() ) {
            if( p.x < dragMin )
                p.x = dragMin;
            if( p.x > dragMax )
                p.x = dragMax;
        } else {
            if( p.y < dragMin )
                p.y = dragMin;
            if( p.y > dragMax )
                p.y = dragMax;
        }
        
        Point prevDragLocation = currentDragLocation;
        currentDragLocation = p;
        
        repaintSplitPane( prevDragLocation );
        repaintSplitPane( currentDragLocation );
    }
    
    private void repaintSplitPane( Point location ) {
        if( isHorizontal() ) {
            splitPane.repaint( location.x, rect.y, rect.width, rect.height );
        } else {
            splitPane.repaint( rect.x, location.y, rect.width, rect.height );
        }
    }
    
    void finishDraggingTo( Point p ) {
        if( isHorizontal() ) {
            p.x -= cursorPositionCompensation;
            if( p.x < dragMin )
                p.x = dragMin;
            if( p.x > dragMax )
                p.x = dragMax;
            
            if( p.x == rect.x ) {
                //split bar position didn't change
                return;
            }
        } else {
            p.y -= cursorPositionCompensation;
            if( p.y < dragMin )
                p.y = dragMin;
            if( p.y > dragMax )
                p.y = dragMax;

            if( p.y == rect.y ) {
                //split bar position didn't change
                return;
            }
        }
        currentDragLocation = null;
    
        int dividerSize = getDividerSize();
        
        if( isHorizontal() ) {
            int delta = p.x - rect.x;
            int x = first.getLocation();
            int y = 0;
            int width = first.getSize() + delta;
            int height = rect.height;
            first.layout( x, y, width, height );
            
            x = second.getLocation() + delta;
            width = second.getSize() - delta;
            second.layout( x, y, width, height );
            
            rect.x = p.x;
        } else {
            int delta = p.y - rect.y;
            int x = 0;
            int y = first.getLocation();
            int width = rect.width;
            int height = first.getSize() + delta;
            first.layout( x, y, width, height );
            
            y = second.getLocation() + delta;
            height = second.getSize() - delta;
            second.layout( x, y, width, height );

            rect.y = p.y;
        }
        splitPane.validate();//invalidate();
    }
    
    private void initDragMinMax() {
        int dividerSize = getDividerSize();
        int firstSize = first.getSize();
        int secondSize = second.getSize();
        int firstMinSize = first.getMinimumSize();
        int secondMinSize = second.getMinimumSize();
        
        if( isHorizontal() ) {
            dragMin = rect.x;
            dragMax = rect.x;
        } else {
            dragMin = rect.y;
            dragMax = rect.y;
        }
            
        if( firstSize >= firstMinSize ) {
            dragMin -= firstSize-firstMinSize;
        }
        if( secondSize >= secondMinSize ) {
            dragMax += secondSize-secondMinSize;
        }
    }
    
    void reshape() {
        Dimension d = splitPane.getSize();
        int location = second.getLocation();

        if( isHorizontal() ) {
            rect.x = location-getDividerSize();
            rect.y = 0;
            rect.width = getDividerSize();
            rect.height = d.height;
        } else {
            rect.x = 0;
            rect.y = location-getDividerSize();
            rect.width = d.width;
            rect.height = getDividerSize();
        }
    }
    
    // *************************************************************************
    // Accessibility
    
    public AccessibleContext getAccessibleContext() {
        if( null == accessibleContext ) {
            accessibleContext = new AccessibleMultiSplitDivider();
        }
        return accessibleContext;
    }
    
    protected class AccessibleMultiSplitDivider extends AccessibleContext
        implements AccessibleValue {
        
        public AccessibleMultiSplitDivider() {
            setAccessibleParent( splitPane );
        }
        
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        public int getAccessibleChildrenCount() {
            return 0;
        }

        public int getAccessibleIndexInParent() {
            return splitPane.getDividerAccessibleIndex( MultiSplitDivider.this );
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPLIT_PANE;
        }

        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet stateSet = new AccessibleStateSet();
            if( isHorizontal() ) {
                stateSet.add( AccessibleState.HORIZONTAL );
            } else {
                stateSet.add( AccessibleState.VERTICAL );
            }
            return stateSet;
        }

        public Locale getLocale() throws java.awt.IllegalComponentStateException {
            return Locale.getDefault();
        }
        
        public boolean setCurrentAccessibleValue(Number n) {
            initDragMinMax();
            int value = n.intValue();
            if( value < dragMin || value > dragMax ) {
                return false;
            }
            if( isHorizontal() ) {
                finishDraggingTo( new Point( value, 0 ) );
            } else {
                finishDraggingTo( new Point( 0, value ) );
            }
            return true;
        }

        public Number getMinimumAccessibleValue() {
            initDragMinMax();
            return new Integer( dragMin );
        }

        public Number getMaximumAccessibleValue() {
            initDragMinMax();
            return new Integer( dragMax );
        }

        public Number getCurrentAccessibleValue() {
            if( isHorizontal() )
                return new Integer( rect.x );
            else
                return new Integer( rect.y );
        }

        public AccessibleValue getAccessibleValue() {
            return this;
        }
    } //end of AccessibleMultiSplitDivider inner class
}

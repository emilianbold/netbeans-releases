/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.MultiSplitPane;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Class which represents model of split element for GUI hierarchy. 
 *
 * @author  Peter Zavadsky
 */
public class SplitView extends ViewElement {
    
    private int orientation;
    
    private ArrayList /*Double*/ splitWeights;
    
    private ArrayList /*ViewElement*/ children;
    
    private MultiSplitPane splitPane;
    
    public SplitView(Controller controller, double resizeWeight, 
                        int orientation, List splitWeights, List children) {
        super(controller, resizeWeight);
        
        this.orientation = orientation;
        this.splitWeights = new ArrayList( splitWeights );
        this.children = new ArrayList( children );
    }

    public void setOrientation( int newOrientation ) {
        this.orientation = newOrientation;
    }
    
    public void setSplitWeights( List newSplitWeights ) {
        splitWeights.clear();
        splitWeights.addAll( newSplitWeights );
    }
    
    public int getOrientation() {
        return orientation;
    }
    
    public List getChildren() {
        return new ArrayList( children );
    }
    
    public Component getComponent() {
        return getSplitPane();
    }
    
    public void remove( ViewElement view ) {
        int index = children.indexOf( view );
        if( index >= 0 ) {
            children.remove( index );
            splitWeights.remove( index );
            if( null != splitPane ) {
                splitPane.removeViewElementAt( index );
            }
        }
    }
    
    public void setChildren( List newChildren ) {
        children.clear();
        children.addAll( newChildren );
        
        assert children.size() == splitWeights.size();
        
        if( null != splitPane ) {
            updateSplitPane();
        }
    }
    
    public boolean updateAWTHierarchy(Dimension availableSpace) {
        boolean res = false;
        
        if( !availableSpace.equals( getSplitPane().getSize() ) ) { 
            getSplitPane().setSize( availableSpace );
            getSplitPane().invalidate();
            res = true;
        }
        for( Iterator i=children.iterator(); i.hasNext(); ) {
            ViewElement child = (ViewElement)i.next();
            res |= child.updateAWTHierarchy( child.getComponent().getSize() );
        }
        
        return res;
    }
    
    private MultiSplitPane getSplitPane() {
        if(splitPane == null) {
            splitPane = new MultiSplitPane();
            updateSplitPane();

            
            splitPane.setDividerSize(orientation == JSplitPane.VERTICAL_SPLIT
                ? Constants.DIVIDER_SIZE_VERTICAL
                : Constants.DIVIDER_SIZE_HORIZONTAL);
            
            splitPane.setBorder(BorderFactory.createEmptyBorder());
            
            splitPane.addPropertyChangeListener("splitPositions", // NOI18N
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        ArrayList weights = new ArrayList( children.size() );
                        ArrayList views = new ArrayList( children.size() );
                        splitPane.calculateSplitWeights( views, weights );
                        ViewElement[] arrViews = new ViewElement[views.size()];
                        double[] arrWeights = new double[views.size()]; 
                        for( int i=0; i<views.size(); i++ ) {
                            arrViews[i] = (ViewElement)views.get( i );
                            arrWeights[i] = ((Double)weights.get( i )).doubleValue();
                        }
                        getController().userMovedSplit( SplitView.this, arrViews, arrWeights );
                    }
                });
        }
        
        return splitPane;
    }

    public int getDividerSize() {
        return getSplitPane().getDividerSize();
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append( super.toString() );
        buffer.append( "[" ); // NOI18N
        for( int i=0; i<children.size(); i++ ) {
            ViewElement child = (ViewElement)children.get( i );
            buffer.append( (i+1) );
            buffer.append( '=' );
            if( child instanceof SplitView ) {
                buffer.append( child.getClass() );
                buffer.append( '@' ); // NOI18N
                buffer.append( Integer.toHexString(child.hashCode()) );
            } else {
                buffer.append( child.toString() );
            }
            if( i < children.size()-1 )
                buffer.append( ", " ); // NOI18N
        }
        buffer.append( "]" ); // NOI18N
        
        return buffer.toString();
    }
    
    private void updateSplitPane() {
        ViewElement[] arrViews = new ViewElement[children.size()];
        double[] arrSplitWeights = new double[children.size()];
        for( int i=0; i<children.size(); i++ ) {
            ViewElement view = (ViewElement)children.get( i );
            
            arrViews[i] = view;
            arrSplitWeights[i] = ((Double)splitWeights.get(i)).doubleValue();
        }
        splitPane.setChildren( orientation, arrViews, arrSplitWeights );
    }

    private static void debugLog(String message) {
        Debug.log(SplitView.class, message);
    }
}


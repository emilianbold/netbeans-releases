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


package org.netbeans.core.windows.view.ui;


import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeListener;
import javax.swing.Icon;

import org.openide.windows.TopComponent;


/**
 * Interface describing component which is used inside <code>SimpleContainer</code>.
 * There will be at two implementations one for view and second one for editor type.
 *
 * @author  Peter Zavadsky
 */
public interface Tabbed {
    
    /** Name of property change when user closed TopComponent, (via close button). */
    public String PROP_TOPCOMPONENT_CLOSED = "topComponentClosed";
    
    
    public void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip);

    public void insertTopComponent(String name, Icon icon, TopComponent tc, String toolTip, int position);
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public int getTopComponentCount();
    
    public TopComponent[] getTopComponents();
    
    public TopComponent getTopComponentAt(int index);
    
    public int indexOfTopComponent(TopComponent tc);
    
    public void removeTopComponent(TopComponent tc);
    
    public void setTitleAt(int index, String title);
    
    public void setIconAt(int index, Icon icon);
    
    public void setToolTipTextAt(int index, String toolTip);
    
    public void setSelectedTopComponent(TopComponent tc);
    
    public TopComponent getSelectedTopComponent();
    
    public void setTabPlacement(int placement);
    
    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);
    
    public void addPropertyChangeListener(String name, PropertyChangeListener listener);
    
    public void removePropertyChangeListener(String name, PropertyChangeListener listener);

    public void setActive(boolean active);
    
    public int tabForCoordinate(int x, int y);
   
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
            Point startingPoint, boolean attachingPossible);
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible);
    
    public Image getDragImage (TopComponent tc);
    
    /** Indicates whether the point is in tab close button.
     @param p <code>Point</code> in coordinates of this component */
    public boolean isPointInCloseButton (Point p);
}


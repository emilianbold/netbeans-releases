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


import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.NestedSplitPane;


/**
 * Class which represents model of split element for GUI hierarchy. 
 *
 * @author  Peter Zavadsky
 */
public class SplitView extends ViewElement {
    
    private int orientation;
    
    private double location;
    
    private ViewElement first;
    private ViewElement second;
    
    private JSplitPane splitPane;
    
    /** Debugging flag. */
    private static boolean DEBUG = Debug.isLoggable(SplitView.class);

    
    
    public SplitView(Controller controller, double resizeWeight,
    int orientation, double location, ViewElement first, ViewElement second) {
        super(controller, resizeWeight);
        
        this.orientation = orientation;
        this.location = location;
        this.first = first;
        this.second = second;
    }

    
    public void setLocation(double location) {
        this.location = location;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        if(splitPane != null) {
            splitPane.setOrientation(orientation);
        }
    }
    
    public int getOrientation() {
        return orientation;
    }
    
    public double getLocation() {
        return location;
    }

    public ViewElement getFirst() {
        return first;
    }
    
    public ViewElement getSecond() {
        return second;
    }
    
    public void setFirst(ViewElement first) {
        // No validation here, the component could differ, currently EditorView.
        this.first = first;
    }
    
    public void setSecond(ViewElement second) {
        // No validation here, the component could differ, currently EditorView.
        this.second = second;
    }

    
    public Component getComponent() {
        assureComponentInSplit(first.getComponent(), true);
        assureComponentInSplit(second.getComponent(), false);
        return getSplitPane();
    }

    private void assureComponentInSplit(Component comp, boolean left){
        // Component is in use, adjust it if necessary.
        Container parent = comp.getParent();
        if(parent == getSplitPane()) {
            return;
        }
        if(parent != null) {
            parent.remove(comp);
        }
        
        int location = getSplitPane().getDividerLocation(); // keep split position
        if(left) {
            splitPane.setLeftComponent(comp);
        } else {
            splitPane.setRightComponent(comp);
        }
        splitPane.setDividerLocation(location);
    }
    
    private JSplitPane getSplitPane() {
        if(splitPane == null) {
            splitPane = new NestedSplitPane(this, orientation,
                first.getComponent(), second.getComponent());

            // Set resize weight.
            double firstResize  = first.getResizeWeight();
            double secondResize = second.getResizeWeight();
            double resize;
            if(firstResize == 0D && secondResize == 0.D) {
                resize = 0.5D;
            } else if(firstResize == 0D) {
                resize = 0D;
            } else if(secondResize == 0D) {
                resize = 1D;
            } else {
                resize = firstResize / (firstResize + secondResize);
            }
            splitPane.setResizeWeight(resize);
            
            splitPane.setDividerSize(orientation == JSplitPane.VERTICAL_SPLIT
                ? Constants.DIVIDER_SIZE_VERTICAL
                : Constants.DIVIDER_SIZE_HORIZONTAL);
            
            splitPane.setBorder(BorderFactory.createEmptyBorder());
            
            splitPane.addPropertyChangeListener("topDividerLocation", // NOI18N
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        int absoluteLocation = ((Integer)evt.getNewValue()).intValue();
                        double relativeLocation;
                        if(splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                            relativeLocation = (double)absoluteLocation/(splitPane.getHeight() - splitPane.getDividerSize());
                        } else {
                            relativeLocation = (double)absoluteLocation/(splitPane.getWidth() - splitPane.getDividerSize());
                        }
                        
                        getController().userMovedSplit(relativeLocation,
                            SplitView.this, getFirst(), getSecond());
                    }
                }
            );

        }
        
        return splitPane;
    }

    public int getDividerSize() {
        return getSplitPane().getDividerSize();
    }
    
    public void updateSplit(java.awt.Dimension realSize) {
        int splitPosition;
        
        if(orientation == JSplitPane.VERTICAL_SPLIT) {
            splitPosition = (int)(realSize.height * location);
        } else {
            splitPosition = (int)(realSize.width * location);
        }

        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Initing split=" + this); // NOI18N
            debugLog("location=" + location); // NOI18N
            debugLog("size=" + realSize); // NOI18N
            debugLog("old position=" + getSplitPane().getDividerLocation()); // NOI18N
            debugLog("location=" + splitPosition); // NOI18N
        }
        
        // XXX SplitPane needs to be valid when setting divider position.
        // Have a look at #4188905, #4182558 etc. in BugParade.
        getSplitPane().validate();
        getSplitPane().setDividerLocation(splitPosition);
    }

    public String toString() {
        String str1 = first instanceof SplitView ? first.getClass() + "@" + Integer.toHexString(first.hashCode()) : first.toString(); // NOI18N
        String str2 = second instanceof SplitView ? second.getClass() + "@" + Integer.toHexString(second.hashCode()) : second.toString(); // NOI18N
        
        return super.toString() + "[first=" + str1 + ", second=" + str2 + "]"; // NOI18N
    }

    private static void debugLog(String message) {
        Debug.log(SplitView.class, message);
    }
}


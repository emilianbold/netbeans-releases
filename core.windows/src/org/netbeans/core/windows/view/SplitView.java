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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.NestedSplitPane;

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
    
    private double location;
    
    private ViewElement first;
    private ViewElement second;
    
    private JSplitPane splitPane;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(SplitView.class);

    
    
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
        this.first = first;
    }
    
    public void setSecond(ViewElement second) {
        this.second = second;
    }

    
    public Component getComponent() {
        return getSplitPane();
    }
    
    
    public void updateAWTHierarchy(Dimension availableSpace) {
        Dimension firstDim;
        Dimension secondDim;
        double location = getLocation();
        int dividerSize = getDividerSize();
        if (getOrientation() == javax.swing.JSplitPane.VERTICAL_SPLIT) {
                firstDim = new Dimension(availableSpace.width, (int)(availableSpace.height * location) - dividerSize);
                secondDim = new Dimension(availableSpace.width, (int)(availableSpace.height * (1D - location)) - dividerSize);
        } else {
                firstDim = new Dimension((int)(availableSpace.width * location) - dividerSize, availableSpace.height);
                secondDim = new Dimension((int)(availableSpace.width * (1D - location)) - dividerSize, availableSpace.height);
        }
//        debugLog("left=" + firstDim.width + " " + first.getClass());
//        debugLog("right=" + secondDim.width + " " + second.getClass());
        getSplitPane().setPreferredSize(availableSpace);
        first.updateAWTHierarchy(firstDim);
        second.updateAWTHierarchy(secondDim);
        assureComponentInSplit(first.getComponent(), true);
        assureComponentInSplit(second.getComponent(), false);
        if (first.getComponent() == null || second.getComponent() == null) {
//            debugLog("setting divider");
            splitPane.setDividerLocation(location);
        } else {
//            debugLog("reset to preffered sizes");
            splitPane.resetToPreferredSizes();
            // need to reset child splitpanes after resetting the parent.
            // not sure why but otherwise one gets strange resize effects..
            //
            if (first instanceof SplitView) {
//                debugLog("reseting left..");
                ((JSplitPane)first.getComponent()).resetToPreferredSizes();
            }
            if (second instanceof SplitView) {
//                debugLog("reseting right..");
                ((JSplitPane)second.getComponent()).resetToPreferredSizes();
            }
        }
    }
    
    private void assureComponentInSplit(Component comp, boolean left){
        // previously: Component is in use, adjust it if necessary.
        // now don't touch the component when in use.. just in case it's not anywhere, put to default place..
        
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
//        splitPane.setDividerLocation(location);
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
//                debugLog("creating splitpane - equal resize");
                resize = 0.5D;
            } else if(firstResize == 0D) {
//                debugLog("creating splitpane - right wins" + second.getClass());
                resize = 0D;
            } else if(secondResize == 0D) {
//                debugLog("creating splitpane - left wins" + first.getClass());
                resize = 1D;
            } else {
                resize = firstResize / (firstResize + secondResize);
//                debugLog("creating splitpane - splitting weight=" + resize);
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
    
    public String toString() {
        String str1 = first instanceof SplitView ? first.getClass() + "@" + Integer.toHexString(first.hashCode()) : first.toString(); // NOI18N
        String str2 = second instanceof SplitView ? second.getClass() + "@" + Integer.toHexString(second.hashCode()) : second.toString(); // NOI18N
        
        return super.toString() + "[first=" + str1 + ", second=" + str2 + "]"; // NOI18N
    }

    private static void debugLog(String message) {
        Debug.log(SplitView.class, message);
    }
}


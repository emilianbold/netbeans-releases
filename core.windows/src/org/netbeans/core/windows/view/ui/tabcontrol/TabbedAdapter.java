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

package org.netbeans.core.windows.view.ui.tabcontrol;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.geom.GeneralPath;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.openide.windows.TopComponent;
import org.openide.ErrorManager;
import java.awt.Image;

/** Adapter class that implements a pseudo JTabbedPane API on top
 * of the new tab control.  This class should eventually be eliminated
 * and the TabbedContainer's model-driven API should be used directly.
 *
 * @author  Tim Boudreau
 */
public class TabbedAdapter extends TabbedContainer implements Tabbed {
    
    public static final int DOCUMENT = 1;
    
    /** Utility field holding list of ChangeListeners. */
    private transient java.util.ArrayList changeListenerList;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(TabbedAdapter.class);
    
    /** Creates a new instance of TabbedAdapter */
    public TabbedAdapter (int type) {
        super(type == Constants.MODE_KIND_EDITOR 
                ? (TabbedContainer.TabsDisplayer)new TabControl(new DefaultTabDataModel())
                : (TabbedContainer.TabsDisplayer)new ViewTabControl(new DefaultTabDataModel()));                                
        getSelectionModel().addChangeListener(this);
    }
    
    public void addTopComponent(String name, javax.swing.Icon icon, TopComponent tc, String toolTip) {
        insertTopComponent (name, icon, tc, toolTip, getTopComponentCount());
    }
    
    public TopComponent getTopComponentAt(int index) {
        return (TopComponent)getModel().getTab(index).getComponent();
    }
    
    public TopComponent getSelectedTopComponent() {
        int i = getSelectionModel().getSelectedIndex();
        return i == -1 ? null : getTopComponentAt(i);
    }
    
    public int getTopComponentCount() {
        return getModel().size();
    }
    
    public int indexOfTopComponent(TopComponent tc) {
        int max = model.size();
        TabDataModel mdl = getModel();
        for (int i=0; i < max; i++) {
            TabData curr = mdl.getTab(i);
            if (tc == curr.getComponent()) return i;
        }
        return -1;
    }
    
    public void insertTopComponent(String name, javax.swing.Icon icon, TopComponent tc, String toolTip, int position) {
        TabData td = new TabData (tc, icon, name, toolTip);
        
        if(DEBUG) {
            debugLog("InsertTab: " + name + " hash:" + System.identityHashCode(tc)); // NOI18N
        }
        
        getModel().addTab(position, td);
    }
    
    protected void initDisplayer () {
        super.initDisplayer();
        TopComponent tc = getSelectedTopComponent();
    }
    
    public void setIconAt(int index, javax.swing.Icon icon) {
        getModel().setIcon(index, icon);
    }
    
    public void setSelectedTopComponent(TopComponent tc) {
        int i = indexOfTopComponent (tc);
        if (i == -1) {
            throw new IllegalArgumentException (
                "Component not a child of this control: " + tc); //NOI18N
        } else {
            getSelectionModel().setSelectedIndex(i);
        }
    }
    
    public TopComponent[] getTopComponents() {
        int max = getModel().size();
        TopComponent[] result = new TopComponent[max];
        for (int i=0; i < max; i++) {
            result[i] = (TopComponent) getModel().getTab(i).getComponent();
        }
        return result;
    }
    
    public void removeTopComponent(TopComponent tc) {
        int i=indexOfTopComponent(tc);
        getModel().removeTab(i);
    }
    
    public void setTabPlacement(int placement) {
        //XXX not supported, this will be handled by using different views/ui's
    }
    
    public void setTitleAt(int index, String title) {
        getModel().setText(index, title);
    }
    
    public void setToolTipTextAt(int index, String toolTip) {
        // PENDING revise.
        
        //The idea here is to silently change the tooltip text in the
        //model without triggering an event from the model (it is very unlikely
        //that the text will change *while* the tooltip happens to be showing) -Tim
        TabData tabData = getModel().getTab(index);
        if(tabData != null) {
            tabData.tip  = toolTip;
        }
    }
    
    public void addTopComponents(TopComponent[] tcs, String[] names, javax.swing.Icon[] icons, String[] tips) {
        ArrayList al = new ArrayList (tcs.length);
        TabData[] data = new TabData[tcs.length];
        for (int i=0; i < tcs.length; i++) {
            TabData td = new TabData (tcs[i], icons[i], names[i], tips[i]);
            data[i] = td;
        }
        getModel().addTabs (0, data);
    }

    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        assert selected != null : "Null passed as component to select";
        
        TabData[] data = new TabData[tcs.length];
        int toSelect=-1;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            Image icon = tc.getIcon();
            String displayName = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            data[i] = new TabData(
                tc,
                icon == null ? null : new ImageIcon(icon),
                displayName == null ? "" : displayName, // NOI18N
                tc.getToolTipText());
            if (selected == tcs[i]) {
                toSelect = i;
            }
        }

        //DO NOT DELETE THIS ASSERTION AGAIN!
        //If it triggered, it means there is a problem in the state of the
        //window system's model.  If it is just diagnostic logging, there
        //*will* be an exception later, it just won't contain any useful
        //information. See issue 39914 for what happens if it is deleted.
        assert toSelect != -1 : "Tried to set a selected component that was " +
            " not in the array of open components. ToSelect: " + selected + 
            " open components: " + Arrays.asList(tcs);
        
        getModel().setTabs(data);
        
        if (toSelect != -1) {
            getSelectionModel().setSelectedIndex(toSelect);
        } else {
            //Assertions are off
            ErrorManager.getDefault().log (ErrorManager.WARNING, "Tried to" +
            "set a selected component that was not in the array of open " +
            "components.  ToSelect: " + selected + " components: " + 
            Arrays.asList(tcs));
        }
    }
    
    // DnD>>
    /** Finds tab which contains x coordinate of given location point.
     * @param input point
     * @return Integer object representing found tab index. Returns null if
     * no such tab can be found.
     */
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        if(attachingPossible) {
            String s = getSideForLocation(location);
            if(s != null) {
                return s;
            }
        }
        
        TabbedContainer.TabsDisplayer tabs = getTabsDisplayer();
        // ignore original y axis to create projection into z-axis
        int newY = tabs.getComponent().getHeight() / 2;
        int index = tabs.getTabsUI().getLayoutModel().dropIndexOfPoint(location.x, newY);
        return index < 0 ? null : new Integer(index);
    }

    /** Computes and returns feedback indication shape for given location
     * point.
     * TBD - extend for various feedback types
     * @return Shape representing feedback indication
     */
    public Shape getIndicationForLocation(Point location,
    TopComponent startingTransfer, Point startingPoint, boolean attachingPossible) {
        Rectangle rect = getBounds();
        rect.setLocation(0, 0);
        
        String side;
        if(attachingPossible) {
            side = getSideForLocation(location);
        } else {
            side = null;
        }
        
        double ratio = Constants.DROP_TO_SIDE_RATIO;
        if(side == Constants.TOP) {
            return new Rectangle(0, 0, rect.width, (int)(rect.height * ratio));
        } else if(side == Constants.LEFT) {
            return new Rectangle(0, 0, (int)(rect.width * ratio), rect.height);
        } else if(side == Constants.RIGHT) {
            return new Rectangle(rect.width - (int)(rect.width * ratio), 0, (int)(rect.width * ratio), rect.height);
        } else if(side == Constants.BOTTOM) {
            return new Rectangle(0, rect.height - (int)(rect.height * ratio), rect.width, (int)(rect.height * ratio));
        }

        Shape s = getTabIndication(startingTransfer, location);
        if(s != null) {
            return s;
        }
        
        if(startingPoint != null
        && Arrays.asList(getTopComponents()).contains(startingTransfer)) {
            return getStartingIndication(startingPoint, location);
        }
        
        return rect;
    }

    private String getSideForLocation(Point location) {
        Rectangle bounds = getBounds();
        bounds.setLocation(0, 0);
        
        final int TOP_HEIGHT = 10;
        final int BOTTOM_HEIGHT = (int)(0.25 * bounds.height);
        TabLayoutModel mdl = tabs.getTabsUI().getLayoutModel();
        final int LEFT_WIDTH = Math.min((int)(0.25 * mdl.getW(0)), 30);
        final int RIGHT_WIDTH = Math.min((int)(0.25 * mdl.getW(getTopComponentCount() - 1)), 30);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("TOP_HEIGHT    =" + TOP_HEIGHT); // NOI18N
            debugLog("BOTTOM_HEIGHT =" + BOTTOM_HEIGHT); // NOI18N
            debugLog("LEFT_WIDTH    =" + LEFT_WIDTH); // NOI18N
            debugLog("RIGHT_WIDTH   =" + RIGHT_WIDTH); // NOI18N
        }
        
        // Size of area which indicates creation of new split.
//        int delta = Constants.DROP_AREA_SIZE;
        
        Rectangle top = new Rectangle(0, 0, bounds.width, TOP_HEIGHT);
        if(top.contains(location)) {
            return Constants.TOP;
        }
        
        Polygon left = new Polygon(
            new int[] {0, LEFT_WIDTH, LEFT_WIDTH, 0},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height},
            4
        );
        if(left.contains(location)) {
            return Constants.LEFT;
        }
        
        Polygon right = new Polygon(
            new int[] {bounds.width - RIGHT_WIDTH, bounds.width, bounds.width, bounds.width - RIGHT_WIDTH},
            new int[] {TOP_HEIGHT, TOP_HEIGHT, bounds.height, bounds.height - BOTTOM_HEIGHT},
            4
        );
        if(right.contains(location)) {
            return Constants.RIGHT;
        }

        Polygon bottom = new Polygon(
            new int[] {LEFT_WIDTH, bounds.width - RIGHT_WIDTH, bounds.width, 0},
            new int[] {bounds.height - BOTTOM_HEIGHT, bounds.height - BOTTOM_HEIGHT, bounds.height, bounds.height},
            4
        );
        if(bottom.contains(location)) {
            return Constants.BOTTOM;
        }
            
        return null;
    }
    
    private Shape getTabIndication(TopComponent startingTransfer, Point location) {
        TabbedContainer.TabsDisplayer tabs = getTabsDisplayer();
        int newY = tabs.getComponent().getHeight() / 2;
        int index = tabs.getTabsUI().getLayoutModel().dropIndexOfPoint(location.x, newY);
        if(index < 0) { // XXX PENDING The tab is not found, later simulate the last one.
            Rectangle r = getBounds();
            r.setLocation(0, 0);
            return r;
        }
        int tabsHeight = tabs.getComponent().getHeight();
        Polygon p;
        int startingIndex = indexOfTopComponent(startingTransfer);
        if(startingIndex >= 0 && (startingIndex == index || startingIndex + 1 == index)) {
            // merge with indication from tabs UI
            p = (Polygon) tabs.getTabsUI().getExactTabIndication(startingIndex);
        } else {
            // merge with indication from tabs UI
            p = (Polygon) tabs.getTabsUI().getInsertTabIndication(index);
        }
        
        int width = getWidth();
        int height = getHeight();
        
        int[] xpoints = new int[p.npoints + 4];
        int[] ypoints = new int[xpoints.length];
        
        int pos = 0;
        
        xpoints[pos] = 0;
        ypoints[pos] = tabsHeight;
        
        pos++;
        
        xpoints[pos] = p.xpoints[p.npoints-1];
        ypoints[pos] = tabsHeight;
        pos++;
        
        for (int i=0; i < p.npoints-2; i++) {
            xpoints [pos] = p.xpoints[i];
            ypoints [pos] = p.ypoints[i];
            pos++;
        }

        xpoints[pos] = xpoints[pos-1];
        ypoints[pos] = tabsHeight;
        
        pos++;
        
        xpoints[pos] = width - 1;
        ypoints[pos] = tabsHeight;
        
        pos++;
        
        xpoints[pos] = width - 1;
        ypoints[pos] = height -1;
        
        pos++;
        
        xpoints[pos] = 0;
        ypoints[pos] = height - 1;
        
        Polygon result = new EqualPolygon (xpoints, ypoints, xpoints.length);
        return result;
    }
     
    private Shape getStartingIndication(Point startingPoint, Point location) {
        Rectangle rect = getBounds();
        rect.setLocation(location.x - startingPoint.x, location.y - startingPoint.y);
        return rect;
    }
    // DnD<<

    
    /** Registers ChangeListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList == null ) {
            changeListenerList = new java.util.ArrayList();
        }
        changeListenerList.add(listener);
    }    
    
    /** Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList != null ) {
            changeListenerList.remove(listener);
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     *
     */
    private void fireStateChanged(javax.swing.event.ChangeEvent event) {
        java.util.ArrayList list;
        synchronized (this) {
            if (changeListenerList == null) return;
            list = (java.util.ArrayList)changeListenerList.clone();
        }
        //Note: Firing the events while holding the tree lock avoids many
        //gratuitous repaints that slow down switching tabs.  To demonstrate this,
        //comment this code out and run the IDE with -J-Dawt.nativeDoubleBuffering=true
        //so you'll really see every repaint.  When switching between a form
        //tab and an editor tab, you will see the property sheet get repainted
        //8 times due to changes in the component hierarchy, before the 
        //selected node is event changed to the appropriate one for the new tab.
        //Synchronizing here ensures that never happens.
        
        //XXX need to double check that this code is *never* called from
        //non AWT thread
        if (!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                "All state changes to the tab component must happen on the event thread!"); //NOI18N
            Exception e = new Exception();
            e.fillInStackTrace();
            System.err.println(e.getStackTrace()[1]);
        }
        
        synchronized (getTreeLock()) {
            for (int i = 0; i < list.size(); i++) {
                ((javax.swing.event.ChangeListener)list.get(i)).stateChanged(event);
            }
        }
    }
    
    public void stateChanged (javax.swing.event.ChangeEvent e) {
        TopComponent tc = getSelectedTopComponent();
        
        super.stateChanged(e);  //move this after?
        fireStateChanged(new ChangeEvent (this));
    }

    
    private static void debugLog(String message) {
        Debug.log(TabbedAdapter.class, message);
    }
    
    public boolean isPointInCloseButton(Point p) {
        return getTabsDisplayer().isPointInCloseButton(p);
    }
    
    public Image getDragImage(TopComponent tc) {
        TabbedContainer.TabsDisplayer tabs = getTabsDisplayer();
        Polygon p;
        int idx = indexOfTopComponent(tc);
        return tabs.getDragImage(idx);
    }
    
}

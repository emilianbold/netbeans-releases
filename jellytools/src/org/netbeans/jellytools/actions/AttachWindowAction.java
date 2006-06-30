/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.openide.windows.TopComponent;

/** Used to attach a window to a new position by IDE API.
 * It also defines constants used for attaching.
 * <p>
 * Usage:<br>
 * <pre>
        TopComponentOperator tco = new TopComponentOperator("Runtime");
        // attach Runtime top component right to Execution View
        new AttachWindowAction("Execution View", AttachWindowAction.RIGHT).perform(tco);
        Thread.sleep(2000);
        // attach Runtime top component back (next to Filesystems as the last tab)
        new AttachWindowAction("Filesystems", AttachWindowAction.AS_LAST_TAB).perform(tco);
 * </pre>
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class AttachWindowAction extends Action {
    /* Attach action is currently hidden in menus. If it is enabled in some
     * next release, only uncomment apropriate methods.
     *
     * // former class javadoc:
     * Used to call "Attach to" popup menu item, "Window|Attach "MyWindow" to" main menu item.
     * It also defines constants used for attaching.
     * @see Action
     */
    
    /** Constants used in "Attach to" menu items. */
    /* As a Last Tab */
    private static final String AS_LAST_TAB_LABEL = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_SideAsLastTab");
    private static final String RIGHT_LABEL = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_SideRight");
    private static final String LEFT_LABEL = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_SideLeft");
    private static final String TOP_LABEL = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_SideTop");
    private static final String BOTTOM_LABEL = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_SideBottom");
    public static final String DOCUMENTS = Bundle.getString(
                                        "org.netbeans.core.windows.actions.Bundle",
                                        "CTL_Documents");
    
    public static final String BOTTOM = Constants.BOTTOM;
    public static final String TOP = Constants.TOP;
    public static final String RIGHT = Constants.RIGHT;
    public static final String LEFT = Constants.LEFT;
    public static final String AS_LAST_TAB = "As a Last Tab";
    
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                                                     "Menu/Window");

    /** Parameter used in menu operations. E.g. "As a Last Tab", "Top", "Right", ... */
    private String sideItem;
    /** Parameter used in API operations. */
    private String sideConstant;

    // at least one of variables is defined in constructor
    private String targetTopComponentName;
    private TopComponentOperator targetTopComponentOperator;
    
    /** Create new AttachWindowAction instance.
     * @param targetTopComponentName name of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(String targetTopComponentName, String side) {
        super(null, null);
        if(targetTopComponentName == null) {
            throw new IllegalArgumentException("targetTopComponentName cannot be null.");
        }
        this.targetTopComponentName = targetTopComponentName;
        mapSide(side);
    }
    
    /** Create new AttachWindowAction instance.
     * @param targetTopComponentOperator operator of top component defining a position
     * where to attach top component
     * @param side side where to attach top component ({@link #LEFT}, 
     * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #AS_LAST_TAB})
     */
    public AttachWindowAction(TopComponentOperator targetTopComponentOperator, String side) {
        super(null, null);
        if(targetTopComponentOperator == null) {
            throw new IllegalArgumentException("targetTopComponentOperator cannot be null.");
        }
        this.targetTopComponentOperator = targetTopComponentOperator;
        mapSide(side);
    }
    
    private String getTargetTopComponentName() {
        if(targetTopComponentName == null) {
            // it is guaranteed targetTopComponentOperator != null
            targetTopComponentName = targetTopComponentOperator.getName();
        }
        return targetTopComponentName;
    }

    private TopComponentOperator getTargetTopComponentOperator() {
        if(targetTopComponentOperator == null) {
            // it is guaranteed targetTopComponentName != null
            targetTopComponentOperator = new TopComponentOperator(targetTopComponentName);
        }
        return targetTopComponentOperator;
    }

    /** Set sideItem and sideConstant from given parameter.
     */
    private void mapSide(String side) {
        if(side == null || side == AS_LAST_TAB || getComparator().equals(AS_LAST_TAB_LABEL, side)) {
            sideItem = AS_LAST_TAB_LABEL;
            sideConstant = AS_LAST_TAB;
        } else if(side == RIGHT || getComparator().equals(RIGHT_LABEL, side)) {
            sideItem = RIGHT_LABEL;
            sideConstant = RIGHT;
        } else if(side == LEFT || getComparator().equals(LEFT_LABEL, side)) {
            sideItem = LEFT_LABEL;
            sideConstant = LEFT;
        } else if(side == TOP || getComparator().equals(TOP_LABEL, side)) {
            sideItem = TOP_LABEL;
            sideConstant = TOP;
        } else if(side == BOTTOM || getComparator().equals(BOTTOM_LABEL, side)) {
            sideItem = BOTTOM_LABEL;
            sideConstant = BOTTOM;
        } else {
            throw new JemmyException("Cannot attach to position \""+side+"\".");
        }
    }

    /** Currently hidden
    public void performMenu(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performMenu((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                "AttachWindowAction can only be called on TopComponentOperator.");
        }
    }
     */

    /** Currently hidden.
    public void performMenu(TopComponentOperator tco) {
        // Window|Attach "tco.name" to|target.name|Top
        menuPath = windowItem+"|"+
                   Bundle.getString("org.netbeans.core.windows.actions.Bundle",
                                    "CTL_AttachWindowAction", 
                                    new String[] {tco.getName()})+
                    "|"+getTargetTopComponentName()+"|"+sideItem;
        getTargetTopComponentOperator().makeComponentVisible();
        tco.makeComponentVisible();
        performMenu();
    }
     */
    
    /** Perform popup action on given TopComponent's tab.
     * @param tco TopComponentOperator instance on which perform
     * the action
     */
    /* Currently not enabled on top components tab. 
    public void performPopup(TopComponentOperator tco) {
        // TODO
        // Attach "SourceTC" to|taargetTC|Top"
        String popupPath = Bundle.getString("org.netbeans.core.windows.actions.Bundle",
        "CTL_AttachWindowAction", new String[] {tco.getName()});
        menuPath += "|"+getTargetTopComponentName()+"|"+sideItem;
        getTargetTopComponentOperator().makeComponentVisible();
        tco.pushMenuOnTab(popupPath);
    }
     */

    /** Attach given TopComponentOperator to position specified in constructor
     * of action. It also waints until given TopComponent is showing in the 
     * new position.
     * @param compOperator TopComponentOperator which should be attached to desired
     * position
     */
    public void performAPI(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performAPI((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                "AttachWindowAction can only be called on TopComponentOperator.");
        }
    }
        
    /** Attaches given TopComponentOperator to position specified in constructor
     * of action. It also waints until given TopComponent is showing in the 
     * new position.
     * @param tco TopComponentOperator which should be attached to desired
     * position
     */
    public void performAPI(TopComponentOperator tco) {
        final TopComponent sourceTc = (TopComponent)tco.getSource();
        final TopComponent targetTc = (TopComponent)getTargetTopComponentOperator().getSource();
        // run in dispatch thread
        tco.getQueueTool().invokeSmoothly(new Runnable() {
            public void run() {
                ModeImpl mode = (ModeImpl)WindowManagerImpl.getDefault().findMode(targetTc);
                if(sideConstant == AS_LAST_TAB) {
                    mode.dockInto(sourceTc);
                    sourceTc.open();
                    sourceTc.requestActive();
                } else {
                    WindowManagerImpl.getInstance().attachTopComponentToSide(sourceTc, mode, sideConstant);
                }
            }
        });
        
        // wait until TopComponent is in new location, i.e. is showing
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object tc) {
                    return ((TopComponent)tc).isShowing() ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("TopComponent is showing."); // NOI18N
                }
            }).waitAction(sourceTc);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e); // NOI18N
        }
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because AttachWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
            "AttachWindowAction doesn't have popup representation on nodes.");
    }
}
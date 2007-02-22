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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * NbFocusControl.java
 *
 * Created on March 26, 2006, 7:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.awt.event.MouseEvent;
import prefuse.Visualization;
import prefuse.controls.FocusControl;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class NbFocusControl extends FocusControl{
    
    private String group = Visualization.FOCUS_ITEMS;
    protected SelectionMode selectionMode = SelectionMode.MULTIPLE;
    
    public enum SelectionMode {SINGLE, MULTIPLE};
    
    /**
     * Creates a new FocusControl that changes the focus to another item
     * when that item is clicked once.
     */
    public NbFocusControl() {
        super();
        
    }
    
    /**
     * Creates a new FocusControl that changes the focus to another item
     * when that item is clicked once.
     * @param focusGroup the name of the focus group to use
     */
    public NbFocusControl(String focusGroup) {
        super(focusGroup);
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     */
    public NbFocusControl(int clicks) {
        super(clicks);
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param focusGroup the name of the focus group to use
     * @param clicks the number of clicks needed to switch the focus.
     */
    public NbFocusControl(String focusGroup, int clicks) {
        super(focusGroup, clicks);
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     * @param act an action run to upon focus change
     */
    public NbFocusControl(int clicks, String act) {
        super(clicks,  act);
    }
    
    /**
     * Creates a new FocusControl that changes the focus when an item is
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param focusGroup the name of the focus group to use
     * @param clicks the number of clicks needed to switch the focus.
     * @param act an action run to upon focus change
     */
    public NbFocusControl(String focusGroup, int clicks, String act) {
        super(focusGroup, clicks, act);
    }
    
    
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( UILib.isButtonPressed(e, button) &&
                e.getClickCount() == ccount ) {
//            if ( item != curFocus ) {
                Visualization vis = item.getVisualization();
                TupleSet ts = vis.getFocusGroup(group);
                
                boolean ctrl = e.isControlDown();
                if ( !ctrl) {
                    curFocus = item;
                    ts.setTuple(item);
                } else if ( ts.containsTuple(item) ) {
                    ts.removeTuple(item);  
                    // MouseoverActionControl sets MOUSEOVER to true
                    //  Set MOUSEOVER to false so the node is
                    //  rendered without MOUSEOVER color
                    //  This mimics Windows selection behaviour for trees and 
                    //     lists.
                    if (item instanceof NodeItem && 
                            item.canSetBoolean(AnalysisConstants.MOUSEOVER)){
                        item.setBoolean(AnalysisConstants.MOUSEOVER, false); 
                    }
                } else {                    
                    if (selectionMode == SelectionMode.MULTIPLE){
                        ts.addTuple(item);
                    } else {
                        curFocus = item;
                        ts.setTuple(item);
                    }
                }
                runActivity(vis);
//            }
        }
    }
    
    
    
    
    protected void runActivity(Visualization vis) {
        if ( activity != null ) {
            vis.run(activity);
        }
    }
    
    public void setSelectionMode(SelectionMode mode){
        this.selectionMode = mode;
    }
    
    
    
    public SelectionMode getSelectionMode(){
        return this.selectionMode;
    }
    
}

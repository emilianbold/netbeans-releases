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
 * FocusTreversalManager.java
 *
 * Created on August 30, 2006, 6:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.xml.schema.abe.visitors.TraversalVisitor;
import org.netbeans.modules.xml.schema.abe.visitors.UpTraversalVisitor;
import org.netbeans.modules.xml.schema.abe.visitors.BackTraversalVisitor;
import org.netbeans.modules.xml.schema.abe.visitors.FrontTraversalVisitor;
import org.netbeans.modules.xml.schema.abe.visitors.DownTraversalVisitor;
import org.netbeans.modules.xml.schema.abe.visitors.TabTraversalVisitor;

/**
 *
 * @author girix
 */
public class FocusTraversalManager{
    
    public enum NavigationType{
        FRONT,
        BACK,
        UP,
        DOWN,
        NONE
    }
    boolean metaKeyDown;
    /** Creates a new instance of FocusTreversalManager */
    InstanceUIContext context;
    KeyEvent currentEvent;
    ABEBaseDropPanel currentComp;
    
    public FocusTraversalManager(InstanceUIContext context) {
        this.context = context;
    }
    
    public void handleEvent(KeyEvent e, ABEBaseDropPanel panel) {
        this.currentEvent = e;
        this.currentComp = panel;
        
        NavigationType navType = getNavigationType(e);
        ABEBaseDropPanel nextFocuComp = null;
        TraversalVisitor tv = null;
        switch(navType){
            case FRONT:
                tv = new FrontTraversalVisitor(panel);
                break;
            case BACK:
                tv = new BackTraversalVisitor(panel);
                break;
            case UP:
                tv = new UpTraversalVisitor(panel);
                break;
            case DOWN:
                tv = new DownTraversalVisitor(panel);
                break;
            case NONE:
                return;
        }
        panel.accept(tv);
        nextFocuComp = tv.getResult();
        if(nextFocuComp != null){
            if(e.isControlDown()){
                //multiple select
                context.getComponentSelectionManager().addToSelectedComponents(nextFocuComp);
            }else{
                //single select
                context.getComponentSelectionManager().setSelectedComponent(nextFocuComp);
            }
            UIUtilities.scrollViewTo(nextFocuComp, context);
        }
        //dont let the scroll pane scroll around
        e.consume();
        
    }
    
    public static NavigationType getNavigationType(KeyEvent e){
        int keyCode = e.getKeyCode();
        
        if(keyCode == KeyEvent.VK_RIGHT)
            return NavigationType.FRONT;
        
        if(keyCode == KeyEvent.VK_LEFT)
            return NavigationType.BACK;
       
        /* if(keyCode == KeyEvent.VK_UP)
            return NavigationType.UP;
        
        if(keyCode == KeyEvent.VK_DOWN)
            return NavigationType.DOWN;*/
        
        return NavigationType.NONE;
    }
    
    public boolean isFocusChangeEvent(KeyEvent e) {
        if(getNavigationType(e) == NavigationType.NONE)
            return false;
        return true;
    }
    
    
    
    
}

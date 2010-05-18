/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

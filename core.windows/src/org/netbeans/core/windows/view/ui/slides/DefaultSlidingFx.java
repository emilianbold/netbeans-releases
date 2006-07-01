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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeListener;

/**
 * default impl of SlidingFX.
 * Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Milos Kleint
 */
public class DefaultSlidingFx  implements SlidingFx {


    public void showEffect(JLayeredPane pane, Integer layer, SlideOperation operation) {
//        Component comp = operation.getComponent();
//        Graphics2D gr2d = (Graphics2D)pane.getGraphics();
//        Color original = gr2d.getColor();
//        gr2d.setColor(Color.BLUE.darker().darker());
//        Rectangle start = operation.getStartBounds();
//        Rectangle finish = operation.getFinishBounds();
//        Rectangle current = start;
//        for (int i = 0; i < 6 /** magic constant **/; i++) {
//            Rectangle newRect;
//            if (i > 0) {
//                // wipe out old
//            } 
//            newRect = new Rectangle();
//            newRect.x = Math.abs((current.x + finish.x) / 2);
//            newRect.y = Math.abs((current.y + finish.y) / 2);
//            newRect.height = Math.abs((current.height + finish.height) / 2);
//            newRect.width = Math.abs((current.width + finish.width) / 2);
//            gr2d.drawRect(newRect.x, newRect.y, newRect.width, newRect.height);
//            gr2d.setColor(gr2d.getColor().brighter());
//            current = newRect;
//        }
//        gr2d.setColor(original);
////        try {
////            Thread.sleep(5000);
////        } catch (Throwable th) {
////            
////        }
        
    }
     
    public void prepareEffect(SlideOperation operation) {
        // no preparation needed
    }    
    
    public void setFinishListener(ChangeListener finishL) {
        // no noperation, operation don't need to wait
    }
    
    public boolean shouldOperationWait() {
        return false;
    }
    
}
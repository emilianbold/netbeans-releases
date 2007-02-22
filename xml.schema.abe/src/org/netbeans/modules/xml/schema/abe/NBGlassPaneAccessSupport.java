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
 * InplaceEditingSupport.java
 *
 * Created on May 24, 2006, 3:08 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author girix
 */
public class NBGlassPaneAccessSupport {
    
    private static JPanel NB_GLASS_PANE = null;
    private static JPanel glass ;
    private static LayoutManager previousLayout;
    private static JFrame NBFRAME;
    
    private static JPanel _getGlassPane(Component leaf){
        if(NB_GLASS_PANE == null){
            //get fresh glass pane
            Component current = leaf;
            while( (current != null) && !(current instanceof JFrame) )
                current = current.getParent();
            if(current == null)
                return null;
            NB_GLASS_PANE = (JPanel) ((JFrame) current).getGlassPane();
            NBFRAME =  (JFrame) current;
            return NB_GLASS_PANE;
        }else{
            //check if the glass pane is still valid
            if(NB_GLASS_PANE.getParent() instanceof JFrame){
                //has frame as root. So valid just return.
                return NB_GLASS_PANE;
            }else{
                //parent is not a frame. Obtain glass pane again.
                NB_GLASS_PANE = null;
                return _getGlassPane(leaf);
            }
        }
    }
    
    
    public static JPanel getNBGlassPane(Component leaf){
        glass = _getGlassPane(leaf);
        if(glass == null)
            return null;
        previousLayout = glass.getLayout();
        glass.setLayout(null);
        return glass;
    }
    
    public static JPanel getCleanNBGlassPane(Component leaf){
        forceDisposeNBGlassPane();
        return getNBGlassPane(leaf);
    }
    
    public static void disposeNBGlassPane(){
        if(glass == null)
            return;
        glass.setVisible(false);
        for(Component comp: glass.getComponents()){
            glass.remove(comp);
        }
        glass.setLayout(previousLayout);
    }
    
    public static void forceDisposeNBGlassPane(){
        JPanel glass  = NB_GLASS_PANE;
        if(glass == null)
            return;
        glass.setVisible(false);
        for(Component comp: glass.getComponents()){
            glass.remove(comp);
        }
        glass.setLayout(previousLayout);
    }
    
    public static JPanel getCurrentShowingPane(){
        return glass;
    }
    
    public static JFrame getNBFRAME(Component leaf){
        if(NBFRAME != null)
            return NBFRAME;
        _getGlassPane(leaf);
        return NBFRAME;
    }
    
}

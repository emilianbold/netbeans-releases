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

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class MVDesc implements MultiViewDescription {
        
        protected String name;
        protected Image img;
        public transient MultiViewElement el;
        protected int type;
        
        
        public MVDesc() {
            
        }
        
        public MVDesc(String name, Image img, int persType, MultiViewElement element) {
            el = element;
            this.name = name;
            this.img = img;
            type = persType;
        }
        
        public MultiViewElement createElement() {
            if (el == null) {
                // for persistence.. elem is transient..
                el = new MVElem();
            }
            return el;
        }
        
        public String getDisplayName() {
            return name;
        }
        
        public org.openide.util.HelpCtx getHelpCtx() {
            return new HelpCtx(name);
        }
        
        public java.awt.Image getIcon() {
            return img;
        }
        
        public int getPersistenceType() {
            return type;
        }
        
        public String preferredID() {
            return name;
        }
        
    }
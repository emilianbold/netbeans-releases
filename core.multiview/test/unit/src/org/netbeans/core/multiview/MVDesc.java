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
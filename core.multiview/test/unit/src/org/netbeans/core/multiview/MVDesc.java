/*
 * MVElem.java
 *
 * Created on April 2, 2004, 3:02 PM
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
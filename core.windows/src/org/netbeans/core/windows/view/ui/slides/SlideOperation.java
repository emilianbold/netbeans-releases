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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import org.netbeans.core.windows.Constants;

/*
 * Interface for slide in and slide out operations. Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Dafe Simonek
 */
public interface SlideOperation {

    public Component getComponent ();
    
    public Component getButtonComponent ();
    
    public Rectangle getStartBounds ();
    
    public Rectangle getFinishBounds ();
    
    public String getSide ();

    public boolean requestsActivation ();
    
    public void run (JLayeredPane pane, Integer layer);
    
    public void setStartBounds (Rectangle bounds);
    
    public void setFinishBounds (Rectangle bounds);
    

    /** Basic abstract implementation of SlideOperation. Real implementors
     * can subclass this isntead of direct realization of SlideOperation
     * to save code
     */
    public abstract class AbstractImpl implements SlideOperation {
        
        /** Overall component that will be sliden, in winsys top component
         * surrounded by titlebar and border envelope */
        private final Component component;
        
        /** The button component that  is associated with the clise operation
         */
        private final Component buttonComponent;
        /** true when component should be activated after slide */
        private final boolean requestsActivation;
        /** Desktop side where slide operation happens */
        private final String side;
        /** Bounds from where should effect start */    
        protected Rectangle startBounds;
        /** Bounds into which should effect finish */
        protected Rectangle finishBounds;
        
        /** Creates a new instance of SlideInOperation */
        protected AbstractImpl(Component component, Component button, int orientation,
                               boolean requestsActivation) {
            this.component = component;
            buttonComponent = button;
            this.requestsActivation = requestsActivation;
            this.side = orientation2Side(orientation);
        }

        public void setFinishBounds(Rectangle bounds) {
            this.finishBounds = bounds;
        }

        public void setStartBounds(Rectangle bounds) {
            this.startBounds = bounds;
        }

        public String getSide() {
            return side;
        }

        public Component getComponent() {
            return component;
        }

        public Rectangle getFinishBounds() {
            return finishBounds;
        }

        public Rectangle getStartBounds() {
            return startBounds;
        }
        
        public boolean requestsActivation() {
            return requestsActivation;
        }
        
        protected static String orientation2Side (int orientation) {
            String side = Constants.LEFT; 
            if (orientation == SlideBarDataModel.WEST) {
                side = Constants.LEFT;
            } else if (orientation == SlideBarDataModel.EAST) {
                side = Constants.RIGHT;
            } else if (orientation == SlideBarDataModel.SOUTH) {
                side = Constants.BOTTOM;
            }
            return side;
        }
        
        public Component getButtonComponent() {
            return buttonComponent;
        }
        
    }
    
}

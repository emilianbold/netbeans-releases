/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ComponentConverter.java
 *
 * Created on March 28, 2004, 9:20 PM
 */

package org.netbeans.swing.tabcontrol;

import java.awt.*;

/** 
 * A class which can provide a component corresponding to a TabData object.
 * While TabData.getComponent can provide a Component via its getComponent()
 * method, there are use cases where the tabbed container should contain a
 * single component, and the data model should be used to control it (the
 * tabbed form of NetBeans' property sheet is one example).
 * <p>
 * A ComponentConvertor can be plugged into an instance of TabbedContainer
 * to enable it to display, for example, the same component for all tabs, which a listener
 * on its selection model will reconfigure for the selected tab; or it can
 * be used for lazy initialization, to construct components on demand.
 *
 * @author  Tim Boudreau
 */
public interface ComponentConverter {
    Component getComponent (TabData data);
    
    /** A default implementation which simply delegates to 
     * TabData.getComponent() */
    static final ComponentConverter DEFAULT = new ComponentConverter() {
        public Component getComponent(TabData data) {
            return data.getComponent();
        }
    };
    
    /** A ComponentConverter implementation which always returns the same
     * component */
    public static final class Fixed implements ComponentConverter {
        private final Component component;
        public Fixed (Component component) {
            this.component = component;
        }
        
        public Component getComponent(TabData data) {
            return component;
        }
    }
}

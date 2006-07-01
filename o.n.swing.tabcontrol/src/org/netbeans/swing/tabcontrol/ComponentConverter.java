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

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

package org.netbeans.swing.tabcontrol;

import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabDataModel;

/*
 * Data model of slide bar. It's the same as TabDataModel, but has 
 * orientation property in addition.
 *
 * @author Dafe Simonek
 */
public interface SlideBarDataModel extends TabDataModel {
    
    public static final int EAST = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;

    /** Orientation of slide bar
     */
    public int getOrientation ();
    
    /** Sets orientation of slide bar, possible values are EAST, WEST, SOUTH.
     */
    public void setOrientation (int orientation);

    /* Basic implementation of SlideBarDataModel.
     */
    public static class Impl extends DefaultTabDataModel implements SlideBarDataModel {

        /** Holds orientation of slide bar */
        private int orientation = EAST;

        /** Constructs new data model */
        public Impl () {
            super();
        }

        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public boolean isMaximized() {
            return false;
        }
        
    } // end of Impl
    
}

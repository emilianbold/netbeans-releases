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
        
    } // end of Impl
    
}

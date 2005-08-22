/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

/**
 * @author Tomas Pavek
 */

public interface LayoutConstants {

    // size constants

    /**
     * Indicates a size or position not defined in the layout model; it must
     * be obtained elsewhere (e.g. from a real component).
     */
    int NOT_EXPLICITLY_DEFINED = -1;

    /**
     * Specifies that a min or max size value should be the same as
     * the preferred size value.
     */
    int USE_PREFERRED_SIZE = -2;

    // structure type constants

    /**
     * Indicates a single layout interval without internal structure.
     */
    int SINGLE = 101;

    /**
     * Indicates a layout interval containing a sequence of sub-intervals
     * (placed one after anoother).
     */
    int SEQUENTIAL = 102;

    /**
     * Indicates a layout interval containing sub-intervals arranged parallely.
     */
    int PARALLEL = 103;

    // structure type attributes (bitwise)

    int CLOSED_GROUP = 1;

    // alignment constants (independent on orientation/axis)
    // also serves the role of index to array of positions

    int DEFAULT = -1;
    int LEADING = 0;
    int TRAILING = 1;
    int CENTER = 2;
    int BASELINE = 3;

    // orientation constants (dimensions)

    /**
     * Constant/index of the horizontal orientation (X axis).
     */
    int HORIZONTAL = 0;

    /**
     * Constant/index of the vertical orientation (Y axis).
     */
    int VERTICAL = 1;

    /**
     * The number of dimensions. Obviously 2 ;)
     */
    int DIM_COUNT = 2;

    // other constants

//    int MAX_OUT = Short.MAX_VALUE;
//    int MIN_OUT = Short.MIN_VALUE;
    String PROP_HORIZONTAL_MIN_SIZE = "horizontalMinSize"; // NOI18N
    String PROP_HORIZONTAL_PREF_SIZE = "horizontalPrefSize"; // NOI18N
    String PROP_HORIZONTAL_MAX_SIZE = "horizontalMaxSize"; // NOI18N
    String PROP_VERTICAL_MIN_SIZE = "verticalMinSize"; // NOI18N
    String PROP_VERTICAL_PREF_SIZE = "verticalPrefSize"; // NOI18N
    String PROP_VERTICAL_MAX_SIZE = "verticalMaxSize"; // NOI18N

    // are components in same linksizegroup?
    int INVALID = -1;
    int FALSE = 0;
    int TRUE = 1;
}

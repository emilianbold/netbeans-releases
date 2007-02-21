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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Color;


/**
 * <p>
 *
 * Description: </p> Stores global values used by the Gtk packaged. <p>
 *
 * @author    Jone Lin
 * @created   December 3, 2002
 */

public interface GtkGlobals {
    /**
     * Description of the Field
     */
    boolean DEBUG = false;
    /**
     * Description of the Field
     */
    Color LIGHT_YELLOW = new Color(255, 255, 216);

    /** Selected color for node label. */
    Color LABEL_BK_SELECTED = new Color(255, 227, 140);

    /** Border color of the label of a selected node. */
    Color LABEL_BORDER_SELECTED = Color.black;

    /** Border color of the label of a selected node. */
    int LABEL_FONT_SIZE = 11;

    /** milli-seconds between two slow clicks to start an edit session */
    int LABEL_TWO_SLOW_CLICKS = 1500;

    /**
     * Description of the Field
     */
    Color GROUP_BK_SELECTED = LIGHT_YELLOW;
    /**
     * Description of the Field
     */
    Color GROUP_BORDER = Color.lightGray;
    /**
     * Description of the Field
     */
    int GROUP_MARGIN = 5;
    /**
     * Description of the Field
     */
    int GROUP_INITIAL_OFFSET_X = 10;
    /**
     * Description of the Field
     */
    int GROUP_INITIAL_OFFSET_Y = 40;
}

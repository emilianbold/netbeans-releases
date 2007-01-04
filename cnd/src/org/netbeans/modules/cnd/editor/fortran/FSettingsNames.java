/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.editor.ext.ExtSettingsNames;

/**
* Names of the Fortran editor settings.
*/

public class FSettingsNames extends ExtSettingsNames {

    /** Determins if a space is inserted after the comma inside the parameter
     * list in a function call.
     * Values: java.lang.Boolean instances
     * Effect: function(a,b)
     *           becomes
     *         function(a, b)
     */
    //public static final String FORMAT_SPACE_AFTER_COMMA
    //                   = "fortran-format-space-after-comma"; //NOI18N

    /** Determines if the Fortran coding style is Free Format or Fixed Format
    * Values: java.lang.Boolean instances
    * Effect: Free format when set to true allows code to be placed in columns
    *         1 thru 132, a comment is designated by a "!" before the comment,
    *         etc. Fortran 90 and up style.
    */
    //public static final String FREE_FORMAT
    //                   = "fortran-free-format"; //NOI18N

}

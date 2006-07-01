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

package org.openide.loaders;

import java.beans.*;

/** Editor for sorting mode
 * @author Jaroslav Tulach, Jesse Glick
 */
class SortModeEditor extends PropertyEditorSupport {
    /** modes */
    private static final DataFolder.SortMode[] values = {
        DataFolder.SortMode.NONE,
        DataFolder.SortMode.NAMES,
        DataFolder.SortMode.CLASS,
        DataFolder.SortMode.FOLDER_NAMES,
        DataFolder.SortMode.LAST_MODIFIED,
        DataFolder.SortMode.SIZE,
    };

    /** Names for modes. First is for displaying files */
    private static final String[] modes = {
        DataObject.getString ("VALUE_sort_none"),
        DataObject.getString ("VALUE_sort_names"),
        DataObject.getString ("VALUE_sort_class"),
        DataObject.getString ("VALUE_sort_folder_names"),
        DataObject.getString ("VALUE_sort_last_modified"),
        DataObject.getString ("VALUE_sort_size"),
    };

    /** @return names of the two possible modes */
    public String[] getTags () {
        return modes;
    }

    /** @return text for the current value (File or Element mode) */
    public String getAsText () {
        Object obj = getValue ();
        for (int i = 0; i < values.length; i++) {
            if (obj == values[i]) {
                return modes[i];
            }
        }
        return null;
    }

    /** Setter.
    * @param str string equal to one value from modes array
    */
    public void setAsText (String str) {
        for (int i = 0; i < modes.length; i++) {
            if (str.equals (modes[i])) {
                setValue (values[i]);
                return;
            }
        }
    }
}

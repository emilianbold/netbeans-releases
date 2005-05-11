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

package org.netbeans.modules.beans;

import java.beans.*;

/** property editor for mode property of Prperty patterns
*
* @author Petr Hrebejk
*/
public class ModePropertyEditor extends PropertyEditorSupport {

    /** Array of tags
    */
    private static String[] tags;
    private static final int [] values = {
        PropertyPattern.READ_WRITE,
        PropertyPattern.READ_ONLY,
        PropertyPattern.WRITE_ONLY };

    /** @return names of the supported member Acces types */
    public String[] getTags() {
        if (tags == null) {
            tags = new String[] {
                PatternNode.getString( "LAB_ReadWriteMODE" ),
                PatternNode.getString( "LAB_ReadOnlyMODE" ),
                PatternNode.getString( "LAB_WriteOnlyMODE" )
            };
        }
        return tags;
    }

    /** @return text for the current value */
    public String getAsText () {
        int value = ((Integer)getValue()).intValue();

        for (int i = 0; i < values.length ; i++)
            if (values[i] == value)
                return getTags()[i];

        return PatternNode.getString( "LAB_Unsupported" );
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        for (int i = 0; i < getTags().length ; i++)
            if (getTags()[i] == text) {
                setValue(new Integer(values[i]));
                return;
            }

        setValue( new Integer(0) );
    }
}

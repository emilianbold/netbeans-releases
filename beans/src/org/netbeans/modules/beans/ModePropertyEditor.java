/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.beans;

import java.beans.*;
import sun.tools.util.ModifierFilter;

import org.openide.util.NbBundle;

/** property editor for mode property of Prperty patterns 
*
* @author Petr Hrebejk
*/
public class ModePropertyEditor extends PropertyEditorSupport {
  /** Array of tags 
  */


  private static final String[] tags = new String[3];
  private static final int [] values = {
                     PropertyPattern.READ_WRITE, 
                     PropertyPattern.READ_ONLY,
                     PropertyPattern.WRITE_ONLY };

  static {                                 
    tags[0]=PatternNode.bundle.getString( "LAB_ReadWriteMODE" );
    tags[1]=PatternNode.bundle.getString( "LAB_ReadOnlyMODE" );
    tags[2]=PatternNode.bundle.getString( "LAB_WriteOnlyMODE" );
    
  }

  /** @return names of the supported member Acces types */
  public String[] getTags() {
    return tags;
  }

  /** @return text for the current value */
  public String getAsText () {
    int value = ((Integer)getValue()).intValue();

    for (int i = 0; i < values.length ; i++) 
      if (values[i] == value) 
        return tags[i];

    return "unupported";
  }

  /** @param text A text for the current value. */
  public void setAsText (String text) {
    for (int i = 0; i < tags.length ; i++) 
      if (tags[i] == text) {
        setValue(new Integer(values[i]));
        return;
        }

    setValue( new Integer(0) );
  }
}

/*
 * Log
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $
 */

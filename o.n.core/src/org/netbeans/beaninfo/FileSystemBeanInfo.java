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

package com.netbeans.developer.impl.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.filesystems.*;

/** Object that provides beaninfo for {@link FileSystem}s.
*
* @author Ian Formanek
*/
public class FileSystemBeanInfo extends SimpleBeanInfo {

  /** Array of property descriptors. */
  private static PropertyDescriptor[] desc;

  // initialization of the array of descriptors
  static {
    try {
      desc = new PropertyDescriptor[] {
        new PropertyDescriptor ("readOnly", FileSystem.class, "isReadOnly", null), // 0
        new PropertyDescriptor ("valid", FileSystem.class, "isValid", null), // 1
        new PropertyDescriptor ("hidden", FileSystem.class, "isHidden", "setHidden") // 2
      };
      ResourceBundle bundle = NbBundle.getBundle(FileSystemBeanInfo.class);
      desc[0].setDisplayName (bundle.getString("PROP_readOnly"));
      desc[0].setShortDescription (bundle.getString("HINT_readOnly"));
      desc[1].setDisplayName (bundle.getString("PROP_valid"));
      desc[1].setShortDescription (bundle.getString("HINT_valid"));
      desc[2].setDisplayName (bundle.getString("PROP_hidden"));
      desc[2].setShortDescription (bundle.getString("HINT_hidden"));
    } catch (IntrospectionException ex) {
      //throw new InternalError ();
      ex.printStackTrace ();
    }
  }


  /* Descriptor of valid properties
  * @return array of properties
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    return desc;
  }

}


/*
 * Log
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         3/12/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/4/99   David Simonek   
 *  5    Gandalf   1.4         3/4/99   Petr Hamernik   
 *  4    Gandalf   1.3         3/4/99   Petr Hamernik   
 *  3    Gandalf   1.2         3/1/99   Jesse Glick     Typo.
 *  2    Gandalf   1.1         3/1/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */

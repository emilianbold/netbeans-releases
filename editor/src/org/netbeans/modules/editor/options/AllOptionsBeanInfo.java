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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AllOptionsBeanInfo extends BaseOptionsBeanInfo {
  
  public static final String[] PROP_NAMES = new String[] {
    BaseOptions.KEY_BINDING_LIST_PROP
  };

  public AllOptionsBeanInfo() {
    super("/org/netbeans/modules/editor/resources/allOptions", "base_"); // NOI18N
  }

  protected Class getBeanClass() {
    return AllOptions.class;
  }
  
  protected String[] getPropNames() {
    return PROP_NAMES;
  }
      
}

/*
* Log
*  6    Gandalf   1.5         1/13/00  Miloslav Metelka Localization
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/27/99  Miloslav Metelka 
*  3    Gandalf   1.2         7/9/99   Ales Novak      print options change
*  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package statement
*       to make it compilable
*  1    Gandalf   1.0         6/30/99  Ales Novak      
* $
*/

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

package com.netbeans.developer.modules.javadoc.search;

import javax.swing.ImageIcon;

/** <DESCRIPTION>

 @author Petr Hrebejk
*/
class DocSearchIcons extends Object {
	
  public static final int ICON_NOTRESOLVED = 0;
  public static final int ICON_PACKAGE = ICON_NOTRESOLVED + 1 ;
  public static final int ICON_CLASS = ICON_PACKAGE + 1 ;
  public static final int ICON_INTERFACE = ICON_CLASS + 1;
  public static final int ICON_EXCEPTION = ICON_INTERFACE + 1;
  public static final int ICON_CONSTRUCTOR = ICON_EXCEPTION + 1;
  public static final int ICON_METHOD = ICON_CONSTRUCTOR + 1;
  public static final int ICON_METHOD_ST = ICON_METHOD + 1;
  public static final int ICON_VARIABLE = ICON_METHOD_ST + 1;
  public static final int ICON_VARIABLE_ST = ICON_VARIABLE + 1;
  public static final int ICON_WAIT = ICON_VARIABLE_ST + 1;
  
  private static ImageIcon[] icons = new ImageIcon[ ICON_WAIT + 1 ];

  static {
    try {
      icons[ ICON_NOTRESOLVED ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/pending.gif"));
      icons[ ICON_PACKAGE ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/defaultFolder.gif"));
      icons[ ICON_CLASS ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/class.gif"));
      icons[ ICON_INTERFACE ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/interface.gif"));
      icons[ ICON_EXCEPTION ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/developer/modules/javadoc/resources/exception.gif"));
      icons[ ICON_CONSTRUCTOR ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/constructorPublic.gif"));
      icons[ ICON_METHOD ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/methodPublic.gif"));
      icons[ ICON_METHOD_ST ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/methodStPublic.gif"));
      icons[ ICON_VARIABLE ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/variablePublic.gif"));
      icons[ ICON_VARIABLE_ST ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/variableStPublic.gif"));
      icons[ ICON_WAIT ] = new ImageIcon (DocSearchIcons.class.getResource ("/org/openide/resources/src/wait.gif"));
    } 
    catch (Throwable w) {
      w.printStackTrace ();
    }
  }

  static ImageIcon getIcon( int index ) {
    return icons[ index ];
  }

}

/* 
 * Log
 *  6    Gandalf   1.5         8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  5    Gandalf   1.4         6/23/99  Petr Hrebejk    HTML doc view & sort 
 *       modes added
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 
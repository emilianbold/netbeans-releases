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
  public static final int ICON_CONSTRUCTOR = ICON_INTERFACE + 1;
  public static final int ICON_METHOD = ICON_CONSTRUCTOR + 1;
  public static final int ICON_METHOD_ST = ICON_METHOD + 1;
  public static final int ICON_VARIABLE = ICON_METHOD_ST + 1;
  public static final int ICON_VARIABLE_ST = ICON_VARIABLE + 1;

  private static ImageIcon[] icons = new ImageIcon[ ICON_VARIABLE_ST + 1 ];

  static {
    try {
      icons[ ICON_NOTRESOLVED ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/pending.gif"));
      icons[ ICON_PACKAGE ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/defaultFolder.gif"));
      icons[ ICON_CLASS ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/class.gif"));
      icons[ ICON_INTERFACE ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/interface.gif"));
      icons[ ICON_CONSTRUCTOR ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/constructorPublic.gif"));
      icons[ ICON_METHOD ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/methodPublic.gif"));
      icons[ ICON_METHOD_ST ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/methodStPublic.gif"));
      icons[ ICON_VARIABLE ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/variablePublic.gif"));
      icons[ ICON_VARIABLE_ST ] = new ImageIcon (DocSearchIcons.class.getResource ("/com/netbeans/ide/resources/src/variableStPublic.gif"));
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
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 
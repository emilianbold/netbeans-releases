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

package com.netbeans.developer.modules.text.options;

import java.awt.Font;
import java.awt.Color;
import java.beans.*;     

import com.netbeans.editor.Coloring;

public class ColoringBean implements java.io.Serializable {
    
  /** Encapsulated Coloring */
  transient Coloring coloring;
  
  /** example text */
  transient String example;

  public ColoringBean() {
  }
  
  public ColoringBean changeColoring( Coloring newColoring ) {
    return new ColoringBean( newColoring, example, defaultColoring, isDefault );
  }
  
  public boolean equals( Object o ) {
    if( o instanceof ColoringBean ) {
      ColoringBean c = (ColoringBean)o;
      return (
        ( ( (coloring == null) && (c.coloring == null) ) ||
          ( (coloring != null) && coloring.equals(c.coloring) ) ) &&
        ( ( (example == null) && (c.example == null) ) ||
          ( (example != null) && example.equals(c.example) ) ) &&
        ( ( (defaultColoring == null) && (c.defaultColoring == null) ) ||
          ( (defaultColoring != null) && defaultColoring.equals(c.defaultColoring) ) ) &&
        (isDefault == c.isDefault) );
    }
    return false;
  }
  
}


/*
 * Log
 *  6    Gandalf   1.5         12/28/99 Miloslav Metelka 
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */

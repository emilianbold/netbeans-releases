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

import com.netbeans.editor.Coloring;

public class ColoringBean implements java.io.Serializable {

  /** Encapsulated Coloring */
  Coloring coloring;
  
  /** Default Coloring */
  transient Coloring defaultColoring;

  /** example text */
  transient String example;

  static final long serialVersionUID =7093605647730152393L;
  public ColoringBean() {
  }
  
  public Coloring getColoring() {
    return coloring;
  }

  public void setColoring(Coloring coloring) {
    this.coloring = coloring;
  }

}


/*
 * Log
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */

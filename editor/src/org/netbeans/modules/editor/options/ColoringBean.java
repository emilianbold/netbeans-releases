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

  public ColoringBean() {
  }
  
  public Coloring getColoring() {
    return coloring;
  }

  public void setColoring(Coloring coloring) {
    this.coloring = coloring;
  }

}


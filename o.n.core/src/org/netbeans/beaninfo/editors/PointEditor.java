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

package com.netbeans.developer.editors;

import java.awt.Point;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** A property editor for Point class.
* @author   Petr Hamernik
* @version  0.10, 21 Jul, 1998
*/
public class PointEditor extends ArrayOfIntSupport {

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    PointEditor.class);

  public PointEditor() {
    super("java.awt.Point", 2);
  }

  /** Abstract method for translating the value from getValue() method to array of int. */
  int[] getValues() {
    Point p = (Point) getValue();
    return new int[] { p.x, p.y };
  }

  /** Abstract method for translating the array of int to value
  * which is set to method setValue(XXX)
  */
  void setValues(int[] val) {
    setValue(new Point(val[0], val[1]));
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    return new PointCustomEditor (this);
  }

  /** @return the format of value set in property editor. */
  String getHintFormat() {
    return bundle.getString ("CTL_HintFormatPE");
  }

  /** Provides name of XML tag to use for XML persistence of the property value */
  protected String getXMLValueTag () {
    return "Point";
  }

}

/*
 * Log
 *  4    Gandalf   1.3         7/19/99  Ian Formanek    XML Serialization
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/4/99   Jan Jancura     bundle moved
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */

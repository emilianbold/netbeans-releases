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

import java.awt.Dimension;
import java.util.ResourceBundle;

import com.netbeans.ide.util.NbBundle;

/** A property editor for Dimension class.
* @author   Petr Hamernik
* @version  0.10, 21 Jul, 1998
*/
public class DimensionEditor extends ArrayOfIntSupport {

  // the bundle to use
  static ResourceBundle bundle = NbBundle.getBundle (
    "com.netbeans.developer.impl.locales.ExplorerBundle");

  public DimensionEditor() {
    super("java.awt.Dimension", 2);
  }

  /** Abstract method for translating the value from getValue() method to array of int. */
  int[] getValues() {
    Dimension d = (Dimension) getValue();
    return new int[] { d.width, d.height };
  }

  /** Abstract method for translating the array of int to value
  * which is set to method setValue(XXX)
  */
  void setValues(int[] val) {
    setValue(new Dimension(val[0], val[1]));
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    return new DimensionCustomEditor (this);
  }


  /** @return the format of value set in property editor. */
  String getHintFormat() {
    return bundle.getString ("CTL_HintFormat");
  }
}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */

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

import org.openide.src.ClassElement;
import org.openide.src.SourceException;
import org.openide.nodes.Node;

/** This is base class for patterns object. These objects hold information
 * about progarammatic patterns i.e. Properties and Events in the source code
 * 
 * PENDING: Write some common abstract methods
 * 
 * @author Petr Hrebejk
 */
public abstract class  Pattern extends Object {

  PatternAnalyser patternAnalyser;
  
  public Pattern( PatternAnalyser patternAnalyser ) {
    this.patternAnalyser = patternAnalyser;
  }

  /** Every pattern knows it's own name */
  public abstract String getName();

  /** Every pattern should know if it is public. i.e. all methods
   * are public. Only these patterns are usable in BeanInfo
   */
  public abstract boolean isPublic();
  
  /** Default behavior - do nothing */
  public abstract void setName( String name ) throws SourceException;
  

  /** Gets the class which declares this property */
  public ClassElement getDeclaringClass() {
    return patternAnalyser.getClassElement();
  }

  /** Temporary implementation of getCookie */

  Node.Cookie getCookie( Class type ) {
    return null;
  }

  /** Default behavior for destroying pattern is to do nothing */
  public void destroy() throws SourceException {
  }

  /** Utility method capitalizes the first letter of string, used to
   * generate method names for patterns
   */
  static String capitalizeFirstLetter( String str ) {
    if ( str == null || str.length() <= 0 )
      return str;
    
    char chars[] = str.toCharArray();
	  chars[0] = Character.toUpperCase(chars[0]);
	  return new String(chars);
  }

}

/* 
 * Log
 *  2    Gandalf   1.1         7/20/99  Petr Hrebejk    
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
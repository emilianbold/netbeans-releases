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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.*;
import org.openide.src.nodes.ClassChildren;
import org.openide.src.nodes.ElementNodeFactory;

/** Implements children for basic source code patterns 
* 
* @author Petr Hrebejk, Jan Jancura
*/
public class PatternChildren extends ClassChildren {

  static {
    Integer i = new Integer (PatternFilter.METHOD | PatternFilter.PROPERTY | 
      PatternFilter.IDXPROPERTY | PatternFilter.EVENT_SET
    );
    propToFilter.put (ElementProperties.PROP_METHODS, i);
    propToFilter.put (ElementProperties.PROP_FIELDS, i);
  }
  
  /** Object for finding patterns in class */ 
  private PatternAnalyser       patternAnalyser;

  
  // Constructors -----------------------------------------------------------------------

  /** Create pattern children. The children are initilay unfiltered. 
   * @param elemrent the atteached class. For this class we recognize the patterns 
   */ 

  public PatternChildren (ClassElement classElement) {
    super (classElement);
    patternAnalyser = new PatternAnalyser( classElement );
    // PENDING : Solve this cyclic references
    patternAnalyser.setPatternChildren( this );
  }

  /** Create pattern children. The children are initilay unfiltered. 
   * @param elemrent the atteached class. For this class we recognize the patterns 
   */ 

  public PatternChildren (ElementNodeFactory factory, ClassElement classElement) {
    super (factory, classElement);
    patternAnalyser = new PatternAnalyser( classElement );
    // PENDING : Solve this cyclic references
    patternAnalyser.setPatternChildren( this );
  }

  
  // FilterCookie implementation --------------------------------------------------------

  /** Updates all the keys (elements) according to the current filter &
  * ordering.
  */
  protected void refreshAllKeys () {
    cpl = new Collection [getOrder ().length];
    refreshKeys (PatternFilter.ALL);
  }
  
  /** @return The class of currently associated filter or null
   * if no filter is associated with these children 
   */
  public Class getFilterClass () {
    return PatternFilter.class;
  }

  /** Gets the pattern analyser which manages the patterns */
  PatternAnalyser getPatternAnalyser( ) {
    return patternAnalyser;
  }

  
  // Children.keys implementation -------------------------------------------------------

  /** Creates node for given key. 
  */
  protected Node[] createNodes (Object key ) {
    if (key instanceof IdxPropertyPattern)
      return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, true) };
    if (key instanceof PropertyPattern) 
      return new Node[] { new PropertyPatternNode((PropertyPattern)key, true) };
    if (key instanceof EventSetPattern)
      return new Node[] { new EventSetPatternNode((EventSetPattern)key, true) };
    
    // Unknown pattern
    return super.createNodes (key);
  }

  
  // Utility methods --------------------------------------------------------------------

  protected Collection getKeysOfType (int elementType) {
    LinkedList keys = (LinkedList) super.getKeysOfType (elementType);
    if ((elementType & PatternFilter.PROPERTY) != 0) 
      keys.addAll( patternAnalyser.getPropertyPatterns() );
    if ((elementType & PatternFilter.IDXPROPERTY) != 0) 
      keys.addAll( patternAnalyser.getIdxPropertyPatterns() );
    if ((elementType & PatternFilter.EVENT_SET) != 0)
      keys.addAll( patternAnalyser.getEventSetPatterns() );

//    if ((filter == null) || filter.isSorted ()) 
//      Collections.sort (keys, comparator);
    return keys;
  }
}

/* 
 * Log
 *  2    Gandalf   1.1         7/1/99   Jan Jancura     Object Browser support
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
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
import org.openide.util.WeakListener;

/** Implements children for basic source code patterns 
* 
* @author Petr Hrebejk, Jan Jancura
*/
public class PatternChildren extends ClassChildren {
 
  private boolean wri = true;

  private MethodElementListener methodListener = new MethodElementListener();
  private FieldElementListener fieldListener = new FieldElementListener();

  private WeakListener.PropertyChange weakMethodListener = new WeakListener.PropertyChange( methodListener );
  private WeakListener.PropertyChange weakFieldListener = new WeakListener.PropertyChange( fieldListener );  

  static {
    Integer i = new Integer (PatternFilter.METHOD | PatternFilter.PROPERTY | 
      PatternFilter.IDXPROPERTY | PatternFilter.EVENT_SET
    );
    propToFilter.put (ElementProperties.PROP_METHODS, i);
    i = new Integer (PatternFilter.FIELD | PatternFilter.PROPERTY |
      PatternFilter.IDXPROPERTY | PatternFilter.EVENT_SET
    );
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
  }

  public PatternChildren (ClassElement classElement, boolean isWritable ) {
    this (classElement);
    wri = isWritable;
  }

  /** Create pattern children. The children are initilay unfiltered. 
   * @param elemrent the atteached class. For this class we recognize the patterns 
   */ 

  public PatternChildren (ElementNodeFactory factory, ClassElement classElement) {
    super (factory, classElement);
    patternAnalyser = new PatternAnalyser( classElement );
  }

  public PatternChildren (ElementNodeFactory factory, ClassElement classElement, boolean isWritable ) {
    this (factory, classElement);
    wri = isWritable;
  }
  
  /*
  PatternChildren cloneChildren() {
    return.clone();
    System.out.println ( "CLONING CHILDREN" );
    return new PatternChildren( patternAnalyser.getClassElement() );
  }
  */

  // FilterCookie implementation --------------------------------------------------------

  /** Updates all the keys (elements) according to the current filter &
  * ordering.
  */
  protected void refreshAllKeys () {
    cpl = new Collection [getOrder ().length];
    refreshKeys (PatternFilter.ALL);
  }
  
  /** Updates all the keys with given filter. Overriden to provide package access tothis method.
  */
  protected void refreshKeys (int filter) {
    
    // Method is added or removed ve have to re-analyze the pattern abd to
    // registrate Children as listener

    reassignMethodListener();
    reassignFieldListener();
    patternAnalyser.analyzeAll();
    super.refreshKeys (filter);
    //Thread.dumpStack();
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
      return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, wri) };
    if (key instanceof PropertyPattern) 
      return new Node[] { new PropertyPatternNode((PropertyPattern)key, wri) };
    if (key instanceof EventSetPattern)
      return new Node[] { new EventSetPatternNode((EventSetPattern)key, wri) };
    
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

  /** Method for removing method listener */
  private void reassignMethodListener() {
    MethodElement[] methods = element.getMethods();
    for ( int i = 0; i < methods.length ; i++ ) {
      methods[i].removePropertyChangeListener( weakMethodListener );
      methods[i].addPropertyChangeListener( weakMethodListener );
    }
  }


  /** Method for removing field listener */
  private void reassignFieldListener() {
    FieldElement[] fields = element.getFields();
    for ( int i = 0; i < fields.length ; i++ ) {
      fields[i].removePropertyChangeListener( weakFieldListener );
      fields[i].addPropertyChangeListener( weakFieldListener );
    }
  }


  // Inner classes ----------------------------------------------------------------------

  /** The listener of method changes temporary used in PatternAnalyser to
   * track changes in 
   */

  final class MethodElementListener implements PropertyChangeListener {
    public void propertyChange ( PropertyChangeEvent e ) {
      refreshKeys(PatternFilter.ALL);
      //patternAnalyser.analyzeAll();
    }
  }  
  
  /** The listener of method changes temporary used in PatternAnalyser to
   * track changes in 
   */

  final class FieldElementListener implements PropertyChangeListener {
    public void propertyChange ( PropertyChangeEvent e ) {
      refreshKeys(PatternFilter.ALL);
      //reassignFieldListener();
      //patternAnalyser.resolveFields();
    }
  }
}

/* 
 * Log
 *  9    Gandalf   1.8         7/28/99  Petr Hrebejk    Property Mode change fix
 *  8    Gandalf   1.7         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  7    Gandalf   1.6         7/21/99  Petr Hrebejk    Debug messages removed
 *  6    Gandalf   1.5         7/21/99  Petr Hrebejk    
 *  5    Gandalf   1.4         7/21/99  Petr Hamernik   some filter bugfix
 *  4    Gandalf   1.3         7/20/99  Petr Hrebejk    
 *  3    Gandalf   1.2         7/3/99   Ian Formanek    Overriden method 
 *       refreshKeys to provide access to classes in thes package and make it 
 *       compilable
 *  2    Gandalf   1.1         7/1/99   Jan Jancura     Object Browser support
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
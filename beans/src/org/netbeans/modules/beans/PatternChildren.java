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
import java.util.LinkedList;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.*;

/** Implements children for basic source code patterns 
 * 
 * @author Petr Hrebejk
 */
public class PatternChildren extends Children.Keys implements FilterCookie {

  /** The class element its subelements are represented */
  protected ClassElement        classElement;
  /** Filter for elements, or <CODE>null</CODE> to disable */
  protected PatternFilter       filter;
  /** Main storage of nodes */
  private Collection[]          cpl;
  /** Object for finding patterns in class */ 
  private PatternAnalyser       patternAnalyser;
  /** Private listener to class changes */
  private ClassElementListener  propL;

  // Constructors -----------------------------------------------------------------------

  /** Create pattern children. The children are initilay unfiltered. 
   * @param elemrent the atteached class. For this class we recognize the patterns 
   */ 

  public PatternChildren (ClassElement classElement) {
    super();
    this.classElement = classElement;
    this.filter = null;
    patternAnalyser = new PatternAnalyser( classElement );
    // PENDING : Solve this cyclic references
    patternAnalyser.setPatternChildren( this );
  }

  // FilterCookie implementation --------------------------------------------------------

  /** Called when the preparetion of nodes is needed
   */
  protected void addNotify() {
    refreshAllKeys ();
    if  ( propL == null ) {
      propL = new ClassElementListener();
      classElement.addPropertyChangeListener(propL);
    }
  }

  /** Called when all children are garbage collected */
  protected void removeNotify() {
    setKeys( java.util.Collections.EMPTY_SET );
  }
  
  /** @return The class of currently associated filter or null
   * if no filter is associated with these children 
   */
  public Class getFilterClass () {
    return PatternFilter.class;
  }

  /** @return The filter currently associated with these children
   */
  public Object getFilter () {
    return filter;
  }
  
  /** Sets new filter for these children
   * @param filter New Filter or null to disable filtering.
   */
  public void setFilter(final Object filter) {
    if (!(filter instanceof PatternFilter))
      throw new IllegalArgumentException();

    this.filter = (PatternFilter) filter;
    refreshAllKeys();
  }

  /** Gets the pattern analyser which manages the patterns */
  PatternAnalyser getPatternAnalyser( ) {
    return patternAnalyser;
  }

  // Children.keys implementation -------------------------------------------------------

  /** Creates node for given key. 
  */

  protected Node[] createNodes( final Object key ) {
    if (key instanceof IdxPropertyPattern)
      return new Node[] { new IdxPropertyPatternNode((IdxPropertyPattern)key, true) };
    if (key instanceof PropertyPattern) 
      return new Node[] { new PropertyPatternNode((PropertyPattern)key, true) };
    if (key instanceof EventSetPattern)
      return new Node[] { new EventSetPatternNode((EventSetPattern)key, true) };
    
    // Unknown pattern
    return new Node[0];
  }

  // Utility methods --------------------------------------------------------------------

  /** Updates all the keys (elements) according to the current filter &
   * ordering.
   */

  private void refreshAllKeys () {
    //System.out.println ("refresh all keys" );
    cpl = new Collection[getOrder().length];
    refreshKeys ( PatternFilter.ALL );
  }


  /** Updates all the keys with given filter 
   */

  /*private*/ void refreshKeys( int filter ) {


    int[] order = getOrder ();
    
    if ( cpl == null )
      cpl = new Collection[order.length];

    //System.out.println ( "refer kyes:" + filter );

    LinkedList keys = new LinkedList();

    for (int i = 0; i < order.length; i++ ) {
      if (((order[i] & filter) != 0) || (cpl[i] == null)) {
        keys.addAll (cpl[i] = getKeysOfType( order[i] ));
        }
      else
        keys.addAll (cpl[i]);
    }
    setKeys(keys);
  }

  private Collection getKeysOfType( int elementType ) {
    LinkedList keys = new LinkedList();

    if ((elementType & PatternFilter.PROPERTY) != 0) 
      keys.addAll( patternAnalyser.getPropertyPatterns() );
    if ((elementType & PatternFilter.IDXPROPERTY) != 0) 
      keys.addAll( patternAnalyser.getIdxPropertyPatterns() );
    if ((elementType & PatternFilter.EVENT_SET) != 0)
      keys.addAll( patternAnalyser.getEventSetPatterns() );
     
    return keys;
   
  }

  /** Return the order from current filter */
  private int[] getOrder() {
    return (filter == null || filter.getOrder() == null ) 
      ? PatternFilter.DEFAULT_ORDER : filter.getOrder();
  }

  // Inner classes ----------------------------------------------------------------------

  /** The listener for listennig to changes in element methods */
  private final class ClassElementListener implements PropertyChangeListener {
    public void propertyChange ( PropertyChangeEvent e ) {
      if ( e.getPropertyName().equals( ElementProperties.PROP_METHODS ) ||
           e.getPropertyName().equals( ElementProperties.PROP_FIELDS )) {
        int filter = patternAnalyser.classMethodsChanged();
        if ( filter != 0 )
          refreshKeys( filter );
      }
    }
  }

}

/* 
 * Log
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
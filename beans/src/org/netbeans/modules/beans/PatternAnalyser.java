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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.FieldElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;


/** Analyses the ClassElement trying to find source code patterns i.e.
 * properties or event sets;
 *
 * @author Petr Hrebejk
 */

public class PatternAnalyser extends Object implements Node.Cookie {

    private static final int PROPERTIES_RESERVE = 11;
   
    private HashMap propertyPatterns; 
    private HashMap idxPropertyPatterns; 
    private HashMap eventSetPatterns; 

    private ClassElement classElement;
    
    private PatternChildren pc = null;
    private MethodElementListener metL;
    private FieldElementListener fieldL;

    /** Nasty temporary procedure */

    void setPatternChildren( PatternChildren pc ) {
      this.pc = pc;
    }

    /** Creates new analyser for ClassElement 
     */
    public PatternAnalyser( ClassElement classElement ) {
      
      this.classElement = classElement;

      metL = new MethodElementListener();
      fieldL = new FieldElementListener();

      int methodCount = classElement.getMethods().length;
      propertyPatterns = new HashMap( methodCount / 2 + PROPERTIES_RESERVE );
      idxPropertyPatterns = new HashMap();          // Initial size 11
      eventSetPatterns = new HashMap();             // Initial size 11

      findPropertyPatterns();
      findEventSetPatterns();
    }

    public Collection getPropertyPatterns() {
      return propertyPatterns.values();
    }

    public Collection getIdxPropertyPatterns() {
      return idxPropertyPatterns.values();
    }

    public Collection getEventSetPatterns() {
      return eventSetPatterns.values();
    }

    /** Gets the classelemnt of this pattern analyser */
    public ClassElement getClassElement() {
      return classElement;
    }

    /** This method analyses the ClassElement for "property patterns". 
    * The method is analogous to JavaBean Introspector methods for classes
    * without a BeanInfo.
    */
    public void findPropertyPatterns() {

    //System.out.println ("Looking for property patterns");

      // First get all methods in classElement

      MethodElement[] methods = classElement.getMethods();

      // Analyze each method

      for ( int i = 0; i < methods.length ; i++ ) {

        MethodElement method = methods[i];

        // Start to listen to changes of this methods
        method.addPropertyChangeListener( metL );
    

        PropertyPattern pp = analyseMethod( method );

        if ( pp != null )
          addProperty( pp );
    
      }

      // Analyze fields

      resolveFields( );

    }


  private void resolveFields() {      
    // Analyze fields
    FieldElement fields[] = classElement.getFields();

    for ( int i = 0; i < fields.length; i++ ) {
      FieldElement field=fields[i];
      field.addPropertyChangeListener( fieldL );


      if ( ( field.getModifiers() & Modifier.STATIC ) != 0 )
        continue;

      PropertyPattern pp = (PropertyPattern)propertyPatterns.get( field.getName().getName() );
      
      if ( pp == null )
        pp = (PropertyPattern)idxPropertyPatterns.get( field.getName().getName() );

      if ( pp == null )
        continue;
  
      Type ppType = pp.getType();

      if ( ppType != null && pp.getType().compareTo( field.getType(), false ) )
        pp.setEstimatedField( field );

    }
  }    


  /** Analyses one method for property charcteristics */

  PropertyPattern analyseMethod( MethodElement method ) {
    // Skip static methods as Introspector does.
    int modifiers = method.getModifiers();
    if ( Modifier.isStatic( modifiers ) )
      return null;

    String name = method.getName().getName();
    MethodParameter[] params = method.getParameters();
    int paramCount = params == null ? 0 : params.length;
    Type returnType = method.getReturn();

    PropertyPattern pp = null;

    try {      
      if ( paramCount == 0 ) {
        if (name.startsWith( "get" )) {
          // SimpleGetter
          pp = new PropertyPattern( this, method, null);
        }
        else if ( returnType.compareTo( Type.BOOLEAN, false ) && name.startsWith("is")) {
          // Boolean getter
          pp = new PropertyPattern( this, method, null );
        }
      }
      else if ( paramCount == 1 ) {
        if ( params[0].getType().compareTo( Type.INT, false ) && name.startsWith( "get" )) {
          pp = new IdxPropertyPattern( this, null, null, method, null );
        }
        else if ( returnType.compareTo( Type.VOID, false ) && name.startsWith( "set" )) {
          pp = new PropertyPattern( this, null, method );
          // PENDING vetoable => constrained
        }
      }
      else if ( paramCount == 2 ) {
        if ( params[0].getType().compareTo( Type.INT, false ) && name.startsWith( "set" )) {
          pp = new IdxPropertyPattern( this, null, null, null, method ); 
          // PENDING vetoable => constrained
        }
      }
    }
    catch (IntrospectionException ex) {
      // PropertyPattern constructor found some differencies from design patterns.
      pp = null;
    }

   return pp;
  }


  /** Method analyses cass methods for EventSetPatterns 
   */


  void findEventSetPatterns() {
    //System.out.println ("Looking for EventSet patterns");

    // First get all methods in classElement

    MethodElement[] methods = classElement.getMethods();

    // Find all suitable "add" and "remove" methods
    Hashtable adds = new Hashtable();
    Hashtable removes = new Hashtable();
    for ( int i = 0; i < methods.length ; i++ ) {

      MethodElement method = methods[i];

      // Start to listen to changes of this methods
      //method.addPropertyChangeListener( metL );

      // Skip static methods
      int modifiers = method.getModifiers();
      if ( Modifier.isStatic( modifiers ) )
        continue;

      String name = method.getName().getName();
      MethodParameter params[] = method.getParameters();
      Type returnType = method.getReturn();

      if ( name.startsWith( "add" ) && params.length == 1 && returnType == Type.VOID ) {
                                                    // !! MAY BE PROBLEM
        String compound = name.substring(3) + ":" + params[0].getType(); 
        //System.out.println ( method.getName() );
        adds.put( compound, method );
      }
      else if ( name.startsWith( "remove" ) && params.length == 1 && returnType == Type.VOID ) {
                                                    // !! MAY BE PROBLEM 
        String compound = name.substring(6) + ":" + params[0].getType(); 
        //System.out.println ( method.getName() );
        removes.put( compound, method );
      }
    }
    // Now look for matching addFooListener+removeFooListener pairs.
    Enumeration keys = adds.keys();
    
    while (keys.hasMoreElements()) {
      String compound = (String) keys.nextElement();
      // Skip any "add" which dosn't have a matching remove
      if (removes.get (compound) == null ) {
        continue;
      }
      // Method name has to end in Listener
      if (compound.indexOf( "Listener:" ) <= 0 ) {
        continue;
      }

      String listenerName = compound.substring( 0, compound.indexOf( ':' ) );
      String eventName = Introspector.decapitalize( listenerName.substring( 0, listenerName.length() - 8 ));
      MethodElement addMethod = (MethodElement)adds.get(compound);
      MethodElement removeMethod = (MethodElement)removes.get(compound);
      Type argType = addMethod.getParameters()[0].getType();

      // Check if the argument is a subtype of EventListener
      try {
        //if (!Introspector.isSubclass( argType.toClass(), java.util.EventListener.class ) ) {
        if (!java.util.EventListener.class.isAssignableFrom( argType.toClass() ) ) {
          continue;
        }
      }
      catch ( java.lang.ClassNotFoundException ex ) {
        continue;
      }

      // PENDING: add methods of listener to Pattern

      
      EventSetPattern esp; 
      
      try {
        esp = new EventSetPattern( this, addMethod, removeMethod );
      }
      catch ( IntrospectionException ex ) {
        esp = null;
      }

      if (esp != null)
        addEventSet( esp );
    }

  }

  /** This method is called when any method is added or removed in ClassElement 
   * // PENDING : NASTY, UGLY & INEFFECTIVE IMPLEMENTATION 
   */
  
  void methodChanged( PropertyChangeEvent e) {
    classMethodsChanged();  
    pc.refreshKeys( PatternFilter.ALL );
  }

  /** This method is called when any method is added or removed in ClassElement 
   * // PENDING : NASTY, UGLY & INEFFECTIVE IMPLEMENTATION 
   */

  int classMethodsChanged() {
    removeMethodListeners();
    removeFieldListeners();

    int methodCount = classElement.getMethods().length;
    propertyPatterns = new HashMap( methodCount / 2 + PROPERTIES_RESERVE );
    idxPropertyPatterns = new HashMap();        // Initial size 11
    eventSetPatterns = new HashMap();           // Initial size 11

    findPropertyPatterns();
    findEventSetPatterns();
    return PatternFilter.ALL;
  }

  // Utility methods --------------------------------------------------------------------

  /** Temporay method to remove all method listeners */
  private void removeMethodListeners() {
    MethodElement[] methods = classElement.getMethods();

    for ( int i = 0; i < methods.length ; i++ ) {
      methods[i].removePropertyChangeListener( metL );
    }
  }


  /** Temporay method to remove all method listeners */
  private void removeFieldListeners() {
    FieldElement[] fields = classElement.getFields();

    for ( int i = 0; i < fields.length ; i++ ) {
      fields[i].removePropertyChangeListener( fieldL );
    }
  }

  /** Adds the new property. Or generates composite property if property
   *  of that name already exists. It puts the property in the right HashMep
   * according to type of property idx || not idx
   */
  
  private void addProperty( PropertyPattern pp ) {
    boolean isIndexed = pp instanceof IdxPropertyPattern;
    HashMap hm = isIndexed ? idxPropertyPatterns : propertyPatterns;
    String name = pp.getName();

    PropertyPattern old = (PropertyPattern)propertyPatterns.get(name);
    if ( old == null )
      old = (PropertyPattern)idxPropertyPatterns.get(name);

    if (old == null) {  // There is no other property of that name
      hm.put(name, pp);
      return;
    }

    // If the property type has changed, use new property pattern
    Type opt = old.getType();
    Type npt = pp.getType();
    if (  opt != null && npt != null && !opt.compareTo(npt, false) ) {
      hm.put( name, pp );
      return;
    }

    PropertyPattern composite;
    boolean isOldIndexed = old instanceof IdxPropertyPattern;

    if  (isIndexed || isOldIndexed ) {
      if ( isIndexed && !isOldIndexed ) {
        propertyPatterns.remove( old.getName() ); // Remove old from not indexed
      }
      else if ( !isIndexed && isOldIndexed ) {
        idxPropertyPatterns.remove( old.getName() ); // Remove old from indexed
      }
      composite = new IdxPropertyPattern( old, pp );
      idxPropertyPatterns.put( name, composite );
    }
    else {
      composite = new PropertyPattern( old, pp );
      propertyPatterns.put( name, composite );
    }
    
    // PENDING : Resolve types of getters and setters to pair correctly
    // methods with equalNames.
    /*
    MethodElement getter = pp.getGetterMethod() == null ?
      old.getGetterMethod() : pp.getGetterMethod();
    MethodElement setter = pp.getSetterMethod() == null ?
      old.getSetterMethod() : pp.getSetterMethod();

    PropertyPattern composite = isIndexed ?
      new IdxPropertyPattern ( getter, setter ) :
      new PropertyPattern( getter, setter );
    hm.put( pp.getName(), composite );
    */

  }


  /** adds an eventSetPattern */

  void addEventSet( EventSetPattern esp ) {
    String key = esp.getName() + esp.getType();
    EventSetPattern old = (EventSetPattern)eventSetPatterns.get( key );
    if ( old == null ) {
      eventSetPatterns.put( key, esp);
      return;
    }

    EventSetPattern composite = new EventSetPattern( old, esp );
    eventSetPatterns.put( key, composite );
  }


  // Inner classes ----------------------------------------------------------------------

  /** The listener of method changes temporary used in PatternAnalyser to
   * track changes in 
   */

  final class MethodElementListener implements PropertyChangeListener {
    public void propertyChange ( PropertyChangeEvent e ) {
      methodChanged(e);
    }
  }  
  
  /** The listener of method changes temporary used in PatternAnalyser to
   * track changes in 
   */

  final class FieldElementListener implements PropertyChangeListener {
    public void propertyChange ( PropertyChangeEvent e ) {
      removeFieldListeners();
      resolveFields();
    }

  }
}

/* 
 * Log
 *  2    Gandalf   1.1         7/9/99   Petr Hrebejk    Factory chaining fix
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
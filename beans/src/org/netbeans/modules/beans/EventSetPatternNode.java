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

import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.src.nodes.ClassChildren;

/** Node representing a event set pattern.
* @see Event Set Pattern
* @author Petr Hrebejk
*/
public class EventSetPatternNode extends PatternNode implements IconBases {

  /** Create a new field node.
  * @param element field element to represent
  * @param writeable <code>true</code> to be writable
  */
  public EventSetPatternNode( EventSetPattern pattern, boolean writeable) {
    //super(pattern, Children.LEAF, writeable);
    super(pattern, new PatternChildren( org.openide.src.nodes.DefaultFactory.READ_ONLY, pattern.getTypeElement(), false ), writeable);
    superSetName( pattern.getName() );
    }
  
  public HelpCtx getHelpCtx () {
    return new HelpCtx (EventSetPatternNode.class);
  }

  /** Sets the name of Pattern, to new value */
  protected void setPatternName( String name ) throws SourceException {
    if ( testNameValidity(name) ) {
      ((EventSetPattern)pattern).setName(name);
      superSetName( name );
      }
  }

  /** Sets the name of the node */
  public void setName( String name ) {
    try {
      setPatternName(name);
    }
    catch (SourceException e) {
    }
  }


  /** Tests if the given string is valid name for associated pattern and if not, notifies
  * the user.
  * @return true if it is ok.
  */
  boolean testNameValidity( String name ) {
    
    if (! Utilities.isJavaIdentifier( name ) ) {
      TopManager.getDefault().notify(
        new NotifyDescriptor.Message(bundle.getString("MSG_Not_Valid_Identifier"),
                                     NotifyDescriptor.ERROR_MESSAGE) );
      return false;
    }

    if (name.indexOf( "Listener" ) <= 0 ) {
      String msg = MessageFormat.format( PatternNode.bundle.getString("FMT_InvalidEventSourceName"),
                                           new Object[] { name } );
      TopManager.getDefault().notify( new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE) );
      return false;
    }

    return true;
  }


  /** Resolve the current icon base.
  * @return icon base string.
  */
  protected String resolveIconBase() {
    if (((EventSetPattern)pattern).isUnicast() )
      return EVENTSET_UNICAST;
    else
      return EVENTSET_MULTICAST;
  }

  /** Gets the short description of this node.
  * @return A localized short description associated with this node.
  */
  public String getShortDescription() {
    return (((EventSetPattern)pattern).isUnicast () ? 
      bundle.getString( "HINT_UnicastEventSet" ) : 
      bundle.getString( "HINT_MulticastEventSet" ) ) 
      + " : " + getName();
  }

  /** This method resolve the appropriate hint format for the type
  * of the element. It defines the short description.
  */
  protected ElementFormat getHintElementFormat() {
    return sourceOptions.getFieldElementLongFormat();
  }

  /** Creates property set for this node */
  protected Sheet createSheet () {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

    ps.put(createNameProperty( writeable ));
    ps.put(createTypeProperty( writeable ));
    ps.put(createIsUnicastProperty( writeable ));
    ps.put(createAddListenerProperty( false ));
    ps.put(createRemoveListenerProperty( false ));
    
    return sheet;
  }

  /** Removes the element from the class and calls superclass.
  *
  * @exception IOException if SourceException is thrown
  *            from the underlayed Element.
  */
  /*
  public void destroy() throws IOException {
    /*
    try {
      FieldElement el = (FieldElement) element;
      el.getDeclaringClass().removeField(el);
    }
    catch (SourceException e) {
      throw new IOException(e.getMessage());
    }
    super.destroy();
  }
  */
  /** Overrides the default implementation of clone node 
   */

  public Node cloneNode() {
    return new EventSetPatternNode((EventSetPattern)pattern, writeable );
  }

  
  /** Create a property for the field type.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createTypeProperty(boolean canW) {
    return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        return ((EventSetPattern)pattern).getType();
      }
      
      /** Sets the value */
      public void setValue(Object val) throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
        super.setValue(val);
        if (!(val instanceof Type))
          throw new IllegalArgumentException();
          
        try {
          pattern.patternAnalyser.setIgnore( true );
          ((EventSetPattern)pattern).setType((Type)val);
          pattern.patternAnalyser.setIgnore( false );
        }
        catch (SourceException e) {
          throw new InvocationTargetException(e);
        }
      }
    };
  }
  

  /** Create a property for the field type.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
 
  protected Node.Property createIsUnicastProperty(boolean canW) {
    return new PatternPropertySupport(PROP_ISUNICAST, boolean.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        return new Boolean (((EventSetPattern)pattern).isUnicast());
      }
      
      /** Sets the value */
      public void setValue(Object val) throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
        super.setValue(val);
        if (!(val instanceof Boolean))
          throw new IllegalArgumentException();
          
        try {
          pattern.patternAnalyser.setIgnore( true );
          ((EventSetPattern)pattern).setIsUnicast(((Boolean)val).booleanValue());
          pattern.patternAnalyser.setIgnore( false );
          setIconBase( resolveIconBase() );
        }
        catch (SourceException e) {
          throw new InvocationTargetException(e);
        }
      }
      
    };
  }

  /** Create a property for the addListener method.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createAddListenerProperty(boolean canW) {
    return new PatternPropertySupport(PROP_ADDLISTENER, String.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        ElementFormat fmt = new ElementFormat ("{n} ({p})");
        MethodElement method = ((EventSetPattern)pattern).getAddListenerMethod();
        if ( method == null )
          return bundle.getString("LAB_NoMethod");
        else
          return (fmt.format (method));
      }
    };
  }

  /** Create a property for the removeListener method.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createRemoveListenerProperty(boolean canW) {
    return new PatternPropertySupport(PROP_REMOVELISTENER, String.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        ElementFormat fmt = new ElementFormat ("{n} ({p})");
        MethodElement method = ((EventSetPattern)pattern).getRemoveListenerMethod();
        if ( method == null )
          return bundle.getString("LAB_NoMethod");
        else
          return (fmt.format (method));
      }
    };
  }
}

/*
* Log
*  4    Gandalf   1.3         8/2/99   Petr Hrebejk    EventSetNode chilfren & 
*       EventSets types with src. code fixed
*  3    Gandalf   1.2         7/26/99  Petr Hrebejk    Better implementation of 
*       patterns resolving
*  2    Gandalf   1.1         7/8/99   Jesse Glick     Context help.
*  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
* $
*/

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
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing a field (variable).
* @see FieldElement
* @author Petr Hrebejk
*/
public class PropertyPatternNode extends PatternNode implements IconBases {
  
  /** Create a new field node.
  * @param element field element to represent
  * @param writeable <code>true</code> to be writable
  */
  public PropertyPatternNode( PropertyPattern pattern, boolean writeable) {
    super(pattern, Children.LEAF, writeable);
    superSetName( pattern.getName() );
    
    //BHM
    //setElementFormat (sourceOptions.getFieldElementFormat());
  }
  
  /* Resolve the current icon base.
  * @return icon base string.
  */
  protected String resolveIconBase() {
    switch (((PropertyPattern)pattern).getMode()) {
    case PropertyPattern.READ_WRITE:
      return PROPERTY_RW;
    case PropertyPattern.READ_ONLY:
      return PROPERTY_RO;
    case PropertyPattern.WRITE_ONLY:
      return PROPERTY_WO;
    default:
      return null;
    }
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (PropertyPatternNode.class);
  }

  /** Gets the localized string name of property pattern type i.e.
   * "Indexed Property", "Property".
   */
  String getTypeForHint() {
    return bundle.getString( "HINT_Property" );
  }
    

  /* Gets the short description of this node.
  * @return A localized short description associated with this node.
  */
  public String getShortDescription() {
    String mode;

    switch( ((PropertyPattern)pattern).getMode() ) {
      case PropertyPattern.READ_WRITE:
      mode = bundle.getString("HINT_ReadWriteProperty") ;
      break;
    case PropertyPattern.READ_ONLY:
      mode = bundle.getString("HINT_ReadOnlyProperty"); 
      break;
    case PropertyPattern.WRITE_ONLY:
      mode = bundle.getString("HINT_WriteOnlyProperty"); 
      break;
    default:
      mode = "";
      break;
    }
    return mode + " " + getTypeForHint() + " : " + getName();
  }

  /** Creates property set for this node 
   */
  protected Sheet createSheet () {
    Sheet sheet = Sheet.createDefault();
    Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

    ps.put(createNameProperty( writeable ));
    ps.put(createTypeProperty( writeable ));
    ps.put(createModeProperty( writeable ));
    ps.put(createGetterProperty( false ));
    ps.put(createSetterProperty( false ));
    ps.put(createFieldProperty(false));

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
    *
    System.out.println ("Pattern should be removed");
    super.destroy();
  }
    */
  /** Overrides the default implementation of clone node 
   */

  public Node cloneNode() {
    return new PropertyPatternNode((PropertyPattern)pattern, writeable);
  }

  /** Sets the name of pattern 
   */
  protected void setPatternName( String name ) throws SourceException {
    if (testNameValidity(name)) {
      ((PropertyPattern)pattern).setName( name );
      superSetName( name );
    } 
  }

  /** Sets the name of the node */
  public void setName( String name ) {
    
    try {
      pattern.patternAnalyser.setIgnore( true );
      setPatternName( name );
      pattern.patternAnalyser.setIgnore( false );
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

    return true;
  }

  /** Create a property for the field type.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createTypeProperty(boolean canW) {
    return new PatternPropertySupport(PROP_TYPE, Type.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        return ((PropertyPattern)pattern).getType();
      }
      
      /** Sets the value */
      public void setValue(Object val) throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
        super.setValue(val);
        if (!(val instanceof Type))
          throw new IllegalArgumentException();
          
       
        try {
          pattern.patternAnalyser.setIgnore( true );
          ((PropertyPattern)pattern).setType((Type)val);
          pattern.patternAnalyser.setIgnore( false );
        }
        catch (SourceException e) {
          throw new InvocationTargetException(e);
        }
        
      }
    };
  }
  
  /** Create a property for the mode of property pattern.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  void fire () { 
    firePropertyChange( null, null, null );  
  }
  
  protected Node.Property createModeProperty(boolean canW) {
    return new PatternPropertySupport(PROP_MODE, int.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        return new Integer( ((PropertyPattern)pattern).getMode() );
      }
      
      /** Sets the value */
      public void setValue(Object val) throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
        super.setValue(val);
        if (!(val instanceof Integer))
          throw new IllegalArgumentException();
          
        try {
          pattern.patternAnalyser.setIgnore( true );
          ((PropertyPattern)pattern).setMode(((Integer)val).intValue());
          pattern.patternAnalyser.setIgnore( false );
          setIconBase( resolveIconBase() );
        }
        catch (SourceException e) {
          throw new InvocationTargetException(e);
        }
        
      }
      
      /** Define property editor for this property. */
    
      public PropertyEditor getPropertyEditor () {
        return new com.netbeans.developer.modules.beans.ModePropertyEditor();
      }
      
    };
  }

  /** Create a property for the getter method.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createGetterProperty(boolean canW) {
    return new PatternPropertySupport(PROP_GETTER, String.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        ElementFormat fmt = new ElementFormat ("{n} ({p})");
        MethodElement method = ((PropertyPattern)pattern).getGetterMethod();
        if ( method == null )
          return bundle.getString("LAB_NoMethod");
        else
          return (fmt.format (method));
      }
    };
  }

  /** Create a property for the getter method.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createSetterProperty(boolean canW) {
    return new PatternPropertySupport(PROP_SETTER, String.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        ElementFormat fmt = new ElementFormat ("{n} ({p})");
        MethodElement method = ((PropertyPattern)pattern).getSetterMethod();
        if ( method == null )
          return bundle.getString("LAB_NoMethod");
        else
          return (fmt.format (method));
      }
    };
  }

  /** Create a property for the estimated filed.
   * @param canW <code>false</code> to force property to be read-only
   * @return the property
   */
 
  protected Node.Property createFieldProperty(boolean canW) {
    return new PatternPropertySupport(PROP_ESTIMATEDFIELD, String.class, canW) {

      /** Gets the value */
     
      public Object getValue () {
        ElementFormat fmt = new ElementFormat ("{t} {n}");
        FieldElement field = ((PropertyPattern)pattern).getEstimatedField();
        if ( field == null )
          return bundle.getString("LAB_NoField");
        else
          return (fmt.format (field));
      }
    };
  }
}

/*
* Log
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/24/99  Petr Hrebejk    Rename of properties from
*       popupmenu fixed
*  4    Gandalf   1.3         7/28/99  Petr Hrebejk    Property Mode change fix
*  3    Gandalf   1.2         7/26/99  Petr Hrebejk    Better implementation of 
*       patterns resolving
*  2    Gandalf   1.1         7/8/99   Jesse Glick     Context help.
*  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
* $
*/

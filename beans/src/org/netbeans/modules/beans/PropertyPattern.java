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

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.src.SourceException; 
import org.openide.src.Identifier; 
import org.openide.src.ElementFormat; 
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

//import com.netbeans.developer.modules.loaders.java.support.AutoCommenter;

/** PropertyPattern: This class holds the information about used property pattern 
 * in code.
 * @author Petr Hrebejk
 *
 * 
 *  PENDING: Add Pattern class hierarchy (abstract classes || interfaces )
 */
public class PropertyPattern extends Pattern {


  private static final ResourceBundle bundle = NbBundle.getBundle( PropertyPattern.class );

  public static final int READ_WRITE = 1;
  public static final int READ_ONLY = 2;
  public static final int WRITE_ONLY = 4;

  protected MethodElement getterMethod = null;
  protected MethodElement setterMethod = null;
  protected FieldElement  estimatedField = null;

  protected Type type;

  /** holds the decapitalized name */
  protected String name;

  /** Creates new PropertyPattern one of the methods may be null */
  public PropertyPattern( PatternAnalyser patternAnalyser, 
                          MethodElement getterMethod, MethodElement setterMethod ) 
    throws IntrospectionException {

    super( patternAnalyser );

    this.getterMethod = getterMethod;
    this.setterMethod = setterMethod;
  
    type = findPropertyType();
    name = findPropertyName();
  }

  PropertyPattern( PatternAnalyser patternAnalyser ) {
    super( patternAnalyser );
  }

  /*
  public boolean equals( Object o ) {
    if ( ! (o instanceof PropertyPattern ) )
      return false;
    
    return ((PropertyPattern)o).getterMethod == getterMethod &&
           ((PropertyPattern)o).setterMethod == setterMethod ;
  }

  public int hashCode( ) {
    return ( getterMethod != null ? getterMethod.hashCode() : 0 ) +
           ( setterMethod != null ? setterMethod.hashCode() : 0 );
  }
  */

  static PropertyPattern create( PatternAnalyser patternAnalyser, 
                                 String name, String type ) throws SourceException {

    PropertyPattern pp = new PropertyPattern( patternAnalyser );
    
    pp.name = name;
    pp.type = Type.parse( type );

    pp.generateGetterMethod();
    pp.generateSetterMethod();

    return pp;
  }

  /** Creates new property pattern with extended options */

  static PropertyPattern create( PatternAnalyser patternAnalyser, 
                                 String name, String type,
                                 int mode, boolean bound, boolean constrained,
                                 boolean withField, boolean withReturn,
                                 boolean withSet, boolean withSupport ) throws SourceException {

    PropertyPattern pp = new PropertyPattern( patternAnalyser );
    
    pp.name = name;
    pp.type = Type.parse( type );

    /** Generate field */ 
    if ( withField || withSupport ) {
      pp.generateField( true );
    }

    /** Ensure property change support field and methods exist */
    String supportName = null;
    String vetoSupportName = null;

    if ( withSupport ) {
      if ( bound )
        supportName = BeanPatternGenerator.supportField( pp.getDeclaringClass() );
      if ( constrained ) 
        vetoSupportName = BeanPatternGenerator.vetoSupportField( pp.getDeclaringClass() );

      if ( bound )
        BeanPatternGenerator.supportListenerMethods( pp.getDeclaringClass(), supportName );
      if ( constrained )
        BeanPatternGenerator.vetoSupportListenerMethods( pp.getDeclaringClass(), vetoSupportName );
    }

    if ( mode == READ_WRITE || mode == READ_ONLY )
      pp.generateGetterMethod( BeanPatternGenerator.propertyGetterBody( name, withReturn ), true );

    if ( mode == READ_WRITE || mode == WRITE_ONLY )
      pp.generateSetterMethod( BeanPatternGenerator.propertySetterBody( name, pp.getType(),
        bound, constrained, withSet, withSupport, supportName, vetoSupportName ), constrained, true );

    return pp;
  }

  /** Gets the name of PropertyPattern */
  public String getName() {
    return name;
  }


  /** Tests if the pattern is public i.e. all needed parts are public */
  public boolean isPublic() {
    return  (getterMethod == null || getterMethod.getModifiers() == Modifier.PUBLIC) &&
            (setterMethod == null || setterMethod.getModifiers() == Modifier.PUBLIC);
  }
  
  /** Sets the name of PropertyPattern */
  public void setName( String name ) throws SourceException {
 
    if ( !Utilities.isJavaIdentifier( name )  )
      throw new SourceException( "Invalid event source name" );

    name = capitalizeFirstLetter( name );

    if ( getterMethod != null ) {
      Identifier getterMethodID = Identifier.create(( getterMethod.getName().getName().startsWith("get") ?
                                                   "get" : "is" ) + name );
      getterMethod.setName( getterMethodID );
      }
    if ( setterMethod != null ) {
      Identifier setterMethodID = Identifier.create( "set" + name );

      setterMethod.setName( setterMethodID );
      }

    // Ask if to set the estimated field
    if ( estimatedField != null ) {
       ElementFormat fmt = new ElementFormat ("{m} {t} {n}");
       String mssg = MessageFormat.format( PatternNode.bundle.getString( "FMT_ChangeFieldName" ), 
                                            new Object[] { fmt.format (estimatedField) } );
       NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
       if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
        estimatedField.setName( Identifier.create( Introspector.decapitalize( name ) ) );
       }
    }
  }

  /** Returns the mode of the property READ_WRITE, READ_ONLY or WRITE_ONLY */
  public int getMode() {
    if ( setterMethod != null && getterMethod != null )
      return READ_WRITE;
    else if ( getterMethod != null && setterMethod == null )
      return READ_ONLY;
    else if ( setterMethod != null && getterMethod == null )
      return WRITE_ONLY;
    else
      return 0;
  }

  /** Sets the property to be writable */
  public void setMode( int mode ) throws SourceException {
    if ( getMode() == mode )
      return;

    switch ( mode ) {
    case READ_WRITE:
      if ( getterMethod == null )
        generateGetterMethod();
      if ( setterMethod == null )
        generateSetterMethod();
      break;
    case READ_ONLY:
      if ( getterMethod == null )
        generateGetterMethod();
      if ( setterMethod != null )
        deleteSetterMethod();
      break;
    case WRITE_ONLY:
      if ( setterMethod == null )
        generateSetterMethod();
      if ( getterMethod != null )
        deleteGetterMethod();
      break;
    }
    
  }


  /** Returns the getter method */
  public MethodElement getGetterMethod() {
    return getterMethod;
  }

  /** Returns the setter method */
  public MethodElement getSetterMethod() {
    return setterMethod;
  }

  /** Gets the type of property */
  public Type getType() {
    return type;
  }

  /** Sets the type of propertyPattern */
  public void setType(Type type) throws SourceException {

    if ( this.type.compareTo( type, true ) )
      return;
    
    if (getterMethod != null ) {
      if ( this.type.compareTo( Type.BOOLEAN, false ) ) {
        getterMethod.setName( Identifier.create( "get" + capitalizeFirstLetter( getName() ) ) );
      }
      else if ( type.compareTo( Type.BOOLEAN, false ) ) {
       String mssg = MessageFormat.format( PatternNode.bundle.getString( "FMT_ChangeToIs" ), 
                                            new Object[] { capitalizeFirstLetter( getName() ) } );
       NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
       TopManager.getDefault().notify( nd );
       if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
          getterMethod.setName( Identifier.create( "is" + capitalizeFirstLetter( getName() ) ) );
          }
      }
      getterMethod.setReturn( type );      
    }

    if (setterMethod != null ) { 
      MethodParameter[] params = setterMethod.getParameters();
      if ( params.length > 0 ) {
        params[0].setType( type );
        setterMethod.setParameters( params );
      }
    }
   
    // Ask if to change estimated field Type
    if ( estimatedField != null ) {
      ElementFormat fmt = new ElementFormat ("{m} {t} {n}");
      String mssg = MessageFormat.format( PatternNode.bundle.getString( "FMT_ChangeFieldType" ), 
                                           new Object[] { fmt.format (estimatedField) } );
      NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
      if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
        estimatedField.setType(type);
      }
    }
  }
  /** Gets the cookie of the first available method */

  public Node.Cookie getCookie( Class cookieType ) {
    if ( getterMethod != null )
      return getterMethod.getCookie( cookieType );
    
    if ( setterMethod != null )
      return setterMethod.getCookie( cookieType );
      
    return null;
  }

  /** Gets the estimated field */

  public FieldElement getEstimatedField( ) {
    return estimatedField;
  }

  /** Sets the estimated field */

  void setEstimatedField( FieldElement field ) {
    estimatedField = field;
  }

  /** Destroys the pattern and the associated methods in source */

  public void destroy() throws SourceException {
    //System.out.println ( " Destroing property pattern" );
    
    if ( estimatedField != null ) {
      ElementFormat fmt = new ElementFormat ("{m} {t} {n}");
      String mssg = MessageFormat.format( PatternNode.bundle.getString( "FMT_DeleteField" ), 
                                           new Object[] { fmt.format (estimatedField) } );
      NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
      if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
        deleteEstimatedField();
      }
    }        

    deleteGetterMethod();
    deleteSetterMethod();
  }

  // Utility methods --------------------------------------------------------------------

  /** Package private constructor. Merges two property descriptors. Where they
   * conflict, gives the second argument (y) priority over the first argumnet (x).
   *
   * @param x The first (lower priority) PropertyPattern.
   * @param y The second (higher priority) PropertyPattern.
   */

  PropertyPattern( PropertyPattern x, PropertyPattern y ) {
    super( y.patternAnalyser );

    // Figure out the merged getterMethod
    MethodElement xr = x.getterMethod;
    MethodElement yr = y.getterMethod;
    getterMethod = xr;

    // Normaly give priority to y's getterMethod
    if ( yr != null ) {
      getterMethod = yr;
    }
    // However, if both x and y reference read method in the same class,
    // give priority to a boolean "is" method over boolean "get" method.
    if ( xr != null && yr != null &&
      xr.getDeclaringClass() == yr.getDeclaringClass() &&
      xr.getReturn().compareTo( Type.BOOLEAN, false ) && 
      yr.getReturn().compareTo( Type.BOOLEAN, false ) &&
      xr.getName().getName().indexOf("is") == 0 &&
      yr.getName().getName().indexOf("get") == 0 ) {
      getterMethod = xr;
    }

    setterMethod = x.getSetterMethod();
    if ( y.getSetterMethod() != null ) {
      setterMethod = y.getSetterMethod();
    }

    // PENDING bound and constrained 
    /*
    bound = x.bound | y.bound;
    constrained = x.constrained | y.constrained
    */

    try {
      type = findPropertyType();
    }
    catch ( IntrospectionException ex ) {
      System.out.println (x.getName() + ":" +  y.getName());
      System.out.println (x.getType() + ":" + y.getType() );
      throw new InternalError( "Mixing invalid PropertyPattrens" + ex );
    }

    name = findPropertyName();
  }

  /** Finds the Type of property.
   * @throws IntrospectionException if the property doesnt folow the design patterns
   */

  private Type findPropertyType() throws IntrospectionException {
    Type resolvedType = null;

    if ( getterMethod != null ) {
      if ( getterMethod.getParameters().length != 0 ) {
        throw new IntrospectionException( "bad read method arg count" );
      }
      resolvedType = getterMethod.getReturn();
      if ( resolvedType.compareTo( Type.VOID, false ) ) {
        throw new IntrospectionException( "read method " + getterMethod.getName().getName() + 
          " returns void" );
      }
    }
    if ( setterMethod != null ) {
      MethodParameter params[] = setterMethod.getParameters();
      if ( params.length != 1 ) {
        throw new IntrospectionException( "bad write method arg count" );
      }
      if ( resolvedType != null && !resolvedType.compareTo( params[0].getType(), false ) ) {
        throw new IntrospectionException( "type mismatch between read and write methods" );
      }
      resolvedType = params[0].getType();
    }
    return resolvedType;
  }    

  /** Decides about the name of the property from names of the methods */
  
  String findPropertyName() {
    
    String methodName = null;
    
    if ( getterMethod != null )
      methodName = getterMethod.getName().getName() ;
    else if ( setterMethod != null )
      methodName = setterMethod.getName().getName() ;
    else {
      return null;
    }

    return  methodName.startsWith( "is" ) ?
      Introspector.decapitalize( methodName.substring(2) ) :
      Introspector.decapitalize( methodName.substring(3) );
  }
  
  // Methods for generating / dleting methods -------------------------------------------

  void generateGetterMethod( ) throws SourceException {
    generateGetterMethod( null, false );
  }
  void generateGetterMethod( String body, boolean javadoc ) throws SourceException {

    ClassElement declaringClass = getDeclaringClass();
    MethodElement newGetter = new MethodElement();

    newGetter.setName( Identifier.create( (type == Type.BOOLEAN ? "is" : "get") + capitalizeFirstLetter( getName() ) ) );
    newGetter.setReturn( type );
    newGetter.setModifiers( Modifier.PUBLIC );
    if ( body != null )
      newGetter.setBody( body );
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_PropertyGetter" ),
                                             new Object[] { getName() } );
      newGetter.getJavaDoc().setRawText( comment );
    }


    //System.out.println ("Generating getter" );

    if ( declaringClass == null ) {
      System.out.println ("nodecl - gen getter");
      throw new SourceException();
    }
    else {
      //System.out.println ( "Adding getter method" );
      declaringClass.addMethod( newGetter );
      getterMethod = newGetter;
      }

  }

  void generateSetterMethod() throws SourceException {
    generateSetterMethod( null, false, false );
  }

  void generateSetterMethod( String body, boolean constrained, boolean javadoc ) throws SourceException {
    ClassElement declaringClass = getDeclaringClass();
    MethodElement newSetter = new MethodElement();
    
    newSetter.setName( Identifier.create( "set" + capitalizeFirstLetter( getName() ) ) );
    newSetter.setReturn( Type.VOID );
    newSetter.setModifiers( Modifier.PUBLIC );
    newSetter.setParameters( ( new MethodParameter[] { new MethodParameter( name, type, false ) } ));
    if ( constrained )
      newSetter.setExceptions( ( new Identifier[] { Identifier.create( "java.beans.PropertyVetoException" ) } ) );
    if ( body != null )
      newSetter.setBody( body );
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_PropertySetter" ),
                                             new Object[] { getName(), name } );
      if ( constrained ) 
        comment = comment + "\n@throws PropertyVetoException\n";
      newSetter.getJavaDoc().setRawText( comment );
    }


    if ( declaringClass == null ) {
      System.out.println ("nodecl - gen setter");
      throw new SourceException();
      }
    else {
      declaringClass.addMethod( newSetter );
      setterMethod = newSetter;
      }
  }

  void generateField() throws SourceException {
    generateField( false );
  }

  void generateField( boolean javadoc ) throws SourceException {
    ClassElement declaringClass = getDeclaringClass();
    FieldElement newField = new FieldElement();
    
    newField.setName( Identifier.create( Introspector.decapitalize( getName() ) ) );
    newField.setType( type );
    newField.setModifiers( Modifier.PRIVATE );
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_PropertyField" ),
                                             new Object[] { getName() } );
      newField.getJavaDoc().setRawText( comment );
    }
   
    if ( declaringClass == null ) {
      System.out.println ("nodecl - gen setter");
      throw new SourceException();
      }
    else {
      declaringClass.addField( newField );
      estimatedField = newField;
      }
  }

  /** Deletes the estimated field in source */
  
  void deleteEstimatedField() throws SourceException {
    
    if ( estimatedField == null )
      return;

    ClassElement declaringClass = getDeclaringClass();

    if ( declaringClass == null ) {
      System.out.println ("nodecl");
      throw new SourceException();
      }
    else {   
      declaringClass.removeField( estimatedField );
      //System.out.println ("removing estimated field");
      }
  }


  /** Deletes the setter method in source */
  
  void deleteGetterMethod() throws SourceException {
    
    if ( getterMethod == null )
      return;

    ClassElement declaringClass = getDeclaringClass();

    if ( declaringClass == null ) {
      System.out.println ("nodecl - del getter");
      throw new SourceException();
      }
    else {   
      declaringClass.removeMethod( getterMethod );
      //System.out.println ("removing getter");
      }
  }

  /** Deletes the setter method in source */

  void deleteSetterMethod() throws SourceException {

    if ( setterMethod == null )
      return;

    ClassElement declaringClass = getDeclaringClass();

    if ( declaringClass == null ) {
      System.out.println ("nodecl - del setter");
      throw new SourceException();
      }
    else {
      declaringClass.removeMethod( setterMethod );
      //System.out.println ("removing setter");
      }
  }

}

/* 
 * Log
 *  2    Gandalf   1.1         7/20/99  Petr Hrebejk    
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
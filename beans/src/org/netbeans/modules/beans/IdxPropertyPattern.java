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
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.openide.src.MethodElement;
import org.openide.src.MethodParameter; 
import org.openide.src.ClassElement; 
import org.openide.src.Type; 
import org.openide.src.SourceException;
import org.openide.src.Identifier;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/** IdxPropertyPattern: This class holds the information about used indexed 
 * property pattern in code.
 * @author Petr Hrebejk
 *
 * 
 *  PENDING: Add Pattern class hierarchy (abstract classes || interfaces )
 */
public class IdxPropertyPattern extends PropertyPattern {

  private static final ResourceBundle bundle = NbBundle.getBundle( IdxPropertyPattern.class );

  protected MethodElement indexedGetterMethod = null;
  protected MethodElement indexedSetterMethod = null;

  protected Type indexedType;

  /** Creates new PropertyPattern one of the methods may be null */
  public IdxPropertyPattern( PatternAnalyser patternAnalyser, 
                             MethodElement getterMethod, MethodElement setterMethod,
                             MethodElement indexedGetterMethod, MethodElement indexedSetterMethod )
    throws IntrospectionException {
 
    super ( patternAnalyser, getterMethod, setterMethod );

    this.indexedGetterMethod = indexedGetterMethod;
    this.indexedSetterMethod = indexedSetterMethod;
  
    findIndexedPropertyType();
    name = findIndexedPropertyName();
  }


  /*
  public boolean equals( Object o ) {
    if ( ! (o instanceof IdxPropertyPattern ) )
      return false;
    
    return super.equals( o ) &&
           ((IdxPropertyPattern)o).indexedGetterMethod == getterMethod &&
           ((IdxPropertyPattern)o).indexedSetterMethod == setterMethod ;
  }

  public int hashCode( ) {
    return super.hashCode() + 
      ( indexedGetterMethod != null ? indexedGetterMethod.hashCode() : 0 ) + 
      ( indexedSetterMethod != null ? indexedSetterMethod.hashCode() : 0 );
  }
  */

  private IdxPropertyPattern( PatternAnalyser patternAnalyser ) {
    super( patternAnalyser );
  }

  static IdxPropertyPattern create( PatternAnalyser patternAnalyser, 
                                 String name, String type, String indexedType ) throws SourceException {

    IdxPropertyPattern ipp = new IdxPropertyPattern( patternAnalyser );
    
    ipp.name = name;
    ipp.type = Type.parse( type );
    ipp.indexedType = Type.parse( indexedType );
  
    ipp.generateGetterMethod();
    ipp.generateSetterMethod();
    ipp.generateIndexedGetterMethod();
    ipp.generateIndexedSetterMethod();
    
    return ipp;
  }

  /** Creates new indexed property pattern with extended options */

  static IdxPropertyPattern create( PatternAnalyser patternAnalyser, 
                                 String name, String type,
                                 int mode, boolean bound, boolean constrained,
                                 boolean withField, boolean withReturn,
                                 boolean withSet, boolean withSupport,
                                 boolean niGetter, boolean niWithReturn,
                                 boolean niSetter, boolean niWithSet ) throws SourceException {

    IdxPropertyPattern ipp = new IdxPropertyPattern( patternAnalyser );

    ipp.name = name;
    ipp.type = null;
    ipp.indexedType = Type.parse( type );

    

    /** Generate field */ 
    if ( withField || withSupport ) {
      ipp.type = Type.createArray( ipp.indexedType );
      if ( ipp.type != null ) 
        ipp.generateField( true );
    }

    /** Ensure property change support field and methods exist */
    String supportName = null;
    String vetoSupportName = null;

    if ( withSupport ) {
      if ( bound )
        supportName = BeanPatternGenerator.supportField( ipp.getDeclaringClass() );
      if ( constrained ) 
        vetoSupportName = BeanPatternGenerator.vetoSupportField( ipp.getDeclaringClass() );

      if ( bound )
        BeanPatternGenerator.supportListenerMethods( ipp.getDeclaringClass(), supportName );
      if ( constrained )
        BeanPatternGenerator.vetoSupportListenerMethods( ipp.getDeclaringClass(), vetoSupportName );
    }

    if ( mode == READ_WRITE || mode == READ_ONLY ) {
      ipp.generateIndexedGetterMethod( BeanPatternGenerator.idxPropertyGetterBody( name, withReturn ), true );      
      if ( niGetter )
        ipp.generateGetterMethod( BeanPatternGenerator.propertyGetterBody( name, niWithReturn), true );
    }
    if ( mode == READ_WRITE || mode == WRITE_ONLY ) {
      ipp.generateIndexedSetterMethod( BeanPatternGenerator.idxPropertySetterBody( name, ipp.getType(),
          bound, constrained, withSet, withSupport, supportName, vetoSupportName ), constrained, true );
      if ( niSetter )
        ipp.generateSetterMethod( BeanPatternGenerator.propertySetterBody( name, ipp.getType(),
          bound, constrained, niWithSet, withSupport, supportName, vetoSupportName ), constrained, true );
    }
    return ipp;
  }


  
  /** Getter for indexed type */
  
  public Type getIndexedType() {
    return indexedType;
  }

  /** Getter for indexedGetter method */

  public MethodElement getIndexedGetterMethod() {
    return indexedGetterMethod; 
	}


  public void setName(String name) throws  SourceException {
    super.setName( name );

    name = capitalizeFirstLetter( name );   

    if ( indexedGetterMethod != null ) {
      Identifier idxGetterMethodID = Identifier.create(( indexedGetterMethod.getName().getName().startsWith("get") ?
                                                   "get" : "is" ) + name );
      indexedGetterMethod.setName( idxGetterMethodID );
    }
    if ( indexedSetterMethod != null ) {
      Identifier idxSetterMethodID = Identifier.create( "set" + name );
      indexedSetterMethod.setName( idxSetterMethodID );
    }
  }

  /** Getter for indexedSetter method */

  public MethodElement getIndexedSetterMethod() {
    return indexedSetterMethod; 
	}

  /** Tests if the pattern is public i.e. all needed parts are public */
  public boolean isPublic() {
    return  super.isPublic() &&
            (indexedGetterMethod == null || ( indexedGetterMethod.getModifiers() & Modifier.PUBLIC ) != 0 ) &&
            (indexedSetterMethod == null || ( indexedSetterMethod.getModifiers() & Modifier.PUBLIC ) != 0 );
  }

  /** Sets the type of property */

  public void setType( Type type ) throws SourceException {
    
    if ( this.type != null && this.type.compareTo( type, true ) )
      return;
    
    // Remember the old type & old indexed type
    Type oldIndexedType = this.indexedType;
    Type oldType = this.type;
    
    if ( oldType == null ) {
      this.type = type;
      oldType = type;
      int mode = getMode();
      if ( mode == READ_WRITE || mode == READ_ONLY )
        generateGetterMethod();
      if ( mode == READ_WRITE || mode == WRITE_ONLY )
        generateSetterMethod();
    }
    else  
      // Change the type
      super.setType( type );

    // Test if the idexedType is the type of array and change it if so

    if ( type.isArray() && oldType.isArray() && oldType.getElementType().compareTo( oldIndexedType, false ) ) {
      Type newType = type.getElementType();

      if (indexedGetterMethod != null ) {
        indexedGetterMethod.setReturn( newType );      
      }
      if (indexedSetterMethod != null ) { 
        MethodParameter[] params = indexedSetterMethod.getParameters();
        if ( params.length > 1 ) {
          params[1].setType( newType );
          indexedSetterMethod.setParameters( params );
        }
      }
    }
  }

  /** Sets the type of propertyPattern */
  public void setIndexedType(Type type) throws SourceException {
    
    if ( this.indexedType.compareTo( type, true ) )
      return;

    // Remember the old type & old indexed type

    Type oldIndexedType = this.indexedType; 
    Type oldType = this.type;

    // Change the indexed type

    if (indexedGetterMethod != null ) {
      indexedGetterMethod.setReturn( type );      
    }
    if (indexedSetterMethod != null ) { 
      MethodParameter[] params = indexedSetterMethod.getParameters();
      if ( params.length > 1 ) {
        params[1].setType( type );
        indexedSetterMethod.setParameters( params );
      }
    }

    // Test if the old type of getter and seter was an array of indexedType
    // if so change the type of that array.

    if ( oldType != null && oldType.isArray() && oldType.getElementType().compareTo( oldIndexedType, false ) ) {
      Type newArrayType = Type.createArray( type );
      super.setType( newArrayType );
    }
  }

  /** Returns the mode of the property READ_WRITE, READ_ONLY or WRITE_ONLY */
  public int getMode() {
    if ( indexedSetterMethod != null && indexedGetterMethod != null )
      return READ_WRITE;
    else if ( indexedGetterMethod != null && indexedSetterMethod == null )
      return READ_ONLY;
    else if ( indexedSetterMethod != null && indexedGetterMethod == null )
      return WRITE_ONLY;
    else
      return super.getMode();
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
      if ( indexedGetterMethod == null )
        generateIndexedGetterMethod();
      if ( indexedSetterMethod == null )
        generateIndexedSetterMethod(); 
      break;
    case READ_ONLY:
      if ( getterMethod == null )
        generateGetterMethod();
      if ( indexedGetterMethod == null )
        generateIndexedGetterMethod();
      if ( setterMethod != null )
        deleteSetterMethod();
      if ( indexedSetterMethod != null )
        deleteIndexedSetterMethod();
      break;
    case WRITE_ONLY:
      if ( setterMethod == null )
        generateSetterMethod();
      if ( indexedSetterMethod == null )
        generateIndexedSetterMethod();
      if ( getterMethod != null )
        deleteGetterMethod();
      if ( indexedGetterMethod != null )
        deleteIndexedGetterMethod();
      break;
    }
    
  }

  /** Gets the cookie of the first available method */

  public Node.Cookie getCookie( Class cookieType ) {
    if ( indexedGetterMethod != null )
      return indexedGetterMethod.getCookie( cookieType );
    
    if ( indexedSetterMethod != null )
      return indexedSetterMethod.getCookie( cookieType );
      
    return super.getCookie( cookieType );
  }

  /** Destroys this pattern */

  public void destroy() throws SourceException {
    deleteIndexedSetterMethod();
    deleteIndexedGetterMethod();
    super.destroy();
  }

  // Utitlity methods -------------------------------------------------------------------

  /** Package private constructor. Merges two property descriptors. Where they
   * conflict, gives the second argument (y) priority over the first argumnet (x).
   *
   * @param x The first (lower priority) PropertyPattern.
   * @param y The second (higher priority) PropertyPattern.
   */

  IdxPropertyPattern( PropertyPattern x, PropertyPattern y ) {
    super( x, y );
    if ( x instanceof IdxPropertyPattern ) {
      IdxPropertyPattern ix = (IdxPropertyPattern)x;
      indexedGetterMethod = ix.indexedGetterMethod;
      indexedSetterMethod = ix.indexedSetterMethod;
      indexedType = ix.indexedType;
    }
    if ( y instanceof IdxPropertyPattern ) {
      IdxPropertyPattern iy = (IdxPropertyPattern)y;
      if ( iy.indexedGetterMethod != null )
        indexedGetterMethod = iy.indexedGetterMethod;
      if ( iy.indexedSetterMethod != null )
        indexedSetterMethod = iy.indexedSetterMethod;
      indexedType = iy.indexedType;
    }
    name  = findIndexedPropertyName();
  }


  /** Resolves the type of indexed property. Chcecks for conformance to 
   * design patterns.
   */

  private void findIndexedPropertyType() throws IntrospectionException {
    
    indexedType = null;

    if ( indexedGetterMethod != null ) {
      MethodParameter[] params = indexedGetterMethod.getParameters();
      if ( params.length != 1 ) {
        throw new IntrospectionException( "bad indexed read method arg count" );
      }
      if ( !params[0].getType().compareTo( Type.INT, false ) ) {
        throw new IntrospectionException( "not int index to indexed read method" );
      }
      indexedType = indexedGetterMethod.getReturn();
      if ( indexedType.compareTo( Type.VOID, false ) ) {
        throw new IntrospectionException( "indexed read method return void" );
      }
    }

    if (indexedSetterMethod != null ) {
      MethodParameter params[] = indexedSetterMethod.getParameters();
      if ( params.length != 2 ) {
        throw new IntrospectionException( "bad indexed write method arg count" );
      }
      if ( !params[0].getType().compareTo( Type.INT, false ) ) {
        throw new IntrospectionException( "non int index to indexed write method" );
      }
      if (indexedType != null && !indexedType.compareTo( params[1].getType(), false ) ) {
        throw new IntrospectionException( 
          "type mismatch between indexed read and write methods" );
      }
      indexedType = params[1].getType();
    }

    //type = indexedType;

    Type propType = getType();
    if ( propType != null &&  (!propType.isArray() || !propType.getElementType().compareTo(indexedType, false))) {
      throw new IntrospectionException( 
          "type mismatch between indexed read and write methods" );
    }
  }

  /** Decides about the name of the property from names of the methods */
  
  String findIndexedPropertyName() {

    String superName = findPropertyName();

    if ( superName == null ) {
      String methodName = null; 

      if ( indexedGetterMethod != null )
        methodName = indexedGetterMethod.getName().getName();  
      else if ( indexedSetterMethod != null )
        methodName = indexedSetterMethod.getName().getName();
      else 
        throw new InternalError( "Indexed property with all methods == null" );
        
      return methodName.startsWith( "is" ) ?
        Introspector.decapitalize( methodName.substring(2) ) :
        Introspector.decapitalize( methodName.substring(3) );  
        }
    else
      return superName;
  }

  // Methods for generating / dleting methods -------------------------------------------


  void generateGetterMethod() throws SourceException {
    if ( type != null )
      super.generateGetterMethod();   
  }

  void generateSetterMethod() throws SourceException {
    if ( type != null )
      super.generateSetterMethod();
  }
 
  void generateIndexedGetterMethod() throws SourceException {
    generateIndexedGetterMethod( null, false );
  }

  void generateIndexedGetterMethod( String body, boolean javadoc ) throws SourceException {

    ClassElement declaringClass = getDeclaringClass();
    MethodElement newGetter = new MethodElement();
    MethodParameter[] newParameters = { new MethodParameter( "index", Type.INT, false ) };

    newGetter.setName( Identifier.create( "get" + capitalizeFirstLetter( getName() ) ) );
    newGetter.setReturn( indexedType );
    newGetter.setModifiers( Modifier.PUBLIC );
    newGetter.setParameters( newParameters );
    if ( declaringClass.isInterface() ) {
      newGetter.setBody( null );
    }
    else if ( body != null )
      newGetter.setBody( body );
    
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_IdxPropertyGetter" ),
                                             new Object[] { getName() } );
      newGetter.getJavaDoc().setRawText( comment );
    }

    //System.out.println ("Generating getter" );

    if ( declaringClass == null )
      throw new SourceException();
    else {
      //System.out.println ( "Adding getter method" );
      declaringClass.addMethod( newGetter );
      indexedGetterMethod = newGetter;
      }
  }

  void generateIndexedSetterMethod() throws SourceException {
    generateIndexedSetterMethod(null, false, false );
  }

  void generateIndexedSetterMethod( String body, boolean constrained, boolean javadoc ) throws SourceException {

    ClassElement declaringClass = getDeclaringClass();
    MethodElement newSetter = new MethodElement();
    MethodParameter[] newParameters = { new MethodParameter( "index", Type.INT, false ),
                                        new MethodParameter( name, indexedType, false ) };
    
    newSetter.setName( Identifier.create( "set" + capitalizeFirstLetter( getName() ) ) );
    newSetter.setReturn( Type.VOID );
    newSetter.setModifiers( Modifier.PUBLIC );
    newSetter.setParameters( newParameters );
    if ( constrained )
      newSetter.setExceptions( ( new Identifier[] { Identifier.create( "java.beans.PropertyVetoException" ) } ) );
    if ( declaringClass.isInterface() ) {
      newSetter.setBody( null );
    }
    else if ( body != null ) {
      newSetter.setBody( body );
    }
    
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_IdxPropertySetter" ),
                                             new Object[] { getName(), name } );
      if ( constrained ) 
        comment = comment + "\n@throws PropertyVetoException\n";
      newSetter.getJavaDoc().setRawText( comment );
    }

    if ( declaringClass == null )
      throw new SourceException();
    else {
      declaringClass.addMethod( newSetter );
      indexedSetterMethod = newSetter;
      }
  }

 
  /** Delete indexedGetter form source */

  void deleteIndexedGetterMethod() throws SourceException {
    
    if ( indexedGetterMethod == null )
      return;

    ClassElement declaringClass = getDeclaringClass();

    if ( declaringClass == null ) {
      throw new SourceException();
    }
    else {
      declaringClass.removeMethod( indexedGetterMethod );
    }
  }
  
  /** Delete indexedSetter form source */

  void deleteIndexedSetterMethod() throws SourceException {
    
    if ( indexedSetterMethod == null )
      return;

    ClassElement declaringClass = getDeclaringClass();

    if ( declaringClass == null ) {
      throw new SourceException();
    }
    else {
      declaringClass.removeMethod( indexedSetterMethod );
    }
    
  }

}

/* 
 * Log
 *  3    Gandalf   1.2         7/21/99  Petr Hrebejk    Bug fixes interface 
 *       bodies, is for boolean etc
 *  2    Gandalf   1.1         7/20/99  Petr Hrebejk    
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
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
import java.beans.Introspector;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.openide.util.NbBundle;
import org.openide.src.*;

/** Singleton with static methods for generating bodies of and 
 * additional elements for bean patterns.
 * @author Petr Hrebejk
 */
class BeanPatternGenerator extends Object {

  /** The ResourceBundle */
  private static final ResourceBundle bundle = NbBundle.getBundle( BeanPatternGenerator.class );

  
  /** Constant for one Tab */
  private static final String TAB = "  ";
  /** Constant for two Tabs */
  private static final String TABx2 = TAB + TAB;
  /** Constant for three Tabs */
  private static final String TABx3 = TABx2 + TAB;

  /** Generates the body of the setter method of Property.
   * @param name Name of the property
   * @param type Type of the property
   * @param bound Is the property bound?
   * @param constrained Is the property constrained?
   * @param withSet Should be the set command of property private field generated.
   * @param withSupport Generate the firing of (Veto|Property)Change Events?
   * @param supportName Name of field containing <CODE>PropertyChangeListeners</CODE>.
   * @param vetoSupportName Name of field containing <CODE>VetoableChangeListeners</CODE>.
   * @return Sring containing the body of the setter method.
   */
  static String propertySetterBody(String name, Type type, 
        boolean bound, boolean constrained, 
        boolean withSet, boolean withSupport,
        String supportName, String vetoSupportName) {

    StringBuffer setterBody = new StringBuffer( 200 );
    setterBody.append( "\n" );
    if ( withSupport ) {
      /* Generates body in the form:
         PropType oldPropName = this.propName;
         this.propName = propName;
         changes.firePropertyChange(propName, oldPropName, propName );
      */

      setterBody.append( TAB + type.toString() );
      setterBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) );
      setterBody.append( " = this." ).append( name ).append( ";\n");

      if ( constrained ) {
        setterBody.append( TAB + vetoSupportName ).append( ".fireVetoableChange(\"").append( name ).append( "\", " );

        if ( type.isPrimitive() ) {
          setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" );
          setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) );
          setterBody.append( "), " );
          setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" );
          setterBody.append( name ).append( "));\n" );
        }
        else {
          setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) );
          setterBody.append( ", " ).append( name ).append( ");\n" );
        }
      }
      if ( bound ) {
        setterBody.append( TAB + "this." ).append( name );
        setterBody.append( " = " ).append( name ).append( ";\n");
        setterBody.append( TAB + supportName ).append( ".firePropertyChange (\"").append( name ).append( "\", " );

        if ( type.isPrimitive() ) {
          setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" );
          setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) );
          setterBody.append( "), " );
          setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" );
          setterBody.append( name ).append( "));\n" );
        }
        else {
          setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) );
          setterBody.append( ", " ).append( name ).append( ");\n" );
        }
      }
    }
    else if ( withSet ) {
      /* Generates body in the form:
         this.propName = propName;
       */
      setterBody.append( TAB + "this." );
      setterBody.append( name );
      setterBody.append( " = " ).append( name ).append( ";\n" );
    }
    return setterBody.toString();
  }

  /** Generates the body of the setter method of IndexedProperty.
   * @param name Name of the property
   * @param type Type of the property
   * @param bound Is the property bound?
   * @param constrained Is the property constrained?
   * @param withSet Should be the set command of property private field generated.
   * @param withSupport Generate the firing of (Veto|Property)Change Events?
   * @param supportName Name of field containing <CODE>PropertyChangeListeners</CODE>.
   * @param vetoSupportName Name of field containing <CODE>VetoableChangeListeners</CODE>.
   * @return Sring containing the body of the setter method.
   */
  static String idxPropertySetterBody( String name, Type type,
                                    boolean bound, boolean constrained,
                                    boolean withSet, boolean withSupport,
                                    String supportName,
                                    String vetoSupportName ) {
    StringBuffer setterBody = new StringBuffer( 200 );
    setterBody.append( "\n" );

    if ( withSet || withSupport ) {
      /* Generates body in the form:
         this.propName = propName;
      */
      setterBody.append( TAB + "this." );
      setterBody.append( name );
      setterBody.append( "[index] = " ).append( name ).append( ";\n" );
    }

    if ( withSupport && constrained ) {
      setterBody.append( TAB + vetoSupportName ).append( ".fireVetoableChange (\"").append( name ).append( "\", " );
      setterBody.append( "null, null );\n" );
    }
    if ( withSupport && bound ) {
      setterBody.append( TAB + supportName ).append( ".firePropertyChange (\"").append( name ).append( "\", " );
      setterBody.append( "null, null );\n" );
    }

    return setterBody.toString();
  }

  /** Generates the body of the getter method of Property.
   * @param name Name of the property.
   * @param withReturn Should be the return command with property private field generated?
   * @return Sring containing the body of the getter method.
   */
  static String propertyGetterBody( String name, boolean withReturn ) {
    StringBuffer getterBody = new StringBuffer( 50 );
    getterBody.append( "\n");
    if ( withReturn ) {
      /* Generates body in the form:
         return propName;
       */
      getterBody.append( TAB + "return " );
      getterBody.append( name ).append( ";\n" );
    }
    return getterBody.toString();
  }

  /** Generates the body of the getter method of IndexedProperty.
   * @param name Name of the property.
   * @param withReturn Should be the return command with property private field generated?
   * @return Sring containing the body of the getter method.
   */
  static String idxPropertyGetterBody( String name, boolean withReturn ) {
    StringBuffer getterBody = new StringBuffer( 50 );
    getterBody.append( "\n");
    if ( withReturn ) {
      /* Generates body in the form:
         return propName;
       */
      getterBody.append( TAB + "return " );
      getterBody.append( name ).append( "[index];\n" );
    }
    return getterBody.toString();
  }

  
  /** Gets the <CODE>PropertyChangeSupport</CODE> field in Class. Tryes to find
   * a field of type <CODE>PropertyChangeSupport</CODE>. If such field doesn't
   * exist creates a new one with name <CODE>propertyChangeSupport</CODE>.
   * @param ce Class to operate on.
   * @throws SourceException If the modification of the source is impossible.
   * @return Name of foun or newly created <CODE>PropertyChangeSupport</CODE> field.
   */
  static String supportField(ClassElement ce) throws SourceException {
    String supportName = null;
    Identifier supportId = Identifier.create( "java.beans.PropertyChangeSupport" );
    FieldElement[] fields = ce.getFields();

    for( int i = 0; i < fields.length; i++ ) {      // Try to find suitable field
      if ( fields[i].getType().isClass() &&
      fields[i].getType().getClassName().compareTo( supportId, false ) ) {
        supportName = fields[i].getName().getName();
        break;
      }
    }

    if ( supportName == null ) { // Field not found we create new
      supportName = "propertyChangeSupport";
      FieldElement supportField = new FieldElement();
      supportField.setName( Identifier.create( supportName ) );
      supportField.setType( Type.createClass( supportId ) );
      supportField.setModifiers( Modifier.PRIVATE );
      supportField.setInitValue( " new java.beans.PropertyChangeSupport (this)" );
      supportField.getJavaDoc().setRawText( bundle.getString( "COMMENT_PropertyChangeSupport" ) );
      ce.addField( supportField );
    }

    return supportName;
  }

  /** Gets the <CODE>VetoableChangeSupport</CODE> field in Class. Tryes to find
   * a field of type <CODE>VetoableChangeSupport</CODE>. If such field doesn't
   * exist creates a new one with name <CODE>vetoableChangeSupport</CODE>.
   * @param ce Class to operate on.
   * @throws SourceException If the modification of the source is impossible.
   * @return Name of foun or newly created <CODE>vetoableChangeSupport</CODE> field.
   */  
  static String vetoSupportField( ClassElement ce ) throws SourceException {
    String vetoSupportName = null;
    Identifier vetoSupportId = Identifier.create( "java.beans.VetoableChangeSupport" ); 
    FieldElement[] fields = ce.getFields();

    for( int i = 0; i < fields.length; i++ ) {      // Try to find suitable field
      if ( fields[i].getType().isClass() && 
           fields[i].getType().getClassName().compareTo( vetoSupportId, false ) ) {
        vetoSupportName = fields[i].getName().getName();
        break;
      }
    }

    if ( vetoSupportName == null ) { // Field not found we create new
      vetoSupportName = "vetoableChangeSupport";
      FieldElement supportField = new FieldElement();
      supportField.setName( Identifier.create( vetoSupportName ) );
      supportField.setType( Type.createClass( vetoSupportId ) );
      supportField.setModifiers( Modifier.PRIVATE );
      supportField.setInitValue( " new java.beans.VetoableChangeSupport (this)" );
      supportField.getJavaDoc().setRawText( bundle.getString( "COMMENT_VetoableChangeSupport" ) );
      ce.addField( supportField );
    }

    return vetoSupportName;
  }

  /** If in the class don't exists methods for adding/removing PropertyChangeListeners
   * for given field adds them.
   * @param classElement Class to operate on.
   * @param supportName The <CODE>PropertyChangeSupport</CODE> field the methods will be generated for.
   * @throws SourceException If the modification of the source is impossible.
   */
  static void supportListenerMethods( ClassElement classElement, String supportName )
    throws SourceException {
    
    Identifier addMethodId = Identifier.create( "addPropertyChangeListener" );
    MethodElement addMethod = null;
    Identifier removeMethodId = Identifier.create( "removePropertyChangeListener" );
    MethodElement removeMethod = null;
    Identifier listenerTypeId = Identifier.create( "java.beans.PropertyChangeListener" ); 
    Type listenerType = Type.createClass( listenerTypeId );

    addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );
    if ( addMethod == null ) {
      addMethod = new MethodElement();
      addMethod.setName( addMethodId );
      addMethod.setReturn( Type.VOID );
      addMethod.setModifiers( Modifier.PUBLIC );
      addMethod.setParameters( ( new MethodParameter[] { new MethodParameter( "l", listenerType, false ) } ));

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" ).append( TAB + supportName );
      body.append( ".addPropertyChangeListener (l);\n" );
      addMethod.setBody( body.toString() );

      /*
      String comment = MessageFormat.format( bundle.getString( "COMMENT_AddPropertyChangeListener" ), 
                                             new Object[] { listenerType.getClassName().getName() } );
      */                                          
      addMethod.getJavaDoc().setRawText( bundle.getString( "COMMENT_AddPropertyChangeListener" ) );
          

      classElement.addMethod( addMethod );
    }

    removeMethod = classElement.getMethod( removeMethodId, new Type[] { listenerType }  );
    if ( removeMethod == null ) {
      removeMethod = new MethodElement();
      removeMethod.setName( removeMethodId );
      removeMethod.setReturn( Type.VOID );
      removeMethod.setModifiers( Modifier.PUBLIC );
      removeMethod.setParameters( ( new MethodParameter[] { new MethodParameter( "l", listenerType, false ) } ));

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" ).append( TAB + supportName );
      body.append( ".removePropertyChangeListener (l);\n" );
      removeMethod.setBody( body.toString() );
      removeMethod.getJavaDoc().setRawText( bundle.getString( "COMMENT_RemovePropertyChangeListener" ) );
      classElement.addMethod( removeMethod );
    }
  }
    
  
  /** If in the class don't exists methods for adding/removing VetoableChangeListeners
   * for given field adds them.
   * @param classElement Class to operate on.
   * @param supportName The <CODE>vetoableChangeSupport</CODE> field the methods will be generated for.
   * @throws SourceException If the modification of the source is impossible.
   */
  static void vetoSupportListenerMethods( ClassElement classElement, String supportName )
    throws SourceException {
    
    Identifier addMethodId = Identifier.create( "addVetoableChangeListener" );
    MethodElement addMethod = null;
    Identifier removeMethodId = Identifier.create( "removeVetoableChangeListener" );
    MethodElement removeMethod = null;
    Identifier listenerTypeId = Identifier.create( "java.beans.VetoableChangeListener" ); 
    Type listenerType = Type.createClass( listenerTypeId );

    addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );
    if ( addMethod == null ) {
      addMethod = new MethodElement();
      addMethod.setName( addMethodId );
      addMethod.setReturn( Type.VOID );
      addMethod.setModifiers( Modifier.PUBLIC );
      addMethod.setParameters( ( new MethodParameter[] { new MethodParameter( "l", listenerType, false ) } ));

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" ).append( TAB + supportName );
      body.append( ".addVetoableChangeListener (l);\n" );
      addMethod.setBody( body.toString() );
      addMethod.getJavaDoc().setRawText( bundle.getString( "COMMENT_AddVetoableChangeListener" ) );
      classElement.addMethod( addMethod );
    }

    removeMethod = classElement.getMethod( removeMethodId, new Type[] { listenerType }  );
    if ( removeMethod == null ) {
      removeMethod = new MethodElement();
      removeMethod.setName( removeMethodId );
      removeMethod.setReturn( Type.VOID );
      removeMethod.setModifiers( Modifier.PUBLIC );
      removeMethod.setParameters( ( new MethodParameter[] { new MethodParameter( "l", listenerType, false ) } ));

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" ).append( TAB + supportName );    
      body.append( ".removeVetoableChangeListener (l);\n" );
      removeMethod.setBody( body.toString() );
      removeMethod.getJavaDoc().setRawText( bundle.getString( "COMMENT_RemoveVetoableChangeListener" ) );
      classElement.addMethod( removeMethod );
    }
  }

  /** Ensures that the listeners array list exists. Used for generating
   * multicast event source support implemented by java.util.ArrayList.
   * Searches the source for suitable field. If the field does not exists
   * creates new one.
   * @param ce Class to operate on.
   * @param type Type of the Event Listener.
   * @throws SourceException If the modification of the source is impossible.
   * @return Name of found or newly created field.
   */
  static String listenersArrayListField( ClassElement ce, Type type ) throws SourceException {
    
    String fieldName = null;
    String fieldNameToFind = Introspector.decapitalize( type.getClassName().getName() ) + "List";
      
    Identifier fieldTypeId = Identifier.create( "java.util.ArrayList" ); 
    FieldElement[] fields = ce.getFields();

    for( int i = 0; i < fields.length; i++ ) {      // Try to find suitable field
      if ( fields[i].getType().isClass() && 
           fields[i].getType().getClassName().compareTo( fieldTypeId, false ) &&
           fields[i].getName().getName().equals( fieldNameToFind ) ) {
        fieldName = fields[i].getName().getName();
        break;
      }
    }

    if ( fieldName == null ) { // Field not found we create new
      fieldName = fieldNameToFind;
      FieldElement field = new FieldElement();
      field.setName( Identifier.create( fieldName ) );
      field.setType( Type.createClass( fieldTypeId ) );
      field.setModifiers( Modifier.PRIVATE | Modifier.TRANSIENT );    
      String comment = MessageFormat.format( bundle.getString( "COMMENT_ListenerArrayList" ),
                                             new Object[] { type.getClassName().getName() } );                                          
      field.getJavaDoc().setRawText( comment );
      
      ce.addField( field );
    }

    return fieldName;
  }

  /** Ensure the listenersList  exists. Used for generating
   * multicast event source support implemented by javax.swing.event.EventListenerList.
   * Searches the source for suitable field. If the field does not exists
   * creates new one.
   * @param ce Class to operate on.
   * @param type Type of the Event Listener.
   * @throws SourceException If the modification of the source is impossible.
   * @return Name of found or newly created field.
   */
  static String eventListenerListField( ClassElement ce, Type type ) throws SourceException {
    
    String fieldName = null;
    
    Identifier fieldTypeId = Identifier.create( "javax.swing.event.EventListenerList" ); 
    FieldElement[] fields = ce.getFields();

    for( int i = 0; i < fields.length; i++ ) {      // Try to find suitable field
      if ( fields[i].getType().isClass() && 
           fields[i].getType().getClassName().compareTo( fieldTypeId, false ) ) {
        fieldName = fields[i].getName().getName();
        break;
      }
    }

    if ( fieldName == null ) { // Field not found we create new
      fieldName = "listenerList";
      FieldElement field = new FieldElement();
      field.setName( Identifier.create( fieldName ) );
      field.setType( Type.createClass( fieldTypeId ) );
      field.setModifiers( Modifier.PRIVATE );
      field.setInitValue( " null" );
      String comment = MessageFormat.format( bundle.getString( "COMMENT_EventListenerList" ),
                                             new Object[] { type.getClassName().getName() } );                                          
      field.getJavaDoc().setRawText( comment );

      ce.addField( field );
    }

    return fieldName;
  }

  /** Ensure that listener field for unicast exists. Used for generating
   * unicast event source support.
   * Searches the source for suitable field. If the field does not exists
   * creates new one.
   * @param ce Class to operate on.
   * @param type Type of the Event Listener.
   * @throws SourceException If the modification of the source is impossible.
   */
  static void unicastListenerField( ClassElement ce, Type type ) throws SourceException {
     
    String fieldName = null;
    String fieldNameToFind = Introspector.decapitalize( type.getClassName().getName() );
    if ( fieldNameToFind.equals( type.getClassName().getName() ) ) {
      fieldNameToFind = new String( "listener" + fieldNameToFind  );
    }
      
    FieldElement[] fields = ce.getFields();

    for( int i = 0; i < fields.length; i++ ) {      // Try to find suitable field
      if ( fields[i].getType().isClass() && 
           fields[i].getType().getClassName().compareTo( type.getClassName(), false ) &&
           fields[i].getName().getName().equals( fieldNameToFind ) ) {
        fieldName = fields[i].getName().getName();
        break;
      }
    }

    if ( fieldName == null ) { // Field not found we create new
      fieldName = fieldNameToFind;
      FieldElement field = new FieldElement();
      field.setName( Identifier.create( fieldName ) );
      field.setType( type );
      field.setModifiers( Modifier.PRIVATE  | Modifier.TRANSIENT );
      field.setInitValue( " null" );
      String comment = MessageFormat.format( bundle.getString( "COMMENT_UnicastEventListener" ),
                                             new Object[] { type.getClassName().getName() } );                                          
      field.getJavaDoc().setRawText( comment );
      ce.addField( field );
    }
  }
  
  static String mcAddBody( Type type, int implementation, String listenerList ) {
  
    String fieldName = Introspector.decapitalize( type.getClassName().getName() ) + "List";

    StringBuffer body = new StringBuffer( 50 );

    if ( listenerList == null )
      listenerList = "listenerList";

    body.append( "\n");

    if ( implementation == 1 ) {
      body.append( TAB + "if (" ).append( fieldName ).append( " == null ) {\n" );
      body.append( TABx2 ).append( fieldName ).append( " = new java.util.ArrayList ();\n" );
      body.append( TAB ).append( "}\n" );
      body.append( TAB + fieldName ).append( ".add (listener);\n" );
    }
    else if ( implementation == 2 ) {
      body.append( TAB + "if (" ).append( listenerList ).append( " == null ) {\n" );
      body.append( TABx2 ).append( listenerList ).append( " = new javax.swing.event.EventListenerList();\n" );
      body.append( TAB ).append( "}\n" );
      body.append( TAB + listenerList ).append( ".add (" );
      body.append( type.toString()).append( ".class, listener);\n" );
    }

    return body.toString();
  }

  static String mcRemoveBody( Type type, int implementation, String listenerList ) {
    
    String fieldName = Introspector.decapitalize( type.getClassName().getName() ) + "List";

    if ( listenerList == null )
      listenerList = "listenerList";

    StringBuffer body = new StringBuffer( 50 );
    body.append( "\n");

    if ( implementation == 1 ) {
      body.append( TAB + "if (" ).append( fieldName ).append( " != null ) {\n" );
      body.append( TABx2 + fieldName ).append( ".remove (listener);\n" );
      body.append( TAB ).append( "}\n" );
    }
    else if ( implementation == 2 ) {
      body.append( TAB + listenerList ).append( ".remove (" );
      body.append( type.toString()).append( ".class, listener);\n" );
    }

    return body.toString();
  }

  static String ucAddBody( Type type, int implementation ) {

    String fieldName = Introspector.decapitalize( type.getClassName().getName() );
    if ( fieldName.equals( type.getClassName().getName() ) ) {
      fieldName = new String( "listener" + fieldName  );
    }

    StringBuffer body = new StringBuffer( 50 );

    body.append( "\n");

    if ( implementation == 1 ) {
      body.append( TAB + "if (").append( fieldName ).append( " != null) {\n" );
      body.append( TABx2 + "throw new java.util.TooManyListenersException ();\n" );
      body.append( TAB + "}\n" );
      body.append( TAB + fieldName ).append( " = listener;\n" );
    }
    
    return body.toString();
  }

  static String ucRemoveBody( Type type, int implementation ) {
    
    String fieldName = Introspector.decapitalize( type.getClassName().getName() );
    if ( fieldName.equals( type.getClassName().getName() ) ) {
      fieldName = new String( "listener" + fieldName  );
    }

    StringBuffer body = new StringBuffer( 50 );
    body.append( "\n");

    if ( implementation == 1 ) {
      body.append( TAB + fieldName ).append( " = null;\n" );
    }

    return body.toString();
  }
  
  
  static void fireMethod( ClassElement classElement, Type type,
                          MethodElement method, int implementation,
                          String listenerList,
                          boolean passEvent )
    throws SourceException {
    
    if ( listenerList == null )
      listenerList = "listenerList";

    Identifier methodId = Identifier.create( 
                            "fire" + 
                            Pattern.capitalizeFirstLetter( type.getClassName().getName() ) +
                            Pattern.capitalizeFirstLetter( method.getName().getName() ) );

    MethodElement newMethod = null;

    Type eventType = null;
    MethodParameter params[] = method.getParameters();
    if ( params.length > 0 )
      eventType = params[0].getType();
    else
      eventType = Type.createClass( Identifier.create( "java.util.EventObject" ) );

    ClassElement eventClass = ClassElement.forName( eventType.toString() );


    //addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );

    //if ( addMethod == null ) {
      newMethod = new MethodElement();
      newMethod.setName( methodId );
      newMethod.setReturn( Type.VOID );
      newMethod.setModifiers( Modifier.PRIVATE );
      
      MethodParameter[] newMethodParams = generateFireParameters( eventType, eventClass, passEvent );
      newMethod.setParameters( newMethodParams );

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" );
     
      if ( implementation == 1 ) {
        String fieldName = Introspector.decapitalize( type.getClassName().getName() ) + "List";

        body.append( TAB + "java.util.ArrayList list;\n" );

        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( TAB + eventType.toString() ).append( " e = new ");
          body.append( eventType.toString() ).append( " (" );
          body.append( fireParameterstoString( newMethodParams ) );
          body.append(");\n");
        }
        body.append( TAB + "synchronized (this) {\n" + TABx2 + "list = (java.util.ArrayList)" );
        body.append( fieldName ).append( ".clone ();\n" + TAB +"}\n" ); 
        body.append( TAB + "for (int i = 0; i < list.size (); i++) {\n" );
        body.append( TABx2 + "((" ).append( type.toString() );
        body.append( ")list.get (i)).").append( method.getName() );
        body.append(" (");
        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( "e" ); 
        }
        else {
          body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
        }
        body.append( ");\n" + TAB + "}\n" );
      }
      else if ( implementation == 2 ) {
        String fooEvent = "theEvent";
        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( TAB + eventType.toString() ).append( " e = null;\n ");
        }
        body.append( TAB + "Object[] listeners = ").append(listenerList).append(".getListenerList ();\n" );
        body.append( TAB + "for (int i = listeners.length-2; i>=0; i-=2) {\n");
        body.append( TABx2 + "if (listeners[i]==" ).append( type.toString()).append( ".class) {\n" );
        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( TABx3 + "if (e == null)\n" );
          body.append( TABx2 + TABx2 + "e = new ").append( eventType.toString() ).append( " (" );
          body.append( fireParameterstoString( newMethodParams ) );
          body.append( ");\n" );
          }
        body.append( TABx3 + "((").append(type.toString()).append(")listeners[i+1]).").append(method.getName());
        body.append(" (");
        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( "e" ); // the created event
        }
        else {
          body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
        }
        body.append( ");\n" + TABx2 + "}\n" + TAB + "}\n");
      }
      
      newMethod.setBody( body.toString() );
  
      StringBuffer comment = new StringBuffer ( bundle.getString( "COMMENT_FireMethodMC" ) );
      if ( !usesConstructorParameters( eventClass, passEvent ) ) {
          comment.append( "\n@param e The event to be fired\n" ); 
        }
        else {
          comment.append( fireParametersComment( newMethodParams, eventType.getClassName().getName() ) ); 
        }
      newMethod.getJavaDoc().setRawText( comment.toString() );

      classElement.addMethod( newMethod );
    //}
  }

  static void unicastFireMethod( ClassElement classElement, Type type,
                          MethodElement method, int implementation,
                          boolean passEvent )
    throws SourceException {
    
    Identifier methodId = Identifier.create( 
                            "fire" + 
                            Pattern.capitalizeFirstLetter( type.getClassName().getName() ) +
                            Pattern.capitalizeFirstLetter( method.getName().getName() ) );

    MethodElement newMethod = null;

    Type eventType = null;
    MethodParameter params[] = method.getParameters();
    if ( params.length > 0 )
      eventType = params[0].getType();
    else
      eventType = Type.createClass( Identifier.create( "java.util.EventObject" ) );

    ClassElement eventClass = ClassElement.forName( eventType.toString() );

    //addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );

    //if ( addMethod == null ) {
      newMethod = new MethodElement();
      newMethod.setName( methodId );
      newMethod.setReturn( Type.VOID );
      newMethod.setModifiers( Modifier.PRIVATE );
      
      MethodParameter[] newMethodParams = generateFireParameters( eventType, eventClass, passEvent );
      newMethod.setParameters( newMethodParams );

      StringBuffer body = new StringBuffer( 80 );
      body.append( "\n" );
     
      if ( implementation == 1 ) {
         String fieldName = Introspector.decapitalize( type.getClassName().getName() );
         if ( fieldName.equals( type.getClassName().getName() ) ) {
          fieldName = new String( "listener" + fieldName  );
         }

        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( TAB + eventType.toString() ).append( " e = new ");
          body.append( eventType.toString() ).append( " (" );
          body.append( fireParameterstoString( newMethodParams ) );
          body.append(");\n");
        }
        
        body.append( TAB + fieldName ).append( "." ).append( method.getName() );
        body.append(" (");
        if ( usesConstructorParameters( eventClass, passEvent ) ) {
          body.append( "e" ); 
        }
        else {
          body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
        }
        body.append( ");\n" );
      }
      
      newMethod.setBody( body.toString() );

      StringBuffer comment = new StringBuffer ( bundle.getString( "COMMENT_FireMethodUC" ) );
      if ( !usesConstructorParameters( eventClass, passEvent ) ) {
          comment.append( "\n@param e The event to be fired\n" ); 
      }
      else {
        comment.append( fireParametersComment( newMethodParams, eventType.getClassName().getName() ) ); // the event parameter
      }
      newMethod.getJavaDoc().setRawText( comment.toString() );

      classElement.addMethod( newMethod );
    //}
  }

  
  
  static boolean usesConstructorParameters( ClassElement eventClass, boolean passEvent ) {
    if ( passEvent || eventClass == null || eventClass.getConstructors().length > 1 )
      return false;
    else 
      return true;
  }


  static MethodParameter[] generateFireParameters( Type eventType, ClassElement eventClass, boolean passEvent ) {
   
    if ( !usesConstructorParameters( eventClass, passEvent ) ) {
      return new MethodParameter[] 
        { new MethodParameter( "event", eventType, false ) }; 
    }
    else {
      ConstructorElement constructor = eventClass.getConstructors()[0];
      MethodParameter[] params = constructor.getParameters();
      MethodParameter[] result = new MethodParameter[ params.length ];
      for ( int i = 0; i < params.length; i++ ) {
        result[i] = new MethodParameter( "param" + (i + 1), params[i].getType(), false  );
      }
      return result;
    }

  }

  static String fireParameterstoString( MethodParameter[]  params ) {

    StringBuffer buffer = new StringBuffer( 60 );

    for( int i = 0; i < params.length; i++ ) {
      buffer.append( params[i].getName() );
      if ( i < params.length -1 )
        buffer.append( ", " );
    }
    return buffer.toString();
  }

  static String fireParametersComment( MethodParameter[]  params, String evntType ) {

    StringBuffer buffer = new StringBuffer( 60 );

    for( int i = 0; i < params.length; i++ ) {
      buffer.append( "\n@param ").append( params[i].getName() );
      buffer.append( " Parameter #" ).append( i + 1 ).append( " of the <CODE>" );
      buffer.append( evntType ).append( "<CODE> constructor." ); 
    }
    buffer.append( "\n" );

    return buffer.toString();
  }
  
  // UTILITY METHODS ----------------------------------------------------------
  
  /** For primitive {@link org.openide.src.Type type} finds class for wrapping it into object.
   * E.g. <CODE>Type.BOOLEAN -> Boolean</CODE>
   * @param type Primitive type.
   * @return Class which wraps the primitive type.
   */
  private static String getWrapperClassName(Type type) {
    if ( type.isClass() )
    return type.getClassName().getName();
    else if ( type == Type.BOOLEAN )
    return "Boolean";
    else if ( type == Type.BYTE )
    return "Byte";
    else if ( type == Type.DOUBLE )
    return "Double";
    else if ( type == Type.FLOAT )
    return "Float";
    else if ( type == Type.CHAR )
    return "Character";
    else if ( type == Type.INT )
    return "Integer";
    else if ( type == Type.LONG )
    return "Long";
    else if ( type == Type.SHORT )
    return "Short";
    else
    return "Object";
}

}
/* 
 * Log
 *  6    Gandalf   1.5         11/10/99 Petr Hrebejk    Generation of new 
 *       EventListenerList added to MultiCast event sources
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/6/99  Petr Hrebejk    Formating fix
 *  3    Gandalf   1.2         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  2    Gandalf   1.1         7/26/99  Petr Hrebejk    BeanInfo fix & Code 
 *       generation fix
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
java.awt.event.WindowListener */ 
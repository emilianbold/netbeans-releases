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
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 import java.lang.reflect.Modifier;

 import org.openide.src.ClassElement;
 import org.openide.src.MethodElement;
 import org.openide.src.MethodParameter;
 import org.openide.src.Type;
 import org.openide.src.Identifier;
 import org.openide.src.SourceException;
 import org.openide.nodes.Node;
 import org.openide.TopManager;
 import org.openide.NotifyDescriptor;
 import org.openide.util.Utilities;
 import org.openide.util.NbBundle;
  
/** EventSetPattern: This class holds the information about used event set pattern 
 * in code.
 * @author Petr Hrebejk
 *
 * 
 *  PENDING: Add Pattern class hierarchy (abstract classes || interfaces )
 */
public class EventSetPattern extends Pattern {

  private static final ResourceBundle bundle = NbBundle.getBundle( EventSetPattern.class );

  protected MethodElement addListenerMethod = null;
  protected MethodElement removeListenerMethod = null;
 
  private Type type;
  private boolean isUnicast = false;
  private ClassElement typeElement; 

  /** holds the decapitalized name */
  protected String name;

  /** Creates new PropertyPattern one of the methods may be null */
  public EventSetPattern( PatternAnalyser patternAnalyser, 
                          MethodElement addListenerMethod, MethodElement removeListenerMethod ) 
    throws IntrospectionException {
    super( patternAnalyser ); 

    if ( addListenerMethod == null || removeListenerMethod == null  )
      throw new InternalError();
    
    this.addListenerMethod = addListenerMethod;
    this.removeListenerMethod = removeListenerMethod;
 
    isUnicast = testUnicast();
    findEventSetType();
    name = findEventSetName();

    typeElement = ClassElement.forName( type.getClassName().getFullName() ) ;
    
        
  }

  private EventSetPattern( PatternAnalyser patternAnalyser ) {
    super( patternAnalyser );
  }

  static EventSetPattern create( PatternAnalyser patternAnalyser, 
                                 String name, String type, boolean isUnicast ) throws SourceException {

    EventSetPattern esp = new EventSetPattern( patternAnalyser );
    
    esp.name = name;
    esp.type = Type.parse( type );
    esp.isUnicast = isUnicast;

    esp.generateAddListenerMethod();
    esp.generateRemoveListenerMethod();
    
    return esp;
  }

  /** Creates new pattern from result of dialog */

  static EventSetPattern create( PatternAnalyser patternAnalyser, 
                                 String type, 
                                 int implementation,
                                 boolean fire,
                                 boolean passEvent,
                                 boolean isUnicast ) throws SourceException {

    EventSetPattern esp = new EventSetPattern( patternAnalyser );
    
    esp.type = Type.parse( type );

    if ( esp.type == null || !esp.type.isClass() ) {
      return null;
    }

    esp.name = Introspector.decapitalize( esp.type.getClassName().getName() );
    esp.isUnicast = isUnicast;

    String listenerList = null;
   
    if ( implementation == 1 ) {
      if ( isUnicast )
        BeanPatternGenerator.unicastListenerField( esp.getDeclaringClass(), esp.type );
      else
        BeanPatternGenerator.listenersArrayListField( esp.getDeclaringClass(), esp.type );
    }
    else if ( implementation == 2 && !isUnicast ) {
      listenerList = BeanPatternGenerator.eventListenerListField( esp.getDeclaringClass(), esp.type );
    }
    

    if ( isUnicast ) {
      esp.generateAddListenerMethod( BeanPatternGenerator.ucAddBody( esp.type, implementation ), true );
      esp.generateRemoveListenerMethod( BeanPatternGenerator.ucRemoveBody( esp.type, implementation ), true );
    }
    else {
      esp.generateAddListenerMethod( BeanPatternGenerator.mcAddBody( esp.type, implementation, listenerList ), true );
      esp.generateRemoveListenerMethod( BeanPatternGenerator.mcRemoveBody( esp.type, implementation, listenerList ), true );
    }
    
    if ( fire ) {
      ClassElement listener = ClassElement.forName( type.toString() );

      System.out.println ( "THE LISTENER " + listener );

      if ( listener != null ) {
        MethodElement methods[] = listener.getMethods();
        
        for( int i = 0; i < methods.length; i++ ) {
          if ( (methods[i].getModifiers() & Modifier.PUBLIC) != 0 ) {
            if ( isUnicast )
              BeanPatternGenerator.unicastFireMethod( esp.getDeclaringClass(), esp.type,
                                               methods[i], implementation, passEvent );
            else
              BeanPatternGenerator.fireMethod( esp.getDeclaringClass(), esp.type,
                                               methods[i], implementation, listenerList, passEvent );
          }
        }
        
      }
    }


    return esp;
  }


  public ClassElement getTypeElement() {
    return typeElement;
  }

  /** Gets the name of PropertyPattern */
  public String getName() {
    return name;
  }

  /** Sets the name of PropertyPattern */
  public void setName( String name ) throws SourceException {
    
    if ( !Utilities.isJavaIdentifier( name ) || name.indexOf( "Listener" ) <= 0 )
      throw new SourceException( "Invalid event source name" );

    name = capitalizeFirstLetter( name );

    Identifier addMethodID = Identifier.create( "add" + name ); //+ "Listener" );
    Identifier removeMethodID = Identifier.create( "remove" + name ); //+ "Listener" );

    addListenerMethod.setName( addMethodID );
    removeListenerMethod.setName( removeMethodID );

  }

  /** Tests if the pattern is public i.e. all needed parts are public */
  public boolean isPublic() {
    return  ( addListenerMethod == null || addListenerMethod.getModifiers() == Modifier.PUBLIC ) &&
            ( removeListenerMethod == null || removeListenerMethod.getModifiers() == Modifier.PUBLIC );
  }

  /** Test if the name is valid for given pattern */
  protected static boolean isValidName( String str ) {
    if ( Utilities.isJavaIdentifier(str) == false )
      return false;

    if (str.indexOf( "Listener" ) <= 0 ) 
      return false;

    return true;
  }

  /** Returns the mode of the property READ_WRITE, READ_ONLY or WRITE_ONLY */
  public boolean isUnicast() {
    return isUnicast;
  }

  /** Sets the property to be unicast or multicast */
  public void setIsUnicast( boolean b ) throws SourceException {
    if ( b != isUnicast) {
      Identifier tooMany = Identifier.create( "java.util.TooManyListenersException" );
      Identifier[] exs = addListenerMethod.getExceptions();

      if (b) {
        Identifier[] nexs = new Identifier[exs.length + 1];
        System.arraycopy( exs, 0, nexs, 0, exs.length );
        nexs[ exs.length ] = tooMany;
        addListenerMethod.setExceptions( nexs );
      }
      else {
        Identifier[] nexs = new Identifier[exs.length -1];
        int found = 0;
        for( int i = 0; i < exs.length; i++ ) {
          //System.out.println ( exs[i] + " == " + tooMany + " : " + exs[i].compareTo( tooMany, false ) + " : " + i );
          if ( !exs[i].compareTo( tooMany, false ) )
            nexs[i-found] = exs[i];
          else
            found = 1;
        }
        addListenerMethod.setExceptions( nexs );
      }
    }
  }

  /** Returns the getter method */
  public MethodElement getAddListenerMethod() {
    return addListenerMethod;
  }

  /** Returns the setter method */
  public MethodElement getRemoveListenerMethod() {
    return removeListenerMethod;
  }

  /** Gets the type of property */
  public Type getType() {
    return type;
  }

  /** Sets the type of property */
  public void setType( Type newType ) throws SourceException {
    
    System.out.println ( newType.getClassName().getFullName() );
    System.out.println ( ClassElement.forName( newType.getClassName().getFullName() ) );

    if ( newType.compareTo(type, true))
      return;

    // PENDING :: Search the source
    
    try {
      //ClassElement.forName( newType.get



      if (!java.util.EventListener.class.isAssignableFrom( newType.toClass() ) ) {
        TopManager.getDefault().notify(
          new NotifyDescriptor.Message(PatternNode.bundle.getString("MSG_InvalidListenerInterface"),
                                       NotifyDescriptor.ERROR_MESSAGE) );
        return;
        }
    }
    catch ( java.lang.ClassNotFoundException ex ) {
      TopManager.getDefault().notify(
          new NotifyDescriptor.Message(PatternNode.bundle.getString("MSG_ListenerInterfaceNotFound"),
                                       NotifyDescriptor.ERROR_MESSAGE) );
          
      return;
    }
        
    MethodParameter[] params = addListenerMethod.getParameters();
    if ( params.length > 0 ) {
      params[0].setType( newType );
      addListenerMethod.setParameters( params );
    }

    params = removeListenerMethod.getParameters();
    if ( params.length > 0 ) {
      params[0].setType( newType );
      removeListenerMethod.setParameters( params );
    }

    // Ask if we have to change the bame of the methods
    String mssg = MessageFormat.format( PatternNode.bundle.getString( "FMT_ChangeEventSourceName" ), 
                                           new Object[] { capitalizeFirstLetter( newType.getClassName().getName() ) } );
                                           //new Object[] { "Blah Blah !" } );
    NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
    if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
      setName( newType.getClassName().getName() );
    }
  }

  /** Gets the cookie of the first available method */

  public Node.Cookie getCookie( Class cookieType ) {
    if ( addListenerMethod != null )
      return addListenerMethod.getCookie( cookieType );
    
    if ( removeListenerMethod != null )
      return removeListenerMethod.getCookie( cookieType );
      
    return null;
  }

  public void destroy() throws SourceException {
    ClassElement declaringClass;

    // Remove addListener method

    if ( addListenerMethod != null ) {
      declaringClass = addListenerMethod.getDeclaringClass();
      if ( declaringClass == null ) {
        throw new SourceException();
      }
      else {   
        declaringClass.removeMethod( addListenerMethod );
      }
    }

    // Remove removeListener method

    if ( removeListenerMethod != null ) {
      declaringClass = removeListenerMethod.getDeclaringClass();

      if ( declaringClass == null ) {
        throw new SourceException();
      }
      else {   
        declaringClass.removeMethod( removeListenerMethod );
      }
    }
  }

  // Utility methods --------------------------------------------------------------------

      /*
     * Package-private constructor
     * Merge two event set descriptors.  Where they conflict, give the
     * second argument (y) priority over the first argument (x).
     *
     * @param x  The first (lower priority) EventSetDescriptor
     * @param y  The second (higher priority) EventSetDescriptor
     */

    EventSetPattern( EventSetPattern x, EventSetPattern y) {
      super( y.patternAnalyser );
      //super(x,y);

      /*
	    listenerMethodDescriptors = x.listenerMethodDescriptors;
	    if (y.listenerMethodDescriptors != null) {
	      listenerMethodDescriptors = y.listenerMethodDescriptors;
	    }
	    if (listenerMethodDescriptors == null) {
	      listenerMethods = y.listenerMethods;
	    }
      */
	    addListenerMethod = y.addListenerMethod;
	    removeListenerMethod = y.removeListenerMethod;
	    isUnicast = y.isUnicast;
	    type = y.type;

      /*
	    if (!x.inDefaultEventSet || !y.inDefaultEventSet) {
	      inDefaultEventSet = false;
	    }
      */
    }


  /** Finds the Type of property.
   * @throws IntrospectionException if the property doesnt folow the design patterns
   */

  private void findEventSetType() throws IntrospectionException {
    if ( addListenerMethod == null )
      throw new InternalError( "add method == nul in event set pattern");
    
    type = addListenerMethod.getParameters()[0].getType();
  }

  /** Decides about the name of the event set from names of the methods */
  
  private String findEventSetName() {
    
    String compound = addListenerMethod.getName().getName().substring(3); 
    return name = Introspector.decapitalize( compound ); 
  }
 

  /** Test if this EventSet pattern is unicast */
  private boolean testUnicast() {
    if (findTooManyListenersException() != null)
      return true;
    else
      return false;
  }

  /** @return The identifier for java.util.TooManyListenersException if the addListener
   * method throws it or null if not. 
   */

  Identifier findTooManyListenersException() {
    Identifier tooMany = Identifier.create( "java.util.TooManyListenersException" );

    Identifier[] exs = addListenerMethod.getExceptions();
      

    for ( int i = 0; i < exs.length; i++ ) {
      if ( exs[i].compareTo( tooMany, false ) ) {
        return exs[i];
      }
    }
    return null;
  }

  void generateAddListenerMethod () throws SourceException {
    generateAddListenerMethod( null, false );
  }

  void generateAddListenerMethod ( String body, boolean javadoc ) throws SourceException {
    ClassElement declaringClass = getDeclaringClass();
    MethodElement newMethod = new MethodElement();

    MethodParameter[] newParameters = { new MethodParameter( "listener", type, false ) };
    
    newMethod.setName( Identifier.create( "add" + capitalizeFirstLetter( getName() ) ) );
    newMethod.setReturn( Type.VOID );
    newMethod.setModifiers( Modifier.PUBLIC );
    newMethod.setParameters( newParameters );
    if ( body != null )
      newMethod.setBody( body );
    if ( isUnicast )
      newMethod.setExceptions( new Identifier[] { Identifier.create( "java.util.TooManyListenersException" ) } );
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_AddListenerMethod" ),
                                             new Object[] { type.getClassName().getName() } );
      newMethod.getJavaDoc().setRawText( comment );  
    }

    if ( declaringClass == null )
      throw new SourceException();
    else {
      declaringClass.addMethod( newMethod );
      addListenerMethod = newMethod;
      }
  }

  void generateRemoveListenerMethod() throws SourceException {
    generateRemoveListenerMethod( null, false );
  }

  void generateRemoveListenerMethod( String body, boolean javadoc ) throws SourceException {
    ClassElement declaringClass = getDeclaringClass();
    MethodElement newMethod = new MethodElement();

    MethodParameter[] newParameters = { new MethodParameter( "listener", type, false ) };
    
    newMethod.setName( Identifier.create( "remove" + capitalizeFirstLetter( getName() ) ) );
    newMethod.setReturn( Type.VOID );
    newMethod.setModifiers( Modifier.PUBLIC );
    newMethod.setParameters( newParameters );
    if ( body != null )
      newMethod.setBody( body );
    if ( javadoc ) {
      String comment = MessageFormat.format( bundle.getString( "COMMENT_RemoveListenerMethod" ),
                                             new Object[] { type.getClassName().getName() } );
      newMethod.getJavaDoc().setRawText( comment );  
    }

    if ( declaringClass == null )
      throw new SourceException();
    else {
      declaringClass.addMethod( newMethod );
      removeListenerMethod = newMethod;
      }
  }

}

/* 
 * Log
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
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

package com.netbeans.developer.modules.beans.beaninfo;

import java.lang.reflect.Modifier;

import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.TopManager;

/** Singleton - utility class


 @author Petr Hrebejk
*/
class BiSuperClass extends Object {


  /** Creates a ClassElement containing all methods from classElement and it's superclasses */

  static ClassElement createForClassElement( ClassElement classElement ) {
    ClassElement result = new ClassElement();

    try {
      result.setName( classElement.getName() );
    }
    catch ( org.openide.src.SourceException e ) {
      TopManager.getDefault().notifyException( e );
    }

    ClassElement ce = classElement;
    int methodsAdded = 0;           // Workaround for getMethd


    while ( ce != null ) {
      MethodElement[] methods = ce.getMethods();

      for( int i = 0; i < methods.length; i++ )  {
    
        if ( ( methods[i].getModifiers() & Modifier.PUBLIC ) == 0 ) 
          continue;

        if ( methodsAdded == 0 || result.getMethod( methods[i].getName(), getParameterTypes( methods[i] ) ) == null ) {
          try {
            result.addMethod( methods[i] );
            methodsAdded ++;
          }
          catch ( org.openide.src.SourceException e ) {
            TopManager.getDefault().notifyException( e );
          }
        }
      }
     
      ce = ce.getSuperclass() == null ? null : ClassElement.forName( ce.getSuperclass().getFullName() );      
    }

    /*
    MethodElement[] methods = result.getMethods();
    for( int i = 0; i < methods.length; i++ ) 
      System.out.println ( methods[i].getName() );
    */
    return result;
  }

  /** Returns array of parameter types  */
  static Type[] getParameterTypes( MethodElement method ) {
    MethodParameter[] params = method.getParameters();
    
    Type[] result = new Type[ params.length ];

    for( int i = 0; i < params.length; i++ ) {
      result[i] = params[i].getType();
    }

    return result;
  }

}

/* 
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/29/99  Petr Hrebejk    Patterns in BeanInfo 
 *       show correctly only public fields and methods
 *  3    Gandalf   1.2         7/28/99  Petr Hrebejk    Property Mode change fix
 *  2    Gandalf   1.1         7/26/99  Petr Hrebejk    BeanInfo fix & Code 
 *       generation fix
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $ 
 */ 
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

package org.netbeans.modules.javadoc.search;

import java.net.URL;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.src.*;


/** This class finds the source to show it instead of documentation.
 *
 * @author  Administrator
 * @version 
 */
public class SrcFinder extends Object {

  private static final ElementFormat FORMATOR = new ElementFormat( "{C}" ); // NOI18N
  
  /** SrcFinder is a singleton */
  private  SrcFinder() {
  }
  
  static Element findSource( String aPackage, URL url  ) {
    
    String thePackage = null;
    String member = url.getRef();
    String clazz = url.getFile().replace( '/', '.' );
    String filename = null;
    Type[] params;
    
    int pIndex;
    if ( ( pIndex = clazz.toLowerCase().indexOf( aPackage.trim().toLowerCase() ) ) != -1 ) {
      thePackage = clazz.substring(pIndex, pIndex + aPackage.trim().length()  - 1 );
      clazz = clazz.substring( pIndex + aPackage.trim().length(), clazz.length() - 5 );
      
      int ei;
      if ( ( ei = clazz.indexOf('.')) != -1 ) {
        filename = clazz.substring(0, ei );
      }
      else
        filename = clazz;
      
    }
    
    
    // System.out.println("MEMBER  :" + member ); // NOI18N
    // System.out.println("CLASS   :" + clazz ); // NOI18N
    // System.out.println("PACKAGE :" + thePackage ); // NOI18N
    // System.out.println("FILENAME:" + filename ); // NOI18N
    
    Repository repository = TopManager.getDefault().getRepository();
     
    FileObject fo = repository.find( thePackage, filename, "java" ); // NOI18N
    
    if ( fo != null ) {
      try {
        DataObject dobj = DataObject.find( fo );

        SourceCookie sc = (SourceCookie)dobj.getCookie( SourceCookie.class );
        ClassElement classes[] = sc.getSource().getAllClasses();
        
        // Search all classes
        for ( int i = 0; i < classes.length; i++ ) {
          String outerName = FORMATOR.format( classes[i] );
          if ( clazz.equals( outerName ) ) {
            if ( member == null ) 
              // We are looking for class
              return classes[i];
            else {
              
              int pi = member.indexOf( '(' );
              if ( pi == -1 ) {
                // we are looking for fiels
                FieldElement fe = classes[i].getField( Identifier.create( member ) );
                return fe;
              }
              else {
                // We are looking for method or constructor
                return getMethod( classes[i], member );
              }
                
            }
          }
        }
       
        
        return null;
      }
      catch ( org.openide.loaders.DataObjectNotFoundException e ) {
        System.out.println( e );
      }
    }
  return null;
  }
   
  /** Gets the method we are looking for
   */
  private static ConstructorElement getMethod( ClassElement ce, String member) {
    
    
    int pi = member.indexOf( '(' );
    String name = member.substring( 0, pi );
    
    StringTokenizer tokenizer = new StringTokenizer( member.substring( pi ), " ,()" ); // NOI18N
    ArrayList paramList = new ArrayList();
    
    while( tokenizer.hasMoreTokens() ) {
      paramList.add( Type.parse( tokenizer.nextToken() ) );
    }
    
    Type[] params = new Type[ paramList.size() ];
    paramList.toArray( params );
    
    ConstructorElement result = ce.getConstructor( params );
    
    if ( result == null ) 
      result = ce.getMethod( Identifier.create( name ), params );
    
    
    /*
    if ( result == null ) {
      // We didn't found the method yet let's try other way
      
      findInArray( ce.getConstructors() );
      findInArray( ce.getMethods() );
      
    }
    */
    return result;
  }
  
  
  /*
  private static void findInArray( ConstructorElement[] cs ) {
    
    for ( int i = 0; i < cs.length; i++ ) {
      
      MethodParameter[] ps = cs[i].getParameters();
      
      for( int j = 0; j < ps.length; j++ ) {
        //System.out.println( ps[j].getType().toString() );
      }
      
    }
    
  }
  */
  
}
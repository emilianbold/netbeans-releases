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

import java.beans.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.TopManager;

import com.netbeans.developer.modules.beans.PatternAnalyser;
import com.netbeans.developer.modules.loaders.java.JavaEditor;

/** 
 * Finds or creates BeanInfo source elemnet for the class.
 * It can regenerate the source if there are the guarded blocks.
 * @author  Petr Hrebejk
 */

public class BeanInfoSource extends Object {
  
  private static final String BEANINFO_NAME_EXT = "BeanInfo";
  
  private static final String PROPERTIES_SECTION = "Properties";
  private static final String EVENTSETS_SECTION = "Events";
  private static final String ICONS_SECTION = "Icons";
  private static final String IDX_SECTION = "Idx";
  
  private ClassElement classElement;
  
  private DataObject   biDataObject = null;
  private JavaEditor   javaEditor =  null;  
  private PatternAnalyser pa = null;
  
  /** Creates new BeanInfoSource */
  public BeanInfoSource (PatternAnalyser pa ) {
    this.classElement = pa.getClassElement();
    this.pa = pa;

    findBeanInfo();
  }

  /** Returns wether the bean info exists or not */
  boolean exists() {
    return biDataObject != null;
  }

  /** Checks wether the bean info object has Guarded sections i.e.
   * was created from netbeans template.
   */
  boolean isNbBeanInfo() {

    if ( !exists() || javaEditor == null ) 
      return false;
    
    JavaEditor.InteriorSection pis = javaEditor.findInteriorSection( PROPERTIES_SECTION );
    JavaEditor.InteriorSection eis = javaEditor.findInteriorSection( EVENTSETS_SECTION );
    JavaEditor.SimpleSection iss = javaEditor.findSimpleSection( ICONS_SECTION );
    JavaEditor.SimpleSection dss = javaEditor.findSimpleSection( IDX_SECTION );
    
    return ( pis != null && eis != null && iss != null && dss != null );
  }


  /** Finds the bean info for classElement asspciated with this
      object */
  void findBeanInfo() {
    
     javaEditor = null; 
    
     DataObject dataObject = (DataObject)classElement.getCookie( DataObject.class );
     if ( dataObject == null )
       return;
     
     FileObject folder = dataObject.getFolder().getPrimaryFile();
     if ( folder == null )
       return;
     
     FileObject biFile = folder.getFileObject( dataObject.getName() + BEANINFO_NAME_EXT, "java" );
     if ( biFile == null )
       return;
     
     try {
      biDataObject = DataObject.find( biFile );
      javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );
      //System.out.println("ClassElem : " + biDataObject );
     }
     catch ( org.openide.loaders.DataObjectNotFoundException e ) {
        // Do nothing if no data object is found
     }
            
  }   
          
  /** Creates beanInfo data object */
  void createFromTemplate() {

    DataFolder dfTemplates = TopManager.getDefault().getPlaces().folders().templates();
    if ( dfTemplates == null )
      return;

    FileObject foTemplates = dfTemplates.getPrimaryFile() ;
    if ( foTemplates == null )
      return;

    FileObject foClassTemplates = foTemplates.getFileObject( "Beans" );
    if ( foClassTemplates == null )
      return;
    
    FileObject foBiTemplate = foClassTemplates.getFileObject( "BeanInfo", "java" );
    if ( foBiTemplate == null )
      return;



    try {
      DataObject doBiTemplate = DataObject.find ( foBiTemplate );

      DataObject dataObject = (DataObject)classElement.getCookie( DataObject.class );

      System.out.println ( "DataObject" + dataObject );

      dataObject = (DataObject)classElement.getCookie( com.netbeans.developer.modules.loaders.java.JavaDataObject.class );

      System.out.println ( "JavaDataObject" + dataObject );



      if ( dataObject == null )
        return;
     
      DataFolder folder = dataObject.getFolder();

      biDataObject = doBiTemplate.createFromTemplate( folder, dataObject.getName() + BEANINFO_NAME_EXT );
      javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );      
    }
    catch ( org.openide.loaders.DataObjectNotFoundException e ) {
      System.out.println ( e );
      // Do nothing if no data object is found
    }
    catch ( java.io.IOException e ) {
      System.out.println ( e );
      // Do nothing if no data object is found
    }
  }
                
  /** If the bean info is available returns the bean info data object */    
  DataObject getDataObject() {
    return biDataObject;
  }
 
  /** opens the source */
  void open() {
    javaEditor.open();
  }

  /** Sets the header and bottom of properties section */
  void setPropertiesSection( String header, String bottom ) {
    JavaEditor.InteriorSection is = javaEditor.findInteriorSection( PROPERTIES_SECTION );

    if ( is != null ) {
      is.setHeader( header );  
      is.setBottom( bottom );
      }
  }

  /** Gets the header of properties setion */
  String getPropertiesSection() {
    JavaEditor.InteriorSection is = javaEditor.findInteriorSection( PROPERTIES_SECTION );

    if ( is != null ) {
      return is.toString();
      }
    else
      return null;
    
  }

  /** Sets the header and bottom of event sets section */
  void setEventSetsSection( String header, String bottom ) {
    JavaEditor.InteriorSection is = javaEditor.findInteriorSection( EVENTSETS_SECTION );
    if ( is != null ) {
      is.setHeader( header );
      is.setBottom( bottom );
    }
  }

  /** Gets the header of properties setion */
  String getEventSetsSection() {
    JavaEditor.InteriorSection is = javaEditor.findInteriorSection( EVENTSETS_SECTION );

    if ( is != null ) {
      return is.toString();
      }
    else
      return null; 
  }
  
  /** Gets the header of properties setion */
  String getIconsSection() {
    JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( ICONS_SECTION );

    if ( ss != null ) {
      return ss.toString();
      }
    else
      return null; 
  }

  /** Sets the header of properties setion */
  void setIconsSection( String text ) {
    JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( ICONS_SECTION );
    if ( ss != null ) 
      ss.setText( text );
  }  
  
  /** Gets the header of properties setion */
  String getDefaultIdxSection() {
    JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( IDX_SECTION );

    if ( ss != null ) {
      return ss.toString();
      }
    else
      return null; 
  }

  /** Sets the header of properties setion */
  void setDefaultIdxSection( String text ) {
    JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( IDX_SECTION );
    if ( ss != null ) 
      ss.setText( text );
  }
  
  /*
  void regenerateMethods() {
    JavaEditor.InteriorSection is = javaEditor.findInteriorSection( "Events" );
    
    if ( is != null ) {
      is.setHeader( BeanInfoGenerator.generateMethods( classElement.getName().getName(), methods ) );
      is.setBottom( BeanInfoGenerator.generateMethodsBottom( methods ) );
    }
  }
  */

}

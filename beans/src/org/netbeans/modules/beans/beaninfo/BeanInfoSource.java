/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.beans.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.src.SourceElement;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.TopManager;

import org.netbeans.modules.beans.PatternAnalyser;
import org.netbeans.modules.java.JavaEditor;

/**
 * Finds or creates BeanInfo source elemnet for the class.
 * It can regenerate the source if there are the guarded blocks.
 * @author  Petr Hrebejk
 */

public class BeanInfoSource extends Object {

    private static final String BEANINFO_NAME_EXT = "BeanInfo"; // NOI18N

    private static final String PROPERTIES_SECTION = "Properties"; // NOI18N
    private static final String EVENTSETS_SECTION = "Events"; // NOI18N
    private static final String ICONS_SECTION = "Icons"; // NOI18N
    private static final String IDX_SECTION = "Idx"; // NOI18N
    private static final String METHODS_SECTION = "Methods"; // NOI18N

    private ClassElement classElement;

    private DataObject   biDataObject = null;
    private JavaEditor   javaEditor =  null;
    //private PatternAnalyser pa = null;

    /** Creates new BeanInfoSource */
    public BeanInfoSource (ClassElement classElement ) {
        this.classElement = classElement;
        //this.pa = pa;

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

        if ( !exists() || javaEditor == null ) {
            return false;
        }

        JavaEditor.InteriorSection pis = javaEditor.findInteriorSection( PROPERTIES_SECTION );
        JavaEditor.InteriorSection eis = javaEditor.findInteriorSection( EVENTSETS_SECTION );
        JavaEditor.InteriorSection mis = javaEditor.findInteriorSection( METHODS_SECTION );
        JavaEditor.SimpleSection iss = javaEditor.findSimpleSection( ICONS_SECTION );
        JavaEditor.SimpleSection dss = javaEditor.findSimpleSection( IDX_SECTION );

        return ( pis != null && eis != null && iss != null && dss != null);
    }


    /** Finds the bean info for classElement asspciated with this
        object */
    void findBeanInfo() {

        javaEditor = null;

        SourceElement sc = classElement.getSource();
        if ( sc == null ) {
            return;
        }
        
        DataObject dataObject = (DataObject)sc.getCookie( DataObject.class );
        if ( dataObject == null ) {
            return;
        }

        FileObject folder = dataObject.getFolder().getPrimaryFile();
        if ( folder == null ) {
            return;
        }
        
        FileObject biFile = folder.getFileObject( dataObject.getName() + BEANINFO_NAME_EXT, "java" ); // NOI18N
        if ( biFile == null ) {
            return;
        }
        
        try {
            biDataObject = DataObject.find( biFile );
            javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );
            //System.out.println("ClassElem : " + biDataObject ); // NOI18N
        }
        catch ( org.openide.loaders.DataObjectNotFoundException e ) {
            // Do nothing if no data object is found
        }

    }

    /** Deletes the BeanInfo */
    void delete() throws java.io.IOException {
        biDataObject.delete();
    }


    /** Creates beanInfo data object */
    void createFromTemplate() {

        DataFolder dfTemplates = TopManager.getDefault().getPlaces().folders().templates();
        if ( dfTemplates == null ) {
            return;
        }
        
        FileObject foTemplates = dfTemplates.getPrimaryFile() ;
        if ( foTemplates == null ) {
            return;
        }

        FileObject foClassTemplates = foTemplates.getFileObject( "Beans" ); // NOI18N
        if ( foClassTemplates == null ) {
            return;
        }    
            
        FileObject foBiTemplate = foClassTemplates.getFileObject( "BeanInfo", "java" ); // NOI18N
        if ( foBiTemplate == null ) {
            return;
        }


        try {
            DataObject doBiTemplate = DataObject.find ( foBiTemplate );
 
            SourceElement sc = classElement.getSource();
            if ( sc == null )
                return;
            
            DataObject dataObject = (DataObject)sc.getCookie( DataObject.class );

            if ( dataObject == null ) {
                return;
            }
            
            DataFolder folder = dataObject.getFolder();

            biDataObject = doBiTemplate.createFromTemplate( folder, dataObject.getName() + BEANINFO_NAME_EXT );
            javaEditor = (JavaEditor)biDataObject.getCookie( JavaEditor.class );
        }
        catch ( org.openide.loaders.DataObjectNotFoundException e ) {
            // System.out.println ( e );
            // Do nothing if no data object is found
        }
        catch ( java.io.IOException e ) {
            // System.out.println ( e );
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
            return is.getText();
        }
        else
            return null;

    }

    /** Sets the header and bottom of methods section */
    void setMethodsSection( String header, String bottom ) {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( METHODS_SECTION );

        if ( is != null ) {
            is.setHeader( header );
            is.setBottom( bottom );
        }
    }

    /** Gets the header of properties setion */
    String getMethodsSection() {
        JavaEditor.InteriorSection is = javaEditor.findInteriorSection( METHODS_SECTION );

        if ( is != null ) {
            return is.getText();
        }
        else {
          return null;
        }

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
            return is.getText();
        }
        else
            return null;
    }

    /** Gets the header of properties setion */
    String getIconsSection() {
        JavaEditor.SimpleSection ss = javaEditor.findSimpleSection( ICONS_SECTION );

        if ( ss != null ) {
            return ss.getText();
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
            return ss.getText();
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

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n  
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/28/99  Petr Hrebejk    Property Mode change fix
 *  2    Gandalf   1.1         7/26/99  Petr Hrebejk    BeanInfo fix & Code 
 *       generation fix
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */

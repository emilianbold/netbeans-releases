/**************************************************************************
 *
 *                          Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Code is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2001.
 * All Rights Reserved.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileSystem;
//import org.netbeans.beaninfo.FileSystemBeanInfo;


/**
 *	<p>Provides information about the "HTTP Javadoc Filesystem" bean.</p>
 *
 *	@since 1.0
 */
public class HTTPFileSystemBeanInfo extends SimpleBeanInfo {
    
    // Property descriptors
    private PropertyDescriptor[] propertyDescriptors = null;    
        
    /**
     *	Returns this bean's property descriptors.
     *
     *	@since 1.0
     */
    public PropertyDescriptor[] getPropertyDescriptors() {        
        
        if( propertyDescriptors == null ){
            try {            
                propertyDescriptors = new PropertyDescriptor[ 1 ];
                propertyDescriptors[ 0 ] = new PropertyDescriptor( HTTPFileSystem.PROP_URL, HTTPFileSystem.class, "getURL", "setURL" );	// NOI18N
                propertyDescriptors[ 0 ].setDisplayName( ResourceUtils.getBundledString( "PROP_URLPropertyName" ) ); //NO I18N
                propertyDescriptors[ 0 ].setShortDescription( ResourceUtils.getBundledString( "HINT_URLPropertyName" ) ); //NO I18N
                propertyDescriptors[ 0 ].setBound( true );
                propertyDescriptors[ 0 ].setConstrained( true );
                propertyDescriptors[ 0 ].setPropertyEditorClass( URLPropertyEditor.class );
            }
            catch( IntrospectionException e ) {            
                if ( Boolean.getBoolean( "netbeans.debug.exceptions" ) ) { //NO I18N
                    e.printStackTrace( );                
                    return null;
                }            
            }
        }
        return propertyDescriptors;        
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (FileSystem.class) };
        } catch (IntrospectionException ie) {
            if ( Boolean.getBoolean( "netbeans.debug.exceptions" ) ) { //NO I18N
                ie.printStackTrace( );                
            }            
            return null;
        }
    }

    public Image getIcon (int kind) {
        try {
            return Introspector.getBeanInfo (FileSystem.class).getIcon (kind);
        } catch (IntrospectionException ie) {
            if ( Boolean.getBoolean( "netbeans.debug.exceptions" ) ) { //NO I18N
                ie.printStackTrace( );
            }            
            return null;
        }
    }    
}
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
import org.openide.util.Utilities;


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
                propertyDescriptors[ 0 ].setDisplayName( ResourceUtils.getBundledString( "PROP_URLPropertyName" ) ); //NOI18N
                propertyDescriptors[ 0 ].setShortDescription( ResourceUtils.getBundledString( "HINT_URLPropertyName" ) ); //NOI18N
                propertyDescriptors[ 0 ].setBound( true );
                propertyDescriptors[ 0 ].setConstrained( true );
            }
            catch( IntrospectionException e ) {            
                if ( Boolean.getBoolean( "netbeans.debug.exceptions" ) ) { //NOI18N
                    e.printStackTrace( );                
                    return null;
                }            
            }
        }
        return propertyDescriptors;        
    }

    /**
     *	Returns this bean's superclass' BeanInfos.
     *
     *	@since 1.0
     */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (FileSystem.class) };
        } catch (IntrospectionException ie) {
            if ( Boolean.getBoolean( "netbeans.debug.exceptions" ) ) { //NOI18N
                ie.printStackTrace( );                
            }            
            return null;
        }
    }

    
    /**
     *  Returns an icon image for this bean.
     *
     *  @since 1.0
     */
    public Image getIcon (int kind) {

        switch( kind ) {
            
            case ICON_COLOR_16x16:
            default:
                return Utilities.loadImage( "/org/netbeans/modules/javadoc/httpfs/resources/BeanIcon16C.gif" );    // NOI18N
            case ICON_COLOR_32x32:
                return Utilities.loadImage( "/org/netbeans/modules/javadoc/httpfs/resources/BeanIcon32C.gif" );    // NOI18N
            case ICON_MONO_16x16:
                return Utilities.loadImage( "/org/netbeans/modules/javadoc/httpfs/resources/BeanIcon16M.gif" );    // NOI18N
            case ICON_MONO_32x32:
                return Utilities.loadImage( "/org/netbeans/modules/javadoc/httpfs/resources/BeanIcon32M.gif" );    // NOI18N
        }
    }

}

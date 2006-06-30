/**************************************************************************
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Software is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2005.
 * All Rights Reserved.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.beans.*;
import java.awt.Image;

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

                propertyDescriptors = new PropertyDescriptor[ 3 ];

                // URL property
                propertyDescriptors[ 0 ] = new PropertyDescriptor( HTTPFileSystem.PROP_URL, HTTPFileSystem.class, "getURL", "setURL" );	// NOI18N
                propertyDescriptors[ 0 ].setDisplayName( NbBundle.getMessage(HTTPFileSystemBeanInfo.class, "PROP_URLPropertyName" ) ); //NOI18N
                propertyDescriptors[ 0 ].setShortDescription( NbBundle.getMessage(HTTPFileSystemBeanInfo.class, "HINT_URLPropertyName" ) ); //NOI18N
                propertyDescriptors[ 0 ].setBound( true );
                propertyDescriptors[ 0 ].setConstrained( true );
                propertyDescriptors[ 0 ].setPreferred( true );

                // RefreshRate property
                propertyDescriptors[ 1 ] = new PropertyDescriptor( HTTPFileSystem.PROP_REFRESH_RATE, HTTPFileSystem.class, "getRefreshRate", "setRefreshRate" );	// NOI18N
                propertyDescriptors[ 1 ].setDisplayName( NbBundle.getMessage(HTTPFileSystemBeanInfo.class, "PROP_RefreshRatePropertyName" ) ); //NOI18N
                propertyDescriptors[ 1 ].setShortDescription( NbBundle.getMessage(HTTPFileSystemBeanInfo.class, "HINT_RefreshRatePropertyName" ) ); //NOI18N
                propertyDescriptors[ 1 ].setBound( true );
                propertyDescriptors[ 1 ].setConstrained( true );
                propertyDescriptors[ 1 ].setExpert( true );

                // State property
                propertyDescriptors[ 2 ] = new PropertyDescriptor( HTTPFileSystem.PROP_STATE, HTTPFileSystem.class, "getState", null );	// NOI18N
                propertyDescriptors[ 2 ].setBound( true );
                propertyDescriptors[ 2 ].setConstrained( false );
                propertyDescriptors[ 2 ].setHidden( true );

            }
            catch( IntrospectionException e ) {            
                org.openide.ErrorManager.getDefault().notify(e);
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
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    
    /**
     *  Returns an icon image for this bean.
     *  @since 1.0
     */
    public Image getIcon (int kind) {
        return Utilities.loadImage( "org/netbeans/modules/javadoc/httpfs/resources/BeanIcon16C.gif" );    // NOI18N
    }

}

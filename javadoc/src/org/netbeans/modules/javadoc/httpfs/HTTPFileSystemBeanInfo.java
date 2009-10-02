/**************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Software is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2005.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.beans.*;
import java.awt.Image;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileSystem;
import org.openide.util.ImageUtilities;


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
        return ImageUtilities.loadImage( "org/netbeans/modules/javadoc/httpfs/resources/BeanIcon16C.gif" );    // NOI18N
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xslt.core;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import org.openide.ErrorManager;
import org.openide.loaders.DataLoader;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Loader BeanInfo adding metadata missing in org.openide.loaders.MultiFileLoaderBeanInfo.
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XSLTDataLoaderBeanInfo extends SimpleBeanInfo {

    private static final String LOADER_DESC = "LBL_loader_desc";    // NOI18N
    public static final String PATH_TO_IMAGE = 
        "org/netbeans/modules/xslt/core/resources/xslt_file.gif";   // NOI18N

    /** {@inheritDoc} */
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {Introspector.getBeanInfo(DataLoader.class)};
        } catch (IntrospectionException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }

    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return ImageUtilities.loadImage(PATH_TO_IMAGE);
        } else {
            return null;
        }

    }
    
    /** {@inheritDoc} */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = 
            new BeanDescriptor ( XSLTDataLoader.class , null );
        beanDescriptor.setDisplayName ( NbBundle.getMessage( XSLTDataLoaderBeanInfo.class, 
                XSLTDataLoader.LOADER_NAME) );
        beanDescriptor.setShortDescription ( NbBundle.getMessage(XSLTDataLoaderBeanInfo.class, 
                LOADER_DESC) );
        
        return beanDescriptor;
    }
    
    /** {@inheritDoc} */
    public PropertyDescriptor[] getPropertyDescriptors() {
        // Make extensions into a r/o property.
        // It will only contain the XSLT MIME type.
        // Customizations should be done on the resolver object, not on the extension list.
        // Does not work to just use additional bean info from UniFileLoader and return one extensions
        // property with no setter--Introspector cleverly (!&#$@&) keeps your display name
        // and everything and adds back in the setter from the superclass.
        // So bypass UniFileLoader in the beaninfo search.
        try {
            PropertyDescriptor extensions = new PropertyDescriptor(
                "extensions", XSLTDataLoader.class, "getExtensions", null);// NOI18N
            extensions.setDisplayName(
                NbBundle.getMessage(XSLTDataLoader.class, "PROP_extensions"));
            extensions.setShortDescription(
                NbBundle.getMessage(XSLTDataLoader.class, "HINT_extensions"));
            extensions.setExpert(true);
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /** {@inheritDoc} */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
    /** {@inheritDoc} */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }
}

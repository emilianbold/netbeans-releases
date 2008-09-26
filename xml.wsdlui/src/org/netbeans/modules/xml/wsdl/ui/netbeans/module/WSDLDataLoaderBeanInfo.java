/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/** Description of {@link WSDLDataLoader}.
 *
 * @author Jerry Waldorf
 */
public class WSDLDataLoaderBeanInfo extends SimpleBeanInfo {



	/**
	 * copied from Ant Module
	 */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        // Make extensions into a r/o property.
        // It will only contain the WSDL MIME type.
        // Customizations should be done on the resolver object, not on the extension list.
        // Does not work to just use additional bean info from UniFileLoader and return one extensions
        // property with no setter--Introspector cleverly (!&#$@&) keeps your display name
        // and everything and adds back in the setter from the superclass.
        // So bypass UniFileLoader in the beaninfo search.
        try {
            PropertyDescriptor extensions = new PropertyDescriptor(
				"extensions", WSDLDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(
				NbBundle.getMessage(WSDLDataLoaderBeanInfo.class, "PROP_extensions"));
            extensions.setShortDescription(
				NbBundle.getMessage(WSDLDataLoaderBeanInfo.class, "HINT_extensions"));
            extensions.setExpert(true);
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }


    // If you have additional properties:
    /*
    public PropertyDescriptor[] getPropertyDescriptors () {
	try {
            PropertyDescriptor myProp = new PropertyDescriptor ("myProp", MyDataLoader.class);
            myProp.setDisplayName (NbBundle.getMessage (MyDataLoaderBeanInfo.class, "PROP_myProp"));
            myProp.setShortDescription (NbBundle.getMessage (MyDataLoaderBeanInfo.class, "HINT_myProp"));
	    return new PropertyDescriptor[] { myProp };
	} catch (IntrospectionException ie) {
            TopManager.getDefault ().getErrorManager ().notify (ie);
	    return null;
	}
    }
    */

    @Override
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            // I.e. MultiFileLoader.class or UniFileLoader.class.
            return new BeanInfo[] { Introspector.getBeanInfo(WSDLDataLoader.class.getSuperclass()) };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault ().notify (ie);
            return null;
        }
    }

    @Override
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return ImageUtilities.loadImage ("org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl_file.png");
        }
        return null;
    }

}

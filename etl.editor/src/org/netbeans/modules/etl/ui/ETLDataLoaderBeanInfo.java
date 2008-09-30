/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.etl.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.openide.ErrorManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/** Description of {@link WSDLDataLoader}.
 *
 * @author Jerry Waldorf
 */
public class ETLDataLoaderBeanInfo extends SimpleBeanInfo {
    private static transient final Logger mLogger = Logger.getLogger(ETLDataLoaderBeanInfo.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
	/**
	 * copied from Ant Module
	 */
        String nbBundle1 = mLoc.t("BUND154: MIME Type");
        String nbBundle2 = mLoc.t("BUND155: The MIME type used for ETL files in the IDE. The ETL MIME resolver recognizes this.");
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
				"extensions", ETLDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(
				nbBundle1.substring(15));
            extensions.setShortDescription(
				nbBundle2.substring(15));
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

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            // I.e. MultiFileLoader.class or UniFileLoader.class.
            return new BeanInfo[] { Introspector.getBeanInfo(ETLDataLoader.class.getSuperclass()) };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault ().notify (ie);
            return null;
        }
    }

    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return ImageUtilities.loadImage ("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        } else {
            return ImageUtilities.loadImage ("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        }
    }

}

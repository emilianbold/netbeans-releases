/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.web;

import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for SecurityConstraint element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface SecurityConstraint extends CommonDDBean, DisplayNameInterface, CreateCapability, FindCapability {
        /** Setter for web-resource-collection element.
         * @param index position in the array of elements
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         */
	public void setWebResourceCollection(int index, org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Getter for web-resource-collection element.
         * @param index position in the array of elements
         * @return web-resource-collection element (WebResourceCollection object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection getWebResourceCollection(int index);
        /** Setter for web-resource-collection elements.
         * @param value array of web-resource-collection elements (WebResourceCollection objects)
         */
	public void setWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[] value);
        /** Getter for web-resource-collection elements.
         * @return array of web-resource-collection elements (WebResourceCollection objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[] getWebResourceCollection();
        /** Returns size of web-resource-collection elements.
         * @return number of web-resource-collection elements 
         */
	public int sizeWebResourceCollection();
        /** Adds web-resource-collection element.
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         * @return index of new web-resource-collection
         */
	public int addWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Removes web-resource-collection element.
         * @param valueInterface web-resource-collection element (WebResourceCollection object)
         * @return index of the removed web-resource-collection
         */
	public int removeWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection valueInterface);
        /** Setter for auth-constraint element.
         * @param valueInterface auth-constraint element (AuthConstraint object)
         */
	public void setAuthConstraint(org.netbeans.modules.j2ee.dd.api.web.AuthConstraint valueInterface);
        /** Getter for auth-constraint element.
         * @return auth-constraint element (AuthConstraintobject)
         */
	public org.netbeans.modules.j2ee.dd.api.web.AuthConstraint getAuthConstraint();
        /** Setter for user-data-constraint element.
         * @param valueInterface user-data-constraint element (UserDataConstraint object)
         */
	public void setUserDataConstraint(org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint valueInterface);
        /** Getter for user-data-constraint element.
         * @return user-data-constraint element (UserDataConstraint object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint getUserDataConstraint();

}

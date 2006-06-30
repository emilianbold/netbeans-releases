/*
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for LocaleEncodingMappingList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface LocaleEncodingMappingList extends CommonDDBean, CreateCapability, FindCapability {
        /** Setter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public void setLocaleEncodingMapping(int index, org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Getter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @return locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping getLocaleEncodingMapping(int index);
        /** Setter for locale-encoding-mapping elements.
         * @param value array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public void setLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] value);
        /** Getter for locale-encoding-mapping elements.
         * @return array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] getLocaleEncodingMapping();
        /** Returns size of locale-encoding-mapping elements.
         * @return number of locale-encoding-mapping elements 
         */
	public int sizeLocaleEncodingMapping();
        /** Adds locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of new locale-encoding-mapping
         */
	public int addLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Removes locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of the removed locale-encoding-mapping
         */
	public int removeLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);

}

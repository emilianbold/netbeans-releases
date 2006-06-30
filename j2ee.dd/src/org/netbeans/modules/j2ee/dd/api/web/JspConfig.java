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
 * Generated interface for JspConfig element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface JspConfig extends CommonDDBean, FindCapability, CreateCapability {
        /** Setter for taglib element.
         * @param index position in the array of elements
         * @param valueInterface taglib element (Taglib object)
         */
	public void setTaglib(int index, org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Getter for taglib element.
         * @param index position in the array of elements
         * @return taglib element (Taglib object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib getTaglib(int index);
        /** Setter for taglib elements.
         * @param value array of taglib elements (Taglib objects)
         */
	public void setTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib[] value);
        /** Getter for taglib elements.
         * @return array of taglib elements (Taglib objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib[] getTaglib();
        /** Returns number of taglib elements.
         * @return number of taglib elements 
         */
	public int sizeTaglib();
        /** Adds taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of new taglib
         */
	public int addTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Removes taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of the removed taglib
         */
	public int removeTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Setter for jsp-property-group element.
         * @param index position in the array of elements
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         */
	public void setJspPropertyGroup(int index, org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Getter for jsp-property-group element.
         * @param index position in the array of elements
         * @return jsp-property-group element (JspPropertyGroup object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup getJspPropertyGroup(int index);
        /** Setter for jsp-property-group elements.
         * @param value array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public void setJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] value);
        /** Getter for jsp-property-group elements.
         * @return array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] getJspPropertyGroup();
        /** Returns number of jsp-property-group elements.
         * @return number of jsp-property-group elements 
         */
	public int sizeJspPropertyGroup();
        /** Adds jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of new jsp-property-group
         */
	public int addJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Removes jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of the removed jsp-property-group
         */
	public int removeJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);

}

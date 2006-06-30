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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Super interface for all DD elements having the description property/properties.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface DescriptionInterface {

    /**
     * Sets the description element for particular locale.<br>
     * If locale=null the method sets the description element without xml:lang attribute.<br>
     * If description=null method removes the description element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param description value for description element
     */
    public void setDescription(String locale, String description) throws VersionNotSupportedException;
    
    /**
     * Sets the description element without xml:lang attribute.
     *
     * @param description value for description element
     */
    public void setDescription(String description);

    /**
     * Sets the multiple description elements.
     *
     * @param descriptions Map of descriptions in the form of [locale,description]
     */
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException;
    
    /**
     * Returns the description element value for particular locale.<br>
     * If locale=null method returns description for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return description element value or null if not specified for given locale
     */
    public String getDescription(String locale) throws VersionNotSupportedException;

    /**
     * Returns the description element value for default locale. 
     *
     * @return description element value or null if not specified for default locale
     */
    public String getDefaultDescription();

    /**
     * Returns all description elements in the form of <@link java.util.Map>. 
     *
     * @return map of all descriptions in the form of [locale:description]
     */
    public java.util.Map getAllDescriptions();
    
    /**
     * Removes the description element for particular locale.
     * If locale=null the method removes the description element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException;
    
    /**
     * Removes description element for default locale.
     */
    public void removeDescription();
    
    /**
     * Removes all description elements from DD element.
     */
    public void removeAllDescriptions();

}

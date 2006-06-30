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
 * Super interface for all DD elements having the display-name property/properties.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface DisplayNameInterface {

    /**
     * Sets the display-name element value for particular locale.<br>
     * If locale=null the method sets the display-name element without xml:lang attribute.<br>
     * If displayName=null method removes the display-name element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param displayName value for display-name element
     */
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException;
    
    /**
     * Sets the display-name element without xml:lang attribute.
     *
     * @param displayName value for display-name element
     */
    public void setDisplayName(String displayName);
    
    /**
     * Sets the multiple display-name elements.
     *
     * @param displayNames Map of display names in the form of [locale,display-name]
     */
    public void setAllDisplayNames(java.util.Map displayNames) throws VersionNotSupportedException;
    
    /**
     * Returns the display-name element value for particular locale.<br>
     * If locale=null method returns display-name for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return display-name element value or null if not specified for given locale
     */
    public String getDisplayName(String locale) throws VersionNotSupportedException;
    
    /**
     * Returns the display-name element value for default locale. 
     *
     * @return display-name element value or null if not specified for default locale
     */
    public String getDefaultDisplayName();
    
    /**
     * Returns all display-name elements in the form of <@link java.util.Map>. 
     *
     * @return map of all display-names in the form of [locale:display-name]
     */
    public java.util.Map getAllDisplayNames();
    
    /**
     * Removes the display-name element for particular locale.
     * If locale=null the method removes the display-name element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException;
    
    /**
     * Removes display-name element for default locale.
     */
    public void removeDisplayName();
    
    /**
     * Removes all display-name elements from DD element.
     */
    public void removeAllDisplayNames();
}

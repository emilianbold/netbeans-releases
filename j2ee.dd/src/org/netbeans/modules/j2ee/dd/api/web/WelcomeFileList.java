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
/**
 * Generated interface for WelcomeFileList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WelcomeFileList extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value property value 
         */
	public void setWelcomeFile(int index, java.lang.String value);
        /** Getter for welcome-file property.
         * @param index position in the array of welcome-files
         * @return property value 
         */
	public java.lang.String getWelcomeFile(int index);
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value array of welcome-file properties
         */
	public void setWelcomeFile(java.lang.String[] value);
        /** Getter for welcome-file property.
         * @return array of welcome-file properties
         */
	public java.lang.String[] getWelcomeFile();
        /** Returns size of welcome-file properties.
         * @return number of welcome-file properties 
         */
	public int sizeWelcomeFile();
        /** Adds welcome-file property.
         * @param value welcome-file property
         * @return index of new welcome-file
         */
	public int addWelcomeFile(java.lang.String value);
        /** Removes welcome-file property.
         * @param value welcome-file property
         * @return index of the removed welcome-file
         */
	public int removeWelcomeFile(java.lang.String value);

}

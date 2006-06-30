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

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

//
// This interface has all of the bean info accessor methods.

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

/**
 * Generated interface for EjbLocalRef element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface EjbLocalRef extends CommonDDBean, DescriptionInterface {

        public static final String EJB_REF_NAME = "EjbRefName";	// NOI18N
	public static final String EJB_REF_TYPE = "EjbRefType";	// NOI18N
	public static final String LOCAL_HOME = "LocalHome";	// NOI18N
	public static final String LOCAL = "Local";	// NOI18N
	public static final String EJB_LINK = "EjbLink";	// NOI18N
        public static final String EJB_REF_TYPE_ENTITY = "Entity"; // NOI18N
        public static final String EJB_REF_TYPE_SESSION = "Session"; // NOI18N
        /** Setter for ejb-ref-name property.
         * @param value property value
         */
	public void setEjbRefName(String value);
        /** Getter for ejb-ref-name property.
         * @return property value 
         */
	public String getEjbRefName();
        /** Setter for ejb-ref-type property.
         * @param value property value 
         */
	public void setEjbRefType(String value);
        /** Getter for ejb-ref-type property.
         * @return property value 
         */
	public String getEjbRefType();
        /** Setter for local-home property.
         * @param value property value
         */
	public void setLocalHome(String value);
        /** Getter for local-home property.
         * @return property value 
         */
	public String getLocalHome();
        /** Setter for local property.
         * @param value property value
         */
	public void setLocal(String value);
        /** Getter for local property.
         * @return property value 
         */
	public String getLocal();
        /** Setter for ejb-link property.
         * @param value property value
         */
	public void setEjbLink(String value);
        /** Getter for ejb-link property.
         * @return property value 
         */
	public String getEjbLink();

        // Java EE 5
        
        void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setInjectionTarget(int index, InjectionTarget valueInterface) throws VersionNotSupportedException;
	InjectionTarget getInjectionTarget(int index) throws VersionNotSupportedException;
	int sizeInjectionTarget() throws VersionNotSupportedException;
	void setInjectionTarget(InjectionTarget[] value) throws VersionNotSupportedException;
	InjectionTarget[] getInjectionTarget() throws VersionNotSupportedException;
	int addInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
	int removeInjectionTarget(InjectionTarget valueInterface) throws VersionNotSupportedException;
	InjectionTarget newInjectionTarget() throws VersionNotSupportedException;

}


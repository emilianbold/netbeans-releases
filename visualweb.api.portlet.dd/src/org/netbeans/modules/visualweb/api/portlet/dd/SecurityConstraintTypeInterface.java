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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public interface SecurityConstraintTypeInterface {
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType newPortletCollectionType();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType newPortletCollectionType(PortletCollectionType source, boolean justData);

	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType newUserDataConstraintType();

	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType newUserDataConstraintType(UserDataConstraintType source, boolean justData);

	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setDisplayName(java.lang.String[] value);

	public void setDisplayName(int index, java.lang.String value);

	public java.lang.String[] getDisplayName();

	public java.util.List fetchDisplayNameList();

	public java.lang.String getDisplayName(int index);

	public int sizeDisplayName();

	public int addDisplayName(java.lang.String value);

	public int removeDisplayName(java.lang.String value);

	public void setDisplayNameXmlLang(java.lang.String[] value);

	public void setDisplayNameXmlLang(int index, java.lang.String value);

	public java.lang.String[] getDisplayNameXmlLang();

	public java.util.List fetchDisplayNameXmlLangList();

	public java.lang.String getDisplayNameXmlLang(int index);

	public int sizeDisplayNameXmlLang();

	public int addDisplayNameXmlLang(java.lang.String value);

	public int removeDisplayNameXmlLang(java.lang.String value);

	public void setPortletCollection(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType getPortletCollection();

	public void setUserDataConstraint(org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType getUserDataConstraint();

}

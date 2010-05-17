/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public interface PortletTypeInterface {
	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setDescription(java.lang.String[] value);

	public void setDescription(int index, java.lang.String value);

	public java.lang.String[] getDescription();

	public java.util.List fetchDescriptionList();

	public java.lang.String getDescription(int index);

	public int sizeDescription();

	public int addDescription(java.lang.String value);

	public int removeDescription(java.lang.String value);

	public void setDescriptionXmlLang(java.lang.String[] value);

	public void setDescriptionXmlLang(int index, java.lang.String value);

	public java.lang.String[] getDescriptionXmlLang();

	public java.util.List fetchDescriptionXmlLangList();

	public java.lang.String getDescriptionXmlLang(int index);

	public int sizeDescriptionXmlLang();

	public int addDescriptionXmlLang(java.lang.String value);

	public int removeDescriptionXmlLang(java.lang.String value);

	public void setPortletName(java.lang.String value);

	public java.lang.String getPortletName();

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

	public void setPortletClass(java.lang.String value);

	public java.lang.String getPortletClass();

	public void setInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType[] value);

	public void setInitParam(int index, org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType[] getInitParam();

	public java.util.List fetchInitParamList();

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType getInitParam(int index);

	public int sizeInitParam();

	public int addInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value);

	public int removeInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType newInitParamType();

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType newInitParamType(InitParamType source, boolean justData);

	public void setExpirationCache(int value);

	public int getExpirationCache();

	public void setSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType[] value);

	public void setSupports(int index, org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType[] getSupports();

	public java.util.List fetchSupportsList();

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType getSupports(int index);

	public int sizeSupports();

	public int addSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value);

	public int removeSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType newSupportsType();

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType newSupportsType(SupportsType source, boolean justData);

	public void setSupportedLocale(java.lang.String[] value);

	public void setSupportedLocale(int index, java.lang.String value);

	public java.lang.String[] getSupportedLocale();

	public java.util.List fetchSupportedLocaleList();

	public java.lang.String getSupportedLocale(int index);

	public int sizeSupportedLocale();

	public int addSupportedLocale(java.lang.String value);

	public int removeSupportedLocale(java.lang.String value);

	public void setResourceBundle(java.lang.String value);

	public java.lang.String getResourceBundle();

	public void setPortletInfo(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType getPortletInfo();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType newPortletInfoType();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType newPortletInfoType(PortletInfoType source, boolean justData);

	public void setPortletInfo2(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType getPortletInfo2();

	public void setPortletPreferences(org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType getPortletPreferences();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType newPortletPreferencesType();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType newPortletPreferencesType(PortletPreferencesType source, boolean justData);

	public void setSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType[] value);

	public void setSecurityRoleRef(int index, org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType[] getSecurityRoleRef();

	public java.util.List fetchSecurityRoleRefList();

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType getSecurityRoleRef(int index);

	public int sizeSecurityRoleRef();

	public int addSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value);

	public int removeSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType newSecurityRoleRefType();

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType newSecurityRoleRefType(SecurityRoleRefType source, boolean justData);

}

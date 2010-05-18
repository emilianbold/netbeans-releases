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
 *	This generated bean class PortletType
 *	matches the schema element 'portletType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				The portlet element contains the declarative data of a portlet.
 *				Used in: portlet-app
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PortletType implements org.netbeans.modules.visualweb.api.portlet.dd.PortletTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String DESCRIPTIONXMLLANG = "DescriptionXmlLang";	// NOI18N
	public static final String PORTLET_NAME = "PortletName";	// NOI18N
	public static final String DISPLAY_NAME = "DisplayName";	// NOI18N
	public static final String DISPLAYNAMEXMLLANG = "DisplayNameXmlLang";	// NOI18N
	public static final String PORTLET_CLASS = "PortletClass";	// NOI18N
	public static final String INIT_PARAM = "InitParam";	// NOI18N
	public static final String EXPIRATION_CACHE = "ExpirationCache";	// NOI18N
	public static final String SUPPORTS = "Supports";	// NOI18N
	public static final String SUPPORTED_LOCALE = "SupportedLocale";	// NOI18N
	public static final String RESOURCE_BUNDLE = "ResourceBundle";	// NOI18N
	public static final String PORTLET_INFO = "PortletInfo";	// NOI18N
	public static final String PORTLET_INFO2 = "PortletInfo2";	// NOI18N
	public static final String PORTLET_PREFERENCES = "PortletPreferences";	// NOI18N
	public static final String SECURITY_ROLE_REF = "SecurityRoleRef";	// NOI18N

	private java.lang.String _Id;
	private java.util.List _Description = new java.util.ArrayList();	// List<java.lang.String>
	private java.util.List _DescriptionXmlLang = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _PortletName;
	private java.util.List _DisplayName = new java.util.ArrayList();	// List<java.lang.String>
	private java.util.List _DisplayNameXmlLang = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _PortletClass;
	private java.util.List _InitParam = new java.util.ArrayList();	// List<InitParamType>
	private int _ExpirationCache;
	private java.util.List _Supports = new java.util.ArrayList();	// List<SupportsType>
	private java.util.List _SupportedLocale = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _ResourceBundle;
	private PortletInfoType _PortletInfo;
	private PortletInfoType _PortletInfo2;
	private PortletPreferencesType _PortletPreferences;
	private java.util.List _SecurityRoleRef = new java.util.ArrayList();	// List<SecurityRoleRefType>

	/**
	 * Normal starting point constructor.
	 */
	public PortletType() {
		_PortletName = "";
		_PortletClass = "";
//		_ResourceBundle = "";
	}

	/**
	 * Required parameters constructor
	 */
	public PortletType(java.lang.String portletName, java.lang.String portletClass, org.netbeans.modules.visualweb.api.portlet.dd.SupportsType[] supports, java.lang.String resourceBundle) {
		_PortletName = portletName;
		_PortletClass = portletClass;
		if (supports!= null) {
			((java.util.ArrayList) _Supports).ensureCapacity(supports.length);
			for (int i = 0; i < supports.length; ++i) {
				_Supports.add(supports[i]);
			}
		}
		_ResourceBundle = resourceBundle;
	}

	/**
	 * Deep copy
	 */
	public PortletType(org.netbeans.modules.visualweb.api.portlet.dd.PortletType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PortletType(org.netbeans.modules.visualweb.api.portlet.dd.PortletType source, boolean justData) {
		_Id = source._Id;
		for (java.util.Iterator it = source._Description.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_Description.add(srcElement);
		}
		for (java.util.Iterator it = source._DescriptionXmlLang.iterator(); 
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DescriptionXmlLang.add(srcElement);
		}
		_PortletName = source._PortletName;
		for (java.util.Iterator it = source._DisplayName.iterator(); 
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DisplayName.add(srcElement);
		}
		for (java.util.Iterator it = source._DisplayNameXmlLang.iterator(); 
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DisplayNameXmlLang.add(srcElement);
		}
		_PortletClass = source._PortletClass;
		for (java.util.Iterator it = source._InitParam.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it.next();
			_InitParam.add((srcElement == null) ? null : newInitParamType(srcElement, justData));
		}
		_ExpirationCache = source._ExpirationCache;
		for (java.util.Iterator it = source._Supports.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it.next();
			_Supports.add((srcElement == null) ? null : newSupportsType(srcElement, justData));
		}
		for (java.util.Iterator it = source._SupportedLocale.iterator(); 
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_SupportedLocale.add(srcElement);
		}
		_ResourceBundle = source._ResourceBundle;
		_PortletInfo = (source._PortletInfo == null) ? null : newPortletInfoType(source._PortletInfo, justData);
		_PortletInfo2 = (source._PortletInfo2 == null) ? null : newPortletInfoType(source._PortletInfo2, justData);
		_PortletPreferences = (source._PortletPreferences == null) ? null : newPortletPreferencesType(source._PortletPreferences, justData);
		for (java.util.Iterator it = source._SecurityRoleRef.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it.next();
			_SecurityRoleRef.add((srcElement == null) ? null : newSecurityRoleRefType(srcElement, justData));
		}
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is an array, possibly empty
	public void setDescription(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_Description.clear();
		((java.util.ArrayList) _Description).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Description.add(value[i]);
		}
	}

	public void setDescription(int index, java.lang.String value) {
		_Description.set(index, value);
	}

	public java.lang.String[] getDescription() {
		java.lang.String[] arr = new java.lang.String[_Description.size()];
		return (java.lang.String[]) _Description.toArray(arr);
	}

	public java.util.List fetchDescriptionList() {
		return _Description;
	}

	public java.lang.String getDescription(int index) {
		return (java.lang.String)_Description.get(index);
	}

	// Return the number of description
	public int sizeDescription() {
		return _Description.size();
	}

	public int addDescription(java.lang.String value) {
		_Description.add(value);
		int positionOfNewItem = _Description.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDescription(java.lang.String value) {
		int pos = _Description.indexOf(value);
		if (pos >= 0) {
			_Description.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setDescriptionXmlLang(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DescriptionXmlLang.clear();
		((java.util.ArrayList) _DescriptionXmlLang).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DescriptionXmlLang.add(value[i]);
		}
	}

	public void setDescriptionXmlLang(int index, java.lang.String value) {
		for (int size = _DescriptionXmlLang.size(); index >= size; ++size) {
			_DescriptionXmlLang.add(null);
		}
		_DescriptionXmlLang.set(index, value);
	}

	public java.lang.String[] getDescriptionXmlLang() {
		java.lang.String[] arr = new java.lang.String[_DescriptionXmlLang.size()];
		return (java.lang.String[]) _DescriptionXmlLang.toArray(arr);
	}

	public java.util.List fetchDescriptionXmlLangList() {
		return _DescriptionXmlLang;
	}

	public java.lang.String getDescriptionXmlLang(int index) {
		return (java.lang.String)_DescriptionXmlLang.get(index);
	}

	// Return the number of descriptionXmlLang
	public int sizeDescriptionXmlLang() {
		return _DescriptionXmlLang.size();
	}

	public int addDescriptionXmlLang(java.lang.String value) {
		_DescriptionXmlLang.add(value);
		int positionOfNewItem = _DescriptionXmlLang.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDescriptionXmlLang(java.lang.String value) {
		int pos = _DescriptionXmlLang.indexOf(value);
		if (pos >= 0) {
			_DescriptionXmlLang.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setPortletName(java.lang.String value) {
		_PortletName = value;
	}

	public java.lang.String getPortletName() {
		return _PortletName;
	}

	// This attribute is an array, possibly empty
	public void setDisplayName(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DisplayName.clear();
		((java.util.ArrayList) _DisplayName).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DisplayName.add(value[i]);
		}
	}

	public void setDisplayName(int index, java.lang.String value) {
		_DisplayName.set(index, value);
	}

	public java.lang.String[] getDisplayName() {
		java.lang.String[] arr = new java.lang.String[_DisplayName.size()];
		return (java.lang.String[]) _DisplayName.toArray(arr);
	}

	public java.util.List fetchDisplayNameList() {
		return _DisplayName;
	}

	public java.lang.String getDisplayName(int index) {
		return (java.lang.String)_DisplayName.get(index);
	}

	// Return the number of displayName
	public int sizeDisplayName() {
		return _DisplayName.size();
	}

	public int addDisplayName(java.lang.String value) {
		_DisplayName.add(value);
		int positionOfNewItem = _DisplayName.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDisplayName(java.lang.String value) {
		int pos = _DisplayName.indexOf(value);
		if (pos >= 0) {
			_DisplayName.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setDisplayNameXmlLang(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DisplayNameXmlLang.clear();
		((java.util.ArrayList) _DisplayNameXmlLang).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DisplayNameXmlLang.add(value[i]);
		}
	}

	public void setDisplayNameXmlLang(int index, java.lang.String value) {
		for (int size = _DisplayNameXmlLang.size(); index >= size; ++size) {
			_DisplayNameXmlLang.add(null);
		}
		_DisplayNameXmlLang.set(index, value);
	}

	public java.lang.String[] getDisplayNameXmlLang() {
		java.lang.String[] arr = new java.lang.String[_DisplayNameXmlLang.size()];
		return (java.lang.String[]) _DisplayNameXmlLang.toArray(arr);
	}

	public java.util.List fetchDisplayNameXmlLangList() {
		return _DisplayNameXmlLang;
	}

	public java.lang.String getDisplayNameXmlLang(int index) {
		return (java.lang.String)_DisplayNameXmlLang.get(index);
	}

	// Return the number of displayNameXmlLang
	public int sizeDisplayNameXmlLang() {
		return _DisplayNameXmlLang.size();
	}

	public int addDisplayNameXmlLang(java.lang.String value) {
		_DisplayNameXmlLang.add(value);
		int positionOfNewItem = _DisplayNameXmlLang.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDisplayNameXmlLang(java.lang.String value) {
		int pos = _DisplayNameXmlLang.indexOf(value);
		if (pos >= 0) {
			_DisplayNameXmlLang.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setPortletClass(java.lang.String value) {
		_PortletClass = value;
	}

	public java.lang.String getPortletClass() {
		return _PortletClass;
	}

	// This attribute is an array, possibly empty
	public void setInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType[] value) {
		if (value == null)
			value = new InitParamType[0];
		_InitParam.clear();
		((java.util.ArrayList) _InitParam).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_InitParam.add(value[i]);
		}
	}

	public void setInitParam(int index, org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value) {
		_InitParam.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType[] getInitParam() {
		InitParamType[] arr = new InitParamType[_InitParam.size()];
		return (InitParamType[]) _InitParam.toArray(arr);
	}

	public java.util.List fetchInitParamList() {
		return _InitParam;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType getInitParam(int index) {
		return (InitParamType)_InitParam.get(index);
	}

	// Return the number of initParam
	public int sizeInitParam() {
		return _InitParam.size();
	}

	public int addInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value) {
		_InitParam.add(value);
		int positionOfNewItem = _InitParam.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeInitParam(org.netbeans.modules.visualweb.api.portlet.dd.InitParamType value) {
		int pos = _InitParam.indexOf(value);
		if (pos >= 0) {
			_InitParam.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setExpirationCache(int value) {
		_ExpirationCache = value;
	}

	public int getExpirationCache() {
		return _ExpirationCache;
	}

	// This attribute is an array containing at least one element
	public void setSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType[] value) {
		if (value == null)
			value = new SupportsType[0];
		_Supports.clear();
		((java.util.ArrayList) _Supports).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Supports.add(value[i]);
		}
	}

	public void setSupports(int index, org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value) {
		_Supports.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType[] getSupports() {
		SupportsType[] arr = new SupportsType[_Supports.size()];
		return (SupportsType[]) _Supports.toArray(arr);
	}

	public java.util.List fetchSupportsList() {
		return _Supports;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType getSupports(int index) {
		return (SupportsType)_Supports.get(index);
	}

	// Return the number of supports
	public int sizeSupports() {
		return _Supports.size();
	}

	public int addSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value) {
		_Supports.add(value);
		int positionOfNewItem = _Supports.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSupports(org.netbeans.modules.visualweb.api.portlet.dd.SupportsType value) {
		int pos = _Supports.indexOf(value);
		if (pos >= 0) {
			_Supports.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setSupportedLocale(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_SupportedLocale.clear();
		((java.util.ArrayList) _SupportedLocale).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_SupportedLocale.add(value[i]);
		}
	}

	public void setSupportedLocale(int index, java.lang.String value) {
		_SupportedLocale.set(index, value);
	}

	public java.lang.String[] getSupportedLocale() {
		java.lang.String[] arr = new java.lang.String[_SupportedLocale.size()];
		return (java.lang.String[]) _SupportedLocale.toArray(arr);
	}

	public java.util.List fetchSupportedLocaleList() {
		return _SupportedLocale;
	}

	public java.lang.String getSupportedLocale(int index) {
		return (java.lang.String)_SupportedLocale.get(index);
	}

	// Return the number of supportedLocale
	public int sizeSupportedLocale() {
		return _SupportedLocale.size();
	}

	public int addSupportedLocale(java.lang.String value) {
		_SupportedLocale.add(value);
		int positionOfNewItem = _SupportedLocale.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSupportedLocale(java.lang.String value) {
		int pos = _SupportedLocale.indexOf(value);
		if (pos >= 0) {
			_SupportedLocale.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setResourceBundle(java.lang.String value) {
		_ResourceBundle = value;
	}

	public java.lang.String getResourceBundle() {
		return _ResourceBundle;
	}

	// This attribute is optional
	public void setPortletInfo(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType value) {
		_PortletInfo = value;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType getPortletInfo() {
		return _PortletInfo;
	}

	// This attribute is mandatory
	public void setPortletInfo2(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType value) {
		_PortletInfo2 = value;
		if (value != null) {
			// It's a mutually exclusive property.
			setResourceBundle(null);
			setPortletInfo(null);
		}
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType getPortletInfo2() {
		return _PortletInfo2;
	}

	// This attribute is optional
	public void setPortletPreferences(org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType value) {
		_PortletPreferences = value;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType getPortletPreferences() {
		return _PortletPreferences;
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType[] value) {
		if (value == null)
			value = new SecurityRoleRefType[0];
		_SecurityRoleRef.clear();
		((java.util.ArrayList) _SecurityRoleRef).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_SecurityRoleRef.add(value[i]);
		}
	}

	public void setSecurityRoleRef(int index, org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value) {
		_SecurityRoleRef.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType[] getSecurityRoleRef() {
		SecurityRoleRefType[] arr = new SecurityRoleRefType[_SecurityRoleRef.size()];
		return (SecurityRoleRefType[]) _SecurityRoleRef.toArray(arr);
	}

	public java.util.List fetchSecurityRoleRefList() {
		return _SecurityRoleRef;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType getSecurityRoleRef(int index) {
		return (SecurityRoleRefType)_SecurityRoleRef.get(index);
	}

	// Return the number of securityRoleRef
	public int sizeSecurityRoleRef() {
		return _SecurityRoleRef.size();
	}

	public int addSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value) {
		_SecurityRoleRef.add(value);
		int positionOfNewItem = _SecurityRoleRef.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSecurityRoleRef(org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType value) {
		int pos = _SecurityRoleRef.indexOf(value);
		if (pos >= 0) {
			_SecurityRoleRef.remove(pos);
		}
		return pos;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType newInitParamType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.InitParamType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.InitParamType newInitParamType(InitParamType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.InitParamType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType newSupportsType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SupportsType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SupportsType newSupportsType(SupportsType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SupportsType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType newPortletInfoType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType newPortletInfoType(PortletInfoType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType newPortletPreferencesType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType newPortletPreferencesType(PortletPreferencesType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType newSecurityRoleRefType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType newSecurityRoleRefType(SecurityRoleRefType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "portletType";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		// id is an attribute with namespace http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd
		if (_Id != null) {
			out.write(" id='");
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Id, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		int index = 0;
		for (java.util.Iterator it = _Description.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<description");	// NOI18N
				if (index < sizeDescriptionXmlLang()) {
					// xml:lang is an attribute with namespace http://www.w3.org/XML/1998/namespace
					if (getDescriptionXmlLang(index) != null) {
						out.write(" xml:lang='");
						org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, getDescriptionXmlLang(index), true);
						out.write("'");	// NOI18N
					}
				}
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</description>\n");	// NOI18N
			}
			++index;
		}
		if (_PortletName != null) {
			out.write(nextIndent);
			out.write("<portlet-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _PortletName, false);
			out.write("</portlet-name>\n");	// NOI18N
		}
		index = 0;
		for (java.util.Iterator it = _DisplayName.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<display-name");	// NOI18N
				if (index < sizeDisplayNameXmlLang()) {
					// xml:lang is an attribute with namespace http://www.w3.org/XML/1998/namespace
					if (getDisplayNameXmlLang(index) != null) {
						out.write(" xml:lang='");
						org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, getDisplayNameXmlLang(index), true);
						out.write("'");	// NOI18N
					}
				}
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</display-name>\n");	// NOI18N
			}
			++index;
		}
		if (_PortletClass != null) {
			out.write(nextIndent);
			out.write("<portlet-class");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _PortletClass, false);
			out.write("</portlet-class>\n");	// NOI18N
		}
		for (java.util.Iterator it = _InitParam.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it.next();
			if (element != null) {
				element.writeNode(out, "init-param", null, nextIndent, namespaceMap);
			}
		}
		out.write(nextIndent);
		out.write("<expiration-cache");	// NOI18N
		out.write(">");	// NOI18N
		out.write(""+_ExpirationCache);
		out.write("</expiration-cache>\n");	// NOI18N
		for (java.util.Iterator it = _Supports.iterator(); it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it.next();
			if (element != null) {
				element.writeNode(out, "supports", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _SupportedLocale.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<supported-locale");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</supported-locale>\n");	// NOI18N
			}
		}
		if (_ResourceBundle != null) {
			out.write(nextIndent);
			out.write("<resource-bundle");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _ResourceBundle, false);
			out.write("</resource-bundle>\n");	// NOI18N
		}
		if (_PortletInfo != null) {
			_PortletInfo.writeNode(out, "portlet-info", null, nextIndent, namespaceMap);
		}
		if (_PortletInfo2 != null) {
			_PortletInfo2.writeNode(out, "portlet-info", null, nextIndent, namespaceMap);
		}
		if (_PortletPreferences != null) {
			_PortletPreferences.writeNode(out, "portlet-preferences", null, nextIndent, namespaceMap);
		}
		for (java.util.Iterator it = _SecurityRoleRef.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it.next();
			if (element != null) {
				element.writeNode(out, "security-role-ref", null, nextIndent, namespaceMap);
			}
		}
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6, attrName.length());
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("id");
			if (attr != null) {
				attrValue = attr.getValue();
				_Id = attrValue;
			}
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		int lastElementType = -1;
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			org.w3c.dom.NamedNodeMap attrs = childNode.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			if (childNodeName == "description") {
				java.lang.String aDescription;
				aDescription = childNodeValue;
				attr = (org.w3c.dom.Attr) attrs.getNamedItem("xml:lang");
				if (attr != null) {
					attrValue = attr.getValue();
				} else {
					attrValue = null;
				}
				java.lang.String processedValueFor_DescriptionXmlLang;
				processedValueFor_DescriptionXmlLang = attrValue;
				addDescriptionXmlLang(processedValueFor_DescriptionXmlLang);
				_Description.add(aDescription);
				lastElementType = 1;
			}
			else if (childNodeName == "portlet-name") {
				_PortletName = childNodeValue;
				lastElementType = 3;
			}
			else if (childNodeName == "display-name") {
				java.lang.String aDisplayName;
				aDisplayName = childNodeValue;
				attr = (org.w3c.dom.Attr) attrs.getNamedItem("xml:lang");
				if (attr != null) {
					attrValue = attr.getValue();
				} else {
					attrValue = null;
				}
				java.lang.String processedValueFor_DisplayNameXmlLang;
				processedValueFor_DisplayNameXmlLang = attrValue;
				addDisplayNameXmlLang(processedValueFor_DisplayNameXmlLang);
				_DisplayName.add(aDisplayName);
				lastElementType = 4;
			}
			else if (childNodeName == "portlet-class") {
				_PortletClass = childNodeValue;
				lastElementType = 6;
			}
			else if (childNodeName == "init-param") {
				InitParamType aInitParam = newInitParamType();
				aInitParam.readNode(childNode, namespacePrefixes);
				_InitParam.add(aInitParam);
				lastElementType = 7;
			}
			else if (childNodeName == "expiration-cache") {
				_ExpirationCache = Integer.parseInt(childNodeValue);
				lastElementType = 8;
			}
			else if (childNodeName == "supports") {
				SupportsType aSupports = newSupportsType();
				aSupports.readNode(childNode, namespacePrefixes);
				_Supports.add(aSupports);
				lastElementType = 9;
			}
			else if (childNodeName == "supported-locale") {
				java.lang.String aSupportedLocale;
				aSupportedLocale = childNodeValue;
				_SupportedLocale.add(aSupportedLocale);
				lastElementType = 10;
			}
			else if (childNodeName == "resource-bundle") {
				_ResourceBundle = childNodeValue;
				lastElementType = 11;
			}
			else if (lastElementType < 12 && childNodeName == "portlet-info") {
				_PortletInfo = newPortletInfoType();
				_PortletInfo.readNode(childNode, namespacePrefixes);
				lastElementType = 12;
			}
			else if (childNodeName == "portlet-info") {
				_PortletInfo2 = newPortletInfoType();
				_PortletInfo2.readNode(childNode, namespacePrefixes);
				lastElementType = 13;
			}
			else if (childNodeName == "portlet-preferences") {
				_PortletPreferences = newPortletPreferencesType();
				_PortletPreferences.readNode(childNode, namespacePrefixes);
				lastElementType = 14;
			}
			else if (childNodeName == "security-role-ref") {
				SecurityRoleRefType aSecurityRoleRef = newSecurityRoleRefType();
				aSecurityRoleRef.readNode(childNode, namespacePrefixes);
				_SecurityRoleRef.add(aSecurityRoleRef);
				lastElementType = 15;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property id
		// Validating property description
		// Validating property descriptionXmlLang
		// Validating property portletName
		if (getPortletName() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getPortletName() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "portletName", this);	// NOI18N
		}
		// Validating property displayName
		for (int _index = 0; _index < sizeDisplayName(); ++_index) {
			java.lang.String element = getDisplayName(_index);
			if (element != null) {
				// has whitespace restriction
				if (restrictionFailure) {
					throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("element whiteSpace (collapse)", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.DATA_RESTRICTION, "displayName", this);	// NOI18N
				}
			}
		}
		// Validating property displayNameXmlLang
		// Validating property portletClass
		if (getPortletClass() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getPortletClass() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "portletClass", this);	// NOI18N
		}
		// Validating property initParam
		for (int _index = 0; _index < sizeInitParam(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element = getInitParam(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property expirationCache
		// Validating property supports
		if (sizeSupports() == 0) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("sizeSupports() == 0", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "supports", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeSupports(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element = getSupports(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property supportedLocale
		// Validating property resourceBundle
		if (getResourceBundle() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getResourceBundle() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "resourceBundle", this);	// NOI18N
		}
		// Validating property portletInfo
		if (getPortletInfo() != null) {
			getPortletInfo().validate();
		}
		// Validating property portletInfo2
		if (getPortletInfo2() != null) {
			getPortletInfo2().validate();
		}
		if (getPortletInfo2() != null) {
			if (getResourceBundle() != null) {
				throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("mutually exclusive properties: PortletInfo2 and ResourceBundle", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "ResourceBundle", this);	// NOI18N
			}
			if (getPortletInfo() != null) {
				throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("mutually exclusive properties: PortletInfo2 and PortletInfo", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.MUTUALLY_EXCLUSIVE, "PortletInfo", this);	// NOI18N
			}
		}
		// Validating property portletPreferences
		if (getPortletPreferences() != null) {
			getPortletPreferences().validate();
		}
		// Validating property securityRoleRef
		for (int _index = 0; _index < sizeSecurityRoleRef(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element = getSecurityRoleRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		if (getPortletInfo() == null && getPortletInfo2() == null && getResourceBundle() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("required properties: getPortletInfo() == null && getPortletInfo2() == null && getResourceBundle() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "PortletInfo2", this);	// NOI18N
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "description")
			addDescription((java.lang.String)value);
		else if (name == "description[]")
			setDescription((java.lang.String[]) value);
		else if (name == "descriptionXmlLang")
			addDescriptionXmlLang((java.lang.String)value);
		else if (name == "descriptionXmlLang[]")
			setDescriptionXmlLang((java.lang.String[]) value);
		else if (name == "portletName")
			setPortletName((java.lang.String)value);
		else if (name == "displayName")
			addDisplayName((java.lang.String)value);
		else if (name == "displayName[]")
			setDisplayName((java.lang.String[]) value);
		else if (name == "displayNameXmlLang")
			addDisplayNameXmlLang((java.lang.String)value);
		else if (name == "displayNameXmlLang[]")
			setDisplayNameXmlLang((java.lang.String[]) value);
		else if (name == "portletClass")
			setPortletClass((java.lang.String)value);
		else if (name == "initParam")
			addInitParam((InitParamType)value);
		else if (name == "initParam[]")
			setInitParam((InitParamType[]) value);
		else if (name == "expirationCache")
			setExpirationCache(((java.lang.Integer)value).intValue());
		else if (name == "supports")
			addSupports((SupportsType)value);
		else if (name == "supports[]")
			setSupports((SupportsType[]) value);
		else if (name == "supportedLocale")
			addSupportedLocale((java.lang.String)value);
		else if (name == "supportedLocale[]")
			setSupportedLocale((java.lang.String[]) value);
		else if (name == "resourceBundle")
			setResourceBundle((java.lang.String)value);
		else if (name == "portletInfo")
			setPortletInfo((PortletInfoType)value);
		else if (name == "portletInfo2")
			setPortletInfo2((PortletInfoType)value);
		else if (name == "portletPreferences")
			setPortletPreferences((PortletPreferencesType)value);
		else if (name == "securityRoleRef")
			addSecurityRoleRef((SecurityRoleRefType)value);
		else if (name == "securityRoleRef[]")
			setSecurityRoleRef((SecurityRoleRefType[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PortletType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "description[]")
			return getDescription();
		if (name == "descriptionXmlLang[]")
			return getDescriptionXmlLang();
		if (name == "portletName")
			return getPortletName();
		if (name == "displayName[]")
			return getDisplayName();
		if (name == "displayNameXmlLang[]")
			return getDisplayNameXmlLang();
		if (name == "portletClass")
			return getPortletClass();
		if (name == "initParam[]")
			return getInitParam();
		if (name == "expirationCache")
			return new java.lang.Integer(getExpirationCache());
		if (name == "supports[]")
			return getSupports();
		if (name == "supportedLocale[]")
			return getSupportedLocale();
		if (name == "resourceBundle")
			return getResourceBundle();
		if (name == "portletInfo")
			return getPortletInfo();
		if (name == "portletInfo2")
			return getPortletInfo2();
		if (name == "portletPreferences")
			return getPortletPreferences();
		if (name == "securityRoleRef[]")
			return getSecurityRoleRef();
		throw new IllegalArgumentException(name+" is not a valid property name for PortletType");
	}

	public String nameSelf() {
		return "PortletType";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof InitParamType) {
			InitParamType child = (InitParamType) childObj;
			int index = 0;
			for (java.util.Iterator it = _InitParam.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it.next();
				if (child == element) {
					if (returnConstName) {
						return INIT_PARAM;
					} else if (returnSchemaName) {
						return "init-param";
					} else if (returnXPathName) {
						return "init-param[position()="+index+"]";
					} else {
						return "InitParam."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Id) {
				if (returnConstName) {
					return ID;
				} else if (returnSchemaName) {
					return "id";
				} else if (returnXPathName) {
					return "@id";
				} else {
					return "Id";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _Description.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DESCRIPTION;
					} else if (returnSchemaName) {
						return "description";
					} else if (returnXPathName) {
						return "description[position()="+index+"]";
					} else {
						return "Description."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _DescriptionXmlLang.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DESCRIPTIONXMLLANG;
					} else if (returnSchemaName) {
						return "xml:lang";
					} else if (returnXPathName) {
						return "@xml:lang[position()="+index+"]";
					} else {
						return "DescriptionXmlLang."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _PortletName) {
				if (returnConstName) {
					return PORTLET_NAME;
				} else if (returnSchemaName) {
					return "portlet-name";
				} else if (returnXPathName) {
					return "portlet-name";
				} else {
					return "PortletName";
				}
			}
			index = 0;
			for (java.util.Iterator it = _DisplayName.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DISPLAY_NAME;
					} else if (returnSchemaName) {
						return "display-name";
					} else if (returnXPathName) {
						return "display-name[position()="+index+"]";
					} else {
						return "DisplayName."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _DisplayNameXmlLang.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DISPLAYNAMEXMLLANG;
					} else if (returnSchemaName) {
						return "xml:lang";
					} else if (returnXPathName) {
						return "@xml:lang[position()="+index+"]";
					} else {
						return "DisplayNameXmlLang."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _PortletClass) {
				if (returnConstName) {
					return PORTLET_CLASS;
				} else if (returnSchemaName) {
					return "portlet-class";
				} else if (returnXPathName) {
					return "portlet-class";
				} else {
					return "PortletClass";
				}
			}
			index = 0;
			for (java.util.Iterator it = _SupportedLocale.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return SUPPORTED_LOCALE;
					} else if (returnSchemaName) {
						return "supported-locale";
					} else if (returnXPathName) {
						return "supported-locale[position()="+index+"]";
					} else {
						return "SupportedLocale."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _ResourceBundle) {
				if (returnConstName) {
					return RESOURCE_BUNDLE;
				} else if (returnSchemaName) {
					return "resource-bundle";
				} else if (returnXPathName) {
					return "resource-bundle";
				} else {
					return "ResourceBundle";
				}
			}
		}
		if (childObj instanceof java.lang.Integer) {
			java.lang.Integer child = (java.lang.Integer) childObj;
			if (((java.lang.Integer)child).intValue() == _ExpirationCache) {
				if (returnConstName) {
					return EXPIRATION_CACHE;
				} else if (returnSchemaName) {
					return "expiration-cache";
				} else if (returnXPathName) {
					return "expiration-cache";
				} else {
					return "ExpirationCache";
				}
			}
		}
		if (childObj instanceof SecurityRoleRefType) {
			SecurityRoleRefType child = (SecurityRoleRefType) childObj;
			int index = 0;
			for (java.util.Iterator it = _SecurityRoleRef.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it.next();
				if (child == element) {
					if (returnConstName) {
						return SECURITY_ROLE_REF;
					} else if (returnSchemaName) {
						return "security-role-ref";
					} else if (returnXPathName) {
						return "security-role-ref[position()="+index+"]";
					} else {
						return "SecurityRoleRef."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof PortletPreferencesType) {
			PortletPreferencesType child = (PortletPreferencesType) childObj;
			if (child == _PortletPreferences) {
				if (returnConstName) {
					return PORTLET_PREFERENCES;
				} else if (returnSchemaName) {
					return "portlet-preferences";
				} else if (returnXPathName) {
					return "portlet-preferences";
				} else {
					return "PortletPreferences";
				}
			}
		}
		if (childObj instanceof PortletInfoType) {
			PortletInfoType child = (PortletInfoType) childObj;
			if (child == _PortletInfo) {
				if (returnConstName) {
					return PORTLET_INFO;
				} else if (returnSchemaName) {
					return "portlet-info";
				} else if (returnXPathName) {
					return "portlet-info";
				} else {
					return "PortletInfo";
				}
			}
			if (child == _PortletInfo2) {
				if (returnConstName) {
					return PORTLET_INFO2;
				} else if (returnSchemaName) {
					return "portlet-info";
				} else if (returnXPathName) {
					return "portlet-info";
				} else {
					return "PortletInfo2";
				}
			}
		}
		if (childObj instanceof SupportsType) {
			SupportsType child = (SupportsType) childObj;
			int index = 0;
			for (java.util.Iterator it = _Supports.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it.next();
				if (child == element) {
					if (returnConstName) {
						return SUPPORTS;
					} else if (returnSchemaName) {
						return "supports";
					} else if (returnXPathName) {
						return "supports[position()="+index+"]";
					} else {
						return "Supports."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[] result = new org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[children.size()];
		return (org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
		for (java.util.Iterator it = _InitParam.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _Supports.iterator(); it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		if (_PortletInfo != null) {
			if (recursive) {
				_PortletInfo.childBeans(true, beans);
			}
			beans.add(_PortletInfo);
		}
		if (_PortletInfo2 != null) {
			if (recursive) {
				_PortletInfo2.childBeans(true, beans);
			}
			beans.add(_PortletInfo2);
		}
		if (_PortletPreferences != null) {
			if (recursive) {
				_PortletPreferences.childBeans(true, beans);
			}
			beans.add(_PortletPreferences);
		}
		for (java.util.Iterator it = _SecurityRoleRef.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PortletType && equals((org.netbeans.modules.visualweb.api.portlet.dd.PortletType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PortletType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (sizeDescription() != inst.sizeDescription())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Description.iterator(), it2 = inst._Description.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeDescriptionXmlLang() != inst.sizeDescriptionXmlLang())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DescriptionXmlLang.iterator(), it2 = inst._DescriptionXmlLang.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_PortletName == null ? inst._PortletName == null : _PortletName.equals(inst._PortletName))) {
			return false;
		}
		if (sizeDisplayName() != inst.sizeDisplayName())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DisplayName.iterator(), it2 = inst._DisplayName.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeDisplayNameXmlLang() != inst.sizeDisplayNameXmlLang())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DisplayNameXmlLang.iterator(), it2 = inst._DisplayNameXmlLang.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_PortletClass == null ? inst._PortletClass == null : _PortletClass.equals(inst._PortletClass))) {
			return false;
		}
		if (sizeInitParam() != inst.sizeInitParam())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _InitParam.iterator(), it2 = inst._InitParam.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.InitParamType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.InitParamType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_ExpirationCache == inst._ExpirationCache)) {
			return false;
		}
		if (sizeSupports() != inst.sizeSupports())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Supports.iterator(), it2 = inst._Supports.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.SupportsType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.SupportsType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeSupportedLocale() != inst.sizeSupportedLocale())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _SupportedLocale.iterator(), it2 = inst._SupportedLocale.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_ResourceBundle == null ? inst._ResourceBundle == null : _ResourceBundle.equals(inst._ResourceBundle))) {
			return false;
		}
		if (!(_PortletInfo == null ? inst._PortletInfo == null : _PortletInfo.equals(inst._PortletInfo))) {
			return false;
		}
		if (!(_PortletInfo2 == null ? inst._PortletInfo2 == null : _PortletInfo2.equals(inst._PortletInfo2))) {
			return false;
		}
		if (!(_PortletPreferences == null ? inst._PortletPreferences == null : _PortletPreferences.equals(inst._PortletPreferences))) {
			return false;
		}
		if (sizeSecurityRoleRef() != inst.sizeSecurityRoleRef())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _SecurityRoleRef.iterator(), it2 = inst._SecurityRoleRef.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityRoleRefType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Description == null ? 0 : _Description.hashCode());
		result = 37*result + (_DescriptionXmlLang == null ? 0 : _DescriptionXmlLang.hashCode());
		result = 37*result + (_PortletName == null ? 0 : _PortletName.hashCode());
		result = 37*result + (_DisplayName == null ? 0 : _DisplayName.hashCode());
		result = 37*result + (_DisplayNameXmlLang == null ? 0 : _DisplayNameXmlLang.hashCode());
		result = 37*result + (_PortletClass == null ? 0 : _PortletClass.hashCode());
		result = 37*result + (_InitParam == null ? 0 : _InitParam.hashCode());
		result = 37*result + (_ExpirationCache);
		result = 37*result + (_Supports == null ? 0 : _Supports.hashCode());
		result = 37*result + (_SupportedLocale == null ? 0 : _SupportedLocale.hashCode());
		result = 37*result + (_ResourceBundle == null ? 0 : _ResourceBundle.hashCode());
		result = 37*result + (_PortletInfo == null ? 0 : _PortletInfo.hashCode());
		result = 37*result + (_PortletInfo2 == null ? 0 : _PortletInfo2.hashCode());
		result = 37*result + (_PortletPreferences == null ? 0 : _PortletPreferences.hashCode());
		result = 37*result + (_SecurityRoleRef == null ? 0 : _SecurityRoleRef.hashCode());
		return result;
	}

	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		try {
			writeNode(sw);
		} catch (java.io.IOException e) {
			// How can we actually get an IOException on a StringWriter?
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

}


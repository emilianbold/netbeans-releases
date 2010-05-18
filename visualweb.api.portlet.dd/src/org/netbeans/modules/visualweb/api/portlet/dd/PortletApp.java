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
 *	This generated bean class PortletApp
 *	matches the schema element 'portlet-app'.
 *
 *	===============================================================
 *	
 *				The portlet-app element is the root of the deployment descriptor
 *				for a portlet application. This element has a required attribute version
 *				to specify to which version of the schema the deployment descriptor
 *				conforms.
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	portletApp <portlet-app> : PortletApp
 * 		[attr: version CDATA #REQUIRED  : java.lang.String]
 * 		[attr: id CDATA #IMPLIED  : java.lang.String]
 * 		portlet <portlet> : PortletType[0,n] 	[unique name='init-param-name-uniqueness', unique name='supports-mime-type-uniqueness', unique name='preference-name-uniqueness', unique name='security-role-ref-name-uniqueness']
 * 			[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			description <description> : java.lang.String[0,n]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			portletName <portlet-name> : java.lang.String
 * 			displayName <display-name> : java.lang.String[0,n] 	[whiteSpace (collapse)]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			portletClass <portlet-class> : java.lang.String
 * 			initParam <init-param> : InitParamType[0,n]
 * 				[attr: id CDATA #IMPLIED  : java.lang.String]
 * 				description <description> : java.lang.String[0,n]
 * 					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 				name <name> : java.lang.String
 * 				value <value> : java.lang.String
 * 			expirationCache <expiration-cache> : int[0,1]
 * 			supports <supports> : SupportsType[1,n]
 * 				[attr: id CDATA #IMPLIED  : java.lang.String]
 * 				mimeType <mime-type> : java.lang.String
 * 				portletMode <portlet-mode> : java.lang.String[0,n]
 * 			supportedLocale <supported-locale> : java.lang.String[0,n]
 * 			| resourceBundle <resource-bundle> : java.lang.String
 * 			| portletInfo <portlet-info> : PortletInfoType[0,1]
 * 			| 	[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			| 	title <title> : java.lang.String
 * 			| 	shortTitle <short-title> : java.lang.String[0,1]
 * 			| 	keywords <keywords> : java.lang.String[0,1]
 * 			| portletInfo2 <portlet-info> : PortletInfoType
 * 			| 	[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			| 	title <title> : java.lang.String
 * 			| 	shortTitle <short-title> : java.lang.String[0,1]
 * 			| 	keywords <keywords> : java.lang.String[0,1]
 * 			portletPreferences <portlet-preferences> : PortletPreferencesType[0,1]
 * 				[attr: id CDATA #IMPLIED  : java.lang.String]
 * 				preference <preference> : PreferenceType[0,n]
 * 					[attr: id CDATA #IMPLIED  : java.lang.String]
 * 					name <name> : java.lang.String
 * 					value <value> : java.lang.String[0,n]
 * 					readOnly <read-only> : java.lang.String[0,1] 	[enumeration (true), enumeration (false)]
 * 				preferencesValidator <preferences-validator> : java.lang.String[0,1]
 * 			securityRoleRef <security-role-ref> : SecurityRoleRefType[0,n]
 * 				[attr: id CDATA #IMPLIED  : java.lang.String]
 * 				description <description> : java.lang.String[0,n]
 * 					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 				roleName <role-name> : java.lang.String
 * 				roleLink <role-link> : java.lang.String[0,1]
 * 		customPortletMode <custom-portlet-mode> : CustomPortletModeType[0,n]
 * 			[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			description <description> : java.lang.String[0,n]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			portletMode <portlet-mode> : java.lang.String
 * 		customWindowState <custom-window-state> : CustomWindowStateType[0,n]
 * 			[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			description <description> : java.lang.String[0,n]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			windowState <window-state> : java.lang.String
 * 		userAttribute <user-attribute> : UserAttributeType[0,n]
 * 			[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			description <description> : java.lang.String[0,n]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			name <name> : java.lang.String
 * 		securityConstraint <security-constraint> : SecurityConstraintType[0,n]
 * 			[attr: id CDATA #IMPLIED  : java.lang.String]
 * 			displayName <display-name> : java.lang.String[0,n] 	[whiteSpace (collapse)]
 * 				[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 			portletCollection <portlet-collection> : PortletCollectionType
 * 				portletName <portlet-name> : java.lang.String[1,n]
 * 			userDataConstraint <user-data-constraint> : UserDataConstraintType
 * 				[attr: id CDATA #IMPLIED  : java.lang.String]
 * 				description <description> : java.lang.String[0,n]
 * 					[attr: xml:lang CDATA #IMPLIED  : java.lang.String]
 * 				transportGuarantee <transport-guarantee> : java.lang.String 	[enumeration (NONE), enumeration (INTEGRAL), enumeration (CONFIDENTIAL)]
 *
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PortletApp implements org.netbeans.modules.visualweb.api.portlet.dd.PortletAppInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String VERSION = "Version";	// NOI18N
	public static final String ID = "Id";	// NOI18N
	public static final String PORTLET = "Portlet";	// NOI18N
	public static final String CUSTOM_PORTLET_MODE = "CustomPortletMode";	// NOI18N
	public static final String CUSTOM_WINDOW_STATE = "CustomWindowState";	// NOI18N
	public static final String USER_ATTRIBUTE = "UserAttribute";	// NOI18N
	public static final String SECURITY_CONSTRAINT = "SecurityConstraint";	// NOI18N

	private java.lang.String _Version;
	private java.lang.String _Id;
	private java.util.List _Portlet = new java.util.ArrayList();	// List<PortletType>
	private java.util.List _CustomPortletMode = new java.util.ArrayList();	// List<CustomPortletModeType>
	private java.util.List _CustomWindowState = new java.util.ArrayList();	// List<CustomWindowStateType>
	private java.util.List _UserAttribute = new java.util.ArrayList();	// List<UserAttributeType>
	private java.util.List _SecurityConstraint = new java.util.ArrayList();	// List<SecurityConstraintType>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public PortletApp() {
		_Version = "";
	}

	/**
	 * Required parameters constructor
	 */
	public PortletApp(java.lang.String version) {
		_Version = version;
	}

	/**
	 * Deep copy
	 */
	public PortletApp(org.netbeans.modules.visualweb.api.portlet.dd.PortletApp source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PortletApp(org.netbeans.modules.visualweb.api.portlet.dd.PortletApp source, boolean justData) {
		_Version = source._Version;
		_Id = source._Id;
		for (java.util.Iterator it = source._Portlet.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it.next();
			_Portlet.add((srcElement == null) ? null : newPortletType(srcElement, justData));
		}
		for (java.util.Iterator it = source._CustomPortletMode.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it.next();
			_CustomPortletMode.add((srcElement == null) ? null : newCustomPortletModeType(srcElement, justData));
		}
		for (java.util.Iterator it = source._CustomWindowState.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it.next();
			_CustomWindowState.add((srcElement == null) ? null : newCustomWindowStateType(srcElement, justData));
		}
		for (java.util.Iterator it = source._UserAttribute.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it.next();
			_UserAttribute.add((srcElement == null) ? null : newUserAttributeType(srcElement, justData));
		}
		for (java.util.Iterator it = source._SecurityConstraint.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it.next();
			_SecurityConstraint.add((srcElement == null) ? null : newSecurityConstraintType(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is mandatory
	public void setVersion(java.lang.String value) {
		_Version = value;
	}

	public java.lang.String getVersion() {
		return _Version;
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is an array, possibly empty
	public void setPortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType[] value) {
		if (value == null)
			value = new PortletType[0];
		_Portlet.clear();
		((java.util.ArrayList) _Portlet).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Portlet.add(value[i]);
		}
	}

	public void setPortlet(int index, org.netbeans.modules.visualweb.api.portlet.dd.PortletType value) {
		_Portlet.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType[] getPortlet() {
		PortletType[] arr = new PortletType[_Portlet.size()];
		return (PortletType[]) _Portlet.toArray(arr);
	}

	public java.util.List fetchPortletList() {
		return _Portlet;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType getPortlet(int index) {
		return (PortletType)_Portlet.get(index);
	}

	// Return the number of portlet
	public int sizePortlet() {
		return _Portlet.size();
	}

	public int addPortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType value) {
		_Portlet.add(value);
		int positionOfNewItem = _Portlet.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType value) {
		int pos = _Portlet.indexOf(value);
		if (pos >= 0) {
			_Portlet.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType[] value) {
		if (value == null)
			value = new CustomPortletModeType[0];
		_CustomPortletMode.clear();
		((java.util.ArrayList) _CustomPortletMode).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_CustomPortletMode.add(value[i]);
		}
	}

	public void setCustomPortletMode(int index, org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value) {
		_CustomPortletMode.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType[] getCustomPortletMode() {
		CustomPortletModeType[] arr = new CustomPortletModeType[_CustomPortletMode.size()];
		return (CustomPortletModeType[]) _CustomPortletMode.toArray(arr);
	}

	public java.util.List fetchCustomPortletModeList() {
		return _CustomPortletMode;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType getCustomPortletMode(int index) {
		return (CustomPortletModeType)_CustomPortletMode.get(index);
	}

	// Return the number of customPortletMode
	public int sizeCustomPortletMode() {
		return _CustomPortletMode.size();
	}

	public int addCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value) {
		_CustomPortletMode.add(value);
		int positionOfNewItem = _CustomPortletMode.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value) {
		int pos = _CustomPortletMode.indexOf(value);
		if (pos >= 0) {
			_CustomPortletMode.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType[] value) {
		if (value == null)
			value = new CustomWindowStateType[0];
		_CustomWindowState.clear();
		((java.util.ArrayList) _CustomWindowState).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_CustomWindowState.add(value[i]);
		}
	}

	public void setCustomWindowState(int index, org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value) {
		_CustomWindowState.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType[] getCustomWindowState() {
		CustomWindowStateType[] arr = new CustomWindowStateType[_CustomWindowState.size()];
		return (CustomWindowStateType[]) _CustomWindowState.toArray(arr);
	}

	public java.util.List fetchCustomWindowStateList() {
		return _CustomWindowState;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType getCustomWindowState(int index) {
		return (CustomWindowStateType)_CustomWindowState.get(index);
	}

	// Return the number of customWindowState
	public int sizeCustomWindowState() {
		return _CustomWindowState.size();
	}

	public int addCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value) {
		_CustomWindowState.add(value);
		int positionOfNewItem = _CustomWindowState.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value) {
		int pos = _CustomWindowState.indexOf(value);
		if (pos >= 0) {
			_CustomWindowState.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType[] value) {
		if (value == null)
			value = new UserAttributeType[0];
		_UserAttribute.clear();
		((java.util.ArrayList) _UserAttribute).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_UserAttribute.add(value[i]);
		}
	}

	public void setUserAttribute(int index, org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value) {
		_UserAttribute.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType[] getUserAttribute() {
		UserAttributeType[] arr = new UserAttributeType[_UserAttribute.size()];
		return (UserAttributeType[]) _UserAttribute.toArray(arr);
	}

	public java.util.List fetchUserAttributeList() {
		return _UserAttribute;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType getUserAttribute(int index) {
		return (UserAttributeType)_UserAttribute.get(index);
	}

	// Return the number of userAttribute
	public int sizeUserAttribute() {
		return _UserAttribute.size();
	}

	public int addUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value) {
		_UserAttribute.add(value);
		int positionOfNewItem = _UserAttribute.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value) {
		int pos = _UserAttribute.indexOf(value);
		if (pos >= 0) {
			_UserAttribute.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType[] value) {
		if (value == null)
			value = new SecurityConstraintType[0];
		_SecurityConstraint.clear();
		((java.util.ArrayList) _SecurityConstraint).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_SecurityConstraint.add(value[i]);
		}
	}

	public void setSecurityConstraint(int index, org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value) {
		_SecurityConstraint.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType[] getSecurityConstraint() {
		SecurityConstraintType[] arr = new SecurityConstraintType[_SecurityConstraint.size()];
		return (SecurityConstraintType[]) _SecurityConstraint.toArray(arr);
	}

	public java.util.List fetchSecurityConstraintList() {
		return _SecurityConstraint;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType getSecurityConstraint(int index) {
		return (SecurityConstraintType)_SecurityConstraint.get(index);
	}

	// Return the number of securityConstraint
	public int sizeSecurityConstraint() {
		return _SecurityConstraint.size();
	}

	public int addSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value) {
		_SecurityConstraint.add(value);
		int positionOfNewItem = _SecurityConstraint.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value) {
		int pos = _SecurityConstraint.indexOf(value);
		if (pos >= 0) {
			_SecurityConstraint.remove(pos);
		}
		return pos;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType newPortletType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType newPortletType(PortletType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType newCustomPortletModeType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType newCustomPortletModeType(CustomPortletModeType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType newCustomWindowStateType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType newCustomWindowStateType(CustomWindowStateType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType newUserAttributeType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType newUserAttributeType(UserAttributeType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType newSecurityConstraintType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType newSecurityConstraintType(SecurityConstraintType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType(source, justData);
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "portlet-app", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "portlet-app";
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
		out.write(" xmlns='");	// NOI18N
		out.write("http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd");	// NOI18N
		out.write("'");	// NOI18N
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		// version is an attribute with namespace http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd
		if (_Version != null) {
			out.write(" version='");
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Version, true);
			out.write("'");	// NOI18N
		}
		// id is an attribute with namespace http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd
		if (_Id != null) {
			out.write(" id='");
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Id, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _Portlet.iterator(); it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType element = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it.next();
			if (element != null) {
				element.writeNode(out, "portlet", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _CustomPortletMode.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it.next();
			if (element != null) {
				element.writeNode(out, "custom-portlet-mode", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _CustomWindowState.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it.next();
			if (element != null) {
				element.writeNode(out, "custom-window-state", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _UserAttribute.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it.next();
			if (element != null) {
				element.writeNode(out, "user-attribute", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _SecurityConstraint.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it.next();
			if (element != null) {
				element.writeNode(out, "security-constraint", null, nextIndent, namespaceMap);
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

	public static PortletApp read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static PortletApp read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static PortletApp readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static PortletApp read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static PortletApp read(org.w3c.dom.Document document) {
		PortletApp aPortletApp = new PortletApp();
		aPortletApp.readFromDocument(document);
		return aPortletApp;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
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
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
				it.hasNext(); ) {
				String prefix = (String) it.next();
				String ns = (String) namespacePrefixes.get(prefix);
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("version");
			if (attr != null) {
				attrValue = attr.getValue();
				_Version = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("id");
			if (attr != null) {
				attrValue = attr.getValue();
				_Id = attrValue;
			}
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "portlet") {
				PortletType aPortlet = newPortletType();
				aPortlet.readNode(childNode, namespacePrefixes);
				_Portlet.add(aPortlet);
			}
			else if (childNodeName == "custom-portlet-mode") {
				CustomPortletModeType aCustomPortletMode = newCustomPortletModeType();
				aCustomPortletMode.readNode(childNode, namespacePrefixes);
				_CustomPortletMode.add(aCustomPortletMode);
			}
			else if (childNodeName == "custom-window-state") {
				CustomWindowStateType aCustomWindowState = newCustomWindowStateType();
				aCustomWindowState.readNode(childNode, namespacePrefixes);
				_CustomWindowState.add(aCustomWindowState);
			}
			else if (childNodeName == "user-attribute") {
				UserAttributeType aUserAttribute = newUserAttributeType();
				aUserAttribute.readNode(childNode, namespacePrefixes);
				_UserAttribute.add(aUserAttribute);
			}
			else if (childNodeName == "security-constraint") {
				SecurityConstraintType aSecurityConstraint = newSecurityConstraintType();
				aSecurityConstraint.readNode(childNode, namespacePrefixes);
				_SecurityConstraint.add(aSecurityConstraint);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public static class ValidateException extends Exception {
		private org.netbeans.modules.visualweb.api.portlet.dd.CommonBean failedBean;
		private String failedPropertyName;
		private FailureType failureType;
		public ValidateException(String msg, String failedPropertyName, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean failedBean) {
			super(msg);
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public ValidateException(String msg, FailureType ft, String failedPropertyName, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean failedBean) {
			super(msg);
			this.failureType = ft;
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public String getFailedPropertyName() {return failedPropertyName;}
		public FailureType getFailureType() {return failureType;}
		public org.netbeans.modules.visualweb.api.portlet.dd.CommonBean getFailedBean() {return failedBean;}
		public static class FailureType {
			private final String name;
			private FailureType(String name) {this.name = name;}
			public String toString() { return name;}
			public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
			public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
			public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
			public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
			public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
		}
	}

	public void validate() throws org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property version
		if (getVersion() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getVersion() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "version", this);	// NOI18N
		}
		// Validating property id
		// Validating property portlet
		for (int _index = 0; _index < sizePortlet(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType element = getPortlet(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property customPortletMode
		for (int _index = 0; _index < sizeCustomPortletMode(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element = getCustomPortletMode(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property customWindowState
		for (int _index = 0; _index < sizeCustomWindowState(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element = getCustomWindowState(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property userAttribute
		for (int _index = 0; _index < sizeUserAttribute(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element = getUserAttribute(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property securityConstraint
		for (int _index = 0; _index < sizeSecurityConstraint(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element = getSecurityConstraint(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "version")
			setVersion((java.lang.String)value);
		else if (name == "id")
			setId((java.lang.String)value);
		else if (name == "portlet")
			addPortlet((PortletType)value);
		else if (name == "portlet[]")
			setPortlet((PortletType[]) value);
		else if (name == "customPortletMode")
			addCustomPortletMode((CustomPortletModeType)value);
		else if (name == "customPortletMode[]")
			setCustomPortletMode((CustomPortletModeType[]) value);
		else if (name == "customWindowState")
			addCustomWindowState((CustomWindowStateType)value);
		else if (name == "customWindowState[]")
			setCustomWindowState((CustomWindowStateType[]) value);
		else if (name == "userAttribute")
			addUserAttribute((UserAttributeType)value);
		else if (name == "userAttribute[]")
			setUserAttribute((UserAttributeType[]) value);
		else if (name == "securityConstraint")
			addSecurityConstraint((SecurityConstraintType)value);
		else if (name == "securityConstraint[]")
			setSecurityConstraint((SecurityConstraintType[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PortletApp");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "version")
			return getVersion();
		if (name == "id")
			return getId();
		if (name == "portlet[]")
			return getPortlet();
		if (name == "customPortletMode[]")
			return getCustomPortletMode();
		if (name == "customWindowState[]")
			return getCustomWindowState();
		if (name == "userAttribute[]")
			return getUserAttribute();
		if (name == "securityConstraint[]")
			return getSecurityConstraint();
		throw new IllegalArgumentException(name+" is not a valid property name for PortletApp");
	}

	public String nameSelf() {
		return "/PortletApp";
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
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Version) {
				if (returnConstName) {
					return VERSION;
				} else if (returnSchemaName) {
					return "version";
				} else if (returnXPathName) {
					return "@version";
				} else {
					return "Version";
				}
			}
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
		}
		if (childObj instanceof SecurityConstraintType) {
			SecurityConstraintType child = (SecurityConstraintType) childObj;
			int index = 0;
			for (java.util.Iterator it = _SecurityConstraint.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it.next();
				if (child == element) {
					if (returnConstName) {
						return SECURITY_CONSTRAINT;
					} else if (returnSchemaName) {
						return "security-constraint";
					} else if (returnXPathName) {
						return "security-constraint[position()="+index+"]";
					} else {
						return "SecurityConstraint."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof CustomWindowStateType) {
			CustomWindowStateType child = (CustomWindowStateType) childObj;
			int index = 0;
			for (java.util.Iterator it = _CustomWindowState.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it.next();
				if (child == element) {
					if (returnConstName) {
						return CUSTOM_WINDOW_STATE;
					} else if (returnSchemaName) {
						return "custom-window-state";
					} else if (returnXPathName) {
						return "custom-window-state[position()="+index+"]";
					} else {
						return "CustomWindowState."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof PortletType) {
			PortletType child = (PortletType) childObj;
			int index = 0;
			for (java.util.Iterator it = _Portlet.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.PortletType element = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it.next();
				if (child == element) {
					if (returnConstName) {
						return PORTLET;
					} else if (returnSchemaName) {
						return "portlet";
					} else if (returnXPathName) {
						return "portlet[position()="+index+"]";
					} else {
						return "Portlet."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof UserAttributeType) {
			UserAttributeType child = (UserAttributeType) childObj;
			int index = 0;
			for (java.util.Iterator it = _UserAttribute.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it.next();
				if (child == element) {
					if (returnConstName) {
						return USER_ATTRIBUTE;
					} else if (returnSchemaName) {
						return "user-attribute";
					} else if (returnXPathName) {
						return "user-attribute[position()="+index+"]";
					} else {
						return "UserAttribute."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof CustomPortletModeType) {
			CustomPortletModeType child = (CustomPortletModeType) childObj;
			int index = 0;
			for (java.util.Iterator it = _CustomPortletMode.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it.next();
				if (child == element) {
					if (returnConstName) {
						return CUSTOM_PORTLET_MODE;
					} else if (returnSchemaName) {
						return "custom-portlet-mode";
					} else if (returnXPathName) {
						return "custom-portlet-mode[position()="+index+"]";
					} else {
						return "CustomPortletMode."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _Portlet.iterator(); it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType element = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _CustomPortletMode.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _CustomWindowState.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _UserAttribute.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _SecurityConstraint.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PortletApp && equals((org.netbeans.modules.visualweb.api.portlet.dd.PortletApp) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PortletApp inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Version == null ? inst._Version == null : _Version.equals(inst._Version))) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (sizePortlet() != inst.sizePortlet())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Portlet.iterator(), it2 = inst._Portlet.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType element = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.PortletType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.PortletType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeCustomPortletMode() != inst.sizeCustomPortletMode())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _CustomPortletMode.iterator(), it2 = inst._CustomPortletMode.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeCustomWindowState() != inst.sizeCustomWindowState())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _CustomWindowState.iterator(), it2 = inst._CustomWindowState.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeUserAttribute() != inst.sizeUserAttribute())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _UserAttribute.iterator(), it2 = inst._UserAttribute.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeSecurityConstraint() != inst.sizeSecurityConstraint())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _SecurityConstraint.iterator(), it2 = inst._SecurityConstraint.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Version == null ? 0 : _Version.hashCode());
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Portlet == null ? 0 : _Portlet.hashCode());
		result = 37*result + (_CustomPortletMode == null ? 0 : _CustomPortletMode.hashCode());
		result = 37*result + (_CustomWindowState == null ? 0 : _CustomWindowState.hashCode());
		result = 37*result + (_UserAttribute == null ? 0 : _UserAttribute.hashCode());
		result = 37*result + (_SecurityConstraint == null ? 0 : _SecurityConstraint.hashCode());
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


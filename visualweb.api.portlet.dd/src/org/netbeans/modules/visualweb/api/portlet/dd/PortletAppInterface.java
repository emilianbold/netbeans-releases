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

public interface PortletAppInterface {
	public void setVersion(java.lang.String value);

	public java.lang.String getVersion();

	public void setId(java.lang.String value);

	public java.lang.String getId();

	public void setPortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType[] value);

	public void setPortlet(int index, org.netbeans.modules.visualweb.api.portlet.dd.PortletType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType[] getPortlet();

	public java.util.List fetchPortletList();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType getPortlet(int index);

	public int sizePortlet();

	public int addPortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType value);

	public int removePortlet(org.netbeans.modules.visualweb.api.portlet.dd.PortletType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType newPortletType();

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletType newPortletType(PortletType source, boolean justData);

	public void setCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType[] value);

	public void setCustomPortletMode(int index, org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType[] getCustomPortletMode();

	public java.util.List fetchCustomPortletModeList();

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType getCustomPortletMode(int index);

	public int sizeCustomPortletMode();

	public int addCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value);

	public int removeCustomPortletMode(org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType newCustomPortletModeType();

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomPortletModeType newCustomPortletModeType(CustomPortletModeType source, boolean justData);

	public void setCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType[] value);

	public void setCustomWindowState(int index, org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType[] getCustomWindowState();

	public java.util.List fetchCustomWindowStateList();

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType getCustomWindowState(int index);

	public int sizeCustomWindowState();

	public int addCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value);

	public int removeCustomWindowState(org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType newCustomWindowStateType();

	public org.netbeans.modules.visualweb.api.portlet.dd.CustomWindowStateType newCustomWindowStateType(CustomWindowStateType source, boolean justData);

	public void setUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType[] value);

	public void setUserAttribute(int index, org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType[] getUserAttribute();

	public java.util.List fetchUserAttributeList();

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType getUserAttribute(int index);

	public int sizeUserAttribute();

	public int addUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value);

	public int removeUserAttribute(org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType newUserAttributeType();

	public org.netbeans.modules.visualweb.api.portlet.dd.UserAttributeType newUserAttributeType(UserAttributeType source, boolean justData);

	public void setSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType[] value);

	public void setSecurityConstraint(int index, org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType[] getSecurityConstraint();

	public java.util.List fetchSecurityConstraintList();

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType getSecurityConstraint(int index);

	public int sizeSecurityConstraint();

	public int addSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value);

	public int removeSecurityConstraint(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType value);

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType newSecurityConstraintType();

	public org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType newSecurityConstraintType(SecurityConstraintType source, boolean justData);

}

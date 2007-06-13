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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;

/**
 *
 * @author Andrei Badea
 */
public class ServletImpl implements Servlet {
    
    private final String servletName;
    private final String servletClass;
    private final RunAs runAs;
    
    public ServletImpl(String servletName, String servletClass, RunAs runAs) {
        this.servletName = servletName;
        this.servletClass = servletClass;
        this.runAs = runAs;
    }
            
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addInitParam(InitParam valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int addSecurityRoleRef(SecurityRoleRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InitParam getInitParam(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InitParam[] getInitParam() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getJspFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigInteger getLoadOnStartup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RunAs getRunAs() {
        return runAs;
    }

    public SecurityRoleRef getSecurityRoleRef(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getServletClass() {
        return servletClass;
    }

    public String getServletName() {
        return servletName;
    }

    public int removeInitParam(InitParam valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int removeSecurityRoleRef(SecurityRoleRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInitParam(int index, InitParam valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setInitParam(InitParam[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setJspFile(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoadOnStartup(BigInteger value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRunAs(RunAs valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRoleRef(int index, SecurityRoleRef valueInterface) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSecurityRoleRef(SecurityRoleRef[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServletClass(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setServletName(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeInitParam() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int sizeSecurityRoleRef() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDefaultDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

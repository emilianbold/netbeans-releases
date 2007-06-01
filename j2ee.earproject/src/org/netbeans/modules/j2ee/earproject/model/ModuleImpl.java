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

package org.netbeans.modules.j2ee.earproject.model;

import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 * Default implementation of {@link Module} for {@link Application}.
 * @author Tomas Mysik
 * @see ApplicationImpl
 */
public class ModuleImpl implements Module {
    private final String connector;
    private final String ejb;
    private final String car;
    private final Web web;

    /**
     * Constructor with all properties.
     * @param connector module connector.
     * @param ejb not <code>null</code> for EJB module.
     * @param car not <code>null</code> for Application Client module.
     * @param web not <code>null</code> for Web module.
     */
    public ModuleImpl(final String connector, final String ejb, final String car, final Web web) {
        this.connector = connector;
        this.ejb = ejb;
        this.car = car;
        this.web = web;
    }

    public void setConnector(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getConnector() {
        return connector;
    }

    public void setConnectorId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getConnectorId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setEjb(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getEjb() {
        return ejb;
    }

    public void setEjbId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getEjbId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setJava(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getJava() {
        return car;
    }

    public void setJavaId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getJavaId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setWeb(Web value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Web getWeb() {
        return web;
    }

    public Web newWeb() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setAltDd(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getAltDd() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setAltDdId(String value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getAltDdId() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        if (connector != null) {
            sb.append(" Module connector: ");
            sb.append(connector);
            sb.append(newLine);
        }

        if (ejb != null) {
            sb.append(" EJB module: ");
            sb.append(ejb);
            sb.append(newLine);
        }

        if (car != null) {
            sb.append(" Application Client module: ");
            sb.append(car);
            sb.append(newLine);
        }

        if (web != null) {
            sb.append(" Web module: ");
            sb.append(web);
            sb.append(newLine);
        }

        sb.append("}");
        return sb.toString();
    }
}
